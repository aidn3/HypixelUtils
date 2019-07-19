
package com.aidn5.hypixelutils.v1.common.annotation;

import java.lang.annotation.Documented;

/**
 * Indicate that the class has common tools, which are common to use in
 * minecraft mods (In hypixel network or somewhere else).
 * 
 * <p>
 * <b>rules:</b>
 * <ul>
 * <li>(none)</li>
 * </ul>
 * 
 * @author aidn5
 * 
 * @since 1.0
 */
@Documented
@IBackend
@IHypixelUtils
public @interface IHelpTools {
  /**
   * This Class support only static methods.
   * Any attempt to create an instance of the class will result
   * in throwing an exception {@link AssertionError}.
   */
  public boolean onlyStatic() default false;
}
