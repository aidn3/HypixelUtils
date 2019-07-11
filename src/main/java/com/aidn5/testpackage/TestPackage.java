
package com.aidn5.testpackage;

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.eventslistener.OnHypixelListener.OnHypixelCallback;
import com.aidn5.hypixelutils.v1.eventslistener.OnHypixelListener.VerificationMethod;
import com.aidn5.hypixelutils.v1.eventslistener.ServerInstanceListener.ServerInstanceCallback;
import com.aidn5.hypixelutils.v1.serverinstance.GameMode;
import com.aidn5.hypixelutils.v1.serverinstance.LobbyType;
import com.aidn5.hypixelutils.v1.serverinstance.ServerInstance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "testmod", name = "TestMod", version = "1.0", clientSideOnly = true)
public class TestPackage implements ServerInstanceCallback, OnHypixelCallback {
  public static final HypixelUtils hypixelUtils = HypixelUtils.defaultInstance();
  private ServerInstance lastSI;

  @EventHandler
  public void init(FMLInitializationEvent event) {
    ClientCommandHandler.instance.registerCommand(new Command());

    hypixelUtils.getOnHypixelListener().register(this);
    hypixelUtils.getServerInstanceListener().register(this);

    new Timer(true).scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        lastSI = ServerInstance.createDummyServerInstance();
      }
    }, 500, 10000);

    MinecraftForge.EVENT_BUS.register(this);
  }

  @Override
  public void onServerInstanceUpdate(ServerInstance si) {
    this.lastSI = si;
    if (si.isGame() && si.getGameMode().equals(GameMode.BED_WARS)) {
      // do something in a bedwars game
    }

    if (si.isLobby() && si.getLobbyType().equals(LobbyType.MAIN_LOBBY)) {
      // player is in the main lobby :)
    }

    if (si.isLimbo()) {
      // Being on limbo makes you feel AFK :)
    }
  }

  @Override
  public void onOnHypixelUpdate(boolean onHypixel, String ip, VerificationMethod method) {
    System.out.println(onHypixel + " onHypixel");
    if (onHypixel) {
      hypixelUtils.messageBuffer.offer(new ChatComponentText("hi hypixel network!"));
    } else {
      hypixelUtils.messageBuffer.offer(new ChatComponentText("bye hypixel :("));
    }
  }

  @SubscribeEvent
  public void onRenderOverlay(RenderGameOverlayEvent event) {
    if (hypixelUtils.onHypixel() || true) {
      if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen == null) {
          if (lastSI != null) {
            String place;
            String type;

            if (lastSI.isGame()) {
              place = "Game";
              type = lastSI.getGameMode().getDisplayGameName();
            } else if (lastSI.isLobby()) {
              place = "Lobby";
              type = lastSI.getLobbyType().getDisplayName();
            } else if (lastSI.isLimbo()) {
              place = "Limbo";
              type = "";
            } else {
              place = "Unknown";
              type = "N/A";
            }

            mc.ingameGUI.drawCenteredString(mc.fontRendererObj,
                lastSI.getServerType().name() + "." + lastSI.getServerId()
                    + " " + place + ":" + type,
                new ScaledResolution(mc).getScaledWidth() / 2, 10, Color.WHITE.getRGB());
          }
        }
      }
    }
  }
}
