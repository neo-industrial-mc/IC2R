package ic2.integration.jade;

import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class Ic2JadePlugin implements IWailaPlugin
{
	@Override
	public void register(IWailaCommonRegistration registration)
	{
		// Progress still uses Jade's universal progress pipeline (colorable via ProgressView.style).
		registration.registerProgress(Ic2ProgressProvider.INSTANCE, Ic2TileEntity.class);
		// Energy is a custom bar (Jade's universal energy colors are fixed).
		registration.registerBlockDataProvider(Ic2EnergyProvider.INSTANCE, Ic2TileEntity.class);
		registration.registerBlockDataProvider(Ic2MachineTooltipProvider.INSTANCE, Ic2TileEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration)
	{
		Ic2JadePluginConfigs.register(registration);
		registration.registerProgressClient(Ic2ProgressProvider.INSTANCE);
		registration.registerBlockComponent(Ic2EnergyProvider.INSTANCE, Ic2TileEntityBlock.class);
		registration.registerBlockComponent(Ic2MachineTooltipProvider.INSTANCE, Ic2TileEntityBlock.class);
	}
}
