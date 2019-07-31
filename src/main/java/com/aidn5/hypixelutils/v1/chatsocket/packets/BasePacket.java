
package com.aidn5.hypixelutils.v1.chatsocket.packets;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * Base packet used to provide general methods, which are required in all
 * packets.
 * 
 * <p>
 * All packets should be extended from this class and should support all its
 * methods. It also should have an empty private constructor and a constructor
 * with all the required parameters. all the variables must not be final. so,
 * {@link #readData(byte[])} can be used to fill them. The outcome of
 * {@link #getBytes()} must be possible to use with {@link #readData(byte[])}
 * to regenerate the packet.
 * 
 * 
 * @author aidn5
 *
 * @since 1.0
 */
@IHypixelUtils
@IBackend
public abstract class BasePacket {
  /**
   * parse the data and use them to fill the variables of the packet.
   * 
   * @param data
   *          the data to parse.
   */
  public abstract void readData(byte[] data);

  /**
   * Create an array of the data, which presents this packet.
   * 
   * @return an array of data, which presents this packet.
   */
  public abstract byte[] getBytes();
}
