package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.IC2R;
import net.neoforged.bus.api.SubscribeEvent;
final class ForgeNetworkHandler
{
	@SubscribeEvent
	public void onS2CPacket(NetworkEvent.ServerCustomPayloadEvent event)
	{
		NetworkEvent.Context context = event.getSource().get();
		IC2R.network.get(false).onPacket(event.getPayload(), IC2R.sideProxy.getPlayerInstance());
		context.setPacketHandled(true);
	}

	@SubscribeEvent
	public void onC2SPacket(NetworkEvent.ClientCustomPayloadEvent event)
	{
		NetworkEvent.Context context = event.getSource().get();
		IC2R.network.get(true).onPacket(event.getPayload(), context.getSender());
		context.setPacketHandled(true);
	}
}
