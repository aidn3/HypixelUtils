
package com.aidn5.hypixelutils.v1.players;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.common.cache.CachedSet;
import com.aidn5.hypixelutils.v1.common.cache.ICacher;
import com.aidn5.hypixelutils.v1.common.cache.JsonCacher;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Usernames {
  @Nonnull
  private final ICacher<String, String> cacher;

  /**
   * Create a new instance with default memory-cacher.
   */
  public Usernames() {
    this(null);
  }

  /**
   * Create a new instance with cacher.
   * 
   * @param cacheFilePath
   *          the file where cache should be saved.
   *          can be <code>null</code>
   */
  public Usernames(@Nullable File cacheFilePath) {
    cacher = new JsonCacher<>(cacheFilePath, 7, TimeUnit.DAYS);
  }

  /**
   * get the username of the player by looking up by their ID.
   * 
   * <p>Check cache first then invoke {@link #getUsernameFromNet(UUID)}
   * and save its value into cache for later use.
   * 
   * @param uuid
   *          the ascoicated id to the player.
   * 
   * @return the username of the player or
   *         <code>null</code> if an error occurs or not found.
   * 
   * @throws NullPointerException
   *           if uuid is <code>null</code>
   */
  @Nullable
  public String getUsername(@Nonnull UUID uuid) throws NullPointerException {
    Objects.requireNonNull(uuid);

    cacher.cleanCache();
    CachedSet<String, String> u = cacher.getCacheByKey(uuid.toString());
    if (u != null) {
      return u.getValue();
    }


    String username = getUsernameFromNet(uuid);
    if (username != null && !username.isEmpty()) {
      cacher.cacheNewSet(uuid.toString(), username);
    }

    return username;
  }

  /**
   * get the ascoicated id with the player.
   * 
   * <p>Check cache first then invoke {@link #getUuidFromNet(String)}
   * and save its value into cache for later use.
   * 
   * @param username
   *          the username of the player to lookup to.
   * @return the ascoicated id with the player. or NULL if error occurs.
   * 
   * @throws NullPointerException
   *           if username is <code>null</code>
   */
  @Nullable
  public UUID getUuid(@Nonnull String username) throws NullPointerException {
    Objects.requireNonNull(username);

    cacher.cleanCache();
    Iterator<CachedSet<String, String>> iterator = cacher.getCacheByValue(username).iterator();

    if (iterator.hasNext()) {
      return UUID.fromString(iterator.next().getKey());
    }


    UUID u = getUuidFromNet(username);
    if (u != null) {
      cacher.cacheNewSet(u.toString(), username);
    }

    return u;
  }

  /**
   * get the ascoicated id with the player from Mojang servers.
   * 
   * @param username
   *          the username of the player to lookup to.
   * @return the ascoicated id with the player. or NULL if error occurs.
   */
  @Nullable
  public static UUID getUuidFromNet(@Nonnull String username) {
    try {
      Objects.requireNonNull(username);

      String json = getString("https://api.mojang.com/users/profiles/minecraft/" + username);

      JsonElement jsonObject = new JsonParser().parse(json);
      String uuid = jsonObject.getAsJsonObject().get("id").getAsString();

      return UUID.fromString(uuid);
    } catch (Exception e) {

      e.printStackTrace();
    }

    return null;
  }

  /**
   * get the username of the player by looking up by their ID.
   * 
   * @param uuid
   *          the ascoicated id to the player.
   * @return the username of the player or
   *         <code>null</code> if an error occurs or not found.
   */
  @Nullable
  public static String getUsernameFromNet(@Nonnull UUID uuid) {
    try {
      Objects.requireNonNull(uuid);

      String json = getString("https://api.mojang.com/user/profiles/" + uuid + "/names");

      JsonArray playerProfiles = new JsonParser().parse(json).getAsJsonArray();

      JsonObject profile = playerProfiles.get(playerProfiles.size() - 1).getAsJsonObject();

      return profile.get("name").getAsString();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


  @Nonnull
  private static String getString(@Nonnull String url) throws IOException {
    URL urlObj = new URL(url);

    URLConnection urlConnection = urlObj.openConnection();
    InputStream inputStream = urlConnection.getInputStream();

    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = inputStream.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }

    inputStream.close();
    return result.toString("UTF-8");
  }

  /*
   * private static Cache<UUID, String> getCacher() {
   * return CacheBuilder.newBuilder()
   * .maximumSize(10000)
   * .expireAfterWrite(7, TimeUnit.DAYS)
   * .initialCapacity(10)
   * .build(
   * new CacheLoader<UUID, String>() {
   * @Override
   * public String load(UUID key) throws Exception {
   * return null;
   * }
   * });
   * }
   */
}
