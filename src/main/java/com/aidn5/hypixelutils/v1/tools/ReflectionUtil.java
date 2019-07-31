
package com.aidn5.hypixelutils.v1.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.common.annotation.IHelpTools;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * A nice reflection class, using a mix of forges ReflectionHandler and some of
 * java's internal reflection implementations.
 * 
 * @author cpw
 * @author robere2
 * @author aidn5
 * 
 * @since 1.0
 * @version 1.0
 * 
 * @category Utils
 */
@IHypixelUtils
@IHelpTools(onlyStatic = true)
public class ReflectionUtil extends ReflectionHelper {

  private ReflectionUtil() {
    throw new AssertionError();
  }

  /**
   * Create new instance of a class from its private constructor.
   * 
   * @param clazz
   *          the class to create new instance of it
   * 
   * @return
   *         a new instance of the given class
   * 
   * @throws ReflectiveOperationException
   *           if any reflection error occurs
   * 
   * @since 1.0
   */
  @Nonnull
  public static <T> T newInstance(@Nonnull Class<T> clazz)
      throws ReflectiveOperationException {
    Constructor<T> c = clazz.getDeclaredConstructor();
    c.setAccessible(true);
    return c.newInstance();
  }

  /**
   * Create new instance of a class from its private constructor.
   * 
   * @param clazz
   *          the class to create new instance of
   * 
   * @param parameters
   *          the given parameters to the class
   * 
   * @return a new instance of the given class
   * 
   *         ReflectiveOperationException
   *         if any reflection error occurs
   * 
   * @since 1.0
   */
  public static <T> T newInstance(@Nonnull Class<T> clazz, @Nonnull Object... parameters)
      throws ReflectiveOperationException {

    Class<?>[] parameterTypes = new Class<?>[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      parameterTypes[i] = parameters[i].getClass();
    }

    Constructor<T> c = clazz.getDeclaredConstructor(parameterTypes);
    c.setAccessible(true);

    return c.newInstance(parameters);
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
   * @since 1.0
   */
  @Nonnull
  public static String getMcVersion() throws NoSuchFieldException, IllegalAccessException {
    getPrivateValue(ForgeVersion.class, null, "mcVersion");
    return (String) findField(ForgeVersion.class, "mcVersion").get(null);
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
   * @since 1.0
   */
  @Nonnull
  public static String getForgeVersion()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    return (String) findMethod(ForgeVersion.class, null, new String[] { "getVersion" })
        .invoke(null);

  }
}
