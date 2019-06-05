package com.aidn5.hypixelutils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.aidn5.hypixelutils.services.HypixelApi;
import com.aidn5.hypixelutils.services.IgnoreList;
import com.aidn5.hypixelutils.services.IsOnline;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = HypixelUtils.MOD_ID, name = HypixelUtils.MOD_NAME, version = HypixelUtils.VERSION, clientSideOnly = true)
public class HypixelUtils {
	public static final String MOD_ID = "hypixelutils";
	public static final String MOD_NAME = "Hypixel Utils";
	public static final String VERSION = "1.0";

	private static HashMap<Class, Object> services = new HashMap<Class, Object>();

	private static void registerNewServices() throws ReflectiveOperationException {
		registerService(HypixelApi.class);
		registerService(IsOnline.class);
	}

	private static void registerService(Class service) throws ReflectiveOperationException {
		Constructor co = service.getDeclaredConstructor();
		co.setAccessible(true);
		services.put(service, co.newInstance());
	}

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		if (event == null) return;

		// StatisticMod.registerMod(ModConfig.MODID, ModConfig.VERSION);
	}

	@EventHandler
	public static void init(FMLInitializationEvent event) throws ReflectiveOperationException {
		registerNewServices();
		ClientCommandHandler.instance.registerCommand(new Command());
	}

	public static void cache(String s) {
		CacheBuilder<Object, Object> as = CacheBuilder.newBuilder();
		as.initialCapacity(15).expireAfterWrite(10, TimeUnit.MINUTES);
		Cache<String, Object> cacher = as.build();
	}

	public static IsOnline getIsOnlineService() {
		return (IsOnline) services.get(IsOnline.class);
	}

	public static HypixelApi getHypixelApiService() {
		return (HypixelApi) services.get(HypixelApi.class);
	}

	public static IgnoreList getIgnoreList() {
		return IgnoreList.newInstance();
	}

	public static boolean onHypixel() {
		return getIsOnlineService().isOnHypixelNetwork();
	}

	public static UUID hypixelApi() {
		return getHypixelApiService().getHypixelApi();
	}
}
