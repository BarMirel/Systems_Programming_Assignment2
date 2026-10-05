package bgu.spl.mics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 *
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {

	private volatile boolean isResolved;
	private AtomicReference<T> result;
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		isResolved = false;
		result = new AtomicReference<>(null);
	}

	/**
	 * retrieves the result the Future object holds if it has been resolved.
	 * This is a blocking method! It waits for the computation in case it has
	 * not been completed.
	 * <p>
	 * @return return the result of type T if it is available, if not wait until it is available.
	 *
	 */
	public synchronized T get(){
		while(!isResolved) {
			try {
				this.wait();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return result.get();
	}

	/**
	 * Resolves the result of this Future object.
	 */
	public synchronized void resolve (T result) {
		if(!isResolved){
			this.result.set(result);
			isResolved = true;
			this.notify();
		}
	}

	/**
	 * @return true if this object has been resolved, false otherwise
	 */
	public boolean isDone() {
		return isResolved;
	}

	/**
	 * retrieves the result the Future object holds if it has been resolved,
	 * This method is non-blocking, it has a limited amount of time determined
	 * by {@code timeout}
	 * <p>
	 * @param timeout 	the maximal amount of time units to wait for the result.
	 * @param unit		the {@link TimeUnit} time units to wait.
	 * @return return the result of type T if it is available, if not,
	 * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
	 *         elapsed, return null.
	 */
	public T get(long timeout, TimeUnit unit) {
		long finalTime = System.currentTimeMillis() + unit.toMillis(timeout);
		long remainingTime;
		while (!isResolved){
			remainingTime  = finalTime - System.currentTimeMillis();
			if (remainingTime <= 0)
				return null;
		}
		return result.get();
	}

}
