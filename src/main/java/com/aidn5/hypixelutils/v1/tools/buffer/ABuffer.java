
package com.aidn5.hypixelutils.v1.tools.buffer;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract buffer element used for... anything buffer-related.
 * 
 * TODO: add proper copyright
 * 
 * @author Buggfroggy
 * 
 * @since 1.0
 * @version 1.0
 * 
 * @category BackendUtils
 */
public abstract class ABuffer<T> implements Runnable {
  /**
   * Buffer of all the objects.
   */
  private ArrayList<T> buffer;
  /**
   * The time in milliseconds between {@link #run()} calls.
   */
  private int sleepTime;
  /**
   * Whether the buffer is currently running.
   */
  private boolean started;
  /**
   * the pool to use when starting the buffer thread.
   */
  @Nonnull
  private ExecutorService threadPool;

  /**
   * Constructor.
   * 
   * @param sleepTime
   *          Time in milliseconds between {@link #run()} calls. See
   *          {@link #sleepTime}
   */
  public ABuffer(int sleepTime, @Nullable ExecutorService threadPool) {
    this.sleepTime = sleepTime;
    this.buffer = new ArrayList<>();
    this.threadPool = (threadPool != null) ? threadPool : Executors.newCachedThreadPool();
  }

  /**
   * Peek at the next buffer item without taking it out of the buffer.
   * 
   * @return The next buffer item
   */
  public T peek() {
    return size() > 0 ? buffer.get(0) : null;
  }

  /**
   * Pull the next buffer item out of the buffer.
   * 
   * @return The next buffer item
   */
  public T pull() {
    T returnValue = peek();
    if (returnValue != null)
      buffer.remove(0);

    return returnValue;
  }

  /**
   * Push a value to the buffer.
   * 
   * @param pushedValue
   *          Value to add to the buffer
   * @return this
   */
  public ABuffer push(T pushedValue) {
    buffer.add(pushedValue);
    return this;
  }

  /**
   * Empty the buffer of all values.
   * 
   * @return this
   */
  public ABuffer clear() {
    buffer.clear();
    return this;
  }

  /**
   * Get the size of the buffer.
   * 
   * @return Number of objects in the buffer
   */
  public int size() {
    return buffer.size();
  }

  /**
   * Getter for {@link #started}.
   * 
   * @return {@link #started}
   */
  public boolean isStarted() {
    return started;
  }

  /**
   * Start looping {@link #run()}.
   * 
   * @return this
   */
  public ABuffer start() {
    this.started = true;
    threadPool.submit(() -> {
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

  /**
   * Stop looping {@link #run()}.
   * 
   * @return this
   */
  public ABuffer stop() {
    started = false;

    return this;
  }
}
