package me.halfcooler.ic2r.core.crop;

import me.halfcooler.ic2r.api.crops.BaseSeed;
import me.halfcooler.ic2r.api.crops.CropCard;
import me.halfcooler.ic2r.api.crops.CropProperties;
import me.halfcooler.ic2r.api.crops.Crops;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.crop.cropcard.CropBaseMetalCommon;
import me.halfcooler.ic2r.core.crop.cropcard.CropBaseMetalUncommon;
import me.halfcooler.ic2r.core.crop.cropcard.CropBaseMushroom;
import me.halfcooler.ic2r.core.crop.cropcard.CropBaseSapling;
import me.halfcooler.ic2r.core.crop.cropcard.CropBeetroots;
import me.halfcooler.ic2r.core.crop.cropcard.CropCarrots;
import me.halfcooler.ic2r.core.crop.cropcard.CropCocoa;
import me.halfcooler.ic2r.core.crop.cropcard.CropCoffee;
import me.halfcooler.ic2r.core.crop.cropcard.CropColorFlower;
import me.halfcooler.ic2r.core.crop.cropcard.CropEating;
import me.halfcooler.ic2r.core.crop.cropcard.CropFlax;
import me.halfcooler.ic2r.core.crop.cropcard.CropHops;
import me.halfcooler.ic2r.core.crop.cropcard.CropMelon;
import me.halfcooler.ic2r.core.crop.cropcard.CropNetherWart;
import me.halfcooler.ic2r.core.crop.cropcard.CropPotato;
import me.halfcooler.ic2r.core.crop.cropcard.CropPumpkin;
import me.halfcooler.ic2r.core.crop.cropcard.CropRedWheat;
import me.halfcooler.ic2r.core.crop.cropcard.CropReed;
import me.halfcooler.ic2r.core.crop.cropcard.CropStickyReed;
import me.halfcooler.ic2r.core.crop.cropcard.CropTerraWart;
import me.halfcooler.ic2r.core.crop.cropcard.CropVenomilia;
import me.halfcooler.ic2r.core.crop.cropcard.CropWeed;
import me.halfcooler.ic2r.core.crop.cropcard.CropWheat;
import me.halfcooler.ic2r.core.crop.cropcard.GenericCropCard;
import me.halfcooler.ic2r.core.proxy.EnvProxy;
import me.halfcooler.ic2r.core.ref.Ic2rBlockTags;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rItems;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class Ic2rCrops extends Crops
{
	public static CropCard cropWheat = new CropWheat(Ic2rCropType.wheat);
	public static CropCard cropPumpkin = new CropPumpkin(Ic2rCropType.pumpkin);
	public static CropCard cropMelon = new CropMelon(Ic2rCropType.melon);
	public static CropCard cropYellowFlower = new CropColorFlower(Ic2rCropType.dandelion, Ic2rBlocks.DANDELION_CROP, new String[] { "Yellow", "Flower" }, DyeColor.YELLOW);
	public static CropCard cropRedFlower = new CropColorFlower(Ic2rCropType.poppy, Ic2rBlocks.POPPY_CROP, new String[] { "Red", "Flower", "Rose" }, DyeColor.RED);
	public static CropCard cropBlackFlower = new CropColorFlower(Ic2rCropType.blackthorn, Ic2rBlocks.BLACKTHORN_CROP, new String[] { "Black", "Flower", "Rose" }, DyeColor.BLACK);
	public static CropCard cropPurpleFlower = new CropColorFlower(Ic2rCropType.tulip, Ic2rBlocks.TULIP_CROP, new String[] { "Purple", "Flower", "Tulip" }, DyeColor.PURPLE);
	public static CropCard cropBlueFlower = new CropColorFlower(Ic2rCropType.cyazint, Ic2rBlocks.CYAZINT_CROP, new String[] { "Blue", "Flower" }, DyeColor.CYAN);
	public static CropCard cropVenomilia = new CropVenomilia(Ic2rCropType.venomilia);
	public static CropCard cropReed = new CropReed(Ic2rCropType.reed);
	public static CropCard cropStickyReed = new CropStickyReed(Ic2rCropType.stickyReed);
	public static CropCard cropCocoa = new CropCocoa(Ic2rCropType.cocoa);
	public static CropCard cropFlax = new CropFlax(Ic2rCropType.flax);
	public static CropCard cropRedMushroom = new CropBaseMushroom(Ic2rCropType.redMushroom, Ic2rBlocks.RED_MUSHROOM_CROP, new String[] { "Red", "Food", "Mushroom" }, new ItemStack(Blocks.RED_MUSHROOM));
	public static CropCard cropBrownMushroom = new CropBaseMushroom(Ic2rCropType.brownMushroom, Ic2rBlocks.BROWN_MUSHROOM_CROP, new String[] { "Brown", "Food", "Mushroom" }, new ItemStack(Blocks.BROWN_MUSHROOM));
	public static CropCard cropNetherWart = new CropNetherWart(Ic2rCropType.netherWart);
	public static CropCard cropTerraWart = new CropTerraWart(Ic2rCropType.terraWart);
	public static CropCard cropOakSapling = new CropBaseSapling(Ic2rCropType.oakSapling, Ic2rBlocks.OAK_SAPLING_CROP, "seedling", new ItemStack(Blocks.OAK_LOG), new ItemStack(Blocks.OAK_SAPLING));
	public static CropCard cropSpruceSapling = new CropBaseSapling(Ic2rCropType.spruceSapling, Ic2rBlocks.SPRUCE_SAPLING_CROP, "seedling", new ItemStack(Blocks.SPRUCE_LOG), new ItemStack(Blocks.SPRUCE_SAPLING));
	public static CropCard cropBirchSapling = new CropBaseSapling(Ic2rCropType.birchSapling, Ic2rBlocks.BIRCH_SAPLING_CROP, "seedling", new ItemStack(Blocks.BIRCH_LOG), new ItemStack(Blocks.BIRCH_SAPLING));
	public static CropCard cropJungleSapling = new CropBaseSapling(Ic2rCropType.jungleSapling, Ic2rBlocks.JUNGLE_SAPLING_CROP, "seedling", new ItemStack(Blocks.JUNGLE_LOG), new ItemStack(Blocks.JUNGLE_SAPLING));
	public static CropCard cropAcaciaSapling = new CropBaseSapling(Ic2rCropType.acaciaSapling, Ic2rBlocks.ACACIA_SAPLING_CROP, "seedling", new ItemStack(Blocks.ACACIA_LOG), new ItemStack(Blocks.ACACIA_SAPLING));
	public static CropCard cropDarkOakSapling = new CropBaseSapling(Ic2rCropType.darkOakSapling, Ic2rBlocks.DARK_OAK_SAPLING_CROP, "seedling", new ItemStack(Blocks.DARK_OAK_LOG), new ItemStack(Blocks.DARK_OAK_SAPLING));
	public static CropCard cropFerru = new CropBaseMetalCommon(Ic2rCropType.ferru, Ic2rBlocks.FERRU_CROP, new String[] { "Gray", "Leaves", "Metal" }, Arrays.asList(BlockTags.IRON_ORES, Ic2rBlockTags.IRON_BLOCKS), new ItemStack(Ic2rItems.SMALL_IRON_DUST));
	public static CropCard cropCyprium = new CropBaseMetalCommon(Ic2rCropType.cyprium, Ic2rBlocks.CYPRIUM_CROP, new String[] { "Orange", "Leaves", "Metal" }, Arrays.asList(BlockTags.COPPER_ORES, Ic2rBlockTags.COPPER_BLOCKS), new ItemStack(Ic2rItems.SMALL_COPPER_DUST));
	public static CropCard cropStagnium = new CropBaseMetalCommon(Ic2rCropType.stagnium, Ic2rBlocks.STAGNIUM_CROP, new String[] { "Shiny", "Leaves", "Metal" }, Arrays.asList(Ic2rBlockTags.TIN_ORES, Ic2rBlockTags.TIN_BLOCKS), new ItemStack(Ic2rItems.SMALL_TIN_DUST));
	public static CropCard cropPlumbiscus = new CropBaseMetalCommon(Ic2rCropType.plumbiscus, Ic2rBlocks.PLUMBISCUS_CROP, new String[] { "Dense", "Leaves", "Metal" }, Arrays.asList(Ic2rBlockTags.LEAD_ORES, Ic2rBlockTags.LEAD_BLOCKS), new ItemStack(Ic2rItems.SMALL_LEAD_DUST));
	public static CropCard cropAurelia = new CropBaseMetalUncommon(Ic2rCropType.aurelia, Ic2rBlocks.AURELIA_CROP, new String[] { "Gold", "Leaves", "Metal" }, Arrays.asList(BlockTags.GOLD_ORES, Ic2rBlockTags.GOLD_BLOCKS), new ItemStack(Ic2rItems.SMALL_GOLD_DUST));
	public static CropCard cropShining = new CropBaseMetalUncommon(Ic2rCropType.shining, Ic2rBlocks.SHINING_CROP, new String[] { "Silver", "Leaves", "Metal" }, Arrays.asList(Ic2rBlockTags.SILVER_ORES, Ic2rBlockTags.SILVER_BLOCKS), new ItemStack(Ic2rItems.SMALL_SILVER_DUST));
	public static CropCard cropRedWheat = new CropRedWheat(Ic2rCropType.redWheat);
	public static CropCard cropCoffee = new CropCoffee(Ic2rCropType.coffee);
	public static CropCard cropHops = new CropHops(Ic2rCropType.hops);
	public static CropCard cropCarrots = new CropCarrots(Ic2rCropType.carrots);
	public static CropCard cropPotato = new CropPotato(Ic2rCropType.potato);
	public static CropCard cropEatingPlant = new CropEating(Ic2rCropType.eatingPlant);
	public static CropCard cropBeetroots = new CropBeetroots(Ic2rCropType.beetroots);
	public static CropCard cropBlazereed = GenericCropCard.create(Ic2rCropType.blazereed, Ic2rBlocks.BLAZEREED_CROP).setDiscoveredBy("Mr. Brain").setProperties(new CropProperties(6, 0, 4, 1, 0, 0)).setAttributes(new String[] { "Fire", "Blaze", "Reed", "Sulfur" }).setMaxSize(4).setDrops(new ItemStack(Items.BLAZE_POWDER)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.BLAZE_ROD), new ItemStack(Ic2rItems.SULFUR_DUST) }).setAfterHarvestSize(1);
	public static CropCard cropBobsYerUncleRanksBerries = GenericCropCard.create(Ic2rCropType.bobsYerUncleRanksBerries, Ic2rBlocks.BOBS_YER_UNCLE_RANKS_BERRIES_CROP).setDiscoveredBy("GenerikB").setProperties(new CropProperties(11, 4, 0, 8, 2, 9)).setAttributes(new String[] { "Shiny", "Vine", "Emerald", "Berylium", "Crystal" }).setMaxSize(4).setDrops(new ItemStack(Ic2rItems.BOBS_YER_UNCLE_RANKS_BERRY)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.EMERALD) }).setAfterHarvestSize(1);
	public static CropCard cropCorium = GenericCropCard.create(Ic2rCropType.corium, Ic2rBlocks.CORIUM_CROP).setDiscoveredBy("Gregorius Techneticies").setProperties(new CropProperties(6, 0, 2, 3, 1, 0)).setAttributes(new String[] { "Cow", "Silk", "Vine" }).setMaxSize(4).setDrops(new ItemStack(Items.LEATHER)).setAfterHarvestSize(1);
	public static CropCard cropCorpsePlant = GenericCropCard.create(Ic2rCropType.corpse_plant, Ic2rBlocks.CORPSE_PLANT_CROP).setDiscoveredBy("Mr. Kenny").setProperties(new CropProperties(5, 0, 2, 1, 0, 3)).setAttributes(new String[] { "Toxic", "Undead", "Vine", "Edible", "Rotten" }).setMaxSize(4).setDrops(new ItemStack(Items.ROTTEN_FLESH)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.BONE), new ItemStack(Items.BONE_MEAL), new ItemStack(Items.BONE_MEAL) }).setAfterHarvestSize(1);
	public static CropCard cropCreeperWeed = GenericCropCard.create(Ic2rCropType.creeper_weed, Ic2rBlocks.CREEPER_WEED_CROP).setDiscoveredBy("General Spaz").setProperties(new CropProperties(7, 3, 0, 5, 1, 3)).setAttributes(new String[] { "Creeper", "Vine", "Explosive", "Fire", "Sulfur", "Saltpeter", "Coal" }).setMaxSize(4).setDrops(new ItemStack(Items.GUNPOWDER)).setAfterHarvestSize(1);
	public static CropCard cropDiareed = GenericCropCard.create(Ic2rCropType.diareed, Ic2rBlocks.DIAREED_CROP).setDiscoveredBy("Diareed").setProperties(new CropProperties(12, 5, 0, 10, 2, 10)).setAttributes(new String[] { "Fire", "Shiny", "Reed", "Coal", "Diamond", "Crystal" }).setMaxSize(4).setDrops(new ItemStack(Ic2rItems.SMALL_DIAMOND_DUST)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.DIAMOND) }).setAfterHarvestSize(1);
	public static CropCard cropEggPlant = GenericCropCard.create(Ic2rCropType.egg_plant, Ic2rBlocks.EGG_PLANT_CROP).setDiscoveredBy("Link").setProperties(new CropProperties(6, 0, 4, 1, 0, 0)).setAttributes(new String[] { "Chicken", "Egg", "Edible", "Feather", "Flower", "Addictive" }).setMaxSize(3).setDrops(new ItemStack(Items.EGG)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.CHICKEN), new ItemStack(Items.FEATHER), new ItemStack(Items.FEATHER), new ItemStack(Items.FEATHER) }).setGrowthSpeed(900).setAfterHarvestSize(2);
	public static CropCard cropEnderBlossom = GenericCropCard.create(Ic2rCropType.ender_blossom, Ic2rBlocks.ENDER_BLOSSOM_CROP).setDiscoveredBy("RichardG").setProperties(new CropProperties(10, 5, 0, 2, 1, 6)).setAttributes(new String[] { "Ender", "Flower", "Shiny" }).setMaxSize(4).setDrops(new ItemStack(Ic2rItems.ENDER_PEARL_DUST)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.ENDER_EYE) }).setAfterHarvestSize(1);
	public static CropCard cropMeatRose = GenericCropCard.create(Ic2rCropType.meat_rose, Ic2rBlocks.MEAT_ROSE_CROP).setDiscoveredBy("VintageBeef").setProperties(new CropProperties(7, 0, 4, 1, 3, 0)).setAttributes(new String[] { "Edible", "Flower", "Cow", "Chicken", "Pig", "Sheep" }).setMaxSize(4).setDrops(new ItemStack(Items.PINK_DYE)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.BEEF), new ItemStack(Items.PORKCHOP), new ItemStack(Items.CHICKEN), new ItemStack(Items.MUTTON) }).setGrowthSpeed(1500).setAfterHarvestSize(1);
	public static CropCard cropMilkWart = GenericCropCard.create(Ic2rCropType.milk_wart, Ic2rBlocks.MILK_WART_CROP).setDiscoveredBy("Mr. Brain").setProperties(new CropProperties(6, 0, 3, 0, 1, 0)).setAttributes(new String[] { "Edible", "Milk", "Cow" }).setMaxSize(3).setDrops(new ItemStack(Ic2rItems.MILK_WART)).setGrowthSpeed(900).setAfterHarvestSize(1).addBaseSeed(new ItemStack(Ic2rItems.MILK_WART));
	public static CropCard cropOilBerries = GenericCropCard.create(Ic2rCropType.oil_berries, Ic2rBlocks.OIL_BERRIES_CROP).setDiscoveredBy("Spacetoad").setProperties(new CropProperties(9, 6, 1, 2, 1, 12)).setAttributes(new String[] { "Fire", "Dark", "Reed", "Rotten", "Coal", "Oil" }).setMaxSize(3).setDrops(new ItemStack(Ic2rItems.OIL_BERRY)).setAfterHarvestSize(1);
	public static CropCard cropSlimePlant = GenericCropCard.create(Ic2rCropType.slime_plant, Ic2rBlocks.SLIME_PLANT_CROP).setDiscoveredBy("Neowulf").setProperties(new CropProperties(6, 3, 0, 0, 0, 2)).setAttributes(new String[] { "Slime", "Bouncy", "Sticky", "Bush" }).setMaxSize(4).setDrops(new ItemStack(Items.SLIME_BALL)).setAfterHarvestSize(2);
	public static CropCard cropSpidernip = GenericCropCard.create(Ic2rCropType.spidernip, Ic2rBlocks.SPIDERNIP_CROP).setDiscoveredBy("Mr. Kenny").setProperties(new CropProperties(4, 2, 1, 4, 1, 3)).setAttributes(new String[] { "Toxic", "Silk", "Spider", "Flower", "Ingredient", "Addictive" }).setMaxSize(4).setDrops(new ItemStack(Items.STRING)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.SPIDER_EYE), new ItemStack(Blocks.COBWEB) }).setGrowthSpeed(600).setAfterHarvestSize(1);
	public static CropCard cropTearstalks = GenericCropCard.create(Ic2rCropType.tearstalks, Ic2rBlocks.TEARSTALKS_CROP).setDiscoveredBy("Neowulf").setProperties(new CropProperties(8, 1, 2, 0, 0, 0)).setAttributes(new String[] { "Healing", "Nether", "Ingredient", "Reed", "Ghast" }).setMaxSize(4).setDrops(new ItemStack(Items.GHAST_TEAR)).setAfterHarvestSize(1);
	public static CropCard cropWithereed = GenericCropCard.create(Ic2rCropType.withereed, Ic2rBlocks.WITHEREED_CROP).setDiscoveredBy("CovertJaguar").setProperties(new CropProperties(8, 2, 0, 4, 1, 3)).setAttributes(new String[] { "Fire", "Undead", "Reed", "Coal", "Rotten", "Wither" }).setMaxSize(4).setDrops(new ItemStack(Ic2rItems.COAL_DUST)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.COAL), new ItemStack(Items.COAL) }).setAfterHarvestSize(1);
	private final Map<EnvProxy.BiomeType, Integer> humidityBiomeTypeBonus = new IdentityHashMap<>();
	private final Map<EnvProxy.BiomeType, Integer> nutrientBiomeTypeBonus = new IdentityHashMap<>();
	private final Map<ItemStack, BaseSeed> baseSeeds = new HashMap<>();
	private final Map<String, Map<String, CropCard>> cropMap = new HashMap<>();

	public static void init()
	{
		Crops.instance = new Ic2rCrops();
		Crops.weed = new CropWeed(Ic2rCropType.weed);
		Crops.instance.addBiomehumidityBonus(EnvProxy.BiomeType.WATER, 10);
		Crops.instance.addBiomehumidityBonus(EnvProxy.BiomeType.WET, 10);
		Crops.instance.addBiomehumidityBonus(EnvProxy.BiomeType.DRY, -10);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.JUNGLE, 10);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.SWAMP, 10);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.MUSHROOM, 5);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.FOREST, 5);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.RIVER, 2);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.PLAINS, 0);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.SAVANNA, -2);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.HILLS, -5);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.MOUNTAIN, -5);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.WASTELAND, -8);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.END, -10);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.NETHER, -10);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.DEAD, -10);
		registerCrops();
		registerBaseSeeds();
	}

	public static void registerCrops()
	{
		Crops.instance.registerCrop(weed);
		Crops.instance.registerCrop(cropWheat);
		Crops.instance.registerCrop(cropPumpkin);
		Crops.instance.registerCrop(cropMelon);
		Crops.instance.registerCrop(cropYellowFlower);
		Crops.instance.registerCrop(cropRedFlower);
		Crops.instance.registerCrop(cropBlackFlower);
		Crops.instance.registerCrop(cropPurpleFlower);
		Crops.instance.registerCrop(cropBlueFlower);
		Crops.instance.registerCrop(cropVenomilia);
		Crops.instance.registerCrop(cropReed);
		Crops.instance.registerCrop(cropStickyReed);
		Crops.instance.registerCrop(cropCocoa);
		Crops.instance.registerCrop(cropFlax);
		Crops.instance.registerCrop(cropFerru);
		Crops.instance.registerCrop(cropAurelia);
		Crops.instance.registerCrop(cropRedWheat);
		Crops.instance.registerCrop(cropNetherWart);
		Crops.instance.registerCrop(cropTerraWart);
		Crops.instance.registerCrop(cropCoffee);
		Crops.instance.registerCrop(cropHops);
		Crops.instance.registerCrop(cropCarrots);
		Crops.instance.registerCrop(cropPotato);
		Crops.instance.registerCrop(cropRedMushroom);
		Crops.instance.registerCrop(cropBrownMushroom);
		Crops.instance.registerCrop(cropEatingPlant);
		Crops.instance.registerCrop(cropCyprium);
		Crops.instance.registerCrop(cropStagnium);
		Crops.instance.registerCrop(cropPlumbiscus);
		Crops.instance.registerCrop(cropShining);
		Crops.instance.registerCrop(cropBeetroots);
		Crops.instance.registerCrop(cropOakSapling);
		Crops.instance.registerCrop(cropSpruceSapling);
		Crops.instance.registerCrop(cropBirchSapling);
		Crops.instance.registerCrop(cropJungleSapling);
		Crops.instance.registerCrop(cropAcaciaSapling);
		Crops.instance.registerCrop(cropDarkOakSapling);
		Crops.instance.registerCrop(cropBlazereed);
		Crops.instance.registerCrop(cropBobsYerUncleRanksBerries);
		Crops.instance.registerCrop(cropCorium);
		Crops.instance.registerCrop(cropCorpsePlant);
		Crops.instance.registerCrop(cropCreeperWeed);
		Crops.instance.registerCrop(cropDiareed);
		Crops.instance.registerCrop(cropEggPlant);
		Crops.instance.registerCrop(cropEnderBlossom);
		Crops.instance.registerCrop(cropMeatRose);
		Crops.instance.registerCrop(cropMilkWart);
		Crops.instance.registerCrop(cropOilBerries);
		Crops.instance.registerCrop(cropSlimePlant);
		Crops.instance.registerCrop(cropSpidernip);
		Crops.instance.registerCrop(cropTearstalks);
		Crops.instance.registerCrop(cropWithereed);
	}

	public static void registerBaseSeeds()
	{
		Crops.instance.registerBaseSeed(new ItemStack(Items.WHEAT_SEEDS), cropWheat, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.PUMPKIN_SEEDS), cropPumpkin, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.MELON_SEEDS), cropMelon, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.NETHER_WART), cropNetherWart, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Ic2rItems.TERRA_WART), cropTerraWart, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Ic2rItems.COFFEE_BEANS), cropCoffee, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.SUGAR_CANE), cropReed, 0, 3, 0, 2);
		Crops.instance.registerBaseSeed(new ItemStack(Items.COCOA_BEANS), cropCocoa, 0, 0, 0, 0);
		Crops.instance.registerBaseSeed(new ItemStack(Items.POPPY, 4), cropRedFlower, 3, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.DANDELION, 4), cropYellowFlower, 3, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.CARROT), cropCarrots, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.POTATO, 1), cropPotato, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.BROWN_MUSHROOM, 4), cropBrownMushroom, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.RED_MUSHROOM, 4), cropRedMushroom, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.CACTUS), cropEatingPlant, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.BEETROOT_SEEDS), cropBeetroots, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.OAK_SAPLING), cropOakSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.SPRUCE_SAPLING), cropSpruceSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.BIRCH_SAPLING), cropBirchSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.JUNGLE_SAPLING), cropJungleSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.ACACIA_SAPLING), cropAcaciaSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.DARK_OAK_SAPLING), cropDarkOakSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Ic2rItems.MILK_WART), cropMilkWart, 0, 1, 1, 1);
	}

	@Override
	public void addBiomenutrientsBonus(EnvProxy.BiomeType type, int nutrientsBonus)
	{
		this.nutrientBiomeTypeBonus.put(type, nutrientsBonus);
	}

	@Override
	public void addBiomehumidityBonus(EnvProxy.BiomeType type, int humidityBonus)
	{
		this.humidityBiomeTypeBonus.put(type, humidityBonus);
	}

	@Override
	public int getHumidityBiomeBonus(Holder<Biome> biome)
	{
		int ret = 0;

		for (EnvProxy.BiomeType type : IC2R.envProxy.getBiomeTypes(biome))
		{
			Integer val = this.humidityBiomeTypeBonus.get(type);
			if (val != null && val > ret)
			{
				ret = val;
			}
		}

		return ret;
	}

	@Override
	public int getNutrientBiomeBonus(Holder<Biome> biome)
	{
		int ret = 0;

		for (EnvProxy.BiomeType type : IC2R.envProxy.getBiomeTypes(biome))
		{
			Integer val = this.nutrientBiomeTypeBonus.get(type);
			if (val != null && val > ret)
			{
				ret = val;
			}
		}

		return ret;
	}

	@Override
	public CropCard getCropCard(String owner, String name)
	{
		Map<String, CropCard> map = this.cropMap.get(owner);
		return map == null ? null : map.get(name);
	}

	@Override
	public CropCard getCropCard(ItemStack stack)
	{
		ResourceLocation identifier = ForgeRegistries.ITEMS.getKey(stack.getItem());
		if (stack.is(ItemTags.SAPLINGS) && identifier.getNamespace().equals("minecraft"))
		{
			return this.getCropCard("ic2r", identifier.getPath());
		} else
		{
			CompoundTag nbt = stack.getTag();
			if (nbt == null)
			{
				return null;
			} else
			{
				return nbt.contains("owner") && nbt.contains("id") ? this.getCropCard(nbt.getString("owner"), nbt.getString("id")) : null;
			}
		}
	}

	@Override
	public CropCard getCropCard(Block cropBlock)
	{
		ResourceLocation cropIdentifier = ForgeRegistries.BLOCKS.getKey(cropBlock);
		String cropOwner = cropIdentifier.getNamespace();
		String cropName = cropIdentifier.getPath().replace("_crop", "");
		return this.getCropCard(cropOwner, cropName);
	}

	@Override
	public Collection<CropCard> getCrops()
	{
		return new AbstractCollection<>()
		{
			@Override
			public @NotNull Iterator<CropCard> iterator()
			{
				return new Iterator<>()
				{
					private final Iterator<Map<String, CropCard>> mapIterator = Ic2rCrops.this.cropMap.values().iterator();
					private Iterator<CropCard> iterator = this.getNextIterator();

					@Override
					public boolean hasNext()
					{
						return this.iterator != null && this.iterator.hasNext();
					}

					public CropCard next()
					{
						if (this.iterator == null)
						{
							throw new NoSuchElementException("no more elements");
						}

						CropCard ret = this.iterator.next();
						if (!this.iterator.hasNext())
						{
							this.iterator = this.getNextIterator();
						}

						return ret;
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException("This iterator is fromJson-only.");
					}

					private Iterator<CropCard> getNextIterator()
					{
						Iterator<CropCard> ret = null;

						while (this.mapIterator.hasNext() && ret == null)
						{
							ret = this.mapIterator.next().values().iterator();
							if (!ret.hasNext())
							{
								ret = null;
							}
						}

						return ret;
					}
				};
			}

			@Override
			public int size()
			{
				int ret = 0;

				for (Map<String, CropCard> map : Ic2rCrops.this.cropMap.values())
				{
					ret += map.size();
				}

				return ret;
			}
		};
	}

	@Override
	public void registerCrop(CropCard crop)
	{
		String owner = crop.getOwner();
		String id = crop.getId();
		if (!owner.equals(owner.toLowerCase(Locale.ENGLISH)))
		{
			throw new IllegalArgumentException("The crop owner=" + owner + " id=" + id + " uses a non-lower case owner");
		}

		Map<String, CropCard> map = this.cropMap.computeIfAbsent(owner, k -> new HashMap<>());

		CropCard prev = map.put(id, crop);
		if (prev != null)
		{
			throw new IllegalArgumentException("The crop owner=" + owner + " id=" + id + " uses a non-unique owner+id pair");
		}
	}

	@Override
	public void registerBaseSeed(ItemStack stack, CropCard crop, int size, int growth, int gain, int resistance)
	{
		for (ItemStack key : this.baseSeeds.keySet())
		{
			if (key.getItem() == stack.getItem() && key.getDamageValue() == stack.getDamageValue())
			{
				return;
			}
		}

		this.baseSeeds.put(stack, new BaseSeed(crop, size, growth, gain, resistance));
	}

	@Override
	public BaseSeed getBaseSeed(ItemStack stack)
	{
		if (stack == null)
		{
			return null;
		}

		for (Entry<ItemStack, BaseSeed> entry : this.baseSeeds.entrySet())
		{
			if (entry.getKey().getItem() == stack.getItem())
			{
				return entry.getValue();
			}
		}

		return null;
	}
}
