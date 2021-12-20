package com.maxorator.vcmp.java.tools.timers;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicLong;

public class TimerRegistry {

    protected final Map<Long, TimerRunInstance> activeTimers = new HashMap<>();
    protected final Object lock = new Object();
    protected final Object processLock = new Object();
    protected final PriorityQueue<TimerRunInstance> timerQueue = new PriorityQueue<>();
    protected final AtomicLong idIncrementor = new AtomicLong();

    public TimerRegistry() {

    }

    public TimerHandle register(boolean isRecurring, long waitTime, Runnable runnable) {
        long uniqueId = idIncrementor.incrementAndGet();
        registerWithId(null, uniqueId, isRecurring, waitTime, runnable);
        return new TimerHandleImpl(uniqueId, this);
    }

    private void registerWithId(TimerRunInstance previous, long uniqueId, boolean isRecurring, long waitTime, Runnable runnable) {
        long executionStart = System.currentTimeMillis() + waitTime;
        TimerRunInstance instance = new TimerRunInstance(uniqueId, executionStart, isRecurring, waitTime, runnable);

        synchronized (lock) {
            if (activeTimers.get(uniqueId) == previous) {
                activeTimers.put(uniqueId, instance);
                timerQueue.add(instance);
            }
        }
    }

    private TimerRunInstance retrieveNext() {
        synchronized (lock) {
            TimerRunInstance next = timerQueue.peek();

            if (next != null && next.executionStart <= System.currentTimeMillis()) {
                timerQueue.remove();
                return next;
            }
        }

        return null;
    }

    public void process() {
        synchronized (processLock) {
            TimerRunInstance instance;

            while ((instance = retrieveNext()) != null) {
                instance.runnable.run();

                if (instance.isRecurring) {
                    registerWithId(instance, instance.uniqueId, true, instance.waitTime, instance.runnable);
                } else {
                    synchronized (lock) {
                        activeTimers.remove(instance.uniqueId);
                    }
                }
            }
        }
    }

    boolean isTimerActive(long uniqueId) {
        synchronized (lock) {
            return activeTimers.containsKey(uniqueId);
        }
    }

    void cancelTimer(long uniqueId) {
        synchronized (lock) {
            TimerRunInstance instance = activeTimers.remove(uniqueId);
            if (instance != null) {
                timerQueue.remove(instance);
            }
        }
    }

    protected static class TimerRunInstance implements Comparable<TimerRunInstance> {

        public final long uniqueId;
        public final long executionStart;
        public final boolean isRecurring;
        public final long waitTime;
        public final Runnable runnable;

        private TimerRunInstance(long uniqueId, long executionStart, boolean isRecurring, long waitTime, Runnable runnable) {
            this.uniqueId = uniqueId;
            this.executionStart = executionStart;
            this.isRecurring = isRecurring;
            this.waitTime = waitTime;
            this.runnable = runnable;
        }

        @Override
        public int compareTo(TimerRunInstance o) {
            if (o.executionStart > executionStart) {
                return -1;
            } else if (o.executionStart < executionStart) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
