
package com.aidn5.hypixelutils.v1;

import com.aidn5.hypixelutils.v1.chatsocket.ChatSocketFactory;
import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.exceptions.HypixelUtilsInternalError;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = "hypixelutils", name = "HypixelUtils", version = "1.0", clientSideOnly = true)
@IHypixelUtils
@IBackend
public class HypixelUtilsMod {
  /**
   * Initialize the library by using the EventHandler to call it.
   * 
   * @param event
   *          the event which is given by forge.
   */
  @EventHandler
  public static void postInitForgeEvent(FMLPostInitializationEvent event) {
    HypixelUtils ht = HypixelUtils.defaultInstance();
    if (!ht.isDefaultInstance()) {
      throw new HypixelUtilsInternalError(
          "HypixelUtils#defaultInstance() should have returned a default instance");
    }

    ChatSocketFactory.class.getName();
  }
}
