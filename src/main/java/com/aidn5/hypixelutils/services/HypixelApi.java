package com.aidn5.hypixelutils.services;

import java.util.UUID;
import java.util.regex.Pattern;

import com.aidn5.hypixelutils.HypixelUtils;
import com.aidn5.hypixelutils.common.EventListener;
import com.aidn5.hypixelutils.common.ListenerBus;
import com.aidn5.hypixelutils.services.HypixelApi.OnHypixelApiUpdate;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HypixelApi extends ListenerBus<OnHypixelApiUpdate> {
	private static final Pattern apiPattern = Pattern.compile("^Your new API key is ([A-Za-z0-9\\-]{36})");

	private final EventsListener eventsListener = new EventsListener();
	private UUID hypixelApi = null;

	public UUID getHypixelApi() {
		return hypixelApi;
	}

	private HypixelApi() {
		// private constructor
		MinecraftForge.EVENT_BUS.register(eventsListener);
	}

	private void onStatusChanged() {
		UUID hypixelApi = getHypixelApi();
		if (hypixelApi == null) return;

		for (OnHypixelApiUpdate listener : getListeners()) {
			listener.onUpdate(hypixelApi);
		}
	}

	public interface OnHypixelApiUpdate extends EventListener {
		public void onUpdate(UUID hypixelApi);
	}

	private class EventsListener {
		@SubscribeEvent
		public void onPlayerChatReceive(ClientChatReceivedEvent event) {
			if (event == null || event.type != 0) return;
			if (!HypixelUtils.onHypixel()) return;

			onChatReceive(event.message.getUnformattedText());
		}

		private void onChatReceive(String message) {}
	}
}
