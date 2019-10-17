package com.coralogix.jenkins;

import hudson.Extension;
import jenkins.security.SecurityListener;
import org.acegisecurity.userdetails.UserDetails;

import javax.annotation.Nonnull;
import api.CoralogixLogger;

import com.coralogix.jenkins.CoralogixConfiguration;


@Extension
public class CoralogixListener extends SecurityListener {

    private CoralogixLogger logger = new CoralogixLogger("audit");

    @Override
    protected void authenticated(@Nonnull UserDetails details) {
        CoralogixLogger.configure(
            CoralogixConfiguration.get().getPrivateKey(),
            "jenkins",
            "security"
        );
        this.logger.info(details.getUsername() + " logged in");
    }

    @Override
    protected void failedToAuthenticate(@Nonnull String username) {
        CoralogixLogger.configure(
            CoralogixConfiguration.get().getPrivateKey(),
            "jenkins",
            "security"
        );
        this.logger.info(username + " failed to login");
    }

    @Override
    protected void loggedIn(@Nonnull String username) {
    }

    @Override
    protected void failedToLogIn(@Nonnull String username) {
    }

    @Override
    protected void loggedOut(@Nonnull String username) {
        CoralogixLogger.configure(
            CoralogixConfiguration.get().getPrivateKey(),
            "jenkins",
            "security"
        );
        this.logger.info(username + " logged out");
    }
}