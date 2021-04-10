package com.coralogix.jenkins.utils;

import java.util.Collections;
import java.util.List;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;
import com.coralogix.jenkins.credentials.CoralogixCredential;
import com.coralogix.jenkins.credentials.CoralogixApiCredential;
import com.coralogix.jenkins.exception.CoralogixPluginException;
import com.google.gson.Gson;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import com.coralogix.jenkins.CoralogixConfiguration;
import com.coralogix.jenkins.model.Log;
import com.coralogix.jenkins.model.Bulk;
import com.coralogix.jenkins.model.Tag;

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
     * @param privateKey    Coralogix Private Key
     * @param applications  applications names
     * @param subsystems    subsystems names
     * @param tag           tag name
     * @param icon          tag icon
     * @throws Exception
     */
    public static void pushTag(String privateKey, List<String> applications, List<String> subsystems, String tag, String icon) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost request = new HttpPost("https://webapi." + CoralogixConfiguration.get().getCoralogixEndpoint() + "/api/v1/external/tags");
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Authorization", "Bearer " + privateKey);
            request.setEntity(new StringEntity(buildTag(tag, applications, subsystems, icon)));
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
            HttpPost request = new HttpPost("https://api." + CoralogixConfiguration.get().getCoralogixEndpoint() + "/api/v1/logs");
            request.addHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(buildData(privateKey, application, subsystem, logEntries)));
            httpclient.execute(request);
        } finally {
            httpclient.close();
        }
    }

    /**
     * Build helper for logs send request
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
            Util.getHostName(),
            logEntries
        );
        return gson.toJson(bulk);
    }

    /**
     * Build helper for tag request
     *
     * @param name          tag name
     * @param applications  applications list
     * @param subsystems    subsystems list
     * @param icon          tag icon
     * @return tag in JSON format
     */
    private static String buildTag(String name, List<String> applications, List<String> subsystems, String icon) {
        Gson gson = new Gson();
        Tag tag = new Tag(
            name,
            applications,
            subsystems,
            icon
        );
        return gson.toJson(tag);
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

    /**
     * Coralogix API Key retriever
     *
     * @param build build context
     * @return Coralogix API Key
     */
    public static String retrieveCoralogixApiCredential(Run build, String apiKeyCredentialId) {
        if (StringUtils.isBlank(apiKeyCredentialId)) {
            throw new CoralogixPluginException(
                "The credential id was not configured - please specify the credentials to use."
            );
        }
        List<CoralogixApiCredential> credentials = CredentialsProvider.lookupCredentials(
            CoralogixApiCredential.class,
            build.getParent(),
            ACL.SYSTEM,
            Collections.emptyList()
        );
        CoralogixApiCredential credential = CredentialsMatchers.firstOrNull(
            credentials,
            new IdMatcher(apiKeyCredentialId)
        );
        if (credential == null) {
            throw new CredentialsUnavailableException(apiKeyCredentialId);
        }
        return credential.getApiKey();
    }

    /**
     * Substitute parameters
     *
     * @param build build context
     * @param listener build listener context
     * @param inputString parameter name
     * @return parameter final value
     */
    public static String replaceMacros(Run<?, ?> build, TaskListener listener, String inputString) {
        String returnString = inputString;
        if (build != null && inputString != null) {
            try {
                Map<String, String> messageEnvVars = getEnvVars(build, listener);
                returnString = Util.replaceMacro(inputString, messageEnvVars);

            } catch (Exception e) {
                listener.getLogger().printf("Couldn't replace macros in message: %s%n", e.getMessage());
            }
        }
        return returnString;
    }

    /**
     * Build environment variables list
     *
     * @param build build context
     * @param listener build listener context
     * @return environment variable list
     */
    private static Map<String, String> getEnvVars(Run<?, ?> build, TaskListener listener) {
        Map<String, String> messageEnvVars = new HashMap<>();
        if (build != null) {
            messageEnvVars.putAll(build.getCharacteristicEnvVars());
            try {
                messageEnvVars.putAll(build.getEnvironment(listener));
            } catch (Exception e) {
                listener.getLogger().printf("Couldn't get Env Variables: %s%n", e.getMessage());
            }
        }
        return messageEnvVars;
    }
}