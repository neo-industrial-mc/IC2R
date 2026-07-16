package me.halfcooler.ic2r.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Forge DataGen entry (W2.5 / G2.6). Register providers on {@link GatherDataEvent}.
 * Run: {@code .\gradlew.bat runData}
 */
@EventBusSubscriber(modid = "ic2r", bus = EventBusSubscriber.Bus.MOD)
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

		Ic2rBlockTagsProvider blockTags = new Ic2rBlockTagsProvider(
			packOutput,
			lookupProvider,
			existingFileHelper
		);
		generator.addProvider(includeServer, blockTags);

		generator.addProvider(includeServer, new Ic2rItemTagsProvider(
			packOutput,
			lookupProvider,
			blockTags.contentsGetter(),
			existingFileHelper
		));
	}
}
