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

    private SelectItem selectedPackage = new SelectItem();

    public List<SelectItem> getGuvnorPackages() {
        log.debug("Retrieving packages from Guvnor.");

        // Call Guvnor to retrieve the available packages.
        final Packages packages = getFromGuvnor(GUVNOR_API_PACKAGES_PATH, Packages.class);

        // Format the output.
        log.debug("Current packages in Guvnor:");
        final List<SelectItem> packageNameList = new ArrayList<SelectItem>();
        for (final org.jboss.brms.test.guvnor.Packages.Package pakkage : packages.getPackage()) {
            log.debug("   - " + pakkage.getTitle());
            packageNameList.add(new SelectItem(pakkage.getTitle()));
        }
        return packageNameList;
    }

    public SelectItem getSelectedPackage() {
        return selectedPackage;
    }

    public void setSelectedPackage(final SelectItem selectedPackage) {
        System.out.println("New value for selected package: " + selectedPackage.getValue());
        this.selectedPackage = selectedPackage;
    }

    public void packageSelected(final ValueChangeEvent evt) {
        System.out.println("Current value for the selected package = " + getSelectedPackage().getValue());
    }

    public List<SelectItem> getGuvnorAssets(final String packageName) {
        log.debug("Retrieving assets for package " + packageName + " from Guvnor.");

        // Call Guvnor to retrieve the available assets.
        final Assets assets = getFromGuvnor(MessageFormat.format(GUVNOR_API_ASSETS_PATH, packageName), Assets.class);

        // Format the output.
        log.debug("Current assets for package " + packageName + " in Guvnor:");
        final List<SelectItem> assetNameList = new ArrayList<SelectItem>();
        for (final org.jboss.brms.test.guvnor.Assets.Asset asset : assets.getAsset()) {
            final String assetName = asset.getRefLink().substring(asset.getRefLink().lastIndexOf("/"));
            log.debug("   - " + assetName);
            assetNameList.add(new SelectItem(assetName));
        }
        return assetNameList;
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
