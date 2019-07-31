
package com.aidn5.hypixelutils.v1.chatsocket.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.chatsocket.ChatSocketFactory;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestSendEvent.RequestResponse;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * Interface used to get the response of requesting new connection.
 * 
 * @author aidn5
 * 
 * @since 1.0
 *
 * @see ChatSocketFactory#createRequest(String, String)
 * @see RequestSendEvent
 */
@IHypixelUtils
public interface IResponseRequest {
  /**
   * Get the response of the request (and the connection if the request is
   * accepted)
   * 
   * @param rr
   *          the response. Whether it is accepted/rejected/timed-out.
   * @param cp
   *          the connection, if the response {@code rr} is
   *          {@link RequestResponse#ACCEPTED}.
   */
  void response(@Nonnull RequestResponse rr, @Nullable Connection cp);
}
