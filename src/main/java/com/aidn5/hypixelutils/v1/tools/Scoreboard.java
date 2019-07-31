
package com.aidn5.hypixelutils.v1.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IHelpTools;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IOnlyHypixel;
import com.aidn5.hypixelutils.v1.exceptions.NotOnHypixelNetwork;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.EnumChatFormatting;

/**
 * a mix of methods help to work with the scoreboard.
 * 
 * @author aidn5
 * @since 1.0
 * 
 * @category Utils
 */
@IHypixelUtils
@IHelpTools(onlyStatic = true)
public class Scoreboard {
  /**
   * Get the title of the current server from the scoreboard.
   * <p>
   * Every server on hypixel shows its type in the first field on the scoreboard.
   * <br>
   * <i>Examples: "SKYWARS", "BEDWARS", "ARCADE GAMES"</i>
   * 
   * @param mc
   *          Minecraft instance.
   * @param hypixelUtils
   *          instance of the library to check
   *          whether the client is connected to hypixel network.
   * 
   * @return
   *         the title on hypixel network
   * 
   * @throws NotOnHypixelNetwork
   *           if hypixelUtils is not <code>null</code> and
   *           the client is not connected to hypixel network
   * 
   * @since 1.0
   * @see #getSidebarScores(Minecraft)
   */
  @IOnlyHypixel
  @Nonnull
  public static String gameServerTitle(@Nonnull Minecraft mc, @Nullable HypixelUtils hypixelUtils)
      throws NotOnHypixelNetwork {
    if (hypixelUtils != null && !hypixelUtils.onHypixel()) {
      throw new NotOnHypixelNetwork();
    }

    net.minecraft.scoreboard.Scoreboard scoreboard = mc.theWorld.getScoreboard();
    ScoreObjective objective = scoreboard.getScoreObjectives().iterator().next();

    return EnumChatFormatting.getTextWithoutFormattingCodes(objective.getDisplayName());
  }

  /**
   * Get Objectives from the scoreboard without {@link EnumChatFormatting}.
   * <p>
   * Useful to get the strings from the scoreboard.
   * 
   * @param mc
   *          Minecraft instance.
   * 
   * @return all Strings viewed in the scoreboard sidebar.
   * 
   * @since 1.0
   * 
   * @see net.minecraft.scoreboard.Scoreboard#getScoreObjectives()
   */
  @Nonnull
  public static List<String> getSidebarScores(@Nonnull Minecraft mc) {
    Collection<String> obCollection = mc.theWorld.getScoreboard().getObjectiveNames();
    List<String> scoreBoard = new ArrayList<String>();

    for (String s : obCollection) {
      String text = EnumChatFormatting.getTextWithoutFormattingCodes(s);
      scoreBoard.add(text);
    }

    return scoreBoard;
  }
}
