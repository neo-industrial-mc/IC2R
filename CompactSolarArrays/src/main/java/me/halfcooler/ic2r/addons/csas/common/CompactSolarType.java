package me.halfcooler.ic2r.addons.csas.common;

import me.halfcooler.ic2r.addons.csas.init.CsasBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum CompactSolarType implements StringRepresentable
{
	LOW_VOLTAGE(8, 1, 64, "lv_transformer"),
	MEDIUM_VOLTAGE(64, 2, 256, "mv_transformer"),
	HIGH_VOLTAGE(512, 3, 1024, "hv_transformer");

	private final int multiplier;
	private final int tier;
	private final int maxStorage;
	private final String transformerName;

	CompactSolarType(int multiplier, int tier, int maxStorage, String transformerName)
	{
		this.multiplier = multiplier;
		this.tier = tier;
		this.maxStorage = maxStorage;
		this.transformerName = transformerName;
	}

	public int getMultiplier()
	{
		return this.multiplier;
	}

	public int getTier()
	{
		return this.tier;
	}

	public int getMaxStorage()
	{
		return this.maxStorage;
	}

	public String getTransformerName()
	{
		return this.transformerName;
	}

	public static CompactSolarType fromBlock(Block block)
	{
		if (block == CsasBlocks.LOW_VOLTAGE_SOLAR_ARRAY.get())
		{
			return LOW_VOLTAGE;
		}
		if (block == CsasBlocks.MEDIUM_VOLTAGE_SOLAR_ARRAY.get())
		{
			return MEDIUM_VOLTAGE;
		}
		if (block == CsasBlocks.HIGH_VOLTAGE_SOLAR_ARRAY.get())
		{
			return HIGH_VOLTAGE;
		}
		return LOW_VOLTAGE;
	}

	@Override
	public @NotNull String getSerializedName()
	{
		return this.name().toLowerCase();
	}
}