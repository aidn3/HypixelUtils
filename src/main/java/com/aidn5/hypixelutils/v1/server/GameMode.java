
package com.aidn5.hypixelutils.v1.server;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.chatreader.MapWrapper;

/**
 * Most games on hypixel network.
 * 
 * <p>
 * <i>This enum may clash with {@link LobbyType},
 * if used without confirming the {@link ServerType} is
 * {@link ServerType#MINIGAME} or {@link ServerType#MEGAGAME}
 * and <u>NOT</u> {@link ServerType#LOBBY}</i>
 * 
 * <p>
 * Used with the command "/map" or scoreboard.
 * 
 * <p>
 * <b>Warning: This enum is not practical.<br>
 * This enum will probably be out-dated in less than 6 months.</b>
 * <p>
 * 
 * @author aidn5
 *
 * @version 1.0
 * @since 1.0
 * 
 * @category ChatReader
 * 
 * @see MapWrapper
 */
// TODO: GameMode: add the rest of the games
// TODO: GameMode: test games
public enum GameMode {
  BED_WARS(LobbyType.BED_WARS, "Bed Wars", "BED WARS"),
  MURDER_MYSTERY(LobbyType.MURDER_MYSTERY, "Murder Mystery", "MURDER MYSTERY"),
  BUILD_BATTLE(LobbyType.BUILD_BATTLE, "Build Battle", "BUILD BATTLE"),
  DUELS(LobbyType.DUELS, "Duels", "DUELS"),
  SKY_WARS(LobbyType.SKY_WARS, "Sky Wars", "SKY WARS"),
  UHC_CHAMPION(LobbyType.UHC_CHAMPION, "UHC Champion", "UHC CHAMPION"),
  TNT_GAMES(LobbyType.TNT_GAMES, "TNT Games", "TNT GAMES"),
  SPEED_UHC(LobbyType.SPEED_UHC, "Speed UHC", "SPEED UHC"),
  /**
   * Indicates the housing servers.
   * Indicates both, client's and other people's base
   */
  HOUSING(LobbyType.MAIN_LOBBY, "Housing", "HOUSING"),

  // arcade games
  BLOCKING_DEAD(LobbyType.ARCADE_GAMES, "Blocking Dead", "BLOCING DEAD"),
  THROW_OUT(LobbyType.ARCADE_GAMES, "Throw Out", "THROW OUT"),
  GALAXY_WARS(LobbyType.ARCADE_GAMES, "Galaxy Wars", "GALAXY WARS"),
  ENDER_SPLEEF(LobbyType.ARCADE_GAMES, "Ender Spleef", "ENDER SPLEEF"),
  DRAGON_WARS(LobbyType.ARCADE_GAMES, "Dragon Wars", "DRAGON WARS"),
  PIXEL_PAINTERS(LobbyType.ARCADE_GAMES, "Pixel Painters", "PIXEL PAINTERS"),
  BOUNTY_HUNTERS(LobbyType.ARCADE_GAMES, "Bounty Hunters", "BOUNTY HUNTERS"),
  FOOTBALL(LobbyType.ARCADE_GAMES, "Football", "FOOTBALL"),
  HOLE_IN_THE_WALL(LobbyType.ARCADE_GAMES, "HoleInTheWall", "HOLEINTHEWALL"),
  CREEPER_ATTACK(LobbyType.ARCADE_GAMES, "Creeper Attack", "CREEPER ATTACK"),
  DEAD_END(LobbyType.ARCADE_GAMES, "Dead End", "DEAD END"),
  PARTY_GAMES(LobbyType.ARCADE_GAMES, "Party Games", "PARTY GAMES"),
  FARM_HUNT(LobbyType.ARCADE_GAMES, "Farm Hunts", "FARM HUNTS"),
  MINI_WALLS(LobbyType.ARCADE_GAMES, "Mini Walls", "MINI WALLS"),
  HIDE_AND_SEEK(LobbyType.ARCADE_GAMES, "Hide & Seek", "HIDE AND SEEK"),

  // prototype
  /**
   * A prototype game.
   * 
   * @deprecated since it is a prototype game,
   *             it may be removed/changed/renamed in the near future.
   */
  THE_PIT(LobbyType.PROTOTYPE, "The Pit", "THE PIT"),
  /**
   * A prototype game.
   * 
   * @deprecated since it is a prototype game,
   *             it may be removed/changed/renamed in the near future.
   */
  SKY_BLOCK(LobbyType.PROTOTYPE, "Skyblock", "SKYBLOCK"),

  /**
   * The game mode and the map are unknown.
   */
  UNKNOWN(LobbyType.UNKNOWN, "UNKNOWN", "UNKNOWN");

  @Nonnull
  private final LobbyType lobby;
  @Nonnull
  private final String displayGameName;
  @Nonnull
  private final String gameName;

  private GameMode(@Nonnull LobbyType lobbyType, @Nonnull String displayGameName,
      @Nonnull String gameName) {
    this.lobby = lobbyType;
    this.displayGameName = displayGameName;
    this.gameName = gameName;
  }

  /**
   * get the official game name of this mode.
   * 
   * @return the official game name of this mode.
   * 
   * @since 1.0
   */
  public String getDisplayGameName() {
    return displayGameName;
  }

  /**
   * get the code name of this game mode.
   * 
   * @return the code name of this game mode.
   * 
   * @since 1.0
   */
  public String getGameName() {
    return gameName;
  }

  /**
   * get the associated lobby with this game mode.
   * 
   * @return the associated lobby with this game mode.
   * 
   * @since 1.0
   */
  public LobbyType getLobby() {
    return lobby;
  }
}
