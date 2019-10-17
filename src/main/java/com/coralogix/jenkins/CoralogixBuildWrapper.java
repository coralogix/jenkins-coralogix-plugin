package com.coralogix.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.util.FormValidation;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.ServletException;

import com.coralogix.jenkins.utils.CoralogixAPI;
import com.coralogix.jenkins.model.Log;


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
     * Application name
     */
    private final String application;

    /**
     * Initialize build wrapper
     *
     * @param application application name
     */
    @DataBoundConstructor
    public CoralogixBuildWrapper(String application) {
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