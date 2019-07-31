
package com.aidn5.hypixelutils.v1.serverinstance;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IOnlyHypixel;
import com.aidn5.hypixelutils.v1.tools.Scoreboard;

import net.minecraft.client.Minecraft;

/**
 * Most games on hypixel network.
 * 
 * <p>
 * <i>This enum may clash with {@link LobbyType},
 * if it used without confirming the {@link ServerType} is
 * {@link ServerType#MINIGAME} or {@link ServerType#MEGAGAME}
 * and <u>NOT</u> {@link ServerType#LOBBY}</i>
 * 
 * <p>
 * can be used with scoreboard.
 * {@link Scoreboard#gameServerTitle(Minecraft, HypixelUtils)}
 * 
 * <p>
 * <b>Warning: This enum is not practical.<br>
 * This enum will probably be out-dated in less than 6 months.</b>
 * 
 * @author aidn5
 *
 * @version 1.0
 * @since 1.0
 * 
 * @category ServerInstance
 */
@IHypixelUtils
@IOnlyHypixel
// TODO: GameMode: add the rest of the games
// TODO: GameMode: test games
public enum GameMode {
  BED_WARS(LobbyType.BED_WARS, ServerType.MINIGAME, "Bed Wars", "BED WARS"),
  MURDER_MYSTERY(LobbyType.MURDER_MYSTERY, ServerType.MINIGAME, "Murder Mystery", "MURDER MYSTERY"),
  BUILD_BATTLE(LobbyType.BUILD_BATTLE, ServerType.MINIGAME, "Build Battle", "BUILD BATTLE"),
  DUELS(LobbyType.DUELS, ServerType.MINIGAME, "Duels", "DUELS"),
  SKY_WARS(LobbyType.SKY_WARS, ServerType.MINIGAME, "Sky Wars", "SKY WARS"),
  UHC_CHAMPION(LobbyType.UHC_CHAMPION, ServerType.MEGAGAME, "UHC Champion", "UHC CHAMPION"),
  TNT_GAMES(LobbyType.TNT_GAMES, ServerType.MINIGAME, "TNT Games", "TNT GAMES"),
  SPEED_UHC(LobbyType.SPEED_UHC, ServerType.MINIGAME, "Speed UHC", "SPEED UHC"),
  WAR_LORDS(LobbyType.WAR_LORDS, ServerType.MINIGAME, "War Lords", "WAR LORDS"),
  SMASH_HEROES(LobbyType.SMASH_HEROES, ServerType.MINIGAME, "Smash Heroes", "SMASH HEROES"),
  CRAZY_WALLS(LobbyType.CRAZY_WALLS, ServerType.MINIGAME, "Crazy Walls", "CRAZY WALLS"),
  // TODO: COPS_AND_CRIMES, CLASSIC_GAMES, TNT_GAMES,
  // TODO: Prototype games, the bridge, blitz,

  /**
   * Indicates the housing servers.
   * Indicates both, client's and other people's base
   */
  HOUSING(LobbyType.MAIN_LOBBY, ServerType.MINIGAME, "Housing", "HOUSING"),

  // arcade games
  BLOCKING_DEAD(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Blocking Dead", "BLOCING DEAD"),
  THROW_OUT(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Throw Out", "THROW OUT"),
  GALAXY_WARS(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Galaxy Wars", "GALAXY WARS"),
  ENDER_SPLEEF(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Ender Spleef", "ENDER SPLEEF"),
  DRAGON_WARS(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Dragon Wars", "DRAGON WARS"),
  PIXEL_PAINTERS(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Pixel Painters", "PIXEL PAINTERS"),
  BOUNTY_HUNTERS(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Bounty Hunters", "BOUNTY HUNTERS"),
  FOOTBALL(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Football", "FOOTBALL"),
  HOLE_IN_THE_WALL(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "HoleInTheWall", "HOLEINTHEWALL"),
  CREEPER_ATTACK(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Creeper Attack", "CREEPER ATTACK"),
  DEAD_END(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Dead End", "DEAD END"),

  // TODO: there should be 3 types of party games
  PARTY_GAMES(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Party Games", "PARTY GAMES"),

  // TODO: farm "hunts" or "hunt"?
  FARM_HUNT(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Farm Hunts", "FARM HUNTS"),
  MINI_WALLS(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Mini Walls", "MINI WALLS"),

  // TODO: HIDE_AND_SEEK: is "HIDE AND SEEK" or "HIDE & SEEK"
  HIDE_AND_SEEK(LobbyType.ARCADE_GAMES, ServerType.MINIGAME, "Hide & Seek", "HIDE AND SEEK"),

  // TODO: are tournament games have the same name as their parents?

  // prototype
  /**
   * A prototype game.
   * 
   * @deprecated since it is a prototype game,
   *             it may be removed/changed/renamed in the near future.
   */
  @Deprecated
  THE_PIT(LobbyType.PROTOTYPE, ServerType.MINIGAME, "The Pit", "THE PIT"),
  /**
   * A prototype game.
   * 
   * @deprecated since it is a prototype game,
   *             it may be removed/changed/renamed in the near future.
   */
  @Deprecated
  SKY_BLOCK(LobbyType.PROTOTYPE, ServerType.MINIGAME, "Sky Block", "SKYBLOCK"),

  /**
   * The game mode and the map are unknown.
   */
  UNKNOWN(LobbyType.UNKNOWN, ServerType.UNKNOWN, "UNKNOWN", "UNKNOWN");

  @Nonnull
  private final LobbyType lobby;
  @Nonnull
  private final ServerType serverType;

  @Nonnull
  private final String displayGameName;
  @Nonnull
  private final String gameName;

  private GameMode(@Nonnull LobbyType lobbyType, @Nonnull ServerType serverType,
      @Nonnull String displayGameName, @Nonnull String gameName) {
    this.lobby = lobbyType;
    this.serverType = serverType;

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
  @Nonnull
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
  @Nonnull
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
  @Nonnull
  public LobbyType getLobby() {
    return lobby;
  }

  /**
   * get the server type the game runs on.
   * 
   * @return
   *         either {@link ServerType#MINIGAME}, {@link ServerType#MEGAGAME}
   *         or {@link ServerType#UNKNOWN}.
   * 
   * @since 1.0
   */
  @Nonnull
  public ServerType getServerType() {
    return serverType;
  }
}
