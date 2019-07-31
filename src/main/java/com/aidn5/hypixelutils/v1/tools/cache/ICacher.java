
package com.aidn5.hypixelutils.v1.tools.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The main interface, which is used to cacher adapters.
 * 
 * <p>This interface gives key-value based cacher.
 * It designed to save cache into disk for later use
 * after ending the session and starting new one.
 * 
 * <p>{@link com.google.common.cache} may be a better
 * If only caching is needed without saving into File.
 * 
 * 
 * @author aidn5
 *
 * @param <K>
 *          the base key type to use to retrieve the cache.
 * @param <V>
 *          the base value type to use for cache.
 * 
 * @since 1.0
 * @version 1.0
 * 
 * @see com.google.common.cache.CacheBuilder
 */
//TODO: let methods return boolean to determine the succession of the process.
public interface ICacher<K, V> {
  /**
   * Create a new cache entry.
   * 
   * @param key
   *          the key to use to retrieve the cache later.
   * @param value
   *          the cache to save.
   */
  public void cacheNewSet(K key, V value);

  /**
   * get cache by its key.
   * 
   * @param key
   *          the key to use to retrieve cache.
   * @return
   *         object contains the cache and some metadata about this cache.
   */
  @Nullable
  public CachedSet<K, V> getCacheByKey(K key);

  /**
   * get an {@link Iterable} object iterate over entry,
   * which contains required value.
   * 
   * @param value
   *          the required value to use to lookup for keys.
   * 
   * @return all cached data with the given value entry.
   */
  @Nonnull
  public Iterable<CachedSet<K, V>> getCacheByValue(V value);

  /**
   * remove all invalidated entries from the cache to free up memory and space
   * and to speed up the process for future lookups.
   */
  public void cleanCache();

  /**
   * Remove all entries without exceptions.
   */
  public void clearCache();

  /**
   * load the cache entry and prepare them to read and write.
   */
  public void loadCache();

  /**
   * Save the current instance of cached data.
   */
  public void saveCache();


}
