
package com.aidn5.hypixelutils.v1.tools.buffer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.IChatComponent;

/**
 * Buffer for chat messages sent to the
 * client. Will send any messages in the
 * buffer to the player as soon as possible.
 * 
 * @category BackendUtils
 * @author robere2
 * @since 1.0
 * @version 1.0
 */
@IHypixelUtils
@IBackend
public class MessageBuffer extends AbNewBuffer<IChatComponent> {

  /**
   * Constructor
   *
   * @param capacity
   *          how many elements maximum can the buffer hold. See
   *          {@link ArrayBlockingQueue#ArrayBlockingQueue(int)}
   * @param sleepTime
   *          Time in milliseconds between {@link #run()} calls. See
   *          {@link #sleepTime}
   * @param threadPool
   *          the pool to use when starting the buffer thread
   */
  public MessageBuffer(int capacity, int sleepTime, ExecutorService threadPool) {
    super(capacity, sleepTime, threadPool);
  }

  /**
   * send messages in the buffer
   */
  @Override
  protected void next(IChatComponent element) {
    EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
    player.addChatMessage(element);
  }

}
