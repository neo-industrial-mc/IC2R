package me.halfcooler.ic2r.forge.ref;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Forge-side {@link DeferredRegister} + {@link RegistryObject} wiring for IC2R sound events.
 * Replaces the core {@link Supplier} stubs with resolved {@link RegistryObject} instances
 * after registration.
 */
public final class Ic2rSoundEventsForge
{
	public static final DeferredRegister<SoundEvent> REGISTRY =
		DeferredRegister.create(Registries.SOUND_EVENT, "ic2r");

	// Mirror of all 52 sound events
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_TREETAP_USE = reg("item.treetap.use");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_WRENCH_USE = reg("item.wrench.use");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_CUTTER_USE = reg("item.cutter.use");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_PAINTER_USE = reg("item.painter.use");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_CROWBAR_USE = reg("item.crowbar.use");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_ELECTRIC_SHUTDOWN = reg("item.electric.shutdown");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_BATTERY_USE = reg("item.battery.use");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_JETPACK_LOOP = reg("item.jetpack.loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_JETPACK_FIRE = reg("item.jetpack.fire");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_TREETAP_ELECTRIC_USE = reg("item.treetap.electric.use");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_CHAINSAW_IDLE = reg("item.chainsaw.idle");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_CHAINSAW_STOP = reg("item.chainsaw.stop");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_CHAINSAW_USE1 = reg("item.chainsaw.use1");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_CHAINSAW_USE2 = reg("item.chainsaw.use2");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_DRILL_IDLE = reg("item.drill.idle");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_DRILL_HARD = reg("item.drill.hard");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_DRILL_SOFT = reg("item.drill.soft");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_LASER_SHOOT = reg("item.laser.shoot");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_LASER_EXPLOSIVE = reg("item.laser.explosive");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_LASER_LONG_RANGE = reg("item.laser.long_range");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_LASER_LOW_FOCUS = reg("item.laser.low_focus");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_LASER_SCATTER = reg("item.laser.scatter");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_NANOSABER_IDLE = reg("item.nanosaber.idle");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_NANOSABER_POWER_UP = reg("item.nanosaber.power_up");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_NANOSABER_SWING1 = reg("item.nanosaber.swing1");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_NANOSABER_SWING2 = reg("item.nanosaber.swing2");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_NANOSABER_SWING3 = reg("item.nanosaber.swing3");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SCANNER_USE = reg("item.scanner.use");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_REMOTE_USE = reg("item.remote.use");
	public static final DeferredHolder<SoundEvent, SoundEvent> GENERATOR_GENERATOR_LOOP = reg("generator.generator.loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> GENERATOR_GEOTHERMAL_LOOP = reg("generator.geothermal.loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> GENERATOR_WATER_LOOP = reg("generator.water.loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> GENERATOR_WIND_LOOP = reg("generator.wind.loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> GENERATOR_NUCLEAR_LOOP = reg("generator.nuclear.loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> GENERATOR_NUCLEAR_LOW_POWER = reg("generator.nuclear.power.low");
	public static final DeferredHolder<SoundEvent, SoundEvent> GENERATOR_NUCLEAR_MEDIUM_POWER = reg("generator.nuclear.power.medium");
	public static final DeferredHolder<SoundEvent, SoundEvent> GENERATOR_NUCLEAR_HIGH_POWER = reg("generator.nuclear.power.high");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_OVERLOAD = reg("machine.overload");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_INTERRUPT1 = reg("machine.interrupt1");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_CANNER_OPERATE = reg("machine.canner.operate");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_CANNER_REVERSE = reg("machine.canner.reverse");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_COMPRESSOR_OPERATE = reg("machine.compressor.operate");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_ELECTROLYZER_LOOP = reg("machine.electrolyzer.loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_EXTRACTOR_OPERATE = reg("machine.extractor.operate");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_MATTER_GENERATOR_LOOP = reg("machine.matter_generator.loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_MATTER_GENERATOR_SCRAP = reg("machine.matter_generator.scrap");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_FURNACE_ELECTRIC_START = reg("machine.furnace.electric.start");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_FURNACE_ELECTRIC_STOP = reg("machine.furnace.electric.stop");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_FURNACE_ELECTRIC_LOOP = reg("machine.furnace.electric.loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_FURNACE_INDUCTION_START = reg("machine.furnace.induction.start");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_FURNACE_INDUCTION_STOP = reg("machine.furnace.induction.stop");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_FURNACE_INDUCTION_LOOP = reg("machine.furnace.induction.loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_FURNACE_IRON_OPERATE = reg("machine.furnace.iron.operate");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_MACERATOR_OPERATE = reg("machine.macerator.operate");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_MINER_OPERATE = reg("machine.miner.operate");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_OMAT_OPERATE = reg("machine.o_mat.operate");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_PUMP_OPERATE = reg("machine.pump.operate");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_RECYCLER_OPERATE = reg("machine.recycler.operate");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_TELEPORTER_USE = reg("machine.teleporter.use");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_TELEPORTER_CHARGE = reg("machine.teleporter.charge");
	public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_TERRAFORMER_LOOP = reg("machine.terraformer.loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> BLOCK_NUKE_EXPLODE = reg("block.nuke.explode");

	private Ic2rSoundEventsForge() {}

	public static void register(IEventBus modEventBus)
	{
		REGISTRY.register(modEventBus);
	}

	@SuppressWarnings({"unchecked"})
	public static void wireCoreFields()
	{
		try
		{
			Field[] forgeFields = Ic2rSoundEventsForge.class.getDeclaredFields();
			for (Field forgeField : forgeFields)
			{
				if (!Modifier.isStatic(forgeField.getModifiers())) continue;
				if (!DeferredHolder.class.isAssignableFrom(forgeField.getType())) continue;

				String name = forgeField.getName();
				Field coreField;
				try
				{
					coreField = Ic2rSoundEvents.class.getDeclaredField(name);
				}
				catch (NoSuchFieldException e)
				{
					continue;
				}

				DeferredHolder<SoundEvent, SoundEvent> ro = (DeferredHolder<SoundEvent, SoundEvent>) forgeField.get(null);
				coreField.setAccessible(true);
				coreField.set(null, (Supplier<SoundEvent>) ro);
			}
		}
		catch (ReflectiveOperationException e)
		{
			throw new RuntimeException("Failed to wire Ic2rSoundEvents core fields", e);
		}
	}

	private static DeferredHolder<SoundEvent, SoundEvent> reg(String path)
	{
		return REGISTRY.register(path,
			() -> SoundEvent.createVariableRangeEvent(IC2R.getIdentifier(path)));
	}
}
