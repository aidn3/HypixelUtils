
package com.aidn5.hypixelutils.v1.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define that the class is a part of the library itself.
 * This is used to distinguish between the library declarations and the others.
 * 
 * <p>
 * <b>rules:</b>
 * <ul>
 * <li>Every class/interface/annotation of this library
 * must have this annotation declared</li>
 * <li>
 * </ul>
 * 
 * @author aidn5
 *
 */
@Documented
@IBackend
@IHypixelUtils
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IHypixelUtils {}
