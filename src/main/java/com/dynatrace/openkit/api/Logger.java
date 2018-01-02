package com.dynatrace.openkit.api;

/**
 * This interface provides logging functionality to OpenKit. By subclassing OpenKit
 * can make use of custom loggers.
 */
public interface Logger {

    /**
     * Log with level 'error'
     * @param message the message to write to the log
     */
    void error(String message);

    void error(String message, Throwable t);

    /**
     * Log with level 'warning'
     * @param message the message to write to the log
     */
    void warning(String message);

    /**
     * Log with level 'info'
     * @param message the message to write to the log
     */
    void info(String message);

    /**
     * Log with level 'debug'
     * @param message the message to write to the log
     */
    void debug(String message);

    /**
     * Return a flag if 'error' level messages are currently printed
     * @return true if 'error' level messages are printed, 'false' if not
     */
    boolean isErrorEnabled();

    /**
     * Return a flag if 'warn' level messages are currently printed
     * @return true if 'warn' level messages are printed, 'false' if not
     */
    boolean isWarnEnabled();

    /**
     * Return a flag if 'info' level messages are currently printed
     * @return true if 'info' level messages are printed, 'false' if not
     */
    boolean isInfoEnabled();

    /**
     * Return a flag if 'debug' level messages are currently printed
     * @return true if 'debug' level messages are printed, 'false' if not
     */
    boolean isDebugEnabled();


}
