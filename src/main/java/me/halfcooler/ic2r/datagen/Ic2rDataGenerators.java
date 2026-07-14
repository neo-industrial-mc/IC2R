package me.halfcooler.ic2r.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge DataGen entry (W2.5). Register providers on {@link GatherDataEvent}.
 * Run: {@code .\gradlew.bat runData}
 */
@Mod.EventBusSubscriber(modid = "ic2r", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Ic2rDataGenerators
{
	private Ic2rDataGenerators()
	{
	}

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event)
	{
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		boolean includeServer = event.includeServer();

		// Item tags only for the pilot; empty block-tag lookup (no copy() usage yet).
		CompletableFuture<TagsProvider.TagLookup<Block>> emptyBlockTags =
			CompletableFuture.completedFuture(TagsProvider.TagLookup.empty());

		generator.addProvider(includeServer, new Ic2rItemTagsProvider(
			packOutput,
			lookupProvider,
			emptyBlockTags,
			existingFileHelper
		));
	}
}
