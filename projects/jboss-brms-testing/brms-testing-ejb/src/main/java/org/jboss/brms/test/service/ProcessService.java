package org.jboss.brms.test.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.SystemEventListenerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.service.ProcessStartParameters.ProcessIndicator;
import org.jboss.brms.test.service.metrics.MetricsCollector;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.hornetq.CommandBasedHornetQWSHumanTaskHandler;
import org.jbpm.task.service.hornetq.HornetQTaskClientConnector;
import org.jbpm.task.service.hornetq.HornetQTaskClientHandler;

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
        final List<String> packageList = new ArrayList<String>();
        for (final ProcessIndicator indicator : parameters.getIndicators()) {
            log.info("Running " + indicator.getNumberOfInstances() + " process instance(s) for process " + indicator.getProcessId() + " contained in package "
                    + indicator.getPackageName());
            packageList.add(indicator.getPackageName());
        }
        // Use a knowledge builder with corresponding change set to create a single knowledge base.
        final KnowledgeBase kbase = guvnorService.retrieveKnowledgeBaseFromGuvnor(packageList.toArray(new String[0]));

        final MetricsCollector collector = new MetricsCollector(metricsService, parameters);
        final Long metricsId = collector.getMetricsId();

        collector.startTest();
        for (final ProcessIndicator indicator : parameters.getIndicators()) {
            // Run the test as indicated.
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
            collector.addSession(ksession);
            TaskClient taskClient = getTaskClient(ksession);

            for (int index = 0; index < indicator.getNumberOfInstances(); ++index) {
                if (parameters.isRunInIndividualKnowledgeSession()) {
                    ksession = kbase.newStatefulKnowledgeSession();
                    collector.addSession(ksession);
                    taskClient = getTaskClient(ksession);
                }
                if (parameters.isStartInParallel()) {
                    runner.runAsync(ksession, indicator.getProcessId(), parameters.isRunInIndividualKnowledgeSession(), taskClient);
                } else {
                    runner.runSync(ksession, indicator.getProcessId(), parameters.isRunInIndividualKnowledgeSession(), taskClient);
                }
            }
        }
        metricsService.waitForTestToEnd(parameters, metricsId);

        return metricsId;
    }

    private TaskClient getTaskClient(final StatefulKnowledgeSession ksession) {
        final TaskClient client = new TaskClient(new HornetQTaskClientConnector("HornetQConnector" + UUID.randomUUID(), new HornetQTaskClientHandler(
                SystemEventListenerFactory.getSystemEventListener())));
        client.connect("127.0.0.1", 5153);
        final CommandBasedHornetQWSHumanTaskHandler handler = new CommandBasedHornetQWSHumanTaskHandler(ksession);
        handler.setClient(client);
        handler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        return client;
    }
}
