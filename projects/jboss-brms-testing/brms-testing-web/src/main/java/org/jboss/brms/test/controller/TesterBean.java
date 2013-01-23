package org.jboss.brms.test.controller;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXB;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jboss.brms.test.guvnor.Assets;
import org.jboss.brms.test.guvnor.Packages;

@Named
@SessionScoped
public class TesterBean implements Serializable {
    /** Serial version ID. */
    private static final long serialVersionUID = 1L;

    private static final String GUVNOR_API_HOST = "localhost";
    private static final int GUVNOR_API_PORT = 8080;
    private static final String GUVNOR_API_USER = "admin";
    private static final String GUVNOR_API_PASSWORD = "admin";
    private static final String GUVNOR_API_BASE_URL = "http://" + GUVNOR_API_HOST + ":" + GUVNOR_API_PORT + "/jboss-brms/rest";
    private static final String GUVNOR_API_PACKAGES_PATH = GUVNOR_API_BASE_URL + "/packages";
    private static final String GUVNOR_API_ASSETS_PATH = GUVNOR_API_PACKAGES_PATH + "/{0}/assets";

    @Inject
    private transient Logger log;

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
        final Packages packages = getFromGuvnor(GUVNOR_API_PACKAGES_PATH, Packages.class);

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
        log.info("Retrieving assets for package " + selectedPackage + " from Guvnor...");

        // Call Guvnor to retrieve the available assets.
        final Assets assets = getFromGuvnor(MessageFormat.format(GUVNOR_API_ASSETS_PATH, selectedPackage), Assets.class);

        // Format the output.
        assetList.clear();
        log.info("Current assets for package " + selectedPackage + " in Guvnor:");
        for (final org.jboss.brms.test.guvnor.Assets.Asset asset : assets.getAsset()) {
            final String assetName = asset.getRefLink().substring(asset.getRefLink().lastIndexOf("/") + 1) + "." + asset.getMetadata().getFormat();
            log.info("   - " + assetName);
            assetList.add(assetName);
        }
    }

    public List<String> getAssets() {
        return assetList;
    }

    private <T> T getFromGuvnor(final String uriString, final Class<T> clazz) {
        // Retrieve the info from Guvnor as XML.
        String responseXml = null;
        final DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.getCredentialsProvider().setCredentials(new AuthScope(GUVNOR_API_HOST, GUVNOR_API_PORT),
                new UsernamePasswordCredentials(GUVNOR_API_USER, GUVNOR_API_PASSWORD));
        try {
            final URI uri = new URI(uriString);
            final HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader("Accept", "application/xml");
            final HttpResponse response = httpclient.execute(httpGet);
            responseXml = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
        } catch (final URISyntaxException uriEx) {
            log.error("Bad URI for request.", uriEx);
        } catch (final IOException ioEx) {
            log.error("Problem accessing Guvnor API.", ioEx);
        } finally {
            // When the HttpClient instance is no longer needed, shut down the connection manager to ensure immediate deallocation of all system resources.
            httpclient.getConnectionManager().shutdown();
        }

        // Unmarshal the response.
        T result = null;
        if (StringUtils.isNotBlank(responseXml)) {
            result = JAXB.unmarshal(new StringReader(responseXml), clazz);
        }
        return result;
    }
}
