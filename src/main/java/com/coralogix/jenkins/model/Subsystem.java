package com.coralogix.jenkins.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;


/**
 * Subsystem parameter definition
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2019-10-21
 */
public class Subsystem extends AbstractDescribableImpl<Subsystem> {

    /**
     * Subsystem name
     */
    private final String name;

    /**
     * Initialize subsystem
     *
     * @param name subsystem name
     */
    @DataBoundConstructor
    public Subsystem(String name) {
        this.name = name;
    }

    /**
     * Subsystem name getter
     *
     * @return subsystem name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Jenkins parameter definition
     */
    @Extension
    public static final class DescriptorImpl extends Descriptor<Subsystem> {

        /**
         * Subsystem name validator
         *
         * @param name subsystem name
         * @return subsystem name validation status
         * @throws IOException
         */
        public FormValidation doCheckName(@QueryParameter String name) throws IOException {
            if (name.length() == 0) {
                return FormValidation.error("Subsystem name is missed!");
            }
            return FormValidation.ok();
        }

        /**
         * Subsystem name display value
         *
         * @return subsystem name
         */
        @Override
        public String getDisplayName() {
            return "Subsystem name";
        }
    }
}