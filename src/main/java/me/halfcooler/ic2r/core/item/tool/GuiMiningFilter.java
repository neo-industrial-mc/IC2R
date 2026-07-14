package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.gui.GuiDefaultBackground;
import me.halfcooler.ic2r.core.gui.SlotGrid;
import me.halfcooler.ic2r.core.gui.VanillaButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMiningFilter extends GuiDefaultBackground<ContainerMiningFilter>
{
	public GuiMiningFilter(ContainerMiningFilter container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 215);

		this.addElement(new VanillaButton(this, 8, 10, 100, 15, button ->
		{
			this.menu.blacklist = !this.menu.blacklist;
			IC2R.network.get(false).sendContainerField(this.menu, "blacklist");
		}).withText(() ->
			Component.translatable(this.menu.blacklist ? "ic2r.MiningFilter.gui.mode.blacklist" : "ic2r.MiningFilter.gui.mode.whitelist").getString()
		));

		this.addElement(new SlotGrid(this, 7, 31, 9, 5, SlotGrid.SlotStyle.Normal));
		this.addElement(new SlotGrid(this, 7, 132, 9, 3, SlotGrid.SlotStyle.Normal));
		this.addElement(new SlotGrid(this, 7, 190, 9, 1, SlotGrid.SlotStyle.Normal));
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
	}
}
