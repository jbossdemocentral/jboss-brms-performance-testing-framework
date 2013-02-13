package org.jboss.brms.test.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.SystemEventListenerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.service.metrics.MetricsCollector;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.task.AccessType;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.hornetq.CommandBasedHornetQWSHumanTaskHandler;
import org.jbpm.task.service.hornetq.HornetQTaskClientConnector;
import org.jbpm.task.service.hornetq.HornetQTaskClientHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskOperationResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskSummaryResponseHandler;

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
        log.info("Running process instance for process " + parameters.getProcessId() + " contained in package " + parameters.getPackageName());

        // If found, use a knowledge builder with corresponding change set to create a knowledge base.
        final KnowledgeBase kbase = guvnorService.retrieveKnowledgeBaseFromGuvnor(parameters.getPackageName());

        // Run the test as indicated.
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        final MetricsCollector collector = new MetricsCollector(metricsService, parameters);
        collector.addSession(ksession);
        collector.startTest();

        for (int index = 0; index < parameters.getNumberOfInstances(); ++index) {
            if (parameters.isRunInIndividualKnowledgeSession()) {
                ksession = kbase.newStatefulKnowledgeSession();
                collector.addSession(ksession);
            }
            if (parameters.isStartInParallel()) {
                runner.runAsync(ksession, parameters.getProcessId(), parameters.isRunInIndividualKnowledgeSession());
            } else {
                runner.runSync(ksession, parameters.getProcessId(), parameters.isRunInIndividualKnowledgeSession());
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

    public Metrics runCustomerEvaluationProcess() {
        final KnowledgeBase kbase = guvnorService.retrieveKnowledgeBaseFromGuvnor("org.jbpm.evaluation.customer");
        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        final MetricsCollector collector = new MetricsCollector(metricsService, ksession);

        collector.startTest();
        runner.runSync(ksession, "org.jbpm.customer-evaluation", false);
        collector.endTest();

        ksession.dispose();
        return collector.getMetrics();
    }

    /* This breaks at the part that retrieves the tasks: list has size 0. */
    public Metrics runRewardsProcess() {
        final KnowledgeBase kbase = guvnorService.retrieveKnowledgeBaseFromGuvnor("org.jbpm.rewards");
        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        final TaskClient taskClient = getTaskClient(ksession);
        ksession.getWorkItemManager().registerWorkItemHandler("Log", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email", new SystemOutWorkItemHandler());
        final MetricsCollector collector = new MetricsCollector(metricsService, ksession);

        collector.startTest();
        runner.runSync(ksession, "org.jbpm.approval.rewards.extended", false);
        performTask(taskClient);
        collector.endTest();

        ksession.dispose();
        return collector.getMetrics();
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

    private void performTask(final TaskClient taskClient) {
        // Get task.
        final BlockingTaskSummaryResponseHandler taskSummaryHandler = new BlockingTaskSummaryResponseHandler();
        taskClient.getTasksAssignedAsPotentialOwner("peter", "en-UK", taskSummaryHandler);
        final TaskSummary task = taskSummaryHandler.getResults().get(0);

        // Complete task.
        final ContentData content = new ContentData();
        content.setAccessType(AccessType.Inline);
        final Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("Explanation", "Great work");
        taskParams.put("Outcome", "Approved");
        content.setContent(SerializationUtils.serialize((Serializable) taskParams));
        final BlockingTaskOperationResponseHandler taskOperationHandler = new BlockingTaskOperationResponseHandler();
        taskClient.complete(task.getId(), "peter", content, taskOperationHandler);
        taskOperationHandler.waitTillDone(1000L);
    }
}
