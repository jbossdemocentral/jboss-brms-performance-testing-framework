package org.jboss.brms.test.service;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.service.ProcessStartParameters.ProcessIndicator;
import org.jboss.brms.test.service.metrics.MetricsCollector;

@Singleton
public class ProcessService {
    @Inject
    EntityManager em;
    @Inject
    private Logger log;

    @EJB
    private GuvnorService guvnorService;
    @EJB
    private MetricsService metricsService;
    @EJB
    private ProcessInstanceRunner runner;

    /**
     * Run instances for the processes given in the parameters, as indicated.
     * 
     * @param parameters
     *            Indicate how many instances of which process are to be run, in which manner.
     * @return The (database) ID of the {@link Metrics} object that collects all metrics for this test run.
     */
    public Long runProcesses(final ProcessStartParameters parameters) {
        for (final ProcessIndicator indicator : parameters.getIndicators()) {
            log.info("Running " + indicator.getNumberOfInstances() + " process instance(s) for process " + indicator.getProcessId() + " contained in package "
                    + indicator.getPackageName());
        }

        final MetricsCollector collector = new MetricsCollector(metricsService, parameters);
        final Long metricsId = collector.getMetricsId();

        collector.startTest();
        for (final ProcessIndicator indicator : parameters.getIndicators()) {
            // If found, use a knowledge builder with corresponding change set to create a knowledge base.
            final KnowledgeBase kbase = guvnorService.retrieveKnowledgeBaseFromGuvnor(indicator.getPackageName());

            // Run the test as indicated.
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
            collector.addSession(ksession);

            for (int index = 0; index < indicator.getNumberOfInstances(); ++index) {
                if (parameters.isRunInIndividualKnowledgeSession()) {
                    ksession = kbase.newStatefulKnowledgeSession();
                    collector.addSession(ksession);
                }
                if (parameters.isStartInParallel()) {
                    runner.runAsync(ksession, indicator.getProcessId(), parameters.isRunInIndividualKnowledgeSession());
                } else {
                    runner.runSync(ksession, indicator.getProcessId(), parameters.isRunInIndividualKnowledgeSession());
                }
            }
        }
        metricsService.waitForTestToEnd(parameters, metricsId);

        return metricsId;
    }
}
