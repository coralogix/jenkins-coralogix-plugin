package com.coralogix.jenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Coralogix API credentials definition
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2021-04-10
 */
public class CoralogixApiCredential extends BaseStandardCredentials {

    /**
     * Coralogix API Key
     */
    private Secret apiKey;

    /**
     * Coralogix API Key credential constructor
     */
    @DataBoundConstructor
    public CoralogixApiCredential(@CheckForNull CredentialsScope scope, @CheckForNull String id,
                               @CheckForNull String description, @NonNull Secret apiKey) {
        super(scope, id, description);
        this.apiKey = apiKey;
    }

    /**
     * Coralogix API Key getter
     *
     * @return the currently configured API key
     */
    public String getApiKey() {
        return Secret.toString(this.apiKey);
    }

    /**
     * Jenkins credentials definition
     */
    @Extension
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        /**
         * Coralogix credentials display value
         *
         * @return build step name
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return "Coralogix API Key";
        }
    }
}