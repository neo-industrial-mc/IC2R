package me.halfcooler.ic2r.core.item.upgrade;

import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicGui;
import me.halfcooler.ic2r.core.proxy.ClientEnvProxy;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen factory for advanced ejector / pulling upgrade configuration.
 * NBT Match and EU Match are toggled via the dynamic GUI buttons; no extra
 * client-only widgets are required for the normal player path.
 */
public final class AdvancedUpgradeScreenFactory implements ClientEnvProxy.ScreenFactory<DynamicContainer<HandHeldAdvancedUpgrade>>
{
	public AbstractContainerScreen<DynamicContainer<HandHeldAdvancedUpgrade>> create(DynamicContainer<HandHeldAdvancedUpgrade> container, Inventory playerInventory, Component title)
	{
		return DynamicGui.create(container, playerInventory, title);
	}
}
