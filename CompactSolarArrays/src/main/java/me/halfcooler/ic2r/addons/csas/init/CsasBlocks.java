package me.halfcooler.ic2r.addons.csas.init;

import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.util.Util;
import me.halfcooler.ic2r.addons.csas.CsasMod;
import me.halfcooler.ic2r.addons.csas.blockentity.TileEntityCompactSolar;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CsasBlocks
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CsasMod.MOD_ID);

	private static final BlockBehaviour.Properties PROPERTIES = BlockBehaviour.Properties.of()
		.mapColor(MapColor.COLOR_LIGHT_GRAY)
		.strength(2.0F, 10.0F)
		.requiresCorrectToolForDrops()
		.sound(SoundType.METAL);

	public static final RegistryObject<Block> LOW_VOLTAGE_SOLAR_ARRAY = registerSolarArray("low_voltage_solar_array");
	public static final RegistryObject<Block> MEDIUM_VOLTAGE_SOLAR_ARRAY = registerSolarArray("medium_voltage_solar_array");
	public static final RegistryObject<Block> HIGH_VOLTAGE_SOLAR_ARRAY = registerSolarArray("high_voltage_solar_array");

	private static RegistryObject<Block> registerSolarArray(String name)
	{
		return BLOCKS.register(name, () -> Ic2TileEntityBlock.create(
			PROPERTIES,
			TileEntityCompactSolar.class,
			true,
			Ic2TileEntityBlock.DefaultDrop.Generator,
			Util.horizontalFacings,
			true
		));
	}

	private CsasBlocks()
	{
	}
}