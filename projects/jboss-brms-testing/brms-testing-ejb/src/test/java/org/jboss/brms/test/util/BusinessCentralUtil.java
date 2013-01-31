package org.jboss.brms.test.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public final class BusinessCentralUtil {
    private static final String BUSINESS_CENTRAL_REST_BASE_URL = "http://localhost:8080/business-central-server/rs";
    private static final String BUSINESS_CENTRAL_USER_NAME = "admin";
    private static final String BUSINESS_CENTRAL_PASSWORD = "admin";

    public static String getFromBusinessCentral(final String path) {
        String output = null;

        final DefaultHttpClient httpClient = new DefaultHttpClient();
        output = get(httpClient, BUSINESS_CENTRAL_REST_BASE_URL + path);
        if (output.contains("j_security_check")) {
            // First need to authenticate, then get again.
            authenticate(httpClient, BUSINESS_CENTRAL_REST_BASE_URL + path);
            output = get(httpClient, BUSINESS_CENTRAL_REST_BASE_URL + path);
        }

        return output;
    }

    private static String get(final HttpClient httpClient, final String url) {
        String responseString = "";

        try {
            final HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

            final HttpResponse response = httpClient.execute(httpGet);
            responseString = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return responseString;
    }

    private static String post(final HttpClient httpClient, final String url, final Map<String, Object> parms) {
        String responseString = "";

        final List<NameValuePair> formParms = new ArrayList<NameValuePair>();
        if (parms != null) {
            for (final Entry<String, Object> parmEntry : parms.entrySet()) {
                formParms.add(new BasicNameValuePair(parmEntry.getKey(), parmEntry.getValue().toString()));
            }
        }

        try {
            final HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(formParms, "UTF-8"));

            final HttpResponse response = httpClient.execute(httpPost);
            responseString = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return responseString;
    }

    private static String authenticate(final HttpClient httpClient, final String url) {
        final Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("j_username", BUSINESS_CENTRAL_USER_NAME);
        parms.put("j_password", BUSINESS_CENTRAL_PASSWORD);
        return post(httpClient, url + "/j_security_check", parms);
    }
}
