
package com.aidn5.hypixelutils.v1.common;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * An event bus manages the events callback system by providing a way to
 * register/unregister new callbacks to an event.
 *
 * @param <T>
 *          the type of the callback
 * 
 * @author aidn5
 * 
 * @version 1.0
 * @since 1.0
 * 
 * @category Common
 */
public class ListenerBus<T extends EventListener> {
  @Nonnull
  private final Set<T> listeners = new HashSet<T>();

  /**
   * register a new callback to the event.
   * 
   * @param listener
   *          (must not be <code>null</code>)
   * 
   * @return true if this set did not already contain the specified element
   * 
   * @throws NullPointerException
   *           if listener is <code>null</code>
   */
  public boolean register(@Nonnull T listener) throws NullPointerException {
    return listeners.add(Objects.requireNonNull(listener));
  }

  /**
   * unregister a registered event to stop receiving the callback.
   * 
   * @param listener
   *          (must not be <code>null</code>)
   * 
   * @return true if this bus contained the specified listener
   * 
   * @throws NullPointerException
   *           if listener is <code>null</code>
   */
  public boolean unregister(@Nonnull T listener) throws NullPointerException {
    return listeners.remove(Objects.requireNonNull(listener));
  }

  /**
   * Get the pointer to the private set where all the listeners are saved.
   * 
   * <p>Used to callback the listeners when the specific event occurs
   * 
   * @return all the registered listeners
   */
  @Nonnull
  protected Set<T> getListeners() {
    return listeners;
  }
}
