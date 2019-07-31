
package com.aidn5.hypixelutils.v1.chatsocket.wrapper;

import static net.minecraft.util.EnumChatFormatting.BOLD;
import static net.minecraft.util.EnumChatFormatting.RESET;
import static net.minecraft.util.EnumChatFormatting.WHITE;
import static net.minecraft.util.EnumChatFormatting.YELLOW;

import com.aidn5.hypixelutils.v1.chatsocket.wrapper.RequestWrapper.PendingRequest;
import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

@IHypixelUtils
@IBackend
public class ChatSocketCommandWrapper extends CommandBase {
  static final String logo = BOLD + "" + YELLOW + "["
      + BOLD + "" + WHITE + "%s"
      + BOLD + "" + YELLOW + "] " + RESET;

  @Override
  public void processCommand(ICommandSender sender, String[] args) throws CommandException {
    if (args.length == 3) {
      final String displayChatName = args[0];
      final boolean accept = args[1].contains("accept") ? true : false;
      final int connectionId = Integer.valueOf(args[2]);

      final String logo = String.format(ChatSocketCommandWrapper.logo, displayChatName);
      final PendingRequest pr = RequestWrapper.pendingRequests.get(connectionId);
      final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

      if (pr == null) {
        player.addChatComponentMessage(
            new ChatComponentText(logo + EnumChatFormatting.RED + "no such request found."));
        return;

      }

      RequestWrapper.pendingRequests.remove(connectionId);

      if (!pr.requestReceiveEvent.canSend()) {
        player.addChatComponentMessage(
            new ChatComponentText(logo + EnumChatFormatting.RED + "connection timed out."));
        return;
      }

      new Thread(() -> {
        if (accept) {
          player.addChatComponentMessage(
              new ChatComponentText(logo + EnumChatFormatting.GREEN + "accepting..."));

          pr.listener.get(pr.requestReceiveEvent.acceptConnection());

        } else {
          player.addChatComponentMessage(new ChatComponentText(logo + "rejecting..."));
          pr.requestReceiveEvent.declineConnection();
        }
      }).start();
    }
  }

  @Override
  public String getCommandName() {
    return "/hucp";
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {
    return null;
  }

  @Override
  public int getRequiredPermissionLevel() {
    return 0;
  }
}
