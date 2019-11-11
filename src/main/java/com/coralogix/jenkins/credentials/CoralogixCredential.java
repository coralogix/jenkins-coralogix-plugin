package com.coralogix.jenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Coralogix credentials definition
 *
 * @author Eldar Aliiev
 * @version 1.1.0
 * @since 2019-11-11
 */
public class CoralogixCredential extends AbstractCoralogixCredential {

    /**
     * Coralogix Private Key
     */
    private Secret privateKey;

    /**
     * Coralogix Private Key credential constructor
     */
    @DataBoundConstructor
    public CoralogixCredential(@CheckForNull CredentialsScope scope, @CheckForNull String id,
                               @CheckForNull String description, @NonNull Secret privateKey) {
        super(scope, id, description);
        this.privateKey = privateKey;
    }

    /**
     * Coralogix Private Key getter
     *
     * @return the currently configured private key
     */
    public String getPrivateKey() {
        return Secret.toString(this.privateKey);
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
            return "Coralogix Private Key";
        }
    }
}