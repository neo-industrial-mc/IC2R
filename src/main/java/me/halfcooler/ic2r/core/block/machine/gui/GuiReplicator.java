package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerReplicator;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityReplicator;
import me.halfcooler.ic2r.core.gui.CustomButton;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.gui.ItemImage;
import me.halfcooler.ic2r.core.gui.TankGauge;
import me.halfcooler.ic2r.core.gui.TextLabel;
import me.halfcooler.ic2r.core.gui.dynamic.TextProvider;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiReplicator extends Ic2rGui<ContainerReplicator>
{
	public GuiReplicator(ContainerReplicator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		this.addElement(EnergyGauge.asBolt(this, 136, 84, container.base));
		this.addElement(TankGauge.createNormal(this, 27, 30, container.base.fluidTank));
		this.addElement(new ItemImage(this, 91, 17, () -> container.base.pattern).withTooltip(() ->
		{
			TileEntityReplicator te = container.base;
			if (te.pattern == null)
			{
				return null;
			}

			String uuReq = Util.toSiString(te.patternUu, 4) + Component.translatable("ic2r.generic.text.bucketUnit").getString();
			String euReq = Util.toSiString(te.patternEu, 4) + Component.translatable("ic2r.generic.text.EU").getString();
			return te.pattern.getHoverName().getString() + " UU: " + uuReq + " EU: " + euReq;
		}));
		this.addElement(new CustomButton(this, 80, 16, 9, 18, this.createEventSender(0)).withTooltip("ic2r.Replicator.gui.info.last"));
		this.addElement(new CustomButton(this, 109, 16, 9, 18, this.createEventSender(1)).withTooltip("ic2r.Replicator.gui.info.next"));
		this.addElement(new CustomButton(this, 75, 82, 16, 16, this.createEventSender(3)).withTooltip("ic2r.Replicator.gui.info.Stop"));
		this.addElement(new CustomButton(this, 92, 82, 16, 16, this.createEventSender(4)).withTooltip("ic2r.Replicator.gui.info.single"));
		this.addElement(new CustomButton(this, 109, 82, 16, 16, this.createEventSender(5)).withTooltip("ic2r.Replicator.gui.info.repeat"));
		this.addElement(TextLabel.create(this, 49, 36, 96, 16, TextProvider.of(() ->
		{
			TileEntityReplicator te = container.base;
			if (te.getMode() == TileEntityReplicator.Mode.STOPPED)
			{
				return Component.translatable("ic2r.Replicator.gui.info.Waiting").getString();
			}

			int progressUu = 0;
			int progressEu = 0;
			if (te.patternUu != 0.0)
			{
				progressUu = Math.min((int) Math.round(100.0 * te.uuProcessed / te.patternUu), 100);
				progressEu = progressUu;
			}

			return String.format("UU:%d%%  EU:%d%%  >%s", progressUu, progressEu, te.getMode() == TileEntityReplicator.Mode.SINGLE ? "" : ">");
		}), () -> container.base.getMode() == TileEntityReplicator.Mode.STOPPED ? 15461152 : 2157374, false, 4, 0, false, true));
	}

	@Override
	public ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guireplicator.png");
	}
}
