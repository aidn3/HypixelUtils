
package com.aidn5.hypixelutils.v1.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.aidn5.hypixelutils.v1.exceptions.NotOnHypixelNetwork;

/**
 * Indicate that the declaration, which are specified to be only
 * used online hypixel network and should never be used on anywhere else.
 * Using them while not being online hypixel network may either result in
 * unpredicted result or throw the exception {@link NotOnHypixelNetwork}.
 * 
 * <p>
 * If this is annotated on an interface, the interface will never be
 * called outside the hypixel network.
 */
@IBackend
@IHypixelUtils
@Documented
@Retention(RetentionPolicy.SOURCE)
// @Target is all
public @interface IOnlyHypixel {

}
