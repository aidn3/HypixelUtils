
package com.aidn5.hypixelutils.v1.common;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * My little boy :)
 * 
 * </p>
 * This helps me monitoring the usage of my own programs. It gives me statistics
 * like how many people are using the program.
 * 
 * <p>
 * <b>By the statistics, I can know:</b>
 * <ul>
 * <li>Whether I should continue developing/maintaining new features (when the
 * program is used by many people)</li>
 * 
 * <li>How to divide my time and focus on the programs, which are used by the
 * most.</li>
 * </ul>
 * 
 * 
 * <p>
 * <b>Data which is sent: </b>
 * <ul>
 * <li><u><b>appId</b></u>: The plugin/mod/program's id. It is sent to the
 * server to know which program is refereed to</li>
 * 
 * <li><u><b>appVersion</b></u>: The plugin/mod/program's version. It is sent to
 * the server to know which version is used the most.</li>
 * 
 * <li><u><b>parentAppId</b></u>: (in case a library) The third party
 * application id, which is using the developed program/library. It is used to
 * know whether the library is being used by other developers. It is also used
 * with userId.</li>
 * 
 * <li><u><b>vmName</b></u>: The machine/server/client's name that is running
 * it. Every platform has it's own codebase and logic (so the program). It's
 * easier to only maintain these platforms (which are used by the most) instead
 * of all of them.</li>
 * 
 * <li><u><b>vmVersion</b></u>: Machine/Server/Client's version.
 * same as <u>vmName</u>! Every version has its own logic and codebase too!</li>
 * 
 * <li><u><b>userId</b></u>: An unique ID is used to avoid duplicated requests.
 * No worries, I can't monitor you just by knowing an
 * anonymous-UUID/user-UUID.</li>
 * 
 * <li><u><b>tags</b></u>: tags separated by ";" without spaces in-between
 * describes the program. Used to sort and filter the statistics.
 * e.g. "minecraft;client;mod"</li>
 * 
 * <li><u><b>osName</b></u>: the operating system's name. It is the same as
 * <u>vmName</u> and <u>vmVersion</u>.</li>
 * </ul>
 * 
 * <p>
 * <b>Usage example: </b>
 * 
 * <pre>
 * <code>#registerMcServerPlugin("awesomePlugin", "1.0", "Bukkit", "1.12.2", null)</code>
 * </pre>
 * 
 * <p>
 * <b>For developers of this class:</b>
 * <ul>
 * <li>
 * <b>This class must not require any external library.</b>
 * It must be a stand-alone-class, available and ready to use without
 * any further requirements (like including external libraries
 * or needing Initialization) under any java platform.</li>
 * 
 * <li><b>only packages from java.** are allowed to use.</b>
 * Packages like javax.** must NOT be used, since they are not always
 * fully available on all platforms (like on android platform).</li>
 * 
 * <li><b>All the non-constant data must be supplied by the user.</b>
 * that means when adding a method to register x type of programs,
 * the method must NOT get the data by itself from external packages
 * (like with with reflection to avoid importing the packages).
 * Instead the method must have parameters to get the data.</li>
 * 
 * <li><b>This class should not be altered to fit the needs of every
 * program.</b>
 * Extending the class and overriding and adding methods is opted,
 * if the program has special needs.
 * If a new method is required to register new type/generation of programs,
 * a new version with all the required methods
 * and further methods for future programs will be added and released.</li>
 * </ul>
 * 
 * 
 * @author aidn5
 * @version 1.4
 * 
 * @see #registerMcClientLibrary(String, String, String, String, String, UUID)
 * @see #registerMcClientMod(String, String, String, String, UUID)
 * @see #registerMcServerPlugin(String, String, String, String, UUID)
 * 
 * @license http://opensource.org/licenses/gpl-3.0.html GNU Public License
 */
// TODO: fix logger
// all logs are marked red except FINE level.
public class StatisticsBase {
  private static final Logger logger = Logger.getLogger(StatisticsBase.class.getName());

  private static final String serverHostName = "http://statistics.aidn5.epizy.com/?";

  /**
   * The pool where the repeated schedule saved.
   * 
   * @see #fetcher
   */
  private static final Timer threadPool = new Timer(true);

  /**
   * After creating the object, wait (n) milliseconds before starting.
   * 
   * @see #runEvery
   */
  private static final long delayAtStartup = TimeUnit.SECONDS.toMillis(5);
  /**
   * Check every (n) milliseconds, whether it's the time to inform the server
   * about the statistic.
   * 
   * @see #delayAtStartup
   */
  private static final long runEvery = TimeUnit.SECONDS.toMillis(60);

  /**
   * If the informing process succeed with no errors (on client and server side).
   * 
   * <p>
   * use this time as to how far should the schedule be to the next run
   * 
   * @see #onFailRepeatAfter
   */
  private static final long onSuccessRepeatAfter = TimeUnit.MINUTES.toMillis(55);
  /**
   * If there is a failure in the process of informing the server (client or
   * server side error), make the next schedule using this time.
   * 
   * @see #onSuccessRepeatAfter
   */
  private static final long onFailRepeatAfter = TimeUnit.SECONDS.toMillis(30);


  /**
   * extended {@link TimerTask} serves as a holder to the server-informing task.
   * 
   * <p>
   * Registered in {@link #pool} by {@link #StatisticMod(String, String)}
   * 
   * @see #threadPool
   */
  private final StatisticUpdater fetcher = new StatisticUpdater();
  /**
   * When should the next schedule run (Unix timestamp).
   * 
   * @see StatisticUpdater
   */
  private long nextRun = -1;

  /**
   * The encoded data to send to the server.
   */
  private final String payload;
  /**
   * the app id to monitor. Used to log data.
   */
  private final String appId;

  /**
   * Register an unknown type of program and start monitoring it.
   * 
   * @param appId
   *          the program id
   * @param appVersion
   *          the program version
   * @param parentAppId
   *          (optional) The application which uses this program.
   * @param vmName
   *          the machine/server/client's name that is running it
   * @param vmVersion
   *          the version of the machine
   * @param userId
   *          An unique ID is used to avoid duplicated requests.
   *          can be <code>null</code>
   * @param tags
   *          tag to later sort and filter the statistics
   * @param osName
   *          the operating system's name.
   *          getOsName() will be used if <code>null</code> is provided.
   *
   * @return
   *         an instance which can be used to control the monitoring.
   * 
   * @see StatisticsBase
   */
  public static StatisticsBase registerUnknownType(String appId, String appVersion,
      String parentAppId, String vmName, String vmVersion, UUID userId,
      String tags, String osName) {

    return new StatisticsBase(
        appId, appVersion, parentAppId,
        vmName, vmVersion, userId,
        tags, osName);
  }

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

  /**
   * create a payload from the provided data and save it to be used by
   * Web-protocol and then start daemon thread.
   * 
   * @param appId
   *          the program id
   * @param appVersion
   *          the program version
   * @param parentAppId
   *          (optional) The application which uses this program.
   *          empty string will be used if <code>null</code> is given.
   * @param vmName
   *          the machine/server/client's name that is running it
   * @param vmVersion
   *          the version of the machine
   * @param userId
   *          An unique ID is used to avoid duplicated requests.
   *          UUID.randomUUID() will be used if <code>null</code> is given.
   * @param tags
   *          tag to later sort and filter the statistics
   *          if <code>null</code> is given. "unknown" will be used.
   * @param osName
   *          the operating system's name.
   *          getOsName() will be used if <code>null</code> is provided.
   *
   * @see StatisticsBase
   */
  protected StatisticsBase(String appId, String appVersion, String parentAppId,
      String vmName, String vmVersion, UUID userId, String tags, String osName) {
    logger.setLevel(Level.FINEST);
    logger.info(appId + ": new program will be registered");

    if (appId == null || appId.isEmpty()) {
      throwNullPointer(appId, "appId");
    }
    if (appVersion == null || appVersion.isEmpty()) {
      throwNullPointer(appId, "appVersion");
    }
    if (parentAppId == null) {
      parentAppId = "";
    }
    if (vmName == null || vmName.isEmpty()) {
      throwNullPointer(appId, "vmName");
    }
    if (vmVersion == null || vmVersion.isEmpty()) {
      throwNullPointer(appId, "vmVersion");
    }
    if (userId == null) {
      System.out.println("SAD");
      logger.fine("userId is null. Random id will be used instead.");
      userId = UUID.randomUUID();
    }
    if (tags == null || tags.isEmpty()) {
      logger.fine("tags is null or empty. 'unknown' will be used instead.");
      tags = "unknown";
    }
    if (osName == null) {
      logger.fine("osName is null or empty."
          + " the supplied system operating's name by java will be used instead.");
      osName = getOsName();
    }

    this.appId = appId;
    this.payload = compilePayload(
        "appId", appId,
        "appVersion", appVersion,
        "parentAppId", parentAppId,
        "vmName", vmName,
        "vmVersion", vmVersion,
        "userId", userId.toString(),
        "tags", tags,
        "osName", osName);

    logger.config("the current compiled payload for " + appId + " is " + this.payload);

    threadPool.schedule(this.fetcher, delayAtStartup, runEvery);
  }

  private static String compilePayload(String... payloads) throws IllegalArgumentException {
    if (payloads.length == 0) {
      return "";

    } else if (payloads.length % 2 != 0) {
      throw new IllegalArgumentException("the payload must be based on key-value");
    }

    StringBuilder compiledPayload = new StringBuilder(payloads.length * 10);

    for (int i = 0; i < payloads.length; i += 2) {
      String key = payloads[i];
      String value = payloads[i++];
      try {
        compiledPayload
            .append(URLEncoder.encode(key, "UTF-8"))
            .append("=")
            .append(URLEncoder.encode(value, "UTF-8"))
            .append("&");

      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        throw new RuntimeException("encoding utf-8 is not supported?!");
      }
    }
    // remove the last "&"
    compiledPayload.deleteCharAt(compiledPayload.length() - 1);

    return compiledPayload.toString();
  }

  protected static String getOsName() {
    return System.getProperty("os.name");
  }

  protected static void throwNullPointer(String id, String parameter) {
    logger.warning(id + ": " + parameter + " must never be empty or null"
        + "cancelling the regiseration and throwing NullPointerException.");

    throw new NullPointerException(parameter + " must not be null or empty");
  }

  /**
   * Send the statistics without effecting the internal daemon.
   * 
   * @return True if the statistics has been accepted.
   */
  public boolean sendStatistics() {
    return sendStatistic(new String[0]);
  }

  /**
   * Send statistics without effecting the internal daemon with extra parameters.
   * 
   * @param extraPayloads
   *          extra parameters to send. must be based on key-value.
   * 
   * @return
   *         True if the statistics has been accepted.
   */
  public boolean sendStatistic(String... extraPayloads) {
    InputStream in = null;

    try {
      URL url = new URL(serverHostName + payload + "&" + compilePayload(extraPayloads));
      in = url.openStream();

      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = in.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }

      String response = result.toString("UTF-8");
      if (response.toLowerCase().contains("true")) {
        return true;
      }
    } catch (Throwable e) {
      logger.throwing(
          this.getClass().getName(), "sendStatistic",
          new Throwable("could not send the statistics due to an I/O error", e));

    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return false;
  }

  /**
   * extended {@link TimerTask} serves as a holder to the server-informing task.
   * 
   * <p>
   * Registered in {@link StatisticsBase#threadPool} by
   * {@link StatisticsBase#StatisticsBase(String, String)}
   * 
   * @author aidn5
   * @see StatisticsBase#fetcher
   */
  private final class StatisticUpdater extends TimerTask {

    // synchronized is used to avoid running the code twice at the same time.
    @Override
    public synchronized void run() {
      if (nextRun > System.currentTimeMillis()) {
        return;
      }

      logger.fine(appId + ": sending the next scheduled statistics...");

      if (sendStatistics()) {
        logger.fine(appId + ": the statistics has been accepted");
        nextRun = System.currentTimeMillis() + onSuccessRepeatAfter;

      } else {
        logger.fine(appId + ": the statistics has failed to deliver.");
        nextRun = System.currentTimeMillis() + onFailRepeatAfter;
      }
    }
  }
}
