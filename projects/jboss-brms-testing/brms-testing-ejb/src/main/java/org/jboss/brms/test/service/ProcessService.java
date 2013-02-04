package org.jboss.brms.test.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.util.Resources.GuvnorConfig;

@Stateless
public class ProcessService {
    private static final String CHANGESET_PATTERN = "<change-set xmlns=\"http://drools.org/drools-5.0/change-set\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " xs:schemaLocation=\"http://drools.org/drools-5.0/change-set http://anonsvn.jboss.org/repos/labs/labs/jbossrules/trunk/drools-api/src/main/resources/change-set-1.0.0.xsd\">"
            + "<add>{0}</add></change-set>";
    private static final String RESOURCE_PATTERN = "<resource source=\"{0}/rest/packages/{1}/binary\" type=\"PKG\" basicAuthentication=\"enabled\" username=\"{2}\" password=\"{3}\" />";

    private static final String GUVNOR_CONFIG_URL = "guvnor.url";
    private static final String GUVNOR_CONFIG_USER_NAME = "guvnor.user";
    private static final String GUVNOR_CONFIG_PASSWORD = "guvnor.password";

    @Inject
    @GuvnorConfig
    private Properties guvnorConfig;

    @Inject
    private Logger log;

    public Metrics runInstance(final String packageName, final String processId, final Map<String, Object> parms) {
        log.info("Running process instance for process " + processId + " contained in package " + packageName);

        // If found, use a knowledge builder with corresponding change set to create a knowledge base.
        final KnowledgeBase kbase = retrieveKnowledgeBaseFromGuvnor(packageName);

        // Create and start the process instance from the knowledge base.
        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        final MetricEventListener eventListener = new MetricEventListener();
        ksession.addEventListener(eventListener);
        eventListener.getMetrics().setStartingTime(new Date());
        ksession.startProcess(processId, parms);
        ksession.fireAllRules();
        eventListener.getMetrics().setEndingTime(new Date());

        return eventListener.getMetrics();
    }

    /**
     * Creates a knowledge base using a change set containing the given package(s).
     * 
     * @param packageNames
     *            The package name(s) to be included in the knowledge base.
     * @return The knowledge base.
     */
    private KnowledgeBase retrieveKnowledgeBaseFromGuvnor(final String... packageNames) {
        final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        final List<String> resources = new ArrayList<String>();
        for (final String packageName : packageNames) {
            resources.add(MessageFormat.format(RESOURCE_PATTERN, guvnorConfig.getProperty(GUVNOR_CONFIG_URL), packageName,
                    guvnorConfig.getProperty(GUVNOR_CONFIG_USER_NAME), guvnorConfig.getProperty(GUVNOR_CONFIG_PASSWORD)));
        }
        final String changeSet = MessageFormat.format(CHANGESET_PATTERN, resources.toArray());
        kbuilder.add(ResourceFactory.newByteArrayResource(changeSet.getBytes()), ResourceType.CHANGE_SET);
        return kbuilder.newKnowledgeBase();
    }
}
