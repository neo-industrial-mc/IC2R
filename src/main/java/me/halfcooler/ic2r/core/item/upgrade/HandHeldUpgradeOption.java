package me.halfcooler.ic2r.core.item.upgrade;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.gui.Button;
import me.halfcooler.ic2r.core.gui.VanillaButton;
import me.halfcooler.ic2r.core.item.tool.HandHeldInventory;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public abstract class HandHeldUpgradeOption extends HandHeldInventory
{
	protected final String name;

	protected HandHeldUpgradeOption(HandHeldAdvancedUpgrade upgradeGUI, String name)
	{
		super(upgradeGUI.getPlayer(), upgradeGUI.getHand(), upgradeGUI.getContainerStack(), 9);
		this.name = name;
	}

	protected CompoundTag getNBT()
	{
		return HandHeldAdvancedUpgrade.getTag(StackUtil.getOrCreateNbtData(this.containerStack), this.name);
	}

	Button<?> getBackButton(Ic2rGui<?> gui, int y)
	{
		return new VanillaButton(gui, 10, y, 50, 15, button -> IC2R.network.get(false).requestGUI(HandHeldUpgradeOption.this)).withText(Component.translatable("ic2r.upgrade.advancedGUI.back").getString());
	}
}
