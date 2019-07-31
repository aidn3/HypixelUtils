
package com.aidn5.hypixelutils.v1.chatsocket.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;


/**
 * Manage coming connections and messages from the protocols.
 *
 * @author aidn5
 * 
 * @since 1.0
 */
@IHypixelUtils
@IBackend
public class ChatSocketsManager {
  private static Set<ChatSocket> chatSockets = new HashSet<>();

  /**
   * add the new connection.
   * 
   * @param chatSocket
   *          the connection to add
   * 
   * @throws IllegalArgumentException
   *           if the securityHash matches
   * 
   * @see #getConnection(int)
   */
  public static void addNewConnection(ChatSocket chatSocket) throws IllegalArgumentException {
    if (getConnection(chatSocket.connectionId) != null) {
      throw new IllegalArgumentException(
          "There is already connection with the id " + chatSocket.connectionId);
    }

    chatSockets.add(chatSocket);
  }

  /**
   * get the connection by the hash.
   * 
   * @param securityHash
   *          the hash to use to look up for the connection.
   * @return
   *         an instance of the connection if it is not closed yet.
   *         otherwise {@code null}.
   */
  public static ChatSocket getConnection(int securityHash) {
    chatSockets.remove(null);

    synchronized (chatSockets) {
      Iterator<ChatSocket> iterator = chatSockets.iterator();

      while (iterator.hasNext()) {
        ChatSocket cp = iterator.next();

        if (cp.isConnectionClosed()) {
          iterator.remove();
        }

        if (cp.connectionId == securityHash) {
          return cp;
        }
      }

      return null;
    }
  }
}
