package ic2.core.init;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;

public class IC2UuScanConfig
{
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> values;

	static
	{
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

		b.push("uu-scan");
		b.comment(
			"UU world scan values for the UU value graph.",
			"Format: \"mod:item value\" where a value of 1 equals cobblestone.",
			"Run /ic2 uu-world-scan <small|medium|large> to calibrate for your world,",
			"then replace this list with the command output."
		);
		values = b.defineListAllowEmpty("values", IC2UuScanConfig::defaultValues, e -> e instanceof String);
		b.pop();

		SPEC = b.build();
	}

	private static List<String> defaultValues()
	{
		List<String> defaults = new ArrayList<>();
		defaults.add("minecraft:cobblestone 1.0");
		defaults.add("minecraft:dirt 14.857483653272267");
		defaults.add("minecraft:sandstone 133.57127953885157");
		defaults.add("minecraft:sand 106.90291914587954");
		defaults.add("minecraft:gravel 44.48567475377741");
		defaults.add("minecraft:brown_terracotta 2102.398128595354");
		defaults.add("minecraft:terracotta 615.557967486897");
		defaults.add("minecraft:flint 399.761391695243");
		defaults.add("minecraft:orange_terracotta 1609.7813554124502");
		defaults.add("minecraft:red_terracotta 2493.3561846915572");
		defaults.add("minecraft:snowball 276.3148760172392");
		defaults.add("minecraft:light_gray_terracotta 9242.072183098591");
		defaults.add("minecraft:white_terracotta 5257.781989802113");
		defaults.add("minecraft:yellow_terracotta 5794.1467991169975");
		defaults.add("minecraft:red_sand 13058.450248756219");
		defaults.add("minecraft:coarse_dirt 16844.943698949824");
		defaults.add("minecraft:lily_pad 45491.96454831933");
		defaults.add("minecraft:red_sandstone 4812038.916666667");
		defaults.add("minecraft:andesite 16.363280265570552");
		defaults.add("minecraft:granite 17.088380001493466");
		defaults.add("minecraft:diorite 17.776792295554227");
		defaults.add("minecraft:coal 60.62629764082485");
		defaults.add("minecraft:redstone 79.72506644392134");
		defaults.add("minecraft:iron_ore 107.27283597727642");
		defaults.add("ic2:tin_ore 136.03834175163496");
		defaults.add("minecraft:lapis_lazuli 416.5736474186792");
		defaults.add("ic2:lead_ore 918.2355519747268");
		defaults.add("minecraft:gold_ore 1015.019634382141");
		defaults.add("minecraft:clay_ball 1734.484771116184");
		defaults.add("ic2:uranium_ore 1607.0037848217517");
		defaults.add("minecraft:dark_oak_log 2654.795963403981");
		defaults.add("minecraft:prismarine 29381.513059701494");
		defaults.add("minecraft:prismarine_bricks 23463.82242990654");
		defaults.add("minecraft:spruce_log 2938.849133104876");
		defaults.add("minecraft:oak_log 3505.4717107126958");
		defaults.add("minecraft:diamond 2879.305260533533");
		defaults.add("minecraft:oak_planks 2053.9642759749113");
		defaults.add("minecraft:oak_fence 2773.6870917125657");
		defaults.add("minecraft:oak_sapling 5349.18638258453");
		defaults.add("minecraft:wheat_seeds 4738.0723428696465");
		defaults.add("minecraft:jungle_log 7112.847505645658");
		defaults.add("minecraft:birch_log 5604.989193386611");
		defaults.add("minecraft:spruce_sapling 10023.34091303593");
		defaults.add("minecraft:dark_oak_sapling 20679.64677092038");
		defaults.add("minecraft:string 9721.29074074074");
		defaults.add("minecraft:rail 8117.398481795604");
		defaults.add("minecraft:birch_sapling 13292.925184162063");
		defaults.add("minecraft:mossy_cobblestone 19798.102971428572");
		defaults.add("minecraft:prismarine_crystals 1007170.9360465116");
		defaults.add("minecraft:apple 42805.38695329874");
		defaults.add("minecraft:dandelion 30148.520884093283");
		defaults.add("minecraft:emerald 26754.193204633204");
		defaults.add("minecraft:dark_prismarine 931362.3709677419");
		defaults.add("minecraft:acacia_log 55012.19466497301");
		defaults.add("minecraft:poppy 46568.118548387094");
		defaults.add("minecraft:white_tulip 373347.8469827586");
		defaults.add("minecraft:acacia_sapling 127565.09646539028");
		defaults.add("minecraft:red_mushroom 165141.4690181125");
		defaults.add("minecraft:brown_mushroom 62971.06543075245");
		defaults.add("minecraft:jungle_sapling 78033.06351351351");
		defaults.add("ic2:rubber_wood 93036.19817400645");
		defaults.add("minecraft:torch 67722.20523846755");
		defaults.add("minecraft:azure_bluet 161147.34976744186");
		defaults.add("minecraft:orange_tulip 386681.69866071426");
		defaults.add("minecraft:red_tulip 448791.19430051814");
		defaults.add("minecraft:stick 115488.934");
		defaults.add("minecraft:pink_tulip 828867.947368421");
		defaults.add("minecraft:cactus 264075.306402439");
		defaults.add("minecraft:sugar_cane 194425.8148148148");
		defaults.add("minecraft:rose_bush 433083.5025");
		defaults.add("ic2:rubber_sapling 357182.2701030928");
		defaults.add("minecraft:allium 425634.8918918919");
		defaults.add("minecraft:peony 502125.8");
		defaults.add("minecraft:lilac 380732.7494505494");
		defaults.add("minecraft:oxeye_daisy 276730.67252396164");
		defaults.add("minecraft:blue_orchid 1219953.528169014");
		defaults.add("minecraft:cocoa_beans 534670.9907407408");
		defaults.add("ic2:resin 3331411.5576923075");
		defaults.add("minecraft:pumpkin 2439907.056338028");
		defaults.add("minecraft:sandstone_stairs 1007170.9360465116");
		defaults.add("minecraft:smooth_sandstone 690172.9123505976");
		defaults.add("minecraft:sunflower 474612.0575342466");
		defaults.add("minecraft:melon 874916.1666666666");
		defaults.add("minecraft:bone 261681.8746223565");
		defaults.add("minecraft:rotten_flesh 226448.89019607843");
		defaults.add("minecraft:gunpowder 247123.25392296718");
		defaults.add("minecraft:chest 821011.3791469195");
		defaults.add("minecraft:melon_seeds 1302506.7744360901");
		defaults.add("minecraft:music_disc_13 3535375.5306122447");
		defaults.add("minecraft:gold_ingot 5774446.7");
		defaults.add("minecraft:name_tag 2510629.0");
		defaults.add("minecraft:pumpkin_seeds 1283210.3777777778");
		defaults.add("minecraft:enchanted_golden_apple 2.88722335E7");
		defaults.add("minecraft:bucket 4330835.025");
		defaults.add("minecraft:music_disc_cat 3937122.75");
		defaults.add("minecraft:saddle 2706771.890625");
		defaults.add("minecraft:golden_apple 3396733.3529411764");
		defaults.add("minecraft:enchanted_book 1.73233401E8");
		defaults.add("minecraft:iron_horse_armor 3149698.2");
		defaults.add("minecraft:bread 2165417.5125");
		defaults.add("minecraft:chiseled_sandstone 1.443611675E7");
		defaults.add("minecraft:iron_ingot 1342894.5813953488");
		defaults.add("minecraft:tnt 603597.1111111111");
		defaults.add("minecraft:gold_block 1.08270875625E7");
		defaults.add("minecraft:cobblestone_stairs 3.46466802E7");
		defaults.add("minecraft:diamond_horse_armor 9624077.833333334");
		defaults.add("minecraft:spider_eye 5432374.0");
		defaults.add("minecraft:stone_pressure_plate 5432374.0");
		defaults.add("minecraft:sandstone_slab 7218058.375");
		defaults.add("minecraft:oak_door 1.443611675E7");
		defaults.add("minecraft:golden_horse_armor 4330835.025");
		defaults.add("minecraft:black_wool 5432374.0");
		defaults.add("minecraft:blue_terracotta 5432374.0");
		defaults.add("minecraft:oak_pressure_plate 1.9248155666666668E7");
		defaults.add("minecraft:wheat 674059.9260700389");
		defaults.add("minecraft:beetroot_seeds 1211422.3846153845");
		defaults.add("minecraft:oak_stairs 577444.67");
		defaults.add("minecraft:potato 1862724.7419354839");
		defaults.add("minecraft:book 2749736.523809524");
		defaults.add("minecraft:stone_slab 3268554.7358490564");
		defaults.add("minecraft:carrot 5249497.0");
		defaults.add("minecraft:stone_bricks 6416051.888888889");
		defaults.add("minecraft:wet_sponge 6662823.115384615");
		defaults.add("minecraft:beetroot 1.443611675E7");
		defaults.add("minecraft:ladder 2.1654175125E7");
		defaults.add("minecraft:obsidian 4.330835025E7");
		defaults.add("minecraft:crafting_table 8.66167005E7");
		defaults.add("minecraft:cracked_stone_bricks 8.66167005E7");
		return defaults;
	}
}
