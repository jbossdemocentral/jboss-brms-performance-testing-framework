package org.jboss.brms.test.controller;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.brms.test.guvnor.Assets;
import org.jboss.brms.test.guvnor.Packages;
import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.service.GuvnorService;
import org.jboss.brms.test.service.ProcessService;
import org.jboss.brms.test.service.ProcessStartParameters;
import org.jboss.brms.test.util.XPathUtil;

@Named
@SessionScoped
public class TesterBean implements Serializable {
    /** Serial version ID. */
    private static final long serialVersionUID = 1L;

    private static final String GUVNOR_API_PACKAGES_PATH = "/packages";
    private static final String GUVNOR_API_ASSETS_PATH = GUVNOR_API_PACKAGES_PATH + "/{0}/assets";
    private static final String GUVNOR_API_BPMN_SOURCE_PATH = GUVNOR_API_ASSETS_PATH + "/{1}/source";

    @Inject
    private transient Logger log;

    @Inject
    private GuvnorService guvnorService;

    @Inject
    private ProcessService processService;

    private final List<SelectItem> packageList = new ArrayList<SelectItem>();
    private String selectedPackage = "";
    private final List<String> processList = new ArrayList<String>();
    private String selectedProcess = null;

    private int numberOfInstances = 1;
    private boolean startInParallel;
    private boolean runInIndividualKnowledgeSession;

    public List<SelectItem> getPackageList() {
        log.info("Retrieving packages from Guvnor...");

        // Call Guvnor to retrieve the available packages.
        final Packages packages = guvnorService.getFromGuvnor(GUVNOR_API_PACKAGES_PATH, Packages.class);

        // Format the output.
        packageList.clear();
        packageList.add(new SelectItem(""));
        log.info("Current packages in Guvnor:");
        for (final org.jboss.brms.test.guvnor.Packages.Package pakkage : packages.getPackage()) {
            log.info("   - " + pakkage.getTitle());
            packageList.add(new SelectItem(pakkage.getTitle()));
        }
        processList.clear();

        return packageList;
    }

    public String getSelectedPackage() {
        return selectedPackage;
    }

    public void setSelectedPackage(final String selectedPackage) {
        this.selectedPackage = selectedPackage;
    }

    public void selectPackage(final ValueChangeEvent evt) {
        log.info("Value of new selected package: " + evt.getNewValue());
        selectedPackage = (String) evt.getNewValue();
        getGuvnorAssets();
    }

    private void getGuvnorAssets() {
        if (StringUtils.isBlank(selectedPackage)) {
            log.debug("No package selected, so no assets retrieved.");
            return;
        }

        log.info("Retrieving assets for package " + selectedPackage + " from Guvnor...");

        // Call Guvnor to retrieve the available assets.
        final Assets assets = guvnorService.getFromGuvnor(MessageFormat.format(GUVNOR_API_ASSETS_PATH, selectedPackage), Assets.class);

        // Format the output.
        processList.clear();
        if ((assets == null) || (assets.getAsset() == null) || assets.getAsset().isEmpty()) {
            log.info("Currently no assets for package " + selectedPackage + " in Guvnor!");
        } else {
            log.info("Current assets for package " + selectedPackage + " in Guvnor:");
            for (final org.jboss.brms.test.guvnor.Assets.Asset asset : assets.getAsset()) {
                final String assetName = asset.getRefLink().substring(asset.getRefLink().lastIndexOf("/") + 1);
                final String format = asset.getMetadata().getFormat();
                final String fileName = assetName + "." + format;
                log.info("   - " + fileName);
                if ("bpmn2".equals(format)) {
                    // Call Guvnor for the BPMN definition (to get the process ID from).
                    final String bpmn = guvnorService.getFromGuvnor(MessageFormat.format(GUVNOR_API_BPMN_SOURCE_PATH, selectedPackage, assetName),
                            MediaType.TEXT_PLAIN_TYPE);
                    final String processId = XPathUtil.getProcessIdFromBpmn(bpmn);
                    processList.add(processId);
                }
            }
        }
    }

    public List<String> getProcesses() {
        return processList;
    }

    public void selectProcess(final ValueChangeEvent evt) {
        String process = null;
        for (final UIComponent comp : evt.getComponent().getParent().getChildren()) {
            if (comp instanceof HtmlInputHidden) {
                process = (String) ((HtmlInputHidden) comp).getValue();
                break;
            }
        }

        if (process != null) {
            log.info("Selecting process with ID " + process);
            selectedProcess = process;
        } else {
            log.error("Trying to select process, but ID is NULL!");
        }
    }

    public String getSelectedProcess() {
        return selectedProcess;
    }

    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(final int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    public boolean isStartInParallel() {
        return startInParallel;
    }

    public void setStartInParallel(final boolean startInParallel) {
        this.startInParallel = startInParallel;
    }

    public boolean isRunInIndividualKnowledgeSession() {
        return runInIndividualKnowledgeSession;
    }

    public void setRunInIndividualKnowledgeSession(final boolean runInIndividualKnowledgeSession) {
        this.runInIndividualKnowledgeSession = runInIndividualKnowledgeSession;
    }

    public void startProcessInstance() {
        if (StringUtils.isBlank(getSelectedPackage())) {
            log.warn("No package selected - not running process(es).");
            return;
        }
        if (StringUtils.isBlank(getSelectedProcess())) {
            log.warn("No process selected - not running process(es).");
            return;
        }

        final ProcessStartParameters parameters = new ProcessStartParameters();
        parameters.setPackageName(getSelectedPackage());
        parameters.setProcessId(getSelectedProcess());
        parameters.setNumberOfInstances(getNumberOfInstances());
        parameters.setRunInIndividualKnowledgeSession(isRunInIndividualKnowledgeSession());
        parameters.setStartInParallel(isStartInParallel());
        final Metrics metrics = processService.runProcesses(parameters);

        // Temp output:
        log.info(metrics.printAll());
    }
}
