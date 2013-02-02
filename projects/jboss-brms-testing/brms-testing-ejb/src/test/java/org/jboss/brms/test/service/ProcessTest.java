package org.jboss.brms.test.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.builder.ResourceType;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jboss.brms.test.model.MeasuredProcess;
import org.jboss.brms.test.model.MeasuredProcessInstance;
import org.jbpm.test.JbpmJUnitTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This is a sample file to test a process.
 */
public class ProcessTest extends JbpmJUnitTestCase {
    private static final boolean USE_RESOURCES_FROM_GUVNOR = false;
    private static final String GUVNOR_URL = "http://localhost:8080/jboss-brms";
    private static final String GUVNOR_USER_NAME = "admin";
    private static final String GUVNOR_PASSWORD = "admin";
    private static final String[] GUVNOR_PACKAGES = { "org.jbpm.evaluation.customer" };

    private static final String LOCAL_PROCESS_NAME = "customereval.bpmn2";
    private static final String LOCAL_RULES_NAME = "financerules.drl";

    private static StatefulKnowledgeSession ksession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Set up the knowledge session with the process and handlers.
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
        final Map<String, Object> parms = new HashMap<String, Object>();

        System.out.println("=======================================================");
        System.out.println("= Starting Process Invalid (empty request) Test Case. =");
        System.out.println("=======================================================");

        final MetricEventListener eventListener = new MetricEventListener();
        ksession.addEventListener(eventListener);
        eventListener.getMetrics().setStartingTime(new Date());

        final ProcessInstance processInstance = ksession.startProcess("org.jbpm.customer-evaluation", parms);
        ksession.fireAllRules();

        eventListener.getMetrics().setEndingTime(new Date());
        // Check whether the process instance has completed successfully.
        assertProcessInstanceCompleted(processInstance.getId(), ksession);

        // Print metrics.
        System.out.println(eventListener.getMetrics().print());
        for (final MeasuredProcess mp : eventListener.getMetrics().getProcesses()) {
            System.out.println(mp.print());
            for (final MeasuredProcessInstance mpi : mp.getInstances()) {
                System.out.println(mpi.print());
            }
        }
    }
}
