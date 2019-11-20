package com.coralogix.jenkins;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.ServletException;

import com.coralogix.jenkins.utils.CoralogixAPI;
import com.coralogix.jenkins.model.Log;
import com.coralogix.jenkins.credentials.CoralogixCredential;

/**
 * Jenkins build wrapper definition
 * Allows to send build logs to Coralogix
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2019-10-21
 */
public class CoralogixBuildWrapper extends BuildWrapper {

    /**
     * Coralogix Private Key
     */
    private final String privateKeyCredentialId;

    /**
     * Application name
     */
    private final String application;

    /**
     * Logs splitting
     */
    private Boolean splitLogs = false;

    /**
     * Initialize build wrapper
     *
     * @param application application name
     */
    @DataBoundConstructor
    public CoralogixBuildWrapper(String privateKeyCredentialId, String application) {
        this.privateKeyCredentialId = privateKeyCredentialId;
        this.application = application;
    }

    /**
     * Coralogix Private Key getter
     *
     * @return the currently configured private key
     */
    public String getPrivateKeyCredentialId() {
        return this.privateKeyCredentialId;
    }

    /**
     * Application name getter
     *
     * @return application name
     */
    public String getApplication() {
        return this.application;
    }

    /**
     * Logs splitting status getter
     *
     * @return logs splitting status
     */
    public Boolean getSplitLogs() {
        return this.splitLogs;
    }

    /**
     * Logs splitting status setter
     */
    @DataBoundSetter
    public void setSplitLogs(Boolean splitLogs) {
        this.splitLogs = splitLogs;
    }

    /**
     * Initialize build wrapper environment
     *
     * @param build    build context
     * @param launcher launcher context
     * @param listener build listener context
     * @return build wrapper environment
     */
    @Override
    public Environment setUp(AbstractBuild build, final Launcher launcher, BuildListener listener) {
        return new Environment() {
            /**
             * Build wrapper post action
             *
             * @param build build context
             * @param listener build listener context
             * @return tearDown wrapper
             * @throws IOException
             * @throws InterruptedException
             */
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                try {
                    List<Log> logEntries = new ArrayList<>();
                    List<String> logLines = build.getLog(Integer.MAX_VALUE);
                    if(splitLogs) {
                        for(String logRecordText : logLines) {
                            logEntries.add(new Log(
                                    1,
                                    logRecordText,
                                    "job",
                                    "",
                                    "",
                                    build.getDisplayName()
                            ));
                        }
                    } else {
                        logEntries.add(new Log(
                                1,
                                String.join("\n", logLines),
                                "job",
                                "",
                                "",
                                build.getDisplayName()
                        ));
                    }
                    CoralogixAPI.sendLogs(
                            CoralogixAPI.retrieveCoralogixCredential(build, privateKeyCredentialId),
                            application,
                            build.getParent().getFullName(),
                            logEntries
                    );
                } catch (Exception e) {
                    listener.getLogger().println("Cannot send build logs to Coralogix!");
                }
                return super.tearDown(build, listener);
            }
        };
    }

    /**
     * Jenkins build wrapper definition
     */
    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        /**
         * Coralogix Private Key validator
         *
         * @param privateKeyCredentialId Coralogix Private Key
         * @return Private Key validation status
         */
        public FormValidation doCheckPrivateKeyCredentialId(@QueryParameter String privateKeyCredentialId) {
            if (StringUtils.isEmpty(privateKeyCredentialId)) {
                return FormValidation.error("You must provide the private key");
            }
            return FormValidation.ok();
        }

        /**
         * Application name validator
         *
         * @param application application name
         * @return application name validation status
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckApplication(@QueryParameter String application) throws IOException, ServletException {
            if (application.length() == 0) {
                return FormValidation.error("Application name is missed!");
            }
            return FormValidation.ok();
        }

        /**
         * Coralogix Private Keys list builder
         *
         * @param owner Credentials owner
         * @param uri   Current URL
         * @return allowed credentials list
         */
        @SuppressWarnings("unused")
        public ListBoxModel doFillPrivateKeyCredentialIdItems(@AncestorInPath Item owner,
                                                              @QueryParameter String uri) {
            List<DomainRequirement> domainRequirements = URIRequirementBuilder.fromUri(uri).build();
            return new StandardListBoxModel().includeEmptyValue().includeAs(ACL.SYSTEM, owner, CoralogixCredential.class, domainRequirements);
        }

        /**
         * Check build wrapper applicable
         *
         * @param aClass build wrapper abstract definition
         * @return build wrapper status
         */
        @Override
        public boolean isApplicable(AbstractProject<?, ?> aClass) {
            return true;
        }

        /**
         * Build wrapper display value
         *
         * @return build wrapper name
         */
        @Override
        public String getDisplayName() {
            return "Send build logs to Coralogix";
        }
    }
}