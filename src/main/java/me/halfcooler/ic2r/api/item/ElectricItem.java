package me.halfcooler.ic2r.api.item;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ElectricItem
{
	private static final List<IBackupElectricItemManager> backupManagers = new ArrayList<>();
	public static IElectricItemManager manager;
	public static IElectricItemManager rawManager;

	public static void registerBackupManager(IBackupElectricItemManager manager)
	{
		backupManagers.add(manager);
	}

	public static IBackupElectricItemManager getBackupManager(ItemStack stack)
	{
		for (IBackupElectricItemManager manager : backupManagers)
		{
			if (manager.handles(stack))
			{
				return manager;
			}
		}

		return null;
	}
}
