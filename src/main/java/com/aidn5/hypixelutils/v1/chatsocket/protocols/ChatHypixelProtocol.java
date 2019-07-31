
package com.aidn5.hypixelutils.v1.chatsocket.protocols;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IOnlyHypixel;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Protocol specialized in sending and receiving packets on the hypixel network.
 * 
 * @author aidn5
 *
 * @since 1.0
 */
@IHypixelUtils
@IBackend
@IOnlyHypixel
public class ChatHypixelProtocol extends BaseProtocol {
  // e.g. "From [MVP+] Spitsy: &HUCSv1c:whar@"?Asd.+"
  // group(1) = "Spitsy", group(3) = "c", group(2) = "whar@"?Asd.+"
  private static Pattern chatFromP = Pattern
      .compile("^From (?:\\[.{2,30}\\] |)(.{3,99}): &HUCSv1(s|c):(.{1,9999})$");
  private static Pattern chatToP = Pattern
      .compile("^To (?:\\[.{2,30}\\] |)(.{3,99}): &HUCSv1(s|c):(.{1,9999})$");

  @SuppressWarnings("unused")
  private ChatHypixelProtocol() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onChat(ClientChatReceivedEvent event) {
    if (event.type != 0) {
      return;
    }

    final String message = event.message.getUnformattedText();

    Matcher m = chatFromP.matcher(message);
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
      return;
    }

    if (chatToP.matcher(message).find()) {
      event.setCanceled(true);
    }
  }

  @Override
  protected void sendPacket(String user, String startIndicator, byte[] packet) {
    Minecraft.getMinecraft().thePlayer
        .sendChatMessage("/msg " + user + " " + startIndicator + packetToString(packet));
  }

  @Override
  protected boolean isProtocolActive() {
    return HypixelUtils.defaultInstance().onHypixel();
  }
}
