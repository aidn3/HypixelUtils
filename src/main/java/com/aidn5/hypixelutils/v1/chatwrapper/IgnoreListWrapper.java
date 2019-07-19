
package com.aidn5.hypixelutils.v1.chatwrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.common.ChatWrapper;
import com.aidn5.hypixelutils.v1.common.annotation.IChatWrapper;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.exceptions.NotOnHypixelNetwork;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * an {@link Iterator} iterates over the usernames of the ignored players by the
 * current user. It uses {@link PageLoader}
 * to list the current ignored players by pages.
 * <p>
 * <b>Note:</b> This class fetches the next page when it's needed. It does not
 * fetch all the pages
 * at once.
 * <p>
 * <b>muster of the used command:</b> "/ignore list 1" to fetch the first page
 * of the list
 * <p>
 * Use {@link #getAll(HypixelUtils)} only when <u>you have to</u>. Use the
 * {@link Iterator} instead!
 * 
 * @author aidn5
 * 
 * @version 1.0
 * @since 1.0
 * 
 * @category ChatWrapper
 */
// some methods has been divided to smaller methods
// to reduce the length number of lines in one method
@IHypixelUtils(OnlyHypixel = true)
@IChatWrapper(usesLock = true)
public class IgnoreListWrapper implements Iterable<String>, Iterator<String> {
  @Nonnull
  private static final String COMMAND = "/ignore list %d";

  /**
   * detects when the user has ignored no one when "/ignore list" is sent.
   * 
   * @see #noIgnore
   */
  @Nonnull
  private static final Pattern noIgnorePattern = Pattern
      .compile("^You are not ignoring anyone\\.$");

  /**
   * detects when the ignore list is started to fetch. so, the chat will be turned
   * off to not spam
   * the chat with the fetched usernames and the {@link #usernamePattern} will be
   * used to fetch them.
   * <p>
   * {@link #patternStarted} is switched to <code>true</code>
   */
  @Nonnull
  private static final Pattern startPattern = Pattern
      .compile("^------ Ignored Users \\(Page ([0-9]{1,5}) of ([0-9]{1,5})\\) ------");
  /**
   * detects the fetched username from the chat.
   * <p>
   * <b>Muster string: </b>"4. aidn5"
   */
  @Nonnull
  private static final Pattern usernamePattern = Pattern
      .compile("^[0-9]{1,16}\\. (([A-Za-z0-9_]{1,32}))");

  /**
   * Array saves all the fetched usernames.
   * 
   * @see #currentUsernameIndex
   */
  @Nonnull
  private final List<String> ignoredUsernames = new ArrayList<String>();

  /**
   * Indicates the current page. used when the next page is needed.
   */
  private int currentPage = 0;

  /**
   * used with {@link #ignoredUsernames} to know where is the pointer at when
   * using {@link #next()}.
   */
  private int currentUsernameIndex = 0;

  /**
   * Instance of the library used to check onHypixel and to send chat buffers.
   */
  @Nonnull
  private final HypixelUtils hypixelUtils;

  /**
   * get a new instance ready to use.
   * 
   * @param hypixelUtils
   *          an instance of the library.
   * 
   * @throws NotOnHypixelNetwork
   *           if the client is not connected to the hypixel network
   * 
   * @since 1.0
   */
  @Nonnull
  public static IgnoreListWrapper newInstance(@Nonnull HypixelUtils hypixelUtils)
      throws NotOnHypixelNetwork {
    if (hypixelUtils.onHypixel()) {
      return new IgnoreListWrapper(hypixelUtils);
    }

    throw new NotOnHypixelNetwork();
  }

  /**
   * Fetches all the pages at once and return the results.
   * <p>
   * <b>Note:</b> This may takes a while depends on how many players did the
   * player /ignore.
   * Do <u>NOT</u> use it on the main thread.
   * 
   * @param hypixelUtils
   *          an instance of the library.
   * 
   * @return all the usernames of the ignored players
   * 
   * @throws NotOnHypixelNetwork
   *           if the client is not connected to the hypixel network
   * 
   * @deprecated It's marked as deprecated to only avoid the abusive usage of this
   *             method. It may be
   *             removed in the future though.
   * 
   * @since 1.0
   */
  @Deprecated
  @Nonnull
  public static List<String> getAll(HypixelUtils hypixelUtils) throws NotOnHypixelNetwork {
    // this returns the pointer to the private list
    // since this class is static and the instance of the IgnoreList is never
    // returned. doing this hack should not cause any problem.
    // this speed up the process by not cloning the list

    IgnoreListWrapper ig = IgnoreListWrapper.newInstance(hypixelUtils);
    // force loading the pages
    for (String username : ig) {}

    return ig.ignoredUsernames;
  }

  private IgnoreListWrapper(@Nonnull HypixelUtils hypixelUtils) {
    this.hypixelUtils = hypixelUtils;
  }

  /**
   * Check whether there is more cached ignored usernames. If not
   * {@link #fetchNextPage()} and check
   * again.
   * 
   * @throws NotOnHypixelNetwork
   *           if the client was not connected to the hypixel network
   * 
   * @since 1.0
   */
  @Override
  public boolean hasNext() throws NotOnHypixelNetwork {
    if (ignoredUsernames.size() <= currentUsernameIndex) {
      fetchNextPage();
    }
    return ignoredUsernames.size() > currentUsernameIndex;
  }

  /**
   * {@inheritDoc}
   * 
   * @throws IndexOutOfBoundsException
   *           if the required page is not loaded with {@link #hasNext()}
   * 
   * @since 1.0
   */
  @Override
  @Nonnull
  public String next() throws IndexOutOfBoundsException {
    return ignoredUsernames.get(currentUsernameIndex++);
  }

  /**
   * Fetch the next page and return.
   * 
   * @throws NotOnHypixelNetwork
   *           if the client was not connected to the hypixel network
   */
  private void fetchNextPage() throws NotOnHypixelNetwork {
    if (!hypixelUtils.onHypixel()) {
      throw new NotOnHypixelNetwork();
    }

    PageLoader pl = PageLoader.loadPage(hypixelUtils, ++currentPage);
    Set<String> newUsernames = pl.getUsernames();

    for (String newUsername : newUsernames) {
      if (!ignoredUsernames.contains(newUsername)) {
        ignoredUsernames.add(newUsername);
      }
    }
  }

  @Override
  @Nonnull
  public Iterator<String> iterator() {
    return this;
  }

  /**
   * Adapter helps reading from /ignore command.
   * 
   * <p>
   * <b>The steps are planned in the following way:</b>
   * <ul>
   * <li>register the {@link #eventsListener}</li>
   * <li>send the command to fetch the page</li>
   * <li>send the command, which indicates the end of the first command.</li>
   * <li>hold the thread current thread till the fetching is finished in
   * {@link EventsListener#onPlayerChatEvent(ClientChatReceivedEvent)}</li>
   * <li>unregister {@link #eventsListener}</li>
   * </ul>
   * 
   * @author aidn5
   * 
   * @version 1.0
   * @since 1.0
   * 
   * @category ChatWrapper
   */
  @IHypixelUtils(OnlyHypixel = true)
  @IChatWrapper(usesLock = true)
  public static class PageLoader {
    /**
     * Create {@link PageLoader},
     * lock the current thread, lock {@link ChatWrapper#chatLock},
     * send commands, start reading from the chat
     * and return when finished with all the locks unlocked.
     * 
     * @param hu
     *          instance to hypixelutils.
     * @param page
     *          the page to get from the ignore list
     * 
     * @return
     *         an object contains the /ignore'ed usernames
     *         and other metadata like total pages
     * 
     * @since 1.0
     */
    public static PageLoader loadPage(@Nonnull HypixelUtils hu, int page) {
      Objects.requireNonNull(hu);

      PageLoader pl = new PageLoader();

      // do not allow to let more than one
      // mod read from the chat at the same time
      ChatWrapper.chatLock.lock();
      try {
        pl.loading = true;
        MinecraftForge.EVENT_BUS.register(pl.eventsListener);

        hu.chatBuffer.offer(String.format(COMMAND, page));
        ChatWrapper.sendHelloCommand(hu.chatBuffer);

        while (pl.loading) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            return pl;
          }
        }
      } finally {
        try {
          pl.loading = false;
          MinecraftForge.EVENT_BUS.unregister(pl.eventsListener);
        } finally {
          ChatWrapper.chatLock.unlock();
        }
      }

      return pl;
    }

    private PageLoader() {
      // private constructor
    }

    /**
     * class holds all the methods, which are used by
     * {@link MinecraftForge#EVENT_BUS}.
     */
    @Nonnull
    private final EventsListener eventsListener = new EventsListener();

    private final Set<String> usernames = new HashSet<>(10);

    private boolean loading = false;
    private boolean patternStarted = false;

    private int currentPage = -1;
    private int totalPages = -1;
    private boolean noIgnore = false;


    public int getCurrentPage() {
      return currentPage;
    }

    public int getTotalPages() {
      return totalPages;
    }

    /**
     * the user has /ignore'ed no one.
     * 
     * @return
     *         true if the user has /ignore'ed no one.
     */
    public boolean isNoIgnore() {
      return noIgnore;
    }

    public Set<String> getUsernames() {
      return usernames;
    }

    // check whether this event/message indicates that the user has not ignored
    // anyone to event have an ignore-list to view.
    private boolean hasClientNotIgnored(String message) {
      if (noIgnorePattern.matcher(message).find()) {
        noIgnore = true;
        return true;
      }

      return false;
    }

    // check whether this event/message indicates the header of the ignore list.
    private boolean isIgnoreListStarted(String message) {
      Matcher startM = startPattern.matcher(message);
      if (startM.find()) {

        currentPage = Integer.parseInt(startM.group(1));
        totalPages = Integer.parseInt(startM.group(2));

        return true;
      }

      return false;
    }

    private boolean isNewUsername(String message) {
      // usernames listed in schema
      Matcher userM = usernamePattern.matcher(message);
      if (userM.find()) {

        String ignoredUsername = userM.group(1);
        usernames.add(ignoredUsername);

        return true;
      }
      return false;
    }

    // check whether this event/message indicates the end of the previous command.
    private boolean isEnded(String message) {
      if (ChatWrapper.helloPattern.matcher(message).find()) {
        return true;
      }

      return false;
    }

    @IHypixelUtils(OnlyHypixel = true)
    @IChatWrapper(usesLock = true)
    private final class EventsListener {
      /**
       * Receive chat events from {@link MinecraftForge#EVENT_BUS} and process them.
       * <p>
       * following way is how the chat is processed:
       * 
       * <ul>
       * 
       * <li>detects when the list is started to fetch with
       * {@link IgnoreListWrapper#startPattern} or
       * {@link IgnoreListWrapper#noIgnorePattern}</li>
       * 
       * <li>start to fetch the usernames with
       * {@link IgnoreListWrapper#usernamePattern} into
       * {@link PageLoader#usernames}</li>
       * 
       * <li>unregister itself when {@link ChatWrapper#helloPattern} is
       * detected</li>
       * </ul>
       * 
       * @param event
       *          the received chat event, which contains the chat-message
       */
      @SubscribeEvent(receiveCanceled = true)
      public void onPlayerChatEvent(ClientChatReceivedEvent event) {
        if (!loading) {
          MinecraftForge.EVENT_BUS.unregister(eventsListener);
          return;
        }

        if (event == null || event.type != 0) {
          return;
        }

        final String message = event.message.getUnformattedText();

        if (hasClientNotIgnored(message) || isIgnoreListStarted(message)) {
          if (event.isCancelable()) {
            event.setCanceled(true);
          }

          patternStarted = true;
          return;
        }

        if (patternStarted) {
          if (isNewUsername(message)) {
            if (event.isCancelable()) {
              event.setCanceled(true);
            }

          } else if (isEnded(message)) {

            if (event.isCancelable()) {
              event.setCanceled(true);
            }
            MinecraftForge.EVENT_BUS.unregister(eventsListener);

            patternStarted = false;
            loading = false;
          }
        }
      }
    }
  }
}
