package ic2.integration.jade;

import ic2.api.energy.profile.VoltageTier;
import ic2.core.IC2;
import ic2.core.block.comp.Energy;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.wiring.tileentity.TileEntityElectricBlock;
import ic2.core.energy.profile.ElectricalDisplay;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;

/**
 * Text lines for IC2 machines on Jade.
 * <ul>
 *   <li>Voltage (all electric tiles)</li>
 *   <li>Recipe / source power (when the energy profile has recipe power)</li>
 *   <li>Storage output + redstone mode (BatBox / MFE / …)</li>
 *   <li>Working / idle</li>
 * </ul>
 * Visibility of each line is controlled by {@code ic2-client.toml} → {@code [jade.machine]}.
 * Item inventory and fluids stay on Jade's universal providers.
 */
public enum Ic2MachineTooltipProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
{
	INSTANCE;

	public static final ResourceLocation UID = IC2.getIdentifier("machine");

	private static final String KEY_HAS_ENERGY = "ic2HasE";
	private static final String KEY_VOLTAGE = "ic2V";
	private static final String KEY_RECIPE_POWER = "ic2RP";
	private static final String KEY_MAX_SOURCE_AMPS = "ic2SrcA";
	private static final String KEY_HAS_SINK = "ic2Sink";
	private static final String KEY_HAS_SOURCE = "ic2Src";
	private static final String KEY_STORAGE = "ic2Store";
	private static final String KEY_REDSTONE_MODE = "ic2RS";
	private static final String KEY_ACTIVE = "ic2Act";

	@Override
	public ResourceLocation getUid()
	{
		return UID;
	}

	@Override
	public int getDefaultPriority()
	{
		return TooltipPosition.BODY;
	}

	@Override
	public void appendServerData(CompoundTag tag, BlockAccessor accessor)
	{
		if (!(accessor.getBlockEntity() instanceof Ic2TileEntity te))
		{
			return;
		}

		if (te.hasComponent(Energy.class))
		{
			Energy energy = te.getComponent(Energy.class);
			boolean hasSink = !energy.getSinkDirs().isEmpty();
			boolean hasSource = !energy.getSourceDirs().isEmpty();
			if (hasSink || hasSource)
			{
				tag.putBoolean(KEY_HAS_ENERGY, true);
				tag.putInt(KEY_VOLTAGE, energy.getWorkingVoltage().getIcTier());
				tag.putInt(KEY_RECIPE_POWER, energy.getElectricalProfile().getRecipePower());
				tag.putInt(KEY_MAX_SOURCE_AMPS, energy.getMaxSourceAmperage());
				tag.putBoolean(KEY_HAS_SINK, hasSink);
				tag.putBoolean(KEY_HAS_SOURCE, hasSource);
			}
		}

		if (te instanceof TileEntityElectricBlock storage)
		{
			tag.putBoolean(KEY_STORAGE, true);
			tag.putByte(KEY_REDSTONE_MODE, storage.redstoneMode);
		}

		tag.putBoolean(KEY_ACTIVE, te.getActive());
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
	{
		CompoundTag data = accessor.getServerData();
		boolean details = accessor.showDetails();

		if (data.getBoolean(KEY_HAS_ENERGY))
		{
			VoltageTier voltage = VoltageTier.fromIcTier(data.getInt(KEY_VOLTAGE));

			if (JadeConfigHelper.voltageMode().isVisible(details))
			{
				tooltip.add(ElectricalDisplay.formatVoltage(voltage));
			}

			if (JadeConfigHelper.powerMode().isVisible(details))
			{
				boolean hasSink = data.getBoolean(KEY_HAS_SINK);
				boolean hasSource = data.getBoolean(KEY_HAS_SOURCE);
				int recipePower = data.getInt(KEY_RECIPE_POWER);
				int sourceAmps = Math.max(1, data.getInt(KEY_MAX_SOURCE_AMPS));

				if (data.getBoolean(KEY_STORAGE) && hasSink && hasSource)
				{
					// Storage block: show continuous output rating.
					int euPerTick = voltage.getVoltage() * sourceAmps;
					tooltip.add(Component.translatable("ic2.electric.tooltip.output", ElectricalDisplay.formatPowerCompact(euPerTick, voltage, sourceAmps)));
				} else if (hasSource && !hasSink && recipePower > 0)
				{
					// Pure generator / source.
					tooltip.add(ElectricalDisplay.formatPower(recipePower, voltage, (double) recipePower / Math.max(1, voltage.getVoltage())));
				} else if (hasSink && recipePower > 0)
				{
					// Recipe machine (or other consumer with known demand).
					tooltip.add(Component.translatable("ic2.jade.eu_per_tick", recipePower));
				}
			}
		}

		if (data.getBoolean(KEY_STORAGE) && data.contains(KEY_REDSTONE_MODE) && JadeConfigHelper.redstoneMode().isVisible(details))
		{
			byte mode = data.getByte(KEY_REDSTONE_MODE);
			if (mode >= 0 && mode < TileEntityElectricBlock.redstoneModes)
			{
				tooltip.add(Component.translatable("ic2.EUStorage.gui.mod.redstone" + mode));
			}
		}

		if (data.contains(KEY_ACTIVE) && JadeConfigHelper.activeMode().isVisible(details))
		{
			tooltip.add(Component.translatable(data.getBoolean(KEY_ACTIVE) ? "ic2.jade.working" : "ic2.jade.idle"));
		}
	}
}
