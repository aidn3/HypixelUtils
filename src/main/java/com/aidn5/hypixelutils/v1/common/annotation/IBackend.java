
package com.aidn5.hypixelutils.v1.common.annotation;

import java.lang.annotation.Documented;

/**
 * Indicates that the class is an internal class for the self library and are
 * not specified to be used outside the library by anyone.
 * 
 * <p>
 * These classes may be removed/altered in the future which
 * may result in failing the program, which used them.
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
public @interface IBackend {}
