package com.dynatrace.openkit.core.util;

import com.dynatrace.openkit.api.Logger;

public class DefaultLogger implements Logger {

    private boolean verbose;

    public DefaultLogger(boolean verbose)
    {
        this.verbose = verbose;
    }

    @Override
    public void error(String message) {
        System.err.print("[ERROR] " + message + "\n");
    }

    @Override
    public void error(String message, Throwable t)
    {
        System.err.print("[ERROR] " + message + " [" + t.toString() + "]");
    }

    @Override
    public void warning(String message) {
        System.out.print(" [WARN] " + message + "\n");
    }

    @Override
    public void info(String message) {
        System.out.print(" [INFO] " + message + "\n");
    }

    @Override
    public void debug(String message) {
        System.out.print("[DEBUG] " + message + "\n");
    }

    public boolean isErrorEnabled() {
        return true;
    }

    public boolean isWarnEnabled() {
        return true;
    }

    public boolean isInfoEnabled() {
        return verbose;
    }

    public boolean isDebugEnabled() {
       return verbose;
    }


}
