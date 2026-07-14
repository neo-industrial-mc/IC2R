package me.halfcooler.ic2r.core.ref.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import me.halfcooler.ic2r.core.item.ItemBlockIc2r;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorCondensator;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorDepletedUranium;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorHeatStorage;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorHeatSwitch;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorHeatpack;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorIridiumReflector;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorLithiumCell;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorMOX;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorPlating;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorReflector;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorUranium;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorVent;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorVentSpread;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rItems;

/** Domain item registrations: Nuclear reactor components */
public final class Ic2rItemsReactor
{
	private Ic2rItemsReactor()
	{
	}

	public static final Item REACTOR_COOLANT_CELL = Ic2rItems.register("reactor_coolant_cell", new ItemReactorHeatStorage(new Properties(), 10000));
	public static final Item TRIPLE_REACTOR_COOLANT_CELL = Ic2rItems.register("triple_reactor_coolant_cell", new ItemReactorHeatStorage(new Properties(), 30000));
	public static final Item SEXTUPLE_REACTOR_COOLANT_CELL = Ic2rItems.register("sextuple_reactor_coolant_cell", new ItemReactorHeatStorage(new Properties(), 60000));
	public static final Item REACTOR_PLATING = Ic2rItems.register("reactor_plating", new ItemReactorPlating(new Properties(), 1000, 0.95F));
	public static final Item REACTOR_HEAT_PLATING = Ic2rItems.register("reactor_heat_plating", new ItemReactorPlating(new Properties(), 2000, 0.99F));
	public static final Item CONTAINMENT_REACTOR_PLATING = Ic2rItems.register("containment_reactor_plating", new ItemReactorPlating(new Properties(), 500, 0.9F));
	public static final Item HEAT_EXCHANGER = Ic2rItems.register("heat_exchanger", new ItemReactorHeatSwitch(new Properties(), 2500, 12, 4));
	public static final Item REACTOR_HEAT_EXCHANGER = Ic2rItems.register("reactor_heat_exchanger", new ItemReactorHeatSwitch(new Properties(), 5000, 0, 72));
	public static final Item COMPONENT_HEAT_EXCHANGER = Ic2rItems.register("component_heat_exchanger", new ItemReactorHeatSwitch(new Properties(), 5000, 36, 0));
	public static final Item ADVANCED_HEAT_EXCHANGER = Ic2rItems.register("advanced_heat_exchanger", new ItemReactorHeatSwitch(new Properties(), 10000, 24, 8));
	public static final Item HEAT_VENT = Ic2rItems.register("heat_vent", new ItemReactorVent(new Properties(), 1000, 6, 0));
	public static final Item REACTOR_HEAT_VENT = Ic2rItems.register("reactor_heat_vent", new ItemReactorVent(new Properties(), 1000, 5, 5));
	public static final Item OVERCLOCKED_HEAT_VENT = Ic2rItems.register("overclocked_heat_vent", new ItemReactorVent(new Properties(), 1000, 20, 36));
	public static final Item COMPONENT_HEAT_VENT = Ic2rItems.register("component_heat_vent", new ItemReactorVentSpread(new Properties(), 4));
	public static final Item ADVANCED_HEAT_VENT = Ic2rItems.register("advanced_heat_vent", new ItemReactorVent(new Properties(), 1000, 12, 0));
	public static final Item NEUTRON_REFLECTOR = Ic2rItems.register("neutron_reflector", new ItemReactorReflector(new Properties(), 30000));
	public static final Item THICK_NEUTRON_REFLECTOR = Ic2rItems.register("thick_neutron_reflector", new ItemReactorReflector(new Properties(), 120000));
	public static final Item IRIDIUM_NEUTRON_REFLECTOR = Ic2rItems.register("iridium_neutron_reflector", new ItemReactorIridiumReflector(new Properties()));
	public static final Item RSH_CONDENSATOR = Ic2rItems.register("rsh_condensator", new ItemReactorCondensator(new Properties(), 20000));
	public static final Item LZH_CONDENSATOR = Ic2rItems.register("lzh_condensator", new ItemReactorCondensator(new Properties(), 100000));
	public static final Item HEATPACK = Ic2rItems.register("heatpack", new ItemReactorHeatpack(new Properties(), 1000, 1));
	public static final Item REACTOR_VESSEL = Ic2rItems.register("reactor_vessel", new ItemBlockIc2r(Ic2rBlocks.REACTOR_VESSEL, new Properties()));
	public static final Item URANIUM_FUEL_ROD = Ic2rItems.register("uranium_fuel_rod", new ItemReactorUranium(new Properties(), 1));
	public static final Item DUAL_URANIUM_FUEL_ROD = Ic2rItems.register("dual_uranium_fuel_rod", new ItemReactorUranium(new Properties(), 2));
	public static final Item QUAD_URANIUM_FUEL_ROD = Ic2rItems.register("quad_uranium_fuel_rod", new ItemReactorUranium(new Properties(), 4));
	public static final Item MOX_FUEL_ROD = Ic2rItems.register("mox_fuel_rod", new ItemReactorMOX(new Properties(), 1));
	public static final Item DUAL_MOX_FUEL_ROD = Ic2rItems.register("dual_mox_fuel_rod", new ItemReactorMOX(new Properties(), 2));
	public static final Item QUAD_MOX_FUEL_ROD = Ic2rItems.register("quad_mox_fuel_rod", new ItemReactorMOX(new Properties(), 4));
	public static final Item LITHIUM_FUEL_ROD = Ic2rItems.register("lithium_fuel_rod", new ItemReactorLithiumCell(new Properties()));
	public static final Item TRITIUM_FUEL_ROD = Ic2rItems.register("tritium_fuel_rod", new Item(new Properties()));
	public static final Item DEPLETED_ISOTOPE_FUEL_ROD = Ic2rItems.register("depleted_isotope_fuel_rod", new ItemReactorDepletedUranium(new Properties()));
}
