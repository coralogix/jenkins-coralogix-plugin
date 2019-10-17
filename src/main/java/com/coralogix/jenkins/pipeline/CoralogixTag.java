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
import java.util.stream.Collectors;

import com.coralogix.jenkins.CoralogixConfiguration;
import com.coralogix.jenkins.model.Subsystem;
import com.coralogix.jenkins.utils.CoralogixAPI;

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
     * Initialize pipeline step
     *
     * @param tag         tag name
     * @param application application name
     * @param subsystems  subsystems name
     * @param icon        tag icon
     */
    @DataBoundConstructor
    public CoralogixTag(String tag, String application, List<Subsystem> subsystems, String icon) {
        this.tag = tag;
        this.application = application;
        this.subsystems = subsystems;
        this.icon = icon;
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
     * Start step execution
     *
     * @param context execution context
     * @return execution step
     * @throws Exception
     */
    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(context, this.tag, this.application, this.subsystems, this.icon);
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
         * Tag name
         */
        private transient final String tag;

        /**
         * Application name
         */
        private transient final String application;

        /**
         * Tag icon
         */
        private transient final String icon;

        /**
         * Subsystems list
         */
        private transient final List<Subsystem> subsystems;

        /**
         * Pipeline step executor initialization
         *
         * @param context     execution context
         * @param tag         tag name
         * @param application application name
         * @param subsystems  subsystems name
         * @param icon        tag icon
         */
        Execution(StepContext context, String tag, String application, List<Subsystem> subsystems, String icon) {
            super(context);
            this.tag = tag;
            this.application = application;
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
            TaskListener listener = getContext().get(TaskListener.class);

            try {
                CoralogixAPI.pushTag(
                        CoralogixConfiguration.get().getPrivateKey(),
                        application,
                        subsystems.stream().map(Subsystem::getName).collect(Collectors.joining(",")),
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
    }
}