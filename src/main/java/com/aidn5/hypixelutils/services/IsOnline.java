package com.aidn5.hypixelutils.services;

import com.aidn5.hypixelutils.common.EventListener;
import com.aidn5.hypixelutils.common.ListenerBus;
import com.aidn5.hypixelutils.services.IsOnline.onIsOnlineupdate;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class IsOnline extends ListenerBus<onIsOnlineupdate> {
	private final EventsListener eventListeners = new EventsListener();
	private boolean isOnHypixelNetwork = false;

	private IsOnline() {
		MinecraftForge.EVENT_BUS.register(eventListeners);

		// after initializing the object we need to check
		// whether it's already On a server
		eventListeners.playerLoggedIn(null);
	}

	public boolean isOnHypixelNetwork() {
		return isOnHypixelNetwork;
	}

	private void onStatusChanged() {
		boolean is = isOnHypixelNetwork();

		for (onIsOnlineupdate listener : getListeners()) {
			listener.onUpdate(is);
		}
	}

	public interface onIsOnlineupdate extends EventListener {
		public void onUpdate(boolean isOnlineHypixelNetwork);
	}

	private class EventsListener {
		@SubscribeEvent
		public void playerLoggedIn(FMLNetworkEvent.ClientConnectedToServerEvent event) {
			try {
				System.out.println(event.getClass().getSimpleName());
				String serverIp = Minecraft.getMinecraft().getCurrentServerData().serverIP;
				isOnHypixelNetwork = serverIp.toLowerCase().contains("hypixel.net");
				onStatusChanged();
			} catch (Exception e) {
				if (event != null) e.printStackTrace();
			}
		}

		@SubscribeEvent
		public void onLoggedOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
			if (event == null) return; // avoid abuse
			// on logout flag the client as not on hypixel network
			isOnHypixelNetwork = false;

			onStatusChanged();
		}
	}
}
