
package com.aidn5.hypixelutils.v1.tools.buffer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/**
 * Buffer for chat messages sent AS the client.
 * Will send all messages as the client as soon
 * as possible. Buffer is cleared when the player
 * disconnects from a server.
 */
public class ChatBuffer extends AbNewBuffer<String> {

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
  public ChatBuffer(int capacity, int sleepTime, ExecutorService threadPool) {
    super(capacity, sleepTime, threadPool);
  }

  @Override
  public AbNewBuffer<String> start() {
    MinecraftForge.EVENT_BUS.register(this);
    return super.start();
  }

  @Override
  public AbNewBuffer<String> stop() {
    MinecraftForge.EVENT_BUS.unregister(this);
    return super.stop();
  }

  @SubscribeEvent
  public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
    clear();
  }

  /**
   * Start sending messages in the buffer
   */
  @Override
  protected void next(String message) {
    final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
    // Only send a message if the player exists & there is a message to send

    // Handle as a command
    if (message.startsWith("/")
        && ClientCommandHandler.instance.executeCommand(player, message) == 0)
      player.sendChatMessage(message);
  }
}
