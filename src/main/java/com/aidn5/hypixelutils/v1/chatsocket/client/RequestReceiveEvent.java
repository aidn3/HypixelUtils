
package com.aidn5.hypixelutils.v1.chatsocket.client;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * An event class used to inform the user about the request of a new connection.
 * The user can either choose {@link #acceptConnection()},
 * {@link #declineConnection()} or just ignore it.
 * 
 * @author aidn5
 *
 * @since 1.0
 */
@IHypixelUtils
public class RequestReceiveEvent {
  @Nonnull
  private final ChatSocket chatSocket;

  /**
   * Constructor.
   * 
   * @param parent
   *          parent of the connection.
   */
  @IBackend
  public RequestReceiveEvent(@Nonnull ChatSocket parent) {
    this.chatSocket = Objects.requireNonNull(parent);
  }

  /**
   * get the user, who at the other end of the connection.
   * Used as a destination for the packets.
   * 
   * @return
   *         the user, who is at the other end of the connection.
   */
  @Nonnull
  public String getUser() {
    return chatSocket.user;
  }

  /**
   * the id of the programs which are trying to communicate with each other.
   *
   * @return
   *         the id of the program.
   */
  @Nonnull
  public String getId() {
    return chatSocket.id;
  }

  /**
   * an extra field for the program.
   * used to define their intends from each other.
   *
   * @return
   *         the extra field.
   */
  @Nonnull
  public String getActionId() {
    return chatSocket.actionId;
  }

  @IBackend
  public int getConnectionId() {
    return chatSocket.connectionId;
  }

  /**
   * Check whether the connection is still active to
   * use {@link #declineConnection()} and {@link #acceptConnection()}.
   * 
   * @return
   *         <code>true</code> if the connection is still waiting.
   */
  public boolean canSend() {
    return chatSocket.connectionId > 0 && !chatSocket.isConnectionClosed();
  }

  /**
   * Accept the requested connection.
   * 
   * @throws IllegalStateException
   *           if the connection is closed/timed-out.
   *           in other words, {@link #canSend()} is <code>false</code>.
   * 
   * @return
   *         an instance, which can be used to communicate with the other end.
   */
  @Nonnull
  public Connection acceptConnection() throws IllegalStateException {
    if (!canSend()) {
      throw new IllegalStateException("can not send a request to accept the packet");
    }

    chatSocket.sendConnectionAcceptPacket();
    return chatSocket.connection;
  }

  /**
   * Reject the requested connection.
   * 
   * @throws IllegalStateException
   *           if the connection is closed/timed-out already.
   *           in other words, {@link #canSend()} is <code>false</code>.
   */
  public void declineConnection() throws IllegalStateException {
    if (!canSend()) {
      throw new IllegalStateException("can not send a request to accept the packet");
    }

    chatSocket.sendConnectionDeclinePacket();
  }

  /**
   * Interface used to inform the user about new request connections.
   * 
   * @author aidn5
   *
   * @since 1.0
   */
  @IHypixelUtils
  @FunctionalInterface
  public interface RequestReceived {
    /**
     * called when the listener found a new request connection.
     * 
     * @param re
     *          an instance used as connection-data-wrapper used provide a way to
     *          read connection's information and to choose whether to accept
     *          or reject the connection.
     */
    void get(@Nonnull RequestReceiveEvent re);
  }
}
