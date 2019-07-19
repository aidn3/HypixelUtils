
package com.aidn5.hypixelutils.v1.serverinstance;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.chatwrapper.WhereAmIWrapper;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.eventslistener.ServerInstanceListener;

/**
 * What type of server is the client connected to on hypixel network. Detects
 * whether the server is {@link #LOBBY}, {@link #MINIGAME}, {@link #LIMBO},
 * etc.)
 * 
 * <p>
 * Used with the command /whereami
 * 
 * @author aidn5
 * 
 * @version 1.0
 * @since 1.0
 * 
 * @category ServerInstance
 */
@IHypixelUtils(OnlyHypixel = true)
// all regex patterns must have one group
public enum ServerType {
  /**
   * The player is here probably because they are either AFK (idling)
   * or they got a problem with their connection.
   */
  LIMBO("^You are currently in (limbo)"),
  /**
   * They are in a lobby waiting/chatting/doing whatever they would like to do.
   * perfect time to send commands like /stats, /profile
   * and other commands, which requires the player to be in a lobby and not in a
   * game.
   * 
   * @see LobbyType
   */
  LOBBY("^You are currently connected to server ([a-z]{0,64}lobby[a-zA-Z0-9]{1,5})"),
  /**
   * The player is playing a mini-game like skywars/duel/bedwars/etc.
   * Best time to activate mods like hitIndicator, pingMod, etc.
   */
  MINIGAME("^You are currently connected to server (mini[a-zA-Z0-9]{1,5})"),
  /**
   * The player is playing a game which will probably takes an hour to finish
   * like UHC-Champion.
   * 
   * <p>
   * It's better not to mess with the player like doing /hub or doing something
   * that may crash the client.
   */
  MEGAGAME("^You are currently connected to server (mega[a-zA-Z0-9]{1,5})"),
  /**
   * The player is on hypixel network, but the {@link HypixelUtils} can not
   * determine where is the player exactly at.
   * 
   * <p>
   * <i>It probably means there is a newer version of the library to download,
   * since this version is out-dated and can't find the type in the registry.</i>
   */
  UNKNOWN("^You are currently ([\\S ]{4,30})");

  @Nonnull
  private final Pattern chatPattern;

  private ServerType(@Nonnull String chatPattern) {
    this.chatPattern = Pattern.compile(chatPattern);
  }

  /**
   * get chat pattern, which can be used with the command "/whereami"
   * to detect the server's type.
   * 
   * @return the pattern that can detect the chosen server's type
   * 
   * @since 1.0
   */
  @Nonnull
  public Pattern getWhereAmIPattern() {
    return chatPattern;
  }

  /**
   * regex to determine, whether the message is a /whereami response.
   * 
   * @return the regex to use to find /whereami response.
   * 
   * @since 1.0
   * 
   * @see WhereAmIWrapper
   * @see ServerInstanceListener
   */
  @Nonnull
  public static Pattern getServerTypePattern() {
    return Pattern.compile("^You are currently (?:in limbo|connected to server )");
  }
}
