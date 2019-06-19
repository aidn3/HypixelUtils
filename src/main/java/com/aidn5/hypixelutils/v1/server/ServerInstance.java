
package com.aidn5.hypixelutils.v1.server;

import java.util.regex.Matcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.chatreader.WhereAmIWrapper;
import com.aidn5.hypixelutils.v1.eventslistener.ServerInstanceListener;
import com.aidn5.hypixelutils.v1.exceptions.NotOnHypixelNetwork;
import com.aidn5.hypixelutils.v1.tools.Scoreboard;

import net.minecraft.client.Minecraft;

public class ServerInstance {
  @Nonnull
  protected LobbyType lobbyType = LobbyType.UNKNOWN;
  protected int lobbyNumber = 0;

  @Nonnull
  protected GameMode gameMode = GameMode.UNKNOWN;

  @Nonnull
  protected ServerType serverType = ServerType.UNKNOWN;
  @Nonnull
  protected String serverId = lobbyType.getLobbyCodeName() + "lobby" + lobbyNumber;

  // empty but never null
  @Nonnull
  protected String whereami = "";

  /**
   * Create an empty instance with everything either unknown
   * or empty (but never <code>null</code>).
   */
  public ServerInstance() {
    // empty constructor
  }

  /**
   * get the current game mode by the scoreboard.
   * 
   * <p>
   * <i>This enum may clash with {@link LobbyType},
   * if used without confirming the {@link ServerType} is
   * {@link ServerType#MINIGAME} or {@link ServerType#MEGAGAME}
   * and <u>NOT</u> {@link ServerType#LOBBY}</i>
   * 
   * @param mc
   *          Minecraft instance.
   * 
   * @param hypixelUtils
   *          instance of the library to check
   *          whether the client is connected to hypixel network.
   * 
   * @return
   *         current game mode. or {@link GameMode#UNKNOWN} if not found.
   * 
   * @throws NotOnHypixelNetwork
   *           if parameter hypixelUtils is not <code>null</code> and
   *           the client is not connected to hypixel network.
   * 
   * @since 1.0
   * 
   * @see Scoreboard#gameServerTitle(Minecraft, HypixelUtils)
   */
  @Nonnull
  public static GameMode getGameModeByCurrentScoreBoard(@Nonnull Minecraft mc,
      @Nullable HypixelUtils hypixelUtils) throws NotOnHypixelNetwork {

    String currentScoreboard = Scoreboard.gameServerTitle(mc, null)
        .trim().toLowerCase()
        .replace("_", "").replace(" ", "");

    for (GameMode gameMode : GameMode.values()) {
      String scoreboardName = gameMode.getGameName().trim().toLowerCase()
          .replace("_", "").replace(" ", "");

      if (currentScoreboard.contains(scoreboardName)) {
        return gameMode;
      }
    }

    return GameMode.UNKNOWN;
  }

  /**
   * get the lobby type from the whereami message.
   * 
   * <p>
   * <i>Use {@link LobbyType#getLobbyTypePattern()} instead of this method,
   * if you only need to determine
   * whether the client is connected to a lobby or not.
   * since this method may return {@link LobbyType#UNKNOWN},
   * if the lobby is not registered in the registry.</i>
   * 
   * 
   * @param whereami
   *          the response from the /whereami command.
   * 
   * @return the lobby the whereami appointed to.
   *         or {@link LobbyType#UNKNOWN} if not found.
   * 
   * @since 1.0
   * 
   * @see LobbyType#getLobbyTypePattern()
   * @see WhereAmIWrapper
   * @see ServerInstanceListener
   */
  @Nonnull
  public static LobbyType getLobbyTypeFromWhereAmI(@Nullable String whereami) {
    if (whereami == null || whereami.isEmpty()) {
      return LobbyType.UNKNOWN;
    }

    for (LobbyType lobbyType : LobbyType.values()) {
      if (lobbyType.getwhereAmILobbyPattern().matcher(whereami).find()) {
        return lobbyType;
      }
    }

    return LobbyType.UNKNOWN;
  }

  /**
   * parse the response of the command "/whereami"
   * and create and instance contains all the parsed info.
   * 
   * <p>
   * if parameter mc is provided and the whereami appoints to
   * a game mode, then game mode will also be set.
   * <br>
   * If the response was empty or <code>null</code>
   * an empty instance will be created.
   * 
   * @param whereami
   *          the message to parse
   * @param mc
   *          (optional) an instance of Minecraft.
   * @param hypixelUtils
   *          (optional) an instance of the library.
   * 
   * @return
   *         and instance contains all the parsed informations
   * 
   * @throws NotOnHypixelNetwork
   *           if the message appoints to a game mode and
   *           the parameter hypixelUtils is not <code>null</code> and
   *           the client is not connected to hypixel network.
   * 
   * @since 1.0
   */
  @Nonnull
  public static ServerInstance createInstance(@Nullable String whereami, @Nullable Minecraft mc,
      @Nullable HypixelUtils hypixelUtils) throws NotOnHypixelNetwork {

    ServerInstance newInstance = new ServerInstance();

    if (whereami == null || whereami.isEmpty()) {
      return newInstance;
    }

    newInstance.whereami = whereami;

    for (ServerType serverType : ServerType.values()) {

      Matcher serverTypeM = serverType.getWhereAmIPattern().matcher(whereami);
      if (!serverTypeM.find()) {
        continue;
      }

      newInstance.serverType = serverType;
      newInstance.serverId = serverTypeM.group(1);

      if (serverType.equals(ServerType.LOBBY)) {
        for (LobbyType lobbyType : LobbyType.values()) {
          Matcher lobbyM = lobbyType.getwhereAmILobbyPattern().matcher(whereami);

          if (lobbyM.find()) {
            newInstance.lobbyType = lobbyType;
            newInstance.lobbyNumber = Integer.parseInt(lobbyM.group(2));

            return newInstance;
          }
        }

        return newInstance;

      } else if (serverType.equals(ServerType.MEGAGAME)
          || serverType.equals(ServerType.MINIGAME)) {

        if (mc != null) {
          newInstance.gameMode = getGameModeByCurrentScoreBoard(mc, hypixelUtils);
        }

        return newInstance;
      }
    }

    return newInstance;
  }

  /**
   * Whether the client is in a lobby.
   * 
   * @return <code>true</code> if it is.
   * 
   * @since 1.0
   */
  public boolean isLobby() {
    return serverType.equals(ServerType.LOBBY);
  }

  /**
   * whether the client is in a game.
   * 
   * @return <code>true</code> if it is.
   * 
   * @since 1.0
   */
  public boolean isGame() {
    return serverType == ServerType.MINIGAME || serverType == ServerType.MEGAGAME;
  }

  /**
   * whether the client is in limbo.
   * 
   * @return <code>true</code> if it is.
   * 
   * @since 1.0
   */
  public boolean isLimbo() {
    return serverType.equals(ServerType.LIMBO);
  }

  /**
   * get the associated id to the server.
   * 
   * @return the server id. if it's unknown/empty instance,
   *         then "lobby0" is returned.
   *         <u>never <code>null</code> or empty.</u>
   * 
   * @since 1.0
   */
  @Nonnull
  public String getServerId() {
    return serverId;
  }

  /**
   * used when the instance is a {@link LobbyType} instance.
   * 
   * @return the lobby type of the this instance
   *         or {@link LobbyType#UNKNOWN},
   *         if the instance is not {@link LobbyType}.
   * 
   * @since 1.0
   */
  @Nonnull
  public LobbyType getLobbyType() {
    return lobbyType;
  }

  /**
   * used when the instance is {@link LobbyType}.
   * 
   * @return the lobby number of this instance
   *         or 0 if the instance is not {@link LobbyType}
   * 
   * @since 1.0
   */
  public int getLobbyNumber() {
    return lobbyNumber;
  }

  /**
   * return the server type of this instance.
   * <p>
   * If the server type is {@link ServerType#LOBBY},
   * then {@link #getLobbyType()} and {@link #getLobbyNumber()}
   * is also available.
   * 
   * @return the server type of this instance.
   * 
   * @since 1.0
   */
  @Nonnull
  public ServerType getServerType() {
    return serverType;
  }

  /**
   * get the game mode this instance appoints to.
   * 
   * <p>
   * used when the instance {@link #getServerType()} is
   * {@link ServerType#MINIGAME} or {@link ServerType#MEGAGAME}.
   * 
   * @return the game mode this instance appoints to.
   * 
   * @since 1.0
   */
  @Nonnull
  public GameMode getGameMode() {
    return gameMode;
  }

  /**
   * get the whereami response, which used to create this instance.
   * 
   * <p>
   * <i>whereami is set by
   * {@link #createInstance(String, Minecraft, HypixelUtils)},
   * if not then it is empty</i>
   * 
   * @return the whereami response, which used to create this instance.
   * 
   * @since 1.0
   */
  @Nonnull
  public String getWhereami() {
    return whereami;
  }

  /*
   * (non-Javadoc)
   * auto-generated
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((gameMode == null) ? 0 : gameMode.hashCode());
    result = prime * result + lobbyNumber;
    result = prime * result + ((lobbyType == null) ? 0 : lobbyType.hashCode());
    result = prime * result + ((serverId == null) ? 0 : serverId.hashCode());
    result = prime * result + ((serverType == null) ? 0 : serverType.hashCode());
    result = prime * result + ((whereami == null) ? 0 : whereami.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * auto-generated
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ServerInstance other = (ServerInstance) obj;
    if (gameMode != other.gameMode) {
      return false;
    }
    if (lobbyNumber != other.lobbyNumber) {
      return false;
    }
    if (lobbyType != other.lobbyType) {
      return false;
    }
    if (serverId == null) {
      if (other.serverId != null) {
        return false;
      }
    } else if (!serverId.equals(other.serverId)) {
      return false;
    }
    if (serverType != other.serverType) {
      return false;
    }
    if (whereami == null) {
      if (other.whereami != null) {
        return false;
      }
    } else if (!whereami.equals(other.whereami)) {
      return false;
    }
    return true;
  }
}
