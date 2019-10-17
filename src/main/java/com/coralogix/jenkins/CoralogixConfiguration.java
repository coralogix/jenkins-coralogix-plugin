package com.coralogix.jenkins;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * Jenkins plugin global configuration definition
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2019-10-21
 */
@Extension
public class CoralogixConfiguration extends GlobalConfiguration {

    /**
     * Coralogix Private Key
     */
    private String privateKey;

    /**
     * Global configuration getter
     *
     * @return global configuration values
     */
    public static CoralogixConfiguration get() {
        return GlobalConfiguration.all().get(CoralogixConfiguration.class);
    }

    /**
     * Load configuration form Jenkins storage
     */
    public CoralogixConfiguration() {
        load();
    }

    /**
     * Coralogix Private Key getter
     *
     * @return the currently configured private key
     */
    public String getPrivateKey() {
        return this.privateKey;
    }

    /**
     * Coralogix Private Key setter
     *
     * @param privateKey the new value of the Private Key
     */
    @DataBoundSetter
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        save();
    }

    /**
     * Coralogix Private Key validator
     *
     * @param privateKey Coralogix Private Key
     * @return Private Key validation status
     */
    public FormValidation doCheckPrivateKey(@QueryParameter String privateKey) {
        if (StringUtils.isEmpty(privateKey)) {
            return FormValidation.error("You must provide the private key");
        }
        return FormValidation.ok();
    }
}