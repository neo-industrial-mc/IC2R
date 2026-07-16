package me.halfcooler.ic2r.core.block.wiring.tileentity;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.tile.IEnergyEmitter;
import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;
import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.comp.ComparatorEmitter;
import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.comp.Redstone;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.event.TickHandler;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.util.StackUtil;

import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.HolderLookup;

public class TileEntityLuminator extends Ic2rTileEntity implements ServerTicker
{
	private static final Map<Direction, List<AABB>> aabbMap = getAabbMap();
	private final Energy energy = this.addComponent(Energy.asBasicSink(this, 5.0));
	private final Redstone redstone = this.addComponent(new Redstone(this));
	private boolean invertRedstone;

	public TileEntityLuminator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.LUMINATOR_FLAT, pos, state);
		ComparatorEmitter comparator = this.addComponent(new ComparatorEmitter(this));
		comparator.setUpdate(this.energy::getComparatorValue);
	}

	public static boolean isValidPosition(LevelReader world, BlockPos pos, Direction side)
	{
		if (world instanceof Level level && !level.isClientSide)
		{
			if (world.getBlockState(pos).isFaceSturdy(world, pos, side, SupportType.FULL))
			{
				return true;
			}

			IEnergyTile tile = EnergyNet.instance.getSubTile(level, pos);
			return tile instanceof IEnergyEmitter;
		} else
		{
			return true;
		}
	}

	private static Map<Direction, List<AABB>> getAabbMap()
	{
		Map<Direction, List<AABB>> ret = new EnumMap<>(Direction.class);

		for (Direction side : Direction.values())
		{
			int dx = side.getStepX();
			int dy = side.getStepY();
			int dz = side.getStepZ();
			double xS = (double) (dx + 1) / 2 * 0.9375;
			double yS = (double) (dy + 1) / 2 * 0.9375;
			double zS = (double) (dz + 1) / 2 * 0.9375;
			double xE = 0.0625 + (double) (dx + 2) / 2 * 0.9375;
			double yE = 0.0625 + (double) (dy + 2) / 2 * 0.9375;
			double zE = 0.0625 + (double) (dz + 2) / 2 * 0.9375;
			ret.put(side.getOpposite(), List.of(new AABB(xS, yS, zS, xE, yE, zE)));
		}

		return ret;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.invertRedstone = nbt.getBoolean("invert");
	}

	@Override
	public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("invert", this.invertRedstone);
	}

	@Override
	protected void onLoaded()
	{
		this.energy.setDirections(Collections.singleton(this.getFacing().getOpposite()), Collections.emptySet());
		this.energy.syncConsumerProfile(0);
		super.onLoaded();
		TickHandler.requestSingleWorldTick(this.getLevel(), world -> TileEntityLuminator.this.checkPlacement());
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean lit = this.isLit() && this.energy.useEnergy(0.25);
		if (this.getActive() != lit)
		{
			this.setActive(lit);
			this.updateLight();
		}

		if (lit)
		{
			this.igniteTouchingMonsters();
		}
	}

	private void igniteTouchingMonsters()
	{
		AABB bounds = aabbMap.get(this.getFacing()).get(0).move(this.worldPosition);
		for (Monster monster : this.getLevel().getEntitiesOfClass(Monster.class, bounds, Entity::isAlive))
		{
			boolean isUndead = monster.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD);
			monster.setRemainingFireTicks(isUndead ? 20 : 10);
		}
	}

	private boolean isLit()
	{
		return this.redstone.hasRedstoneInput() != this.invertRedstone;
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		if (!this.getLevel().isClientSide)
		{
			ItemStack stack = StackUtil.get(player, hand);
			double amount = 10000.0 - this.energy.getEnergy();
			if (!StackUtil.isEmpty(stack) && amount > 0.0 && (amount = ElectricItem.manager.discharge(stack, amount, this.energy.getSinkTier(), true, true, true)) > 0.0)
			{
				ElectricItem.manager.discharge(stack, amount, this.energy.getSinkTier(), true, true, false);
				this.energy.forceAddEnergy(amount);
			} else
			{
				this.invertRedstone = !this.invertRedstone;
				IC2R.network.get(true).updateTileEntityField(this, "invertRedstone");
			}
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		this.checkPlacement();
	}

	private void checkPlacement()
	{
		Level world = this.getLevel();
		if (!isValidPosition(world, this.worldPosition.relative(this.getFacing().getOpposite()), this.getFacing()))
		{
			world.destroyBlock(this.worldPosition, true);
		}
	}

	@Override
	protected VoxelShape getCollisionShape()
	{
		return Shapes.empty();
	}

	@Override
	protected List<AABB> getAabbs(boolean forCollision)
	{
		return aabbMap.get(this.getFacing());
	}

	@Override
	protected boolean canSetFacingWrench(Direction facing, Player player)
	{
		return true;
	}

	@Override
	protected boolean setFacingWrench(Level world, Direction facing, Player player)
	{
		this.invertRedstone = !this.invertRedstone;
		return true;
	}

	@Override
	protected boolean wrenchCanRemove(Player player)
	{
		return false;
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		super.onNetworkUpdate(field);
		if (field.equals("active"))
		{
			this.updateLight();
		}
	}

	private void updateLight()
	{
		this.getLevel().getChunkSource().getLightEngine().checkBlock(this.worldPosition);
	}
}
