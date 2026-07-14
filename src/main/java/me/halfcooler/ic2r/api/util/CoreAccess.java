package me.halfcooler.ic2r.api.util;

import me.halfcooler.ic2r.api.info.IInfoProvider;
import me.halfcooler.ic2r.api.network.INetworkManager;
import me.halfcooler.ic2r.api.tile.IRotorProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface CoreAccess
{
	boolean isCallingFromIc2r();

	IInfoProvider getItemInfo();

	INetworkManager getClientNetworkManager();

	INetworkManager getServerNetworkManager();

	DamageSource getElectricDamageSource();

	DamageSource getNukeExplosionDamageSource();

	DamageSource getRadiationDamageSource();

	MobEffect getRadiationStatusEffect();

	<T extends BlockEntity & IRotorProvider> void registerRotorProvider(BlockEntityType<T> var1);
}
