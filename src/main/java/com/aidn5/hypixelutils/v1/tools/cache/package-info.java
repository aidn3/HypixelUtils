/**
 * small cacher library designed to cache data
 * and save them to disk for later usage
 * without the need to require big cacher library.
 * 
 * <p>
 * {@link com.aidn5.hypixelutils.v1.tools.cache.ICacher}
 * is the base interface, which is used for key-value adapters.
 * {@link com.aidn5.hypixelutils.v1.tools.cache.CachedSet}
 * is where the key and its value is saved as a one object to save.
 * 
 * <p>
 * The key-value available adapters are:
 * {@link com.aidn5.hypixelutils.v1.tools.cache.DbCacher},
 * which requires Database library/connection
 * and {@link com.aidn5.hypixelutils.v1.tools.cache.JsonCacher}
 * which requires {@link com.google.gson.Gson}.
 * 
 * <p>
 * {@code com.google.common.cache} may be a better
 * If only caching is needed without saving into File.
 * 
 * @author aidn5
 *
 * @category ICacher
 * 
 * @see com.google.common.cache.CacheBuilder
 */

package com.aidn5.hypixelutils.v1.tools.cache;
