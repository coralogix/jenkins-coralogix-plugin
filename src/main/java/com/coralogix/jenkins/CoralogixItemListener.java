package com.coralogix.jenkins;

import com.coralogix.jenkins.model.Log;
import com.coralogix.jenkins.utils.CoralogixAPI;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Jenkins CRUD events listener definition
 *
 * @author Eldar Aliiev
 * @version 1.1.0
 * @since 2019-11-11
 */
@Extension
public class CoralogixItemListener extends ItemListener {

    /**
     * Events listener logger
     */
    private static final Logger logger = Logger.getLogger(CoralogixItemListener.class.getName());

    /**
     * Create event
     *
     * @param item Jenkins job item
     */
    @Override
    public void onCreated(Item item) {
        sendAuditLog(item.getFullName() + " " + item.getClass().getSimpleName().toLowerCase() + " was created", 3);
    }

    /**
     * Update event
     *
     * @param item Jenkins job item
     */
    @Override
    public void onUpdated(Item item) {
        sendAuditLog(item.getFullName() + " " + item.getClass().getSimpleName().toLowerCase() + " was updated", 4);
    }

    /**
     * Copy event
     *
     * @param item Jenkins job item
     */
    @Override
    public void onCopied(Item src, Item item) {
        sendAuditLog(src.getFullName() + " " + item.getClass().getSimpleName().toLowerCase() + " was copied to " + item.getFullName(), 3);
    }

    /**
     * Delete event
     *
     * @param item Jenkins job item
     */
    @Override
    public void onDeleted(Item item) {
        sendAuditLog(item.getFullName() + " " + item.getClass().getSimpleName().toLowerCase() + " was deleted", 6);
    }

    /**
     * Move/rename event
     *
     * @param item Jenkins job item
     */
    @Override
    public void onLocationChanged(Item item, String oldFullName, String newFullName) {
        sendAuditLog(oldFullName + " " + item.getClass().getSimpleName().toLowerCase() + " was moved/renamed to " + newFullName, 4);
    }

    /**
     * Audit logs sender
     *
     * @param message event message
     * @param severity event severity
     */
    static void sendAuditLog(String message, Integer severity) {
        if (CoralogixConfiguration.get().getAuditLogsEnabled()) {
            try {
                List<Log> logEntries = new ArrayList<>();
                logEntries.add(new Log(
                        severity,
                        message,
                        "audit",
                        "",
                        "",
                        ""
                ));
                CoralogixAPI.sendLogs(
                        CoralogixConfiguration.get().getPrivateKey(),
                        CoralogixConfiguration.get().getJenkinsName(),
                        "audit",
                        logEntries
                );
            } catch (Exception e) {
                logger.log(Level.WARNING, "Cannot send build logs to Coralogix!");
            }
        }
    }
}