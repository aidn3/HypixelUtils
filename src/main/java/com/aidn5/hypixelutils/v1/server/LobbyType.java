
package com.aidn5.hypixelutils.v1.server;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Indicates the type of the lobby the client is connected to.
 * 
 * <p>
 * Used with the command "/whereami".
 * 
 * @author aidn5
 * 
 * @since 1.0
 * @version 1.0
 * 
 * @see ServerType#LOBBY
 */
public enum LobbyType {
  MAIN_LOBBY(""), // main lobby has no codename.
  ARCADE_GAMES("arcade"),
  BED_WARS("bedwars"),
  SKY_WARS("sw"),
  MURDER_MYSTERY("mm"),
  BUILD_BATTLE("bb"),
  DUELS("duels"),
  PROTOTYPE("prototype"),
  UHC_CHAMPION("uhc"),
  TNT_GAMES("tnt"),
  CLASSIC_GAMES("legacy"),
  COPS_AND_CRIMES("mcgo"),
  BLITZ_SG("blitz"),
  MEGA_WALLS("megawalls"),
  SMASH_HEROES("smash"),
  WAR_LORDS("bg"),
  SPEED_UHC("speeduhc"),
  CRAZY_WALLS("truepvp"), // yes, it's called true pvp...
  TOURNAMENT_HALL("tourney"),
  UNKNOWN("") {
    @Override
    public Pattern getwhereAmILobbyPattern() {
      return Pattern.compile("^You are currently connected to server ([a-z]{0,16}lobby[0-9]{1,4})");
    }
  }; // to avoid using null as a LobbyType.

  @Nonnull
  private final Pattern whereAmILobbyPattern;
  @Nonnull
  private final String lobbyCodeName;

  private LobbyType(@Nonnull String lobbyCodeName) {
    this.lobbyCodeName = lobbyCodeName;

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
