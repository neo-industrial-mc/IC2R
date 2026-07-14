package me.halfcooler.ic2r.core.ref;

import me.halfcooler.ic2r.core.IC2R;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class Ic2rBlockTags
{
	public static final TagKey<Block> EMPTY = create("ic2r:empty", "ic2r:empty");
	/** Blocks correctly mined with the IC2R wrench (1.12 HarvestTool.Wrench). */
	public static final TagKey<Block> MINEABLE_WITH_WRENCH = create("ic2r:mineable/wrench", "ic2r:mineable/wrench");
	public static final TagKey<Block> ORES = create("c:ores", "forge:ores");
	public static final TagKey<Block> RUBBER_LOGS = create("c:rubber_logs", "forge:rubber_logs");
	public static final TagKey<Block> LEAD_ORES = create("c:lead_ores", "forge:ores/lead");
	public static final TagKey<Block> SILVER_ORES = create("c:silver_ores", "forge:ores/silver");
	public static final TagKey<Block> TIN_ORES = create("c:tin_ores", "forge:ores/tin");
	public static final TagKey<Block> COPPER_BLOCKS = create("c:copper_blocks", "forge:storage_blocks/copper");
	public static final TagKey<Block> GOLD_BLOCKS = create("c:gold_blocks", "forge:storage_blocks/gold");
	public static final TagKey<Block> IRON_BLOCKS = create("c:iron_blocks", "forge:storage_blocks/iron");
	public static final TagKey<Block> LEAD_BLOCKS = create("c:lead_blocks", "forge:storage_blocks/lead");
	public static final TagKey<Block> SILVER_BLOCKS = create("c:silver_blocks", "forge:storage_blocks/silver");
	public static final TagKey<Block> TIN_BLOCKS = create("c:tin_blocks", "forge:storage_blocks/tin");

	public static void init()
	{
	}

	private static TagKey<Block> create(String fabricName, String forgeName)
	{
		ResourceLocation id = ResourceLocation.parse(IC2R.envProxy.isFabricEnv() ? fabricName : forgeName);
		return TagKey.create(Registries.BLOCK, id);
	}
}
