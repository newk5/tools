package com.maxorator.vcmp.java.tools.timers;

public interface TimerHandle {
    boolean isActive();

	long getIterations();

    void cancel();
}
