/**
 * Package contains all the low level and the underlying protocols of
 * connections.
 * 
 * <p>
 * {@link com.aidn5.hypixelutils.v1.chatsocket.protocols.BaseProtocol} handles
 * the incoming and the outgoing packets from protocols.
 * 
 * <p>
 * All protocols must be extended from base class
 * {@link com.aidn5.hypixelutils.v1.chatsocket.protocols.BaseProtocol} and must
 * be registered in
 * {@link com.aidn5.hypixelutils.v1.chatsocket.protocols.ProtocolsRegistry}.
 * They must also support receiving packets, by passing them to
 * {@link com.aidn5.hypixelutils.v1.chatsocket.protocols.BaseProtocol
 * #receivePacket(String, boolean, java.nio.ByteBuffer)}.
 * 
 * @author aidn5
 *
 * @since 1.0
 */

package com.aidn5.hypixelutils.v1.chatsocket.protocols;
