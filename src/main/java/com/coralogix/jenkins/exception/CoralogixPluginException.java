package com.coralogix.jenkins.exception;

/**
 * Coralogix plugin exception
 *
 * @author Eldar Aliiev
 * @version 1.1.0
 * @since 2019-11-11
 */
public class CoralogixPluginException extends RuntimeException {

    /**
     * Exception method
     *
     * @param message exception message
     */
    public CoralogixPluginException(String message) {
        super(message);
    }

    /**
     * Exception method
     *
     * @param message exception message
     * @param cause   exception metadata
     */
    public CoralogixPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}