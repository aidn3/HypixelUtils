
package com.aidn5.testpackage;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.eventslistener.OnHypixelListener;
import com.aidn5.hypixelutils.v1.eventslistener.OnHypixelListener.VerificationMethod;
import com.aidn5.hypixelutils.v1.eventslistener.ServerInstanceListener.ServiceInstanceEvent;
import com.aidn5.hypixelutils.v1.server.GameMode;
import com.aidn5.hypixelutils.v1.server.LobbyType;
import com.aidn5.hypixelutils.v1.server.ServerInstance;

import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = TestPackage.MOD_ID, name = TestPackage.MOD_NAME, version = TestPackage.VERSION, clientSideOnly = true)
public class TestPackage {
  public static final String MOD_ID = "testpackage";
  public static final String MOD_NAME = "Test Package";
  public static final String VERSION = "1.0";

  public static final HypixelUtils hypixelUtils = HypixelUtils.defaultInstance();

  @EventHandler
  public static void init(FMLInitializationEvent event) {
    ClientCommandHandler.instance.registerCommand(new Command());

    hypixelUtils.getOnHypixelListener().register(
        (boolean onHypixel, String ip, VerificationMethod method) -> {
          String message = onHypixel ? "hi hypixel server" : "bye hypixel 7u7";
          System.out.println(onHypixel + " onHypixel");

          hypixelUtils.messageBuffer.offer(new ChatComponentText(message));
        });

    hypixelUtils.getServerInstanceListener().register((st) -> {
      String text = st.getServerType().name() + ":" + st.getServerId() + ":" + st.isLimbo()
          + ", " + st.getLobbyType().name() + ":" + st.getLobbyNumber() + ":" + st.isLobby()
          + ", " + st.getGameMode().name() + ":" + st.isGame();

      System.out.println(text);
      hypixelUtils.messageBuffer.offer(new ChatComponentText(text));
    });

    hypixelUtils.getServerInstanceListener().register((st) -> {
      if (st.isGame() && st.getGameMode().equals(GameMode.BED_WARS)) {
        // do something in a bedwars game
      }

      if (st.isLobby() && st.getLobbyType().equals(LobbyType.MAIN_LOBBY)) {
        // player is in the main lobby :)
      }

      if (st.isLimbo()) {
        // Being on limbo makes you feel AFK :)
      }
    });
  }

  @SubscribeEvent
  public void serverChange(ServiceInstanceEvent event) {
    ServerInstance st = event.getServerInstance();

    if (st.isLimbo()) {
      // do something in limbo.
    }
  }

  @SubscribeEvent
  public void onHypixel(OnHypixelListener.OnHypixelEvent event) {
    boolean onHypixel = event.isOnHypixel();
  }
}
