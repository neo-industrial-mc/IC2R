package me.halfcooler.ic2r.integration.jade;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

/**
 * EU buffer display for IC2R tiles on Jade.
 * <p>
 * Rendered as a custom progress bar so fill colors and label format can be
 * configured in {@code ic2r-client.toml} → {@code [jade.energy]}.
 * Jade's universal energy colors are hard-coded and cannot be changed from a
 * {@code EnergyView} provider, which is why this uses a block component instead.
 */
public enum Ic2rEnergyProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
{
	INSTANCE;

	public static final ResourceLocation UID = IC2R.getIdentifier("energy_storage");

	private static final String KEY_HAS = "ic2EHas";
	private static final String KEY_STORED = "ic2ECur";
	private static final String KEY_CAPACITY = "ic2ECap";

	@Override
	public ResourceLocation getUid()
	{
		return UID;
	}

	@Override
	public int getDefaultPriority()
	{
		// Above body text so the bar sits with other storage UIs.
		return TooltipPosition.BODY + 50;
	}

	@Override
	public void appendServerData(CompoundTag tag, BlockAccessor accessor)
	{
		if (!(accessor.getBlockEntity() instanceof Ic2rTileEntity te) || !te.hasComponent(Energy.class))
		{
			return;
		}

		Energy energy = te.getComponent(Energy.class);
		long capacity = Math.max(0L, (long) energy.getCapacity());
		if (capacity <= 0L)
		{
			return;
		}

		long stored = Math.max(0L, Math.min(capacity, (long) energy.getEnergy()));
		tag.putBoolean(KEY_HAS, true);
		tag.putLong(KEY_STORED, stored);
		tag.putLong(KEY_CAPACITY, capacity);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
	{
		CompoundTag data = accessor.getServerData();
		if (!data.getBoolean(KEY_HAS))
		{
			return;
		}
		if (!JadeConfigHelper.energyMode().isVisible(accessor.showDetails()))
		{
			return;
		}

		long capacity = data.getLong(KEY_CAPACITY);
		if (capacity <= 0L)
		{
			return;
		}

		long stored = Math.max(0L, Math.min(capacity, data.getLong(KEY_STORED)));
		float ratio = (float) stored / (float) capacity;
		Component text = JadeConfigHelper.formatEnergyText(stored, capacity, ratio);

		IElementHelper helper = IElementHelper.get();
		tooltip.add(helper.progress(ratio, text, JadeConfigHelper.energyStyle(), BoxStyle.getNestedBox(), true));
	}
}
