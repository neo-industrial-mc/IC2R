package me.halfcooler.ic2r.addons.csas.init;

import ic2.core.item.ItemBlockIc2;
import me.halfcooler.ic2r.addons.csas.CsasMod;
import me.halfcooler.ic2r.addons.csas.common.CompactSolarType;
import me.halfcooler.ic2r.addons.csas.item.ItemSolarHat;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CsasItems
{
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CsasMod.MOD_ID);

	public static final RegistryObject<Item> LOW_VOLTAGE_SOLAR_ARRAY = registerBlockItem("low_voltage_solar_array", CsasBlocks.LOW_VOLTAGE_SOLAR_ARRAY);
	public static final RegistryObject<Item> MEDIUM_VOLTAGE_SOLAR_ARRAY = registerBlockItem("medium_voltage_solar_array", CsasBlocks.MEDIUM_VOLTAGE_SOLAR_ARRAY);
	public static final RegistryObject<Item> HIGH_VOLTAGE_SOLAR_ARRAY = registerBlockItem("high_voltage_solar_array", CsasBlocks.HIGH_VOLTAGE_SOLAR_ARRAY);

	public static final RegistryObject<Item> SOLAR_HAT_LOW_VOLTAGE = registerSolarHat("solar_hat_low_voltage", CompactSolarType.LOW_VOLTAGE);
	public static final RegistryObject<Item> SOLAR_HAT_MEDIUM_VOLTAGE = registerSolarHat("solar_hat_medium_voltage", CompactSolarType.MEDIUM_VOLTAGE);
	public static final RegistryObject<Item> SOLAR_HAT_HIGH_VOLTAGE = registerSolarHat("solar_hat_high_voltage", CompactSolarType.HIGH_VOLTAGE);

	private static RegistryObject<Item> registerBlockItem(String name, RegistryObject<net.minecraft.world.level.block.Block> block)
	{
		return ITEMS.register(name, () -> new ItemBlockIc2(block.get(), new Item.Properties()));
	}

	private static RegistryObject<Item> registerSolarHat(String name, CompactSolarType type)
	{
		return ITEMS.register(name, () -> new ItemSolarHat(type, new Item.Properties()));
	}

	private CsasItems()
	{
	}
}