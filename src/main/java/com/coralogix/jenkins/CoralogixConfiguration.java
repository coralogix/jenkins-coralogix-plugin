package com.coralogix.jenkins;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * Coralogix global configuration.
 */
@Extension
public class CoralogixConfiguration extends GlobalConfiguration {

    public static CoralogixConfiguration get() {
        return GlobalConfiguration.all().get(CoralogixConfiguration.class);
    }

    private String privateKey;

    public CoralogixConfiguration() {
        load();
    }

    /**
     * @return the currently configured private key
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * @param privateKey the new value of this field
     */
    @DataBoundSetter
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        save();
    }

    public FormValidation doCheckPrivateKey(@QueryParameter String privateKey) {
        if (StringUtils.isEmpty(privateKey)) {
            return FormValidation.error("You must provide the private key");
        }
        return FormValidation.ok();
    }
}