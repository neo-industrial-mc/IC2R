package me.halfcooler.ic2r.integration.jade;

import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.addon.harvest.SimpleToolHandler;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class Ic2rJadePlugin implements IWailaPlugin
{
	@Override
	public void register(IWailaCommonRegistration registration)
	{
		registration.registerProgress(Ic2rProgressProvider.INSTANCE, Ic2rTileEntity.class);
		registration.registerBlockDataProvider(Ic2rEnergyProvider.INSTANCE, Ic2rTileEntity.class);
		registration.registerBlockDataProvider(Ic2rMachineTooltipProvider.INSTANCE, Ic2rTileEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration)
	{
		Ic2rJadePluginConfigs.register(registration);
		registration.registerProgressClient(Ic2rProgressProvider.INSTANCE);
		registration.registerBlockComponent(Ic2rEnergyProvider.INSTANCE, Ic2rTileEntityBlock.class);
		registration.registerBlockComponent(Ic2rMachineTooltipProvider.INSTANCE, Ic2rTileEntityBlock.class);

		HarvestToolProvider.registerHandler(SimpleToolHandler.create(
			net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ic2r", "wrench"),
			java.util.List.of(Ic2rItems.WRENCH)));
	}
}
