package com.aidn5.hypixelutils.common;

import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;

public class CommandQueuer {
	public static final Pattern helloPattern = Pattern.compile("^Why hello there\\.");

	public static void sendCommand(String command) {
		System.out.println("CommandQueuer.sendCommand()");
		System.out.println(command);
		Minecraft.getMinecraft().thePlayer.sendChatMessage(command);
	}

	public static void sendHelloCommand() {
		System.out.println("CommandQueuer.sendHelloCommand()");
		Minecraft.getMinecraft().thePlayer.sendChatMessage("/hello");
	}
}
