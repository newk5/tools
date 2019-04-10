package com.maxorator.vcmp.java.tools.timers;

public class TimerHandleImpl implements TimerHandle {
    private final long uniqueId;
    private final TimerRegistry timerRegistry;

    TimerHandleImpl(long uniqueId, TimerRegistry timerRegistry) {
        this.uniqueId = uniqueId;
        this.timerRegistry = timerRegistry;
    }

    @Override
    public boolean isActive() {
        return timerRegistry.isTimerActive(uniqueId);
    }

	@Override
	public long getIterations() {
		return timerRegistry.getIterations(uniqueId);
	}

    @Override
    public void cancel() {
        timerRegistry.cancelTimer(uniqueId);
    }
}
