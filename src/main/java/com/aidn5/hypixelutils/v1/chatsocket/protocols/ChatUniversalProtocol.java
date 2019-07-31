
package com.aidn5.hypixelutils.v1.chatsocket.protocols;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.exceptions.HypixelUtilsInternalError;
import com.aidn5.hypixelutils.v1.tools.AssetHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/**
 * Chat protocol used as the last resort to send and receive packets. This
 * protocol is always active. It contains many famous chat patterns.
 * 
 * <p>
 * It is also unstable and can produce unexpected results. {@link BaseProtocol}
 * will filter any non-valid packet.
 * 
 * @author aidn5
 * 
 * @since 1.0
 */
@IHypixelUtils
@IBackend
public class ChatUniversalProtocol extends BaseProtocol {
  // TODO: create the universal patterns. Change it to list
  // Join some servers and start /msg'ing people to get more patterns samples

  private static final List<Pattern> fromList = new ArrayList<>();
  private static final List<Pattern> toList = new ArrayList<>();
  static {
    final String path = "asset/ChatSocket/ChatUniversalPatterns.json";

    try {
      String json = AssetHelper.getString(ChatUniversalProtocol.class, path);
      JsonArray jsonArr = new JsonParser().parse(json).getAsJsonArray();


      for (int i = 0; i < jsonArr.size(); i++) {
        JsonObject group = jsonArr.get(i).getAsJsonObject();

        JsonElement fromP = group.get("from");
        if (fromP != null) {
          fromList.add(Pattern.compile(fromP.getAsString()));
        }
        JsonElement toP = group.get("to");
        if (toP != null) {
          toList.add(Pattern.compile(toP.getAsString()));
        }
      }
    } catch (Exception e) {
      throw new HypixelUtilsInternalError("Can not load the data from " + path);
    }
  }

  private boolean isActive = false;

  @SuppressWarnings("unused")
  private ChatUniversalProtocol() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  // @SubscribeEvent
  public void onChat(ClientChatReceivedEvent event) {
    if (event.type != 0) {
      return;
    }

    final String message = event.message.getUnformattedText();

    for (Pattern pattern : fromList) {

      Matcher m = pattern.matcher(message);
      if (m.find()) {
        String user = m.group(1);
        String isServer = m.group(2); // one letter: "c" or "s"
        String packet = m.group(3);

        try {
          receivePacket(user, isServer.contains("s"), ByteBuffer.wrap(stringToPacket(packet)));
        } catch (Exception e) {
          e.printStackTrace();
        }

        event.setCanceled(true);
      }
    }

    // remove all outgoing packet messages from the chat
    for (Pattern pattern : toList) {
      Matcher m = pattern.matcher(message);
      if (m.find()) {
        event.setCanceled(true);
      }
    }
  }

  @Override
  protected void sendPacket(String user, String startIndicator, byte[] packet) {
    Minecraft.getMinecraft().thePlayer
        .sendChatMessage("/msg " + user + " " + startIndicator + packetToString(packet));
  }

  @SubscribeEvent
  public void onLoggedOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
    isActive = false;
  }

  @SubscribeEvent
  public void onLoggedIn(FMLNetworkEvent.ClientConnectedToServerEvent event) {
    isActive = true;
  }

  @Override
  protected boolean isProtocolActive() {
    return isActive;
  }

}
