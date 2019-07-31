
package com.aidn5.hypixelutils.v1.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.aidn5.hypixelutils.v1.common.ChatWrapper;

/**
 * Indicates that the class is used to send command and listen to the chat
 * and use the callback when the message is found.<br>
 * 
 * <p>
 * <b>rules:</b>
 * <ul>
 * <li>Every Wrapper must has its own inline callback interface,
 * which are annotated as {@link FunctionalInterface}</li>
 * <li>Wrapper must use the lock {@link ChatWrapper#chatLock} when executing
 * complicated commands or/and have long/complicated chat output.</li>
 * </ul>
 * 
 * @author aidn5
 * 
 * @since 1.0
 */
@Documented
@IBackend
@IHypixelUtils
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IChatWrapper {
  /**
   * Define whether this wrapper does complicated/long process that needs to hold
   * the lock to prevent other instances from working till it finishes.
   */
  public boolean usesLock();
}
