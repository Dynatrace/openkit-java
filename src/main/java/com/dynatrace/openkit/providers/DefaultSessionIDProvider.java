package com.dynatrace.openkit.providers;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultSessionIDProvider implements SessionIDProvider {

    private int initialIntegerOffset;

    public DefaultSessionIDProvider(){
        Random generator = new Random(System.currentTimeMillis());
        this.initialIntegerOffset = generator.nextInt(Integer.MAX_VALUE );
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
