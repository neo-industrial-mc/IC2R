package me.halfcooler.ic2r.core.ref;

import me.halfcooler.ic2r.core.IC2R;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Item tags used by IC2R.
 * <p>
 * Common material tags use the shared {@code c:} namespace in hierarchical form
 * ({@code c:<form>/<material>}, e.g. {@code c:ingots/tin}), matching NeoForge and
 * Fabric convention-tags since 1.21. Loader-specific dual paths and flat Fabric
 * names ({@code c:tin_ingots}) are intentionally not used.
 * <p>
 * IC2R-private tool / behaviour tags stay under the {@code ic2r:} namespace.
 */
public final class Ic2rItemTags
{
	public static final TagKey<Item> BRONZE_INGOTS = common("ingots/bronze");
	public static final TagKey<Item> LEAD_INGOTS = common("ingots/lead");
	public static final TagKey<Item> PLUTONIUM_INGOTS = common("ingots/plutonium");
	public static final TagKey<Item> SILVER_INGOTS = common("ingots/silver");
	public static final TagKey<Item> STEEL_INGOTS = common("ingots/steel");
	public static final TagKey<Item> TIN_INGOTS = common("ingots/tin");
	public static final TagKey<Item> URANIUM_INGOTS = common("ingots/uranium");

	public static final TagKey<Item> IRIDIUM_NUGGETS = common("nuggets/iridium");

	public static final TagKey<Item> COAL_DUSTS = common("dusts/coal");
	public static final TagKey<Item> COPPER_DUSTS = common("dusts/copper");
	public static final TagKey<Item> DIAMOND_DUSTS = common("dusts/diamond");
	public static final TagKey<Item> GOLD_DUSTS = common("dusts/gold");
	public static final TagKey<Item> IRON_DUSTS = common("dusts/iron");
	public static final TagKey<Item> LAPIS_DUSTS = common("dusts/lapis");
	public static final TagKey<Item> LEAD_DUSTS = common("dusts/lead");
	public static final TagKey<Item> OBSIDIAN_DUSTS = common("dusts/obsidian");
	public static final TagKey<Item> SILVER_DUSTS = common("dusts/silver");
	public static final TagKey<Item> STONE_DUSTS = common("dusts/stone");
	public static final TagKey<Item> SULFUR_DUSTS = common("dusts/sulfur");
	public static final TagKey<Item> TIN_DUSTS = common("dusts/tin");

	public static final TagKey<Item> IRON_PLATES = common("plates/iron");
	public static final TagKey<Item> GOLD_PLATES = common("plates/gold");
	public static final TagKey<Item> LEAD_PLATES = common("plates/lead");
	public static final TagKey<Item> BRONZE_PLATES = common("plates/bronze");
	public static final TagKey<Item> TIN_PLATES = common("plates/tin");
	public static final TagKey<Item> COPPER_PLATES = common("plates/copper");
	public static final TagKey<Item> LAPIS_PLATES = common("plates/lapis");
	public static final TagKey<Item> OBSIDIAN_PLATES = common("plates/obsidian");
	public static final TagKey<Item> STEEL_PLATES = common("plates/steel");

	public static final TagKey<Item> ORES = common("ores");
	public static final TagKey<Item> LEAD_ORES = common("ores/lead");
	public static final TagKey<Item> SILVER_ORES = common("ores/silver");
	public static final TagKey<Item> TIN_ORES = common("ores/tin");
	public static final TagKey<Item> URANIUM_ORES = common("ores/uranium");

	public static final TagKey<Item> LEAD_RAW_MATERIALS = common("raw_materials/lead");
	public static final TagKey<Item> TIN_RAW_MATERIALS = common("raw_materials/tin");
	public static final TagKey<Item> URANIUM_RAW_MATERIALS = common("raw_materials/uranium");

	public static final TagKey<Item> LEAD_BLOCKS = common("storage_blocks/lead");
	public static final TagKey<Item> TIN_BLOCKS = common("storage_blocks/tin");
	public static final TagKey<Item> URANIUM_BLOCKS = common("storage_blocks/uranium");
	public static final TagKey<Item> PLUTONIUM_BLOCKS = common("storage_blocks/plutonium");
	public static final TagKey<Item> BRONZE_BLOCKS = common("storage_blocks/bronze");
	public static final TagKey<Item> STEEL_BLOCKS = common("storage_blocks/steel");
	public static final TagKey<Item> SILVER_BLOCKS = common("storage_blocks/silver");

	public static final TagKey<Item> DIAMONDS = common("gems/diamond");
	public static final TagKey<Item> WOODEN_CHESTS = common("chests/wooden");
	/**
	 * Common rubber-wood item tag (paired with {@link Ic2rBlockTags#RUBBER_LOGS}).
	 */
	public static final TagKey<Item> RUBBER_LOGS = common("rubber_logs");

	/**
	 * Blacksmith forge hammer items (not the Forge loader namespace).
	 */
	public static final TagKey<Item> FORGE_HAMMERS = mod("forge_hammers");
	public static final TagKey<Item> WIRE_CUTTERS = mod("wire_cutters");
	/**
	 * Items that count as the correct tool for {@link Ic2rBlockTags#MINEABLE_WITH_WRENCH}.
	 */
	public static final TagKey<Item> WRENCHES = mod("wrenches");

	public static void init()
	{
	}

	private static TagKey<Item> common(String path)
	{
		return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
	}

	private static TagKey<Item> mod(String path)
	{
		return TagKey.create(Registries.ITEM, IC2R.getIdentifier(path));
	}
}
