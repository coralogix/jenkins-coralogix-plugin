package com.coralogix.jenkins;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.Executor;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.util.FormValidation;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Stack;
import java.util.List;
import javax.servlet.ServletException;
import org.apache.commons.lang.StringUtils;
import api.CoralogixLogger;

import com.coralogix.jenkins.CoralogixConfiguration;

public class CoralogixBuildWrapper extends BuildWrapper {

    private final String application;

    @DataBoundConstructor
    public CoralogixBuildWrapper(String application) {
        this.application = application;
    }

    public String getApplication() {
        return this.application;
    }

    @Override
    public Environment setUp(AbstractBuild build, final Launcher launcher, BuildListener listener) {
        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                List<String> logLines = build.getLog(Integer.MAX_VALUE);

                CoralogixLogger.configure(
                        CoralogixConfiguration.get().getPrivateKey(),
                        application,
                        build.getParent().getFullName()
                );

                CoralogixLogger logger = new CoralogixLogger("job");

                for(String message: logLines) {
                    logger.debug(message, "", "", build.getDisplayName());
                }

                return super.tearDown(build, listener);
            }
        };
    }

    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        public FormValidation doCheckApplication(@QueryParameter String application) throws IOException, ServletException {
            if (application.length() == 0) {
                return FormValidation.error("Application name is missed!");
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Send build logs to Coralogix";
        }

    }
}