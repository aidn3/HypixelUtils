
package com.aidn5.hypixelutils.v1.common;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.tools.storage.AJsonConfiguration;
import com.google.gson.JsonSyntaxException;


public class ConfigAsset {
  private final HypixelUtils instance;

  public final HypixelUtilsConfig a;


  public ConfigAsset(HypixelUtils instance) throws JsonSyntaxException, IOException {
    this.instance = instance;
    this.a = AJsonConfiguration.load(getConfigFile("main.json"), HypixelUtilsConfig.class);
  }

  @Nonnull
  public File getConfigFile(@Nonnull String name) {
    return new File(getConfigDir(), name);
  }

  @Nonnull
  public File getConfigDir() {
    if (instance.isDefaultInstance()) {
      return getMainConfigDir();
    }

    return new File(getMainConfigDir().getAbsolutePath(), instance.modidForInstance);
  }

  @Nonnull
  public File getMainConfigDir() {
    return new File(
        instance.mc.mcDataDir + "/config/" + HypixelUtils.MODID + HypixelUtils.VERSION + "/");
  }
}
