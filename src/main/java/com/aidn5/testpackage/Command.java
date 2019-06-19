
package com.aidn5.testpackage;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class Command extends CommandBase {

  @Override
  public String getCommandName() {
    return "/hypeU";
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {
    return null;
  }

  @Override
  public void processCommand(final ICommandSender sender, String[] args) throws CommandException {
    if (args[0].toLowerCase().contains("ignore")) {
      new Thread(new Runnable() {

        @Override
        public void run() {
          sender.addChatMessage(new ChatComponentText("ignore start"));
          for (String username : TestPackage.hypixelUtils.getIgnoreListWrapper()) {
            sender.addChatMessage(new ChatComponentText("ignored: " + username));
          }
          sender.addChatMessage(new ChatComponentText("ignore end"));

        }
      }).start();
    } else if (args[0].toLowerCase().contains("on")) {
      sender
          .addChatMessage(new ChatComponentText("online: " + TestPackage.hypixelUtils.onHypixel()));
    } else if (args[0].toLowerCase().contains("api")) {
      sender.addChatMessage(
          new ChatComponentText(
              "api: " + TestPackage.hypixelUtils.getHypixelApiListener().getHypixelApi()));
    } else {
      sender.addChatMessage(new ChatComponentText("args: on, api, ignore"));
    }
  }

  @Override
  public int getRequiredPermissionLevel() {
    return 0;
  }

}
