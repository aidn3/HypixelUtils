
package com.aidn5.hypixelutils.v1.chatsocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.chatsocket.client.ChatSocket;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestReceiveEvent;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestReceiveEvent.RequestReceived;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestSendEvent;
import com.aidn5.hypixelutils.v1.chatsocket.protocols.BaseProtocol;
import com.aidn5.hypixelutils.v1.chatsocket.protocols.ChatHypixelProtocol;
import com.aidn5.hypixelutils.v1.chatsocket.protocols.ChatUniversalProtocol;
import com.aidn5.hypixelutils.v1.chatsocket.protocols.ChatVanillaProtocol;
import com.aidn5.hypixelutils.v1.chatsocket.protocols.ProtocolsRegistry;
import com.aidn5.hypixelutils.v1.chatsocket.wrapper.RequestWrapper;
import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.exceptions.HypixelUtilsInternalError;

/**
 * Factory used to register protocols and packets for service,
 * to register listeners for new incoming connections
 * and to send request for new connection.
 * 
 * <p>
 * <b>ChatSocket</b> is a protocol used to connect two mods/programs of two
 * different players on the same minecraft server and help them communicate with
 * each other by providing them with {@link InputStream}, {@link OutputStream},
 * etc. through chat. It also provides authentication and other features for a
 * stable connection between the two ends. It is a completely side-Client and
 * needs only the ability to listen to incoming chat-messages and the ability to
 * send private messages to the other player (like permission to use the command
 * "/msg [username] {message}" on the specified server).
 * 
 * <p>
 * <b>Protocols: </b> are chat listeners, which used to send and receive chat
 * messages for the server it intends to work on. for example
 * {@link ChatHypixelProtocol} is used to send and receive messages online
 * hypixel network.<br>
 * Registering protocols are done by {@link #registerProtocol(Class)}.
 * Unregistering protocols is not supported to avoid problems.
 * 
 * <p>
 * <b>Listeners: </b> are interfaces used to inform for new request connections.
 * <i>Every {@code modid} can register only <u>ONE</u> listener.
 * Trying to register more than one listener will remove the old one and replace
 * it with the new one</i><br>
 * Registering and unregistering listeners are done by
 * {@link #registerListener(String, RequestReceived)} and
 * {@link #unregisterListener(String)}. See {@link RequestWrapper} for a
 * wrapper.
 * 
 * @author aidn5
 * 
 * @since 1.0
 * 
 * @see RequestWrapper
 * @see ProtocolsRegistry
 */
@IHypixelUtils
public class ChatSocketFactory {
  @Nonnull
  private static final HashMap<String, RequestReceived> listeners = new HashMap<>();
  /**
   * Initiate the sockets and listeners by calling their static methods.
   */
  static {
    try {
      registerProtocol(ChatHypixelProtocol.class);
      registerProtocol(ChatVanillaProtocol.class);

      // should be the last in the list.
      // since it is the last resort
      // and it is always active.
      registerProtocol(ChatUniversalProtocol.class);
    } catch (Exception e) {
      throw new HypixelUtilsInternalError("can not initiate the ChatSocket service", e);
    }
  }

  private ChatSocketFactory() {
    throw new AssertionError();
  }

  /**
   * Register a listener to start receiving connection requests.
   * 
   * @param modid
   *          the id of the listener to be called to.
   *          Must be between 3 and 16 length.
   * @param callback
   *          a callback to notify when a new connection is incoming.
   * 
   * @throws IllegalArgumentException
   *           if {@code modid} is null, shorter than 3 or longer than 16,
   *           or {@code callback} is <code>null</code>.
   */
  public static void registerListener(@Nonnull String modid, @Nonnull RequestReceived callback)
      throws IllegalArgumentException {

    if (modid == null || modid.trim().length() < 3 || modid.trim().length() > 16) {
      throw new IllegalArgumentException(
          "modid must not be null and must be beween 3 and 16 length.");
    }
    if (callback == null) {
      throw new IllegalArgumentException("callback must not be null.");
    }

    listeners.put(modid, callback);
  }

  /**
   * Register new protocol for other servers. See {@link BaseProtocol} for further
   * information about the Protocols specifications.
   * 
   * 
   * @param protocolClass
   *          protocol class to register.
   * @throws RuntimeException
   *           if an exception is encountered while registering the protocol.
   */
  public static void registerProtocol(@Nonnull Class<? extends BaseProtocol> protocolClass)
      throws RuntimeException {
    try {
      ProtocolsRegistry.registerNewProtocol(protocolClass);

    } catch (Exception e) {
      throw new RuntimeException("Could not register the protocol " + protocolClass.getName(), e);
    }
  }

  /**
   * Remove a listener and stop receiving incoming connections.
   * 
   * @param modid
   *          the id to stop listening to.
   * @return
   *         the registered callback to this id,
   *         or <code>null</code>
   */
  @Nonnull
  public static RequestReceived unregisterListener(@Nonnull String modid) {
    return listeners.remove(modid);
  }

  /**
   * Initiate new request connection to use.
   * 
   * @param modid
   *          the id of the listener to request when connecting.
   * @param actionId
   *          the action to do with the listener.
   * 
   * @return
   *         an instance, which can be used to send requests.
   */
  public static RequestSendEvent createRequest(@Nonnull String modid, String actionId) {
    return new RequestSendEvent(modid, actionId);
  }

  /**
   * Create an instance and make it listen to the id. This instance has the
   * feature to register an individual listener to every action instead of
   * listening to all actions with the same id. It also notify the user for new
   * requests and provides a callback, when the user accept the request connection
   * through chat<i><u>. This will remove any current listener, which is
   * registered at this moment.</u></i>
   * 
   * @param modid
   *          the id of the requests to listen to.
   * @param displayChatName
   *          the name to display on the chat when a new request is incoming and
   *          approve is required from the player to connect.
   * 
   * @return
   *         a registered instance.
   * 
   * @see ChatSocketFactory#createWrapper(String, String)
   * @see ChatSocketFactory#registerListener(String, RequestReceived)
   */
  public static RequestWrapper createWrapper(@Nonnull String modid,
      @Nonnull String displayChatName) {
    return new RequestWrapper(modid, displayChatName);
  }

  /**
   * handle the new connection.<br>
   * <b><i>This is a Backend method. Do NOT use it.</i></b>
   * 
   * @param conneciton
   *          the new connection to handle.
   * 
   * @throws IllegalStateException
   *           if there is no listener to this connection.
   */
  @IBackend
  public static void newRequestReceived(@Nonnull ChatSocket conneciton)
      throws IllegalStateException {
    RequestReceived listener = listeners.get(conneciton.id);

    if (listener != null) {
      RequestReceiveEvent re = new RequestReceiveEvent(conneciton);
      listener.get(re);

    } else {
      throw new IllegalStateException(
          "New incoming connection with the id " + conneciton.id
              + " has no listener. the request has been ignored.");
    }
  }
}
