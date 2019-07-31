/**
 * <b>ChatSocket</b> is a protocol used to connect two mods/programs of two
 * different players on the same minecraft server and help them communicate with
 * each other by providing them with {@link java.io.InputStream},
 * {@link java.io.OutputStream}, etc. through chat. It also provides
 * authentication and other features for a stable connection between the two
 * ends. It is a completely side-Client and needs only the ability to listen to
 * incoming chat-messages and the ability to send private messages to the other
 * player (like permission to use the command "/msg [username] {message}" on the
 * specified server).
 * 
 * <p>
 * <b>Protocols: </b> are chat listeners, which used to send and receive chat
 * messages for the server it intends to work on. for example
 * {@link com.aidn5.hypixelutils.v1.chatsocket.protocols.ChatHypixelProtocol} is
 * used to send and receive messages online hypixel network.<br>
 * Registering protocols are done by
 * {@link com.aidn5.hypixelutils.v1.chatsocket.ChatSocketFactory#registerProtocol(Class)}.
 * Unregistering protocols is not supported to avoid problems.
 * 
 * <p>
 * <b>Listeners: </b> are interfaces used to inform for new request connections.
 * <i>Every {@code modid} can register only <u>ONE</u> listener.
 * Trying to register more than one listener will remove the old one and replace
 * it with the new one</i><br>
 * Registering and unregistering listeners are done by
 * {@link com.aidn5.hypixelutils.v1.chatsocket.ChatSocketFactory
 * #registerListener(String, com.aidn5.hypixelutils.v1.chatsocket.
 * client.RequestReceiveEvent.RequestReceived)} and
 * {@link com.aidn5.hypixelutils.v1.chatsocket.ChatSocketFactory#unregisterListener(String)}.
 * See {@link com.aidn5.hypixelutils.v1.chatsocket.wrapper.RequestWrapper} for a
 * wrapper.
 * 
 * <p>
 * <b>Understanding of ChatSocket: </b>As first {@code protocols} are registered
 * to receive and send messages through the chat. They have regex, which allow
 * them to detect the messages. All detected messages are canceled to prevent
 * the user from seeing the messages (spam). The detected message are passed to
 * {@link com.aidn5.hypixelutils.v1.chatsocket.protocols.BaseProtocol
 * #receivePacket(String, boolean, java.nio.ByteBuffer)}. Here the message will
 * be parsed. Every message has an id attached to it. This id is used to
 * identifies what connection is the other end referring to. The rest of the
 * data are parsed into the packet. See
 * {@link com.aidn5.hypixelutils.v1.chatsocket.protocols.BaseProtocol} for
 * further protocol and packet explanations. <br>
 * 
 * After parsing the packet, the packet will be sent to its connection
 * ({@link com.aidn5.hypixelutils.v1.chatsocket.client.ChatSocket} (if exists.
 * The connection's id) or be ignored. The only exception is
 * {@link com.aidn5.hypixelutils.v1.chatsocket.packets.ProtocolPacket}. This
 * packet is used to create, reject, accept, close connection. If the
 * ProtocolPaclet is referring to create a new connection (request connection)
 * a new object holds
 * {@link com.aidn5.hypixelutils.v1.chatsocket.client.ChatSocket} will be
 * created and saved in
 * {@link com.aidn5.hypixelutils.v1.chatsocket.client.ChatSocketsManager}.
 * Otherwise the packet will also be handled as a normal packet and be sent
 * {@link com.aidn5.hypixelutils.v1.chatsocket.client.ChatSocket} to be handled
 * by the connection itself. If a new connection is created the listeners will
 * be informed about the new connection through
 * {@link com.aidn5.hypixelutils.v1.chatsocket.ChatSocketFactory
 * #newRequestReceived(com.aidn5.hypixelutils.v1.chatsocket.client.ChatSocket)}.<br>
 * 
 * {@link com.aidn5.hypixelutils.v1.chatsocket.client.ChatSocket} is the core
 * connection between the two clients. It has all the required methods for
 * a connection. Sending packets like accepting, rejecting, creating, closing
 * connection is all handled in it. This object is not given to the user to use,
 * since it has all the underlying declarations, which can ruin the socket.
 * The upper class
 * {@link com.aidn5.hypixelutils.v1.chatsocket.client.Connection} is the most
 * suitable for the user. It has all the needed methods for a stable
 * connection.<br>
 * 
 * One of the features is: the ability to create custom packets and provide a
 * way to send and receive these packets. See
 * {@link com.aidn5.hypixelutils.v1.chatsocket.packets.PacketsRegistry} for
 * further explanations.
 * 
 * 
 * @author aidn5
 *
 * @since 1.0
 * 
 * @see com.aidn5.hypixelutils.v1.chatsocket.packets.PacketsRegistry
 * @see com.aidn5.hypixelutils.v1.chatsocket.protocols.BaseProtocol
 * @see com.aidn5.hypixelutils.v1.chatsocket.packets.BasePacket
 * 
 * @see com.aidn5.hypixelutils.v1.chatsocket.wrapper.RequestWrapper
 */

package com.aidn5.hypixelutils.v1.chatsocket;
