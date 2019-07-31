/**
 * This package contains all the high level classes, which used from the both
 * clients ends create connection request and to communicate. All the classes
 * except {@link com.aidn5.hypixelutils.v1.chatsocket.client.RequestSendEvent}
 * should not be constructed by the user.
 * 
 * <p>
 * {@link com.aidn5.hypixelutils.v1.chatsocket.client.ChatSocket} is the
 * underlying core class of the connection. It handles all the incoming and
 * outgoing packets.
 * 
 * <p>
 * {@link com.aidn5.hypixelutils.v1.chatsocket.client.Connection} is the highest
 * class, which is given to the user to control the connection and its data
 * flowing.
 * 
 * @author aidn5
 *
 * @since 1.0
 */

package com.aidn5.hypixelutils.v1.chatsocket.client;
