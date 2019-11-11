package com.coralogix.jenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

/**
 * Coralogix credentials interface definition
 *
 * @author Eldar Aliiev
 * @version 1.1.0
 * @since 2019-11-11
 */
public abstract class AbstractCoralogixCredential extends BaseStandardCredentials {

    /**
     * Coralogix Private Key credential constructor
     */
    protected AbstractCoralogixCredential(CredentialsScope scope, String id, String description) {
        super(scope, id, description);
    }

    /**
     * Coralogix Private Key getter
     *
     * @return the currently configured private key
     */
    protected abstract String getPrivateKey();
}