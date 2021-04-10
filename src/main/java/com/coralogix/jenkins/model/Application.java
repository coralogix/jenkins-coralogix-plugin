package com.coralogix.jenkins.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;


/**
 * Application parameter definition
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2021-04-10
 */
public class Application extends AbstractDescribableImpl<Application> {

    /**
     * Application name
     */
    private final String name;

    /**
     * Initialize application
     *
     * @param name application name
     */
    @DataBoundConstructor
    public Application(String name) {
        this.name = name;
    }

    /**
     * Application name getter
     *
     * @return application name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Jenkins parameter definition
     */
    @Extension
    public static final class DescriptorImpl extends Descriptor<Application> {

        /**
         * Application name validator
         *
         * @param name application name
         * @return application name validation status
         * @throws IOException
         */
        public FormValidation doCheckName(@QueryParameter String name) throws IOException {
            if (name.length() == 0) {
                return FormValidation.error("Application name is missed!");
            }
            return FormValidation.ok();
        }

        /**
         * Application name display value
         *
         * @return application name
         */
        @Override
        public String getDisplayName() {
            return "Application name";
        }
    }
}