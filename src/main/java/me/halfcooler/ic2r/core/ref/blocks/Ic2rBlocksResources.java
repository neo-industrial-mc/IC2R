package me.halfcooler.ic2r.core.ref.blocks;

import me.halfcooler.ic2r.core.block.steam.BlockRefractoryBricks;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;


import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;
import me.halfcooler.ic2r.forge.EnvProxyForge;

/** Domain block registrations: ores, metal blocks, machine casings, structural resources */
public final class Ic2rBlocksResources
{
	private Ic2rBlocksResources()
	{
	}

	public static final RegistryObject<Block> LEAD_ORE = EnvProxyForge.BLOCKS.register("lead_ore", () -> new Block(Properties.of().strength(2.0F, 4.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> TIN_ORE = EnvProxyForge.BLOCKS.register("tin_ore", () -> new Block(Properties.of().strength(3.0F, 5.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> URANIUM_ORE = EnvProxyForge.BLOCKS.register("uranium_ore", () -> new Block(Properties.of().strength(4.0F, 6.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> DEEPSLATE_LEAD_ORE = EnvProxyForge.BLOCKS.register("deepslate_lead_ore", () -> new Block(Properties.of().mapColor(MapColor.DEEPSLATE).strength(3.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
	public static final RegistryObject<Block> DEEPSLATE_TIN_ORE = EnvProxyForge.BLOCKS.register("deepslate_tin_ore", () -> new Block(Properties.of().mapColor(MapColor.DEEPSLATE).strength(4.0F, 7.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
	public static final RegistryObject<Block> DEEPSLATE_URANIUM_ORE = EnvProxyForge.BLOCKS.register("deepslate_uranium_ore", () -> new Block(Properties.of().mapColor(MapColor.DEEPSLATE).strength(5.0F, 8.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
	public static final RegistryObject<Block> RAW_LEAD_BLOCK = EnvProxyForge.BLOCKS.register("raw_lead_block", () -> new Block(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(3.0F, 11.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> RAW_TIN_BLOCK = EnvProxyForge.BLOCKS.register("raw_tin_block", () -> new Block(Properties.of().mapColor(MapColor.SNOW).strength(4.0F, 12.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> RAW_URANIUM_BLOCK = EnvProxyForge.BLOCKS.register("raw_uranium_block", () -> new Block(Properties.of().mapColor(MapColor.COLOR_GREEN).strength(5.0F, 13.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> BRONZE_BLOCK = EnvProxyForge.BLOCKS.register("bronze_block", () -> new Block(Properties.of().strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final RegistryObject<Block> LEAD_BLOCK = EnvProxyForge.BLOCKS.register("lead_block", () -> new Block(Properties.of().strength(4.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final RegistryObject<Block> STEEL_BLOCK = EnvProxyForge.BLOCKS.register("steel_block", () -> new Block(Properties.of().strength(8.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final RegistryObject<Block> TIN_BLOCK = EnvProxyForge.BLOCKS.register("tin_block", () -> new Block(Properties.of().strength(4.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final RegistryObject<Block> URANIUM_BLOCK = EnvProxyForge.BLOCKS.register("uranium_block", () -> new Block(Properties.of().strength(6.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final RegistryObject<Block> REINFORCED_STONE = EnvProxyForge.BLOCKS.register("reinforced_stone", () -> new Block(Properties.of().strength(80.0F, 180.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> REFRACTORY_BRICKS = EnvProxyForge.BLOCKS.register("refractory_bricks", () -> new BlockRefractoryBricks());
	public static final RegistryObject<Block> MACHINE = EnvProxyForge.BLOCKS.register("machine", () -> new Block(Properties.of().strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final RegistryObject<Block> ADVANCED_MACHINE = EnvProxyForge.BLOCKS.register("advanced_machine", () -> new Block(Properties.of().strength(8.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final RegistryObject<Block> REACTOR_VESSEL = EnvProxyForge.BLOCKS.register("reactor_vessel", () -> new Block(Properties.of().strength(40.0F, 90.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> SILVER_BLOCK = EnvProxyForge.BLOCKS.register("silver_block", () -> new Block(Properties.of().strength(4.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
}
