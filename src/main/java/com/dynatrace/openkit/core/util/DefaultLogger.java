/**
 * Copyright 2018-2021 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core.util;

import com.dynatrace.openkit.api.LogLevel;
import com.dynatrace.openkit.api.Logger;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.dynatrace.openkit.api.LogLevel.DEBUG;
import static com.dynatrace.openkit.api.LogLevel.ERROR;
import static com.dynatrace.openkit.api.LogLevel.INFO;
import static com.dynatrace.openkit.api.LogLevel.WARN;

public class DefaultLogger implements Logger {

    private final LogLevel logLevel;
    private final PrintStream outputStream;

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static String getUTCTime() {
        return DATE_FORMAT.format(new Date());
    }

    public DefaultLogger(LogLevel logLevel) {
        this(logLevel, System.out);
    }

    DefaultLogger(LogLevel logLevel, PrintStream outputStream) {
        this.logLevel = logLevel;
        this.outputStream = outputStream;
    }

    @Override
    public void log(LogLevel level, String message) {
        log(level, message, null);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        if(!level.hasSameOrGreaterPriorityThan(this.logLevel)) {
            return;
        }

        String logEntry = getUTCTime() + " " + level.name() + " [" + Thread.currentThread().getName() + "] " + message;

        if(throwable != null) {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter, true);
            throwable.printStackTrace(printWriter);
            final String stacktrace = stringWriter.getBuffer().toString();

            logEntry += LINE_SEPARATOR + stacktrace;
        }

        outputStream.println(logEntry);
    }

    @Override
    public void error(String message) {
        log(ERROR, message);
    }

    @Override
    public void error(String message, Throwable t) {
        log(ERROR, message, t);
    }

    @Override
    public void warning(String message) {
        log(WARN, message);
    }

    @Override
    public void info(String message) {
        log(INFO, message);
    }

    @Override
    public void debug(String message) {
        log(DEBUG, message);
    }

    @Override
    public boolean isErrorEnabled() {
        return ERROR.hasSameOrGreaterPriorityThan(logLevel);
    }

    @Override
    public boolean isWarnEnabled() {
        return WARN.hasSameOrGreaterPriorityThan(logLevel);
    }

    @Override
    public boolean isInfoEnabled() {
        return INFO.hasSameOrGreaterPriorityThan(logLevel);
    }

    @Override
    public boolean isDebugEnabled() {
        return DEBUG.hasSameOrGreaterPriorityThan(logLevel);
    }
}
