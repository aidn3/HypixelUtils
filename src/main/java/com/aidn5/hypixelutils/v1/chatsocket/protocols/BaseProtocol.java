
package com.aidn5.hypixelutils.v1.chatsocket.protocols;

import java.nio.ByteBuffer;

import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base64;

import com.aidn5.hypixelutils.v1.chatsocket.ChatSocketFactory;
import com.aidn5.hypixelutils.v1.chatsocket.client.ChatSocket;
import com.aidn5.hypixelutils.v1.chatsocket.client.ChatSocketsManager;
import com.aidn5.hypixelutils.v1.chatsocket.packets.BasePacket;
import com.aidn5.hypixelutils.v1.chatsocket.packets.PacketsRegistry;
import com.aidn5.hypixelutils.v1.chatsocket.packets.ProtocolPacket;
import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * Class handles the incoming and the outgoing packets from protocols.
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
 * <p>
 * <b>Packet:</b>
 * <i>Example: "&HUCSv1s:AQZ0ZXN0SWQDYXNk"</i>
 * <table border="2">
 * <tbody>
 * <tr><th>Byte(s)</th><th>Encoded</th><th>Contains</th><th>Usage</th></tr>
 * <tr><td>7</td><td>No</td><td>"&HUCSv1"</td>
 * <td>Start indicator. See {@link #START_INDICATOR}</td>
 * </tr><td>1</td><td>No</td><td>"s" or "c"</td><td>"s" if {@link ChatSocket#isServer}</td></tr>
 * <tr><td>1</td><td>No</td><td>":"</td><td>Indicates the end of metadata</td></tr>
 * <tr><td>1</td><td>Yes</td><td>Byte</td><td>How the rest of the bytes are changed. 
 * Used to bypass anti-chat-spam by shifting/converting the rest of the bytes.
 *  See {@link #shiftNumberCount} and {@link #dataShifting(byte[], byte, boolean)}.</td></tr>
 * <tr><td>4</td><td>Yes</td><td>Integer</td><td>the connection id, which used to know what
 * connection is the packet referring to. {@link ChatSocket#connectionId}.</td></tr>
 * <tr><td>2</td><td>Yes</td><td>Short</td><td>The packet's id, What type of packet are the data
 * used for. Used to get the class of the packet from the registry {@link PacketsRegistry} 
 * to construct the packet from the data</td></tr>
 * <tr><td>N\A</td><td>Yes</td><td>Byte[]</td><td>The packet's data. Used with {@code packet's id}
 *  and {@link PacketsRegistry#getPacket(Class, byte[])} to construct the packet</td></tr>
 * </tbody>
 * </table>
 * 
 * <i>Note: All metadata (first 3 sections from the table) must be plain text
 * with encoding of {@code UTF-8}. So, Regex can find the packets from the chat.
 * The rest of the bytes are encoded as one group with {@code Base64}.</i>
 * 
 * @author aidn5
 *
 * @since 1.0
 */
@IHypixelUtils
@IBackend
public abstract class BaseProtocol {
  /**
   * added to the first byte before the packet.
   * 
   * <p>
   * This is used to to shift the data to generate a whole new set of packet to 
   * prevent chat-anti-spam from recognizing the packet and blocking it with 
   * the message "Don't repeat yourself".
   * 
   * @see #dataShifting(byte[], byte, boolean)
   */
  private static byte shiftNumberCount = 0;
  /**
   * Indicates the start of the packet.
   * 
   * <p>
   * <b>explains: </b> & = special char as a starter, HU = Hypixel-Utils, CS =
   * Chat-Socket, v1 = Version-1, %s = ("s" for {@link ChatSocket#isServer()} "c"
   * if not).
   */
  private static final String START_INDICATOR = "&HUCSv1%s:";
  /**
   * The default registry. Used to get the key of {@link ProtocolPacket}, to
   * process new connections.
   */
  private static PacketsRegistry defaultPR = new PacketsRegistry();

  /**
   * Search for an active protocol to use to send the packet.
   * 
   * @param chatSocket
   *          the connection to use to get the metadata of the
   *          packet, destination, etc.
   * @param packet
   *          the packet to send.
   * 
   * @throws RuntimeException
   *           If there is no active protocol that can send the packet.
   */
  public static void sendPacket(ChatSocket chatSocket, BasePacket packet)
      throws RuntimeException {

    final byte shiftNumber;
    synchronized (defaultPR) {
      if (shiftNumberCount > 250) {
        shiftNumberCount = 0;
      }

      shiftNumber = ++shiftNumberCount;
    }

    final byte[] packetData = packet.getBytes();

    final ByteBuffer bf = ByteBuffer.allocate(1 + 4 + 2 + packetData.length);
    bf.put(shiftNumber);
    bf.putInt(chatSocket.connectionId);
    bf.putShort(chatSocket.packetsRegistry.getPacketKey(packet.getClass()));
    bf.put(dataShifting(packetData, shiftNumber, false));

    final byte[] finalPacket = bf.array();

    for (BaseProtocol protocol : ProtocolsRegistry.getProtocols()) {
      if (protocol.isProtocolActive()) {
        String startIndicator = String.format(START_INDICATOR, chatSocket.isServer ? "s" : "c");
        protocol.sendPacket(chatSocket.user, startIndicator, finalPacket);
        return;
      }
    }

    throw new RuntimeException("no protocol can handle the current request at this time.");
  }

   /**
   * process the received packet.
   * 
   * @param user
   *          the user who sent this packet.
   * @param isServer
   *          whether the sender of this packet is the one who created the
   *          connection
   * @param packetBuffer
   *          the packet data without the start indicator.
   * 
   * @throws RuntimeException
   *           If any error occurs while processing the packet.
   * 
   * @see ChatSocket#isServer
   */
  static synchronized void receivePacket(String user, boolean isServer,
      ByteBuffer packetBuffer) throws RuntimeException {
    final byte shiftNumber = packetBuffer.get();
    final int connectionId = packetBuffer.getInt();
    final short packetType = packetBuffer.getShort();

    byte[] shiftedPacketData = new byte[packetBuffer.remaining()];
    packetBuffer.get(shiftedPacketData);
    final byte[] packetData = dataShifting(shiftedPacketData, shiftNumber, true);

    @Nullable
    final ChatSocket chatSocket = ChatSocketsManager.getConnection(connectionId);
    final Class<? extends BasePacket> packetClass;


    if (defaultPR.getPacketKey(ProtocolPacket.class) == packetType) {
      final ProtocolPacket packet = PacketsRegistry.getPacket(ProtocolPacket.class, packetData);

      // check for new incoming request connections
      if (packet.getAction() == ProtocolPacket.ACTION_REQUEST) {

        if (chatSocket == null) {
          ChatSocket cp = new ChatSocket(user, packet.getId(), packet.getActionId(),
              connectionId, false);
          ChatSocketsManager.addNewConnection(cp);
          ChatSocketFactory.newRequestReceived(cp);


        } else {
          // what are the odds for a random integer
          // to be the same one that is being used at THIS moment by the other client?!

          // request to only inform the listener that someone is trying to connect
          // but failed to generated unused hash for the this user.
          // passing -1 will do the job
          ChatSocketFactory.newRequestReceived(
              new ChatSocket(user, packet.getId(), packet.getActionId(), -1, false));
        }

        return;
      }


      if (chatSocket != null) {
        // to prevent receiving its own packet
        if (chatSocket.isServer != isServer) {

          // If the connection is closed and a packet with close action is received
          // it means that both clients sent the close message at the same time
          // sending it to the handler will rise an exception since the connection
          // marked as closed. Ignoring it is the best course to do here
          if (packet.getAction() == ProtocolPacket.ACTION_CLOSE) {
            return;
          }
        }
      }
    }

    if (chatSocket == null) {
      throw new IllegalStateException(
          "Trying to interact with an already closed/timed-out/not-existed connection.");
    }

    // to prevent receiving its own packet
    if (chatSocket.isServer == isServer) {
      return;
    }

    if (!chatSocket.user.equalsIgnoreCase((user))) {
      throw new RuntimeException(
          "Hijacking the ChatProtocol connection has been prevented."
              + " The current allowed user is " + chatSocket.user + ", "
              + user + " was given in this packet.");
    }

    // this only happens when both ends send close action at the same time
    if (!chatSocket.isConnectionClosed()) {
      chatSocket.handleReceivedPacket(packetType, packetData);
    }
  }

  /**
   * Push all the data {@code shiftNumber} times. Helps to change the packet's
   * data to prevent chat-anti-spam from recognizing the packet and blocking it
   * with "Don't repeat yourself".
   * 
   * <p>
   * Examples:
   * <code><pre>
   * data = [1, 2, 3, 4, 5, 6, 7, 8, 9, 0]
   * method(data, 2, false) => NewData = [8, 9, 0, 1, 2, 3, 4, 5, 6, 7]
   * method(NewData, 2, true) => OriginalData = [1, 2, 3, 4, 5, 6, 7, 8, 9, 0]
   * </pre></code>
   * 
   * 
   * @param data
   *          the data to change
   * @param shiftNumber
   *          times to change the data.
   * @param dicipher
   *          <code>true</code> to change,
   *          <code>false</code> to reverse the operation.
   * 
   * @return
   *         new data array with data shifted.
   */
  private static byte[] dataShifting(byte[] data, byte shiftNumber, boolean dicipher) {
    byte finalShift = shiftNumber;

    while (finalShift > data.length) {
      finalShift -= data.length;
    }

    if (finalShift < 0) {
      finalShift = (byte) (-256 - finalShift);
    }

    byte[] newArray = new byte[data.length];

    if (dicipher) {
      System.arraycopy(data, finalShift, newArray, 0, data.length - finalShift);
      System.arraycopy(data, 0, newArray, data.length - finalShift, finalShift);

    } else {
      System.arraycopy(data, 0, newArray, finalShift, data.length - finalShift);
      System.arraycopy(data, data.length - finalShift, newArray, 0, finalShift);
    }

    return newArray;
  }

  /**
   * Encode the packet's to {@link java.util.Base64} to be able to send it through
   * the only-text-chat.
   * 
   * @param packet
   *          the packet to encode.
   * 
   * @return
   *         an encoded String, which can be sent through the chat.
   * 
   * @see #stringToPacket(String)
   */
  protected String packetToString(byte[] packet) {
    return Base64.encodeBase64String(packet);
  }

  /**
   * Decode the string to get the packet's data after receiving it from the chat.
   * 
   * @param s
   *          the string, which represents the packet's data.
   * 
   * @return
   *         the packet's data.
   * 
   * @see #packetToString(byte[])
   */
  protected byte[] stringToPacket(String s) {
    return Base64.decodeBase64(s);
  }

  /**
   * Send the packet using this Protocol.
   * 
   * @param user
   *          the destination the packet should be send to.
   * @param startIndicator
   *          the indicator of the packet, which should be also send before the
   *          packet. The indicator created from {@link #START_INDICATOR}.
   *          See {@link ChatSocket#isServer} for further information.
   * @param packet
   *          the packet to send.
   */
  protected abstract void sendPacket(String user, String startIndicator, byte[] packet);

  /**
   * Check whether this protocol can send and receive data on the current server.
   * 
   * @return
   *         <code>true</code> if this protocol can send and receive data on the
   *         current server.
   */
  protected abstract boolean isProtocolActive();
}
