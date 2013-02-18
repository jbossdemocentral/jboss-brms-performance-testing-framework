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

    public Metrics runProcesses(final ProcessStartParameters parameters) {
        for (final ProcessIndicator indicator : parameters.getIndicators()) {
            log.info("Running " + indicator.getNumberOfInstances() + " process instance(s) for process " + indicator.getProcessId() + " contained in package "
                    + indicator.getPackageName());
        }

        final MetricsCollector collector = new MetricsCollector(metricsService, parameters);
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

        // TODO: Use polling to get there.
        try {
            Thread.sleep(5000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        collector.endTest();

        return collector.getMetrics();
    }
}
