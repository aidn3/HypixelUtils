
package com.aidn5.hypixelutils.v1.chatsocket.protocols;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;


/**
 * Protocols works on the minecraft vanilla chat private chat style and system.
 * 
 * <p>
 * This protocol is inactive as a starter. After the first private sent or
 * received message, the protocol will detect it and determine that it can work
 * on this server. Usually other protocols (like {@link ChatUniversalProtocol})
 * will send the first packet on these servers. The protocol will be active
 * till disconnecting from the server.
 * 
 * @author aidn5
 *
 * @since 1.0
 */
@IHypixelUtils
@IBackend
public class ChatVanillaProtocol extends BaseProtocol {
  // {username} whispers to you: &HUCPv1:{packet}
  private static Pattern messageFromP = Pattern
      .compile("^([a-zA-Z_][a-zA-Z0-9_]{2,15}) whispers to you: &HUCSv1(s|c):(.{1,9999})$");

  // You whisper to {username}: &HUCPv1:{packet}
  private static Pattern messageToP = Pattern
      .compile("^You whisper to ([a-zA-Z_][a-zA-Z0-9_]{2,15}): &HUCSv1(s|c)(.{1,9999})$");

  private boolean isActive = false;

  @SuppressWarnings("unused")
  private ChatVanillaProtocol() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onChat(ClientChatReceivedEvent event) {
    if (event.type != 0) {
      return;
    }

    final String message = event.message.getUnformattedText();

    Matcher m = messageFromP.matcher(message);
    if (m.find()) {
      isActive = true;

      String user = m.group(1);
      String isServer = m.group(2); // one letter: "c" or "s"
      String packet = m.group(3);

      try {
        receivePacket(user, isServer.contains("s"), ByteBuffer.wrap(stringToPacket(packet)));
      } catch (Exception e) {
        e.printStackTrace();
      }

      event.setCanceled(true);
      return;
    }

    if (messageToP.matcher(message).find()) {
      event.setCanceled(true);
      isActive = true;
    }
  }

  @SubscribeEvent
  public void onLoggedOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
    isActive = false;
  }

  @Override
  protected void sendPacket(String user, String startIndicator, byte[] packet) {
    Minecraft.getMinecraft().thePlayer
        .sendChatMessage("/msg " + user + " " + startIndicator + packetToString(packet));
  }

  @Override
  protected boolean isProtocolActive() {
    return isActive;
  }

}
