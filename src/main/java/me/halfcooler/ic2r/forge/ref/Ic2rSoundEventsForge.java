package me.halfcooler.ic2r.forge.ref;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

/**
 * Forge-side {@link DeferredRegister} + {@link RegistryObject} wiring for IC2R sound events.
 * Replaces the core {@link Supplier} stubs with resolved {@link RegistryObject} instances
 * after registration.
 */
public final class Ic2rSoundEventsForge
{
	public static final DeferredRegister<SoundEvent> REGISTRY =
		DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "ic2r");

	// Mirror of all 52 sound events
	public static final RegistryObject<SoundEvent> ITEM_TREETAP_USE = reg("item.treetap.use");
	public static final RegistryObject<SoundEvent> ITEM_WRENCH_USE = reg("item.wrench.use");
	public static final RegistryObject<SoundEvent> ITEM_CUTTER_USE = reg("item.cutter.use");
	public static final RegistryObject<SoundEvent> ITEM_PAINTER_USE = reg("item.painter.use");
	public static final RegistryObject<SoundEvent> ITEM_CROWBAR_USE = reg("item.crowbar.use");
	public static final RegistryObject<SoundEvent> ITEM_ELECTRIC_SHUTDOWN = reg("item.electric.shutdown");
	public static final RegistryObject<SoundEvent> ITEM_BATTERY_USE = reg("item.battery.use");
	public static final RegistryObject<SoundEvent> ITEM_JETPACK_LOOP = reg("item.jetpack.loop");
	public static final RegistryObject<SoundEvent> ITEM_JETPACK_FIRE = reg("item.jetpack.fire");
	public static final RegistryObject<SoundEvent> ITEM_TREETAP_ELECTRIC_USE = reg("item.treetap.electric.use");
	public static final RegistryObject<SoundEvent> ITEM_CHAINSAW_IDLE = reg("item.chainsaw.idle");
	public static final RegistryObject<SoundEvent> ITEM_CHAINSAW_STOP = reg("item.chainsaw.stop");
	public static final RegistryObject<SoundEvent> ITEM_CHAINSAW_USE1 = reg("item.chainsaw.use1");
	public static final RegistryObject<SoundEvent> ITEM_CHAINSAW_USE2 = reg("item.chainsaw.use2");
	public static final RegistryObject<SoundEvent> ITEM_DRILL_IDLE = reg("item.drill.idle");
	public static final RegistryObject<SoundEvent> ITEM_DRILL_HARD = reg("item.drill.hard");
	public static final RegistryObject<SoundEvent> ITEM_DRILL_SOFT = reg("item.drill.soft");
	public static final RegistryObject<SoundEvent> ITEM_LASER_SHOOT = reg("item.laser.shoot");
	public static final RegistryObject<SoundEvent> ITEM_LASER_EXPLOSIVE = reg("item.laser.explosive");
	public static final RegistryObject<SoundEvent> ITEM_LASER_LONG_RANGE = reg("item.laser.long_range");
	public static final RegistryObject<SoundEvent> ITEM_LASER_LOW_FOCUS = reg("item.laser.low_focus");
	public static final RegistryObject<SoundEvent> ITEM_LASER_SCATTER = reg("item.laser.scatter");
	public static final RegistryObject<SoundEvent> ITEM_NANOSABER_IDLE = reg("item.nanosaber.idle");
	public static final RegistryObject<SoundEvent> ITEM_NANOSABER_POWER_UP = reg("item.nanosaber.power_up");
	public static final RegistryObject<SoundEvent> ITEM_NANOSABER_SWING1 = reg("item.nanosaber.swing1");
	public static final RegistryObject<SoundEvent> ITEM_NANOSABER_SWING2 = reg("item.nanosaber.swing2");
	public static final RegistryObject<SoundEvent> ITEM_NANOSABER_SWING3 = reg("item.nanosaber.swing3");
	public static final RegistryObject<SoundEvent> ITEM_SCANNER_USE = reg("item.scanner.use");
	public static final RegistryObject<SoundEvent> ITEM_REMOTE_USE = reg("item.remote.use");
	public static final RegistryObject<SoundEvent> GENERATOR_GENERATOR_LOOP = reg("generator.generator.loop");
	public static final RegistryObject<SoundEvent> GENERATOR_GEOTHERMAL_LOOP = reg("generator.geothermal.loop");
	public static final RegistryObject<SoundEvent> GENERATOR_WATER_LOOP = reg("generator.water.loop");
	public static final RegistryObject<SoundEvent> GENERATOR_WIND_LOOP = reg("generator.wind.loop");
	public static final RegistryObject<SoundEvent> GENERATOR_NUCLEAR_LOOP = reg("generator.nuclear.loop");
	public static final RegistryObject<SoundEvent> GENERATOR_NUCLEAR_LOW_POWER = reg("generator.nuclear.power.low");
	public static final RegistryObject<SoundEvent> GENERATOR_NUCLEAR_MEDIUM_POWER = reg("generator.nuclear.power.medium");
	public static final RegistryObject<SoundEvent> GENERATOR_NUCLEAR_HIGH_POWER = reg("generator.nuclear.power.high");
	public static final RegistryObject<SoundEvent> MACHINE_OVERLOAD = reg("machine.overload");
	public static final RegistryObject<SoundEvent> MACHINE_INTERRUPT1 = reg("machine.interrupt1");
	public static final RegistryObject<SoundEvent> MACHINE_CANNER_OPERATE = reg("machine.canner.operate");
	public static final RegistryObject<SoundEvent> MACHINE_CANNER_REVERSE = reg("machine.canner.reverse");
	public static final RegistryObject<SoundEvent> MACHINE_COMPRESSOR_OPERATE = reg("machine.compressor.operate");
	public static final RegistryObject<SoundEvent> MACHINE_ELECTROLYZER_LOOP = reg("machine.electrolyzer.loop");
	public static final RegistryObject<SoundEvent> MACHINE_EXTRACTOR_OPERATE = reg("machine.extractor.operate");
	public static final RegistryObject<SoundEvent> MACHINE_MATTER_GENERATOR_LOOP = reg("machine.matter_generator.loop");
	public static final RegistryObject<SoundEvent> MACHINE_MATTER_GENERATOR_SCRAP = reg("machine.matter_generator.scrap");
	public static final RegistryObject<SoundEvent> MACHINE_FURNACE_ELECTRIC_START = reg("machine.furnace.electric.start");
	public static final RegistryObject<SoundEvent> MACHINE_FURNACE_ELECTRIC_STOP = reg("machine.furnace.electric.stop");
	public static final RegistryObject<SoundEvent> MACHINE_FURNACE_ELECTRIC_LOOP = reg("machine.furnace.electric.loop");
	public static final RegistryObject<SoundEvent> MACHINE_FURNACE_INDUCTION_START = reg("machine.furnace.induction.start");
	public static final RegistryObject<SoundEvent> MACHINE_FURNACE_INDUCTION_STOP = reg("machine.furnace.induction.stop");
	public static final RegistryObject<SoundEvent> MACHINE_FURNACE_INDUCTION_LOOP = reg("machine.furnace.induction.loop");
	public static final RegistryObject<SoundEvent> MACHINE_FURNACE_IRON_OPERATE = reg("machine.furnace.iron.operate");
	public static final RegistryObject<SoundEvent> MACHINE_MACERATOR_OPERATE = reg("machine.macerator.operate");
	public static final RegistryObject<SoundEvent> MACHINE_MINER_OPERATE = reg("machine.miner.operate");
	public static final RegistryObject<SoundEvent> MACHINE_OMAT_OPERATE = reg("machine.o_mat.operate");
	public static final RegistryObject<SoundEvent> MACHINE_PUMP_OPERATE = reg("machine.pump.operate");
	public static final RegistryObject<SoundEvent> MACHINE_RECYCLER_OPERATE = reg("machine.recycler.operate");
	public static final RegistryObject<SoundEvent> MACHINE_TELEPORTER_USE = reg("machine.teleporter.use");
	public static final RegistryObject<SoundEvent> MACHINE_TELEPORTER_CHARGE = reg("machine.teleporter.charge");
	public static final RegistryObject<SoundEvent> MACHINE_TERRAFORMER_LOOP = reg("machine.terraformer.loop");
	public static final RegistryObject<SoundEvent> BLOCK_NUKE_EXPLODE = reg("block.nuke.explode");

	private Ic2rSoundEventsForge() {}

	public static void register(IEventBus modEventBus)
	{
		REGISTRY.register(modEventBus);
	}

	/**
	 * Called after registry freeze to copy {@link RegistryObject} references back to
	 * the core class so {@code Ic2rSoundEvents.X.get()} resolves correctly.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void wireCoreFields()
	{
		try
		{
			Field[] forgeFields = Ic2rSoundEventsForge.class.getDeclaredFields();
			for (Field forgeField : forgeFields)
			{
				if (!Modifier.isStatic(forgeField.getModifiers())) continue;
				if (!RegistryObject.class.isAssignableFrom(forgeField.getType())) continue;

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

				RegistryObject<SoundEvent> ro = (RegistryObject<SoundEvent>) forgeField.get(null);
				coreField.setAccessible(true);
				coreField.set(null, (Supplier<SoundEvent>) ro);
			}
		}
		catch (ReflectiveOperationException e)
		{
			throw new RuntimeException("Failed to wire Ic2rSoundEvents core fields", e);
		}
	}

	private static RegistryObject<SoundEvent> reg(String path)
	{
		return REGISTRY.register(path,
			() -> SoundEvent.createVariableRangeEvent(IC2R.getIdentifier(path)));
	}
}
