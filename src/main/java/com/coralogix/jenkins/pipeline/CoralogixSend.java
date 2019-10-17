package com.coralogix.jenkins.pipeline;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import api.CoralogixLogger;

import com.coralogix.jenkins.CoralogixConfiguration;

/**
 * Pipeline counterpart of the CoralogixBuildWrapper.
 */
public class CoralogixSend extends Step {

    private final String application;

    @DataBoundConstructor
    public CoralogixSend(String application) {
        this.application = application;
    }

    public String getApplication() {
        return this.application;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(context, this.application);
    }

    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {

        private transient final String application;

        Execution(StepContext context, String application) {
            super(context);
            this.application = application;
        }

        @Override
        protected Void run() throws Exception {
            Run<?, ?> build = getContext().get(Run.class);
            List<String> logLines = build.getLog(Integer.MAX_VALUE);

            CoralogixLogger.configure(
                    CoralogixConfiguration.get().getPrivateKey(),
                    application,
                    build.getParent().getFullName()
            );

            CoralogixLogger logger = new CoralogixLogger("job");

            for (String message : logLines) {
                logger.debug(message, "", "", build.getDisplayName());
            }

            return null;
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getDisplayName() {
            return "Send logs to Coralogix";
        }

        @Override
        public String getFunctionName() {
            return "coralogixSend";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            Set<Class<?>> contexts = new HashSet<>();
            contexts.add(TaskListener.class);
            contexts.add(Run.class);
            return contexts;
        }
    }
}