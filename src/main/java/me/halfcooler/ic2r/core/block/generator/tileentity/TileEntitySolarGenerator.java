package me.halfcooler.ic2r.core.block.generator.tileentity;

import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.generator.container.ContainerSolarGenerator;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.proxy.EnvProxy;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.util.BiomeUtil;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySolarGenerator extends TileEntityBaseGenerator
{
	private static final int tickRate = 128;
	public float skyLight = 0.0F;
	@GuiSynced
	public boolean sunlight = false;
	private int ticker = IC2R.random.nextInt(128);

	public TileEntitySolarGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.SOLAR_GENERATOR, pos, state, 1.0, 1, 32);
	}

	public static float getSkyLight(Level world, BlockPos pos)
	{
		if (!world.dimensionType().hasSkyLight())
		{
			return 0.0F;
		}

		float sunBrightness = Util.limit((float) Math.cos(world.getSunAngle(1.0F)) * 2.0F + 0.2F, 0.0F, 1.0F);
		if (!IC2R.envProxy.biomeHasType(BiomeUtil.getBiome(world, pos), EnvProxy.BiomeType.SANDY))
		{
			sunBrightness *= 1.0F - world.getRainLevel(1.0F) * 5.0F / 16.0F;
			sunBrightness *= 1.0F - world.getThunderLevel(1.0F) * 5.0F / 16.0F;
			sunBrightness = Util.limit(sunBrightness, 0.0F, 1.0F);
		}

		return world.getBrightness(LightLayer.SKY, pos) / 15.0F * sunBrightness;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateSunVisibility();
	}

	@Override
	public boolean gainEnergy()
	{
		if (++this.ticker % 128 == 0)
		{
			this.updateSunVisibility();
		}

		if (this.skyLight > 0.0F)
		{
			this.energy.addEnergy(IC2RConfig.balance.energy.generator.solar.get() * this.skyLight);
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public boolean gainFuel()
	{
		return false;
	}

	public void updateSunVisibility()
	{
		this.skyLight = getSkyLight(this.getLevel(), this.worldPosition.above());
		this.sunlight = this.skyLight > 0.0F;
	}

	@Override
	public boolean needsFuel()
	{
		return false;
	}

	@Override
	public ContainerBase<TileEntitySolarGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerSolarGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerSolarGenerator(syncId, inventory, this);
	}

	@Override
	protected boolean delayActiveUpdate()
	{
		return true;
	}

	public boolean isSunlight()
	{
		return this.sunlight;
	}
}
