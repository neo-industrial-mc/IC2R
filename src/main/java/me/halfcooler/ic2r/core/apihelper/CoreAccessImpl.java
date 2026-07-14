package me.halfcooler.ic2r.core.apihelper;

import me.halfcooler.ic2r.api.info.IInfoProvider;
import me.halfcooler.ic2r.api.network.INetworkManager;
import me.halfcooler.ic2r.api.tile.IRotorProvider;
import me.halfcooler.ic2r.api.util.CoreAccess;
import me.halfcooler.ic2r.api.util.CoreAccessRef;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rDamageSource;
import me.halfcooler.ic2r.core.Ic2rPotion;
import me.halfcooler.ic2r.core.util.ItemInfo;
import me.halfcooler.ic2r.core.util.Util;

import java.lang.reflect.Field;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class CoreAccessImpl implements CoreAccess
{
	public static void init()
	{
		try
		{
			Field field = CoreAccessRef.class.getDeclaredField("CORE_ACCESS");
			field.setAccessible(true);
			field.set(null, new CoreAccessImpl());
			field.setAccessible(false);
		} catch (ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isCallingFromIc2r()
	{
		return Util.isCallingFromIc2r(1);
	}

	@Override
	public IInfoProvider getItemInfo()
	{
		return new ItemInfo();
	}

	@Override
	public INetworkManager getClientNetworkManager()
	{
		return IC2R.network.get(false);
	}

	@Override
	public INetworkManager getServerNetworkManager()
	{
		return IC2R.network.get(true);
	}

	@Override
	public DamageSource getElectricDamageSource()
	{
		return Ic2rDamageSource.electricity;
	}

	@Override
	public DamageSource getNukeExplosionDamageSource()
	{
		return Ic2rDamageSource.nuke;
	}

	@Override
	public DamageSource getRadiationDamageSource()
	{
		return Ic2rDamageSource.radiation;
	}

	@Override
	public MobEffect getRadiationStatusEffect()
	{
		return Ic2rPotion.radiation;
	}

	@Override
	public <T extends BlockEntity & IRotorProvider> void registerRotorProvider(BlockEntityType<T> type)
	{
		IC2R.sideProxy.registerRotorProvider(type);
	}
}
