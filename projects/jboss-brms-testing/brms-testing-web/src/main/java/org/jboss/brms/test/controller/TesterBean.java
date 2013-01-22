package org.jboss.brms.test.controller;

import java.io.IOException;
import java.io.Serializable;
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

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.jboss.brms.test.guvnor.Assets;
import org.jboss.brms.test.guvnor.Packages;
import org.richfaces.component.UISelect;

@Named
@SessionScoped
public class TesterBean implements Serializable {
    /** Serial version ID. */
    private static final long serialVersionUID = 1L;

    private static final String GUVNOR_API_BASE_URL = "http://localhost:8080/jboss-brms/rest";
    private static final String GUVNOR_API_PACKAGES_PATH = GUVNOR_API_BASE_URL + "/packages";
    private static final String GUVNOR_API_ASSETS_PATH = GUVNOR_API_PACKAGES_PATH + "/{0}/assets";

    @Inject
    private transient Logger log;

    public List<SelectItem> getGuvnorPackages() {
        log.debug("Retrieving packages from Guvnor.");

        // Call Guvnor to retrieve the available packages.
        final String responseXml = doGetCall(GUVNOR_API_PACKAGES_PATH);

        // Unmarshal the response.
        final Packages packages = JAXB.unmarshal(responseXml, Packages.class);

        // Format the output.
        log.debug("Current packages in Guvnor:");
        final List<SelectItem> packageNameList = new ArrayList<SelectItem>();
        for (final org.jboss.brms.test.guvnor.Packages.Package pakkage : packages.getPackage()) {
            log.debug("   - " + pakkage.getTitle());
            packageNameList.add(new SelectItem(pakkage.getTitle()));
        }
        return packageNameList;
    }

    public void packageSelected(final ValueChangeEvent evt) {
        final Object value = ((UISelect) evt.getComponent()).getValue();
        log.info("Current value for the selected package = " + value);
    }

    public List<SelectItem> getGuvnorAssets(final String packageName) {
        log.debug("Retrieving assets for package " + packageName + " from Guvnor.");

        // Call Guvnor to retrieve the available assets.
        final String responseXml = doGetCall(MessageFormat.format(GUVNOR_API_ASSETS_PATH, packageName));

        // Unmarshal the response.
        final Assets assets = JAXB.unmarshal(responseXml, Assets.class);

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

    private String doGetCall(final String uriString) {
        String responseBody = null;
        final HttpClient httpclient = new DefaultHttpClient();
        try {
            final URI uri = new URI(uriString);
            final HttpGet httpget = new HttpGet(uri);
            responseBody = httpclient.execute(httpget, new BasicResponseHandler());
        } catch (final URISyntaxException uriEx) {
            log.error("Bad URI for request.", uriEx);
        } catch (final IOException ioEx) {
            log.error("Problem accessing Guvnor API.", ioEx);
        } finally {
            // When the HttpClient instance is no longer needed, shut down the connection manager to ensure immediate deallocation of all system resources.
            httpclient.getConnectionManager().shutdown();
        }

        return responseBody;
    }
}
