
package com.aidn5.hypixelutils.v1.common.analytics;

import java.util.UUID;

public class McStatistics extends StatisticsBase {

  /**
   * Register a Minecraft-Mod and start monitoring it.
   * 
   * @param modId
   *          the id of the mod to monitor.
   * @param modVersion
   *          the version of the mod.
   * @param mcName
   *          minecraft's name. e.g. Vanilla, Forge, etc.
   * @param mcVersion
   *          minecraft's version. e.g. 1.12.2, 1.14.2, 1.8
   * @param playerUuid
   *          a unique id to use.
   *          can be <code>null</code>
   * 
   * @return
   *         an instance which can be used to control the monitoring.
   * 
   * @see StatisticsBase
   */
  public static StatisticsBase registerMcClientMod(String modId, String modVersion,
      String mcName, String mcVersion, UUID playerUuid) {

    return new StatisticsBase(
        modId, modVersion, null,
        mcName.toLowerCase(), mcVersion,
        playerUuid, "minecraft;client;mod", getOsName());
  }

  /**
   * Register a Minecraft-library and start monitoring it.
   * 
   * @param libraryId
   *          the library's id to start monitor it.
   * @param libraryVersion
   *          the library's version.
   * @param parentMod
   *          (optional) the third party program which is using the library.
   *          can be <code>null</code>.
   * @param mcName
   *          minecraft name. e.g. Vanilla, Forge, etc.
   * @param mcVersion
   *          minecraft version. e.g. 1.12.2, 1.14.2, 1.8.9
   * @param playerUuid
   *          a unique id to avoid duplications.
   *          can be <code>null</code>
   * 
   * @return
   *         an instance which can be used to control the monitoring.
   * 
   * @see StatisticsBase
   */
  public static StatisticsBase registerMcClientLibrary(String libraryId, String libraryVersion,
      String parentMod, String mcName, String mcVersion, UUID playerUuid) {

    return new StatisticsBase(
        libraryId, libraryVersion, parentMod,
        mcName, mcVersion,
        playerUuid, "minecraft;client;library", getOsName());
  }

  /**
   * register a Minecraft Server-Plugin to start monitoring it.
   * 
   * @param pluginId
   *          the plugin's id to start monitor.
   * @param pluginVersion
   *          the plugin's version.
   * @param serverType
   *          e.g. Bukkit, Paper, Forge, Vanilla
   * @param serverVerion
   *          e.g. 1.12.2, 1.14.2, 1.8
   * @param uniqueId
   *          a unique id to avoid duplications.
   *          can be <code>null</code>
   * 
   * @return
   *         an instance which can be used to control the monitoring.
   * 
   * @see StatisticsBase
   */
  public static StatisticsBase registerMcServerPlugin(String pluginId, String pluginVersion,
      String serverType, String serverVerion, UUID uniqueId) {

    return new StatisticsBase(
        pluginId, pluginVersion, null,
        serverType.toLowerCase(), serverVerion,
        uniqueId, "minecraft;server;plugin", getOsName());
  }

  /**
   * register a Minecraft Server-library to start monitoring it.
   * 
   * @param libraryId
   *          the library's id to start monitor it.
   * @param libraryVersion
   *          the library's version.
   * @param parentPlugin
   *          (optional) the third party program which is using the library.
   *          can be <code>null</code>.
   * @param serverType
   *          e.g. Bukkit, Paper, Forge, Vanilla
   * @param serverVerion
   *          e.g. 1.12.2, 1.14.2, 1.8
   * @param uniqueId
   *          a unique id to avoid duplications.
   *          can be <code>null</code>
   * 
   * @return
   *         an instance which can be used to control the monitoring.
   * 
   * @see StatisticsBase
   */
  public static StatisticsBase registerMcServerLibrary(String libraryId, String libraryVersion,
      String parentPlugin, String serverType, String serverVerion, UUID uniqueId) {

    return new StatisticsBase(
        libraryId, libraryVersion, parentPlugin,
        serverType.toLowerCase(), serverVerion,
        uniqueId, "minecraft;server;library", getOsName());
  }

  protected McStatistics(String appId, String appVersion, String parentAppId, String vmName,
      String vmVersion, UUID userId, String tags, String osName) {
    super(appId, appVersion, parentAppId, vmName, vmVersion, userId, tags, osName);
  }

}
