package com.dynatrace.openkit.test;

import com.dynatrace.openkit.core.util.DefaultLogger;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultLoggerTest {

    @Test
    public void defaultLoggerWithVerboseOutputWritesErrorLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(true);

        //then
        assertThat(log.isErrorEnabled(), is(true));
    }

    @Test
    public void defaultLoggerWithVerboseOutputWritesWarnLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(true);

        //then
        assertThat(log.isWarnEnabled(), is(true));
    }

    @Test
    public void defaultLoggerWithVerboseOutputWritesInfoLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(true);

        //then
        assertThat(log.isInfoEnabled(), is(true));
    }

    @Test
    public void defaultLoggerWithVerboseOutputWritesDebugLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(true);

        //then
        assertThat(log.isDebugEnabled(), is(true));
    }

    @Test
    public void defaultLoggerWithoutVerboseOutputWritesErrorLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(false);

        //then
        assertThat(log.isErrorEnabled(), is(true));
    }

    @Test
    public void defaultLoggerWithoutVerboseOutputWritesWarnLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(false);

        //then
        assertThat(log.isWarnEnabled(), is(true));
    }

    @Test
    public void defaultLoggerWithoutVerboseOutputWritesInfoLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(false);

        //then
        assertThat(log.isInfoEnabled(), is(false));
    }

    @Test
    public void defaultLoggerWithoutVerboseOutputWritesDebugLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(false);

        //then
        assertThat(log.isDebugEnabled(), is(false));
    }
}
