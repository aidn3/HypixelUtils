
package com.aidn5.hypixelutils.v1.players;

/**
 * Indicates that the provided username is not a valid minecraft-username.
 * 
 * @author aidn5
 *
 * @since 1.0
 */
public class NotValidUsername extends IllegalArgumentException {
  /**
   * Construct an empty exception.
   */
  public NotValidUsername() {
    // empty constructor
  }

  /**
   * Construct the exception.
   * 
   * @param username
   *          the invalid username.
   */
  public NotValidUsername(String username) {
    super(username);
  }
}
