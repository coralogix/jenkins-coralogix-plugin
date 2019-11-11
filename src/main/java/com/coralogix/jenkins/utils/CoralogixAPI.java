package com.coralogix.jenkins.utils;

import java.util.Collections;
import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;
import com.coralogix.jenkins.credentials.CoralogixCredential;
import com.coralogix.jenkins.exception.CoralogixPluginException;
import com.google.gson.Gson;
import hudson.model.Run;
import hudson.security.ACL;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import com.coralogix.jenkins.model.Log;
import com.coralogix.jenkins.model.Bulk;

/**
 * Coralogix API methods
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2019-10-21
 */
public class CoralogixAPI {

    /**
     * Push tag request
     *
     * @param privateKey  Coralogix Private Key
     * @param application application name
     * @param subsystems  subsystem name
     * @param tag         tag name
     * @param icon        tag icon
     * @throws Exception
     */
    public static void pushTag(String privateKey, String application, String subsystems, String tag, String icon) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet request = new HttpGet(String.format(
                    "https://api.coralogix.com/api/v1/addTag?key=%s&application=%s&subsystem=%s&name=%s&iconUrl=%s",
                    privateKey,
                    application,
                    subsystems,
                    tag,
                    icon
            ));
            httpclient.execute(request);
        } finally {
            httpclient.close();
        }
    }

    /**
     * Send logs request
     *
     * @param application application name
     * @param subsystem   subsystem name
     * @param logEntries  logs bunch
     * @throws Exception
     */
    public static void sendLogs(String privateKey, String application, String subsystem, List<Log> logEntries) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost request = new HttpPost("https://api.coralogix.com/api/v1/logs");
            request.addHeader("content-type", "application/json");
            request.setEntity(new StringEntity(buildData(privateKey, application, subsystem, logEntries)));
            httpclient.execute(request);
        } finally {
            httpclient.close();
        }
    }

    /**
     * Build helper for request data
     *
     * @param application application name
     * @param subsystem   subsystem name
     * @param logEntries  logs bunch
     * @return logs bulk in JSON format
     */
    private static String buildData(String privateKey, String application, String subsystem, List<Log> logEntries) {
        Gson gson = new Gson();
        Bulk bulk = new Bulk(
                privateKey,
                application,
                subsystem,
                getHostName(),
                logEntries
        );
        return gson.toJson(bulk);
    }

    /**
     * Get current machine hostname
     *
     * @return hostname
     */
    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "master";
        }
    }

    /**
     * Coralogix Private Key retriever
     *
     * @param build build context
     * @return Coralogix Private Key
     */
    public static String retrieveCoralogixCredential(Run build, String privateKeyCredentialId) {
        if (StringUtils.isBlank(privateKeyCredentialId)) {
            throw new CoralogixPluginException(
                    "The credential id was not configured - please specify the credentials to use."
            );
        }
        List<CoralogixCredential> credentials = CredentialsProvider.lookupCredentials(
                CoralogixCredential.class,
                build.getParent(),
                ACL.SYSTEM,
                Collections.emptyList()
        );
        CoralogixCredential credential = CredentialsMatchers.firstOrNull(
                credentials,
                new IdMatcher(privateKeyCredentialId)
        );
        if (credential == null) {
            throw new CredentialsUnavailableException(privateKeyCredentialId);
        }
        return credential.getPrivateKey();
    }
}