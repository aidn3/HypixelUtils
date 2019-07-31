
package com.aidn5.hypixelutils.v1.tools;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.common.annotation.IHelpTools;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * Class help with time-out events by creating a new thread and start waiting.
 * the listener will be called, if {@link #tick()} is not called in the given
 * timeout time.
 * 
 * @author aidn5
 * 
 * @since 1.0
 * @version 1.0
 */
@IHypixelUtils
@IHelpTools
public class TimeOut {
  @Nullable
  private Runnable timeOutListener;

  private long timeOut = TimeUnit.SECONDS.toMillis(30);
  private long clock = System.currentTimeMillis();
  private boolean isTimedOut = false;

  private boolean threadStarted = false;

  /**
   * Constructor.
   * 
   * @param timeOutListener
   *          the listener to call when timed out.
   * 
   */
  public TimeOut(@Nullable Runnable timeOutListener) {
    this.timeOutListener = timeOutListener;

    start();
  }

  /**
   * get the timeout in milliseconds.
   * 
   * @return
   *         the timeout used in milliseconds
   */
  public long getTimeOut() {
    return timeOut;
  }

  /**
   * set timeout for this listener.
   * 
   * @param duration
   *          the duration for timeout.
   * @param unit
   *          the unit to use for {@code duration}
   * 
   * @return
   *         an instance of this.
   */
  @Nonnull
  public TimeOut setTimeOut(long duration, @Nonnull TimeUnit unit) {
    this.timeOut = unit.toMillis(duration);
    return this;
  }

  /**
   * Return whether it is already timed out
   * and the listener has already been called.
   * 
   * @return
   *         <code>true</code> if it is already timed out.
   */
  public boolean isTimedOut() {
    return isTimedOut;
  }

  /**
   * set timeout listener.
   * 
   * @param timeOutListener
   *          the listener to set.
   * 
   * @return
   *         an instance of this.
   */
  @Nonnull
  public TimeOut setTimeOutListener(@Nullable Runnable timeOutListener) {
    this.timeOutListener = timeOutListener;
    return this;
  }

  /**
   * Get the current timeout listener.
   * 
   * @return
   *         the current timeout listener.
   */

  public Runnable getTimeOutListener() {
    return timeOutListener;
  }

  /**
   * Do tick. So, the timeout is reset and start over to count.
   */
  public void tick() {
    clock = System.currentTimeMillis();
  }

  private void start() {
    if (threadStarted) {
      return;
    }
    threadStarted = true;

    Thread th = new Thread(() -> {
      clock = System.currentTimeMillis();
      while (true) {
        if (clock + timeOut < System.currentTimeMillis()) {

          isTimedOut = true;
          if (timeOutListener != null) {
            timeOutListener.run();
          }
          return;
        }

        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();

          // we need to inform the listener. Otherwise the listener will never be called
          // and there might be an infinite loop
          isTimedOut = true;
          if (timeOutListener != null) {
            timeOutListener.run();
          }

          Thread.currentThread().interrupt();
          return;
        }
      }
    });

    th.setDaemon(true);
    th.start();
  }
}
