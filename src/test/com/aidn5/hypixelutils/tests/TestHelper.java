package com.aidn5.hypixelutils.tests;

import com.aidn5.hypixelutils.HypixelUtils;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class TestHelper {
	public static void initiateForge() throws ReflectiveOperationException {
		HypixelUtils.init(new FMLInitializationEvent(new Object[] {}));
	}
}
