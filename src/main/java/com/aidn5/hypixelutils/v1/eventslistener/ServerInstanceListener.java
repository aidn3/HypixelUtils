
package com.aidn5.hypixelutils.v1.eventslistener;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.chatwrapper.WhereamiWrapper;
import com.aidn5.hypixelutils.v1.chatwrapper.WhereamiWrapper.WhereamiCallback;
import com.aidn5.hypixelutils.v1.common.EventListener;
import com.aidn5.hypixelutils.v1.common.ListenerBus;
import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IEventListener;
import com.aidn5.hypixelutils.v1.common.annotation.IEventListener.IForgeEvent;
import com.aidn5.hypixelutils.v1.common.annotation.IEventListener.IInterfaceEvent;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IOnlyHypixel;
import com.aidn5.hypixelutils.v1.eventslistener.OnHypixelListener.OnHypixelCallback;
import com.aidn5.hypixelutils.v1.eventslistener.OnHypixelListener.VerificationMethod;
import com.aidn5.hypixelutils.v1.eventslistener.ServerInstanceListener.ServerInstanceCallback;
import com.aidn5.hypixelutils.v1.serverinstance.ServerInstance;
import com.aidn5.hypixelutils.v1.serverinstance.ServerType;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Class checks to what server is the client is connected to every time the
 * world changes.
 * 
 * <p>
 * It also provides {@link ListenerBus} to register listeners and callback when
 * the status
 * {@link #getLastServerInstance()} changes.
 * 
 * @author aidn5
 * 
 * @version 1.0 @since 1.0
 * 
 * @category ListenerBus @category
 */
@IHypixelUtils
@IOnlyHypixel
@IEventListener
public final class ServerInstanceListener extends ListenerBus<ServerInstanceCallback> {
  @Nonnull
  private final HypixelUtils hypixelUtils;

  // in case the message is not received after a while
  // from the event onWorldChange,
  // this callback will automatically be called
  // to inform the depended on listeners.
  @Nonnull
  private final WhereamiCallback callback = new WhereamiCallback() {
    @Override
    public void call(ServerType serverType, String serverName, String fullMessage) {
      runCallbacks(
          ServerInstance.createInstance(fullMessage, Minecraft.getMinecraft(), hypixelUtils));
    }
  };

  // last time #runCallbacks()
  @Nonnull
  private ServerInstance lastServerInstance = new ServerInstance();

  private ServerInstanceListener(@Nonnull HypixelUtils hypixelUtils) {
    this.hypixelUtils = hypixelUtils;

    hypixelUtils.onHypixelListener.register(new OnHypixelCallback() {
      @Override
      public void onOnHypixelUpdate(boolean onHypixel, String ip, VerificationMethod method) {
        if (onHypixel) {
          MinecraftForge.EVENT_BUS.register(ServerInstanceListener.this);
          new WhereamiWrapper(callback, hypixelUtils);
        } else {
          MinecraftForge.EVENT_BUS.unregister(ServerInstanceListener.this);
        }
      }
    });
  }

  /**
   * get the saved server instance since the last time is requested (every time
   * when the world
   * changes).
   * 
   * @return the serverInstance (mostly up-to-date). <u>Never
   *         <code>null</code></u>
   * 
   * @since 1.0
   */
  @Nonnull
  public ServerInstance getLastServerInstance() {
    return lastServerInstance;
  }

  @IBackend
  @SubscribeEvent
  public void onWorldChange(WorldEvent.Load event) {
    if (hypixelUtils.onHypixel()) {
      new WhereamiWrapper(callback, hypixelUtils);
    }
  }

  /**
   * Always receive chat events and always checks for the /whereami response.
   * 
   * @param event
   *          the event, which contains the /whereami response
   */
  @IBackend
  @SubscribeEvent(receiveCanceled = true) // in case other mod is using it and cancels it
  public void onPlayerChat(ClientChatReceivedEvent event) {
    if (event == null || event.type != 0 || !hypixelUtils.onHypixel()) {
      return;
    }
    final String message = event.message.getUnformattedText();

    if (ServerType.getServerTypePattern().matcher(message).find()) {
      runCallbacks(ServerInstance.createInstance(message, Minecraft.getMinecraft(), hypixelUtils));
    }
  }

  private void runCallbacks(@Nonnull ServerInstance serverInstance) {
    if (!this.lastServerInstance.equals(serverInstance)) {
      this.lastServerInstance = serverInstance;

      if (hypixelUtils.isDefaultInstance()) {
        hypixelUtils.threadPool.submit(() -> {
          MinecraftForge.EVENT_BUS.post(new ServerInstanceEvent(serverInstance));
        });
      }

      for (ServerInstanceCallback listener : getListeners()) {
        hypixelUtils.threadPool.submit(() -> {
          listener.onServerInstanceUpdate(serverInstance);
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
  @IHypixelUtils
  @IOnlyHypixel
  @IInterfaceEvent
  @FunctionalInterface
  public interface ServerInstanceCallback extends EventListener {
    /**
     * callback on a separate thread when listener is triggered.
     * 
     * @param si
     *          an instance which contains all the parsed information.
     * 
     * @since 1.0
     */
    void onServerInstanceUpdate(ServerInstance si);
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
  @IHypixelUtils
  @IOnlyHypixel
  @IForgeEvent
  public static class ServerInstanceEvent extends Event {
    private final ServerInstance serverInstance;

    private ServerInstanceEvent(@Nonnull ServerInstance st) {
      this.serverInstance = st;
    }

    /**
     * get the saved server instance.
     * 
     * @return get the saved server instance.
     */
    @Nonnull
    public ServerInstance getServerInstance() {
      return serverInstance;
    }
  }
}
