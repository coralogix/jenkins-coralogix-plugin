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

import com.coralogix.jenkins.model.Application;
import com.coralogix.jenkins.model.Subsystem;
import com.coralogix.jenkins.utils.CoralogixAPI;
import com.coralogix.jenkins.credentials.CoralogixApiCredential;

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
     * Coralogix API Key
     */
    private final String apiKeyCredentialId;

    /**
     * Tag name
     */
    private final String tag;

    /**
     * Application name
     */
    private final List<Application> applications;

    /**
     * Subsystems list
     */
    private final List<Subsystem> subsystems;

    /**
     * Tag icon
     */
    private final String icon;

    /**
     * Initialize build step
     *
     * @param tag         tag name
     * @param applications applications names
     * @param subsystems  subsystems names
     * @param icon        tag icon
     */
    @DataBoundConstructor
    public CoralogixBuildStep(String apiKeyCredentialId, String tag, List<Application> applications, List<Subsystem> subsystems, String icon) {
        this.apiKeyCredentialId = apiKeyCredentialId;
        this.tag = tag;
        this.applications = applications;
        this.subsystems = subsystems;
        this.icon = icon;
    }

    /**
     * Coralogix API Key getter
     *
     * @return the currently configured API key
     */
    public String getApiKeyCredentialId() {
        return this.apiKeyCredentialId;
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
     * Applications list getter
     *
     * @return applications list
     */
    public List<Application> getApplications() {
        return this.applications;
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
                CoralogixAPI.retrieveCoralogixApiCredential(run, apiKeyCredentialId),
                applications.stream().map(application -> CoralogixAPI.replaceMacros(run, listener, application.getName())).collect(Collectors.toList()),
                subsystems.stream().map(subsystem -> CoralogixAPI.replaceMacros(run, listener, subsystem.getName())).collect(Collectors.toList()),
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
         * Coralogix API Key validator
         *
         * @param apiKeyCredentialId Coralogix API Key
         * @return API Key validation status
         */
        public FormValidation doCheckApiKeyCredentialId(@QueryParameter String apiKeyCredentialId) {
            if (StringUtils.isEmpty(apiKeyCredentialId)) {
                return FormValidation.error("You must provide the API key");
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
         * Coralogix API Keys list builder
         *
         * @param owner Credentials owner
         * @param uri   Current URL
         * @return allowed credentials list
         */
        @SuppressWarnings("unused")
        public ListBoxModel doFillApiKeyCredentialIdItems(@AncestorInPath Item owner,
                                                          @QueryParameter String uri) {
            List<DomainRequirement> domainRequirements = URIRequirementBuilder.fromUri(uri).build();
            return new StandardListBoxModel().includeEmptyValue().includeAs(ACL.SYSTEM, owner, CoralogixApiCredential.class, domainRequirements);
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