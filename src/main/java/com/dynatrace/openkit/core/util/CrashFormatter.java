/**
 * Copyright 2018-2020 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Utility class used to format a given {@link Throwable} into a format usable for OpenKit.
 */
public class CrashFormatter {

    private final Throwable throwable;

    public CrashFormatter(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getName() {
        return throwable.getClass().getName();
    }

    public String getReason() {
        // note: throwable.toString() will also use getLocalizedMessage()
        return throwable.getLocalizedMessage();
    }

    public String getStackTrace() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new StackTracePrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    private static final class StackTracePrintWriter extends PrintWriter {

        private static final char NEWLINE = '\n';

        private StackTracePrintWriter(Writer out) {
            super(out, true);
        }

        @Override
        public void println() {
            write(NEWLINE);
        }
    }
}
