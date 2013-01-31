package org.jboss.brms.test.util;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

public final class GuvnorUtil {
    private static final String GUVNOR_REST_BASE_URL = "http://localhost:8080/jboss-brms/rest";
    private static final String GUVNOR_USER_NAME = "admin";
    private static final String GUVNOR_PASSWORD = "admin";

    public static String getFromGuvnor(final String path) {
        String output = null;

        // Create HTTP client for authenticated REST API.
        final DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(GUVNOR_USER_NAME, GUVNOR_PASSWORD));

        // Configure HTTP client to authenticate preemptively by prepopulating the authentication cache in a context.
        final AuthCache authCache = new BasicAuthCache();
        authCache.put(new HttpHost("localhost", 8080), new BasicScheme());
        final BasicHttpContext ctx = new BasicHttpContext();
        ctx.setAttribute(ClientContext.AUTH_CACHE, authCache);

        // Create request from the client and context, accepting JSON replies.
        final ClientRequest request = new ClientRequest(GUVNOR_REST_BASE_URL + path, new ApacheHttpClient4Executor(httpClient, ctx));
        request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

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
