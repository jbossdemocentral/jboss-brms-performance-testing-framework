package org.jbpm.rewards;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;
import org.drools.KnowledgeBase;
import org.drools.builder.ResourceType;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.AccessType;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.test.JbpmJUnitTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This is a sample file to launch a process.
 */
public class RewardsProcessTest extends JbpmJUnitTestCase {
    private static final boolean USE_RESOURCES_FROM_GUVNOR = false;
    private static final String GUVNOR_URL = "http://localhost:8080/jboss-brms";
    private static final String GUVNOR_USER_NAME = "admin";
    private static final String GUVNOR_PASSWORD = "admin";
    private static final String[] GUVNOR_PACKAGES = { "org.jbpm.rewards" };

    private static final String LOCAL_PROCESS_NAME = "rewardsapprovalextended.bpmn2";

    private StatefulKnowledgeSession ksession;
    private TaskService taskService;

    public RewardsProcessTest() {
        super(true);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Set up the knowledge session with the process.
        KnowledgeBase kbase = null;
        if (USE_RESOURCES_FROM_GUVNOR) {
            kbase = createKnowledgeBaseGuvnor(false, GUVNOR_URL, GUVNOR_USER_NAME, GUVNOR_PASSWORD, GUVNOR_PACKAGES);
        } else {
            // Use the local files.
            final Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
            resources.put(LOCAL_PROCESS_NAME, ResourceType.BPMN2);
            kbase = createKnowledgeBase(resources);
        }
        ksession = createKnowledgeSession(kbase);
        taskService = getTaskService(ksession);

        // Register human task work item.
        final SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(taskService, ksession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);

        // Register other (default!) work items.
        ksession.getWorkItemManager().registerWorkItemHandler("Log", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email", new SystemOutWorkItemHandler());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        ksession.dispose();

        super.tearDown();
    }

    @Test
    public void rewardApprovedTest() {
        // Add collector for the metrics.
        // final MetricsCollector collector = new MetricsCollector(MockFactory.createMetricsFactory(), ksession);

        // Start process instance.
        // collector.startTest();
        final ProcessInstance processInstance = ksession.startProcess("org.jbpm.approval.rewards.extended");

        // Retrieve and execute task with approval.
        final List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("mary", new ArrayList<String>(), "en-UK");
        final TaskSummary task = list.get(0); // Only 1 task in the process!
        taskService.claim(task.getId(), "mary", new ArrayList<String>());
        taskService.start(task.getId(), "mary");

        final ContentData content = new ContentData();
        content.setAccessType(AccessType.Inline);
        final Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("Explanation", "Great work");
        taskParams.put("Outcome", "Approved");
        content.setContent(SerializationUtils.serialize((Serializable) taskParams));
        taskService.complete(task.getId(), "mary", content);

        // Capture end of test run.
        // collector.endTest();

        // Test for completion and in correct end node.
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "End Approved");

        // Print metrics.
        // System.out.println(collector.getMetrics().printAll());
    }

    @Test
    public void rewardRejectedTest() {
        // Add collector for the metrics.
        // final MetricsCollector collector = new MetricsCollector(MockFactory.createMetricsFactory(), ksession);

        // Start process instance.
        // collector.startTest();
        final ProcessInstance processInstance = ksession.startProcess("org.jbpm.approval.rewards.extended");

        // Retrieve and execute task with rejection.
        final List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("john", new ArrayList<String>(), "en-UK");
        final TaskSummary task = list.get(0); // Only 1 task in the process!
        taskService.claim(task.getId(), "john", new ArrayList<String>());
        taskService.start(task.getId(), "john");

        final ContentData content = new ContentData();
        content.setAccessType(AccessType.Inline);
        final Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("Explanation", "Too complicated for me");
        taskParams.put("Outcome", "Rejected");
        content.setContent(SerializationUtils.serialize((Serializable) taskParams));
        taskService.complete(task.getId(), "john", content);

        // Capture end of test run.
        // collector.endTest();

        // Test for completion and in correct end node.
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "End Rejected");

        // Print metrics.
        // System.out.println(collector.getMetrics().printAll());
    }
}
