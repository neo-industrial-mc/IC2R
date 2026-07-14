package me.halfcooler.ic2r.core.block.reactor.tileentity;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.profile.IElectricalNode;
import me.halfcooler.ic2r.api.energy.profile.VoltageTier;
import me.halfcooler.ic2r.api.energy.tile.IEnergyAcceptor;
import me.halfcooler.ic2r.api.energy.tile.IEnergySource;
import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;
import me.halfcooler.ic2r.core.energy.EnergyNetMode;
import me.halfcooler.ic2r.core.energy.profile.ElectricalProfile;
import me.halfcooler.ic2r.api.energy.tile.IMetaDelegate;
import me.halfcooler.ic2r.api.reactor.IBaseReactorComponent;
import me.halfcooler.ic2r.api.reactor.IReactor;
import me.halfcooler.ic2r.api.reactor.IReactorChamber;
import me.halfcooler.ic2r.api.reactor.IReactorComponent;
import me.halfcooler.ic2r.api.recipe.ILiquidHeatExchangerManager;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.Ic2rDamageSource;
import me.halfcooler.ic2r.core.Ic2rExplosion;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.comp.Redstone;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquid;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquidByManager;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquidByTank;
import me.halfcooler.ic2r.core.block.invslot.InvSlotOutput;
import me.halfcooler.ic2r.core.block.invslot.InvSlotReactor;
import me.halfcooler.ic2r.core.block.reactor.container.ContainerNuclearReactor;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.gui.dynamic.IGuiValueProvider;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.item.reactor.ItemReactorHeatStorage;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.sound.Sound;
import me.halfcooler.ic2r.core.util.LegacyNbt;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;
import me.halfcooler.ic2r.core.util.WorldUtil;

import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;
import me.halfcooler.ic2r.core.block.tileentity.ClientTicker;

import java.util.ArrayList;
import java.util.List;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class TileEntityNuclearReactorElectric extends TileEntityInventory implements IHasGui, IReactor, IEnergySource, IMetaDelegate, IGuiValueProvider, IElectricalNode, ServerTicker, ClientTicker
{
	public final Fluids.InternalFluidTank inputTank;
	public final Fluids.InternalFluidTank outputTank;
	public final InvSlotReactor reactorSlot;
	public final InvSlotOutput coolantOutputSlot;
	public final InvSlotOutput hotCoolantOutputSlot;
	public final InvSlotConsumableLiquidByManager coolantInputSlot;
	public final InvSlotConsumableLiquidByTank hotCoolantInputSlot;
	public final Redstone redstone;
	protected final Fluids fluids;
	private final List<IEnergyTile> subTiles = new ArrayList<>();
	public Sound soundMain;
	public Sound soundGeiger;
	public float output = 0.0F;
	public int updateTicker;
	public int heat = 0;
	public int maxHeat = 10000;
	public float hem = 1.0F;
	public int EmitHeat = 0;
	public boolean addedToEnergyNet = false;
	private float lastOutput = 0.0F;
	private final ElectricalProfile profile = new ElectricalProfile(VoltageTier.LV);
	private float lastSyncedOfferedOutput = -1.0F;
	private double energyBuffer = 0.0;
	private int EmitHeatBuffer = 0;
	private boolean fluidCooled = false;

	/**
	 * Modern NBT key for GT-mode energy buffer (G1.5 naming expansion).
	 * Same logical name as {@link me.halfcooler.ic2r.core.block.comp.Energy#NBT_ENERGY_BUFFER}.
	 */
	public static final String NBT_ENERGY_BUFFER = "energy_buffer";
	/** Legacy camelCase key; still readable via {@link LegacyNbt}. */
	public static final String LEGACY_NBT_ENERGY_BUFFER = "energyBuffer";

	public TileEntityNuclearReactorElectric(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.NUCLEAR_REACTOR, pos, state);
		this.updateTicker = IC2R.random.nextInt(this.getTickRate());
		this.fluids = this.addComponent(new Fluids(this));
		this.inputTank = this.fluids.addTank("inputTank", 10000, InvSlot.Access.NONE, InvSlot.InvSide.ANY, Fluids.fluidPredicate(Recipes.liquidHeatUpManager));
		this.outputTank = this.fluids.addTank("outputTank", 10000, InvSlot.Access.NONE);
		this.reactorSlot = new InvSlotReactor(this, "reactor", 54);
		this.coolantInputSlot = new InvSlotConsumableLiquidByManager(
			this, "coolantinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Drain, Recipes.liquidHeatUpManager
		);
		this.hotCoolantInputSlot = new InvSlotConsumableLiquidByTank(
			this, "hotcoolinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Fill, this.outputTank
		);
		this.coolantOutputSlot = new InvSlotOutput(this, "coolantoutputSlot", 1);
		this.hotCoolantOutputSlot = new InvSlotOutput(this, "hotcoolantoutputSlot", 1);
		this.redstone = this.addComponent(new Redstone(this));
	}

	public static void showHeatEffects(Level world, BlockPos pos, int heat)
	{
		RandomSource rnd = world.random;
		if (rnd.nextInt(8) == 0)
		{
			int puffs = heat / 1000;
			if (puffs > 0)
			{
				puffs = rnd.nextInt(puffs);

				for (int n = 0; n < puffs; n++)
				{
					world.addParticle(
						ParticleTypes.SMOKE, pos.getX() + rnd.nextFloat(), pos.getY() + 0.95F, pos.getZ() + rnd.nextFloat(), 0.0, 0.0, 0.0
					);
				}

				puffs -= rnd.nextInt(4) + 3;

				for (int n = 0; n < puffs; n++)
				{
					world.addParticle(ParticleTypes.FLAME, pos.getX() + rnd.nextFloat(), pos.getY() + 1, pos.getZ() + rnd.nextFloat(), 0.0, 0.0, 0.0);
				}
			}
		}
	}

	private static boolean isFluidChamberBlock(BlockGetter world, BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() == Ic2rBlocks.REACTOR_VESSEL)
		{
			return true;
		}

		BlockEntity te = world.getBlockEntity(pos);
		return te instanceof IReactorChamber && ((IReactorChamber) te).isWall();
	}

	private float getHuOutputModifier()
	{
		return 40.0F * IC2RConfig.balance.energy.fluidReactor.outputModifier.get().floatValue();
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide && !this.isFluidCooled())
		{
			this.refreshChambers();
			EnergyNet.instance.addBlockEntityTile(this);
			this.addedToEnergyNet = true;
		}

		this.createChamberRedstoneLinks();
		if (this.isFluidCooled())
		{
			this.createCasingRedstoneLinks();
			this.openTanks();
		}
	}

	@Override
	protected void onUnloaded()
	{
		if (IC2R.sideProxy.isRendering())
		{
			IC2R.soundManager.removeAllSound(this);
			this.soundMain = null;
			this.soundGeiger = null;
		}

		if (IC2R.sideProxy.isSimulating() && this.addedToEnergyNet)
		{
			EnergyNet.instance.removeTile(this);
			this.addedToEnergyNet = false;
		}

		super.onUnloaded();
	}

	public int gaugeHeatScaled(int i)
	{
		return i * this.heat / (this.maxHeat / 100 * 85);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.heat = nbt.getInt("heat");
		this.output = nbt.getShort("output");
		this.energyBuffer = readEnergyBufferNbt(nbt);
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("heat", this.heat);
		nbt.putShort("output", (short) this.getReactorEnergyOutput());
		writeEnergyBufferNbt(nbt, this.energyBuffer);
	}

	/** Pure NBT write (snake_case only). Unit-test entry (NS-003 / G1.5). */
	public static void writeEnergyBufferNbt(CompoundTag nbt, double energy)
	{
		nbt.putDouble(NBT_ENERGY_BUFFER, energy);
	}

	/** Pure NBT read: prefer {@link #NBT_ENERGY_BUFFER}, else legacy {@link #LEGACY_NBT_ENERGY_BUFFER}. */
	public static double readEnergyBufferNbt(CompoundTag nbt)
	{
		return LegacyNbt.getDouble(nbt, NBT_ENERGY_BUFFER, LEGACY_NBT_ENERGY_BUFFER);
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		if (this.addedToEnergyNet)
		{
			this.refreshChambers();
		}
	}

	@Override
	public void drawEnergy(double amount)
	{
		if (this.isGtEnergyNet())
		{
			this.energyBuffer = Math.max(0.0, this.energyBuffer - amount);
		}
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction direction)
	{
		return true;
	}

	@Override
	public double getOfferedEnergy()
	{
		float productionEuPerTick = this.getCurrentOfferedOutput();
		this.syncSourceProfile(productionEuPerTick);
		// GT mode only emits whole amp packets (offer >= V). Buffer production like ConversionGenerator.
		return this.isGtEnergyNet() ? this.energyBuffer : productionEuPerTick;
	}

	private void syncSourceProfile(float outputEuPerTick)
	{
		if (outputEuPerTick > 0.0F)
		{
			if (this.lastSyncedOfferedOutput == outputEuPerTick)
			{
				return;
			}

			this.lastSyncedOfferedOutput = outputEuPerTick;
			this.profile.setWorkingVoltage(VoltageTier.fromPower(outputEuPerTick));
			this.profile.setRecipePower(Math.round(outputEuPerTick));
		} else if (!this.isGtEnergyNet() || this.energyBuffer <= 0.0)
		{
			if (this.lastSyncedOfferedOutput == 0.0F)
			{
				return;
			}

			this.lastSyncedOfferedOutput = 0.0F;
			this.profile.setWorkingVoltage(VoltageTier.ULV);
			this.profile.setRecipePower(0);
		}
	}

	@Override
	public VoltageTier getWorkingVoltage()
	{
		return this.profile.getWorkingVoltage();
	}

	@Override
	public int getWorkingCurrent()
	{
		return this.profile.getWorkingCurrent();
	}

	@Override
	public double getAverageCurrent()
	{
		return this.profile.getDisplayCurrent();
	}

	@Override
	public int getMaxSourceAmperage()
	{
		return 1;
	}

	@Override
	public int getMaxSinkAmperage()
	{
		return 1;
	}

	private float getCurrentOfferedOutput()
	{
		return this.getReactorEnergyOutput() * 5.0F * IC2RConfig.balance.energy.generator.nuclear.get().floatValue();
	}

	private VoltageTier getOutputVoltageTier(float outputEuPerTick)
	{
		return outputEuPerTick <= 0.0F ? VoltageTier.ULV : VoltageTier.fromPower(outputEuPerTick);
	}

	private boolean isGtEnergyNet()
	{
		return EnergyNetMode.fromConfig(IC2RConfig.misc.energyNetMode.get()) == EnergyNetMode.GT;
	}

	/**
	 * GT mode accumulates production until at least 1 full amp packet can be emitted.
	 * Without this, dynamic V from {@link VoltageTier#fromPower} is almost always greater than
	 * the instantaneous EU/t, so {@code ElectricalNodes.getGtOfferAmps} returns 0.
	 */
	private void accumulateGtEnergyBuffer()
	{
		if (!this.isGtEnergyNet() || this.fluidCooled)
		{
			return;
		}

		float outputEuPerTick = this.getCurrentOfferedOutput();
		if (outputEuPerTick > 0.0F)
		{
			int voltage = this.getOutputVoltageTier(outputEuPerTick).getVoltage();
			double maxBuffer = Math.max(voltage, outputEuPerTick);
			this.energyBuffer = Math.min(this.energyBuffer + outputEuPerTick, maxBuffer);
			this.syncSourceProfile(outputEuPerTick);
		} else if (this.energyBuffer <= 0.0)
		{
			this.syncSourceProfile(0.0F);
		}
	}

	@Override
	public double getEnergyBufferCapacity()
	{
		float offered = this.getCurrentOfferedOutput();
		if (offered <= 0.0F && this.isGtEnergyNet() && this.energyBuffer > 0.0)
		{
			return this.profile.getWorkingVoltage().getVoltage();
		}

		int voltage = this.getOutputVoltageTier(offered).getVoltage();
		return Math.max(voltage, offered);
	}

	@Override
	public double getEnergyBufferFree()
	{
		double capacity = this.getEnergyBufferCapacity();
		if (this.isGtEnergyNet())
		{
			return Math.max(0.0, capacity - Math.min(this.energyBuffer, capacity));
		}

		float offered = this.getCurrentOfferedOutput();
		return Math.max(0.0, capacity - Math.min(offered, capacity));
	}

	@Override
	public int getSourceTier()
	{
		return this.profile.getWorkingVoltage().getIcTier();
	}

	@Override
	public double getReactorEUEnergyOutput()
	{
		// Always the production rate (EU/t), never the GT packet buffer fill.
		return this.getCurrentOfferedOutput();
	}

	@Override
	public List<IEnergyTile> getSubTiles()
	{
		return List.copyOf(this.subTiles);
	}

	private void processFluidsSlots()
	{
		this.coolantInputSlot.processIntoTank(this.inputTank, this.coolantOutputSlot);
		this.hotCoolantInputSlot.processFromTank(this.outputTank, this.hotCoolantOutputSlot);
	}

	public void refreshChambers()
	{
		Level world = this.getLevel();
		List<IEnergyTile> newSubTiles = new ArrayList<>();
		newSubTiles.add(this);

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity te = world.getBlockEntity(this.worldPosition.relative(dir));
			if (te instanceof TileEntityReactorChamberElectric && !te.isRemoved())
			{
				newSubTiles.add((TileEntityReactorChamberElectric) te);
			}
		}

		if (!newSubTiles.equals(this.subTiles))
		{
			if (this.addedToEnergyNet)
			{
				EnergyNet.instance.removeTile(this);
			}

			this.subTiles.clear();
			this.subTiles.addAll(newSubTiles);
			if (this.addedToEnergyNet)
			{
				EnergyNet.instance.addBlockEntityTile(this);
			}
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.accumulateGtEnergyBuffer();
		if (this.updateTicker++ % this.getTickRate() == 0)
		{
			if (!Util.isAreaLoaded(this.getLevel(), this.worldPosition, 8))
			{
				this.output = 0.0F;
			} else
			{
				boolean toFluidCooled = this.isFluidReactor();
				if (this.fluidCooled != toFluidCooled)
				{
					if (toFluidCooled)
					{
						this.enableFluidMode();
					} else
					{
						this.disableFluidMode();
					}

					this.fluidCooled = toFluidCooled;
				}

				this.dropAllUnfittingStuff();
				this.output = 0.0F;
				this.maxHeat = 10000;
				this.hem = 1.0F;
				this.processChambers();
				if (this.fluidCooled)
				{
					this.processFluidsSlots();
					Ic2rFluidStack inputFluid = this.inputTank.getFluidStack();
					assert inputFluid == null || Recipes.liquidHeatUpManager.acceptsFluid(this.inputTank.getFluidStack().getFluid());
					int huOtput = (int) (getHuOutputModifier() * this.EmitHeatBuffer);
					int outputRoom = this.outputTank.getCapacity() - this.outputTank.getFluidAmount();
					this.EmitHeatBuffer = 0;
					if (outputRoom > 0 && inputFluid != null)
					{
						ILiquidHeatExchangerManager.HeatExchangeProperty prop = Recipes.liquidHeatUpManager.getHeatExchangeProperty(inputFluid.getFluid());
						int fluidOutput = huOtput / prop.huPerMB();
						Ic2rFluidStack drainCoolant;
						if (fluidOutput < outputRoom)
						{
							this.EmitHeatBuffer = (int) (huOtput % prop.huPerMB() / getHuOutputModifier());
							this.EmitHeat = (int) (huOtput / getHuOutputModifier());
							drainCoolant = this.inputTank.drainMbUnchecked(fluidOutput, true);
						} else
						{
							this.EmitHeat = outputRoom * prop.huPerMB();
							drainCoolant = this.inputTank.drainMbUnchecked(outputRoom, true);
						}

						if (drainCoolant != null)
						{
							this.EmitHeat = drainCoolant.getAmountMb() * prop.huPerMB();
							huOtput -= this.inputTank.drainMbUnchecked(drainCoolant.getAmountMb(), false).getAmountMb() * prop.huPerMB();
							this.outputTank.fillMbUnchecked(Ic2rFluidStack.create(prop.outputFluid(), drainCoolant.getAmountMb()), false);
						} else
						{
							this.EmitHeat = 0;
						}
					} else
					{
						this.EmitHeat = 0;
					}

					this.addHeat((int) (huOtput / getHuOutputModifier()));
				}

				if (this.calculateHeatEffects())
				{
					return;
				}

				this.setActive(this.heat >= 1000 || this.output > 0.0F);
				this.setChanged();
				if (!this.isFluidCooled())
				{
					this.syncSourceProfile(this.getCurrentOfferedOutput());
				}
			}

			IC2R.network.get(true).updateTileEntityField(this, "output");
		}
	}

	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		showHeatEffects(this.getLevel(), this.worldPosition, this.heat);
	}

	public void dropAllUnfittingStuff()
	{
		for (int i = 0; i < this.reactorSlot.size(); i++)
		{
			ItemStack stack = this.reactorSlot.get(i);
			if (stack != null && !this.isUsefulItem(stack, false))
			{
				this.reactorSlot.put(i, null);
				this.eject(stack);
			}
		}

		for (int i = this.reactorSlot.size(); i < this.reactorSlot.rawSize(); i++)
		{
			ItemStack stack = this.reactorSlot.get(i);
			this.reactorSlot.put(i, null);
			this.eject(stack);
		}
	}

	public boolean isUsefulItem(ItemStack stack, boolean forInsertion)
	{
		Item item = stack.getItem();
		return (!forInsertion || !this.fluidCooled || item.getClass() != ItemReactorHeatStorage.class || stack.getDamageValue() <= 0) && item instanceof IBaseReactorComponent && (!forInsertion || ((IBaseReactorComponent) item).canBePlacedIn(stack, this));
	}

	public void eject(ItemStack drop)
	{
		if (IC2R.sideProxy.isSimulating() && drop != null)
		{
			StackUtil.dropAsEntity(this.getLevel(), this.worldPosition, drop);
		}
	}

	public boolean calculateHeatEffects()
	{
		if (this.heat >= 4000 && IC2R.sideProxy.isSimulating() && !(IC2RConfig.protection.reactorExplosionPowerLimit.get() <= 0.0F))
		{
			float power = (float) this.heat / this.maxHeat;
			if (power >= 1.0F)
			{
				this.explode();
				return true;
			}

			Level world = this.getLevel();
			RandomSource rng = world.random;
			if (power >= 0.85F && rng.nextFloat() <= 0.2F * this.hem)
			{
				BlockPos coordination = this.getRandCoordination(2);
				BlockState state = world.getBlockState(coordination);
				if (state.isAir())
				{
					world.setBlockAndUpdate(coordination, Blocks.FIRE.defaultBlockState());
				} else if (state.getDestroySpeed(world, coordination) >= 0.0F && world.getBlockEntity(coordination) == null)
				{
					if (state.canOcclude() || state.getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA))
					{
						world.setBlockAndUpdate(coordination, net.minecraft.world.level.material.Fluids.LAVA.defaultFluidState().createLegacyBlock());
					} else
					{
						world.setBlockAndUpdate(coordination, Blocks.FIRE.defaultBlockState());
					}
				}
			}

			if (power >= 0.7F)
			{
				for (LivingEntity entity : world.getEntitiesOfClass(
					LivingEntity.class,
					new AABB(
						this.worldPosition.getX() - 3,
						this.worldPosition.getY() - 3,
						this.worldPosition.getZ() - 3,
						this.worldPosition.getX() + 4,
						this.worldPosition.getY() + 4,
						this.worldPosition.getZ() + 4
					),
					EntitySelector.NO_CREATIVE_OR_SPECTATOR
				))
				{
        entity.hurt(Ic2rDamageSource.radiation, (int) (rng.nextInt(4) * this.hem));
				}
			}

			if (power >= 0.5F && rng.nextFloat() <= this.hem)
			{
				BlockPos coordination = this.getRandCoordination(2);
				BlockState state = world.getBlockState(coordination);
				if (state.getFluidState().is(net.minecraft.world.level.material.Fluids.WATER))
				{
					world.removeBlock(coordination, false);
				}
			}

			if (power >= 0.4F && rng.nextFloat() <= this.hem)
			{
				BlockPos coordination = this.getRandCoordination(2);
				if (world.getBlockEntity(coordination) == null)
				{
					BlockState state = world.getBlockState(coordination);
					if (state.isFlammable(world, coordination, Direction.UP))
					{
						world.setBlockAndUpdate(coordination, Blocks.FIRE.defaultBlockState());
					}
				}
			}

		}
		return false;
	}

	public BlockPos getRandCoordination(int radius)
	{
		if (radius <= 0)
		{
			return null;
		}

		Level world = this.getLevel();
		RandomSource rng = world.random;

		BlockPos ret;
		do
		{
			ret = this.worldPosition
				.offset(
					rng.nextInt(2 * radius + 1) - radius,
					rng.nextInt(2 * radius + 1) - radius,
					rng.nextInt(2 * radius + 1) - radius
				);
		} while (ret.equals(this.worldPosition));

		return ret;
	}

	public void processChambers()
	{
		int size = this.getReactorSize();

		for (int pass = 0; pass < 2; pass++)
		{
			for (int y = 0; y < 6; y++)
			{
				for (int x = 0; x < size; x++)
				{
					ItemStack stack = this.reactorSlot.get(x, y);
					if (stack != null && stack.getItem() instanceof IReactorComponent comp)
					{
						comp.processChamber(stack, this, x, y, pass == 0);
					}
				}
			}
		}
	}

	@Override
	public boolean produceEnergy()
	{
		return this.redstone.hasRedstoneInput() && IC2RConfig.balance.energy.generator.nuclear.get() > 0.0F;
	}

	public int getReactorSize()
	{
		Level world = this.getLevel();
		if (world == null)
		{
			return 9;
		}

		int cols = 3;

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity target = world.getBlockEntity(this.worldPosition.relative(dir));
			if (target instanceof TileEntityReactorChamberElectric)
			{
				cols++;
			}
		}

		return cols;
	}

	private boolean isFullSize()
	{
		return this.getReactorSize() == 9;
	}

	@Override
	public int getTickRate()
	{
		return 20;
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		return StackUtil.checkItemEquality(StackUtil.get(player, hand), new ItemStack(Ic2rItems.REACTOR_CHAMBER))
			? InteractionResult.PASS
			: super.onActivated(player, hand, side, hit);
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerNuclearReactor(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerNuclearReactor(syncId, inventory, this);
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("output"))
		{
			if (this.output > 0.0F)
			{
				if (this.lastOutput <= 0.0F)
				{
					if (this.soundMain == null)
					{
						this.soundMain = IC2R.soundManager.createSound(this, Ic2rSoundEvents.GENERATOR_NUCLEAR_LOOP.get(), SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
					}

					if (this.soundMain != null)
					{
						this.soundMain.play();
					}
				}

				if (this.output < 40.0F)
				{
					if (this.lastOutput <= 0.0F || this.lastOutput >= 40.0F)
					{
						if (this.soundGeiger != null)
						{
							IC2R.soundManager.removeSound(this, this.soundGeiger);
						}

						this.soundGeiger = IC2R.soundManager
							.createSound(this, Ic2rSoundEvents.GENERATOR_NUCLEAR_LOW_POWER.get(), SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
						if (this.soundGeiger != null)
						{
							this.soundGeiger.play();
						}
					}
				} else if (this.output < 80.0F)
				{
					if (this.lastOutput < 40.0F || this.lastOutput >= 80.0F)
					{
						if (this.soundGeiger != null)
						{
							IC2R.soundManager.removeSound(this, this.soundGeiger);
						}

						this.soundGeiger = IC2R.soundManager
							.createSound(this, Ic2rSoundEvents.GENERATOR_NUCLEAR_MEDIUM_POWER.get(), SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
						if (this.soundGeiger != null)
						{
							this.soundGeiger.play();
						}
					}
				} else if (this.output >= 80.0F && this.lastOutput < 80.0F)
				{
					if (this.soundGeiger != null)
					{
						IC2R.soundManager.removeSound(this, this.soundGeiger);
					}

					this.soundGeiger = IC2R.soundManager
						.createSound(this, Ic2rSoundEvents.GENERATOR_NUCLEAR_HIGH_POWER.get(), SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
					if (this.soundGeiger != null)
					{
						this.soundGeiger.play();
					}
				}
			} else if (this.lastOutput > 0.0F)
			{
				if (this.soundMain != null)
				{
					this.soundMain.stop();
				}

				if (this.soundGeiger != null)
				{
					this.soundGeiger.stop();
				}
			}

			this.lastOutput = this.output;
		}

		super.onNetworkUpdate(field);
	}

	@Override
	public BlockEntity getCoreTe()
	{
		return this;
	}

	@Override
	public BlockPos getPosition()
	{
		return this.worldPosition;
	}

	@Override
	public Level getWorldObj()
	{
		return this.getLevel();
	}

	@Override
	public int getHeat()
	{
		return this.heat;
	}

	@Override
	public void setHeat(int heat)
	{
		this.heat = heat;
	}

	@Override
	public int addHeat(int amount)
	{
		this.heat += amount;
		return this.heat;
	}

	@Override
	public ItemStack getItemAt(int x, int y)
	{
		return x >= 0 && x < this.getReactorSize() && y >= 0 && y < 6 ? this.reactorSlot.get(x, y) : null;
	}

	@Override
	public void setItemAt(int x, int y, ItemStack item)
	{
		if (x >= 0 && x < this.getReactorSize() && y >= 0 && y < 6)
		{
			this.reactorSlot.put(x, y, item);
		}
	}

	@Override
	public void explode()
	{
		float boomPower = 10.0F;
		float boomMod = 1.0F;

		for (int i = 0; i < this.reactorSlot.size(); i++)
		{
			ItemStack stack = this.reactorSlot.get(i);
			if (stack != null && stack.getItem() instanceof IReactorComponent)
			{
				float f = ((IReactorComponent) stack.getItem()).influenceExplosion(stack, this);
				if (f > 0.0F && f < 1.0F)
				{
					boomMod *= f;
				} else
				{
					boomPower += f;
				}
			}

			this.reactorSlot.put(i, null);
		}

		boomPower *= this.hem * boomMod;
		IC2R.log
			.log(
				LogCategory.PlayerActivity,
				org.apache.logging.log4j.Level.INFO,
				"Nuclear Reactor at %s melted (raw explosion power %f)",
				Util.formatPosition(this),
				boomPower
			);
		boomPower = Math.min(boomPower, IC2RConfig.protection.reactorExplosionPowerLimit.get().floatValue());
		Level world = this.getLevel();

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity target = world.getBlockEntity(this.worldPosition.relative(dir));
			if (target instanceof TileEntityReactorChamberElectric)
			{
				world.removeBlock(target.getBlockPos(), false);
			}
		}

		world.removeBlock(this.worldPosition, false);
		Ic2rExplosion explosion = new Ic2rExplosion(world, null, this.worldPosition, boomPower, 0.01F, Ic2rExplosion.Type.ReactorMeltdown);
		explosion.doExplosion();
	}

	@Override
	public void addEmitHeat(int heat)
	{
		this.EmitHeatBuffer += heat;
	}

	@Override
	public int getMaxHeat()
	{
		return this.maxHeat;
	}

	@Override
	public void setMaxHeat(int newMaxHeat)
	{
		this.maxHeat = newMaxHeat;
	}

	@Override
	public float getHeatEffectModifier()
	{
		return this.hem;
	}

	@Override
	public void setHeatEffectModifier(float newHEM)
	{
		this.hem = newHEM;
	}

	@Override
	public float getReactorEnergyOutput()
	{
		return this.output;
	}

	@Override
	public float addOutput(float energy)
	{
		return this.output += energy;
	}

	@Override
	public boolean isFluidCooled()
	{
		return this.fluidCooled;
	}

	private void createChamberRedstoneLinks()
	{
		Level world = this.getLevel();

		for (Direction facing : Util.ALL_DIRS)
		{
			BlockPos cPos = this.worldPosition.relative(facing);
			if (world.getBlockEntity(cPos) instanceof TileEntityReactorChamberElectric chamber)
			{
				if (chamber.redstone.isLinked() && chamber.redstone.getLinkReceiver() != this.redstone)
				{
					chamber.destroyChamber(true);
				} else
				{
					chamber.redstone.linkTo(this.redstone);
				}
			}
		}
	}

	private void createCasingRedstoneLinks()
	{
		WorldUtil.findTileEntities(this.getLevel(), this.worldPosition, 2, te ->
		{
			if (te instanceof TileEntityReactorRedstonePort)
			{
				((TileEntityReactorRedstonePort) te).redstone.linkTo(TileEntityNuclearReactorElectric.this.redstone);
			}

			return false;
		});
	}

	private void removeCasingRedstoneLinks()
	{
		for (Redstone rs : this.redstone.getLinkedOrigins())
		{
			if (rs.getParent() instanceof TileEntityReactorRedstonePort)
			{
				rs.unlinkOutbound();
			}
		}
	}

	private void enableFluidMode()
	{
		if (this.addedToEnergyNet)
		{
			EnergyNet.instance.removeTile(this);
			this.addedToEnergyNet = false;
		}

		this.energyBuffer = 0.0;
		this.createCasingRedstoneLinks();
		this.openTanks();
	}

	private void disableFluidMode()
	{
		if (!this.addedToEnergyNet)
		{
			this.refreshChambers();
			EnergyNet.instance.addBlockEntityTile(this);
			this.addedToEnergyNet = true;
		}

		this.removeCasingRedstoneLinks();
		this.closeTanks();
	}

	private void openTanks()
	{
		this.fluids.changeConnectivity(this.inputTank, InvSlot.Access.I, InvSlot.InvSide.ANY);
		this.fluids.changeConnectivity(this.outputTank, InvSlot.Access.O, InvSlot.InvSide.ANY);
	}

	private void closeTanks()
	{
		this.fluids.changeConnectivity(this.inputTank, InvSlot.Access.NONE, InvSlot.InvSide.ANY);
		this.fluids.changeConnectivity(this.outputTank, InvSlot.Access.NONE, InvSlot.InvSide.ANY);
	}

	private boolean isFluidReactor()
	{
		if (!this.isFullSize())
		{
			return false;
		}

		if (!this.hasFluidChamber())
		{
			return false;
		}

		int range = 2;
		final MutableBoolean foundConflict = new MutableBoolean();
		WorldUtil.findTileEntities(this.getLevel(), this.worldPosition, 4, te ->
		{
			if (!(te instanceof TileEntityNuclearReactorElectric reactor))
			{
				return false;
			} else if (te == TileEntityNuclearReactorElectric.this)
			{
				return false;
			} else
			{
				if (reactor.isFullSize() && reactor.hasFluidChamber())
				{
					foundConflict.setTrue();
					return true;
				} else
				{
					return false;
				}
			}
		});
		return !foundConflict.getValue();
	}

	private boolean hasFluidChamber()
	{
		int range = 2;
		PathNavigationRegion cache = new PathNavigationRegion(this.getLevel(), this.worldPosition.offset(-2, -2, -2), this.worldPosition.offset(2, 2, 2));
		MutableBlockPos cPos = new MutableBlockPos();

		for (int i = 0; i < 2; i++)
		{
			int y = this.worldPosition.getY() + 2 * (i * 2 - 1);

			for (int z = this.worldPosition.getZ() - 2; z <= this.worldPosition.getZ() + 2; z++)
			{
				for (int x = this.worldPosition.getX() - 2; x <= this.worldPosition.getX() + 2; x++)
				{
					cPos.set(x, y, z);
					if (!isFluidChamberBlock(cache, cPos))
					{
						return false;
					}
				}
			}
		}

		for (int i = 0; i < 2; i++)
		{
			int z = this.worldPosition.getZ() + 2 * (i * 2 - 1);

			for (int y = this.worldPosition.getY() - 2 + 1; y <= this.worldPosition.getY() + 2 - 1; y++)
			{
				for (int x = this.worldPosition.getX() - 2; x <= this.worldPosition.getX() + 2; x++)
				{
					cPos.set(x, y, z);
					if (!isFluidChamberBlock(cache, cPos))
					{
						return false;
					}
				}
			}
		}

		for (int i = 0; i < 2; i++)
		{
			int x = this.worldPosition.getX() + 2 * (i * 2 - 1);

			for (int y = this.worldPosition.getY() - 2 + 1; y <= this.worldPosition.getY() + 2 - 1; y++)
			{
				for (int z = this.worldPosition.getZ() - 2 + 1; z <= this.worldPosition.getZ() + 2 - 1; z++)
				{
					cPos.set(x, y, z);
					if (!isFluidChamberBlock(cache, cPos))
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("heat".equals(name))
		{
			return this.maxHeat == 0 ? 0.0 : (double) this.heat / this.maxHeat;
		} else
		{
			throw new IllegalArgumentException("Invalid value: " + name);
		}
	}

	public Ic2rFluidTank getInputTank()
	{
		return this.inputTank;
	}

	public Ic2rFluidTank getOutputTank()
	{
		return this.outputTank;
	}

	@Override
	public int getMaxStackSize()
	{
		return 1;
	}
}
