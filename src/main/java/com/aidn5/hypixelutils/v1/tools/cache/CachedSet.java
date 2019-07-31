
package com.aidn5.hypixelutils.v1.tools.cache;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.common.annotation.IHelpTools;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * Object holds the cached data and its metadata.
 * 
 * @author aidn5
 *
 * @param <K>
 *          the base key type to use to retrieve the cache.
 * @param <V>
 *          the base value type to use for cache.
 * 
 * @since 1.0
 * @category ICacher
 * 
 * @see ICacher
 */
@IHypixelUtils
@IHelpTools
public class CachedSet<K, V> {
  private final K key;
  private final V value;
  private final long time;

  /**
   * Create set instance for new data.
   * 
   * @param key
   *          the key to the cache.
   * @param value
   *          the cache
   * @param time
   *          when was is cached.
   */
  public CachedSet(@Nonnull K key, @Nullable V value, long time) {
    this.key = Objects.requireNonNull(key);
    this.value = value;
    this.time = time;
  }

  /**
   * get the time when it was cached.
   * 
   * @return
   *         the time when it was cached.
   */
  public long getTime() {
    return time;
  }

  /**
   * get the key to the cache.
   * 
   * @return
   *         the key to the cache.
   */
  @Nonnull
  public K getKey() {
    return key;
  }

  /**
   * get the cached data.
   * 
   * @return
   *         the cached data.
   */
  @Nullable
  public V getValue() {
    return value;
  }
}
