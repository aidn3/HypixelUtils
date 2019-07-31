
package com.aidn5.hypixelutils.v1.common;

import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHelpTools;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IOnlyHypixel;
import com.aidn5.hypixelutils.v1.tools.buffer.ChatBuffer;

import net.minecraft.client.Minecraft;

/**
 * Object helps interacting with the chat and command on hypixel network.
 * 
 * @author aidn5
 * 
 * @version 1.0
 * @since 1.0
 */
@IHelpTools(onlyStatic = true)
@IHypixelUtils
public class ChatWrapper {
  private ChatWrapper() {
    throw new AssertionError();
  }

  /**
   * Hello pattern command. used to indicate the end of the previous command.
   * 
   * <p>
   * used as an indicator because it has no effect.
   */
  @IOnlyHypixel
  public static final Pattern helloPattern = Pattern.compile("^Why hello there");

  /**
   * Lock used to lock a wrapper and wait till the other one is finished.
   * <p>
   * Complex wrappers which read multiple time at runtime may conflict each other.
   * This lock will force other threads to wait till the current wrapper
   * is finished reading from the chat and sending commands.
   * 
   * <p>
   * Example of a conflict: two wrapper send commands at the same time
   * and requesting two different lists, which both have the same pattern.
   */
  @IBackend
  public static final ReentrantLock chatLock = new ReentrantLock();

  /**
   * send /hello command to the server.
   * 
   * @param cb
   *          use the chat buffer to send the command.
   *          if <code>null</code> send it directly by
   *          <code>Minecraft.getMinecraft().thePlayer#sendChatMessage()</code>
   */
  @IOnlyHypixel
  public static void sendHelloCommand(@Nullable ChatBuffer cb) {
    if (cb != null) {
      cb.offer("/hello");
    } else {
      Minecraft.getMinecraft().thePlayer.sendChatMessage("/hello");
    }
  }
}
