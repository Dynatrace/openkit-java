package com.dynatrace.openkit.providers;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultSessionIDProvider implements SessionIDProvider {

    private int initialIntegerOffset;

    DefaultSessionIDProvider(int initialOffset)
    {
        this.initialIntegerOffset = initialOffset;
    }

    public DefaultSessionIDProvider(){
        this(new Random(System.currentTimeMillis()).nextInt(Integer.MAX_VALUE));
    }

    @Override
    public synchronized int getNextSessionID() {
        if(initialIntegerOffset == Integer.MAX_VALUE) {
            initialIntegerOffset = 0;
        }
        initialIntegerOffset = initialIntegerOffset + 1;
        return initialIntegerOffset;
    }


}
