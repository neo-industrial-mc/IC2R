package me.halfcooler.ic2r.datagen;

import java.util.concurrent.CompletableFuture;

import me.halfcooler.ic2r.core.ref.Ic2rBlockTags;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

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
	protected void addTags(HolderLookup.Provider provider)
	{
		// data/ic2r/tags/blocks/mineable/wrench.json
		// Machines / storage / energy blocks correctly mined with the IC2R wrench.
		tag(Ic2rBlockTags.MINEABLE_WITH_WRENCH).add(
			// Generators
			Ic2rBlocks.GENERATOR,
			Ic2rBlocks.GEO_GENERATOR,
			Ic2rBlocks.KINETIC_GENERATOR,
			Ic2rBlocks.RT_GENERATOR,
			Ic2rBlocks.SEMIFLUID_GENERATOR,
			Ic2rBlocks.SOLAR_GENERATOR,
			Ic2rBlocks.STIRLING_GENERATOR,
			Ic2rBlocks.WATER_GENERATOR,
			Ic2rBlocks.WIND_GENERATOR,
			// Heat generators
			Ic2rBlocks.ELECTRIC_HEAT_GENERATOR,
			Ic2rBlocks.FLUID_HEAT_GENERATOR,
			Ic2rBlocks.RT_HEAT_GENERATOR,
			Ic2rBlocks.SOLID_HEAT_GENERATOR,
			// Kinetic generators
			Ic2rBlocks.ELECTRIC_KINETIC_GENERATOR,
			Ic2rBlocks.STEAM_KINETIC_GENERATOR,
			Ic2rBlocks.STIRLING_KINETIC_GENERATOR,
			Ic2rBlocks.WATER_KINETIC_GENERATOR,
			Ic2rBlocks.WIND_KINETIC_GENERATOR,
			// Reactor
			Ic2rBlocks.NUCLEAR_REACTOR,
			Ic2rBlocks.REACTOR_CHAMBER,
			// Fluid / heat machines
			Ic2rBlocks.CONDENSER,
			Ic2rBlocks.FLUID_BOTTLER,
			Ic2rBlocks.FLUID_DISTRIBUTOR,
			Ic2rBlocks.FLUID_REGULATOR,
			Ic2rBlocks.LIQUID_HEAT_EXCHANGER,
			Ic2rBlocks.PUMP,
			Ic2rBlocks.SOLAR_DISTILLER,
			Ic2rBlocks.STEAM_GENERATOR,
			// Logistics / utility
			Ic2rBlocks.ITEM_BUFFER,
			Ic2rBlocks.MAGNETIZER,
			Ic2rBlocks.SORTING_MACHINE,
			Ic2rBlocks.TELEPORTER,
			Ic2rBlocks.TERRAFORMER,
			Ic2rBlocks.TESLA_COIL,
			// Basic processing
			Ic2rBlocks.CANNER,
			Ic2rBlocks.COMPRESSOR,
			Ic2rBlocks.ELECTRIC_FURNACE,
			Ic2rBlocks.EXTRACTOR,
			Ic2rBlocks.MACERATOR,
			Ic2rBlocks.RECYCLER,
			Ic2rBlocks.SOLID_CANNER,
			// Advanced processing
			Ic2rBlocks.BLAST_FURNACE,
			Ic2rBlocks.BLOCK_CUTTER,
			Ic2rBlocks.CENTRIFUGE,
			Ic2rBlocks.FERMENTER,
			Ic2rBlocks.INDUCTION_FURNACE,
			Ic2rBlocks.METAL_FORMER,
			Ic2rBlocks.ORE_WASHING_PLANT,
			// Mining / crops / UU
			Ic2rBlocks.ADVANCED_MINER,
			Ic2rBlocks.CROP_HARVESTER,
			Ic2rBlocks.CROPMATRON,
			Ic2rBlocks.MINER,
			Ic2rBlocks.MATTER_GENERATOR,
			Ic2rBlocks.PATTERN_STORAGE,
			Ic2rBlocks.REPLICATOR,
			Ic2rBlocks.UU_SCANNER,
			Ic2rBlocks.ELECTROLYZER,
			// Energy storage / transformers
			Ic2rBlocks.BATBOX,
			Ic2rBlocks.CESU,
			Ic2rBlocks.MFE,
			Ic2rBlocks.MFSU,
			Ic2rBlocks.LV_TRANSFORMER,
			Ic2rBlocks.MV_TRANSFORMER,
			Ic2rBlocks.HV_TRANSFORMER,
			Ic2rBlocks.EV_TRANSFORMER,
			// Misc machines
			Ic2rBlocks.TANK,
			Ic2rBlocks.CHUNK_LOADER,
			Ic2rBlocks.STEAM_REPRESSURIZER,
			Ic2rBlocks.WEIGHTED_FLUID_DISTRIBUTOR,
			Ic2rBlocks.WEIGHTED_ITEM_DISTRIBUTOR,
			Ic2rBlocks.RCI_RSH,
			Ic2rBlocks.RCI_LZH,
			// Chargepads
			Ic2rBlocks.BATBOX_CHARGEPAD,
			Ic2rBlocks.CESU_CHARGEPAD,
			Ic2rBlocks.MFE_CHARGEPAD,
			Ic2rBlocks.MFSU_CHARGEPAD,
			// Storage boxes / tanks
			Ic2rBlocks.IRON_STORAGE_BOX,
			Ic2rBlocks.BRONZE_STORAGE_BOX,
			Ic2rBlocks.STEEL_STORAGE_BOX,
			Ic2rBlocks.IRIDIUM_STORAGE_BOX,
			Ic2rBlocks.BRONZE_TANK,
			Ic2rBlocks.IRON_TANK,
			Ic2rBlocks.STEEL_TANK,
			Ic2rBlocks.IRIDIUM_TANK,
			// Crafting
			Ic2rBlocks.INDUSTRIAL_WORKBENCH,
			Ic2rBlocks.BATCH_CRAFTER
		);
	}
}
