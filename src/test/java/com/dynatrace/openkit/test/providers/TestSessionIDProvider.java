package com.dynatrace.openkit.test.providers;

import com.dynatrace.openkit.providers.SessionIDProvider;

import java.util.Random;

public class TestSessionIDProvider implements SessionIDProvider {

    private int initialIntegerOffset = 0;

    @Override
    public synchronized int getNextSessionID() {
        if(initialIntegerOffset == Integer.MAX_VALUE) {
            initialIntegerOffset = 0;
        }
        initialIntegerOffset = initialIntegerOffset + 1;
        return initialIntegerOffset;
    }
}