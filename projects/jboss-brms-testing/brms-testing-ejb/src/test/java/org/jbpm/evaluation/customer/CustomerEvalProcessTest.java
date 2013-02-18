package org.jbpm.evaluation.customer;

import java.util.HashMap;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.builder.ResourceType;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jboss.brms.test.service.ProcessInstanceRunner;
import org.jbpm.test.JbpmJUnitTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This is a sample file to test a process.
 */
public class CustomerEvalProcessTest extends JbpmJUnitTestCase {
    private static final boolean USE_RESOURCES_FROM_GUVNOR = false;
    private static final String GUVNOR_URL = "http://localhost:8080/jboss-brms";
    private static final String GUVNOR_USER_NAME = "admin";
    private static final String GUVNOR_PASSWORD = "admin";
    private static final String[] GUVNOR_PACKAGES = { "org.jbpm.evaluation.customer" };

    private static final String LOCAL_PROCESS_NAME = "customereval.bpmn2";
    private static final String LOCAL_RULES_NAME = "financerules.drl";

    private StatefulKnowledgeSession ksession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Set up the knowledge session with the process and rules.
        KnowledgeBase kbase = null;
        if (USE_RESOURCES_FROM_GUVNOR) {
            kbase = createKnowledgeBaseGuvnor(false, GUVNOR_URL, GUVNOR_USER_NAME, GUVNOR_PASSWORD, GUVNOR_PACKAGES);
        } else {
            // Use the local files.
            final Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
            resources.put(LOCAL_PROCESS_NAME, ResourceType.BPMN2);
            resources.put(LOCAL_RULES_NAME, ResourceType.DRL);
            kbase = createKnowledgeBase(resources);
        }
        ksession = createKnowledgeSession(kbase);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        ksession.dispose();

        super.tearDown();
    }

    @Test
    public void emptyRequest() {
        // Add collector for the metrics.
        // final MetricsCollector collector = new MetricsCollector(MockFactory.createMetricsFactory(), ksession);

        // Start process instance.
        // collector.startTest();
        final ProcessInstance processInstance = ksession.startProcess("org.jbpm.customer-evaluation");
        ksession.fireAllRules();

        // Capture end of test run.
        // collector.endTest();

        // Check whether the process instance has completed successfully.
        assertProcessInstanceCompleted(processInstance.getId(), ksession);

        // Print metrics.
        // System.out.println(collector.getMetrics().printAll());
    }

    @Test
    public void richAdultRequest() {
        // Prepare for an adult, rich customer.
        final Map<String, Object> params = new HashMap<String, Object>();
        final Person person = new Person("pid", "John Doe");
        person.setAge(21);
        params.put("person", person);
        final Request request = new Request("rid");
        request.setPersonId(person.getId());
        request.setAmount(5000);
        params.put("request", request);

        // Add collector for the metrics.
        // final MetricsCollector collector = new MetricsCollector(new MetricsService(), ksession);

        // Start process instance.
        // collector.startTest();
        final ProcessInstance processInstance = ksession.startProcess("org.jbpm.customer-evaluation", params);
        ksession.fireAllRules();

        // Capture end of test run.
        // collector.endTest();

        // Check whether the process instance has completed successfully.
        assertProcessInstanceCompleted(processInstance.getId(), ksession);

        // Print metrics.
        // System.out.println(collector.getMetrics().printAll());
    }

    @Test
    public void dataProviderTest() {
        // Run the process using the data provider (which has the Rich Customer scenario).
        new ProcessInstanceRunner().runSync(ksession, "org.jbpm.customer-evaluation", false);

        // Process instance ID = 0, as each test has its own session.
        assertProcessInstanceCompleted(0, ksession);
        assertNodeTriggered(0, "End Rich Customer");
    }
}
