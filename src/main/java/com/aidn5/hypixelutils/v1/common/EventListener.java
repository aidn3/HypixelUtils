
package com.aidn5.hypixelutils.v1.common;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * Interface added (extends) to any event listener interface. Used to determine
 * that the interface is an event listener and not just a functional callback.
 * 
 * <p>
 * This interface is useless and unnecessary in the current version of the
 * library. It may be later needed (e.g. to collect all the event listeners in
 * one array of {@link EventListener}).
 * 
 * @author aidn5
 * 
 * @version 1.0
 * @since 1.0
 */
@IBackend
@IHypixelUtils
public interface EventListener {}
