package com.dynatrace.openkit.test;

import com.dynatrace.openkit.core.util.DefaultLogger;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultLoggerTest {

    @Test
    public void DefaultLoggerWithVerboseOutputWritesErrorLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(true);

        //then
        assertThat(log.isErrorEnabled(), is(true));
    }

    @Test
    public void DefaultLoggerWithVerboseOutputWritesWarnLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(true);

        //then
        assertThat(log.isWarnEnabled(), is(true));
    }

    @Test
    public void DefaultLoggerWithVerboseOutputWritesInfoLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(true);

        //then
        assertThat(log.isInfoEnabled(), is(true));
    }

    @Test
    public void DefaultLoggerWithVerboseOutputWritesDebugLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(true);

        //then
        assertThat(log.isDebugEnabled(), is(true));
    }

    @Test
    public void DefaultLoggerWithoutVerboseOutputWritesErrorLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(false);

        //then
        assertThat(log.isErrorEnabled(), is(true));
    }

    @Test
    public void DefaultLoggerWithoutVerboseOutputWritesWarnLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(false);

        //then
        assertThat(log.isWarnEnabled(), is(true));
    }

    @Test
    public void DefaultLoggerWithoutVerboseOutputWritesInfoLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(false);

        //then
        assertThat(log.isInfoEnabled(), is(false));
    }

    @Test
    public void DefaultLoggerWithoutVerboseOutputWritesDebugLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(false);

        //then
        assertThat(log.isDebugEnabled(), is(false));
    }
}
