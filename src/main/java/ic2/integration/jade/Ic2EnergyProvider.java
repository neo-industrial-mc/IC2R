package ic2.integration.jade;

import ic2.core.IC2;
import ic2.core.block.comp.Energy;
import ic2.core.block.tileentity.Ic2TileEntity;

import java.util.List;
import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

/**
 * Exposes IC2 EU buffers to Jade's universal energy storage UI.
 * Forge ENERGY (FE) is intentionally not used — IC2R runs on EU.
 */
public enum Ic2EnergyProvider implements IServerExtensionProvider<Object, CompoundTag>, IClientExtensionProvider<CompoundTag, EnergyView>
{
	INSTANCE;

	public static final ResourceLocation UID = IC2.getIdentifier("energy_storage");
	private static final String UNIT = "EU";

	@Override
	public ResourceLocation getUid()
	{
		return UID;
	}

	@Override
	public List<ViewGroup<CompoundTag>> getGroups(ServerPlayer player, ServerLevel world, Object target, boolean showDetails)
	{
		if (!(target instanceof Ic2TileEntity te) || !te.hasComponent(Energy.class))
		{
			return null;
		}

		Energy energy = te.getComponent(Energy.class);
		long capacity = Math.max(0L, (long) energy.getCapacity());
		if (capacity <= 0L)
		{
			return null;
		}

		long stored = Math.max(0L, Math.min(capacity, (long) energy.getEnergy()));
		ViewGroup<CompoundTag> group = new ViewGroup<>(List.of(EnergyView.of(stored, capacity)));
		group.getExtraData().putString("Unit", UNIT);
		return List.of(group);
	}

	@Override
	public List<ClientViewGroup<EnergyView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups)
	{
		return groups.stream().map(group ->
		{
			String unit = group.getExtraData().getString("Unit");
			if (unit.isEmpty())
			{
				unit = UNIT;
			}

			String finalUnit = unit;
			return new ClientViewGroup<>(
				group.views.stream().map(tag -> EnergyView.read(tag, finalUnit)).filter(Objects::nonNull).toList()
			);
		}).toList();
	}
}
