package me.halfcooler.ic2r.core.ref.blocks;

import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import me.halfcooler.ic2r.core.block.wiring.CableBlock;
import me.halfcooler.ic2r.core.block.wiring.CableType;
import me.halfcooler.ic2r.core.block.wiring.DetectorCableBlock;
import me.halfcooler.ic2r.core.block.wiring.DetectorFoamCableBlock;
import me.halfcooler.ic2r.core.block.wiring.FoamCableBlock;
import me.halfcooler.ic2r.core.block.wiring.SplitterCableBlock;
import me.halfcooler.ic2r.core.block.wiring.SplitterFoamCableBlock;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityChargePadBatBox;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityLuminator;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityChargePadCESU;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityChargePadMFE;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityChargePadMFSU;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityElectricBatBox;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityElectricCESU;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityElectricMFE;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityElectricMFSU;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityTransformerEV;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityTransformerHV;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityTransformerLV;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityTransformerMV;
import me.halfcooler.ic2r.core.util.Util;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;


import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;
import me.halfcooler.ic2r.forge.EnvProxyForge;

/** Domain block registrations: cables, transformers, energy storage, chargepads, luminator */
public final class Ic2rBlocksWiring
{
	private Ic2rBlocksWiring()
	{
	}

	public static final RegistryObject<Block> LUMINATOR_FLAT = EnvProxyForge.BLOCKS.register("luminator", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion().noCollission().lightLevel(state -> state.getValue(Ic2rTileEntityBlock.ACTIVE) ? 15 : 0), TileEntityLuminator.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, false));
	public static final RegistryObject<FoamCableBlock> GLASS_FIBRE_FOAM_CABLE = EnvProxyForge.BLOCKS.register("glass_fibre_foam_cable", () -> FoamCableBlock.create(Properties.of().strength(0.5F, 5.0F).sound(SoundType.GLASS), CableType.glass, 0));
	public static final RegistryObject<Block> GLASS_FIBRE_CABLE = EnvProxyForge.BLOCKS.register("glass_fibre_cable", () -> CableBlock.create(Properties.of().strength(0.5F, 5.0F).sound(SoundType.GLASS), CableType.glass, 0, GLASS_FIBRE_FOAM_CABLE.get()));
	public static final RegistryObject<Block> BATBOX_CHARGEPAD = EnvProxyForge.BLOCKS.register("batbox_chargepad", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(), TileEntityChargePadBatBox.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.downSideFacings, true));
	public static final RegistryObject<Block> CESU_CHARGEPAD = EnvProxyForge.BLOCKS.register("cesu_chargepad", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(), TileEntityChargePadCESU.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.downSideFacings, true));
	public static final RegistryObject<Block> MFE_CHARGEPAD = EnvProxyForge.BLOCKS.register("mfe_chargepad", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(), TileEntityChargePadMFE.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.downSideFacings, true));
	public static final RegistryObject<Block> MFSU_CHARGEPAD = EnvProxyForge.BLOCKS.register("mfsu_chargepad", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(), TileEntityChargePadMFSU.class, true, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.downSideFacings, true));
	public static final RegistryObject<Block> BATBOX = EnvProxyForge.BLOCKS.register("batbox", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricBatBox.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final RegistryObject<Block> CESU = EnvProxyForge.BLOCKS.register("cesu", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricCESU.class, false, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final RegistryObject<Block> MFE = EnvProxyForge.BLOCKS.register("mfe", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricMFE.class, false, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final RegistryObject<Block> MFSU = EnvProxyForge.BLOCKS.register("mfsu", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricMFSU.class, false, Ic2rTileEntityBlock.DefaultDrop.AdvMachine, Util.allFacings, true));
	public static final RegistryObject<Block> LV_TRANSFORMER = EnvProxyForge.BLOCKS.register("lv_transformer", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTransformerLV.class, true, Ic2rTileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final RegistryObject<Block> MV_TRANSFORMER = EnvProxyForge.BLOCKS.register("mv_transformer", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTransformerMV.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final RegistryObject<Block> HV_TRANSFORMER = EnvProxyForge.BLOCKS.register("hv_transformer", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTransformerHV.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final RegistryObject<Block> EV_TRANSFORMER = EnvProxyForge.BLOCKS.register("ev_transformer", () -> Ic2rTileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTransformerEV.class, true, Ic2rTileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	private static final Properties cableSettings = Properties.of().strength(0.5F, 5.0F).sound(SoundType.METAL);
	public static final RegistryObject<FoamCableBlock> COPPER_FOAM_CABLE = EnvProxyForge.BLOCKS.register("copper_foam_cable", () -> FoamCableBlock.create(cableSettings, CableType.copper, 0));
	public static final RegistryObject<Block> COPPER_CABLE = EnvProxyForge.BLOCKS.register("copper_cable", () -> CableBlock.create(cableSettings, CableType.copper, 0, COPPER_FOAM_CABLE.get()));
	public static final RegistryObject<FoamCableBlock> GOLD_FOAM_CABLE = EnvProxyForge.BLOCKS.register("gold_foam_cable", () -> FoamCableBlock.create(cableSettings, CableType.gold, 0));
	public static final RegistryObject<Block> GOLD_CABLE = EnvProxyForge.BLOCKS.register("gold_cable", () -> CableBlock.create(cableSettings, CableType.gold, 0, GOLD_FOAM_CABLE.get()));
	public static final RegistryObject<FoamCableBlock> IRON_FOAM_CABLE = EnvProxyForge.BLOCKS.register("iron_foam_cable", () -> FoamCableBlock.create(cableSettings, CableType.iron, 0));
	public static final RegistryObject<Block> IRON_CABLE = EnvProxyForge.BLOCKS.register("iron_cable", () -> CableBlock.create(cableSettings, CableType.iron, 0, IRON_FOAM_CABLE.get()));
	public static final RegistryObject<FoamCableBlock> TIN_FOAM_CABLE = EnvProxyForge.BLOCKS.register("tin_foam_cable", () -> FoamCableBlock.create(cableSettings, CableType.tin, 0));
	public static final RegistryObject<Block> TIN_CABLE = EnvProxyForge.BLOCKS.register("tin_cable", () -> CableBlock.create(cableSettings, CableType.tin, 0, TIN_FOAM_CABLE.get()));
	public static final RegistryObject<DetectorFoamCableBlock> DETECTOR_FOAM_CABLE = EnvProxyForge.BLOCKS.register("detector_foam_cable", () -> DetectorFoamCableBlock.create(cableSettings));
	public static final RegistryObject<Block> DETECTOR_CABLE = EnvProxyForge.BLOCKS.register("detector_cable", () -> DetectorCableBlock.create(cableSettings, DETECTOR_FOAM_CABLE.get()));
	public static final RegistryObject<SplitterFoamCableBlock> SPLITTER_FOAM_CABLE = EnvProxyForge.BLOCKS.register("splitter_foam_cable", () -> SplitterFoamCableBlock.create(cableSettings));
	public static final RegistryObject<Block> SPLITTER_CABLE = EnvProxyForge.BLOCKS.register("splitter_cable", () -> SplitterCableBlock.create(cableSettings, SPLITTER_FOAM_CABLE.get()));
	private static final Properties insulatedCableSettings = Properties.of().strength(0.5F, 5.0F).sound(SoundType.WOOL);
	public static final RegistryObject<FoamCableBlock> INSULATED_COPPER_FOAM_CABLE = EnvProxyForge.BLOCKS.register("insulated_copper_foam_cable", () -> FoamCableBlock.create(insulatedCableSettings, CableType.copper, 1));
	public static final RegistryObject<Block> INSULATED_COPPER_CABLE = EnvProxyForge.BLOCKS.register("insulated_copper_cable", () -> CableBlock.create(insulatedCableSettings, CableType.copper, 1, INSULATED_COPPER_FOAM_CABLE.get()));
	public static final RegistryObject<FoamCableBlock> INSULATED_GOLD_FOAM_CABLE = EnvProxyForge.BLOCKS.register("insulated_gold_foam_cable", () -> FoamCableBlock.create(insulatedCableSettings, CableType.gold, 1));
	public static final RegistryObject<Block> INSULATED_GOLD_CABLE = EnvProxyForge.BLOCKS.register("insulated_gold_cable", () -> CableBlock.create(insulatedCableSettings, CableType.gold, 1, INSULATED_GOLD_FOAM_CABLE.get()));
	public static final RegistryObject<FoamCableBlock> DOUBLE_INSULATED_GOLD_FOAM_CABLE = EnvProxyForge.BLOCKS.register("double_insulated_gold_foam_cable", () -> FoamCableBlock.create(insulatedCableSettings, CableType.gold, 2));
	public static final RegistryObject<Block> DOUBLE_INSULATED_GOLD_CABLE = EnvProxyForge.BLOCKS.register("double_insulated_gold_cable", () -> CableBlock.create(insulatedCableSettings, CableType.gold, 2, DOUBLE_INSULATED_GOLD_FOAM_CABLE.get()));
	public static final RegistryObject<FoamCableBlock> INSULATED_IRON_FOAM_CABLE = EnvProxyForge.BLOCKS.register("insulated_iron_foam_cable", () -> FoamCableBlock.create(insulatedCableSettings, CableType.iron, 1));
	public static final RegistryObject<Block> INSULATED_IRON_CABLE = EnvProxyForge.BLOCKS.register("insulated_iron_cable", () -> CableBlock.create(insulatedCableSettings, CableType.iron, 1, INSULATED_IRON_FOAM_CABLE.get()));
	public static final RegistryObject<FoamCableBlock> DOUBLE_INSULATED_IRON_FOAM_CABLE = EnvProxyForge.BLOCKS.register("double_insulated_iron_foam_cable", () -> FoamCableBlock.create(insulatedCableSettings, CableType.iron, 2));
	public static final RegistryObject<Block> DOUBLE_INSULATED_IRON_CABLE = EnvProxyForge.BLOCKS.register("double_insulated_iron_cable", () -> CableBlock.create(insulatedCableSettings, CableType.iron, 2, DOUBLE_INSULATED_IRON_FOAM_CABLE.get()));
	public static final RegistryObject<FoamCableBlock> TRIPLE_INSULATED_IRON_FOAM_CABLE = EnvProxyForge.BLOCKS.register("triple_insulated_iron_foam_cable", () -> FoamCableBlock.create(insulatedCableSettings, CableType.iron, 3));
	public static final RegistryObject<Block> TRIPLE_INSULATED_IRON_CABLE = EnvProxyForge.BLOCKS.register("triple_insulated_iron_cable", () -> CableBlock.create(insulatedCableSettings, CableType.iron, 3, TRIPLE_INSULATED_IRON_FOAM_CABLE.get()));
	public static final RegistryObject<FoamCableBlock> INSULATED_TIN_FOAM_CABLE = EnvProxyForge.BLOCKS.register("insulated_tin_foam_cable", () -> FoamCableBlock.create(insulatedCableSettings, CableType.tin, 1));
	public static final RegistryObject<Block> INSULATED_TIN_CABLE = EnvProxyForge.BLOCKS.register("insulated_tin_cable", () -> CableBlock.create(insulatedCableSettings, CableType.tin, 1, INSULATED_TIN_FOAM_CABLE.get()));
}
