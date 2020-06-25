/**
 * Copyright 2018-2020 Dynatrace LLC
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import static com.dynatrace.openkit.api.LogLevel.DEBUG;
import static com.dynatrace.openkit.api.LogLevel.WARN;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

public class DefaultLoggerTest {

    private static final String CHARSET = "UTF-8";
    private static final String LOGGER_DATE_TIME_PATTERN = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}";

    private ByteArrayOutputStream byteArrayOutputStream;
    private PrintStream printStream;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        byteArrayOutputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(byteArrayOutputStream, true, CHARSET);
    }

    @After
    public void tearDown() {
        printStream.close();
    }

    @Test
    public void defaultLoggerWithVerboseOutputWritesErrorLevelMessages() {
        //given
        DefaultLogger target = new DefaultLogger(DEBUG);

        //then
        assertThat(target.isErrorEnabled(), is(true));
    }

    @Test
    public void errorLogsAppropriateMessage() throws UnsupportedEncodingException {

        //given
        DefaultLogger target = new DefaultLogger(DEBUG, printStream);

        // when
        target.error("Error message");
        String obtained = byteArrayOutputStream.toString(CHARSET).trim();

        // then
        assertThat(Pattern.matches("^" + LOGGER_DATE_TIME_PATTERN + " ERROR \\[.*?] Error message$", obtained),
            is(true));
    }

    @Test
    public void errorWithStacktraceLogsAppropriateMessage() throws UnsupportedEncodingException {

        //given
        Exception e = new Exception("test exception");
        DefaultLogger target = new DefaultLogger(DEBUG, printStream);

        // when
        target.error("Error message", e);
        String[] obtained = byteArrayOutputStream.toString(CHARSET).trim().split(System.getProperty("line.separator"), 2);

        // then
        assertThat(obtained.length, is(equalTo(2)));
        assertThat(Pattern.matches("^" + LOGGER_DATE_TIME_PATTERN + " ERROR \\[.*?] Error message$", obtained[0]),
            is(true));

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter, true);
        e.printStackTrace(printWriter);
        final String expectedStacktrace = stringWriter.getBuffer().toString().trim();
        assertThat(obtained[1], is(equalTo(expectedStacktrace)));
    }

    @Test
    public void warningLogsAppropriateMessage() throws UnsupportedEncodingException {

        //given
        DefaultLogger target = new DefaultLogger(DEBUG, printStream);

        // when
        target.warning("Warning message");
        String obtained = byteArrayOutputStream.toString(CHARSET).trim();

        // then
        assertThat(Pattern.matches("^" + LOGGER_DATE_TIME_PATTERN + " WARN \\[.*?] Warning message$", obtained),
            is(true));
    }

    @Test
    public void infoLogsAppropriateMessage() throws UnsupportedEncodingException {

        //given
        DefaultLogger target = new DefaultLogger(DEBUG, printStream);

        // when
        target.info("Info message");
        String obtained = byteArrayOutputStream.toString(CHARSET).trim();

        // then
        assertThat(Pattern.matches("^" + LOGGER_DATE_TIME_PATTERN + " INFO \\[.*?] Info message$", obtained),
            is(true));
    }

    @Test
    public void infoDoesNotLogIfVerboseIsDisabled() throws UnsupportedEncodingException {

        //given
        DefaultLogger target = new DefaultLogger(WARN, printStream);

        // when
        target.info("Info message");
        String obtained = byteArrayOutputStream.toString(CHARSET);

        // then
        assertThat(obtained, isEmptyString());
    }

    @Test
    public void debugLogsAppropriateMessage() throws UnsupportedEncodingException {

        //given
        DefaultLogger target = new DefaultLogger(DEBUG, printStream);

        // when
        target.debug("Debug message");
        String obtained = byteArrayOutputStream.toString(CHARSET).trim();

        // then
        assertThat(Pattern.matches("^" + LOGGER_DATE_TIME_PATTERN + " DEBUG \\[.*?] Debug message$", obtained),
            is(true));
    }

    @Test
    public void debugDoesNotLogIfVerboseIsDisabled() throws UnsupportedEncodingException {

        //given
        DefaultLogger target = new DefaultLogger(WARN, printStream);

        // when
        target.debug("Debug message");
        String obtained = byteArrayOutputStream.toString(CHARSET);

        // then
        assertThat(obtained, isEmptyString());
    }

    @Test
    public void isErrorEnabledIsTrueIfVerboseIsTrue() {

        // then
        assertThat(new DefaultLogger(DEBUG).isErrorEnabled(), is(true));
    }

    @Test
    public void isErrorEnabledIsTrueIfVerboseIsFalse() {

        // then
        assertThat(new DefaultLogger(WARN).isErrorEnabled(), is(true));
    }

    @Test
    public void isWarnEnabledIsTrueIfVerboseIsTrue() {

        // then
        assertThat(new DefaultLogger(DEBUG).isWarnEnabled(), is(true));
    }

    @Test
    public void isWarnEnabledIsTrueIfVerboseIsFalse() {

        // then
        assertThat(new DefaultLogger(WARN).isWarnEnabled(), is(true));
    }

    @Test
    public void isInfoEnabledIsTrueIfVerboseIsTrue() {

        // then
        assertThat(new DefaultLogger(DEBUG).isInfoEnabled(), is(true));
    }

    @Test
    public void isInfoEnabledIsFalseIfVerboseIsFalse() {

        // then
        assertThat(new DefaultLogger(WARN).isInfoEnabled(), is(false));
    }

    @Test
    public void isDebugEnabledIsTrueIfVerboseIsTrue() {

        // then
        assertThat(new DefaultLogger(DEBUG).isDebugEnabled(), is(true));
    }

    @Test
    public void isDebugEnabledIsFalseIfVerboseIsFalse() {

        // then
        assertThat(new DefaultLogger(WARN).isDebugEnabled(), is(false));
    }
}
