
package com.aidn5.hypixelutils.v1.serverinstance;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * Indicates the type of the lobby the client is connected to.
 * 
 * <p>
 * Used with the command "/whereami".
 * 
 * <p>
 * <i>This enum may clash with {@link GameMode},
 * if it used without confirming the {@link ServerType} is
 * {@link ServerType#LOBBY} and <u>NOT</u>
 * {@link ServerType#MEGAGAME} or {@link ServerType#MINIGAME}</i>
 * 
 * @author aidn5
 * 
 * @since 1.0
 * @version 1.0
 * 
 * @category ServerInstance
 * 
 * @see ServerType#LOBBY
 */
@IHypixelUtils(OnlyHypixel = true)
public enum LobbyType {
  MAIN_LOBBY("", "Main Lobby"), // main lobby has no codename.
  ARCADE_GAMES("arcade", "Arcade Games"),
  BED_WARS("bedwars", "Bed Wars"),
  SKY_WARS("sw", "Sky Wars"),
  MURDER_MYSTERY("mm", "Murder Mystery"),
  BUILD_BATTLE("bb", "Build Battle"),
  DUELS("duels", "Duels"),
  PROTOTYPE("prototype", "Prototype"),
  UHC_CHAMPION("uhc", "UHC Champion"),
  TNT_GAMES("tnt", "TNT Games"),
  CLASSIC_GAMES("legacy", "Classic Games"),
  COPS_AND_CRIMES("mcgo", "Cops And Crimes"),
  BLITZ_SG("blitz", "Blitz Survival Games"), // too long displayName
  MEGA_WALLS("megawalls", "Mega Walls"),
  SMASH_HEROES("smash", "Smash Heroes"),
  WAR_LORDS("bg", "War Lords"),
  SPEED_UHC("speeduhc", "Speed UHC"),
  CRAZY_WALLS("truepvp", "Crazy Walls"), // yes, it's called true PVP...
  TOURNAMENT_HALL("tourney", "Tournament Hall"),
  UNKNOWN("", "Unknown") {
    @Override
    public Pattern getwhereAmILobbyPattern() {
      return Pattern.compile("^You are currently connected to server ([a-z]{0,16}lobby[0-9]{1,4})");
    }
  }; // to avoid using null as a LobbyType.

  @Nonnull
  private final Pattern whereAmILobbyPattern;
  @Nonnull
  private final String lobbyCodeName;
  @Nonnull
  private final String displayName;

  private LobbyType(@Nonnull String lobbyCodeName, @Nonnull String displayName) {
    this.lobbyCodeName = lobbyCodeName;
    this.displayName = displayName;

    this.whereAmILobbyPattern = Pattern
        .compile("^You are currently connected to server ("
            + lobbyCodeName + "lobby([0-9]{1,4})" + ")");
  }

  /**
   * the lobby code-name.
   * 
   * <p>
   * examples: "tnt", "uhc", "prototype", "mcgo".
   * 
   * @return the lobby code-name.
   * 
   * @since 1.0
   */
  @Nonnull
  public String getLobbyCodeName() {
    return lobbyCodeName;
  }

  /**
   * get the lobby name to display/show.
   * 
   * @return the lobby name to display/show.
   * 
   * @since 1.0
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * return the pattern, which can be used with whereami response.
   * 
   * @return the pattern, which can be used with whereami response.
   * 
   * @since 1.0
   */
  @Nonnull
  public Pattern getwhereAmILobbyPattern() {
    return whereAmILobbyPattern;
  }

  /**
   * the pattern which can detect that the /whereami response is a lobby.
   * 
   * @return the pattern to detect that the /whereami response is a lobby.
   * 
   * @since 1.0
   */
  @Nonnull
  public static Pattern getLobbyTypePattern() {
    return ServerType.LOBBY.getWhereAmIPattern();
  }
}
