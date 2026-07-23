package me.halfcooler.ic2r.core.init;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.platform.services.PlatformServices;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * UU matter replication costs loaded from {@code config/ic2r/ic2r-uu-matter.toml}.
 * <p>
 * Flat map format (no ModConfigSpec section nesting):
 * <pre>
 * "minecraft:iron_ingot" = 100
 * "minecraft:cobblestone" = 1
 * </pre>
 * Values seed the UU graph; recipe propagation may lower final costs for craftable items.
 */
public final class IC2RUuMatterConfig
{
	public static final String RELATIVE_PATH = "ic2r/ic2r-uu-matter.toml";

	private static Map<String, Double> values = Collections.emptyMap();

	private IC2RUuMatterConfig()
	{
	}

	public static Map<String, Double> getValues()
	{
		return values;
	}

	/**
	 * Load (or create default) {@link #RELATIVE_PATH}. Safe to call multiple times;
	 * each call re-reads the file so edits apply on the next UU graph rebuild.
	 */
	public static void load()
	{
		Path path = PlatformServices.config().getConfigDirectory().resolve(RELATIVE_PATH);
		try
		{
			Files.createDirectories(path.getParent());
			if (!Files.exists(path))
			{
				writeDefaultFile(path);
			}

			try (CommentedFileConfig config = CommentedFileConfig.builder(path, TomlFormat.instance())
				.sync()
				.writingMode(WritingMode.REPLACE)
				.build())
			{
				config.load();
				Map<String, Double> loaded = new LinkedHashMap<>();
				for (Config.Entry entry : config.entrySet())
				{
					Object raw = entry.getValue();
					if (raw instanceof Number number)
					{
						loaded.put(entry.getKey(), number.doubleValue());
					}
				}
				values = Collections.unmodifiableMap(loaded);
			}

			if (values.isEmpty())
			{
				IC2R.log.warn(LogCategory.Uu, "No UU matter values in %s.", RELATIVE_PATH);
			}
			else
			{
				IC2R.log.debug(LogCategory.Uu, "Loaded %d UU matter values from %s.", values.size(), RELATIVE_PATH);
			}
		}
		catch (Exception e)
		{
			IC2R.log.warn(LogCategory.Uu, e, "Failed to load %s.", RELATIVE_PATH);
			values = Collections.emptyMap();
		}
	}

	private static void writeDefaultFile(Path path) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("# UU matter replication seed values for the UU cost graph.\n");
		sb.append("# Format: \"modid:item_id\" = number\n");
		sb.append("# A value of 1 equals cobblestone. Recipes may lower final costs for craftable items.\n");
		sb.append("# Only items with a finite UU value (from this file and/or recipe propagation) can be scanned/replicated.\n");
		sb.append('\n');

		for (Map.Entry<String, Double> entry : defaultValues().entrySet())
		{
			sb.append('"').append(entry.getKey()).append("\" = ");
			double v = entry.getValue();
			if (v == Math.rint(v) && !Double.isInfinite(v))
			{
				sb.append((long) v);
			}
			else
			{
				sb.append(v);
			}
			sb.append('\n');
		}

		Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
		IC2R.log.info(LogCategory.Uu, "Created default UU matter config at %s.", RELATIVE_PATH);
	}

	private static Map<String, Double> defaultValues()
	{
		Map<String, Double> defaults = new LinkedHashMap<>();
		defaults.put("minecraft:cobblestone", 1.0);
		defaults.put("minecraft:dirt", 14.857483653272267);
		defaults.put("minecraft:sandstone", 133.57127953885157);
		defaults.put("minecraft:sand", 106.90291914587954);
		defaults.put("minecraft:gravel", 44.48567475377741);
		defaults.put("minecraft:brown_terracotta", 2102.398128595354);
		defaults.put("minecraft:terracotta", 615.557967486897);
		defaults.put("minecraft:flint", 399.761391695243);
		defaults.put("minecraft:orange_terracotta", 1609.7813554124502);
		defaults.put("minecraft:red_terracotta", 2493.3561846915572);
		defaults.put("minecraft:snowball", 276.3148760172392);
		defaults.put("minecraft:light_gray_terracotta", 9242.072183098591);
		defaults.put("minecraft:white_terracotta", 5257.781989802113);
		defaults.put("minecraft:yellow_terracotta", 5794.1467991169975);
		defaults.put("minecraft:red_sand", 13058.450248756219);
		defaults.put("minecraft:coarse_dirt", 16844.943698949824);
		defaults.put("minecraft:lily_pad", 45491.96454831933);
		defaults.put("minecraft:red_sandstone", 4812038.916666667);
		defaults.put("minecraft:andesite", 16.363280265570552);
		defaults.put("minecraft:granite", 17.088380001493466);
		defaults.put("minecraft:diorite", 17.776792295554227);
		defaults.put("minecraft:coal", 60.62629764082485);
		defaults.put("minecraft:redstone", 79.72506644392134);
		defaults.put("minecraft:copper_ore", 24.902622637582024);
		defaults.put("minecraft:deepslate_copper_ore", 24.902622637582024);
		defaults.put("minecraft:iron_ore", 107.27283597727642);
		defaults.put("ic2r:tin_ore", 136.03834175163496);
		defaults.put("minecraft:lapis_lazuli", 416.5736474186792);
		defaults.put("ic2r:lead_ore", 918.2355519747268);
		defaults.put("minecraft:gold_ore", 1015.019634382141);
		defaults.put("minecraft:clay_ball", 1734.484771116184);
		defaults.put("ic2r:uranium_ore", 1607.0037848217517);
		defaults.put("minecraft:dark_oak_log", 2654.795963403981);
		defaults.put("minecraft:prismarine", 29381.513059701494);
		defaults.put("minecraft:prismarine_bricks", 23463.82242990654);
		defaults.put("minecraft:spruce_log", 2938.849133104876);
		defaults.put("minecraft:oak_log", 3505.4717107126958);
		defaults.put("minecraft:diamond", 2879.305260533533);
		defaults.put("minecraft:oak_planks", 2053.9642759749113);
		defaults.put("minecraft:oak_fence", 2773.6870917125657);
		defaults.put("minecraft:oak_sapling", 5349.18638258453);
		defaults.put("minecraft:wheat_seeds", 4738.0723428696465);
		defaults.put("minecraft:jungle_log", 7112.847505645658);
		defaults.put("minecraft:birch_log", 5604.989193386611);
		defaults.put("minecraft:spruce_sapling", 10023.34091303593);
		defaults.put("minecraft:dark_oak_sapling", 20679.64677092038);
		defaults.put("minecraft:string", 9721.29074074074);
		defaults.put("minecraft:rail", 8117.398481795604);
		defaults.put("minecraft:birch_sapling", 13292.925184162063);
		defaults.put("minecraft:mossy_cobblestone", 19798.102971428572);
		defaults.put("minecraft:prismarine_crystals", 1007170.9360465116);
		defaults.put("minecraft:apple", 42805.38695329874);
		defaults.put("minecraft:dandelion", 30148.520884093283);
		defaults.put("minecraft:emerald", 26754.193204633204);
		defaults.put("minecraft:dark_prismarine", 931362.3709677419);
		defaults.put("minecraft:acacia_log", 55012.19466497301);
		defaults.put("minecraft:poppy", 46568.118548387094);
		defaults.put("minecraft:white_tulip", 373347.8469827586);
		defaults.put("minecraft:acacia_sapling", 127565.09646539028);
		defaults.put("minecraft:red_mushroom", 165141.4690181125);
		defaults.put("minecraft:brown_mushroom", 62971.06543075245);
		defaults.put("minecraft:jungle_sapling", 78033.06351351351);
		defaults.put("ic2r:rubber_wood", 93036.19817400645);
		defaults.put("minecraft:torch", 67722.20523846755);
		defaults.put("minecraft:azure_bluet", 161147.34976744186);
		defaults.put("minecraft:orange_tulip", 386681.69866071426);
		defaults.put("minecraft:red_tulip", 448791.19430051814);
		defaults.put("minecraft:stick", 115488.934);
		defaults.put("minecraft:pink_tulip", 828867.947368421);
		defaults.put("minecraft:cactus", 264075.306402439);
		defaults.put("minecraft:sugar_cane", 194425.8148148148);
		defaults.put("minecraft:rose_bush", 433083.5025);
		defaults.put("ic2r:rubber_sapling", 357182.2701030928);
		defaults.put("minecraft:allium", 425634.8918918919);
		defaults.put("minecraft:peony", 502125.8);
		defaults.put("minecraft:lilac", 380732.7494505494);
		defaults.put("minecraft:oxeye_daisy", 276730.67252396164);
		defaults.put("minecraft:blue_orchid", 1219953.528169014);
		defaults.put("minecraft:cocoa_beans", 534670.9907407408);
		defaults.put("ic2r:resin", 3331411.5576923075);
		defaults.put("minecraft:pumpkin", 2439907.056338028);
		defaults.put("minecraft:sandstone_stairs", 1007170.9360465116);
		defaults.put("minecraft:smooth_sandstone", 690172.9123505976);
		defaults.put("minecraft:sunflower", 474612.0575342466);
		defaults.put("minecraft:melon", 874916.1666666666);
		defaults.put("minecraft:bone", 261681.8746223565);
		defaults.put("minecraft:rotten_flesh", 226448.89019607843);
		defaults.put("minecraft:gunpowder", 247123.25392296718);
		defaults.put("minecraft:chest", 821011.3791469195);
		defaults.put("minecraft:melon_seeds", 1302506.7744360901);
		defaults.put("minecraft:music_disc_13", 3535375.5306122447);
		defaults.put("minecraft:gold_ingot", 5774446.7);
		defaults.put("minecraft:name_tag", 2510629.0);
		defaults.put("minecraft:pumpkin_seeds", 1283210.3777777778);
		defaults.put("minecraft:enchanted_golden_apple", 2.88722335E7);
		defaults.put("minecraft:bucket", 4330835.025);
		defaults.put("minecraft:music_disc_cat", 3937122.75);
		defaults.put("minecraft:saddle", 2706771.890625);
		defaults.put("minecraft:golden_apple", 3396733.3529411764);
		defaults.put("minecraft:enchanted_book", 1.73233401E8);
		defaults.put("minecraft:iron_horse_armor", 3149698.2);
		defaults.put("minecraft:bread", 2165417.5125);
		defaults.put("minecraft:chiseled_sandstone", 1.443611675E7);
		defaults.put("minecraft:iron_ingot", 1342894.5813953488);
		defaults.put("minecraft:tnt", 603597.1111111111);
		defaults.put("minecraft:gold_block", 1.08270875625E7);
		defaults.put("minecraft:cobblestone_stairs", 3.46466802E7);
		defaults.put("minecraft:diamond_horse_armor", 9624077.833333334);
		defaults.put("minecraft:spider_eye", 5432374.0);
		defaults.put("minecraft:stone_pressure_plate", 5432374.0);
		defaults.put("minecraft:sandstone_slab", 7218058.375);
		defaults.put("minecraft:oak_door", 1.443611675E7);
		defaults.put("minecraft:golden_horse_armor", 4330835.025);
		defaults.put("minecraft:black_wool", 5432374.0);
		defaults.put("minecraft:blue_terracotta", 5432374.0);
		defaults.put("minecraft:oak_pressure_plate", 1.9248155666666668E7);
		defaults.put("minecraft:wheat", 674059.9260700389);
		defaults.put("minecraft:beetroot_seeds", 1211422.3846153845);
		defaults.put("minecraft:oak_stairs", 577444.67);
		defaults.put("minecraft:potato", 1862724.7419354839);
		defaults.put("minecraft:book", 2749736.523809524);
		defaults.put("minecraft:stone_slab", 3268554.7358490564);
		defaults.put("minecraft:carrot", 5249497.0);
		defaults.put("minecraft:stone_bricks", 6416051.888888889);
		defaults.put("minecraft:wet_sponge", 6662823.115384615);
		defaults.put("minecraft:beetroot", 1.443611675E7);
		defaults.put("minecraft:ladder", 2.1654175125E7);
		defaults.put("minecraft:obsidian", 4.330835025E7);
		defaults.put("minecraft:crafting_table", 8.66167005E7);
		defaults.put("minecraft:cracked_stone_bricks", 8.66167005E7);
		// Extra seeds previously under balance.uu-values.predefined
		defaults.put("ic2r:iridium_ore", 12000.0);
		defaults.put("ic2r:iridium_shard", 1333.0);
		return defaults;
	}
}
