package com.coralogix.jenkins.pipeline;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import com.coralogix.jenkins.model.Application;
import com.coralogix.jenkins.model.Subsystem;
import com.coralogix.jenkins.utils.CoralogixAPI;
import com.coralogix.jenkins.credentials.CoralogixApiCredential;

/**
 * Pipeline counterpart of the CoralogixBuildWrapper.
 * Allows to push tags to Coralogix
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2019-10-21
 */
public class CoralogixTag extends Step {

    /**
     * Coralogix API Key
     */
    private final String apiKeyCredentialId;

    /**
     * Tag name
     */
    private final String tag;

    /**
     * Applications list
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
     * Initialize pipeline step
     *
     * @param tag         tag name
     * @param applications applications name
     * @param subsystems  subsystems name
     * @param icon        tag icon
     */
    @DataBoundConstructor
    public CoralogixTag(String apiKeyCredentialId, String tag, List<Application> applications, List<Subsystem> subsystems, String icon) {
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
     * Start step execution
     *
     * @param context execution context
     * @return execution step
     * @throws Exception
     */
    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(context, this.apiKeyCredentialId, this.tag, this.applications, this.subsystems, this.icon);
    }

    /**
     * Pipeline step executor
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {

        /**
         * Serial UID
         */
        private static final long serialVersionUID = 1L;

        /**
         * Coralogix API Key
         */
        private transient final String apiKeyCredentialId;

        /**
         * Tag name
         */
        private transient final String tag;

        /**
         * Applications list
         */
        private transient final List<Application> applications;

        /**
         * Subsystems list
         */
        private transient final List<Subsystem> subsystems;

        /**
         * Tag icon
         */
        private transient final String icon;

        /**
         * Pipeline step executor initialization
         *
         * @param context     execution context
         * @param tag         tag name
         * @param applications applications names
         * @param subsystems  subsystems names
         * @param icon        tag icon
         */
        Execution(StepContext context, String apiKeyCredentialId, String tag, List<Application> applications, List<Subsystem> subsystems, String icon) {
            super(context);
            this.apiKeyCredentialId = apiKeyCredentialId;
            this.tag = tag;
            this.applications = applications;
            this.subsystems = subsystems;
            this.icon = icon;
        }

        /**
         * Executor step action
         *
         * @return action result
         * @throws Exception
         */
        @Override
        protected Void run() throws Exception {
            Run<?, ?> build = getContext().get(Run.class);
            TaskListener listener = getContext().get(TaskListener.class);
            try {
                CoralogixAPI.pushTag(
                    CoralogixAPI.retrieveCoralogixApiCredential(build, apiKeyCredentialId),
                    applications.stream().map(Application::getName).collect(Collectors.toList()),
                    subsystems.stream().map(Subsystem::getName).collect(Collectors.toList()),
                    tag,
                    icon
                );
            } catch (Exception e) {
                listener.getLogger().println("Cannot push tag to Coralogix!");
            }

            return null;
        }
    }

    /**
     * Jenkins pipeline step definition
     */
    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        /**
         * Pipeline step description display value
         *
         * @return pipeline step description
         */
        @Override
        public String getDisplayName() {
            return "Push tag to Coralogix";
        }

        /**
         * Pipeline step name display value
         *
         * @return pipeline step name
         */
        @Override
        public String getFunctionName() {
            return "coralogixTag";
        }

        /**
         * Context builder
         *
         * @return execution context
         */
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            Set<Class<?>> contexts = new HashSet<>();
            contexts.add(TaskListener.class);
            contexts.add(Run.class);
            return contexts;
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
    }
}