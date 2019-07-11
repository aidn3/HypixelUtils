/**
 * 
 * Used to provide an easy way to listen to specific changes/events
 * which are usually used in many mods<br>
 * <b>Notes:</b><br>
 * <ul>
 * <li>All Callbacks from listeners are extended from
 * {@link com.aidn5.hypixelutils.v1.common.EventListener}.</li>
 * 
 * <li>You can register to a listener with
 * {@link com.aidn5.hypixelutils.v1.common.ListenerBus
 * #register(com.aidn5.hypixelutils.v1.common.EventListener)}.</li>
 * 
 * <li>Listeners are <u>ONLY</u> accessed by
 * {@link com.aidn5.hypixelutils.v1.HypixelUtils#INSTANCE}.</li>
 * 
 * <li>All listeners also have
 * {@link net.minecraftforge.fml.common.eventhandler.Event},
 * which can be used as forge events.</li>
 * </ul>
 * 
 * @author aidn5
 * 
 * @since 1.0
 */

package com.aidn5.hypixelutils.v1.eventslistener;
