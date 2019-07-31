
package com.aidn5.hypixelutils.v1.chatsocket.packets;

import java.nio.ByteBuffer;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * An event, which is used to inform the other client not to time out.
 * 
 * <p>
 * The keep alive packet should be sent and received back to confirm the
 * connection. {@code shouldRespond} is used to prevent infinite loop.
 * 
 * @author aidn5
 *
 * @since 1.0
 */
@IHypixelUtils
@IBackend
public class KeepAlivePacket extends BasePacket {
  private boolean shouldRespond;

  @SuppressWarnings("unused")
  private KeepAlivePacket() {}

  /**
   * Constructor to create the packet.
   * 
   * @param shouldResponse
   *          whether the other client should also respond back with keepAlive
   *          packet.
   */
  public KeepAlivePacket(boolean shouldResponse) {
    this.shouldRespond = shouldResponse;
  }

  /**
   * Check whether the other client should also respond back with keepAlive
   * packet.
   * 
   * @return
   *         true if the other client should also respond back with keep alive
   *         packet.
   */
  public boolean shouldRespond() {
    return shouldRespond;
  }

  @Override
  public void readData(byte[] data) {
    ByteBuffer bf = ByteBuffer.wrap(data);
    shouldRespond = (bf.get() == 1);
  }

  @Override
  public byte[] getBytes() {
    ByteBuffer bf = ByteBuffer.allocate(1);

    bf.put((byte) (shouldRespond ? 1 : 0));

    return bf.array();
  }

}
