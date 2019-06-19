
package com.aidn5.hypixelutils.v1.common;

import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;

public class CommandQueuer {
  public static final Pattern helloPattern = Pattern.compile("^Why hello there");

  public static void sendHelloCommand() {
    System.out.println("CommandQueuer.sendHelloCommand()");
    Minecraft.getMinecraft().thePlayer.sendChatMessage("/hello");
  }
}
