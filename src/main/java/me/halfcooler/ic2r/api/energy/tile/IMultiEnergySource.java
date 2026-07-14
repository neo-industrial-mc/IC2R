package me.halfcooler.ic2r.api.energy.tile;

public interface IMultiEnergySource extends IEnergySource
{
	boolean sendMultipleEnergyPackets();

	int getMultipleEnergyPacketAmount();
}
