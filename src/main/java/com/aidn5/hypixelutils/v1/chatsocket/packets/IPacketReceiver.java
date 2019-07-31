
package com.aidn5.hypixelutils.v1.chatsocket.packets;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * An interface used to receive packets from the handler.
 * It can also be used to receive customs packets.
 * 
 * @author aidn5
 *
 * @since 1.0
 * 
 * @See PacketsRegistry
 */
@FunctionalInterface
@IHypixelUtils
public interface IPacketReceiver {
  /**
   * send packet to process.
   * 
   * @param packet
   *          the packet to process in instance.
   */
  <T extends BasePacket> void packetReceived(@Nonnull T packet);
}
