package org.jboss.brms.test.service;

import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.service.metrics.MetricsCollector;

@Singleton
public class ProcessService {
    @Inject
    private Logger log;

    @EJB
    private GuvnorService guvnorService;

    @EJB
    private ProcessInstanceRunner runner;

    public Metrics runInstance(final String packageName, final String processId, final Map<String, Object> parms) {
        log.info("Running process instance for process " + processId + " contained in package " + packageName);

        // If found, use a knowledge builder with corresponding change set to create a knowledge base.
        final KnowledgeBase kbase = guvnorService.retrieveKnowledgeBaseFromGuvnor(packageName);

        // Create and start the process instance from the knowledge base.
        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        final ProcessStartParameters parameters = new ProcessStartParameters();
        parameters.setStartInParallel(false);
        parameters.setRunInIndividualKnowledgeSession(false);
        final MetricsCollector collector = new MetricsCollector(parameters);
        collector.startTest();
        ksession.startProcess(processId, parms);
        ksession.fireAllRules();
        collector.endTest();

        return collector.getMetrics();
    }

    public Metrics runProcesses(final ProcessStartParameters parameters) {
        log.info("Running process instance for process " + parameters.getProcessId() + " contained in package " + parameters.getPackageName());

        // If found, use a knowledge builder with corresponding change set to create a knowledge base.
        final KnowledgeBase kbase = guvnorService.retrieveKnowledgeBaseFromGuvnor(parameters.getPackageName());

        // Run the test as indicated.
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        final MetricsCollector collector = new MetricsCollector(parameters);
        collector.addSession(ksession);
        collector.startTest();

        for (int index = 0; index < parameters.getNumberOfInstances(); ++index) {
            if (parameters.isRunInIndividualKnowledgeSession()) {
                ksession = kbase.newStatefulKnowledgeSession();
                collector.addSession(ksession);
            }
            if (parameters.isStartInParallel()) {
                runner.runAsync(ksession, parameters.getProcessId());
            } else {
                runner.runSync(ksession, parameters.getProcessId());
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
