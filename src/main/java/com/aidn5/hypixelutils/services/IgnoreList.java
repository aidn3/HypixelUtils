package com.aidn5.hypixelutils.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aidn5.hypixelutils.common.CommandQueuer;

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
 * Use {@link #getAll()} only when <u>you have to</u>. Use the {@link Iterator}
 * instead!
 * 
 * @author aidn5
 * @version 1.0
 * @since 1.0
 * @category ChatReader
 *
 */
public class IgnoreList implements Iterable<String>, Iterator<String> {
	private static final String COMMAND = "/ignore list %d";
	/**
	 * detects when the user has ignored no one when "/ignore list" is sent
	 * 
	 * @see #noIgnore
	 */
	private static final Pattern noIgnorePattern = Pattern.compile("^You are not ignoring anyone\\.$");
	/**
	 * detects when the ignore list is started to fetch. so, the chat will be turned
	 * off to not spam the chat with the fetched usernames and the
	 * {@link #usernamePattern} will be used to fetch them.
	 * <p>
	 * {@link #patternStarted} is switched to <code>true</code>
	 */
	private static final Pattern startPattern = Pattern
			.compile("------ Ignored Users \\(Page ([0-9]{1,5}) of ([0-9]{1,5})\\) ------");
	/**
	 * detects the fetched username from the chat.
	 * <p>
	 * <b>Muster string: </b>"4. aidn5"
	 */
	private static final Pattern usernamePattern = Pattern.compile("^[0-9]{1,16}\\. (([A-Za-z0-9_]{1,32}))");
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
	private final EventsListener eventsListener = new EventsListener();
	/**
	 * Array saves all the fetched usernames.
	 * 
	 * @see #currentUsernameIndex
	 */
	private final List<String> ignoredUsernames = new ArrayList<String>();
	/**
	 * indicates that the user has /ignore no one. so, return <code>false</code>
	 * when {@link #hasNext()} is called.
	 * 
	 * @see #noIgnorePattern
	 */
	private boolean noIgnore = false;
	/** Indicates the current page. used when the next page is needed */
	private int currentPage = 1;
	/**
	 * indicated the total pages of the list. Used with {@link #currentPage} to know
	 * when the list is at its end
	 */
	private int totalPages = -1;
	/**
	 * used with {@link #ignoredUsernames} to know where is the pointer at when
	 * using {@link #next()}
	 */
	private int currentUsernameIndex = 0;
	/**
	 * Indicates whether the next page is fetching at the moment. Used with
	 * {@link #fetchNextPage()} to hold the thread and wait till it finishes the
	 * loading.
	 * <p>
	 * The loading is finished when
	 * {@link EventsListener#onPlayerChatEvent(ClientChatReceivedEvent)} changes it
	 * to <code>false</code>
	 */
	private boolean loading = false;
	/** see {@link #startPattern} */
	private boolean patternStarted = false;

	/** get a new instance ready to use */
	public static IgnoreList newInstance() {
		return new IgnoreList();
	}

	/**
	 * Fetches all the pages at once and return the results.
	 * <p>
	 * <b>Note:</b> This may takes a while depends on how many players did the
	 * player /ignore. Do <u>NOT</u> use it on the main thread
	 * 
	 * @return all the usernames of the ignored players
	 */
	public static List<String> getAll() {
		// this returns the pointer to the private list
		// since this class is static and the instance of the IgnoreList is never
		// returned. doing this hack should not cause any problem.
		// this speed up the process by NOT creating a new list and clone it
		IgnoreList ig = IgnoreList.newInstance();
		// force load the pages
		for (String username : ig) {}

		return ig.ignoredUsernames;
	}

	private IgnoreList() {
		// private constructor
	}

	@Override
	public boolean hasNext() {
		System.out.println("IgnoreList.hasNext()");
		if (noIgnore) return false;
		if (ignoredUsernames.size() <= currentUsernameIndex) fetchNextPage();
		return ignoredUsernames.size() > currentUsernameIndex + 1;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the required page is not loaded with {@link #hasNext()}
	 */
	@Override
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
	 * @see #loading
	 * @see EventsListener
	 */
	private void fetchNextPage() {
		System.out.println("IgnoreList.fetchNextPage()");
		loading = true;

		MinecraftForge.EVENT_BUS.register(eventsListener);
		CommandQueuer.sendCommand(String.format(COMMAND, currentPage++));
		CommandQueuer.sendHelloCommand();

		while (loading) {
			try {
				Thread.sleep(10);
			} catch (Exception ignored) {}
		}

		MinecraftForge.EVENT_BUS.unregister(eventsListener);
		System.out.println("IgnoreList.fetchNextPage()#finish");
	}

	@Override
	public Iterator<String> iterator() {
		return this;
	}

	/**
	 * see {@link IgnoreList#eventsListener}
	 * 
	 * @category EventsListener
	 */
	private class EventsListener {
		/**
		 * Receive events from {@link MinecraftForge#EVENT_BUS} and process them in the
		 * following way:
		 * <ul>
		 * <li>If {@link IgnoreList#loading} is not <code>true</code> unregister itself
		 * and return</li>
		 * <li>detects when the list is started to fetch with
		 * {@link IgnoreList#startPattern}</li>
		 * <li>start to fetch the usernames with {@link IgnoreList#usernamePattern} into
		 * {@link IgnoreList#ignoredUsernames}</li>
		 * <li>unregister itself when {@link CommandQueuer#helloPattern} is
		 * detected</li>
		 * 
		 * @param event
		 */
		@SubscribeEvent
		public void onPlayerChatEvent(ClientChatReceivedEvent event) {
			System.out.println("IgnoreList.EventsListener.onPlayerChatEvent()#start");

			if (!loading) {
				MinecraftForge.EVENT_BUS.unregister(this);
				return;
			}

			System.out.println("IgnoreList.EventsListener.onPlayerChatEvent()#loading");
			if (event == null || event.type != 0) return;

			System.out.println("IgnoreList.EventsListener.onPlayerChatEvent()#message");
			String message = event.message.getUnformattedText();

			// user has ignored no one
			if (noIgnorePattern.matcher(message).find()) {
				System.out.println("IgnoreList.EventsListener.onPlayerChatEvent()#noIgnore");
				if (event.isCancelable()) event.setCanceled(true);
				MinecraftForge.EVENT_BUS.unregister(this);

				noIgnore = true;
				loading = false;
				return;
			}

			// the header of the ignore list
			Matcher startM = startPattern.matcher(message);
			if (startM.find()) {
				System.out.println("IgnoreList.EventsListener.onPlayerChatEvent()#start");
				if (event.isCancelable()) event.setCanceled(true);
				currentPage = Integer.parseInt(startM.group(1));
				totalPages = Integer.parseInt(startM.group(2));
				patternStarted = true;
				return;
			}

			// to not avoid detecting other lists,
			// which are not related to the current process
			if (patternStarted) {
				// usernames listed in schema "{$nr}. {$username}"
				Matcher userM = usernamePattern.matcher(message);
				if (userM.find()) {
					System.out.println("IgnoreList.EventsListener.onPlayerChatEvent()#username");
					if (event.isCancelable()) event.setCanceled(true);

					if (!ignoredUsernames.contains(userM.group(1))) {
						ignoredUsernames.add(userM.group(1));
					}

					return;
				}

				// pattern indicates the end of the previous command
				if (CommandQueuer.helloPattern.matcher(message).find()) {
					System.out.println("IgnoreList.EventsListener.onPlayerChatEvent()#end");
					if (event.isCancelable()) event.setCanceled(true);
					MinecraftForge.EVENT_BUS.unregister(this);

					patternStarted = false;
					loading = false;
				}
			}
		}
	}
}
