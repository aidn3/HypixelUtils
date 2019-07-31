
package com.aidn5.hypixelutils.v1.chatwrapper;

import java.util.regex.Matcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IChatWrapper;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IOnlyHypixel;
import com.aidn5.hypixelutils.v1.exceptions.NotOnHypixelNetwork;
import com.aidn5.hypixelutils.v1.serverinstance.ServerType;
import com.aidn5.hypixelutils.v1.tools.TickDelay;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Class checks to what server is the client is connected to
 * every time the world changes.
 * 
 * @author aidn5
 * 
 * @version 1.0
 * @since 1.0
 * 
 * @category ChatWrapper
 * 
 * @see "https://github.com/robere2/Quickplay2.0/blob/1.8.9/src/main/java/co/bugg/quickplay/util/WhereamiWrapper.java"
 */
/*
 * The template of this wrapper is copied from
 * "https://github.com/robere2/Quickplay2.0/blob/1.8.9/src/main/java/co/bugg/quickplay/util/WhereamiWrapper.java"
 */
@IHypixelUtils
@IOnlyHypixel
@IChatWrapper(usesLock = false)
public class WhereamiWrapper {
  /**
   * Whether this wrapper should listen for & action on chat messages.
   */
  boolean listening;
  /**
   * Whether this wrapper should cancel whereami messages it finds.
   */
  boolean cancel;

  @Nullable
  private WhereamiCallback callback;

  @Nonnull
  private final HypixelUtils hypixelUtils;

  /**
   * Constructor.
   * 
   * @param callback
   *          Callback when this wrapper finds a /whereami message
   * @param hypixelUtils
   *          a library instance. if <code>null</code>
   *          the default instance will be used.
   * 
   * @throws NotOnHypixelNetwork
   *           if the client was not connected to the hypixel network
   */
  public WhereamiWrapper(@Nullable WhereamiCallback callback, @Nullable HypixelUtils hypixelUtils)
      throws NotOnHypixelNetwork {

    if (hypixelUtils != null) {
      this.hypixelUtils = hypixelUtils;
    } else {
      this.hypixelUtils = HypixelUtils.defaultInstance();
    }

    if (!this.hypixelUtils.onHypixel()) {
      throw new NotOnHypixelNetwork();
    }

    this.callback = callback;

    MinecraftForge.EVENT_BUS.register(this);

    new TickDelay(this::sendCommand, 15);
    new TickDelay(this::sendCommand, 60);
    new TickDelay(this::stopListening, 1200);
  }

  /**
   * Stop listening for /whereami and callback with empty response.
   * This method has no effect if the callback has already been made.
   */
  public void stopListening() {
    stopListening(ServerType.UNKNOWN, "", "");
  }

  /**
   * Unregister this class from {@link MinecraftForge#EVENT_BUS}
   * and {@link #callback} if {@link #listening} is still <code>true</code>.
   * 
   * @param serverType
   *          what type of server is the client connected to.
   * @param serverName
   *          the server name the client connected to. e.g. "swlobby123",
   *          "mini12J". <u>might be empty but never <code>null</code></u>
   * @param fullMessage
   *          the message which is used to detect serverType and serverName.
   *          Useful for debugging
   *          <u>might be empty but never <code>null</code></u>
   * 
   */
  private synchronized void stopListening(@Nonnull ServerType serverType,
      @Nonnull String serverName, @Nonnull String fullMessage) {

    MinecraftForge.EVENT_BUS.unregister(this);

    if (!listening) {
      return;
    }

    listening = false;

    if (callback != null) {
      hypixelUtils.threadPool.submit(() -> {
        callback.call(serverType, serverName, fullMessage);
      });
    }
  }

  private void sendCommand() {
    hypixelUtils.chatBuffer.add("/whereami");
  }

  @IBackend
  @SubscribeEvent(receiveCanceled = true)
  public void onPlayerChat(ClientChatReceivedEvent event) {
    if (!hypixelUtils.onHypixel()) {
      stopListening();
    }

    if (event == null || event.type != 0) {
      return;
    }

    final String message = event.message.getUnformattedText();

    for (ServerType serverType : ServerType.values()) {
      Matcher matcher = serverType.getWhereAmIPattern().matcher(message);

      if (matcher.find()) {
        if (cancel) {
          event.setCanceled(true);
        }

        stopListening(serverType, matcher.group(1), message);
        return;
      }
    }
  }

  /**
   * The interface to use to receive the callback
   * when the process of {@link WhereamiWrapper} is finished.
   * 
   * @author aidn5
   * 
   * @version 1.0
   * @since 1.0
   */
  @IHypixelUtils
  @IOnlyHypixel
  @FunctionalInterface
  public interface WhereamiCallback {
    /**
     * callback on a separate thread when the message is found in the chat.
     * 
     * @param serverType
     *          what type of server is the client connected to.
     * @param serverName
     *          the server name the client connected to. e.g. "swlobby123",
     *          "mini12J". <u>might be empty but never <code>null</code></u>
     * @param fullMessage
     *          the message which is used to detect serverType and serverName.
     *          Useful for debugging
     *          <u>might be empty but never <code>null</code></u>
     * 
     * @see ServerType
     */
    public void call(@Nonnull ServerType serverType, @Nonnull String serverName,
        @Nonnull String fullMessage);
  }
}
