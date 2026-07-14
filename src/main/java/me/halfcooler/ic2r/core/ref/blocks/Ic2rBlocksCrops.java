package me.halfcooler.ic2r.core.ref.blocks;

import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import me.halfcooler.ic2r.core.crop.Ic2rCropType;
import me.halfcooler.ic2r.core.crop.TileEntityCrop;
import me.halfcooler.ic2r.core.util.Util;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;


import net.minecraftforge.registries.RegistryObject;
import me.halfcooler.ic2r.forge.EnvProxyForge;

/** Domain block registrations: crop sticks and crop blocks */
public final class Ic2rBlocksCrops
{
	private Ic2rBlocksCrops()
	{
	}

	private static final Properties cropSettings = Properties.of().strength(0.8F, 0.2F).sound(SoundType.CROP).noCollission();
	public static final RegistryObject<Block> CROP_STICK = EnvProxyForge.BLOCKS.register("crop_stick", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.none));
	public static final RegistryObject<Block> WEED_CROP = EnvProxyForge.BLOCKS.register("weed_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.weed));
	public static final RegistryObject<Block> WHEAT_CROP = EnvProxyForge.BLOCKS.register("wheat_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.wheat));
	public static final RegistryObject<Block> CARROTS_CROP = EnvProxyForge.BLOCKS.register("carrots_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.carrots));
	public static final RegistryObject<Block> POTATO_CROP = EnvProxyForge.BLOCKS.register("potato_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.potato));
	public static final RegistryObject<Block> BEETROOTS_CROP = EnvProxyForge.BLOCKS.register("beetroots_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.beetroots));
	public static final RegistryObject<Block> PUMPKIN_CROP = EnvProxyForge.BLOCKS.register("pumpkin_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.pumpkin));
	public static final RegistryObject<Block> MELON_CROP = EnvProxyForge.BLOCKS.register("melon_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.melon));
	public static final RegistryObject<Block> DANDELION_CROP = EnvProxyForge.BLOCKS.register("dandelion_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.dandelion));
	public static final RegistryObject<Block> POPPY_CROP = EnvProxyForge.BLOCKS.register("poppy_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.poppy));
	public static final RegistryObject<Block> BLACKTHORN_CROP = EnvProxyForge.BLOCKS.register("blackthorn_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.blackthorn));
	public static final RegistryObject<Block> TULIP_CROP = EnvProxyForge.BLOCKS.register("tulip_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.tulip));
	public static final RegistryObject<Block> CYAZINT_CROP = EnvProxyForge.BLOCKS.register("cyazint_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.cyazint));
	public static final RegistryObject<Block> VENOMILIA_CROP = EnvProxyForge.BLOCKS.register("venomilia_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.venomilia));
	public static final RegistryObject<Block> REED_CROP = EnvProxyForge.BLOCKS.register("reed_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.reed));
	public static final RegistryObject<Block> STICKY_REED_CROP = EnvProxyForge.BLOCKS.register("sticky_reed_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.stickyReed));
	public static final RegistryObject<Block> COCOA_CROP = EnvProxyForge.BLOCKS.register("cocoa_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.cocoa));
	public static final RegistryObject<Block> FLAX_CROP = EnvProxyForge.BLOCKS.register("flax_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.flax));
	public static final RegistryObject<Block> RED_MUSHROOM_CROP = EnvProxyForge.BLOCKS.register("red_mushroom_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.redMushroom));
	public static final RegistryObject<Block> BROWN_MUSHROOM_CROP = EnvProxyForge.BLOCKS.register("brown_mushroom_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.brownMushroom));
	public static final RegistryObject<Block> NETHER_WART_CROP = EnvProxyForge.BLOCKS.register("nether_wart_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.netherWart));
	public static final RegistryObject<Block> TERRA_WART_CROP = EnvProxyForge.BLOCKS.register("terra_wart_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.terraWart));
	public static final RegistryObject<Block> OAK_SAPLING_CROP = EnvProxyForge.BLOCKS.register("oak_sapling_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.oakSapling));
	public static final RegistryObject<Block> SPRUCE_SAPLING_CROP = EnvProxyForge.BLOCKS.register("spruce_sapling_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.spruceSapling));
	public static final RegistryObject<Block> BIRCH_SAPLING_CROP = EnvProxyForge.BLOCKS.register("birch_sapling_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.birchSapling));
	public static final RegistryObject<Block> JUNGLE_SAPLING_CROP = EnvProxyForge.BLOCKS.register("jungle_sapling_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.jungleSapling));
	public static final RegistryObject<Block> ACACIA_SAPLING_CROP = EnvProxyForge.BLOCKS.register("acacia_sapling_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.acaciaSapling));
	public static final RegistryObject<Block> DARK_OAK_SAPLING_CROP = EnvProxyForge.BLOCKS.register("dark_oak_sapling_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.darkOakSapling));
	public static final RegistryObject<Block> FERRU_CROP = EnvProxyForge.BLOCKS.register("ferru_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.ferru));
	public static final RegistryObject<Block> CYPRIUM_CROP = EnvProxyForge.BLOCKS.register("cyprium_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.cyprium));
	public static final RegistryObject<Block> STAGNIUM_CROP = EnvProxyForge.BLOCKS.register("stagnium_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.stagnium));
	public static final RegistryObject<Block> PLUMBISCUS_CROP = EnvProxyForge.BLOCKS.register("plumbiscus_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.plumbiscus));
	public static final RegistryObject<Block> AURELIA_CROP = EnvProxyForge.BLOCKS.register("aurelia_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.aurelia));
	public static final RegistryObject<Block> SHINING_CROP = EnvProxyForge.BLOCKS.register("shining_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.shining));
	public static final RegistryObject<Block> RED_WHEAT_CROP = EnvProxyForge.BLOCKS.register("red_wheat_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.redWheat));
	public static final RegistryObject<Block> COFFEE_CROP = EnvProxyForge.BLOCKS.register("coffee_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.coffee));
	public static final RegistryObject<Block> HOPS_CROP = EnvProxyForge.BLOCKS.register("hops_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.hops));
	public static final RegistryObject<Block> EATING_PLANT_CROP = EnvProxyForge.BLOCKS.register("eating_plant_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.eatingPlant));
	public static final RegistryObject<Block> BLAZEREED_CROP = EnvProxyForge.BLOCKS.register("blazereed_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.blazereed));
	public static final RegistryObject<Block> BOBS_YER_UNCLE_RANKS_BERRIES_CROP = EnvProxyForge.BLOCKS.register("bobs_yer_uncle_ranks_berries_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.bobsYerUncleRanksBerries));
	public static final RegistryObject<Block> CORIUM_CROP = EnvProxyForge.BLOCKS.register("corium_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.corium));
	public static final RegistryObject<Block> CORPSE_PLANT_CROP = EnvProxyForge.BLOCKS.register("corpse_plant_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.corpse_plant));
	public static final RegistryObject<Block> CREEPER_WEED_CROP = EnvProxyForge.BLOCKS.register("creeper_weed_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.creeper_weed));
	public static final RegistryObject<Block> DIAREED_CROP = EnvProxyForge.BLOCKS.register("diareed_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.diareed));
	public static final RegistryObject<Block> EGG_PLANT_CROP = EnvProxyForge.BLOCKS.register("egg_plant_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.egg_plant));
	public static final RegistryObject<Block> ENDER_BLOSSOM_CROP = EnvProxyForge.BLOCKS.register("ender_blossom_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.ender_blossom));
	public static final RegistryObject<Block> MEAT_ROSE_CROP = EnvProxyForge.BLOCKS.register("meat_rose_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.meat_rose));
	public static final RegistryObject<Block> MILK_WART_CROP = EnvProxyForge.BLOCKS.register("milk_wart_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.milk_wart));
	public static final RegistryObject<Block> OIL_BERRIES_CROP = EnvProxyForge.BLOCKS.register("oil_berries_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.oil_berries));
	public static final RegistryObject<Block> SLIME_PLANT_CROP = EnvProxyForge.BLOCKS.register("slime_plant_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.slime_plant));
	public static final RegistryObject<Block> SPIDERNIP_CROP = EnvProxyForge.BLOCKS.register("spidernip_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.spidernip));
	public static final RegistryObject<Block> TEARSTALKS_CROP = EnvProxyForge.BLOCKS.register("tearstalks_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.tearstalks));
	public static final RegistryObject<Block> WITHEREED_CROP = EnvProxyForge.BLOCKS.register("withereed_crop", () -> Ic2rTileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2rCropType.withereed));
}
