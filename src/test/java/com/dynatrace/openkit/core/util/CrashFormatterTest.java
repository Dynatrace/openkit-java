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

import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class CrashFormatterTest {

    @Test
    public void getNameGivesFullyQualifiedThrowableClassName() {
        // given
        CrashFormatter target = new CrashFormatter(new IllegalArgumentException());

        // when
        String obtained = target.getName();

        // then
        assertThat(obtained, is(equalTo(IllegalArgumentException.class.getName())));
    }

    @Test
    public void getReasonGivesExceptionMessage() {
        // given
        String exceptionMessage = "Austria erit in orbe ultima";
        CrashFormatter target = new CrashFormatter(new IllegalArgumentException(exceptionMessage));

        // when
        String obtained = target.getReason();

        // then
        assertThat(obtained, is(equalTo(exceptionMessage)));
    }

    @Test
    public void getReasonGivesLocalizedExceptionMessageIfMethodIsOverridden() {
        // given
        CrashFormatter target = new CrashFormatter(new LocalizedIllegalArgumentException());

        // when
        String obtained = target.getReason();

        // then
        assertThat(obtained, is(equalTo(LocalizedIllegalArgumentException.LOCALIZED_MESSAGE)));
    }

    @Test
    public void getStackTraceReturnsFormattedStackTrace() {
        // given
        Throwable caught = null;
        try {
            createRecursiveCrash("foobar", 10);
        } catch(Exception e) {
            caught = e;
        }

        assertThat(caught, is(notNullValue()));
        CrashFormatter target = new CrashFormatter(caught);

        // when
        String obtained = target.getStackTrace();

        // then
        assertThat(obtained, is(equalTo(getExpectedStackTrace(caught))));
    }

    private static String getExpectedStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.getBuffer().toString().replace("\r\n", "\n");
    }

    private static void createRecursiveCrash(String message, int callCount) {
        if (callCount > 0) {
            try {
                createRecursiveCrash(message, callCount - 1);
            } catch (Exception e) {
                // get in some nested exception
                if (callCount % 2 == 0) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        throw new UnsupportedOperationException(message);
    }

    private static final class LocalizedIllegalArgumentException extends IllegalArgumentException {

        private static final String LOCALIZED_MESSAGE = "Österreich wird bestehen bis ans Ende der Welt";

        @Override
        public String getLocalizedMessage() {
            return LOCALIZED_MESSAGE;
        }
    }
}
