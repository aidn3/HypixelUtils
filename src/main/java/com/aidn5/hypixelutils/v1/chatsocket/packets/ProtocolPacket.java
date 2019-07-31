
package com.aidn5.hypixelutils.v1.chatsocket.packets;

import java.nio.ByteBuffer;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * Packet used as an underlying packet for the connection. This packet is used
 * to create, accept, reject and close connections. This packet contains all the
 * metadata of the connections.
 * 
 * @author aidn5
 *
 * @since 1.0
 */
@IHypixelUtils
@IBackend
public class ProtocolPacket extends BasePacket {
  public static final byte ACTION_REQUEST = 1;
  public static final byte ACTION_ACCEPT = 2;
  public static final byte ACTION_DECLINE = 3;
  public static final byte ACTION_CLOSE = 4;

  /**
   * The message type. either sending a request to connect to the client, accept
   * the connection & decline the connection or close it.
   * 
   * actions are defined above as variables "ACTION_**"
   */
  private byte action;

  /**
   * the id of the programs which are trying to communicate with each
   * other.
   */
  private String id;
  /**
   * an extra field for the program.
   * used to define their intends from each other.
   */
  private String actionId;

  @SuppressWarnings("unused")
  // Constructed in BaseProtocol.java by reflection.
  // used with #readData(byte[])
  private ProtocolPacket() {}

  /**
   * Constructor to create new Packet.
   * 
   * @param id
   *          the id of the programs which are trying to communicate with each
   *          other.
   * @param actionId
   *          an extra field for the program.
   *          used to define their intends from each other.
   * @param action
   *          what the packet want from the other end to do with it.
   */
  public ProtocolPacket(String id, String actionId, byte action) {
    id = id.trim();
    actionId = actionId.trim();

    this.id = id;
    this.actionId = actionId;
    this.action = action;
  }

  /**
   * the id of the programs which are trying to communicate with each other.
   *
   * @return
   *         the id of the program.
   */
  public String getId() {
    return id;
  }

  /**
   * an extra field for the program.
   * used to define their intends from each other.
   *
   * @return
   *         the extra field.
   */
  public String getActionId() {
    return actionId;
  }

  /**
   * The message type. either sending a request to connect to the client, accept
   * the connection & decline the connection or close it.
   * 
   * actions are defined above as variables "ACTION_**"
   * 
   * @see #ACTION_REQUEST
   * @see #ACTION_ACCEPT
   * @see #ACTION_DECLINE
   * @see #ACTION_CLOSE
   */
  public byte getAction() {
    return action;
  }

  @Override
  public byte[] getBytes() {
    byte[] idArray = getId().getBytes();
    byte[] actionIdArray = getActionId().getBytes();

    ByteBuffer bf = ByteBuffer.allocate(
        1
            + 1 + idArray.length
            + 1 + actionIdArray.length);

    bf.put(action);

    bf.put((byte) idArray.length);
    bf.put(idArray);

    bf.put((byte) actionIdArray.length);
    bf.put(actionIdArray);

    return bf.array();
  }

  @Override
  public void readData(byte[] data) {
    byte[] idArray;
    byte[] actionIdArray;

    ByteBuffer bf = ByteBuffer.wrap(data);

    action = bf.get();

    idArray = new byte[bf.get()];
    bf.get(idArray);
    id = new String(idArray);

    actionIdArray = new byte[bf.get()];
    bf.get(actionIdArray);
    actionId = new String(actionIdArray);
  }
}
