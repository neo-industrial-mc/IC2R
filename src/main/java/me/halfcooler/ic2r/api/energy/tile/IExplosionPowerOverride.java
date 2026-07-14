package me.halfcooler.ic2r.api.energy.tile;

public interface IExplosionPowerOverride
{
	boolean shouldExplode();

	float getExplosionPower(int var1, float var2);
}
