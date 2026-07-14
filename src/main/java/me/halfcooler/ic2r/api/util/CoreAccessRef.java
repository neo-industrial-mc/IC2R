package me.halfcooler.ic2r.api.util;

public final class CoreAccessRef
{
	static CoreAccess CORE_ACCESS;

	public static CoreAccess get()
	{
		CoreAccess ret = CORE_ACCESS;
		if (ret == null)
		{
			throw new IllegalStateException("IC2R is not loaded");
		} else if (!ret.isCallingFromIc2r())
		{
			throw new IllegalAccessError("external core access");
		} else
		{
			return ret;
		}
	}

	public static boolean exists()
	{
		return CORE_ACCESS != null;
	}
}
