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

	public TimerHandle register(long iterations, long waitTime, Runnable runnable) {
        long uniqueId = idIncrementor.incrementAndGet();
		registerWithId(null, uniqueId, iterations, waitTime, runnable);
        return new TimerHandleImpl(uniqueId, this);
    }

	private void registerWithId(TimerRunInstance previous, long uniqueId, long iterations, long waitTime, Runnable runnable) {
        long executionStart = System.currentTimeMillis() + waitTime;
		TimerRunInstance instance = new TimerRunInstance(uniqueId, executionStart, iterations, waitTime, runnable);

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

				if (instance.iterations == 0) {
					registerWithId(instance, instance.uniqueId, 0, instance.waitTime, instance.runnable);
				} else if (instance.iterations != 1) {
					registerWithId(instance, instance.uniqueId, instance.iterations - 1, instance.waitTime, instance.runnable);
				} else {
                    synchronized (lock) {
                        activeTimers.remove(instance.uniqueId);
                    }
                }
            }
        }
    }

	long getIterations(long uniqueId) {
		synchronized (lock) {
			if (isTimerActive(uniqueId)) {
				return activeTimers.get(uniqueId).iterations;
			}
			return -1;
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
		public final long iterations;
        public final long waitTime;
        public final Runnable runnable;

		private TimerRunInstance(long uniqueId, long executionStart, long iterations, long waitTime, Runnable runnable) {
            this.uniqueId = uniqueId;
            this.executionStart = executionStart;
			this.iterations = iterations;
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
