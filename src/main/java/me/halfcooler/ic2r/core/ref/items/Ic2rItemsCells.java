package me.halfcooler.ic2r.core.ref.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.material.Fluids;
import me.halfcooler.ic2r.core.item.ItemClassicCell;
import me.halfcooler.ic2r.core.item.ItemHydrationCell;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.core.ref.Ic2rItems;

/** Domain item registrations: Fluid and special cells */
public final class Ic2rItemsCells
{
	private Ic2rItemsCells()
	{
	}

	public static final Item FACADE_CELL = Ic2rItems.register("facade_cell", new ItemClassicCell(new Properties(), Fluids.EMPTY));
	public static final Item WATER_CELL = Ic2rItems.register("water_cell", new ItemClassicCell(new Properties(), Fluids.WATER));
	public static final Item LAVA_CELL = Ic2rItems.register("lava_cell", new ItemClassicCell(new Properties(), Fluids.LAVA));
	public static final Item AIR_CELL = Ic2rItems.register("air_cell", new ItemClassicCell(new Properties(), Ic2rFluids.AIR.still()));
	// Not a fluid cell: plain item only (null-fluid ItemClassicCell crashes GTCEu fluid tooltips).
	public static final Item ELECTROLYZED_WATER_CELL = Ic2rItems.register("electrolyzed_water_cell", new Item(new Properties()));
	public static final Item WEED_EX_CELL = Ic2rItems.register("weed_ex_cell", new ItemClassicCell(new Properties(), Ic2rFluids.WEED_EX.still()));
	// Not a fluid cell: durability-tracked crop item; see ItemHydrationCell.
	public static final Item HYDRATION_CELL = Ic2rItems.register("hydration_cell", new ItemHydrationCell(new Properties().stacksTo(1)));
	public static final Item UU_MATTER_CELL = Ic2rItems.register("uu_matter_cell", new ItemClassicCell(new Properties(), Ic2rFluids.UU_MATTER.still()));
	public static final Item CONSTRUCTION_FOAM_CELL = Ic2rItems.register("construction_foam_cell", new ItemClassicCell(new Properties(), Ic2rFluids.CONSTRUCTION_FOAM.still()));
	public static final Item COOLANT_CELL = Ic2rItems.register("coolant_cell", new ItemClassicCell(new Properties(), Ic2rFluids.COOLANT.still()));
	public static final Item CREOSOTE_CELL = Ic2rItems.register("creosote_cell", new ItemClassicCell(new Properties(), Ic2rFluids.CREOSOTE.still()));
	public static final Item HOT_COOLANT_CELL = Ic2rItems.register("hot_coolant_cell", new ItemClassicCell(new Properties(), Ic2rFluids.HOT_COOLANT.still()));
	public static final Item PAHOEHOE_LAVA_CELL = Ic2rItems.register("pahoehoe_lava_cell", new ItemClassicCell(new Properties(), Ic2rFluids.PAHOEHOE_LAVA.still()));
	public static final Item BIOMASS_CELL = Ic2rItems.register("biomass_cell", new ItemClassicCell(new Properties(), Ic2rFluids.BIOMASS.still()));
	public static final Item BIOGAS_CELL = Ic2rItems.register("biogas_cell", new ItemClassicCell(new Properties(), Ic2rFluids.BIOGAS.still()));
	public static final Item DISTILLED_WATER_CELL = Ic2rItems.register("distilled_water_cell", new ItemClassicCell(new Properties(), Ic2rFluids.DISTILLED_WATER.still()));
	public static final Item SUPERHEATED_STEAM_CELL = Ic2rItems.register("superheated_steam_cell", new ItemClassicCell(new Properties(), Ic2rFluids.SUPERHEATED_STEAM.still()));
	public static final Item STEAM_CELL = Ic2rItems.register("steam_cell", new ItemClassicCell(new Properties(), Ic2rFluids.STEAM.still()));
	public static final Item HOT_WATER_CELL = Ic2rItems.register("hot_water_cell", new ItemClassicCell(new Properties(), Ic2rFluids.HOT_WATER.still()));
	public static final Item HYDROGEN_CELL = Ic2rItems.register("hydrogen_cell", new ItemClassicCell(new Properties(), Ic2rFluids.HYDROGEN.still()));
	public static final Item OXYGEN_CELL = Ic2rItems.register("oxygen_cell", new ItemClassicCell(new Properties(), Ic2rFluids.OXYGEN.still()));
	public static final Item HEAVY_WATER_CELL = Ic2rItems.register("heavy_water_cell", new ItemClassicCell(new Properties(), Ic2rFluids.HEAVY_WATER.still()));
}
