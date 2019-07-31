
package com.aidn5.hypixelutils.v1.chatsocket.client;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.players.Player;

/**
 * A holder Holds the connection's id and its metadata can be saved and reused
 * to send the same request to multiple destinations.
 * 
 * @author aidn5
 *
 * @since 1.0
 */
@IHypixelUtils
public class RequestSendEvent {
  @Nonnull
  private final String id;
  @Nonnull
  private final String actionId;

  /**
   * Constructor.
   * 
   * @param id
   *          the id of the programs which are trying to communicate with each
   *          other.
   * @param actionId
   *          an extra field for the program.
   *          used to define their intends from each other.
   */
  public RequestSendEvent(@Nonnull String id, @Nonnull String actionId) {

    if (id == null || id.length() > 16 || id.length() < 3) {
      throw new IllegalArgumentException("id must be between 3 and 16 long");
    }

    if (actionId == null || actionId.length() > 16 || actionId.length() < 3) {
      throw new IllegalArgumentException("actionId must be between 3 and 16 long");
    }

    this.id = id;
    this.actionId = actionId;
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
   * Send a new request to the other user.
   * 
   * @param user
   *          the user, who is at the other end of the connection.
   * @param callback
   *          callback to receive when a response is given.
   */
  public void sendNewRequest(@Nonnull String user, IResponseRequest callback) {
    Player.validateUsername(user);

    ChatSocket chatSocket = new ChatSocket(
        user, getId(), getActionId(), ChatSocket.generateConnectionId(), true);

    ChatSocketsManager.addNewConnection(chatSocket);
    chatSocket.sendConnectionRequestPacket(callback);
  }

  /**
   * Enum indicates the response to the conneciton request.
   * 
   * @author aidn5
   * 
   * @since 1.0
   */
  @IHypixelUtils
  public enum RequestResponse {
    /**
     * Indicates that the client has accepted the connection request and ready to
     * receive and send data without any further auth.
     */
    ACCEPTED,
    /**
     * Indicates that the client has rejected the connection request and has closed
     * the connection.
     */
    REJECTED,
    /**
     * Indicates that the client has neither accepted nor rejected the connection
     * and that the client has just ignored or didn't receive the request. the
     * connection is closed and should create a new one to connect if needed.
     */
    TIMED_OUT;
  }
}
