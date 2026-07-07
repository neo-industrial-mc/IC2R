package me.halfcooler.ic2r.addons.csas.init;

import net.minecraftforge.common.ForgeConfigSpec;

public final class CsasConfig
{
	public static final ForgeConfigSpec SPEC;
	public static final ForgeConfigSpec.IntValue PRODUCTION_RATE;

	static
	{
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		builder.push("general");
		PRODUCTION_RATE = builder.comment(
				"The EU generation scaling factor. The average number of ticks needed to generate one EU packet.",
				"1 is every tick, 2 is every other tick, etc.",
				"Each array still generates a whole packet when it does produce.")
			.defineInRange("productionRate", 1, 1, Integer.MAX_VALUE);
		builder.pop();
		SPEC = builder.build();
	}

	private CsasConfig()
	{
	}
}