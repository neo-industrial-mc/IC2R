package me.halfcooler.ic2r.core.gui;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.IUpgradeItem;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.api.upgrade.UpgradeRegistry;
import me.halfcooler.ic2r.core.Ic2rGui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class UpgradesWidget extends GuiElement<UpgradesWidget>
{
	private static final int xCoord = 96;
	private static final int yCoord = 128;
	private static final int iWidth = 10;
	private static final int iHeight = 10;
	private final List<ItemStack> compatibleUpgrades;

	public UpgradesWidget(Ic2rGui<?> gui, int x, int y, IUpgradableBlock te)
	{
		super(gui, x, y, 10, 10);
		this.compatibleUpgrades = getCompatibleUpgrades(te);
	}

	private static List<ItemStack> getCompatibleUpgrades(IUpgradableBlock block)
	{
		List<ItemStack> ret = new ArrayList<>();
		Set<UpgradableProperty> properties = block.getUpgradableProperties();

		for (ItemStack stack : UpgradeRegistry.getUpgrades())
		{
			IUpgradeItem item = (IUpgradeItem) stack.getItem();
			if (item.isSuitableFor(stack, properties))
			{
				ret.add(stack);
			}
		}

		return ret;
	}

	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		bindCommonTexture();
		this.gui.drawTexturedRect(guiGraphics.pose(), this.x, this.y, this.width, this.height, 96.0, 128.0);
	}

	@Override
	protected List<Component> getToolTip()
	{
		List<Component> ret = super.getToolTip();
		ret.add(Component.translatable("ic2r.generic.text.upgrade"));

		for (ItemStack itemstack : this.compatibleUpgrades)
		{
			ret.add(itemstack.getHoverName());
		}

		return ret;
	}
}
