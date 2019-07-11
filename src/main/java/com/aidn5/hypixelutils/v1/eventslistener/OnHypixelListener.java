
package com.aidn5.hypixelutils.v1.eventslistener;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.common.EventListener;
import com.aidn5.hypixelutils.v1.common.ListenerBus;
import com.aidn5.hypixelutils.v1.eventslistener.OnHypixelListener.OnHypixelCallback;
import com.aidn5.hypixelutils.v1.tools.TickDelay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/**
 * Class checks whether the client is connected to the hypixel network.
 * 
 * <p>
 * It also provides {@link ListenerBus} to register listeners and callback
 * when the status {@link #onHypixel()} changes
 * 
 * @author aidn5
 * 
 * @version 1.0
 * @since 1.0
 * 
 * @category ListenerBus
 */
public final class OnHypixelListener extends ListenerBus<OnHypixelCallback> {
  @Nonnull
  private final HypixelUtils hypixelUtils;

  private boolean isOnlineHypixelNetwork = false;

  private OnHypixelListener(@Nonnull HypixelUtils hypixelUtils) {
    this.hypixelUtils = hypixelUtils;
  }

  /**
   * Whether the client is currently connected to the Hypixel network.
   * 
   * @return <code>true</code> if the client is online hypixel network.
   *         <code>false</code> if not
   * 
   * @since 1.0
   */
  public boolean onHypixel() {
    if (!isOnlineHypixelNetwork) {
      // fast check
      return checkIp(getCurrentIP());
    }

    return isOnlineHypixelNetwork;
  }

  /**
   * Called when a Minecraft world is loaded.
   * 
   * @param event
   *          Event data
   * @see net.minecraftforge.event.world.WorldEvent.Load
   */
  @SubscribeEvent
  public void onJoinWorld(WorldEvent.Load event) {
    final String ip = getCurrentIP();

    // check the server's ip
    if (checkIp(ip)) {
      runCallback(true, ip, VerificationMethod.IP);
      return;
    }

    // if the ip couldn't determine, then check the server's metadata

    // Wait one second for everything to load properly
    new TickDelay(() -> {
      // Check the server's metadata
      final VerificationMethod metadataVerification = checkServerMetadataForHypixel();
      if (metadataVerification != null) {
        runCallback(true, ip, metadataVerification);
      } else {
        runCallback(false, ip, null);
      }
    }, 20);
  }

  @SubscribeEvent
  public void onLoggedOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
    runCallback(false, null, null);
  }

  /**
   * check {@link #isOnlineHypixelNetwork} with onHypixel.
   * If the status changed, set the new value and
   * inform the registered listeners by the callbacks
   * 
   * @param onHypixel
   *          whether online Hypixel
   * @param ip
   *          IP the user is connected to
   * @param method
   *          the used method to determine that it's online hypixel
   */
  private void runCallback(boolean onHypixel, @Nullable String ip,
      @Nullable VerificationMethod method) {
    if (onHypixel != isOnlineHypixelNetwork) {
      isOnlineHypixelNetwork = onHypixel;

      if (hypixelUtils.isDefaultInstance()) {

        hypixelUtils.threadPool.submit(() -> {
          MinecraftForge.EVENT_BUS.post(new OnHypixelEvent(onHypixel, ip, method));
        });
      }

      for (OnHypixelCallback listener : getListeners()) {
        hypixelUtils.threadPool.submit(() -> {
          listener.onOnHypixelUpdate(onHypixel, ip, method);
        });
      }
    }
  }

  /**
   * Gets the current IP the client is connected to.
   * 
   * @return The IP the client is currently connected to
   * 
   * @since 1.0
   */
  @Nonnull
  public static String getCurrentIP() {
    String ip;
    if (Minecraft.getMinecraft().isSingleplayer()) {
      ip = "singleplayer";
    } else {
      ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
      ip = (serverData == null) ? "unknown/null" : serverData.serverIP;
    }

    return ip;
  }

  /**
   * check if the Server IP matches the hostname/ip of hypixel network.
   * 
   * @param ipToCheck
   *          the server's IP to check
   * 
   * @return <code>true</code> if the IP matches with the hypixel network.
   *         <code>false</code> if the current world is a singleplayer or doesn't
   *         match
   * 
   * @see #getCurrentIP()
   */
  protected static boolean checkIp(@Nonnull String ipToCheck) {
    if (ipToCheck.equals("singleplayer")) {
      return false;
    }

    Pattern hypixelPattern = Pattern.compile(
        "^(?:(?:(?:.*\\.)?hypixel\\.net)|(?:209\\.222\\.115\\.\\d{1,3}))(?::\\d{1,5})?$",
        Pattern.CASE_INSENSITIVE);
    Matcher matcher = hypixelPattern.matcher(ipToCheck);

    // If the current IP matches the regex above, then it's on hypixel network
    return matcher.find();
  }

  /**
   * Check if any of the server's metadata contains anything that might point to
   * this server being Hypixel.
   * If the tablist header contains "hypixel.net" then the server is verified.
   * If the tablist fooder contains "hypixel.net" then the server is verified.
   * If the server MOTD contains "hypixel network" then the server is verified.
   * If the server favicon base64 matches Hypixel's logo then the server is
   * verified.
   * 
   * @return Which of the above checks were true, or null otherwise.
   */
  protected static VerificationMethod checkServerMetadataForHypixel() {

    // First check tab list, if it contains any references to Hypixel in the header
    // & footer.
    final GuiPlayerTabOverlay tab = Minecraft.getMinecraft().ingameGUI.getTabList();
    try {
      if (checkTabField(tab, "header", "field_175256_i")) {
        return VerificationMethod.HEADER;
      }
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }

    try {
      if (checkTabField(tab, "footer", "field_175255_h")) {
        return VerificationMethod.FOOTER;
      }
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }

    // Next check server MOTD
    final ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
    if (serverData != null) {
      final String motd = serverData.serverMOTD;
      if (motd != null && motd.toLowerCase().contains("hypixel network")) {
        return VerificationMethod.MOTD;
      }

      try {
        // Next check server favicon
        final String faviconBase64 = serverData.getBase64EncodedIconData();
        if (faviconBase64 != null) {
          // TODO: get Hypixel server's favicon
          final String hypixelBase64 = Base64.encodeBase64String(
              IOUtils.toByteArray(
                  OnHypixelListener.class.getClassLoader()
                      .getResourceAsStream("HypixelMCLogo.png")));
          if (faviconBase64.equals(hypixelBase64)) {
            return VerificationMethod.FAVICON;
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Return null if none of these conditions matched
    return null;
  }

  /**
   * Checks whether the tab list header & footer contain information that would
   * point to the current server being Hypixel.
   * 
   * @param tabOverlay
   *          GUI overlay for the tab list
   * @param fieldName
   *          name of the field to check (either <code>header</code> or
   *          <code>footer</code>)
   * @param srgName
   *          field name in an obfuscated environment
   * @return Whether the field <code>fieldName</code> in the object
   *         <code>tabOverlay</code> contains "hypixel.net"
   * @throws NoSuchFieldException
   *           The field couldn't be found
   * @throws IllegalAccessException
   *           The field couldn't be accessed
   */
  protected static boolean checkTabField(@Nonnull GuiPlayerTabOverlay tabOverlay,
      @Nonnull String fieldName, @Nonnull String srgName)
      throws ReflectiveOperationException {
    Field headerField;
    try {
      // Try deobfuscated
      headerField = tabOverlay.getClass().getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      // Assume SRG otherwise
      headerField = tabOverlay.getClass().getDeclaredField(srgName);
    }

    if (headerField != null) {
      headerField.setAccessible(true);

      // OrangeMarshall's Vanilla Enhancements conflicts with this mod. He has a field
      // called
      // FieldWrapper which wraps IChatComponent, and you must call .get() on the
      // wrapper as well
      // i.e. instead of headerField.get(tabOverlay),
      // headerField.get(tabOverlay).get(tabOverlay).

      final Object headerObj = headerField.get(tabOverlay);
      IChatComponent component;
      // If user is using Vanilla Enhancements
      if (Loader.instance().getModList().stream()
          .anyMatch(mod -> mod.getName().equals("Vanilla Enhancements"))) {
        final String type = "com.orangemarshall.enhancements.util.FieldWrapper";
        final Class<?> clazz = Class.forName(type);

        // Cast to FieldWrapper, then get the method "get" taking one parameter Object
        // obj
        final Method fieldWrapperGetMethod = clazz.cast(headerObj).getClass()
            .getDeclaredMethod("get", Object.class);
        fieldWrapperGetMethod.setAccessible(true);

        // Execute the method on headerObj, passing tabOverlay as parameter obj
        component = (IChatComponent) fieldWrapperGetMethod.invoke(headerObj, tabOverlay);
      } else {
        component = (IChatComponent) headerObj;
      }

      return component != null && component.getUnformattedText() != null
          && component.getUnformattedText().toLowerCase().contains("hypixel.net");
    }

    return false;
  }

  /**
   * Callback for when an {@link OnHypixelListener} verifies the client's server.
   * 
   * @author aidn5
   * 
   * @version 1.0
   * @since 1.0
   * 
   * @category EventListener
   */
  @FunctionalInterface
  public interface OnHypixelCallback extends EventListener {

    /**
     * Called when a {@link OnHypixelListener} verifies a server.
     * 
     * @param onHypixel
     *          Whether the client is on Hypixel or not
     * @param ip
     *          The IP the client is on. If <code>null</code>,
     *          it means the client is not connected to any server
     * @param method
     *          The method used to verify that the client is on Hypixel, or null
     *          if not on hypixel
     * 
     * @since 1.0
     */
    void onOnHypixelUpdate(boolean onHypixel, @Nullable String ip,
        @Nullable VerificationMethod method);
  }

  /**
   * Enum for possible ways that the client was verified to be on Hypixel.
   */
  public enum VerificationMethod {
    /**
     * the server has been confirmed to by hypixel network by its IP.
     */
    IP,
    /**
     * the server has been confirmed to by hypixel network by the TAB overlay
     * (header).
     */
    HEADER,
    /**
     * the server has been confirmed to by hypixel network by the TAB overlay
     * (footer).
     */
    FOOTER,
    /**
     * the server has been confirmed to by hypixel network
     * by its daily server status message.
     */
    MOTD,
    /**
     * the server has been confirmed to by hypixel network
     * by its icon.
     */
    FAVICON
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
  public static class OnHypixelEvent extends Event {
    private final boolean onHypixel;
    @Nullable
    private final String ip;
    @Nullable
    private final VerificationMethod method;

    private OnHypixelEvent(
        boolean onHypixel, @Nullable String ip, @Nullable VerificationMethod method) {
      this.onHypixel = onHypixel;
      this.ip = ip;
      this.method = method;
    }

    /**
     * Whether the client is online Hypixel or not.
     * 
     * @return
     *         true if the client is online hypixel network.
     */
    public boolean isOnHypixel() {
      return onHypixel;
    }

    /**
     * The IP the client is on. If <code>null</code>,
     * it means the client is not connected to any server.
     * 
     * @return
     *         The IP the client is on or <code>null</code>.
     */
    @Nullable
    public String getIp() {
      return ip;
    }

    /**
     * The method used to verify that the client is on Hypixel, or null
     * if not on hypixel.
     * 
     * @return
     *         The method used to verify that the client is on Hypixel,
     *         or null if not on hypixel.
     */
    @Nullable
    public VerificationMethod getMethod() {
      return method;
    }
  }
}
