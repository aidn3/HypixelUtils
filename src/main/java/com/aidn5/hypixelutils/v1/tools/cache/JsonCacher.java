
package com.aidn5.hypixelutils.v1.tools.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.common.annotation.IHelpTools;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Cacher based on key-value supports saving data in File as JSON.
 * 
 * @author aidn5
 *
 * @param <T>
 *          the object to convert to JSON and save.
 * 
 * @category ICacher
 * 
 * @since 1.0
 * @version 1.0
 * 
 * @see ICacher
 */
@IHelpTools
@IHypixelUtils
public class JsonCacher<T> implements ICacher<String, T> {
  private HashMap<String, CachedSet<String, T>> cachedSets = new HashMap<>(100);

  private final File cacheFilePath;
  private final int duration;
  private final TimeUnit durationUnit;

  /**
   * create new instance of {@link JsonCacher}.
   * 
   * <p>
   * if <code>cacheFilePath</code> is null,
   * {@link #saveCache()} and {@link #loadCache()} will be disabled.
   * 
   * @param cacheFilePath
   *          the path where the cached saved.
   * @param duration
   *          the length of time after an entry is created
   *          that it should be removed
   * @param durationUnit
   *          the unit that {@code duration} is expressed in
   * 
   */
  public JsonCacher(@Nullable File cacheFilePath, int duration, TimeUnit durationUnit) {
    this.cacheFilePath = cacheFilePath;
    this.duration = duration;
    this.durationUnit = durationUnit;
  }

  @Override
  @Nullable
  public CachedSet<String, T> getCacheByKey(String key) {
    final long cacheTime = System.currentTimeMillis() - this.durationUnit.toMillis(duration);

    CachedSet<String, T> cachedSet = this.cachedSets.get(key);

    if (cachedSet != null && cacheTime < cachedSet.getTime()) {
      return cachedSet;
    }

    return null;
  }

  @Override
  @Nonnull
  public Iterable<CachedSet<String, T>> getCacheByValue(@Nonnull final T value) {
    final long cacheTime = System.currentTimeMillis() - this.durationUnit.toMillis(duration);
    final Iterator<CachedSet<String, T>> iterator = cachedSets.values().iterator();

    return new Iterable<CachedSet<String, T>>() {

      @Override
      public Iterator<CachedSet<String, T>> iterator() {
        return new Iterator<CachedSet<String, T>>() {
          private CachedSet<String, T> nextValue = null;

          @Override
          public boolean hasNext() {
            while (iterator.hasNext()) {
              CachedSet<String, T> cacheSet = iterator.next();
              if (cacheTime < cacheSet.getTime()) {
                if (cacheSet.getValue().equals(value)) {
                  nextValue = cacheSet;
                  return true;
                }

              } else {
                iterator.remove();
              }
            }

            return false;
          }

          @Override
          public CachedSet<String, T> next() {
            return nextValue;
          }
        };
      }
    };
  }

  @Override
  public void clearCache() {
    cachedSets.clear();
  }

  @Override
  public void cleanCache() {
    final long cacheTime = System.currentTimeMillis() - this.durationUnit.toMillis(duration);

    Iterator<Entry<String, CachedSet<String, T>>> iterator = cachedSets.entrySet().iterator();

    while (iterator.hasNext()) {
      CachedSet<String, T> cacheSet = iterator.next().getValue();

      if (cacheSet != null && cacheTime > cacheSet.getTime()) {
        iterator.remove();
      }
    }
  }

  @Override
  public void loadCache() {
    if (this.cacheFilePath == null || !this.cacheFilePath.exists()) {
      synchronized (this) {
        this.cachedSets = new HashMap<>(100);
      }
      return;
    }

    try {
      synchronized (cacheFilePath) {
        FileInputStream fis = new FileInputStream(this.cacheFilePath);

        byte[] data = new byte[(int) this.cacheFilePath.length()];
        fis.read(data);
        fis.close();


        String string = new String(data, "UTF-8");

        Type listType = new TypeToken<ArrayList<CachedSet<String, T>>>() {}.getType();
        Gson gson = new Gson();

        this.cachedSets = gson.fromJson(string, listType);
      }
    } catch (IOException e) {
      e.printStackTrace();

      this.cachedSets = new HashMap<>(100);
    }
  }

  @Override
  public void saveCache() {
    if (this.cacheFilePath == null) {
      return;
    }

    try {
      synchronized (cacheFilePath) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<CachedSet<String, T>>>() {}.getType();
        String json = gson.toJson(this.cachedSets, listType);

        OutputStream out = new FileOutputStream(this.cacheFilePath);
        out.write(json.getBytes());
        out.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void cacheNewSet(String key, T value) {
    this.cachedSets.put(key, new CachedSet<String, T>(key, value, System.currentTimeMillis()));
  }

}
