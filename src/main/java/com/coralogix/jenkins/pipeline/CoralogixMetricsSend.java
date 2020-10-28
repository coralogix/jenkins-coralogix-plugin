package com.coralogix.jenkins.pipeline;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudbees.workflow.rest.external.RunExt;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import com.coralogix.jenkins.model.Log;
import com.coralogix.jenkins.utils.CoralogixAPI;
import com.coralogix.jenkins.credentials.CoralogixCredential;

/**
 * Allows to send metrics to Coralogix
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2020-10-27
 */
public class CoralogixMetricsSend extends Step {

    /**
     * Coralogix Private Key
     */
    private final String privateKeyCredentialId;

    /**
     * Application name
     */
    private final String application;

    /**
     * Subsystem name
     */
    private final String subsystem;

    /**
     * Stages metrics splitting
     */
    private final Boolean splitStages;

    /**
     * Initialize pipeline step
     *
     * @param application application name
     */
    @DataBoundConstructor
    public CoralogixMetricsSend(String privateKeyCredentialId, String application, String subsystem, Boolean splitStages) {
        this.privateKeyCredentialId = privateKeyCredentialId;
        this.application = application;
        this.subsystem = subsystem;
        this.splitStages = splitStages;
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
     * Subsystem name getter
     *
     * @return subsystem name
     */
    public String getSubsystem() {
        return this.subsystem;
    }

    /**
     * Stages metrics splitting status getter
     *
     * @return stages metrics splitting status
     */
    public Boolean getSplitStages() {
        return this.splitStages;
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
        return new Execution(context, this.privateKeyCredentialId, this.application, this.subsystem, this.splitStages);
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
         * Coralogix Private Key
         */
        private transient final String privateKeyCredentialId;

        /**
         * Application name
         */
        private transient final String application;

        /**
         * Subsystem name
         */
        private transient final String subsystem;

        /**
         * Stages metrics splitting
         */
        private transient final Boolean splitStages;

        /**
         * Pipeline step executor initialization
         *
         * @param context     execution context
         * @param application application name
         */
        Execution(StepContext context, String privateKeyCredentialId, String application, String subsystem, Boolean splitStages) {
            super(context);
            this.privateKeyCredentialId = privateKeyCredentialId;
            this.application = application;
            this.subsystem = subsystem;
            this.splitStages = splitStages;
        }

        /**
         * Executor step action
         *
         * @return action result
         * @throws Exception
         */
        @Override
        protected Void run() throws Exception {
            WorkflowRun run = getContext().get(WorkflowRun.class);
            Run<?, ?> build = getContext().get(Run.class);
            TaskListener listener = getContext().get(TaskListener.class);
            GsonBuilder builder = new GsonBuilder();
            JsonObject metrics = (JsonObject) builder.create().toJsonTree(RunExt.create(run));
            JsonArray stages = metrics.getAsJsonArray("stages");

            metrics.remove("_links");
            metrics.addProperty("job", build.getParent().getFullName());

            for(int i = 0; i < stages.size(); i++) {
                JsonObject stage = stages.get(i).getAsJsonObject();
                stage.remove("_links");
                stage.remove("stageFlowNodes");
                stages.set(i, stage);
            }

            try {
                List<Log> logEntries = new ArrayList<>();
                if(splitStages) {
                    for(JsonElement stage : stages) {
                        JsonObject record = metrics.deepCopy();
                        record.remove("stages");
                        record.add("stage", stage);
                        logEntries.add(new Log(
                            1,
                            record.toString(),
                            "job",
                            "",
                            "",
                            build.getDisplayName()
                        ));
                    }
                } else {
                    logEntries.add(new Log(
                        1,
                        metrics.toString(),
                        "job",
                        "",
                        "",
                        build.getDisplayName()
                    ));
                }
                CoralogixAPI.sendLogs(
                    CoralogixAPI.retrieveCoralogixCredential(build, privateKeyCredentialId),
                    application,
                    subsystem,
                    logEntries
                );
            } catch (Exception e) {
                listener.getLogger().println("Cannot send pipeline metrics to Coralogix!");
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
            return "Send metrics to Coralogix";
        }

        /**
         * Pipeline step name display value
         *
         * @return pipeline step name
         */
        @Override
        public String getFunctionName() {
            return "coralogixMetricsSend";
        }

        /**
         * Context builder
         *
         * @return execution context
         */
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            Set<Class<?>> contexts = new HashSet<>();
            contexts.add(WorkflowRun.class);
            contexts.add(TaskListener.class);
            contexts.add(Run.class);
            return contexts;
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
    }
}