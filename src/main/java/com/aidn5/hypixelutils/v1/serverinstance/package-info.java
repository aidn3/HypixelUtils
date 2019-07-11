/**
 * the ServerInstance represents the server the client connected to.
 *
 * <p>
 * {@link com.aidn5.hypixelutils.v1.serverinstance.ServerType}
 * is the type of the server the client connected to.
 * 
 * Furthermore if the server type is
 * {@link com.aidn5.hypixelutils.v1.serverinstance.ServerType#LOBBY},
 * {@link com.aidn5.hypixelutils.v1.serverinstance.LobbyType}
 * can be used. And if the server type is a game (MINIGAME/MEGAGAME)
 * {@link com.aidn5.hypixelutils.v1.serverinstance.GameMode} can also be used.
 * 
 * <p>
 * {@link com.aidn5.hypixelutils.v1.serverinstance.ServerInstance}
 * is the main class which parse the given information and create
 * an instance provides methods to determine the server type, lobby type,
 * game mode and etc.
 * 
 * <p>
 * <b>Notice (When using the enum directly and not relying on
 * {@link com.aidn5.hypixelutils.v1.serverinstance.ServerInstance}):</b>
 * Enum {@link com.aidn5.hypixelutils.v1.serverinstance.LobbyType}
 * and {@link com.aidn5.hypixelutils.v1.serverinstance.GameMode}
 * may clash with each other if used without determining the server type.<br>
 * Since Hypixel network uses the same displayName
 * in both the game and its lobby, using the wrong enum will return value
 * (even though it should gives "UNKNOWN", since it's not a game/lobby).<br>
 * <i> Example: using the enum
 * {@link com.aidn5.hypixelutils.v1.serverinstance.GameMode}
 * or using
 * {@link com.aidn5.hypixelutils.v1.serverinstance.ServerInstance
 * #getGameModeByCurrentScoreBoard(net.minecraft.client.Minecraft, com.aidn5.hypixelutils.v1.HypixelUtils)}
 * directly to check when the player is playing
 * bedwars, will also returns positive
 * when the player is in a bedwars lobby.</i>
 * 
 * @author aidn5
 * 
 * @since 1.0
 */

package com.aidn5.hypixelutils.v1.serverinstance;
