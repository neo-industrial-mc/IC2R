package me.halfcooler.ic2r.core.item.upgrade;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.gui.EnumCycleHandler;
import me.halfcooler.ic2r.core.gui.MouseButton;
import me.halfcooler.ic2r.core.gui.VanillaButton;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicGui;
import me.halfcooler.ic2r.core.proxy.ClientEnvProxy;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.Supplier;

public final class AdvancedUpgradeScreenFactory implements ClientEnvProxy.ScreenFactory<DynamicContainer<HandHeldAdvancedUpgrade>>
{
	public AbstractContainerScreen<DynamicContainer<HandHeldAdvancedUpgrade>> create(DynamicContainer<HandHeldAdvancedUpgrade> container, Inventory playerInventory, Component title)
	{
		final DynamicGui<HandHeldAdvancedUpgrade> gui = DynamicGui.create(container, playerInventory, title);
		if (Util.inDev())
		{
			gui.addElement(new VanillaButton(gui, 10, 62, 50, 20, new EnumCycleHandler<>(NbtSettings.VALUES, container.base.nbt)
			{
				@Override
				public void onClick(MouseButton button)
				{
					super.onClick(button);
					container.base.nbt = this.getCurrentValue();
					IC2R.network.get(false).sendHandHeldInvField(gui.getContainer(), "nbt");
				}
			}).withText("ic2r.upgrade.advancedGUI.nbt").withTooltip(new Supplier<>()
			{
				private final String NBT = Component.translatable("ic2r.upgrade.advancedGUI.nbt").getString();

				public String get()
				{
					return Component.translatable("ic2r.upgrade.advancedGUI.nbt.desc", Component.translatable(container.base.nbt.name), ChatFormatting.GRAY, Component.translatable(container.base.nbt.name + ".desc", this.NBT)).getString();
				}
			}));
		}

		return gui;
	}
}
