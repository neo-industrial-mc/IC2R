package me.halfcooler.ic2r.core.ref.items;

import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;
import me.halfcooler.ic2r.core.crop.ItemCrop;
import me.halfcooler.ic2r.core.item.ItemCropSeed;
import me.halfcooler.ic2r.core.item.ItemMug;
import me.halfcooler.ic2r.core.item.ItemTerraWart;
import me.halfcooler.ic2r.core.item.tool.ItemCropAnalyzer;
import me.halfcooler.ic2r.core.item.tool.ItemWeedingTrowel;
import me.halfcooler.ic2r.core.ref.Ic2rItems;

/** Domain item registrations: Crops, food, and farming items */
public final class Ic2rItemsFarming
{
	private Ic2rItemsFarming()
	{
	}

	public static final Item EMPTY_MUG = Ic2rItems.register("empty_mug", new ItemMug(new Properties().stacksTo(1), ItemMug.MugType.empty));
	public static final Item COFFEE_MUG = Ic2rItems.register("coffee_mug", new ItemMug(new Properties().stacksTo(1), ItemMug.MugType.coffee));
	public static final Item COLD_COFFEE_MUG = Ic2rItems.register("cold_coffee_mug", new ItemMug(new Properties().stacksTo(1), ItemMug.MugType.cold_coffee));
	public static final Item DARK_COFFEE_MUG = Ic2rItems.register("dark_coffee_mug", new ItemMug(new Properties().stacksTo(1), ItemMug.MugType.dark_coffee));
	public static final Item CROP_STICK = Ic2rItems.register("crop_stick", new ItemCrop(new Properties()));
	public static final Item CROP_SEED_BACK = Ic2rItems.register("crop_seed_bag", new ItemCropSeed(new Properties().stacksTo(1)));
	public static final Item CROPNALYZER = Ic2rItems.register("crop_analyzer", new ItemCropAnalyzer(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
	public static final Item WEEDING_TROWEL = Ic2rItems.register("weeding_trowel", new ItemWeedingTrowel(new Properties().stacksTo(1)));
	public static final Item COFFEE_BEANS = Ic2rItems.register("coffee_beans", new Item(new Properties()));
	public static final Item COFFEE_POWDER = Ic2rItems.register("coffee_powder", new Item(new Properties()));
	public static final Item FERTILIZER = Ic2rItems.register("fertilizer", new Item(new Properties()));
	public static final Item GRIN_POWDER = Ic2rItems.register("grin_powder", new Item(new Properties()));
	public static final Item HOPS = Ic2rItems.register("hops", new Item(new Properties()));
	public static final Item WEED = Ic2rItems.register("weed", new Item(new Properties()));
	public static final Item BOBS_YER_UNCLE_RANKS_BERRY = Ic2rItems.register("bobs_yer_uncle_ranks_berry", new Item(new Properties()));
	public static final Item OIL_BERRY = Ic2rItems.register("oil_berry", new Item(new Properties()));
	public static final Item MILK_WART = Ic2rItems.register("milk_wart", new Item(new Properties()));
	public static final Item SMALL_DIAMOND_DUST = Ic2rItems.register("small_diamond_dust", new Item(new Properties()));
	public static final Item ENDER_PEARL_DUST = Ic2rItems.register("ender_pearl_dust", new Item(new Properties()));
	public static final Item TERRA_WART = Ic2rItems.register("terra_wart", new ItemTerraWart(new Properties().food(new Builder().nutrition(0).saturationMod(1.0F).alwaysEat().build()).rarity(Rarity.RARE)));
}
