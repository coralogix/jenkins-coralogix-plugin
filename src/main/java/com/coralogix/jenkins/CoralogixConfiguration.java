package com.coralogix.jenkins;

import hudson.Extension;
import hudson.util.Secret;
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
    private Secret privateKey;

    /**
     * Jenkins name
     */
    private String jenkinsName = "Jenkins";

    /**
     * System logs sending status
     */
    private Boolean systemLogsEnabled = false;

    /**
     * Audit logs sending status
     */
    private Boolean auditLogsEnabled = false;

    /**
     * Security logs sending status
     */
    private Boolean securityLogsEnabled = false;

    /**
     * Metrics sending status
     */
    private Boolean metricsEnabled = false;

    /**
     * Metrics sending interval
     */
    private Integer metricsInterval = 5;

    /**
     * Coralogix Region
     */
    private String region = "coralogix.com";

    /**
     * Coralogix API endpoint
     */
    private String apiEndpoint;

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
    public Secret getPrivateKey() {
        return this.privateKey;
    }

    /**
     * Jenkins name getter
     *
     * @return the currently configured Jenkins name
     */
    public String getJenkinsName() {
        return this.jenkinsName;
    }

    /**
     * Jenkins system logs status getter
     *
     * @return system logs sending status
     */
    public Boolean getSystemLogsEnabled() {
        return this.systemLogsEnabled;
    }

    /**
     * Jenkins audit logs status getter
     *
     * @return audit logs sending status
     */
    public Boolean getAuditLogsEnabled() {
        return this.auditLogsEnabled;
    }

    /**
     * Jenkins security logs status getter
     *
     * @return security logs sending status
     */
    public Boolean getSecurityLogsEnabled() {
        return this.securityLogsEnabled;
    }

    /**
     * Jenkins metrics status getter
     *
     * @return metrics sending status
     */
    public Boolean getMetricsEnabled() {
        return this.metricsEnabled;
    }

    /**
     * Jenkins metrics interval getter
     *
     * @return metrics sending interval
     */
    public Integer getMetricsInterval() {
        return this.metricsInterval;
    }

    /**
     * Coralogix Region getter
     *
     * @return the currently configured Coralogix Region
     */
    public String getRegion() {
        return this.region;
    }

    /**
     * Coralogix API endpoint getter
     *
     * @return the currently configured Coralogix API endpoint
     */
    public String getApiEndpoint() {
        return this.apiEndpoint;
    }

    /**
     * Coralogix endpoint builder
     *
     * @return Coralogix endpoint
     */
    public String getCoralogixEndpoint() {
        if (this.region != "custom") {
            return this.region;
        }
        return this.apiEndpoint;
    }

    /**
     * Coralogix Private Key setter
     *
     * @param privateKey the new value of the Private Key
     */
    @DataBoundSetter
    public void setPrivateKey(Secret privateKey) {
        this.privateKey = privateKey;
        save();
    }

    /**
     * Jenkins setter
     *
     * @param jenkinsName the new value of the Jenkins name
     */
    @DataBoundSetter
    public void setJenkinsName(String jenkinsName) {
        this.jenkinsName = jenkinsName;
        save();
    }

    /**
     * Jenkins system logs status setter
     *
     * @param systemLogsEnabled the new status for system logs sending
     */
    @DataBoundSetter
    public void setSystemLogsEnabled(Boolean systemLogsEnabled) {
        this.systemLogsEnabled = systemLogsEnabled;
        save();
    }

    /**
     * Jenkins audit logs status setter
     *
     * @param auditLogsEnabled the new status for audit logs sending
     */
    @DataBoundSetter
    public void setAuditLogsEnabled(Boolean auditLogsEnabled) {
        this.auditLogsEnabled = auditLogsEnabled;
        save();
    }

    /**
     * Jenkins security logs status setter
     *
     * @param securityLogsEnabled the new status for security logs sending
     */
    @DataBoundSetter
    public void setSecurityLogsEnabled(Boolean securityLogsEnabled) {
        this.securityLogsEnabled = securityLogsEnabled;
        save();
    }

    /**
     * Jenkins metrics status setter
     *
     * @param metricsEnabled the new status for metrics sending
     */
    @DataBoundSetter
    public void setMetricsEnabled(Boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
        save();
    }

    /**
     * Jenkins metrics interval setter
     *
     * @param metricsInterval the new interval for metrics sending
     */
    @DataBoundSetter
    public void setMetricsInterval(Integer metricsInterval) {
        this.metricsInterval = metricsInterval;
        save();
    }

    /**
     * Coralogix Region setter
     *
     * @param region the new value of the Coralogix Region
     */
    @DataBoundSetter
    public void setRegion(String region) {
        this.region = region;
        save();
    }

    /**
     * Coralogix API endpoint setter
     *
     * @param apiEndpoint the new value of the Coralogix API endpoint
     */
    @DataBoundSetter
    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
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

    /**
     * Jenkins name validator
     *
     * @param jenkinsName Jenkins name
     * @return Jenkins name validation status
     */
    public FormValidation doCheckJenkinsName(@QueryParameter String jenkinsName) {
        if (StringUtils.isEmpty(jenkinsName)) {
            return FormValidation.error("You must provide the Jenkins name");
        }
        return FormValidation.ok();
    }

    /**
     * Coralogix metrics interval validator
     *
     * @param metricsInterval Jenkins metrics interval
     * @return Jenkins metrics interval validation status
     */
    public FormValidation doCheckMetricsInterval(@QueryParameter Integer metricsInterval) {
        if (metricsInterval < 5) {
            return FormValidation.error("Metrics collector interval should be greater or equals to 5 seconds");
        }
        return FormValidation.ok();
    }

    /**
     * Coralogix API endpoint validator
     *
     * @param apiEndpoint Coralogix API endpoint
     * @return Coralogix API endpoint validation status
     */
    public FormValidation doCheckApiEndpoint(@QueryParameter String apiEndpoint) {
        if (StringUtils.startsWithAny(apiEndpoint, new String[]{"https://", "http://"}) || StringUtils.endsWith(apiEndpoint, "/")) {
            return FormValidation.error("Incorrect Coralogix endpoint");
        }
        return FormValidation.ok();
    }
}