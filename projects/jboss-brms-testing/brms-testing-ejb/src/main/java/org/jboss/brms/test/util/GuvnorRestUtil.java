package org.jboss.brms.test.util;

import java.io.StringReader;
import java.util.Properties;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXB;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.jboss.brms.test.util.Resources.GuvnorConfig;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

public final class GuvnorRestUtil {
    private static final String GUVNOR_CONFIG_HOST = "guvnor.host";
    private static final String GUVNOR_CONFIG_PORT = "guvnor.port";
    private static final String GUVNOR_CONFIG_REST_BASE_URL = "guvnor.rest.url";
    private static final String GUVNOR_CONFIG_USER_NAME = "guvnor.user";
    private static final String GUVNOR_CONFIG_PASSWORD = "guvnor.password";

    private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.APPLICATION_XML_TYPE;

    private static MediaType EXPECTED_MEDIA_TYPE = DEFAULT_MEDIA_TYPE;

    @Inject
    @GuvnorConfig
    private Properties guvnorConfig;

    public static MediaType getExpectedMediaType() {
        return EXPECTED_MEDIA_TYPE;
    }

    /**
     * If any other type of output is required, set it here.
     * 
     * @param expectedMediaType
     *            Any of {@link MediaType#APPLICATION_XML_TYPE} (the default), {@link MediaType#APPLICATION_JSON_TYPE} or
     *            {@link MediaType#APPLICATION_ATOM_XML_TYPE}. Other values are not supported by the Guvnor API.
     */
    public static void setExpectedMediaType(final MediaType expectedMediaType) {
        EXPECTED_MEDIA_TYPE = expectedMediaType;
    }

    /**
     * Get a resource from Guvnor through its REST API.
     * 
     * @param path
     *            The path to the resource.
     * @param clazz
     *            The (JAXB generated) {@link Class} into which the Guvnor reponse is to be unmarshalled.
     * @return The response, unmarshalled into the given class.
     */
    public <T> T getFromGuvnor(final String path, final Class<T> clazz) {
        // Retrieve the info from Guvnor as XML.
        setExpectedMediaType(MediaType.APPLICATION_XML_TYPE);
        final String responseXml = getFromGuvnor(path);

        // Unmarshal the response.
        T result = null;
        if (StringUtils.isNotBlank(responseXml)) {
            result = JAXB.unmarshal(new StringReader(responseXml), clazz);
        }
        return result;
    }

    /**
     * Get a resource from Guvnor through its REST API.
     * 
     * @param path
     *            The path to the resource.
     * @return The response, in the expected media type, as a {@link String}.
     */
    public String getFromGuvnor(final String path) {
        String output = null;

        // Create HTTP client for authenticated REST API.
        final DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(guvnorConfig.getProperty(GUVNOR_CONFIG_USER_NAME), guvnorConfig.getProperty(GUVNOR_CONFIG_PASSWORD)));

        // Configure HTTP client to authenticate preemptively by prepopulating the authentication cache in a context.
        final AuthCache authCache = new BasicAuthCache();
        authCache.put(new HttpHost(guvnorConfig.getProperty(GUVNOR_CONFIG_HOST), Integer.parseInt(guvnorConfig.getProperty(GUVNOR_CONFIG_PORT))),
                new BasicScheme());
        final BasicHttpContext ctx = new BasicHttpContext();
        ctx.setAttribute(ClientContext.AUTH_CACHE, authCache);

        // Create request from the client and context, accepting JSON replies.
        final ClientRequest request = new ClientRequest(guvnorConfig.getProperty(GUVNOR_CONFIG_REST_BASE_URL) + path, new ApacheHttpClient4Executor(httpClient,
                ctx));
        request.header(HttpHeaders.ACCEPT, EXPECTED_MEDIA_TYPE.toString());

        // Make the call.
        try {
            final ClientResponse<String> response = request.get(String.class);
            output = response.getEntity();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            // Close the connection.
            try {
                httpClient.getConnectionManager().shutdown();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        return output;
    }
}
