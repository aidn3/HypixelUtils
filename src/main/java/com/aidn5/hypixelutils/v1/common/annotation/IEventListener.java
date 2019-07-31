
package com.aidn5.hypixelutils.v1.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.common.ListenerBus;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;

/**
 * Indicates that the class is a listener type and is used to give an easy way
 * to listen to specific changes/events which are usually used in many mods<br>
 * 
 * <p>
 * <b>rules:</b>
 * <ul>
 * <li>The class must be extended from {@link ListenerBus}</li>
 * 
 * <li>All Callbacks from listeners must be extended from {@link IEventListener}
 * and must be declared with the annotation
 * {@link FunctionalInterface} and {@link IInterfaceEvent}</li>
 * 
 * <li>Listeners are <u>ONLY</u> accessed by an instance of
 * {@link HypixelUtils}</li>
 * 
 * <li>All listeners should also have {@link Event}, which can be listened
 * to as forge events and have the the annotation {@link IForgeEvent}</li>
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
public @interface IEventListener {
  /**
   * Define that the class belongs to HypixelUtils and is extended from
   * {@link Event} and are sent to Forge's {@link EventBus}.
   */
  @IBackend
  @IHypixelUtils
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface IForgeEvent {}

  @IBackend
  @IHypixelUtils
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface IInterfaceEvent {}
}
