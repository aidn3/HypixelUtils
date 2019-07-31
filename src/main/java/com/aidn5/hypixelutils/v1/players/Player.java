
package com.aidn5.hypixelutils.v1.players;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.common.annotation.IHelpTools;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

@IHypixelUtils
@IHelpTools(onlyStatic = true)
public class Player {
  private static Pattern validUsername = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{2,15}$");

  private Player() {
    throw new AssertionError();
  }

  /**
   * Check whether the username is a valid minecraft-username.
   * 
   * @param username
   *          the username to validate.
   * 
   * @return <code>true</code> if the username is valid.
   * 
   */
  public static boolean isValidUsername(@Nullable String username) {
    try {
      validateUsername(username);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Check whether the username is valid as a minecraft-username.
   * <p>
   * <b>Usage Example:</b>
   * <code>
   * void method(String username) throws IllegalArgumentException {
   *     this.username = validateUsername(username);
   * }
   * </code>
   * 
   * @param username
   *          the username to validate
   * 
   * @return
   *         returns {@code username} back.
   * 
   * @throws NotValidUsername
   *           if the username is not valid with {@link Exception#getMessage()}
   *           for the reason.
   */
  @Nonnull
  public static String validateUsername(@Nullable String username) throws NotValidUsername {
    if (username == null || username.isEmpty()) {
      throw new NotValidUsername("minecraft username '" + username + "' must not be null or empty");
    }

    if (username.length() < 3 || username.length() > 16) {
      throw new NotValidUsername("minecraft username '" + username
          + "' lengh must be between 3 and 16. " + username.length() + " is given");
    }

    if (!validUsername.matcher(username).find()) {
      throw new NotValidUsername(
          "minecraft username '" + username + "'must only contain a-z, A-Z, 0-9 and '_'");
    }

    return username;
  }

}
