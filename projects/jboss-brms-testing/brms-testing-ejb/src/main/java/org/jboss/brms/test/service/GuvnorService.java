package org.jboss.brms.test.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.jboss.brms.test.guvnor.Assets;
import org.jboss.brms.test.guvnor.Packages;
import org.jboss.brms.test.service.ProcessStartParameters.ProcessIndicator;
import org.jboss.brms.test.util.GuvnorRestUtil;
import org.jboss.brms.test.util.Resources.GuvnorConfig;
import org.jboss.brms.test.util.XPathUtil;

@Stateless
public class GuvnorService {
    private static final String CHANGESET_PATTERN = "<change-set xmlns=\"http://drools.org/drools-5.0/change-set\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " xs:schemaLocation=\"http://drools.org/drools-5.0/change-set http://anonsvn.jboss.org/repos/labs/labs/jbossrules/trunk/drools-api/src/main/resources/change-set-1.0.0.xsd\">"
            + "<add>{0}</add></change-set>";
    private static final String RESOURCE_PATTERN = "<resource source=\"{0}/rest/packages/{1}/binary\" type=\"PKG\" basicAuthentication=\"enabled\" username=\"{2}\" password=\"{3}\" />";

    private static final String GUVNOR_CONFIG_URL = "guvnor.url";
    private static final String GUVNOR_CONFIG_USER_NAME = "guvnor.user";
    private static final String GUVNOR_CONFIG_PASSWORD = "guvnor.password";

    private static final String GUVNOR_API_PACKAGES_PATH = "/packages";
    private static final String GUVNOR_API_ASSETS_PATH = GUVNOR_API_PACKAGES_PATH + "/{0}/assets";
    private static final String GUVNOR_API_BPMN_SOURCE_PATH = GUVNOR_API_ASSETS_PATH + "/{1}/source";

    @Inject
    @GuvnorConfig
    private Properties guvnorConfig;

    @Inject
    private Logger log;

    /**
     * Creates a knowledge base using a change set containing the given package(s).
     * 
     * @param packageNames
     *            The package name(s) to be included in the knowledge base.
     * @return The knowledge base.
     */
    public KnowledgeBase retrieveKnowledgeBaseFromGuvnor(final String... packageNames) {
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

    /**
     * Retrieve the available (BPMN2) processes from Guvnor.
     * 
     * @return A list of the available package-process ID tuples.
     */
    public List<ProcessIndicator> getProcessesFromGuvnor() {
        final List<ProcessIndicator> processes = new ArrayList<ProcessStartParameters.ProcessIndicator>();

        // Call Guvnor to retrieve the available packages.
        final Packages packages = getFromGuvnor(GUVNOR_API_PACKAGES_PATH, Packages.class);
        for (final org.jboss.brms.test.guvnor.Packages.Package pakkage : packages.getPackage()) {
            // Call Guvnor to retrieve the available assets.
            final Assets assets = getFromGuvnor(MessageFormat.format(GUVNOR_API_ASSETS_PATH, pakkage.getTitle()), Assets.class);
            if ((assets == null) || (assets.getAsset() == null) || assets.getAsset().isEmpty()) {
                log.info("Currently no assets for package " + pakkage.getTitle() + " in Guvnor!");
            } else {
                for (final org.jboss.brms.test.guvnor.Assets.Asset asset : assets.getAsset()) {
                    final String assetName = asset.getRefLink().substring(asset.getRefLink().lastIndexOf("/") + 1);
                    final String format = asset.getMetadata().getFormat();
                    if ("bpmn2".equals(format)) {
                        // Call Guvnor for the BPMN definition (to get the process ID from).
                        final String bpmn = getFromGuvnor(MessageFormat.format(GUVNOR_API_BPMN_SOURCE_PATH, pakkage.getTitle(), assetName),
                                MediaType.TEXT_PLAIN_TYPE);
                        final String processId = XPathUtil.getProcessIdFromBpmn(bpmn);
                        processes.add(new ProcessIndicator(pakkage.getTitle(), processId, 0));
                    }
                }
            }
        }

        return processes;
    }

    public boolean isProcessAvailable(final String packageName, final String processName, final String processId) {
        final String bpmn = getFromGuvnor(MessageFormat.format(GUVNOR_API_BPMN_SOURCE_PATH, packageName, processName), MediaType.TEXT_PLAIN_TYPE);
        return processId.equals(XPathUtil.getProcessIdFromBpmn(bpmn));
    }

    /**
     * Get a resource from Guvnor.
     * 
     * @param path
     *            The path to the resource.
     * @param clazz
     *            The (JAXB generated) {@link Class} into which the Guvnor reponse is to be unmarshalled.
     * @return The response, unmarshalled into the given class.
     */
    public <T> T getFromGuvnor(final String path, final Class<T> clazz) {
        log.info("Retrieving " + clazz.getSimpleName() + " from Guvnor on path [" + path + "].");
        return GuvnorRestUtil.getFromGuvnor(guvnorConfig, path, clazz);
    }

    /**
     * Get a resource from Guvnor.
     * 
     * @param path
     *            The path to the resource.
     * @param mediaType
     *            The expected media type.
     * @return The resource, in the expected media type, as a {@link String}.
     */
    public String getFromGuvnor(final String path, final MediaType mediaType) {
        log.info("Retrieving resource with type " + mediaType + " from Guvnor on path [" + path + "].");
        return GuvnorRestUtil.getFromGuvnor(guvnorConfig, path, mediaType);
    }
}
