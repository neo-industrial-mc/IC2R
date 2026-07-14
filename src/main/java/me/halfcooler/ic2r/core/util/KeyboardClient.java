package me.halfcooler.ic2r.core.util;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.proxy.SideProxyClient;

import java.util.EnumSet;
import java.util.Set;


import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class KeyboardClient extends Keyboard
{
	public static final KeyMapping altKey = new KeyMapping("ic2r.keyboard.alt_key", 342, "ic2r.name");
	public static final KeyMapping boostKey = new KeyMapping("ic2r.keyboard.boost_key", 341, "ic2r.name");
	public static final KeyMapping modeSwitchKey = new KeyMapping("ic2r.keyboard.mode_switch_key", 77, "ic2r.name");
	public static final KeyMapping sideInventoryKey = new KeyMapping("ic2r.keyboard.side_inventory_key", 67, "ic2r.name");
	public static final KeyMapping expandInfo = new KeyMapping("ic2r.keyboard.hub_expand_key", 88, "ic2r.name");
	private static boolean registeredKeys = false;
	private final Minecraft mc = Minecraft.getInstance();
	private int lastKeyState = 0;

	public KeyboardClient()
	{
		if (!registeredKeys)
		{
			registeredKeys = true;
			SideProxyClient.envProxy.registerKeyBinding(altKey);
			SideProxyClient.envProxy.registerKeyBinding(boostKey);
			SideProxyClient.envProxy.registerKeyBinding(modeSwitchKey);
			SideProxyClient.envProxy.registerKeyBinding(sideInventoryKey);
			SideProxyClient.envProxy.registerKeyBinding(expandInfo);
		}
	}

	@Override
	public void sendKeyUpdate()
	{
		Set<Keyboard.Key> keys = EnumSet.noneOf(Keyboard.Key.class);
		Screen currentScreen = SideProxyClient.mc.screen;
		if (currentScreen == null)
		{
			if (altKey.isDown())
			{
				keys.add(Keyboard.Key.alt);
			}

			if (boostKey.isDown())
			{
				keys.add(Keyboard.Key.boost);
			}

			if (this.mc.options.keyUp.isDown())
			{
				keys.add(Keyboard.Key.forward);
			}

			if (modeSwitchKey.isDown())
			{
				keys.add(Keyboard.Key.modeSwitch);
			}

			if (this.mc.options.keyJump.isDown())
			{
				keys.add(Keyboard.Key.jump);
			}

			if (sideInventoryKey.isDown())
			{
				keys.add(Keyboard.Key.sideInventory);
			}

			if (expandInfo.isDown())
			{
				keys.add(Keyboard.Key.hubMode);
			}

			for (Keyboard.IKeyWatcher watcher : this.watchers)
			{
				watcher.checkForKey(keys);
			}
		}

		int currentKeyState = Keyboard.Key.toInt(keys);
		if (currentKeyState != this.lastKeyState)
		{
			IC2R.network.get(false).initiateKeyUpdate(currentKeyState);
			super.processKeyUpdate(IC2R.sideProxy.getPlayerInstance(), currentKeyState);
			this.lastKeyState = currentKeyState;
		}
	}
}
