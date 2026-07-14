package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.api.item.IMiningDrill;
import me.halfcooler.ic2r.api.network.INetworkClientTileEntityEventListener;
import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.Ic2rPlayer;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumable;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableBlock;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableClass;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableId;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.block.machine.container.ContainerMiner;
import me.halfcooler.ic2r.core.fluid.FluidHandler;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.init.OreValues;
import me.halfcooler.ic2r.core.item.tool.ItemScanner;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rBlockTags;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.util.LiquidUtil;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import me.halfcooler.ic2r.core.IC2R;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityMiner extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, INetworkClientTileEntityEventListener
{
	public final InvSlot buffer;
	public final InvSlotUpgrade upgradeSlot;
	public final InvSlotConsumable drillSlot;
	public final InvSlotConsumable pipeSlot;
	public final InvSlotConsumable scannerSlot;
	public int progress = 0;
	@GuiSynced
	public boolean pumpMode = false;
	public boolean canProvideLiquid = false;
	public BlockPos liquidPos;
	boolean tickingUpgrades = false;
	private TileEntityMiner.Mode lastMode = TileEntityMiner.Mode.None;
	private int scannedLevel = -1;
	private int scanRange = 0;
	private int lastX;
	private int lastZ;

	public TileEntityMiner(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.MINER, pos, state, 1000, IC2RConfig.balance.minerDischargeTier.get(), false);
		this.drillSlot = new InvSlotConsumableClass(this, "drill", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP, IMiningDrill.class)
		{
			@Override
			public boolean canOutput()
			{
				return !TileEntityMiner.this.tickingUpgrades && super.canOutput();
			}
		};
		this.pipeSlot = new InvSlotConsumableBlock(this, "pipe", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP)
		{
			@Override
			public boolean canOutput()
			{
				return !TileEntityMiner.this.tickingUpgrades && super.canOutput();
			}
		};
		this.scannerSlot = new InvSlotConsumableId(this, "scanner", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, Ic2rItems.SCANNER, Ic2rItems.ADVANCED_SCANNER)
		{
			@Override
			public boolean canOutput()
			{
				return !TileEntityMiner.this.tickingUpgrades && super.canOutput();
			}
		};
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 1);
		this.buffer = new InvSlot(this, "buffer", InvSlot.Access.IO, 15, InvSlot.InvSide.SIDE);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.scannedLevel = -1;
		this.lastX = this.worldPosition.getX();
		this.lastZ = this.worldPosition.getZ();
		this.canProvideLiquid = false;
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.lastMode = TileEntityMiner.Mode.values()[nbt.getInt("lastMode")];
		this.progress = nbt.getInt("progress");
		this.pumpMode = nbt.getBoolean("pumpMode");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("lastMode", this.lastMode.ordinal());
		nbt.putInt("progress", this.progress);
		nbt.putBoolean("pumpMode", this.pumpMode);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.chargeTools();
		this.tickingUpgrades = true;
		this.upgradeSlot.tick();
		this.tickingUpgrades = false;
		if (this.work())
		{
			this.setChanged();
			this.activate(false);
		} else
		{
			this.shutdown(false);
		}
	}

	private void chargeTools()
	{
		if (!this.scannerSlot.isEmpty())
		{
			this.energy.useEnergy(ElectricItem.manager.charge(this.scannerSlot.get(), this.energy.getEnergy(), 2, false, false));
		}

		if (!this.drillSlot.isEmpty())
		{
			this.energy.useEnergy(ElectricItem.manager.charge(this.drillSlot.get(), this.energy.getEnergy(), 3, false, false));
		}
	}

	private boolean work()
	{
		MutableBlockPos operatingPos = this.getOperationPos();
		if (this.drillSlot.isEmpty())
		{
			return this.withDrawPipe(operatingPos);
		}

		if (operatingPos.getY() >= this.level.getMinBuildHeight())
		{
			Level world = this.getLevel();
			BlockState state = world.getBlockState(operatingPos);
			if (state.getBlock() != Ic2rBlocks.MINING_PIPE_TIP)
			{
				return operatingPos.getY() > world.getMinBuildHeight() && this.digDown(operatingPos, state, false);
			} else
			{
				TileEntityMiner.MineResult result = this.mineLevel(operatingPos.getY());
				if (result == TileEntityMiner.MineResult.Done)
				{
					operatingPos.move(Direction.DOWN);
					state = world.getBlockState(operatingPos);
					return this.digDown(operatingPos, state, true);
				} else
				{
					return result == TileEntityMiner.MineResult.Working;
				}
			}
		} else
		{
			return false;
		}
	}

	private MutableBlockPos getOperationPos()
	{
		MutableBlockPos ret = this.worldPosition.mutable().move(Direction.DOWN);
		Level world = this.getLevel();
		int bottom = world.getMinBuildHeight();
		BlockState pipeState = Ic2rBlocks.MINING_PIPE.defaultBlockState();

		while (ret.getY() >= bottom)
		{
			BlockState state = world.getBlockState(ret);
			if (state != pipeState)
			{
				return ret;
			}

			ret.move(Direction.DOWN);
		}

		return ret;
	}

	private boolean withDrawPipe(MutableBlockPos operatingPos)
	{
		if (this.lastMode != TileEntityMiner.Mode.Withdraw)
		{
			this.lastMode = TileEntityMiner.Mode.Withdraw;
			this.progress = 0;
		}

		if (operatingPos.getY() < this.level.getMinBuildHeight() || this.level.getBlockState(operatingPos).getBlock() != Ic2rBlocks.MINING_PIPE_TIP)
		{
			operatingPos.move(Direction.UP);
		}

		if (operatingPos.getY() != this.worldPosition.getY() && this.energy.getEnergy() >= 3.0)
		{
			this.syncElectricalProfile(3);
			if (this.progress < 20)
			{
				this.energy.useEnergy(3.0);
				this.progress++;
			} else
			{
				this.progress = 0;
				this.removePipe(operatingPos);
			}

			return true;
		} else
		{
			return false;
		}
	}

	private void removePipe(MutableBlockPos operatingPos)
	{
		Level world = this.getLevel();
		world.removeBlock(operatingPos, false);
		this.storeDrop(new ItemStack(Ic2rItems.MINING_PIPE));
		ItemStack pipe = this.pipeSlot.consume(1, true, false);
		if (pipe != null && pipe.getItem() != Ic2rItems.MINING_PIPE)
		{
			ItemStack filler = this.pipeSlot.consume(1);
			Item fillerItem = filler.getItem();
			if (fillerItem instanceof BlockItem)
			{
				((BlockItem) fillerItem).place(new DirectionalPlaceContext(world, operatingPos.above(), Direction.DOWN, filler, Direction.UP));
			}
		}
	}

	private boolean digDown(MutableBlockPos operatingPos, BlockState state, boolean removeTipAbove)
	{
		ItemStack pipe = this.pipeSlot.consume(1, true, false);
		if (pipe == null || pipe.getItem() != Ic2rItems.MINING_PIPE)
		{
			return false;
		}

		if (operatingPos.getY() < this.level.getMinBuildHeight())
		{
			if (removeTipAbove)
			{
				this.getLevel().setBlockAndUpdate(operatingPos.setY(this.level.getMinBuildHeight()), Ic2rBlocks.MINING_PIPE.defaultBlockState());
			}

			return false;
		} else
		{
			TileEntityMiner.MineResult result = this.mineBlock(operatingPos, state);
			if (result != TileEntityMiner.MineResult.Failed_Temp && result != TileEntityMiner.MineResult.Failed_Perm)
			{
				if (result == TileEntityMiner.MineResult.Done)
				{
					if (removeTipAbove)
					{
						this.getLevel().setBlockAndUpdate(operatingPos.above(), Ic2rBlocks.MINING_PIPE.defaultBlockState());
					}

					this.pipeSlot.consume(1);
					this.getLevel().setBlockAndUpdate(operatingPos, Ic2rBlocks.MINING_PIPE_TIP.defaultBlockState());
				}

				return true;
			} else
			{
				if (removeTipAbove)
				{
					this.getLevel().setBlockAndUpdate(operatingPos.move(Direction.UP), Ic2rBlocks.MINING_PIPE.defaultBlockState());
				}

				return false;
			}
		}
	}

	private TileEntityMiner.MineResult mineLevel(int y)
	{
		if (this.scannerSlot.isEmpty())
		{
			return TileEntityMiner.MineResult.Done;
		}

		if (this.scannedLevel != y)
		{
			this.scanRange = ((ItemScanner) this.scannerSlot.get().getItem()).startLayerScan(this.scannerSlot.get());
		}

		if (this.scanRange <= 0)
		{
			return TileEntityMiner.MineResult.Failed_Temp;
		}

		this.scannedLevel = y;
		MutableBlockPos target = new MutableBlockPos();
		Level world = this.getLevel();
		Player player = Ic2rPlayer.get(world);

		for (int x = this.worldPosition.getX() - this.scanRange; x <= this.worldPosition.getX() + this.scanRange; x++)
		{
			for (int z = this.worldPosition.getZ() - this.scanRange; z <= this.worldPosition.getZ() + this.scanRange; z++)
			{
				target.set(x, y, z);
				BlockState state = world.getBlockState(target);
				boolean isValidTarget = false;
				if ((
					state.is(Ic2rBlockTags.ORES)
						|| OreValues.get(StackUtil.getDrops(world, target, state, 0)) > 0
						|| OreValues.get(StackUtil.getPickStack(world, target, state, player)) > 0
				)
					&& this.canMine(target, state))
				{
					isValidTarget = true;
				} else if (this.pumpMode)
				{
					LiquidUtil.LiquidData liquid = LiquidUtil.getLiquid(world, target);
					if (liquid != null && this.canPump(target))
					{
						isValidTarget = true;
					}
				}

				if (isValidTarget)
				{
					TileEntityMiner.MineResult result = this.mineTowards(target);
					if (result == TileEntityMiner.MineResult.Done)
					{
						return TileEntityMiner.MineResult.Working;
					}

					if (result != TileEntityMiner.MineResult.Failed_Perm)
					{
						return result;
					}
				}
			}
		}

		return TileEntityMiner.MineResult.Done;
	}

	private TileEntityMiner.MineResult mineTowards(BlockPos dst)
	{
		int dx = Math.abs(dst.getX() - this.worldPosition.getX());
		int sx = this.worldPosition.getX() < dst.getX() ? 1 : -1;
		int dz = -Math.abs(dst.getZ() - this.worldPosition.getZ());
		int sz = this.worldPosition.getZ() < dst.getZ() ? 1 : -1;
		int err = dx + dz;
		MutableBlockPos target = new MutableBlockPos();
		int cx = this.worldPosition.getX();
		int cz = this.worldPosition.getZ();

		while (cx != dst.getX() || cz != dst.getZ())
		{
			boolean isCurrentPos = cx == this.lastX && cz == this.lastZ;
			int e2 = 2 * err;
			if (e2 > dz)
			{
				err += dz;
				cx += sx;
			} else if (e2 < dx)
			{
				err += dx;
				cz += sz;
			}

			target.set(cx, dst.getY(), cz);
			Level world = this.getLevel();
			BlockState state = world.getBlockState(target);
			boolean isBlocking = false;
			if (isCurrentPos)
			{
				isBlocking = true;
			} else if (!state.isAir())
			{
				LiquidUtil.LiquidData liquid = LiquidUtil.getLiquid(world, target);
				if (liquid == null || liquid.isSource || this.pumpMode && this.canPump(target))
				{
					isBlocking = true;
				}
			}

			if (isBlocking)
			{
				TileEntityMiner.MineResult result = this.mineBlock(target, state);
				if (result == TileEntityMiner.MineResult.Done)
				{
					this.lastX = cx;
					this.lastZ = cz;
				}

				return result;
			}
		}

		this.lastX = this.worldPosition.getX();
		this.lastZ = this.worldPosition.getZ();
		return TileEntityMiner.MineResult.Done;
	}

	private TileEntityMiner.MineResult mineBlock(BlockPos target, BlockState state)
	{
		Level world = this.getLevel();
		Block block = state.getBlock();
		boolean isAirBlock = true;
		if (!state.isAir())
		{
			isAirBlock = false;
			LiquidUtil.LiquidData liquidData = LiquidUtil.getLiquid(world, target);
			if (liquidData != null)
			{
				if (liquidData.isSource || this.pumpMode && this.canPump(target))
				{
					this.liquidPos = new BlockPos(target);
					this.canProvideLiquid = true;
					return !this.pumpMode && !this.canMine(target, state) ? TileEntityMiner.MineResult.Failed_Perm : TileEntityMiner.MineResult.Failed_Temp;
				}
			} else if (!this.canMine(target, state))
			{
				return TileEntityMiner.MineResult.Failed_Perm;
			}
		}

		this.canProvideLiquid = false;
		Item drillItem = this.drillSlot.get().getItem();
		int energyPerTick;
		int duration;
		TileEntityMiner.Mode mode;
		if (isAirBlock)
		{
			mode = TileEntityMiner.Mode.MineAir;
			energyPerTick = 3;
			duration = 20;
		} else if (drillItem == Ic2rItems.DRILL)
		{
			mode = TileEntityMiner.Mode.MineDrill;
			energyPerTick = 6;
			duration = 200;
		} else if (drillItem == Ic2rItems.DIAMOND_DRILL)
		{
			mode = TileEntityMiner.Mode.MineDDrill;
			energyPerTick = 20;
			duration = 50;
		} else if (drillItem == Ic2rItems.IRIDIUM_DRILL)
		{
			mode = TileEntityMiner.Mode.MineIDrill;
			energyPerTick = 200;
			duration = 20;
		} else
		{
			if (!(drillItem instanceof IMiningDrill))
			{
				throw new IllegalStateException("invalid drill: " + this.drillSlot.get());
			}

			mode = TileEntityMiner.Mode.MineCustomDrill;
			IMiningDrill drill = (IMiningDrill) this.drillSlot.get().getItem();
			energyPerTick = drill.energyUse(this.drillSlot.get(), world, target, state);
			duration = drill.breakTime(this.drillSlot.get(), world, target, state);
		}

		if (this.lastMode != mode)
		{
			this.lastMode = mode;
			this.progress = 0;
		}

		this.syncElectricalProfile(energyPerTick);

		if (this.progress < duration)
		{
			if (this.energy.useEnergy(energyPerTick))
			{
				this.progress++;
				return TileEntityMiner.MineResult.Working;
			}
		} else if (isAirBlock || this.harvestBlock(target, state))
		{
			this.progress = 0;
			return TileEntityMiner.MineResult.Done;
		}

		return TileEntityMiner.MineResult.Failed_Temp;
	}

	private boolean harvestBlock(BlockPos target, BlockState state)
	{
		int energyCost = 2 * (this.worldPosition.getY() - target.getY());
		if (this.energy.getEnergy() < energyCost)
		{
			return false;
		}

		Level world = this.getLevel();
		switch (this.lastMode)
		{
			case MineDrill:
				if (!ElectricItem.manager.use(this.drillSlot.get(), 50.0, null))
				{
					return false;
				}
				break;
			case MineDDrill:
				if (!ElectricItem.manager.use(this.drillSlot.get(), 80.0, null))
				{
					return false;
				}
				break;
			case MineIDrill:
				if (!ElectricItem.manager.use(this.drillSlot.get(), 800.0, null))
				{
					return false;
				}
				break;
			case MineCustomDrill:
				if (!((IMiningDrill) this.drillSlot.get().getItem()).breakBlock(this.drillSlot.get(), world, target, state))
				{
					return false;
				}
				break;
			default:
				throw new IllegalStateException("Invalid mode " + this.lastMode + " with drill: " + this.drillSlot.get());
		}

		this.energy.useEnergy(energyCost);

		for (ItemStack drop : StackUtil.getDrops(world, target, state, this.lastMode == TileEntityMiner.Mode.MineIDrill ? 3 : 0))
		{
			this.storeDrop(drop);
		}

		world.removeBlock(target, false);
		return true;
	}

	private void storeDrop(ItemStack stack)
	{
		if (StackUtil.putInInventory(this, Direction.WEST, stack, true) == 0)
		{
			StackUtil.dropAsEntity(this.getLevel(), this.worldPosition, stack);
		} else
		{
			StackUtil.putInInventory(this, Direction.WEST, stack, false);
		}
	}

	public boolean canPump(BlockPos target)
	{
		return this.isPumpConnected(target);
	}

	public String getPumpModeTooltip()
	{
		return Component.translatable(this.pumpMode ? "ic2r.Miner.gui.pumpMode.on" : "ic2r.Miner.gui.pumpMode.off").getString();
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event == 0)
		{
			this.pumpMode = !this.pumpMode;
			IC2R.sideProxy.messagePlayer(player, this.getPumpModeTooltip());
		}
	}

	public boolean canMine(BlockPos target, BlockState state)
	{
		Block block = state.getBlock();
		if (state.isAir())
		{
			return true;
		}

		if (block != Ic2rBlocks.MINING_PIPE && block != Ic2rBlocks.MINING_PIPE_TIP && block != Blocks.CHEST)
		{
			if ((block == Blocks.WATER || block == Blocks.LAVA || FluidHandler.getWorldFluid(state) != null) && this.isPumpConnected(target))
			{
				return true;
			} else
			{
				Level world = this.getLevel();
				if (state.getDestroySpeed(world, target) < 0.0F)
				{
					return false;
				} else if (!state.requiresCorrectToolForDrops())
				{
					return true;
				} else if (block == Blocks.COBWEB)
				{
					return true;
				} else
				{
					return !this.drillSlot.isEmpty() && this.drillSlot.get().isCorrectToolForDrops(state);
				}
			}
		} else
		{
			return false;
		}
	}

	public boolean isPumpConnected(BlockPos target)
	{
		Level world = this.getLevel();

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity te = world.getBlockEntity(this.worldPosition.relative(dir));
			if (te instanceof TileEntityPump && ((TileEntityPump) te).pump(target, true, this) != null)
			{
				return true;
			}
		}

		return false;
	}

	public boolean isAnyPumpConnected()
	{
		Level world = this.getLevel();

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity te = world.getBlockEntity(this.worldPosition.relative(dir));
			if (te instanceof TileEntityPump)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2rSoundEvents.MACHINE_MINER_OPERATE;
	}

	@Override
	public ContainerBase<TileEntityMiner> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerMiner(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerMiner(syncId, inventory, this);
	}

	@Override
	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
	}

	enum MineResult
	{
		Working,
		Done,
		Failed_Temp,
		Failed_Perm
	}

	enum Mode
	{
		None,
		Withdraw,
		MineAir,
		MineDrill,
		MineDDrill,
		MineIDrill,
		MineCustomDrill
	}
}
