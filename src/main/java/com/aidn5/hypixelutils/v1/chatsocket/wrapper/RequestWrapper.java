
package com.aidn5.hypixelutils.v1.chatsocket.wrapper;

import static net.minecraft.util.EnumChatFormatting.BOLD;
import static net.minecraft.util.EnumChatFormatting.GREEN;
import static net.minecraft.util.EnumChatFormatting.ITALIC;
import static net.minecraft.util.EnumChatFormatting.RED;
import static net.minecraft.util.EnumChatFormatting.RESET;
import static net.minecraft.util.EnumChatFormatting.WHITE;
import static net.minecraft.util.EnumChatFormatting.YELLOW;

import java.util.HashMap;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.chatsocket.ChatSocketFactory;
import com.aidn5.hypixelutils.v1.chatsocket.client.Connection;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestReceiveEvent;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestReceiveEvent.RequestReceived;
import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;

/**
 * Wrapper used to help managing the incoming connections. A an instance and
 * listens to the id on {@link ChatSocketFactory}. This class has the feature to
 * register an individual listener to every action instead of listening to all
 * actions with the same id. It also notify the user for new requests and
 * provides a callback, when the user accept the request connection through
 * chat<i><u>. This will remove any current listener, which is registered at
 * this moment.</u></i>
 * 
 * <p>
 * For more control grasping, {@link ChatSocketFactory} is more preferable.
 * 
 * @author aidn5
 *
 * @since 1.0
 * 
 * @see ChatSocketFactory
 * @see ChatSocketFactory#registerListener(String, RequestReceived)
 */
@IHypixelUtils
public class RequestWrapper implements RequestReceived {
  @IBackend
  static final HashMap<Integer, PendingRequest> pendingRequests = new HashMap<>();
  static {
    ClientCommandHandler.instance.registerCommand(new ChatSocketCommandWrapper());
  }

  private final HashMap<String, IGetChatProtocol> listeners = new HashMap<>();

  @Nonnull
  public final String modid;
  @Nonnull
  public final String displayChatName;

  /**
   * Constructor.
   * 
   * @param modid
   *          the id of the requests to listen to.
   * @param displayChatName
   *          the name to display on the chat when a new request is incoming and
   *          approve is required from the player to connect.
   */
  public RequestWrapper(@Nonnull String modid, @Nonnull String displayChatName) {
    this.modid = modid;
    this.displayChatName = displayChatName.replace(" ", "").trim();
    ChatSocketFactory.registerListener(modid, this);
  }

  /**
   * Register new listener to receive the successful connections.
   * 
   * @param actionId
   *          the command to start listening to.
   * @param iAccept
   *          callback to retrieve the {@link Connection}.
   */
  public void registerListener(@Nonnull String actionId, @Nonnull IGetChatProtocol iAccept) {
    listeners.put(
        Objects.requireNonNull(actionId),
        Objects.requireNonNull(iAccept));
  }

  /**
   * Unregister a listener to stop receiving callbacks.
   * 
   * @param actionId
   *          the command to stop listening to.
   */
  public void unregisterListener(@Nonnull String actionId) {
    listeners.remove(actionId);
  }

  @IBackend
  @Override
  public void get(@Nonnull RequestReceiveEvent re) {
    final String actionId = re.getActionId();
    final IGetChatProtocol listener = listeners.get(actionId);

    if (!re.canSend()) {
      return;
    }

    boolean hasListener = (listener != null);
    if (hasListener) {
      PendingRequest pr = new PendingRequest();
      pr.listener = listener;
      pr.requestReceiveEvent = re;

      pendingRequests.put(re.getConnectionId(), pr);
    }

    sendNotifyMessage(re, hasListener);
  }

  private void sendNotifyMessage(RequestReceiveEvent re, boolean hasListener) {
    final String logo = String.format(ChatSocketCommandWrapper.logo, displayChatName);
    final String message = ITALIC + "" + YELLOW + re.getUser() + RESET
        + "" + WHITE + " is trying to connect to you!";

    final ChatComponentText wholeMsg = new ChatComponentText(logo + message);
    final ChatStyle cs = wholeMsg.getChatStyle();

    final ChatComponentText reason = new ChatComponentText("Their intends is: "
        + ITALIC + "" + YELLOW + re.getActionId() + RESET + ".\n\n");


    IChatComponent result;
    // no listener to handle the request
    if (!hasListener) {
      result = new ChatComponentText(RED + "Unfortunatly, I don't know how to handle it...");

    } else if (re.canSend()) {
      cs.setChatClickEvent(
          new ClickEvent(Action.RUN_COMMAND,
              "//hucp " + displayChatName + " accept " + re.getConnectionId()));

      ChatComponentText acceptC = new ChatComponentText(BOLD + "" + GREEN + "Accept");

      result = new ChatComponentText(WHITE + "Click this message to ")
          .appendSibling(acceptC);

    } else {
      result = new ChatComponentText(
          RED + "Unfortunatly, this request is invalid...");
    }

    reason.appendSibling(result);

    cs.setChatHoverEvent(
        new HoverEvent(net.minecraft.event.HoverEvent.Action.SHOW_TEXT, reason));


    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(wholeMsg);
  }

  static class PendingRequest {
    RequestReceiveEvent requestReceiveEvent;
    IGetChatProtocol listener;
  }

  @IHypixelUtils
  @FunctionalInterface
  public interface IGetChatProtocol {
    void get(@Nonnull Connection cp);
  }
}
