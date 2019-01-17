package com.dynatrace.openkit.api;

public enum Level {
    DEBUG(0),
    INFO(10),
    WARN(20),
    ERROR(30);

    private final int priority;

    Level(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public boolean hasSameOrGreaterPriorityThan(Level other){
        return getPriority() >= other.getPriority();
    }
}
