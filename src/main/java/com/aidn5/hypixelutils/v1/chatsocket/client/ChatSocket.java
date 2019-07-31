
package com.aidn5.hypixelutils.v1.chatsocket.client;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestSendEvent.RequestResponse;
import com.aidn5.hypixelutils.v1.chatsocket.packets.BasePacket;
import com.aidn5.hypixelutils.v1.chatsocket.packets.KeepAlivePacket;
import com.aidn5.hypixelutils.v1.chatsocket.packets.PacketsRegistry;
import com.aidn5.hypixelutils.v1.chatsocket.packets.ProtocolPacket;
import com.aidn5.hypixelutils.v1.chatsocket.protocols.BaseProtocol;
import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;


/**
 * The core of a one connection between two ends.
 * Used to handle events between the two clients.
 * 
 * @author aidn5
 *
 * @since 1.0
 */
@IHypixelUtils
@IBackend
public class ChatSocket {
  /**
   * Get the packet registry to register custom packets, which can be used with
   * the custom packet receiver.
   */
  @Nonnull
  public final PacketsRegistry packetsRegistry = new PacketsRegistry();
  /**
   * whether this client is the one which requested to connect and that
   * the other end is the client, which accepted the connection request.
   * 
   * <p>
   * Some ChatProtocols detects any message, which contains the chat indicators.
   * This means the protocol will also detect the messages the client sent.
   * To prevent this a check will be placed upon receiving every packet.
   * The packet will be ignored, if it has the same status {@code isServer}.
   */
  public final boolean isServer;
  /**
   * id used to verify the messages between the two clients.
   * This id is randomly generated when the connection request initiated.
   * It is sent at the start of every packet and used for
   * the rest of the session (connection) or till timeout is flagged.
   */
  public final int connectionId;
  /**
   * the at the other end of the connection user.
   * Used as a destination for the packets.
   */
  @Nonnull
  public final String user;
  /**
   * the id of the programs which are trying to communicate with each other.
   */
  @Nonnull
  public final String id;
  /**
   * an extra field for the program.
   * used to define their intends from each other.
   */
  @Nonnull
  public final String actionId;
  /**
   * higher level of the connection, which can be used to communicate with
   * the other end of the connection or/and to get the metadata about the
   * Connection.
   */
  @Nonnull
  final Connection connection;

  private long lastTimeSentPacket = -1;

  private Status status = Status.PENDING;

  private IResponseRequest connectionCallback;
  private boolean responseRequestCalled = false;

  @IBackend
  public ChatSocket(@Nonnull String user, @Nonnull String id, @Nonnull String actionId,
      int connectionId, boolean isServer) {

    if (id == null || id.length() > 16 || id.length() < 3) {
      throw new IllegalArgumentException("id must be between 3 and 16 long");
    }

    if (actionId == null || actionId.length() > 16 || actionId.length() < 3) {
      throw new IllegalArgumentException("actionId must be between 3 and 16 long");
    }

    this.isServer = isServer;
    this.user = Objects.requireNonNull(user);
    this.connectionId = connectionId;

    this.id = Objects.requireNonNull(id);
    this.actionId = Objects.requireNonNull(actionId);

    this.connection = new Connection(this);
  }

  /**
   * Get when the last time a packet was sent from this connection.
   * 
   * @return
   *         the last time a packet was sent from this connection.
   */
  public long getLastTimeSentPacket() {
    return lastTimeSentPacket;
  }

  /**
   * Accept the request connection by sending the packet.
   */
  void sendConnectionAcceptPacket() {
    checkConnection(Status.PENDING);

    sendPacket(new ProtocolPacket(id, actionId, ProtocolPacket.ACTION_ACCEPT));
    status = Status.OPEN;
  }

  /**
   * Respond to the connection request with rejection.
   */
  void sendConnectionDeclinePacket() {
    checkConnection(Status.PENDING);
    status = Status.CLOSED;

    try {
      sendPacket(new ProtocolPacket(id, actionId, ProtocolPacket.ACTION_DECLINE));

    } catch (Exception e) {
      // the other client will soon time out

      new RuntimeException(
          "Could not send the decline packet. connection is only marked as closed.", e)
              .printStackTrace();
    }
  }

  /**
   * ping the other end to prevent connection from timing out.
   */
  void sendConnectionKeepAlivePacket(boolean shouldRespond) {
    checkConnection(Status.OPEN);

    sendPacket(new KeepAlivePacket(shouldRespond));
  }

  /**
   * Send packet to the other client requesting to connect.
   */
  void sendConnectionRequestPacket(@Nonnull IResponseRequest callback) {
    checkConnection(Status.PENDING);
    status = Status.REQUESTING;

    connectionCallback = Objects.requireNonNull(callback);

    connection.getTimeout().setTimeOutListener(() -> {
      synchronized (ChatSocket.this) {
        if (status != Status.REQUESTING) {
          return;
        }

        status = Status.CLOSED;

        if (!responseRequestCalled) {
          responseRequestCalled = true;
          HypixelUtils.threadPool.submit(() -> {
            connectionCallback.response(RequestResponse.TIMED_OUT, null);
          });
        }
      }
    });

    try {
      sendPacket(new ProtocolPacket(id, actionId, ProtocolPacket.ACTION_REQUEST));

    } catch (Exception e) {
      status = Status.CLOSED;
      connection.getTimeout().setTimeOutListener(null);

      throw e;
    }
  }

  /**
   * Try to close the connection is the connection is opened,
   * try to cancel if it is trying to connect to.
   * 
   * <p>
   * This method has no effect, if the connection is already closed
   * {@link #isConnectionClosed()}.
   */
  void closeConnection() {
    if (status == Status.OPEN) {
      try {
        sendPacket(new ProtocolPacket(id, actionId, ProtocolPacket.ACTION_CLOSE));

      } catch (Exception e) {
        status = Status.CLOSED;
        throw e;
      }


    } else if (status == Status.PENDING) {
      // we are receiving connection request.
      // just mark the connection as closed.
      // the other client will eventually time out and close too

    } else if (status == Status.REQUESTING) {

    }

    status = Status.CLOSED;
  }

  /**
   * Check whether the connection should be considered as closed.
   * 
   * @return
   *         true if the connection should be considered as closed.
   */
  public boolean isConnectionClosed() {
    return (status == Status.CLOSED);
  }

  /**
   * Check whether the connection is ready to send and receive data.
   * 
   * @return
   *         true if the connection is ready to send and receive data.
   */
  public boolean isConnectionOpened() {
    return (status == Status.OPEN);
  }

  /**
   * Send packet from this connection to the other end.
   * This will also prevent timing out.
   * 
   * @param packet
   *          the packet to send.
   */
  void sendPacket(@Nonnull BasePacket packet) {
    checkConnection(Status.OPEN, Status.PENDING, Status.REQUESTING);

    lastTimeSentPacket = System.currentTimeMillis();
    connection.getTimeout().tick();

    BaseProtocol.sendPacket(this, packet);
    connection.getTimeout().tick();
  }

  /**
   * Handle the received packet with its own {@link PacketsRegistry}.
   * 
   * @param packetType
   *          the registered packet type in {@link PacketsRegistry}.
   * @param packetData
   *          packet's data
   */
  public void handleReceivedPacket(short packetType, byte[] packetData) {
    checkConnection(Status.OPEN, Status.PENDING, Status.REQUESTING);
    connection.getTimeout().tick();

    Class<? extends BasePacket> packetClass = packetsRegistry.getPacket(packetType);
    BasePacket packet = PacketsRegistry.getPacket(packetClass, packetData);


    if (packet instanceof KeepAlivePacket) {
      receiveConnectionKeepAlivePacket(((KeepAlivePacket) packet).shouldRespond());
      return;

    } else if (packet instanceof ProtocolPacket) {
      ProtocolPacket pp = (ProtocolPacket) packet;
      validateProtocolPacket(pp);

      if (pp.getAction() == ProtocolPacket.ACTION_ACCEPT) {
        receiveConnectionAcceptPacket();

      } else if (pp.getAction() == ProtocolPacket.ACTION_DECLINE) {
        receiveConnectionDeclinedPacket();

      } else if (pp.getAction() == ProtocolPacket.ACTION_CLOSE) {
        status = Status.CLOSED;

      } else {
        throw new IllegalArgumentException(
            "action " + pp.getAction() + " is unknown in ProtocolPacket.");
      }

      return;
    }

    connection.packetReceived(packet);
  }

  /**
   * Handle connection-accepted event.
   */
  private void receiveConnectionAcceptPacket() {
    checkConnection(Status.REQUESTING);

    synchronized (ChatSocket.this) {
      connection.getTimeout().tick();

      // see #sendRequestConnectionPacket()
      connection.getTimeout().setTimeOutListener(null);


      if (!responseRequestCalled) {
        responseRequestCalled = true;

        if (isConnectionClosed()) {
          status = Status.CLOSED;

          HypixelUtils.threadPool.submit(() -> {
            connectionCallback.response(RequestResponse.TIMED_OUT, null);
          });

        } else {
          status = Status.OPEN;

          HypixelUtils.threadPool.submit(() -> {
            connectionCallback.response(RequestResponse.ACCEPTED, connection);
          });
        }
      }
    }
  }

  /**
   * Handle the connection-rejected event.
   */
  private void receiveConnectionDeclinedPacket() {
    checkConnection(Status.REQUESTING);

    synchronized (ChatSocket.this) {
      // see #sendRequestConnectionPacket()
      connection.getTimeout().setTimeOutListener(null);


      if (!responseRequestCalled) {
        responseRequestCalled = true;

        status = Status.CLOSED;

        HypixelUtils.threadPool.submit(() -> {
          connectionCallback.response(RequestResponse.REJECTED, null);
        });
      }
    }
  }

  /**
   * handle keep alive event. An event to inform the other client (which is this
   * one) to not time out. The keep alive packet should be sent and received back
   * to confirm the connection. {@code shouldRespond} is used to prevent infinite
   * loop.
   * 
   * @param shouldRespond
   *          true if the client should also respond back with keep alive packet.
   */
  private void receiveConnectionKeepAlivePacket(boolean shouldRespond) {
    checkConnection(Status.OPEN);

    if (shouldRespond) {
      sendConnectionKeepAlivePacket(false);
    }
  }

  /**
   * Check the current connection status from {@link #status} with the
   * {@code allowedStatus}. Throw an error if the current status does not match
   * any of the {@code allowedStatus}. Used to confirm the connection's status
   * before invoking methods, which require a special status.
   * 
   * @param allowedStatus
   *          status, which the method should not throw an exception.
   * 
   * @throws IllegalStateException
   *           if the current status does not match any of the
   *           {@code allowedStatus}.
   */
  private void checkConnection(@Nonnull Status... allowedStatus) throws IllegalStateException {
    if (connection.connectionClosed()) {
      throw new IllegalStateException("Connection is closed");
    }

    for (Status status : allowedStatus) {
      if (this.status == status) {
        return;
      }
    }

    throw new IllegalStateException(
        "Trying to handle a packet while not expecting it in this moment." +
            " current status: " + this.status.name() +
            ", required status(es) for this packet: " + Arrays.toString(allowedStatus));
  }

  /**
   * Validate the protocol packet by checking its metadata against this
   * connection.
   * 
   * @param packet
   *          the packet, which contains the metadata.
   * 
   * @throws IllegalArgumentException
   *           If the packet's metadata does not match the connections's
   */
  private void validateProtocolPacket(@Nonnull ProtocolPacket packet)
      throws IllegalArgumentException {
    if (!id.equals(packet.getId())) {
      throw new IllegalArgumentException(
          "packet's id does not match with the associated connection."
              + id + " is the current connection, "
              + packet.getId() + " was given in this packet.");
    }
    if (!actionId.equals(packet.getActionId())) {
      throw new IllegalArgumentException(
          "packet's actionId does not match with the associated connection."
              + actionId + " is the current connection, "
              + packet.getAction() + " was given in this packet.");
    }
  }

  /**
   * Generate a random number to use for a unique id that binds both with the
   * connection.
   * 
   * @return
   *         a random number, which is made of 4 bits.
   */
  static int generateConnectionId() {
    Random r = new Random();
    return r.nextInt();
  }

  /**
   * Socket's status. Used to with {@link ChatSocket#checkConnection(Status...)}
   * to prevent unwanted packets from passing through the handler.
   * 
   * @author aidn5
   *
   * @since 1.0
   */
  @IHypixelUtils
  @IBackend
  private enum Status {
    /**
     * Either the connection is new and has no real status,
     * or the connection is initiated due to a request and it is waiting a response
     * (accept/reject) from the user.
     */
    PENDING,
    /**
     * This connection is waiting for a response from the other end (accept/reject).
     */
    REQUESTING,
    /**
     * The connection is initiated and ready to send data and packets.
     */
    OPEN,
    /**
     * The connection is closed and must never be tried to send anything at all.
     */
    CLOSED;
  }
}
