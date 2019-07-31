
package com.aidn5.hypixelutils.v1;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.chatsocket.ChatSocketFactory;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestReceiveEvent.RequestReceived;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestSendEvent;
import com.aidn5.hypixelutils.v1.chatsocket.protocols.BaseProtocol;
import com.aidn5.hypixelutils.v1.chatsocket.wrapper.RequestWrapper;
import com.aidn5.hypixelutils.v1.chatwrapper.MapWrapper;
import com.aidn5.hypixelutils.v1.chatwrapper.WhereamiWrapper;
import com.aidn5.hypixelutils.v1.common.ChatWrapper;
import com.aidn5.hypixelutils.v1.common.EventListener;
import com.aidn5.hypixelutils.v1.common.ListenerBus;
import com.aidn5.hypixelutils.v1.common.analytics.McStatistics;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IOnlyHypixel;
import com.aidn5.hypixelutils.v1.eventslistener.HypixelApiListener;
import com.aidn5.hypixelutils.v1.eventslistener.OnHypixelListener;
import com.aidn5.hypixelutils.v1.eventslistener.ServerInstanceListener;
import com.aidn5.hypixelutils.v1.exceptions.HypixelUtilsInternalError;
import com.aidn5.hypixelutils.v1.exceptions.NotOnHypixelNetwork;
import com.aidn5.hypixelutils.v1.players.NotValidUsername;
import com.aidn5.hypixelutils.v1.players.Player;
import com.aidn5.hypixelutils.v1.players.UsernameCache;
import com.aidn5.hypixelutils.v1.serverinstance.ServerInstance;
import com.aidn5.hypixelutils.v1.tools.AssetHelper;
import com.aidn5.hypixelutils.v1.tools.ReflectionUtil;
import com.aidn5.hypixelutils.v1.tools.Scoreboard;
import com.aidn5.hypixelutils.v1.tools.TickDelay;
import com.aidn5.hypixelutils.v1.tools.TimeOut;
import com.aidn5.hypixelutils.v1.tools.buffer.ChatBuffer;
import com.aidn5.hypixelutils.v1.tools.buffer.MessageBuffer;
import com.aidn5.hypixelutils.v1.tools.cache.DbCacher;
import com.aidn5.hypixelutils.v1.tools.cache.ICacher;
import com.aidn5.hypixelutils.v1.tools.cache.JsonCacher;
import com.aidn5.hypixelutils.v1.tools.io.buffer.ByteBufferNetInput;
import com.aidn5.hypixelutils.v1.tools.io.buffer.ByteBufferNetOutput;
import com.aidn5.hypixelutils.v1.tools.io.stream.StreamNetInput;
import com.aidn5.hypixelutils.v1.tools.io.stream.StreamNetOutput;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This library contains most of used the classes/wrappers/APIs in mods for
 * hypixel network.
 * 
 * <p>
 * <i><u>Most</u> of the library features/services are provided by THIS class
 * and accessible either by static method like
 * {@link #createTickDelay(Runnable)} or from
 * {@link #defaultInstance()}/{@link #newInstance(Minecraft, String)}</i>
 * 
 * <p>
 * <h3>Grand-Features</h3> (Big/good features)
 * <ul>
 * <li>{@link ServerInstance}</li>
 * <li>{@link ChatSocketFactory}</li>
 * </ul>
 * 
 * <p>
 * <h3>Buffer-tools/thread-Tools:</h3>
 * (Used to improve the speed of the code/readability and care about unnoticed
 * problems like sending too many commands to the hypixel network will result in
 * blocking them and not executing them)
 * <ul>
 * <li>{@link #threadPool}</li>
 * <li>{@link #chatBuffer}</li>
 * <li>{@link #messageBuffer}</li>
 * </ul>
 * 
 * 
 * <p>
 * <h3>ChatWrappers</h3> (Used to send command to listen to the chat and use the
 * callback when the message is found)<br>
 * <i>Note: Every Wrapper has its own inline callback interface.</i>
 * <ul>
 * <li>{@link MapWrapper}</li>
 * <li>{@link WhereamiWrapper}</li>
 * </ul>
 * 
 * 
 * <p>
 * <h3>Listeners/Events bus:</h3> (Used to give an easy way to listen to
 * specific changes/events which are usually used in many mods)<br>
 * <b>Notes:</b><br>
 * - All Callbacks from listeners are extended from {@link EventListener}<br>
 * - You can register to a listener with
 * {@link ListenerBus#register(EventListener)} <br>
 * - Listeners are <u>ONLY</u> accessed by {@link HypixelUtils#INSTANCE}. - All
 * listeners also have
 * {@link Event}, which can be used as forge events.
 * <ul>
 * <li>{@link OnHypixelListener}</li>
 * <li>{@link HypixelApiListener}</li>
 * <li>{@link ServerInstanceListener}</li>
 * </ul>
 * 
 * 
 * <p>
 * <h3>Help-Tools:</h3> (These tools are common to use in minecraft mods. In
 * hypixel network or somewhere else)<br>
 * <i>Note: These can either be accessed directly or through
 * {@link HypixelUtils}'s static methods</i>
 * <ul>
 * <li>{@link ChatSocketFactory}</li>
 * <li>{@link TickDelay}</li>
 * <li>{@link ReflectionUtil}</li>
 * <li>{@link UsernameCache}</li>
 * <li>{@link JsonCacher}</li>
 * <li>{@link DbCacher}</li>
 * <li>{@link Scoreboard}</li>
 * <li>{@link ChatWrapper}</li>
 * <li>{@link TimeOut}</li>
 * <li>{@link Player}</li>
 * </ul>
 * 
 * <p>
 * <h3>IO-Tools</h3> (These classes help with reading/writing/parsing
 * streams/buffers/etc.)
 * <ul>
 * <li>{@link AssetHelper}</li>
 * <li>{@link ByteBufferNetInput}</li>
 * <li>{@link ByteBufferNetOutput}</li>
 * <li>{@link StreamNetInput}</li>
 * <li>{@link StreamNetOutput}</li>
 * </ul>
 * 
 * 
 * <p>
 * <h3>Exceptions:</h3> (These exceptions are provided/used in the library.)
 * <ul>
 * <li>{@link NotOnHypixelNetwork}</li>
 * <li>{@link HypixelUtilsInternalError}</li>
 * <li>{@link NotValidUsername}</li>
 * </ul>
 * 
 * @author aidn5
 * 
 * @version 1.0
 * @since 1.0
 */
// TODO: create logger and use it to log
// TODO: add the licenses to the build along with README.md
// TODO: add package-info.java and javadoc to tools.**, players.**,
@IHypixelUtils
public final class HypixelUtils {
  private static final Logger LOGGER = Logger.getLogger(HypixelUtils.class.getName());

  /**
   * Name of the library to display.
   */
  public static final String NAME = "HypixelUtils";
  /**
   * the id of the library. used to identify the library in forge as a mod.
   */
  @Nonnull
  public static final String MODID = "hypixelutils";
  /**
   * The current version of the library.
   */
  @Nonnull
  public static final String VERSION = "1.0";
  /**
   * The authors of the library.
   */
  @Nonnull
  public static final String[] AUTHORS = new String[] { "aidn5" };
  /**
   * Credits for other people, who made this library possible.
   */
  @Nonnull
  public static final String CREDITS = "robere2";

  /**
   * the id of the mod, which created this instance. if <code>null</code> then
   * this instance is the
   * default shared instance.
   * 
   * <p>
   * used to create private settings and files for each individual mod.
   */
  @Nullable
  public final String modidForInstance;

  /**
   * Instance of minecraft.
   */
  @Nonnull
  public final Minecraft mc;

  /**
   * Thread pool for blocking code.
   * 
   * @since 1.0
   * 
   * @see Executors#newCachedThreadPool()
   */
  @Nonnull
  public static final ExecutorService threadPool = Executors.newCachedThreadPool();

  /**
   * a Provided instance of the library used to register listeners, push elements
   * to the buffer.
   */
  // idea make a shared instance between all the mods.
  // so all mods work together
  // Benefits: reduce the amount of events
  // if 5 mods are using the mod at the same time and all of them want to receive
  // an event when changing the world by sending a command, the command will
  // probably be sent at least 5 times at the same time,
  // which will result in a blocking from the server side
  // with the message "Please don't spam the command!"
  @Nonnull
  private static final HypixelUtils INSTANCE = new HypixelUtils(Minecraft.getMinecraft(), null);

  /**
   * Buffer for sending chat messages to the server.
   * 
   * @since 1.0
   * 
   * @see Queue#offer(Object)
   */
  @Nonnull
  public final ChatBuffer chatBuffer = new ChatBuffer(5000, 100, threadPool);
  /**
   * Buffer for sending messages to the client.
   * 
   * @since 1.0
   * 
   * @see Queue#offer(Object)
   */
  @Nonnull
  public final MessageBuffer messageBuffer = new MessageBuffer(5000, 100, threadPool);

  /**
   * Class checks whether the client is connected to the hypixel network.
   * 
   * @since 1.0
   * 
   * @category ListenerBus
   * 
   * @see #onHypixel()
   * @see OnHypixelListener#onHypixel()
   * @see ListenerBus
   */
  @Nonnull
  @IOnlyHypixel
  public final OnHypixelListener onHypixelListener;

  /**
   * Reads chat and detects when a new Hypixel's API is generated.
   *
   * @category ListenerBus
   * @category chatwrapper
   * 
   * @see HypixelApiListener#getHypixelApi()
   * @see ListenerBus
   */
  @Nonnull
  public final HypixelApiListener hypixelApiListener;

  /**
   * Class checks to what server is the client is connected to every time the
   * world changes.
   * 
   * @since 1.0
   *
   * @category ListenerBus
   * @category ChatReader
   * 
   * @see ListenerBus
   */
  @Nonnull
  public final ServerInstanceListener serverInstanceListener;

  /**
   * get the instance of the current username-cacher with save-to-disk feature
   * enabled by default.
   * 
   * @see HypixelUtils#newUsernameCache(File)
   */
  @Nonnull
  public final UsernameCache usernameCache;

  /**
   * get a shared-default instance between all mods.
   * 
   * @return a shared instance.
   * 
   * @see #newInstance(Minecraft, String)
   */
  @Nonnull
  public static HypixelUtils defaultInstance() {
    return INSTANCE;
  }

  /**
   * create new private instance for a mod.
   * 
   * @param modid
   *          the mod id to create the private instance for
   * @return the private instance for the mod
   * 
   * @throws IllegalArgumentException
   *           if modid is <code>null</code> or empty
   * 
   * @see #defaultInstance()
   */
  @Nonnull
  public static HypixelUtils newInstance(@Nonnull Minecraft mc, @Nonnull String modid)
      throws IllegalArgumentException {

    if (modid == null || modid.isEmpty()) {
      throw new IllegalArgumentException("modid can not be empty or null");
    }

    LOGGER.info("new instance for mod '" + modid + "' is been created");
    return new HypixelUtils(Minecraft.getMinecraft(), modid);

  }

  private HypixelUtils(@Nonnull Minecraft mc, @Nullable String modid) {
    try {
      this.mc = Objects.requireNonNull(mc);
      this.modidForInstance = modid;


      // create instances of the services
      String usernameCachePath = mc.mcDataDir + "/mod/"
          + (isDefaultInstance() ? "" : modidForInstance + "/")
          + "hypixelutils/" + VERSION + "/usernamecache.json";
      usernameCache = new UsernameCache(new File(usernameCachePath));


      // create instances of the listeners
      // note: these listeners MUST be created in this sequence!
      // they are depended on each other, otherwise NullPointerException
      onHypixelListener = reflectionNewInstance(OnHypixelListener.class, this);
      hypixelApiListener = reflectionNewInstance(HypixelApiListener.class, this);
      serverInstanceListener = reflectionNewInstance(ServerInstanceListener.class, this);


      // register the listeners to start receive events
      // to let them save data and make the results on-demand
      MinecraftForge.EVENT_BUS.register(onHypixelListener);
      MinecraftForge.EVENT_BUS.register(hypixelApiListener);
      MinecraftForge.EVENT_BUS.register(serverInstanceListener);


      // start any service which needs a stand-alone thread
      chatBuffer.start();
      messageBuffer.start();


      // start tracking the usage of this library.
      McStatistics.registerMcClientLibrary(
          MODID, VERSION, modid,
          "Forge", mc.getVersion(),
          mc.getSession().getProfile().getId());

    } catch (Throwable e) {
      throw new HypixelUtilsInternalError(e);
    }
  }

  /**
   * whether this instance is the shared instance.
   * 
   * @return true if this instance is the default shared instance.
   * 
   * @see #modidForInstance
   */
  public boolean isDefaultInstance() {
    return (this.modidForInstance == null || this.modidForInstance.isEmpty());
  }

  /**
   * Whether the client is currently connected to the Hypixel network.
   * 
   * <p>
   * returns the result of
   * <code>HypixelUtils.INSTANCE.onHypixelListener.onHypixel()</code>
   * 
   * @return <code>true</code> if the client is online hypixel network.
   *         <code>false</code> if not
   * 
   * @since 1.0
   * 
   * @category EventListener
   * 
   * @see #onHypixelListener
   * @see OnHypixelListener#onHypixel()
   */
  public boolean onHypixel() {
    return onHypixelListener.onHypixel();
  }

  /**
   * get the registered/saved/newly detected API from the chat.
   * <p>
   * returns the result of
   * <code>HypixelUtils.INSTANCE.hypixelApiListener.getHypixelApi()</code>
   * 
   * @return last detected API. <code>null</code> if not detected yet
   * 
   * @since 1.0
   * 
   * @category ChatReader
   * 
   * @see #hypixelApiListener
   * @see HypixelApiListener
   * @see HypixelApiListener#getHypixelApi()
   */
  @Nullable
  public UUID getHypixelApi() {
    return hypixelApiListener.getHypixelApi();
  }

  /**
   * get the saved server instance since the last time is requested (every time
   * when the world
   * changes).
   * 
   * @return the serverInstance (mostly up-to-date). <u>Never
   *         <code>null</code></u>
   * 
   * @since 1.0
   * 
   * @category EventListener
   * @category ChatReader
   * 
   * @see #serverInstanceListener
   * @see ServerInstanceListener
   * @see ServerInstanceListener#getLastServerInstance()
   */
  @Nonnull
  public ServerInstance getLastServerInstance() {
    return serverInstanceListener.getLastServerInstance();
  }

  /**
   * delay code by a certain number of game ticks.
   * 
   * <p>
   * Default 20 ticks when unprovided
   * 
   * @param codeToRun
   *          Code to be delayed
   * 
   * @since 1.0
   * 
   * @category Utils
   * 
   * @see TickDelay#TickDelay(Runnable)
   */
  @Nonnull
  public static TickDelay createTickDelay(@Nonnull Runnable codeToRun) {
    return new TickDelay(codeToRun);
  }

  /**
   * delay code by a certain number of game ticks.
   * 
   * @param codeToRun
   *          Code to be delayed
   * @param ticks
   *          How many ticks to delay it
   * 
   * @since 1.0
   * 
   * @category Utils
   * 
   * @see TickDelay#TickDelay(Runnable, int)
   */
  @Nonnull
  public static TickDelay createTickDelay(@Nonnull Runnable codeToRun, int ticks) {
    return new TickDelay(codeToRun, ticks);
  }

  /**
   * Check whether the username is a valid minecraft-username.
   * 
   * @param username
   *          the username to validate.
   * 
   * @return <code>true</code> if the username is valid.
   * 
   * @see Player
   * @see Player#isValidUsername(String)
   */
  public static boolean isValidUsername(@Nullable String username) {
    return Player.isValidUsername(username);
  }

  /**
   * Check whether the username is valid as a minecraft-username.
   * <p>
   * <b>Usage Example:</b>
   * <code>
   * void method(String username) throws IllegalArgumentException {
   *     this.username = validateUsername(username);
   * }
   * </code>
   * 
   * @param username
   *          the username to validate
   * 
   * @return
   *         returns {@code username} back.
   * 
   * @throws NotValidUsername
   *           if the username is not valid with {@link Exception#getMessage()}
   *           for the reason.
   * @see Player
   * @see Player#validateUsername(String)
   */
  @Nonnull
  public static String validateUsername(@Nullable String username) throws NotValidUsername {
    return Player.validateUsername(username);
  }

  /**
   * get file's content as {@link String} from the resources.
   * 
   * @param clazz
   *          any class inside the the same resource location. {@link ClassLoader}
   * @param path
   *          the resource path to the file to get.
   * 
   * @return File's contents as {@link String}
   * 
   * @throws IOException
   *           if an I/O error occurs
   * 
   * @since 1.0
   * 
   * @category AssetHelper
   * 
   * @see AssetHelper#getString(Class, String)
   */
  public static String assetGetString(@Nonnull Class<?> clazz, @Nonnull String path)
      throws IOException {
    return AssetHelper.getString(clazz, path);
  }

  /**
   * get file's content's bytes from the resources.
   * 
   * @param clazz
   *          any class inside the the same resource location. {@link ClassLoader}
   * @param path
   *          the resource path to the file to get.
   * 
   * @return File's content's bytes
   * 
   * @throws IOException
   *           if an I/O error occurs
   * 
   * @since 1.0
   * 
   * @category AssetHelper
   * 
   * @see AssetHelper#getByteArray(Class, String)
   */
  @Nonnull
  public static byte[] assestGetByteArray(@Nonnull Class<?> clazz, @Nonnull String path)
      throws IOException {
    return AssetHelper.getByteArray(clazz, path);
  }

  /**
   * Create new instance of a class from its private constructor.
   * 
   * @param clazz
   *          the class to create new instance of it
   * 
   * @return the new instance of the given class
   * 
   * @throws ReflectiveOperationException
   *           if any reflection error occurs
   * 
   * @since 1.0
   * 
   * @category ReflectionUtil
   * 
   * @see ReflectionUtil#newInstance(Class)
   */
  @Nonnull
  public static <T> T reflectionNewInstance(@Nonnull Class<T> clazz)
      throws ReflectiveOperationException {
    return ReflectionUtil.newInstance(clazz);
  }

  /**
   * Create new instance of a class from its private constructor.
   * 
   * @param clazz
   *          the class to create new instance of
   * 
   * @param parameters
   *          the given parameters to the class
   * 
   * @return a new instance of the given class
   * 
   *         ReflectiveOperationException
   *         if any reflection error occurs
   * 
   * @since 1.0
   * 
   * @see ReflectionUtil#newInstance(Class, Object...)
   */
  public static <T> T reflectionNewInstance(@Nonnull Class<T> clazz, @Nonnull Object... parameters)
      throws ReflectiveOperationException {
    return ReflectionUtil.newInstance(clazz, parameters);
  }

  /**
   * Get the current Minecraft version.
   * 
   * @return String containing the Minecraft version
   * 
   * @throws NoSuchFieldException
   *           Minecraft version couldn't be found for some reason
   * @throws IllegalAccessException
   *           Couldn't access the minecraft version for some reason
   * 
   * @since 1.0
   * 
   * @category ReflectionUtil
   * 
   * @see ReflectionUtil#getMcVersion()
   */
  @Nonnull
  public static String getMcVersion() throws NoSuchFieldException, IllegalAccessException {
    return ReflectionUtil.getMcVersion();
  }

  /**
   * Get the current Forge version.
   * 
   * @return String containing the Forge version
   * 
   * @throws InvocationTargetException
   *           Exception caused by getVersion method
   * @throws IllegalAccessException
   *           Couldn't access the Forge version for some reason
   * @throws NoSuchMethodException
   *           Forge version method doesn't exist for some reason
   * 
   * @since 1.0
   * 
   * @category ReflectionUtil
   * 
   * @see ReflectionUtil#getForgeVersion()
   */
  @Nonnull
  public static String getForgeVersion()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    return ReflectionUtil.getForgeVersion();
  }

  /**
   * Get the title of the current server from the scoreboard.
   * 
   * <p>
   * Every server on hypixel shows its type in the first field on the scoreboard.
   * <br>
   * <i>Examples: "SKYWARS", "BEDWARS", "ARCADE GAMES"</i>
   * 
   * @param mc
   *          Minecraft instance.
   * @param hypixelUtils
   *          instance of the library to check whether the client is connected to
   *          hypixel
   *          network.
   * 
   * @return the title on hypixel network
   * 
   * @throws NotOnHypixelNetwork
   *           if hypixelUtils is not <code>null</code> and the client is not
   *           connected to hypixel network
   * 
   * @since 1.0
   * 
   * @category Scoreboard
   * 
   * @see Scoreboard#gameServerTitle(Minecraft, HypixelUtils)
   * @see Scoreboard#getSidebarScores(Minecraft)
   */
  @IOnlyHypixel
  @Nonnull
  public static String gameServerTitle(@Nonnull Minecraft mc, @Nullable HypixelUtils hypixelUtils)
      throws NotOnHypixelNetwork {
    return Scoreboard.gameServerTitle(mc, hypixelUtils);
  }

  /**
   * create new instance of {@link JsonCacher}.
   * 
   * <p>
   * if <code>cacheFilePath</code> is null, {@link ICacher#saveCache()} and
   * {@link ICacher#loadCache()} will be disabled.
   * 
   * @param cacheFilePath
   *          the path where the cached saved.
   * @param duration
   *          the length of time after an entry is created that it should be
   *          removed
   * @param durationUnit
   *          the unit that {@code duration} is expressed in
   * 
   * @see JsonCacher
   */
  public static <T> JsonCacher<T> newJsonCacher(@Nullable File cacheFilePath, int duration,
      TimeUnit durationUnit) {
    return new JsonCacher<T>(cacheFilePath, duration, durationUnit);
  }

  /**
   * Create a new instance with cacher.
   * 
   * @param cacheFilePath
   *          the file where cache should be saved. can be <code>null</code>
   * 
   * @return new instance with cacher.
   * 
   * @see UsernameCache
   * @see #usernameCache
   */
  public static UsernameCache newUsernameCache(@Nullable File cacheFilePath) {
    return new UsernameCache(cacheFilePath);
  }

  /**
   * schedule the GUI to be displayed on the screen.
   * The GUI will be displayed after one tick.
   * 
   * <p>
   * Useful to use after processing command,
   * which is triggered by the chat-command.
   * 
   * @param gui
   *          the GUI to display
   * 
   * @see #createTickDelay(Runnable, int)
   * @see TickDelay
   * @see Minecraft#displayGuiScreen(GuiScreen)
   */
  public static void displayGui(GuiScreen gui) {
    createTickDelay(() -> {
      Minecraft.getMinecraft().displayGuiScreen(gui);
    }, 1);
  }

  /**
   * Register a listener to start receiving connection requests.
   * 
   * @param modid
   *          the id of the listener to be called to.
   *          Must be between 3 and 16 length.
   * @param callback
   *          a callback to notify when a new connection is incoming.
   * 
   * @throws IllegalArgumentException
   *           if {@code modid} is null, shorter than 3 or longer than 16,
   *           or {@code callback} is <code>null</code>.
   * 
   * @see ChatSocketFactory#registerListener(String, RequestReceived)
   * @see ChatSocketFactory
   */
  public static void ChatSocketRegisterListener(@Nonnull String modid,
      @Nonnull RequestReceived callback) throws IllegalArgumentException {
    ChatSocketFactory.registerListener(modid, callback);
  }

  /**
   * Remove a listener and stop receiving incoming connections.
   * 
   * @param modid
   *          the id to stop listening to.
   * @return
   *         the registered callback to this id,
   *         or <code>null</code>
   * 
   * @see ChatSocketFactory#unregisterListener(String)
   * @see ChatSocketFactory
   */
  @Nonnull
  public static RequestReceived ChatSocketUnregisterListener(@Nonnull String modid) {
    return ChatSocketFactory.unregisterListener(modid);
  }

  /**
   * Register new protocol for other servers. See {@link BaseProtocol} for further
   * information about the Protocols specifications.
   * 
   * 
   * @param protocolClass
   *          protocol class to register.
   * @throws RuntimeException
   *           if an exception is encountered while registering the protocol.
   * 
   * @see ChatSocketFactory#registerProtocol(Class)
   * @see ChatSocketFactory
   */
  public static void ChatSocketRegisterProtocol(
      @Nonnull Class<? extends BaseProtocol> protocolClass) throws RuntimeException {
    ChatSocketFactory.registerProtocol(protocolClass);
  }

  /**
   * Initiate new request connection to use.
   * 
   * @param modid
   *          the id of the listener to request when connecting.
   * @param actionId
   *          the action to do with the listener.
   * 
   * @return
   *         an instance, which can be used to send requests.
   * 
   * @see ChatSocketFactory#createRequest(String, String)
   * @see ChatSocketFactory
   */
  public static RequestSendEvent ChatSocketCreateRequest(@Nonnull String modid, String actionId) {
    return ChatSocketFactory.createRequest(modid, actionId);
  }

  /**
   * Create an instance and make it listen to the id. This instance has the
   * feature to register an individual listener to every action instead of
   * listening to all actions with the same id. It also notify the user for new
   * requests and provides a callback, when the user accept the request connection
   * through chat<i><u>. This will remove any current listener, which is
   * registered at this moment.</u></i>
   * 
   * @param modid
   *          the id of the requests to listen to.
   * @param displayChatName
   *          the name to display on the chat when a new request is incoming and
   *          approve is required from the player to connect.
   * 
   * @return
   *         a registered instance.
   * 
   * @see ChatSocketFactory#createWrapper(String, String)
   * @see ChatSocketFactory#registerListener(String, RequestReceived)
   * @see ChatSocketFactory
   */
  public static RequestWrapper ChatSocketCreateWrapper(@Nonnull String modid,
      @Nonnull String displayChatName) {
    return ChatSocketFactory.createWrapper(modid, displayChatName);
  }
}
