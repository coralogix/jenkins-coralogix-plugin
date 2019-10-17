package com.coralogix.jenkins.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Coralogix API log record
 *
 * @author Eldar Aliiev
 * @version 1.0.0
 * @since 2019-10-21
 */
@SuppressFBWarnings(value = "URF_UNREAD_FIELD")
public class Log {

    /**
     * Log record timestamp
     */
    private long timestamp;

    /**
     * Log record severity level
     */
    private int severity;

    /**
     * Log record message
     */
    private String text;

    /**
     * Log record category
     */
    private String category;

    /**
     * Log record class name
     */
    private String className;

    /**
     * Log record method name
     */
    private String methodName;

    /**
     * Thread ID
     */
    private String threadId;

    /**
     * Initialize log record
     *
     * @param severity   record severity level
     * @param text       record message
     * @param category   record category
     * @param className  record class name
     * @param methodName record method name
     * @param threadId   Thread ID
     */
    public Log(int severity, String text, String category, String className, String methodName, String threadId) {
        this.timestamp = System.currentTimeMillis();
        this.severity = severity;
        this.text = text;
        this.category = category;
        this.className = className;
        this.methodName = methodName;
        this.threadId = threadId;
    }
}