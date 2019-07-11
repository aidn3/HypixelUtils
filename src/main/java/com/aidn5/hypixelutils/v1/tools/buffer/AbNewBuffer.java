
package com.aidn5.hypixelutils.v1.tools.buffer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * abstract buffer array used to buffer the pushed elements on its own
 * {@link Thread} with {@link #sleepTime} in between.
 * 
 * @param <T>
 *          the type of the supplied/pushed elements
 * 
 * @author aidn5
 * @version 1.0
 * @since 1.0
 * @category BackendUtils
 */
public abstract class AbNewBuffer<T> extends ArrayBlockingQueue<T> {
  /**
   * The time in milliseconds between {@link #run()}/{@link #next(Object)} calls.
   */
  private int sleepTime;
  /**
   * Whether the buffer is currently running.
   */
  private boolean started;
  /**
   * the pool to use when starting the buffer thread.
   */
  private ExecutorService threadPool;

  /**
   * Constructor.
   *
   * @param capacity
   *          how many elements maximum can the buffer hold. See
   *          {@link ArrayBlockingQueue#ArrayBlockingQueue(int)}
   * @param sleepTime
   *          Time in milliseconds between {@link #run()} calls. See
   *          {@link #sleepTime}
   * @param threadPool
   *          the pool to use when starting the buffer thread
   */
  public AbNewBuffer(int capacity, int sleepTime, ExecutorService threadPool) {
    super(capacity);
    this.sleepTime = sleepTime;
    this.threadPool = threadPool;
  }

  /**
   * Constructor.
   * <p>
   * default capacity will be 5000 and {@link #sleepTime} will be 100 milliseconds
   * with.
   */
  public AbNewBuffer() {
    this(5000, 100, Executors.newCachedThreadPool());
  }

  /**
   * Start the buffer to start buffering the elements to {@link #next(T)}.
   * 
   * @return this
   */
  public AbNewBuffer<T> start() {
    this.started = true;
    this.threadPool.submit(() -> {
      while (!Thread.currentThread().isInterrupted() && started) {
        try {
          run();
          Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
          stop();
          e.printStackTrace();
          Thread.currentThread().interrupt();
        }
      }
    });

    return this;
  }


  private final void run() throws InterruptedException {
    next(super.take());
  }

  /**
   * the next element from the buffer to process.
   * 
   * @param element
   *          the next element. always not <code>null</code>
   */
  protected abstract void next(T element);

  /**
   * Stop the buffer.
   * 
   * @return this
   */
  public AbNewBuffer<T> stop() {
    started = false;

    return this;
  }
}
