
package com.aidn5.hypixelutils.v1.common.annotation;

import java.lang.annotation.Documented;

import com.aidn5.hypixelutils.v1.exceptions.NotOnHypixelNetwork;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;

@Documented
@IBackend
@IHypixelUtils
/**
 * Define that the class is a part of the library itself.
 * 
 * <p>
 * <b>rules:</b>
 * <ul>
 * <li>Every class/interface/annotation must have this annotation declared</li>
 * <li>
 * </ul>
 * 
 * @author aidn5
 *
 */
public @interface IHypixelUtils {
  /**
   * True if it is used to indicate the declaration, which are specified to only
   * work online hypixel network and should never be used on any other server.
   * Using them while not being online hypixel network may either result in
   * unpredicted result or throw Exception {@link NotOnHypixelNetwork}.
   * 
   * <p>
   * If this is annotated on an interface, the interface will never be
   * called outside the hypixel network.
   */
  public boolean OnlyHypixel() default false;

  /**
   * Define that the class belongs to HypixelUtils and is extended from
   * {@link Event} and are sent to Forge's {@link EventBus}.
   * 
   * @deprecated
   *             create annotation in {@link IEventListener} instead of this.
   */
  @Deprecated
  public boolean isForgeEvent() default false;
}
