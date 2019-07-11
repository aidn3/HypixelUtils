
package com.aidn5.hypixelutils.v1.common;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;

/**
 * <p>
 * My little boy :)
 * </p>
 * 
 * This helps me monitoring the usage of my own programs. It gives me statistics
 * like how many people do use the program.
 * <p>
 * <b>By the statistics, I can know:</b>
 * 
 * <ul>
 * <li>Whether I should continue developing/maintaining new features (when the
 * program is used by many people)</li>
 * 
 * <li>How to divide my time and focus on the programs.</li>
 * 
 * <p>
 * <b>Usage:</b> <code>registerMod("examplemod", "1.0")</code>
 * 
 * @author aidn5
 * @version 1.2
 * @see #registerMod(String, String)
 * 
 * @see "http://opensource.org/licenses/gpl-3.0.html GNU Public License"
 */
// About privacy: There is no third party in the process.
public final class StatisticMod {
  private static final String serverHostName = "http://statistics.aidn5.epizy.com/?";

  /**
   * After creating the object, wait (n) milliseconds before starting
   * 
   * @see #runEvery
   */
  private static final long delayAtStartup = TimeUnit.SECONDS.toMillis(5);
  /**
   * Check every (n) milliseconds, whether it's the time to inform the server
   * about the statistic
   * 
   * @see #delayAtStartup
   */
  private static final long runEvery = TimeUnit.SECONDS.toMillis(60);

  /**
   * If the informing process succeed with no errors (on client and server side).
   * use this time as to how far should the schedule be to the next run
   * 
   * @see #onFailRepeatAfter
   */
  private static final long onSuccessRepeatAfter = TimeUnit.MINUTES.toMillis(55);
  /**
   * If there is a failure in the process of informing the server (client or
   * server side error), make the next schedule using this time
   * 
   * @see #onSuccessRepeatAfter
   */
  private static final long onFailRepeatAfter = TimeUnit.SECONDS.toMillis(30);

  /**
   * The pool where the repeated schedule saved
   * 
   * @see #fetcher
   */
  private final Timer pool = new Timer(true);
  /**
   * extended {@link TimerTask} serves as a holder to the server-informing task.
   * Registered in {@link #pool} by {@link #StatisticMod(String, String)}
   * 
   * @see #pool
   */
  private final StatisticUpdater fetcher = new StatisticUpdater();

  /**
   * The MOD's id. It is sent to the server to know which MOD is refereed to
   */
  private final String modid;
  /**
   * The MOD's version. It is sent to the server to know which version of the MOD
   * is used the most.
   */
  private final String modVersion;
  /**
   * Minecraft's version. Every minecraft's version has it's own codebase and
   * logic (so the MOD). It's easier to only maintain these minecraft versions
   * instead of all of them.
   */
  private final String mcVersion;

  /**
   * To avoid duplicated requests. Username/UUID is used as a unique ID.
   */
  private final String username;
  /**
   * To avoid duplicated requests. Username/UUID is used as a unique ID.
   */
  private final String uuid;

  /**
   * When should the next schedule run (Unix timestamp)
   * 
   * @see StatisticUpdater
   */
  private long nextRun = -1;

  /**
   * Register a new instance and start tracking the MOD
   * 
   * @param modid
   *          MOD's id
   * @param modVersion
   *          MOD's version
   */
  public static void registerMod(final String modid, final String modVersion) {
    new StatisticMod(modid, modVersion);
  }

  /**
   * sign parameter to its finals, get settings from {@link Minecraft} instance
   * and start daemon thread
   * 
   * @param modid
   * @param modVersion
   */
  private StatisticMod(final String modid, final String modVersion) {
    this.modid = modid;
    this.modVersion = modVersion;

    this.mcVersion = Minecraft.getMinecraft().getVersion();
    this.username = Minecraft.getMinecraft().thePlayer.getName();
    this.uuid = Minecraft.getMinecraft().thePlayer.getUniqueID().toString();

    this.pool.schedule(this.fetcher, delayAtStartup, runEvery);
  }

  /**
   * Encode the data stack them together to be used by Web protocol
   * 
   * @return the encoded data to be send by HTTP protocol
   */
  private String getPayLoad() {
    StringBuilder payload = new StringBuilder(200);

    payload.append("username=").append(this.username);
    payload.append("&uuid=").append(this.uuid);

    payload.append("&appId=").append(this.modid);
    payload.append("&appVersion=").append(this.modVersion);

    payload.append("&vmVersion=").append(this.mcVersion);

    System.out.println("payload: " + payload.toString());
    return payload.toString();
  }

  /**
   * extended {@link TimerTask} serves as a holder to the server-informing task.
   * Registered in {@link #pool} by {@link #StatisticMod(String, String)}
   * 
   * @author aidn5
   * @see #fetcher
   */
  private final class StatisticUpdater extends TimerTask {

    // synchronized is used to avoid running the code twice at the same time.
    @Override
    public synchronized void run() {
      if (nextRun > System.currentTimeMillis()) return;

      InputStream in = null;

      try {
        URL url = new URL(serverHostName + getPayLoad());
        in = url.openStream();

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) != -1) {
          result.write(buffer, 0, length);
        }

        String response = result.toString("UTF-8");
        if (response.toLowerCase().contains("true")) {
          nextRun = System.currentTimeMillis() + onSuccessRepeatAfter;
        } else {
          nextRun = System.currentTimeMillis() + onFailRepeatAfter;
        }
      } catch (Throwable e) {
        nextRun = System.currentTimeMillis() + onFailRepeatAfter;

      } finally {
        try {
          if (in != null) in.close();
        } catch (Exception ignored) {}
      }
    }
  }
}
