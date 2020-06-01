/*
 * WorkerThreadPool.java
 * 
 * A thread pool implementation.
 * There is no bound on the number off queued tasks.
 * 
 * @author James Barnes (820946)
 */

package server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WorkerThreadPool {
	private final BlockingQueue<Runnable> queue;
	private final WorkerThread[] threads;

	/*
	 * Creates a new WorkerThreadPool with a specified number of threads
	 * 
	 * @param nThreads int number of threads to create in the pool
	 */
	public WorkerThreadPool(int nThreads) {
		queue = new LinkedBlockingQueue<Runnable>();
		threads = new WorkerThread[nThreads];

		for (int i = 0; i < nThreads; i++) {
			WorkerThread thread = new WorkerThread();
			threads[i] = thread;
			thread.start();
		}
	}

	/*
	 * Adds a Runnable to the WorkerThreadPool's queue
	 * 
	 * @param runner Runnable to add to queue
	 */
	public synchronized void add(Runnable runner) {
		queue.add(runner);
	}

	/*
	 * Internal class used to run tasks in queue
	 * 
	 * @param runner Runnable to add to queue
	 */
	private class WorkerThread extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					// take causes thread to wait until queue has an element
					queue.take().run();
				} catch (InterruptedException e) {
					if (e.getMessage() != null) {
						System.err
							.println(this.getName() + ": " + e.getMessage());
						e.printStackTrace();
					}
					break;
				} catch (RuntimeException re) {
					/* ignored */
				}
			}
		}
	}

	/*
	 * Interrupts all threads in the pool. This should be used with caution
	 */
	public synchronized void interrupt() {
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].interrupt();
			} catch (SecurityException se) {
				se.printStackTrace();
			}
		}
	}
}
