package me.halfcooler.ic2r.core.crop;

import me.halfcooler.ic2r.api.crops.CropCard;
import me.halfcooler.ic2r.api.crops.Crops;
import me.halfcooler.ic2r.api.crops.ICropType;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

public enum Ic2rCropType implements StringRepresentable, ICropType
{
	none("none", null, 0),
	weed("weed", Ic2rBlocks.WEED_CROP.get(), 4),
	wheat("wheat", Ic2rBlocks.WHEAT_CROP.get(), 7),
	carrots("carrots", Ic2rBlocks.CARROTS_CROP.get(), 3),
	potato("potato", Ic2rBlocks.POTATO_CROP.get(), 3),
	beetroots("beetroots", Ic2rBlocks.BEETROOTS_CROP.get(), 3),
	pumpkin("pumpkin", Ic2rBlocks.PUMPKIN_CROP.get(), 3),
	melon("melon", Ic2rBlocks.MELON_CROP.get(), 3),
	dandelion("dandelion", Ic2rBlocks.DANDELION_CROP.get(), 3),
	poppy("poppy", Ic2rBlocks.POPPY_CROP.get(), 3),
	blackthorn("blackthorn", Ic2rBlocks.BLACKTHORN_CROP.get(), 3),
	tulip("tulip", Ic2rBlocks.TULIP_CROP.get(), 3),
	cyazint("cyazint", Ic2rBlocks.CYAZINT_CROP.get(), 3),
	venomilia("venomilia", Ic2rBlocks.VENOMILIA_CROP.get(), 5),
	reed("reed", Ic2rBlocks.REED_CROP.get(), 2),
	stickyReed("sticky_reed", Ic2rBlocks.STICKY_REED_CROP.get(), 3),
	cocoa("cocoa", Ic2rBlocks.COCOA_CROP.get(), 3),
	flax("flax", Ic2rBlocks.FLAX_CROP.get(), 3),
	redMushroom("red_mushroom", Ic2rBlocks.RED_MUSHROOM_CROP.get(), 2),
	brownMushroom("brown_mushroom", Ic2rBlocks.BROWN_MUSHROOM_CROP.get(), 2),
	netherWart("nether_wart", Ic2rBlocks.NETHER_WART_CROP.get(), 2),
	terraWart("terra_wart", Ic2rBlocks.TERRA_WART_CROP.get(), 2),
	oakSapling("oak_sapling", Ic2rBlocks.OAK_SAPLING_CROP.get(), 4),
	spruceSapling("spruce_sapling", Ic2rBlocks.SPRUCE_SAPLING_CROP.get(), 4),
	birchSapling("birch_sapling", Ic2rBlocks.BIRCH_SAPLING_CROP.get(), 4),
	jungleSapling("jungle_sapling", Ic2rBlocks.JUNGLE_SAPLING_CROP.get(), 4),
	acaciaSapling("acacia_sapling", Ic2rBlocks.ACACIA_SAPLING_CROP.get(), 4),
	darkOakSapling("dark_oak_sapling", Ic2rBlocks.DARK_OAK_SAPLING_CROP.get(), 4),
	ferru("ferru", Ic2rBlocks.FERRU_CROP.get(), 3),
	cyprium("cyprium", Ic2rBlocks.CYPRIUM_CROP.get(), 3),
	stagnium("stagnium", Ic2rBlocks.STAGNIUM_CROP.get(), 3),
	plumbiscus("plumbiscus", Ic2rBlocks.PLUMBISCUS_CROP.get(), 3),
	aurelia("aurelia", Ic2rBlocks.AURELIA_CROP.get(), 3),
	shining("shining", Ic2rBlocks.SHINING_CROP.get(), 3),
	redWheat("red_wheat", Ic2rBlocks.RED_WHEAT_CROP.get(), 6),
	coffee("coffee", Ic2rBlocks.COFFEE_CROP.get(), 4),
	hops("hops", Ic2rBlocks.HOPS_CROP.get(), 6),
	eatingPlant("eating_plant", Ic2rBlocks.EATING_PLANT_CROP.get(), 5),
	blazereed("blazereed", Ic2rBlocks.BLAZEREED_CROP.get(), 3),
	bobsYerUncleRanksBerries("bobs_yer_uncle_ranks_berries", Ic2rBlocks.BOBS_YER_UNCLE_RANKS_BERRIES_CROP.get(), 3),
	corium("corium", Ic2rBlocks.CORIUM_CROP.get(), 3),
	corpse_plant("corpse_plant", Ic2rBlocks.CORPSE_PLANT_CROP.get(), 3),
	creeper_weed("creeper_weed", Ic2rBlocks.CREEPER_WEED_CROP.get(), 3),
	diareed("diareed", Ic2rBlocks.DIAREED_CROP.get(), 3),
	egg_plant("egg_plant", Ic2rBlocks.EGG_PLANT_CROP.get(), 2),
	ender_blossom("ender_blossom", Ic2rBlocks.ENDER_BLOSSOM_CROP.get(), 3),
	meat_rose("meat_rose", Ic2rBlocks.MEAT_ROSE_CROP.get(), 3),
	milk_wart("milk_wart", Ic2rBlocks.MILK_WART_CROP.get(), 2),
	oil_berries("oil_berries", Ic2rBlocks.OIL_BERRIES_CROP.get(), 2),
	slime_plant("slime_plant", Ic2rBlocks.SLIME_PLANT_CROP.get(), 3),
	spidernip("spidernip", Ic2rBlocks.SPIDERNIP_CROP.get(), 3),
	tearstalks("tearstalks", Ic2rBlocks.TEARSTALKS_CROP.get(), 3),
	withereed("withereed", Ic2rBlocks.WITHEREED_CROP.get(), 3);

	private static final Ic2rCropType[] values = values();
	private final String owner;
	private final String name;
	private final Block cropBlock;
	private final int maxAge;

	Ic2rCropType(String cropName, Block cropBlock, int maxAge)
	{
		this.name = cropName;
		this.owner = "ic2r";
		this.cropBlock = cropBlock;
		this.maxAge = maxAge;
	}

	Ic2rCropType(String cropName, String owner, Block cropBlock, int maxAge)
	{
		this.name = cropName;
		this.owner = owner;
		this.cropBlock = cropBlock;
		this.maxAge = maxAge;
	}

	public static CropCard getCropCard(Ic2rCropType cropType)
	{
		return Crops.instance.getCropCard(cropType.getOwner(), cropType.getName());
	}

	public static Ic2rCropType getCropType(CropCard cropCard)
	{
		return valueOf(Util.toCamel(cropCard.getId()));
	}

	public String getSerializedName()
	{
		return this.name;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public String getOwner()
	{
		return this.owner;
	}

	@Override
	public Block getCropBlock()
	{
		return this.cropBlock;
	}

	@Override
	public int getMaxAge()
	{
		return this.maxAge;
	}
}
