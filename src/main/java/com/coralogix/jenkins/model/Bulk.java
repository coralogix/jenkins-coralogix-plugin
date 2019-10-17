package com.coralogix.jenkins.model;

import java.util.List;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Coralogix API bulk definition
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2019-10-21
 */
@SuppressFBWarnings(value = "URF_UNREAD_FIELD")
public class Bulk {

    /**
     * Coralogix Private Key
     */
    private String privateKey;

    /**
     * Application name
     */
    private String applicationName;

    /**
     * Subsystem name
     */
    private String subsystemName;

    /**
     * Hostname
     */
    private String computerName;

    /**
     * Logs list
     */
    private List<Log> logEntries;

    /**
     * Initialize logs bulk
     *
     * @param privateKey      Coralogix Private Key
     * @param applicationName application name
     * @param subsystemName   subsystem name
     * @param computerName    hostname
     * @param logEntries      logs bunch
     */
    public Bulk(String privateKey, String applicationName, String subsystemName, String computerName, List<Log> logEntries) {
        this.privateKey = privateKey;
        this.applicationName = applicationName;
        this.subsystemName = subsystemName;
        this.computerName = computerName;
        this.logEntries = logEntries;
    }
}