
package com.aidn5.hypixelutils.v1.eventslistener;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.common.EventListener;
import com.aidn5.hypixelutils.v1.common.ListenerBus;
import com.aidn5.hypixelutils.v1.eventslistener.HypixelApiListener.HypixelApiCallback;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Class reads chat and detects when a new Hypixel's API is generated.
 * <p>
 * It also provides {@link ListenerBus} to register listeners and callback
 * when the status {@link #getHypixelApi()} changes
 * 
 * @author aidn5
 * 
 * @version 1.0
 * @since 1.0
 * 
 * @category ChatReader
 * @category ListenerBus
 */
public final class HypixelApiListener extends ListenerBus<HypixelApiCallback> {
  @Nonnull
  private static final Pattern apiPattern = Pattern
      .compile("^Your new API key is ([A-Za-z0-9\\-]{36})");

  @Nullable
  private UUID api;

  @Nonnull
  private final HypixelUtils hypixelUtils;

  private HypixelApiListener(@Nonnull HypixelUtils hypixelUtils) {
    this.hypixelUtils = hypixelUtils;
  }

  /**
   * get Hypixel's API.
   * 
   * @return last detected API. <code>null</code> if not yet detected.
   */
  @Nullable
  public UUID getHypixelApi() {
    return api;
  }

  @SubscribeEvent
  public void onPlayerChatReceive(ClientChatReceivedEvent event) {
    if (hypixelUtils.onHypixel() && event != null || event.type == 0) {

      final String message = event.message.getUnformattedText();

      Matcher matcher = apiPattern.matcher(message);
      if (matcher.find()) {
        runCallbacks(UUID.fromString(matcher.group(1)));
      }
    }
  }

  private void runCallbacks(@Nonnull UUID api) {
    this.api = api;

    if (api != null) {

      if (hypixelUtils.isDefaultInstance()) {

        hypixelUtils.threadPool.submit(() -> {
          MinecraftForge.EVENT_BUS.post(new HypixelApiEvent(api));
        });
      }

      for (HypixelApiCallback listener : getListeners()) {
        hypixelUtils.threadPool.submit(() -> {
          listener.onHypixelApiUpdate(api);
        });
      }
    }
  }

  /**
   * an inline interface used to callback when a new result is found.
   * 
   * @author aidn5
   *
   * @version 1.0
   * @since 1.0
   * 
   * @category EventListener
   */
  @FunctionalInterface
  public interface HypixelApiCallback extends EventListener {
    /**
     * callback on a separate thread
     * when listener is triggered.
     * 
     * @param hypixelApi
     *          the new detected Hypixel's api
     * 
     * @since 1.0
     */
    public void onHypixelApiUpdate(@Nonnull UUID hypixelApi);
  }

  /**
   * event which is only called in the default instance of {@link HypixelUtils}.
   * 
   * @author aidn5
   * 
   * @since 1.0
   * @version 1.0
   * 
   * @category Event
   */
  public static class HypixelApiEvent extends Event {
    @Nonnull
    private final UUID hypixelApi;

    private HypixelApiEvent(@Nonnull UUID hypixelApi) {
      this.hypixelApi = Objects.requireNonNull(hypixelApi);
    }

    /**
     * get Hypixel's API.
     * 
     * @return Hypixel's API.
     */
    public UUID getHypixelApi() {
      return hypixelApi;
    }
  }
}
