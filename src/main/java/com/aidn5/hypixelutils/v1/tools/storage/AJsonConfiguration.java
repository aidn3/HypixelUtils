
package com.aidn5.hypixelutils.v1.tools.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * Abstract configuration using json.
 */
public abstract class AJsonConfiguration implements Serializable {

  /**
   * Pretty-printing GSON instance.
   */
  private static transient final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  /**
   * File for this configuration.
   */
  @Nonnull
  protected transient File file;


  protected AJsonConfiguration() {
    // private empty constructor
  }

  /**
   * Save this configuration.
   * 
   * @return This
   * @throws IOException
   *           on a writing error
   */
  public synchronized AJsonConfiguration save() throws IOException {
    String contents = GSON.toJson(this);
    Files.write(contents.getBytes(), file);
    return this;
  }

  /**
   * Load a configuration.
   * 
   * @param filePath
   *          path of the configuration
   * @param type
   *          Type of the configuration being loaded
   * 
   * @return The configuration loaded
   * 
   * @throws IOException
   *           Reading error
   * 
   * @throws JsonSyntaxException
   *           Invalid JSON
   */
  public static <T extends AJsonConfiguration> T load(
      @Nonnull File filePath, @Nonnull Class<T> type) throws IOException {

    if (!filePath.exists()) {
      throw new FileNotFoundException(
          "Configuration file \"" + filePath.toPath().getFileName() + "\" not found.");
    }

    final String contents = Files.toString(filePath, Charset.forName("UTF-8"));

    final T newConfig = GSON.fromJson(contents, type);
    newConfig.file = filePath;

    return newConfig;
  }
}
