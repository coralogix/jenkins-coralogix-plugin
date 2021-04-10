package com.coralogix.jenkins.model;

import java.util.List;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Coralogix API Tag definition
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2021-04-10
 */
@SuppressFBWarnings(value = "URF_UNREAD_FIELD")
public class Tag {

    /**
     * Tag timestamp
     */
    private Long timestamp;

    /**
     * Tag name
     */
    private String name;

    /**
     * Applications list
     */
    private List<String> application;

    /**
     * Subsystems list
     */
    private List<String> subsystem;

    /**
     * Tag icon
     */
    private String iconUrl;

    /**
     * Initialize tag
     *
     * @param name          tag name
     * @param applications  applications list
     * @param subsystems    subsystems list
     */
    public Tag(String name, List<String> applications, List<String> subsystems, String iconUrl) {
        this.timestamp = System.currentTimeMillis();
        this.name = name;
        this.application = applications;
        this.subsystem = subsystems;
        this.iconUrl = iconUrl;
    }
}