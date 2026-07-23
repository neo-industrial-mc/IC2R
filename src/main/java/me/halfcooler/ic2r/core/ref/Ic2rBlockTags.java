package me.halfcooler.ic2r.core.ref;

import me.halfcooler.ic2r.core.IC2R;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Block tags used by IC2R.
 * <p>
 * Common material tags use the shared {@code c:} namespace in hierarchical form
 * ({@code c:<form>/<material>}, e.g. {@code c:ores/tin}, {@code c:storage_blocks/lead}).
 * Flat Fabric names ({@code c:tin_ores}) and loader dual-paths are not used.
 * <p>
 * IC2R-private behaviour tags stay under the {@code ic2r:} namespace.
 */
public final class Ic2rBlockTags
{
	public static final TagKey<Block> EMPTY = mod("empty");
	/** Blocks correctly mined with the IC2R wrench (1.12 HarvestTool.Wrench). */
	public static final TagKey<Block> MINEABLE_WITH_WRENCH = mod("mineable/wrench");

	public static final TagKey<Block> ORES = common("ores");
	public static final TagKey<Block> RUBBER_LOGS = common("rubber_logs");

	public static final TagKey<Block> LEAD_ORES = common("ores/lead");
	public static final TagKey<Block> SILVER_ORES = common("ores/silver");
	public static final TagKey<Block> TIN_ORES = common("ores/tin");
	public static final TagKey<Block> URANIUM_ORES = common("ores/uranium");

	public static final TagKey<Block> COPPER_BLOCKS = common("storage_blocks/copper");
	public static final TagKey<Block> GOLD_BLOCKS = common("storage_blocks/gold");
	public static final TagKey<Block> IRON_BLOCKS = common("storage_blocks/iron");
	public static final TagKey<Block> LEAD_BLOCKS = common("storage_blocks/lead");
	public static final TagKey<Block> SILVER_BLOCKS = common("storage_blocks/silver");
	public static final TagKey<Block> TIN_BLOCKS = common("storage_blocks/tin");
	public static final TagKey<Block> BRONZE_BLOCKS = common("storage_blocks/bronze");
	public static final TagKey<Block> STEEL_BLOCKS = common("storage_blocks/steel");
	public static final TagKey<Block> URANIUM_BLOCKS = common("storage_blocks/uranium");
	public static final TagKey<Block> PLUTONIUM_BLOCKS = common("storage_blocks/plutonium");

	public static void init()
	{
	}

	private static TagKey<Block> common(String path)
	{
		return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
	}

	private static TagKey<Block> mod(String path)
	{
		return TagKey.create(Registries.BLOCK, IC2R.getIdentifier(path));
	}
}
