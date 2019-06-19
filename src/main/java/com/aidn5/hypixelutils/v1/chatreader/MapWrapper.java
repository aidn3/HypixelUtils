
package com.aidn5.hypixelutils.v1.chatreader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.server.GameMode;
import com.aidn5.hypixelutils.v1.server.ServerType;
import com.aidn5.hypixelutils.v1.tools.TickDelay;

import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Wrapper for the <code>/map</code> command on Hypixel and determining the
 * map and game mode the client is playing.
 * 
 * @author aidn5
 * @author Buggfroggy
 * 
 * @since 1.0
 * @version 1.0
 * 
 * @category ChatReader
 */
public class MapWrapper {

  /**
   * Whether this wrapper should listen for & action on chat messages.
   */
  private boolean listening;
  /**
   * Whether this wrapper should cancel whereami messages it finds.
   */
  private boolean cancel;
  /**
   * Callback when this wrapper finds a /whereami message.
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
   * 
   * @since 1.0
   */
  public MapWrapper(@Nullable MapCallback callback, HypixelUtils hypixelUtils) {

    MinecraftForge.EVENT_BUS.register(this);
    this.callback = callback;
    this.listening = true;
    this.cancel = true;
    this.hypixelUtils = hypixelUtils;

    // Send the /whereami command
    hypixelUtils.chatBuffer.offer("/whereami");
    // If a /whereami isn't received within 120 ticks (6 seconds),
    // don't cancel the message
    new TickDelay(this::stopCancelling, 120);

    // If a /whereami isn't received within 1200 ticks (60 seconds), stop listening
    new TickDelay(() -> stopListening(GameMode.UNKNOWN, "", ""), 1200);
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
   * and call the callback
   * 
   * @param instance
   *          Current instance to pass to callback
   * 
   * @since 1.0
   */

  /**
   * Stop listening for a chat message
   * and call the callback.
   * 
   * @param gameType
   *          the param to pass to the callback
   * @param mapName
   *          the param to pass to the callback
   * @param fullMessage
   *          the param to pass to the callback
   * 
   * @since 1.0
   */
  public void stopListening(@Nonnull GameMode gameType, @Nonnull String mapName,
      @Nonnull String fullMessage) {
    if (listening) {
      this.listening = false;
      MinecraftForge.EVENT_BUS.unregister(this);

      if (callback != null) {
        hypixelUtils.threadPool.submit(() -> {
          callback.call(gameType, mapName, fullMessage);
        });

      }
    }
  }

  @SubscribeEvent(receiveCanceled = true, priority = EventPriority.LOW)
  public void onChat(ClientChatReceivedEvent event) {
    if (!listening) {
      MinecraftForge.EVENT_BUS.unregister(this);
      return;
    }

    final String message = event.message.getUnformattedText();

    Matcher unitedGameModeM = getUnitedPattern().matcher(message);

    // find the /map response first.
    if (hypixelUtils.onHypixel() && unitedGameModeM.find()) {
      // TODO: fix map wrapper
      stopListening(GameMode.UNKNOWN, unitedGameModeM.group(1), message);

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
  public static Pattern getUnitedPattern() {
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
   * @category EventsCallback
   */
  @FunctionalInterface
  public interface MapCallback {
    /**
     * callback on a separate thread when the message is found in the chat.
     * 
     * <p>
     * if gameMode is {@link GameMode#UNKNOWN}
     * and mapName and fullMessage are empty,
     * it means that the listener could not
     * find any response in the last 60 seconds.
     * 
     * @param gameMode
     *          what type of game mode is the client playing/connected to.
     * @param mapName
     *          the map name the client connected to. e.g. "Waterfall",
     *          "Warehouse". <u>might be empty but never <code>null</code></u>
     * @param fullMessage
     *          the message which is used to detect {@link GameType} and mapName.
     *          Useful for debugging
     *          <u>might be empty but never <code>null</code></u>
     * 
     * @see ServerType
     */
    void call(@Nonnull GameMode gameMode, @Nonnull String mapName, @Nonnull String fullMessage);
  }
}
