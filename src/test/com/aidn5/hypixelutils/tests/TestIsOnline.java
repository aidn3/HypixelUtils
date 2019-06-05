package com.aidn5.hypixelutils.tests;

import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

import com.aidn5.hypixelutils.services.IsOnline;
import com.aidn5.hypixelutils.services.IsOnline.onIsOnlineupdate;

import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class TestIsOnline {
	private boolean notificationReceived = false;

	@Test
	public void listener_can_receive_updates() throws ReflectiveOperationException {
		Constructor c = IsOnline.class.getDeclaredConstructor();
		c.setAccessible(true);
		IsOnline isOnline = (IsOnline) c.newInstance();

		isOnline.register(new onIsOnlineupdate() {
			@Override
			public void onUpdate(boolean isOnlineHypixelNetwork) {
				notificationReceived = true;
			}
		});

		Field eventsListener = isOnline.getClass().getDeclaredField("eventslistener");
		eventsListener.setAccessible(true);
		Object obj = eventsListener.get(isOnline);

		Method eventMethod = obj.getClass().getDeclaredMethod("playerLoggedIn",
				FMLNetworkEvent.ClientConnectedToServerEvent.class);
		eventMethod.invoke(obj, new Object[] { null });

		if (!notificationReceived) fail("didn't receive the updates from the listener");
	}
}
