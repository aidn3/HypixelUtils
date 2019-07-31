
package com.aidn5.hypixelutils.v1.chatsocket.packets;

import java.nio.ByteBuffer;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * Packet used to transfer data between the two ends. These data are what the
 * both clients want to send to the other. This is NOT an underlying protocol.
 * The packet, which is responsible to initiate and to hold the connection is
 * {@link ProtocolPacket}.
 * 
 * @author aidn5
 * 
 * @since 1.0
 * 
 * @see ProtocolPacket
 */
@IHypixelUtils
@IBackend
public class DataPacket extends BasePacket {
  private int total;
  private int currentPointer;

  private boolean isAtEnd;
  @Nonnull
  private byte[] rawData;

  @SuppressWarnings("unused")
  private DataPacket() {}

  /**
   * Constructor to create the packet.
   * 
   * @param data
   *          the raw data to send to the other client.
   *          (can also be a chunk of the total data)
   * @param total
   *          the total data size from all chunks
   * @param currentPointer
   *          the current position of this data
   * @param isAtEnd
   *          indicates whether this packet is the last packet of the stream.
   */
  public DataPacket(@Nonnull byte[] data, int total, int currentPointer, boolean isAtEnd) {
    this.rawData = Objects.requireNonNull(data);
    this.total = total;
    this.currentPointer = currentPointer;
    this.isAtEnd = isAtEnd;
  }

  /**
   * Get the current position for this packet of the stream.
   * 
   * @return
   *         the current position for this packet of the stream.
   */
  public int getCurrentPointer() {
    return currentPointer;
  }

  /**
   * Get the raw data of the stream.
   * 
   * @return
   *         the raw data of the stream.
   */
  @Nonnull
  public byte[] getRawData() {
    return rawData;
  }

  /**
   * get the total data size from all chunks.
   * 
   * @return
   *         the total data size from all chunks.
   */
  public int getTotal() {
    return total;
  }

  /**
   * Check whether this packet is the last packet of the stream.
   * 
   * @return
   *         <code>true</code> if this packet is the last packet of the stream.
   */
  public boolean isAtEnd() {
    return isAtEnd;
  }

  @Override
  public void readData(byte[] data) {
    ByteBuffer bf = ByteBuffer.wrap(data);

    total = bf.getInt();
    currentPointer = bf.getInt();
    isAtEnd = (bf.get() == 1);

    rawData = new byte[bf.remaining()];
    bf.get(rawData);
  }

  @Nonnull
  @Override
  public byte[] getBytes() {
    ByteBuffer bf = ByteBuffer.allocate(
        4 // total
            + 4 // currentPointer
            + 1 // isAtEnd
            + this.rawData.length); // rawData

    bf.putInt(total);
    bf.putInt(currentPointer);
    bf.put((byte) (isAtEnd ? 1 : 0));

    bf.put(rawData);

    return bf.array();
  }
}
