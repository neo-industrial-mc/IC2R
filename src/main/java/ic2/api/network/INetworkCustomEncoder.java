package ic2.api.network;

public interface INetworkCustomEncoder
{
	void encode(IGrowingBuffer var1, Object var2);

	Object decode(IGrowingBuffer var1);

	boolean isThreadSafe();
}
