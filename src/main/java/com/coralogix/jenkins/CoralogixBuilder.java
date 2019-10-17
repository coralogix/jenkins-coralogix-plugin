package com.coralogix.jenkins;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

public class CoralogixBuilder extends Builder implements SimpleBuildStep {

    private final String tag;
    private final String application;
    private final String icon;
    private final List<String> subsystems;

    @DataBoundConstructor
    public CoralogixBuilder(String tag, String application, List<String> subsystems, String icon) {
        this.tag = tag;
        this.application = application;
        this.subsystems = subsystems;
        this.icon = icon;
    }

    public String getTag() {
        return this.tag;
    }

    public String getApplication() {
        return this.application;
    }

    public List<String> getSubsystems() {
        return this.subsystems;
    }

    public String getIcon() {
        return this.icon;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Hello, " + tag + "!");
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckTag(@QueryParameter String tag) throws IOException, ServletException {
            if (tag.length() == 0) {
                return FormValidation.error("Tag name is missed!");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckApplication(@QueryParameter String application) throws IOException, ServletException {
            if (application.length() == 0) {
                return FormValidation.error("Application name is missed!");
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Push Coralogix tag";
        }
    }
}