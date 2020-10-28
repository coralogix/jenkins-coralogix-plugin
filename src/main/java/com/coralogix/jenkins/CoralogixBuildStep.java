package com.coralogix.jenkins;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.coralogix.jenkins.model.Subsystem;
import com.coralogix.jenkins.utils.CoralogixAPI;
import com.coralogix.jenkins.credentials.CoralogixCredential;

/**
 * Jenkins build step definition
 * Allows to push tags to Coralogix
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2019-10-21
 */
public class CoralogixBuildStep extends Builder implements SimpleBuildStep {

    /**
     * Coralogix Private Key
     */
    private final String privateKeyCredentialId;

    /**
     * Tag name
     */
    private final String tag;

    /**
     * Application name
     */
    private final String application;

    /**
     * Tag icon
     */
    private final String icon;

    /**
     * Subsystems list
     */
    private final List<Subsystem> subsystems;

    /**
     * Initialize build step
     *
     * @param tag         tag name
     * @param application application name
     * @param subsystems  subsystems name
     * @param icon        tag icon
     */
    @DataBoundConstructor
    public CoralogixBuildStep(String privateKeyCredentialId, String tag, String application, List<Subsystem> subsystems, String icon) {
        this.privateKeyCredentialId = privateKeyCredentialId;
        this.tag = tag;
        this.application = application;
        this.subsystems = subsystems;
        this.icon = icon;
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
     * Tag name getter
     *
     * @return tag name
     */
    public String getTag() {
        return this.tag;
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
     * Subsystems list getter
     *
     * @return subsystems list
     */
    public List<Subsystem> getSubsystems() {
        return this.subsystems;
    }

    /**
     * Tag icon getter
     *
     * @return tag icon
     */
    public String getIcon() {
        return this.icon;
    }

    /**
     * Pipeline step action
     *
     * @param run       build context
     * @param workspace workspace context
     * @param launcher  launcher context
     * @param listener  build listener context
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        try {
            CoralogixAPI.pushTag(
                CoralogixAPI.retrieveCoralogixCredential(run, privateKeyCredentialId),
                CoralogixAPI.replaceMacros(run, listener, application),
                CoralogixAPI.replaceMacros(run, listener,
                        subsystems.stream().map(Subsystem::getName).collect(Collectors.joining(","))
                ),
                CoralogixAPI.replaceMacros(run, listener, tag),
                icon
            );
        } catch (Exception e) {
            listener.getLogger().println("Cannot push tag to Coralogix!");
        }
    }

    /**
     * Jenkins build step definition
     */
    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

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
         * Tag name validator
         *
         * @param tag tag name
         * @return tag name validation status
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckTag(@QueryParameter String tag) throws IOException, ServletException {
            if (tag.length() == 0) {
                return FormValidation.error("Tag name is missed!");
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
         * Check build step applicable
         *
         * @param aClass build step abstract definition
         * @return build step status
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * Build step display value
         *
         * @return build step name
         */
        @Override
        public String getDisplayName() {
            return "Push Coralogix tag";
        }
    }
}