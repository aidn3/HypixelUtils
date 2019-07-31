
package com.aidn5.hypixelutils.v1.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the declaration is an internal class for the self library and
 * are not specified to be used outside the library by anyone.
 * 
 * <p>
 * They are {@code public} just to be able to use them from other declaration
 * outside the package. These declarations are mostly designed to do the backend
 * work. Accessing/modifying/invoking them may result in an unpredicted output
 * results.
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
// @Target is all
@Retention(RetentionPolicy.SOURCE)
public @interface IBackend {}
