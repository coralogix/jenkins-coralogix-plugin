package com.coralogix.jenkins.utils;

import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;
import com.google.gson.Gson;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import com.coralogix.jenkins.CoralogixConfiguration;
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
    public static void sendLogs(String application, String subsystem, List<Log> logEntries) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost request = new HttpPost("https://api.coralogix.com/api/v1/logs");
            request.addHeader("content-type", "application/json");
            request.setEntity(new StringEntity(buildData(application, subsystem, logEntries)));
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
    private static String buildData(String application, String subsystem, List<Log> logEntries) {
        Gson gson = new Gson();
        Bulk bulk = new Bulk(
                CoralogixConfiguration.get().getPrivateKey(),
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
}