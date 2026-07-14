package me.halfcooler.ic2r.core.network;

public interface IRpcProvider<V>
{
	V executeRpc(Object... var1);
}
