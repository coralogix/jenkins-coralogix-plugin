package com.coralogix.jenkins;

import hudson.Extension;
import jenkins.security.SecurityListener;
import org.acegisecurity.userdetails.UserDetails;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.coralogix.jenkins.model.Log;
import com.coralogix.jenkins.utils.CoralogixAPI;

/**
 * Jenkins events listener definition
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2019-10-21
 */
@Extension
public class CoralogixListener extends SecurityListener {

    /**
     * Events listener logger
     */
    private static final Logger logger = Logger.getLogger(CoralogixListener.class.getName());

    /**
     * Login event
     *
     * @param details user details
     */
    @Override
    protected void authenticated(@Nonnull UserDetails details) {
        this.sendAuditLog(details.getUsername() + " logged in");
    }

    /**
     * Login fail event
     *
     * @param username username
     */
    @Override
    protected void failedToAuthenticate(@Nonnull String username) {
        this.sendAuditLog(username + " failed to login");
    }

    /**
     * Login event
     *
     * @param username username
     */
    @Override
    protected void loggedIn(@Nonnull String username) {
    }

    /**
     * Login fail event
     *
     * @param username username
     */
    @Override
    protected void failedToLogIn(@Nonnull String username) {
    }

    /**
     * Logout event
     *
     * @param username username
     */
    @Override
    protected void loggedOut(@Nonnull String username) {
        this.sendAuditLog(username + " logged out");
    }

    /**
     * Audit logs sender
     *
     * @param message audit message
     */
    static void sendAuditLog(String message) {
        try {
            List<Log> logEntries = new ArrayList<>();
            logEntries.add(new Log(
                    3,
                    message,
                    "audit",
                    "",
                    "",
                    ""
            ));
            CoralogixAPI.sendLogs("jenkins", "security", logEntries);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Cannot send build logs to Coralogix!");
        }
    }
}