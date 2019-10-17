package com.coralogix.jenkins.pipeline;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import com.coralogix.jenkins.model.Log;
import com.coralogix.jenkins.utils.CoralogixAPI;

/**
 * Pipeline counterpart of the CoralogixBuildWrapper.
 * Allows to send logs to Coralogix
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2019-10-21
 */
public class CoralogixSend extends Step {

    /**
     * Application name
     */
    private final String application;

    /**
     * Initialize pipeline step
     *
     * @param application application name
     */
    @DataBoundConstructor
    public CoralogixSend(String application) {
        this.application = application;
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
     * Start step execution
     *
     * @param context execution context
     * @return execution step
     * @throws Exception
     */
    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(context, this.application);
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
         * Application name
         */
        private transient final String application;

        /**
         * Pipeline step executor initialization
         *
         * @param context execution context
         * @param application application name
         */
        Execution(StepContext context, String application) {
            super(context);
            this.application = application;
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
                List<Log> logEntries = new ArrayList<>();
                List<String> logLines = build.getLog(Integer.MAX_VALUE);
                logEntries.add(new Log(
                        1,
                        String.join("\n", logLines),
                        "job",
                        "",
                        "",
                        build.getDisplayName()
                ));
                CoralogixAPI.sendLogs(application, build.getParent().getFullName(), logEntries);
            } catch (Exception e) {
                listener.getLogger().println("Cannot send build logs to Coralogix!");
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
            return "Send logs to Coralogix";
        }

        /**
         * Pipeline step name display value
         *
         * @return pipeline step name
         */
        @Override
        public String getFunctionName() {
            return "coralogixSend";
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
    }
}