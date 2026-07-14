package me.halfcooler.ic2r.api.reactor;

import me.halfcooler.ic2r.api.info.ILocatable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IReactor extends ILocatable
{
	BlockEntity getCoreTe();

	int getHeat();

	void setHeat(int var1);

	int addHeat(int var1);

	int getMaxHeat();

	void setMaxHeat(int var1);

	void addEmitHeat(int var1);

	float getHeatEffectModifier();

	void setHeatEffectModifier(float var1);

	float getReactorEnergyOutput();

	double getReactorEUEnergyOutput();

	float addOutput(float var1);

	ItemStack getItemAt(int var1, int var2);

	void setItemAt(int var1, int var2, ItemStack var3);

	void explode();

	int getTickRate();

	boolean produceEnergy();

	boolean isFluidCooled();
}
