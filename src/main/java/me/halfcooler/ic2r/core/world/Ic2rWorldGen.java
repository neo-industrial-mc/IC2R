package me.halfcooler.ic2r.core.world;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.misc.RubberLogBlock;
import me.halfcooler.ic2r.core.proxy.EnvProxy;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.Holder;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration.TreeConfigurationBuilder;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

public class Ic2rWorldGen
{
	private static final WeightedStateProvider RUBBER_LOG_PROVIDER = new WeightedStateProvider(
		(SimpleWeightedRandomList.Builder) SimpleWeightedRandomList.builder()
			.add(Ic2rBlocks.RUBBER_LOG.get().defaultBlockState(), 16)
			.add(Ic2rBlocks.RUBBER_LOG.get().defaultBlockState().setValue(RubberLogBlock.stateProperty, RubberLogBlock.RubberWoodState.wet_north), 1)
			.add(Ic2rBlocks.RUBBER_LOG.get().defaultBlockState().setValue(RubberLogBlock.stateProperty, RubberLogBlock.RubberWoodState.wet_east), 1)
			.add(Ic2rBlocks.RUBBER_LOG.get().defaultBlockState().setValue(RubberLogBlock.stateProperty, RubberLogBlock.RubberWoodState.wet_south), 1)
			.add(Ic2rBlocks.RUBBER_LOG.get().defaultBlockState().setValue(RubberLogBlock.stateProperty, RubberLogBlock.RubberWoodState.wet_west), 1)
	);
	public static final CompletableFuture<Holder<ConfiguredFeature<TreeConfiguration, ?>>> RUBBER_TREE = register(
		new TreeConfigurationBuilder(
			RUBBER_LOG_PROVIDER,
			new StraightTrunkPlacer(4, 4, 0),
			BlockStateProvider.simple(Ic2rBlocks.RUBBER_LEAVES.get()),
			RubberTreeFoliagePlacer.INSTANCE,
			new TwoLayersFeatureSize(1, 0, 1)
		)
			.ignoreVines()
			.build()
	);

	public static void init()
	{
		attachOreFeatureToBiome("lead_ore");
		attachOreFeatureToBiome("lead_ore_lower");
		attachOreFeatureToBiome("tin_ore_small");
		attachOreFeatureToBiome("tin_ore_upper");
		attachOreFeatureToBiome("uranium_ore");
		attachOreFeatureToBiome("uranium_ore_buried");
		attachOreFeatureToBiome("uranium_ore_large");
		attachRubberTreeFeatureToBiome("trees_rubber_jungle", EnvProxy.BiomeSelector.JUNGLE);
		attachRubberTreeFeatureToBiome("trees_rubber_forest", EnvProxy.BiomeSelector.FOREST);
		attachRubberTreeFeatureToBiome("trees_rubber_swamp", EnvProxy.BiomeSelector.SWAMP);
		RubberTreeFoliagePlacer.init();
	}

	private static void attachOreFeatureToBiome(String id)
	{
		IC2R.envProxy.attachPlacedFeatureToBiome(IC2R.getIdentifier(id), EnvProxy.BiomeSelector.OVERWORLD, Decoration.UNDERGROUND_ORES);
	}

	private static void attachRubberTreeFeatureToBiome(String id, EnvProxy.BiomeSelector selector)
	{
		IC2R.envProxy.attachPlacedFeatureToBiome(IC2R.getIdentifier(id), selector, Decoration.VEGETAL_DECORATION);
	}

	private static <FC extends FeatureConfiguration, F extends Feature<FC>> CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> register(
		FC config
	)
	{
		return IC2R.envProxy.registerConfiguredFeature(IC2R.getIdentifier("rubber_tree"), (F) Feature.TREE, config);
	}
}
