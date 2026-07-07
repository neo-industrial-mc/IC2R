package me.halfcooler.ic2r.addons.csas.blockentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.block.generator.tileentity.TileEntityBaseGenerator;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.init.IC2Config;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.util.Ic2Tooltip;
import me.halfcooler.ic2r.addons.csas.common.CompactSolarType;
import me.halfcooler.ic2r.addons.csas.generator.container.ContainerCompactSolar;
import me.halfcooler.ic2r.addons.csas.init.CsasBlockEntities;
import me.halfcooler.ic2r.addons.csas.init.CsasConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TileEntityCompactSolar extends TileEntityBaseGenerator
{
	private static final int SUN_CHECK_INTERVAL = 64;

	public float skyLight = 0.0F;
	@GuiSynced
	public boolean sunlight = false;
	private int ticker = IC2.random.nextInt(SUN_CHECK_INTERVAL);

	public TileEntityCompactSolar(BlockPos pos, BlockState state)
	{
		super(
			CsasBlockEntities.COMPACT_SOLAR.get(),
			pos,
			state,
			1.0,
			getTypeFromState(state).getTier(),
			getTypeFromState(state).getMaxStorage()
		);
	}

	private static CompactSolarType getTypeFromState(BlockState state)
	{
		return CompactSolarType.fromBlock(state.getBlock());
	}

	public CompactSolarType getSolarType()
	{
		return getTypeFromState(this.getBlockState());
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.configureProduction();
		this.chargeSlot.setTier(this.getSolarType().getTier());
		this.updateSunVisibility();
	}

	private void configureProduction()
	{
		this.production = IC2Config.balance.energy.generator.solar.get() * this.getSolarType().getMultiplier();
		this.energy.configureFixedSource((int) this.production);
	}

	@Override
	public boolean gainEnergy()
	{
		if (++this.ticker % SUN_CHECK_INTERVAL == 0)
		{
			this.updateSunVisibility();
		}

		if (this.skyLight > 0.0F && this.shouldProduce())
		{
			this.energy.addEnergy(IC2Config.balance.energy.generator.solar.get() * this.getSolarType().getMultiplier() * this.skyLight);
			return true;
		}

		return false;
	}

	private boolean shouldProduce()
	{
		int rate = CsasConfig.PRODUCTION_RATE.get();
		return rate <= 1 || IC2.random.nextInt(rate) == 0;
	}

	@Override
	public boolean gainFuel()
	{
		return false;
	}

	@Override
	public boolean needsFuel()
	{
		return false;
	}

	public void updateSunVisibility()
	{
		if (this.level == null)
		{
			return;
		}

		this.skyLight = TileEntitySolarGenerator.getSkyLight(this.level, this.worldPosition.above());
		this.sunlight = this.skyLight > 0.0F;
	}

	public boolean isSunlight()
	{
		return this.sunlight;
	}

	@Override
	protected boolean delayActiveUpdate()
	{
		return true;
	}

	@Override
	public ContainerBase<TileEntityCompactSolar> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerCompactSolar(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerCompactSolar(syncId, inventory, this);
	}

	@Override
	public void appendItemTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag advanced)
	{
		super.appendItemTooltip(stack, tooltip, advanced);
		Ic2Tooltip.add(tooltip, Component.translatable("tooltip.ic2r_csas.power_tier", this.getSolarType().getTier()));
	}
}