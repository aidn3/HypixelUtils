
package com.aidn5.hypixelutils.v1.chatwrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.common.CommandQueuer;
import com.aidn5.hypixelutils.v1.exceptions.NotOnHypixelNetwork;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * an {@link Iterator} iterates over the usernames of the ignored players by the
 * current user. It uses {@link CommandQueuer} to list the current ignored
 * players by pages.
 * <p>
 * <b>Note:</b> This class fetches the next page when it's needed. It does not
 * fetch all the pages at once.
 * <p>
 * <b>muster of the used command:</b> "/ignore list 1" to fetch the first page
 * of the list
 * <p>
 * Use {@link #getAll(HypixelUtils)} only when <u>you have to</u>.
 * Use the {@link Iterator} instead!
 * 
 * @author aidn5
 * 
 * @version 1.0
 * @since 1.0
 * 
 * @category chatwrapper
 */
// some methods has been divided to smaller methods
// to reduce the length number of lines in one method
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
   * off to not spam the chat with the fetched usernames and the
   * {@link #usernamePattern} will be used to fetch them.
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
   * class holds all the methods, which are used by
   * {@link MinecraftForge#EVENT_BUS}.
   * <p>
   * The methods must be public to be used by the events handler. They are in a
   * private inner-class to avoid the programmer from using them like
   * <code>IgnoreList#onChatEvent</code>
   * <p>
   * it is registered <u>only</u> when it's needed.<br>
   * registered at: {@link #fetchNextPage()} and unregistered at
   * {@link EventsListener#onPlayerChatEvent(ClientChatReceivedEvent)}
   */
  @Nonnull
  private final EventsListener eventsListener = new EventsListener();
  /**
   * Array saves all the fetched usernames.
   * 
   * @see #currentUsernameIndex
   */
  @Nonnull
  private final List<String> ignoredUsernames = new ArrayList<String>();
  /**
   * indicates that the user has /ignore no one. so, return <code>false</code>
   * when {@link #hasNext()} is called.
   * 
   * @see #noIgnorePattern
   */
  private boolean noIgnore = false;
  /**
   * Indicates the current page. used when the next page is needed.
   */
  private int currentPage = 0;
  /**
   * indicated the total pages of the list. Used with {@link #currentPage} to know
   * when the list is at its end.
   */
  private int totalPages = -1;
  /**
   * used with {@link #ignoredUsernames} to know where is the pointer at when
   * using {@link #next()}.
   */
  private int currentUsernameIndex = 0;
  /**
   * Indicates whether the next page is fetching at the moment. Used with
   * {@link #fetchNextPage()} to hold the thread and wait till it finishes the
   * loading.
   * <p>
   * The loading is finished when
   * {@link EventsListener#onPlayerChatEvent(ClientChatReceivedEvent)} changes it
   * to <code>false</code>.
   * 
   * TODO: use {@link #wait()} instead
   */
  private boolean loading = false;
  /**
   * see {@link #startPattern}.
   */
  private boolean patternStarted = false;
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
   * player /ignore. Do <u>NOT</u> use it on the main thread.
   * 
   * @param hypixelUtils
   *          an instance of the library.
   * 
   * @return
   *         all the usernames of the ignored players
   * 
   * @throws NotOnHypixelNetwork
   *           if the client is not connected to the hypixel network
   * 
   * @deprecated It's marked as deprecated to only avoid the abusive usage of this
   *             method.
   *             It may be removed in the future though.
   * 
   * @since 1.0
   */
  @Deprecated
  @Nonnull
  public static List<String> getAll(HypixelUtils hypixelUtils) throws NotOnHypixelNetwork {
    // this returns the pointer to the private list
    // since this class is static and the instance of the IgnoreList is never
    // returned. doing this hack should not cause any problem.
    // this speed up the process by NOT creating a new list and clone it

    IgnoreListWrapper ig = IgnoreListWrapper.newInstance(hypixelUtils);
    // force load the pages
    for (String username : ig) {}

    return ig.ignoredUsernames;
  }

  private IgnoreListWrapper(@Nonnull HypixelUtils hypixelUtils) {
    this.hypixelUtils = hypixelUtils;
  }

  /**
   * Check whether there is more cached ignored usernames.
   * If not {@link #fetchNextPage()} and check again.
   * 
   * @throws NotOnHypixelNetwork
   *           if the client was not connected to the hypixel network
   * 
   * @since 1.0
   */
  @Override
  public boolean hasNext() throws NotOnHypixelNetwork {
    if (noIgnore) {
      return false;
    }
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
   * <p>
   * <b>The steps are planned in the following way:</b>
   * <ul>
   * <li>register the {@link #eventsListener}</li>
   * <li>send the command to fetch the next page</li>
   * <li>send the command, which indicates the end of the first command.</li>
   * <li>hold the thread current thread till the fetching is finished in
   * {@link EventsListener#onPlayerChatEvent(ClientChatReceivedEvent)}</li>
   * <li>unregister {@link #eventsListener}, in case it didn't
   * </ul>
   *
   * @throws NotOnHypixelNetwork
   *           if the client was not connected to the hypixel network
   * 
   * @see #loading
   * @see EventsListener
   */
  private void fetchNextPage() throws NotOnHypixelNetwork {
    if (!hypixelUtils.onHypixel()) {
      throw new NotOnHypixelNetwork();
    }

    if (loading) {
      throw new RuntimeException("It's already loading");
    }

    loading = true;

    MinecraftForge.EVENT_BUS.register(eventsListener);
    hypixelUtils.chatBuffer.offer(String.format(COMMAND, ++currentPage));
    CommandQueuer.sendHelloCommand();

    while (loading) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
        MinecraftForge.EVENT_BUS.unregister(eventsListener);
        loading = false;
      }
    }

    MinecraftForge.EVENT_BUS.unregister(eventsListener);
  }

  @Override
  @Nonnull
  public Iterator<String> iterator() {
    return this;
  }

  /**
   * see {@link IgnoreListWrapper#eventsListener}.
   * 
   * @category EventsListener
   */
  private final class EventsListener {
    /**
     * Receive chat events from {@link MinecraftForge#EVENT_BUS} and process them.
     * <p>
     * following way is how the chat is processed:
     * <ul>
     * <li>If {@link IgnoreListWrapper#loading} is not <code>true</code>
     * unregister itself and return</li>
     * <li>detects when the list is started to fetch with
     * {@link IgnoreListWrapper#startPattern}</li>
     * <li>start to fetch the usernames
     * with {@link IgnoreListWrapper#usernamePattern} into
     * {@link IgnoreListWrapper#ignoredUsernames}</li>
     * <li>unregister itself when
     * {@link CommandQueuer#helloPattern} is detected</li>
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

      String message = event.message.getUnformattedText();

      // this code has been divided to multiple methods
      // to shorten the length of the lines in one method
      // 1. this#hasClientNotIgnored(...),
      // 2. this#isIgnoreListStarted(...)
      // 3. this#isNewUsername(...)

      if (hasClientNotIgnored(event, message) || isIgnoreListStarted(event, message)) {
        // everything is done inside their own methods.
        return;
      }

      // to avoid detecting other lists,
      // which are not related to the current process
      // see #isIgnoreListStarted(...)
      if (patternStarted) {
        if (isNewUsername(event, message)) {
          // everything is done inside their own methods.

        } else if (isEnded(event, message)) {
          // everything is done inside their own methods.

        }
      }
    }
  }

  /**
   * check whether this event/message indicates
   * that the user has not ignored anyone to event have an ignore-list to view.
   * <p>
   * If it is, set {@link #noIgnore} to <code>true</code>,
   * {@link #patternStarted} to <code>true</code>,
   * cancel the event to not spam it to the client
   * and unregister the listener to stop receiving chat events.
   * 
   * @param event
   *          the chat event to cancel, if needed.
   * @param message
   *          the extracted chat message from the event.
   * 
   * @return <code>true</code> if it is. <code>false</code> if not and should
   *         probably try something else.
   * 
   * @see #noIgnorePattern
   */
  private boolean hasClientNotIgnored(final ClientChatReceivedEvent event, final String message) {
    if (noIgnorePattern.matcher(message).find()) {
      if (event.isCancelable()) {
        event.setCanceled(true);
      }
      MinecraftForge.EVENT_BUS.unregister(eventsListener);

      noIgnore = true;

      // we early sent "/hello".
      // we need to cancel it before we return
      patternStarted = true;
      return true;
    }

    return false;
  }

  /**
   * check whether this event/message indicates the header of the ignore list.
   * <p>
   * If it is, set {@link #patternStarted} to <code>true</code>
   * and set {@link #currentPage} and {@link #totalPages} to their
   * values, which are extracted from the message by {@link #startPattern}
   * and cancel the event to not spam it to the client.
   * 
   * @param event
   *          the chat event to cancel, if needed.
   * @param message
   *          the extracted chat message from the event.
   * 
   * @return <code>true</code> if it is. <code>false</code> if not and should
   *         probably try something else.
   * 
   * @see #startPattern
   */
  private boolean isIgnoreListStarted(final ClientChatReceivedEvent event, final String message) {
    Matcher startM = startPattern.matcher(message);
    if (startM.find()) {
      if (event.isCancelable()) {
        event.setCanceled(true);
      }

      currentPage = Integer.parseInt(startM.group(1));
      totalPages = Integer.parseInt(startM.group(2));
      patternStarted = true;

      System.out.println("current: " + currentPage + ", total: " + totalPages);
      return true;
    }

    return false;
  }

  /**
   * check whether this event/message has the ignored username list pattern.
   * If it has, then cancel the event to not spam it to the client
   * and try to add the username to {@link #ignoredUsernames} if it is not
   * existed.
   * <p>
   * ignored username pattern: <i>{$nr}. {$username}</i>
   * 
   * @param event
   *          the chat event to cancel, if needed.
   * @param message
   *          the extracted chat message from the event.
   * 
   * @return <code>true</code> if it is. <code>false</code> if not and should
   *         probably try something else.
   * 
   * @see #usernamePattern
   */
  private boolean isNewUsername(final ClientChatReceivedEvent event, final String message) {
    // usernames listed in schema
    Matcher userM = usernamePattern.matcher(message);
    if (userM.find()) {
      if (event.isCancelable()) {
        event.setCanceled(true);
      }

      String ignoredUsername = userM.group(1);
      if (!ignoredUsernames.contains(ignoredUsername)) {
        ignoredUsernames.add(ignoredUsername);
      }

      return true;
    }
    return false;
  }

  /**
   * check whether this event/message indicates
   * the end of the previous command.
   * <p>
   * If it is, set {@link #patternStarted} and {@link #loading}
   * to <code>false</code>,
   * cancel the event to not spam it to the client
   * and unregister the listener to stop receiving chat events.
   * 
   * @param event
   *          the chat event to cancel, if needed.
   * @param message
   *          the extracted chat message from the event.
   * 
   * @return <code>true</code> if it is. <code>false</code> if not and should
   *         probably try something else.
   * 
   * @see CommandQueuer#helloPattern
   */
  private boolean isEnded(final ClientChatReceivedEvent event, final String message) {

    if (CommandQueuer.helloPattern.matcher(message).find()) {
      if (event.isCancelable()) {
        event.setCanceled(true);
      }
      MinecraftForge.EVENT_BUS.unregister(eventsListener);

      patternStarted = false;
      loading = false;
      return true;
    }

    return false;
  }
}
