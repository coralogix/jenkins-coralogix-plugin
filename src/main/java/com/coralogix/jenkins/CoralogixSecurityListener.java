package com.coralogix.jenkins;

import hudson.Extension;
import hudson.util.Secret;
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
 * Jenkins security events listener definition
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2019-10-21
 */
@Extension
public class CoralogixSecurityListener extends SecurityListener {

    /**
     * Events listener logger
     */
    private static final Logger logger = Logger.getLogger(CoralogixSecurityListener.class.getName());

    /**
     * Login event
     *
     * @param details user details
     */
    @Override
    protected void authenticated(@Nonnull UserDetails details) {
        this.sendSecurityLog(details.getUsername() + " logged in");
    }

    /**
     * Login fail event
     *
     * @param username username
     */
    @Override
    protected void failedToAuthenticate(@Nonnull String username) {
        this.sendSecurityLog(username + " failed to login");
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
        this.sendSecurityLog(username + " logged out");
    }

    /**
     * Security logs sender
     *
     * @param message security message
     */
    static void sendSecurityLog(String message) {
        if (CoralogixConfiguration.get().getSecurityLogsEnabled()) {
            try {
                List<Log> logEntries = new ArrayList<>();
                logEntries.add(new Log(
                    3,
                    message,
                    "security",
                    "",
                    "",
                    ""
                ));
                CoralogixAPI.sendLogs(
                    Secret.toString(CoralogixConfiguration.get().getPrivateKey()),
                    CoralogixConfiguration.get().getJenkinsName(),
                    "security",
                    logEntries
                );
            } catch (Exception e) {
                logger.log(Level.WARNING, "Cannot send build logs to Coralogix!");
            }
        }
    }
}