
package com.aidn5.hypixelutils.v1.tools;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.apache.commons.compress.utils.IOUtils;

/**
 * helps working with resources.
 * 
 * @author aidn5
 * 
 * @since 1.0
 * @version 1.0
 * 
 * @category Utils
 *
 */
public class AssetHelper {
  private AssetHelper() {
    throw new AssertionError();
  }

  /**
   * get file's content as {@link String} from the resources.
   * 
   * @param clazz
   *          any class inside the the same resource location. {@link ClassLoader}
   * @param path
   *          the resource path to the file to get.
   * 
   * @return File's contents as {@link String}
   * 
   * @throws IOException
   *           if an I/O error occurs
   * 
   * @since 1.0
   */
  @Nonnull
  public static String getString(@Nonnull Class<?> clazz, @Nonnull String path) throws IOException {
    return new String(getByteArray(clazz, path));
  }

  /**
   * get file's content's bytes from the resources.
   * 
   * @param clazz
   *          any class inside the the same resource location. {@link ClassLoader}
   * @param path
   *          the resource path to the file to get.
   * 
   * @return File's content's bytes
   * 
   * @throws IOException
   *           if an I/O error occurs
   * 
   * @since 1.0
   */
  @Nonnull
  public static byte[] getByteArray(@Nonnull Class<?> clazz, @Nonnull String path)
      throws IOException {
    InputStream is = null;
    try {
      is = clazz.getClassLoader().getResourceAsStream(path);
      return IOUtils.toByteArray(is);

    } catch (Exception e) {
      if (is != null) {
        is.close();
      }
      throw e;

    } finally {
      if (is != null) {
        is.close();
      }
    }
  }
}
