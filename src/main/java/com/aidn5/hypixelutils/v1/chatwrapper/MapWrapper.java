
package com.aidn5.hypixelutils.v1.chatwrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IChatWrapper;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IOnlyHypixel;
import com.aidn5.hypixelutils.v1.exceptions.NotOnHypixelNetwork;
import com.aidn5.hypixelutils.v1.tools.TickDelay;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Wrapper for the <code>/map</code> command on Hypixel and determining the
 * map the client is playing on.
 * 
 * @author aidn5
 * @author robere2
 * 
 * @since 1.0
 * @version 1.0
 * 
 * @category ChatWrapper
 */
/*
 * The template of this wrapper is copied from
 * "https://github.com/robere2/Quickplay2.0/blob/1.8.9/src/main/java/co/bugg/quickplay/util/WhereamiWrapper.java"
 */
@IHypixelUtils
@IOnlyHypixel
@IChatWrapper(usesLock = false)
public class MapWrapper {

  /**
   * Whether this wrapper should listen for & action on chat messages.
   */
  private boolean listening;
  /**
   * Whether this wrapper should cancel map messages it finds.
   */
  private boolean cancel;
  /**
   * Callback when this wrapper finds a /map message.
   */
  @Nullable
  private final MapCallback callback;

  @Nonnull
  private final HypixelUtils hypixelUtils;

  /**
   * Constructor.
   *
   * @param callback
   *          Callback when this wrapper finds a /map message
   * @param hypixelUtils
   *          a library instance.
   * 
   * @throws NotOnHypixelNetwork
   *           if the client was not connected to the hypixel network
   * 
   * @since 1.0
   */
  public MapWrapper(@Nullable MapCallback callback, HypixelUtils hypixelUtils) {

    if (!hypixelUtils.onHypixel()) {
      throw new NotOnHypixelNetwork();
    }

    MinecraftForge.EVENT_BUS.register(this);
    this.callback = callback;
    this.listening = true;
    this.cancel = true;
    this.hypixelUtils = hypixelUtils;

    // Send the /map command
    hypixelUtils.chatBuffer.offer("/whereami");
    // If a /whereami isn't received within 120 ticks (6 seconds),
    // don't cancel the message
    new TickDelay(this::stopCancelling, 120);

    // If a /map isn't received within 1200 ticks (60 seconds), stop listening
    new TickDelay(() -> stopListening("", ""), 1200);
  }

  /**
   * Don't cancel the chat message if it comes
   * in, but still listen & call the callback.
   * 
   * @since 1.0
   */
  public void stopCancelling() {
    this.cancel = false;
  }

  /**
   * Stop listening for a chat message
   * and call the callback.
   * 
   * @param mapName
   *          the param to pass to the callback
   * @param fullMessage
   *          the param to pass to the callback
   * 
   * @since 1.0
   */
  public void stopListening(@Nonnull String mapName, @Nonnull String fullMessage) {
    if (listening) {
      this.listening = false;
      MinecraftForge.EVENT_BUS.unregister(this);

      if (callback != null) {
        hypixelUtils.threadPool.submit(() -> {
          callback.call(mapName, fullMessage);
        });

      }
    }
  }

  @IBackend
  @SubscribeEvent(receiveCanceled = true, priority = EventPriority.LOW)
  public void onChat(ClientChatReceivedEvent event) {
    if (!listening) {
      MinecraftForge.EVENT_BUS.unregister(this);
      return;
    }

    final String message = event.message.getUnformattedText();

    Matcher mapPatternM = getMapPattern().matcher(message);

    // find the /map response first.
    if (hypixelUtils.onHypixel() && mapPatternM.find()) {
      stopListening(mapPatternM.group(1), message);

      if (this.cancel) {
        event.setCanceled(true);
      }
    }
  }

  /**
   * Return a pattern, which can find out whether the text is a /map response.
   * 
   * @return pattern detects /map's response.
   * 
   * @since 1.0
   */
  public static Pattern getMapPattern() {
    return Pattern.compile("^You are currently playing on ([a-zA-Z0-9 ]{1,32})");
  }

  /**
   * Interface for inline callbacks
   * Called when a response to /map is received,
   * or after 60 seconds of no response.
   * 
   * @author aidn5
   * @version 1.0
   * @since 1.0
   */
  @FunctionalInterface
  @IHypixelUtils
  @IOnlyHypixel
  public interface MapCallback {
    /**
     * callback on a separate thread when the message is found in the chat.
     * 
     * @param mapName
     *          the current map the client is playing on.
     *          <u>might be empty but never <code>null</code></u>
     * @param fullMessage
     *          the used message to extract the mapName.
     *          <u>might be empty but never <code>null</code></u>
     */
    void call(@Nonnull String mapName, @Nonnull String fullMessage);
  }
}
