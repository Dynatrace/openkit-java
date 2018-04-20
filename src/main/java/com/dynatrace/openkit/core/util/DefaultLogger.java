/**
 * Copyright 2018 Dynatrace LLC
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

import com.dynatrace.openkit.api.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DefaultLogger implements Logger {

    private final boolean verbose;

    static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static String getUTCTime() {
        return dateFormat.format(new Date());
    }

    public DefaultLogger(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void error(String message) {
        System.out.println(getUTCTime() + " ERROR [" + Thread.currentThread().getName() + "] " + message);
    }

    @Override
    public void error(String message, Throwable t) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter, true);
        t.printStackTrace(printWriter);
        final String stacktrace = stringWriter.getBuffer().toString();

        System.out.println(getUTCTime() + " ERROR [" + Thread.currentThread().getName() + "] " + message
                + System.getProperty("line.separator") + stacktrace);
    }

    @Override
    public void warning(String message) {
        System.out.println(getUTCTime() + " WARN  [" + Thread.currentThread().getName() + "] " + message);
    }

    @Override
    public void info(String message) {
        if (isInfoEnabled()) {
            System.out.println(getUTCTime() + " INFO  [" + Thread.currentThread().getName() + "] " + message);
        }
    }

    @Override
    public void debug(String message) {
        if (isDebugEnabled()) {
            System.out.println(getUTCTime() + " DEBUG [" + Thread.currentThread().getName() + "] " + message);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return verbose;
    }

    @Override
    public boolean isDebugEnabled() {
        return verbose;
    }


}
