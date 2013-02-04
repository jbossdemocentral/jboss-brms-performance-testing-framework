package org.jboss.brms.test.controller;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.brms.test.guvnor.Assets;
import org.jboss.brms.test.guvnor.Packages;
import org.jboss.brms.test.model.MeasuredProcess;
import org.jboss.brms.test.model.MeasuredProcessInstance;
import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.service.GuvnorService;
import org.jboss.brms.test.service.ProcessService;

@Named
@SessionScoped
public class TesterBean implements Serializable {
    /** Serial version ID. */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(TesterBean.class);

    private static final String GUVNOR_API_PACKAGES_PATH = "/packages";
    private static final String GUVNOR_API_ASSETS_PATH = GUVNOR_API_PACKAGES_PATH + "/{0}/assets";

    @Inject
    private transient Logger log;

    @Inject
    private GuvnorService guvnorService;

    @Inject
    private ProcessService processService;

    private final List<SelectItem> packageList = new ArrayList<SelectItem>();
    private String selectedPackage = "";
    private final List<String> assetList = new ArrayList<String>();

    public List<SelectItem> getPackageList() {
        if (packageList.isEmpty()) {
            getGuvnorPackages();
        }
        return packageList;
    }

    private void getGuvnorPackages() {
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
        assetList.clear();
    }

    public String getSelectedPackage() {
        return selectedPackage;
    }

    public void setSelectedPackage(final String selectedPackage) {
        this.selectedPackage = selectedPackage;
    }

    public void packageSelected(final ValueChangeEvent evt) {
        log.info("Value of new selected package: " + evt.getNewValue());
        selectedPackage = (String) evt.getNewValue();
        getGuvnorAssets();
    }

    private void getGuvnorAssets() {
        if (StringUtils.isBlank(selectedPackage)) {
            LOGGER.debug("No package selected, so no assets retrieved.");
            return;
        }

        log.info("Retrieving assets for package " + selectedPackage + " from Guvnor...");

        // Call Guvnor to retrieve the available assets.
        final Assets assets = guvnorService.getFromGuvnor(MessageFormat.format(GUVNOR_API_ASSETS_PATH, selectedPackage), Assets.class);

        // Format the output.
        assetList.clear();
        if ((assets == null) || (assets.getAsset() == null) || assets.getAsset().isEmpty()) {
            log.info("Currently no assets for package " + selectedPackage + " in Guvnor!");
        } else {
            log.info("Current assets for package " + selectedPackage + " in Guvnor:");
            for (final org.jboss.brms.test.guvnor.Assets.Asset asset : assets.getAsset()) {
                final String assetName = asset.getRefLink().substring(asset.getRefLink().lastIndexOf("/") + 1) + "." + asset.getMetadata().getFormat();
                log.info("   - " + assetName);
                assetList.add(assetName);
            }
        }
    }

    public List<String> getAssets() {
        return assetList;
    }

    public void startProcessInstance() {
        final Metrics metrics = processService.runInstance("org.jbpm.evaluation.customer", "org.jbpm.customer-evaluation", null);

        // Temp output:
        LOGGER.info(metrics.print());
        for (final MeasuredProcess process : metrics.getProcesses()) {
            LOGGER.info(process.print());
            for (final MeasuredProcessInstance processInstance : process.getInstances()) {
                LOGGER.info(processInstance.print());
            }
        }
    }
}
