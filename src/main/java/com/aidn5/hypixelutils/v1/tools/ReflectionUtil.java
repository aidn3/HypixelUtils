
package com.aidn5.hypixelutils.v1.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * A nice reflection class, using a mix of forges ReflectionHandler and some of
 * java's internal reflection implementations.
 * 
 * @author cpw
 * @author Buggfroggy
 * 
 * @since 1.0
 * @version 1.0
 * 
 * @category Utils
 */
public class ReflectionUtil extends ReflectionHelper {

  private ReflectionUtil() {
    throw new AssertionError();
  }

  /**
   * Get field "name" from class "clazz" and
   * handle any universal modifications to it.
   * 
   * @param clazz
   *          Class to get field from
   * @param name
   *          Name of field to get
   * @return The field
   * 
   * @throws NoSuchFieldException
   *           The field doesn't exist
   * 
   * @author Buggfroggy
   * @since 1.0
   */
  @Nonnull
  public static Field getField(@Nonnull Class<?> clazz, @Nonnull String name)
      throws NoSuchFieldException {
    Field field = clazz.getField(name);
    field.setAccessible(true);
    return field;
  }

  /**
   * Get method "name" from class "clazz" and
   * handle any universal modifications to it.
   * 
   * @param clazz
   *          Clazz to get the method from
   * @param name
   *          Name of the method to get
   * @return The method
   * 
   * @throws NoSuchMethodException
   *           The method doesn't exist
   * 
   * @author Buggfroggy
   * @since 1.0
   */
  @Nonnull
  public static Method getMethod(@Nonnull Class<?> clazz, @Nonnull String name)
      throws NoSuchMethodException {
    Method method = clazz.getMethod(name);
    method.setAccessible(true);
    return method;
  }

  /**
   * Create new instance of a class from its private constructor.
   * 
   * @param clazz
   *          the class to create new instance of it
   * 
   * @return
   *         the new instance of the given class
   * 
   * @throws ReflectiveOperationException
   *           if any reflection error occurs
   * 
   * @author aidn5
   * @since 1.0
   */
  @Nonnull
  public static <T> T newInstance(@Nonnull Class<T> clazz)
      throws ReflectiveOperationException {
    Constructor c = clazz.getDeclaredConstructor();
    c.setAccessible(true);
    return (T) c.newInstance();
  }

  /**
   * Get the current Minecraft version.
   * 
   * @return String containing the Minecraft version
   * 
   * @throws NoSuchFieldException
   *           Minecraft version couldn't be found for some reason
   * @throws IllegalAccessException
   *           Couldn't access the minecraft version for some reason
   * 
   * @author Buggfroggy
   * @since 1.0
   */
  @Nonnull
  public static String getMcVersion() throws NoSuchFieldException, IllegalAccessException {
    return (String) getField(ForgeVersion.class, "mcVersion").get(null);
  }

  /**
   * Get the current Forge version.
   * 
   * @return String containing the Forge version
   * 
   * @throws InvocationTargetException
   *           Exception caused by getVersion method
   * @throws IllegalAccessException
   *           Couldn't access the Forge version for some reason
   * @throws NoSuchMethodException
   *           Forge version method doesn't exist for some reason
   * 
   * @author Buggfroggy
   * @since 1.0
   */
  @Nonnull
  public static String getForgeVersion()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    return (String) getMethod(ForgeVersion.class, "getVersion").invoke(null);
  }
}
