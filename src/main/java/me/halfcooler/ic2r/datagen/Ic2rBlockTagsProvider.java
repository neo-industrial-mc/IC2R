package me.halfcooler.ic2r.datagen;

import java.util.concurrent.CompletableFuture;

import me.halfcooler.ic2r.core.ref.Ic2rBlockTags;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Block-tag DataGen (G2.6): migrates handwritten {@code data/ic2r/tags/blocks/**}.
 * Content is intentionally identical to the old JSON.
 */
public final class Ic2rBlockTagsProvider extends BlockTagsProvider
{
	public Ic2rBlockTagsProvider(
		PackOutput output,
		CompletableFuture<HolderLookup.Provider> lookupProvider,
		ExistingFileHelper existingFileHelper
	)
	{
		super(output, lookupProvider, "ic2r", existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.@NotNull Provider provider)
	{
		// data/ic2r/tags/blocks/mineable/wrench.json
		// Machines / storage / energy blocks correctly mined with the IC2R wrench.
		tag(Ic2rBlockTags.MINEABLE_WITH_WRENCH).add(
			// Generators
			Ic2rBlocks.GENERATOR.get(),
			Ic2rBlocks.GEO_GENERATOR.get(),
			Ic2rBlocks.KINETIC_GENERATOR.get(),
			Ic2rBlocks.RT_GENERATOR.get(),
			Ic2rBlocks.SEMIFLUID_GENERATOR.get(),
			Ic2rBlocks.SOLAR_GENERATOR.get(),
			Ic2rBlocks.STIRLING_GENERATOR.get(),
			Ic2rBlocks.WATER_GENERATOR.get(),
			Ic2rBlocks.WIND_GENERATOR.get(),
			// Heat generators
			Ic2rBlocks.ELECTRIC_HEAT_GENERATOR.get(),
			Ic2rBlocks.FLUID_HEAT_GENERATOR.get(),
			Ic2rBlocks.RT_HEAT_GENERATOR.get(),
			Ic2rBlocks.SOLID_HEAT_GENERATOR.get(),
			// Kinetic generators
			Ic2rBlocks.ELECTRIC_KINETIC_GENERATOR.get(),
			Ic2rBlocks.STEAM_KINETIC_GENERATOR.get(),
			Ic2rBlocks.STIRLING_KINETIC_GENERATOR.get(),
			Ic2rBlocks.WATER_KINETIC_GENERATOR.get(),
			Ic2rBlocks.WIND_KINETIC_GENERATOR.get(),
			// Reactor
			Ic2rBlocks.NUCLEAR_REACTOR.get(),
			Ic2rBlocks.REACTOR_CHAMBER.get(),
			// Fluid / heat machines
			Ic2rBlocks.CONDENSER.get(),
			Ic2rBlocks.FLUID_BOTTLER.get(),
			Ic2rBlocks.FLUID_DISTRIBUTOR.get(),
			Ic2rBlocks.FLUID_REGULATOR.get(),
			Ic2rBlocks.LIQUID_HEAT_EXCHANGER.get(),
			Ic2rBlocks.PUMP.get(),
			Ic2rBlocks.SOLAR_DISTILLER.get(),
			Ic2rBlocks.STEAM_GENERATOR.get(),
			// Logistics / utility
			Ic2rBlocks.ITEM_BUFFER.get(),
			Ic2rBlocks.MAGNETIZER.get(),
			Ic2rBlocks.SORTING_MACHINE.get(),
			Ic2rBlocks.TELEPORTER.get(),
			Ic2rBlocks.TERRAFORMER.get(),
			Ic2rBlocks.TESLA_COIL.get(),
			// Basic processing
			Ic2rBlocks.CANNER.get(),
			Ic2rBlocks.COMPRESSOR.get(),
			Ic2rBlocks.ELECTRIC_FURNACE.get(),
			Ic2rBlocks.EXTRACTOR.get(),
			Ic2rBlocks.MACERATOR.get(),
			Ic2rBlocks.RECYCLER.get(),
			Ic2rBlocks.SOLID_CANNER.get(),
			// Advanced processing
			Ic2rBlocks.BLAST_FURNACE.get(),
			Ic2rBlocks.BLOCK_CUTTER.get(),
			Ic2rBlocks.CENTRIFUGE.get(),
			Ic2rBlocks.FERMENTER.get(),
			Ic2rBlocks.INDUCTION_FURNACE.get(),
			Ic2rBlocks.METAL_FORMER.get(),
			Ic2rBlocks.ORE_WASHING_PLANT.get(),
			// Mining / crops / UU
			Ic2rBlocks.ADVANCED_MINER.get(),
			Ic2rBlocks.CROP_HARVESTER.get(),
			Ic2rBlocks.CROPMATRON.get(),
			Ic2rBlocks.MINER.get(),
			Ic2rBlocks.MATTER_GENERATOR.get(),
			Ic2rBlocks.PATTERN_STORAGE.get(),
			Ic2rBlocks.REPLICATOR.get(),
			Ic2rBlocks.UU_SCANNER.get(),
			Ic2rBlocks.ELECTROLYZER.get(),
			// Energy storage / transformers
			Ic2rBlocks.BATBOX.get(),
			Ic2rBlocks.CESU.get(),
			Ic2rBlocks.MFE.get(),
			Ic2rBlocks.MFSU.get(),
			Ic2rBlocks.LV_TRANSFORMER.get(),
			Ic2rBlocks.MV_TRANSFORMER.get(),
			Ic2rBlocks.HV_TRANSFORMER.get(),
			Ic2rBlocks.EV_TRANSFORMER.get(),
			Ic2rBlocks.EU_TO_FE_CONVERTER.get(),
			// Misc machines
			Ic2rBlocks.TANK.get(),
			Ic2rBlocks.CHUNK_LOADER.get(),
			Ic2rBlocks.STEAM_REPRESSURIZER.get(),
			Ic2rBlocks.WEIGHTED_FLUID_DISTRIBUTOR.get(),
			Ic2rBlocks.WEIGHTED_ITEM_DISTRIBUTOR.get(),
			Ic2rBlocks.RCI_RSH.get(),
			Ic2rBlocks.RCI_LZH.get(),
			// Chargepads
			Ic2rBlocks.BATBOX_CHARGEPAD.get(),
			Ic2rBlocks.CESU_CHARGEPAD.get(),
			Ic2rBlocks.MFE_CHARGEPAD.get(),
			Ic2rBlocks.MFSU_CHARGEPAD.get(),
			// Storage boxes / tanks
			Ic2rBlocks.IRON_STORAGE_BOX.get(),
			Ic2rBlocks.BRONZE_STORAGE_BOX.get(),
			Ic2rBlocks.STEEL_STORAGE_BOX.get(),
			Ic2rBlocks.IRIDIUM_STORAGE_BOX.get(),
			Ic2rBlocks.BRONZE_TANK.get(),
			Ic2rBlocks.IRON_TANK.get(),
			Ic2rBlocks.STEEL_TANK.get(),
			Ic2rBlocks.IRIDIUM_TANK.get(),
			// Crafting
			Ic2rBlocks.INDUSTRIAL_WORKBENCH.get(),
			Ic2rBlocks.BATCH_CRAFTER.get()
		);
	}
}
