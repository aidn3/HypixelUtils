
package com.aidn5.hypixelutils.v1.chatwrapper;

import java.util.regex.Matcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.exceptions.NotOnHypixelNetwork;
import com.aidn5.hypixelutils.v1.serverinstance.ServerType;
import com.aidn5.hypixelutils.v1.tools.TickDelay;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
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
 * @category ChatReader
 */
public class WhereAmIWrapper {
  /**
   * how many times is /whereami sent.
   * <p>
   * Used to know how many times should the class cancel the message.
   * so when the user manually sends the command, the message won't be canceled
   * by the library.
   */
  private int timesCommandSent = 0;

  private boolean callbackSent = false;

  @Nullable
  private WhereAmICallback callback;

  @Nonnull
  private final HypixelUtils hypixelUtils;

  /**
   * Constructor.
   * 
   * @param callback
   *          Callback when this wrapper finds a /whereami message
   * @param hypixelUtils
   *          a library instance.
   * 
   * @throws NotOnHypixelNetwork
   *           if the client was not connected to the hypixel network
   */
  public WhereAmIWrapper(@Nullable WhereAmICallback callback, @Nonnull HypixelUtils hypixelUtils)
      throws NotOnHypixelNetwork {
    if (!hypixelUtils.onHypixel()) {
      throw new NotOnHypixelNetwork();
    }

    this.callback = callback;
    this.hypixelUtils = hypixelUtils;
    MinecraftForge.EVENT_BUS.register(this);


    new TickDelay(this::sendCommand, 15);
    new TickDelay(this::sendCommand, 60);
    new TickDelay(this::stopListening, 1200);
  }

  /**
   * Unregister this class from {@link MinecraftForge#EVENT_BUS}
   * and {@link #callback} if {@link #callbackSent} is still <code>false</code>.
   */
  public void stopListening() {
    MinecraftForge.EVENT_BUS.unregister(this);

    if (!callbackSent && callback != null) {
      callbackSent = true;

      hypixelUtils.threadPool.submit(() -> {
        callback.call(ServerType.UNKNOWN, "", "");
      });
    }
  }

  private void sendCommand() {
    timesCommandSent++;
    hypixelUtils.chatBuffer.add("/whereami");
  }

  @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
  public void onPlayerChat(ClientChatReceivedEvent event) {
    if (event == null || event.type != 0 || !hypixelUtils.onHypixel()) {
      return;
    }
    final String message = event.message.getUnformattedText();

    for (ServerType serverType : ServerType.values()) {
      Matcher matcher = serverType.getWhereAmIPattern().matcher(message);
      if (matcher.find()) {
        System.out.println("whereAmI found! " + timesCommandSent);
        if (!event.isCanceled()) {
          if (timesCommandSent > 0) {
            timesCommandSent--;
            event.setCanceled(true);
          }
        }

        if (!callbackSent && callback != null) {
          callbackSent = true;
          hypixelUtils.threadPool.submit(() -> {
            callback.call(serverType, matcher.group(1), message);
          });
        }

        if (timesCommandSent <= 0) {
          stopListening();
        }
        return;
      }
    }
  }

  /**
   * The interface to use to receive the callback
   * when the process of {@link WhereAmIWrapper} is finished.
   * 
   * @author aidn5
   * 
   * @version 1.0
   * @since 1.0
   */
  @FunctionalInterface
  public interface WhereAmICallback {
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
