package me.halfcooler.ic2r.core.block.tileentity;

import me.halfcooler.ic2r.api.network.INetworkDataProvider;
import me.halfcooler.ic2r.api.network.INetworkUpdateListener;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Components;
import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.comp.TileEntityComponent;
import me.halfcooler.ic2r.core.event.TickHandler;
import me.halfcooler.ic2r.core.gui.dynamic.IGuiConditionProvider;
import me.halfcooler.ic2r.core.network.sync.BlockEntitySync;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.energy.profile.ElectricalDisplay;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class Ic2rTileEntity extends BlockEntity implements INetworkDataProvider, INetworkUpdateListener, IGuiConditionProvider
{
	private static final List<AABB> defaultAabbs = List.of(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
	private static final List<TileEntityComponent> emptyComponents = Collections.emptyList();
	private static final boolean debugLoad = System.getProperty("ic2r.te.debugload") != null;
	protected final Ic2rTileEntityBlock teBlock;
	private Map<Class<? extends TileEntityComponent>, TileEntityComponent> components;
	private List<TileEntityComponent> updatableComponents;
	private boolean active = false;
	private byte loadState = 0;
	private boolean enableWorldTick;
	/** Modern SyncKey registry; empty until subclasses override {@link #registerSyncedData}. Unregistered fields still use reflection. */
	private BlockEntitySync blockEntitySync;

	public Ic2rTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.teBlock = (Ic2rTileEntityBlock) state.getBlock();
	}

	/**
	 * Whether this TE should invoke {@link #updateEntityServer()} each server tick after load.
	 * Default: {@code this instanceof ServerTicker}. Prefer implementing {@link ServerTicker}
	 * over overriding this; keep override for rare edge cases only.
	 */
	protected boolean enablesServerWorldTick()
	{
		return this instanceof ServerTicker;
	}

	/**
	 * Whether this TE should invoke {@link #updateEntityClient()} each client tick after load.
	 * Default: {@code this instanceof ClientTicker}. Prefer implementing {@link ClientTicker}
	 * over overriding this; keep override for rare edge cases only.
	 */
	protected boolean enablesClientWorldTick()
	{
		return this instanceof ClientTicker;
	}

	public final Ic2rTileEntityBlock getBlockType()
	{
		return this.teBlock;
	}

	public final void setRemoved()
	{
		if (this.loadState == 2)
		{
			if (debugLoad)
			{
				IC2R.log.debug(LogCategory.Block, "TE markRemoved for %s at %s.", this, Util.formatPosition(this));
			}

			this.onUnloaded();
		} else
		{
			if (debugLoad)
			{
				IC2R.log.debug(LogCategory.Block, "Skipping TE markRemoved for %s at %s, state: %d.", this, Util.formatPosition(this), this.loadState);
			}

			this.loadState = 3;
		}

		super.setRemoved();
	}

	public final void clearRemoved()
	{
		super.clearRemoved();
		Level world = this.getLevel();
		if (world == null)
		{
			throw new IllegalStateException("no world/pos");
		}

		if (this.loadState != 0 && this.loadState != 3)
		{
			throw new IllegalStateException("invalid load state: " + this.loadState);
		}

		this.loadState = 1;
		TickHandler.requestSingleWorldTick(
			world,
			world1 ->
			{
				if (world1 == Ic2rTileEntity.this.getLevel() && !Ic2rTileEntity.this.isRemoved() && Ic2rTileEntity.this.loadState == 1 && world1.isLoaded(Ic2rTileEntity.this.worldPosition) && world1.getBlockState(Ic2rTileEntity.this.worldPosition).getBlock() == Ic2rTileEntity.this.teBlock && world1.getBlockEntity(Ic2rTileEntity.this.worldPosition) == Ic2rTileEntity.this)
				{
					if (Ic2rTileEntity.debugLoad)
					{
						IC2R.log.debug(LogCategory.Block, "TE onLoaded for %s at %s.", Ic2rTileEntity.this, Util.formatPosition(Ic2rTileEntity.this));
					}

					Ic2rTileEntity.this.onLoaded();
				} else if (Ic2rTileEntity.debugLoad)
				{
					IC2R.log.debug(LogCategory.Block, "Skipping TE init for %s at %s.", Ic2rTileEntity.this, Util.formatPosition(Ic2rTileEntity.this));
				}
			}
		);
	}

	protected void onLoaded()
	{
		if (this.loadState != 1)
		{
			throw new IllegalStateException("invalid load state: " + this.loadState);
		}

		this.loadState = 2;
		this.enableWorldTick = this.level.isClientSide ? this.enablesClientWorldTick() : this.enablesServerWorldTick();
		if (this.components != null)
		{
			for (TileEntityComponent component : this.components.values())
			{
				component.onLoaded();
				if (component.enableWorldTick())
				{
					if (this.updatableComponents == null)
					{
						this.updatableComponents = new ArrayList<>(4);
					}

					this.updatableComponents.add(component);
				}
			}
		}
	}

	protected void onUnloaded()
	{
		if (this.loadState == 3)
		{
			throw new IllegalStateException("invalid load state: " + this.loadState);
		}

		this.loadState = 3;
		if (this.components != null)
		{
			for (TileEntityComponent component : this.components.values())
			{
				component.onUnloaded();
			}
		}
	}

	public void load(CompoundTag nbt)
	{
		this.active = nbt.getBoolean("active");
		if (this.components != null && nbt.contains("components", 10))
		{
			CompoundTag componentsNbt = nbt.getCompound("components");

			for (String name : componentsNbt.getAllKeys())
			{
				Class<? extends TileEntityComponent> cls = Components.getClass(name);
				TileEntityComponent component;
				if (cls != null && (component = this.getComponent(cls)) != null)
				{
					CompoundTag componentNbt = componentsNbt.getCompound(name);
					component.readFromNbt(componentNbt);
				} else
				{
					IC2R.log.warn(LogCategory.Block, "Can't find component %s while loading %s.", name, this);
				}
			}
		}
	}

	public void saveAdditional(CompoundTag nbt)
	{
		nbt.putBoolean("active", this.active);
		if (this.components != null)
		{
			CompoundTag componentsNbt = null;

			for (TileEntityComponent component : this.components.values())
			{
				CompoundTag componentNbt = component.writeToNbt();
				if (componentNbt != null)
				{
					if (componentsNbt == null)
					{
						componentsNbt = new CompoundTag();
						nbt.put("components", componentsNbt);
					}

					String id = Components.getId(component.getClass());
					if (id == null)
					{
						throw new RuntimeException("no component id for " + component.getClass().getName());
					}

					componentsNbt.put(id, componentNbt);
				}
			}
		}
	}

	public final void tick()
	{
		if (this.loadState == 2)
		{
			if (this.updatableComponents != null)
			{
				for (TileEntityComponent component : this.updatableComponents)
				{
					component.onWorldTick();
				}
			}

			if (this.enableWorldTick)
			{
				if (this.level.isClientSide)
				{
					this.updateEntityClient();
				} else
				{
					this.updateEntityServer();
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	protected void updateEntityClient()
	{
	}

	protected void updateEntityServer()
	{
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = new ArrayList<>(3);
		ret.add("teBlk=" + ForgeRegistries.BLOCKS.getKey(this.teBlock));
		ret.add("active");
		return ret;
	}

	/**
	 * Modern sync registry (W1.1+ / G1.1). Subclasses register SyncKeys via {@link #registerSyncedData}.
	 * TeUpdate and {@code NetworkManager.writeFieldData} prefer this table when a field (or legacy alias)
	 * is registered; unregistered names still use reflection. Packet field <em>names</em> remain
	 * legacy strings from {@link #getNetworkedFields()}.
	 */
	public final BlockEntitySync getBlockEntitySync()
	{
		if (this.blockEntitySync == null)
		{
			BlockEntitySync sync = new BlockEntitySync();
			this.registerSyncedData(sync);
			this.blockEntitySync = sync;
		}

		return this.blockEntitySync;
	}

	/**
	 * Override to register {@link me.halfcooler.ic2r.core.network.sync.SyncKey}-based fields
	 * (snake_case logical names + optional legacy TeUpdate aliases). Default no-op.
	 */
	protected void registerSyncedData(BlockEntitySync sync)
	{
	}

	/**
	 * Apply {@code active} from modern sync decode without network re-broadcast
	 * (mirrors client-side reflection field write).
	 */
	protected void applySyncedActive(boolean active)
	{
		this.active = active;
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("active") && this.hasActiveTexture() || field.equals("facing"))
		{
			this.rerender();
		}
	}

	@OnlyIn(Dist.CLIENT)
	private boolean hasActiveTexture()
	{
		return this.teBlock.canActive();
	}

	public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing)
	{
	}

	protected VoxelShape getOutlineShape()
	{
		return this.getShape(false);
	}

	protected VoxelShape getCollisionShape()
	{
		return this.getShape(true);
	}

	protected VoxelShape getCullingShape()
	{
		return this.getShape(false);
	}

	private VoxelShape getShape(boolean forCollision)
	{
		List<AABB> aabbs = this.getAabbs(forCollision);
		if (aabbs == defaultAabbs)
		{
			return Shapes.block();
		}

		if (aabbs.isEmpty())
		{
			throw new RuntimeException("No AABBs for " + this);
		}

		if (aabbs.size() == 1)
		{
			return Shapes.create(aabbs.get(0));
		}

		VoxelShape ret = null;

		for (AABB aabb : aabbs)
		{
			VoxelShape shape = Shapes.create(aabb);
			if (ret == null)
			{
				ret = shape;
			} else
			{
				ret = Shapes.or(ret, shape);
			}
		}

		return ret;
	}

	protected void onEntityCollision(Entity entity)
	{
	}

	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		if (!(this instanceof IHasGui) || this.level == null)
		{
			return InteractionResult.PASS;
		} else if (this.level.isClientSide)
		{
			return InteractionResult.SUCCESS;
		} else
		{
			return ((IHasGui) this).openManagedBe(player, hand) ? InteractionResult.CONSUME : InteractionResult.PASS;
		}
	}

	protected InteractionResult onClicked(Player player)
	{
		return InteractionResult.PASS;
	}

	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		if (this.components != null)
		{
			for (TileEntityComponent component : this.components.values())
			{
				component.onNeighborChange(neighbor, neighborPos);
			}
		}
	}

	protected boolean recolor(Direction side, DyeColor color)
	{
		return false;
	}

	protected void onExploded(Explosion explosion)
	{
	}

	protected void onBlockBreak()
	{
	}

	protected boolean onRemovedByPlayer(Player player, boolean willHarvest)
	{
		return true;
	}

	protected ItemStack getPickBlock()
	{
		return new ItemStack(this.teBlock);
	}

	protected List<ItemStack> getSelfDrops(boolean wrench)
	{
		ItemStack drop = this.getPickBlock();
		drop = this.adjustDrop(drop, wrench);
		return drop == null ? Collections.emptyList() : List.of(drop);
	}

	protected List<ItemStack> getAuxDrops(int fortune)
	{
		return Collections.emptyList();
	}

	protected boolean canEntityDestroy(Entity entity)
	{
		return true;
	}

	public Direction getFacing()
	{
		return this.teBlock.facingProperty == null ? Direction.NORTH : this.getBlockState().getValue(this.teBlock.facingProperty);
	}

	protected boolean canSetFacingWrench(Direction facing, Player player)
	{
		if (!this.teBlock.allowWrenchRotating())
		{
			return false;
		} else
		{
			return facing != this.getFacing() && this.getSupportedFacings().contains(facing);
		}
	}

	protected boolean setFacingWrench(Level world, Direction facing, Player player)
	{
		if (!this.canSetFacingWrench(facing, player))
		{
			return false;
		}

		this.setFacing(world, facing);
		return true;
	}

	protected boolean wrenchCanRemove(Player player)
	{
		return true;
	}

	protected List<AABB> getAabbs(boolean forCollision)
	{
		return defaultAabbs;
	}

	public ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		if (wrench)
		{
			return drop;
		}

		return switch (this.teBlock.getDefaultDrop())
		{
			case Self -> drop;
			case None -> null;
			case Generator -> new ItemStack(Ic2rItems.GENERATOR);
			case Machine -> new ItemStack(Ic2rItems.MACHINE);
			case AdvMachine -> new ItemStack(Ic2rItems.ADVANCED_MACHINE);
		};
	}

	protected Set<Direction> getSupportedFacings()
	{
		return this.teBlock.getSupportedFacings();
	}

	protected void setFacing(Level world, Direction facing)
	{
		if (facing == null)
		{
			throw new NullPointerException("null facing");
		}

		if (this.getFacing().ordinal() == facing.ordinal())
		{
			throw new IllegalArgumentException("unchanged facing");
		}

		if (!this.getSupportedFacings().contains(facing))
		{
			throw new IllegalArgumentException("invalid facing: " + facing + ", supported: " + this.getSupportedFacings());
		}

		BlockState newState = world.getBlockState(this.worldPosition).setValue(this.teBlock.facingProperty, facing);
		world.setBlockAndUpdate(this.worldPosition, newState);
	}

	public boolean getActive()
	{
		return this.active;
	}

	public void setActive(boolean active)
	{
		if (this.teBlock.canActive())
		{
			if (this.active != active)
			{
				this.active = active;
				IC2R.network.get(true).updateTileEntityField(this, "active");
			}
		}
	}

	@Override
	public boolean getGuiState(String name)
	{
		if ("active".equals(name))
		{
			return this.getActive();
		} else
		{
			throw new IllegalArgumentException("Unexpected GUI value requested: " + name);
		}
	}

	public void appendItemTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag advanced)
	{
		if (this.hasComponent(Energy.class))
		{
			Energy energy = this.getComponent(Energy.class);
			boolean hasSource = !energy.getSourceDirs().isEmpty();
			boolean hasSink = !energy.getSinkDirs().isEmpty();
			if (hasSource || hasSink)
			{
				Ic2rTooltip.add(tooltip, ElectricalDisplay.formatVoltage(energy.getWorkingVoltage()));
			}

			if (hasSource && !hasSink)
			{
				Ic2rTooltip.add(tooltip, ElectricalDisplay.formatPower(energy));
			} else if (hasSink && !hasSource)
			{
				Ic2rTooltip.add(tooltip, ElectricalDisplay.formatPower(energy));
			}
		}
	}

	protected final <T extends TileEntityComponent> T addComponent(T component)
	{
		if (component == null)
		{
			throw new NullPointerException("null component");
		}

		if (this.components == null)
		{
			this.components = new IdentityHashMap<>(4);
		}

		TileEntityComponent prev = this.components.put(component.getClass(), component);
		if (prev != null)
		{
			throw new RuntimeException("conflicting component while adding " + component + ", already used by " + prev + ".");
		} else
		{
			return component;
		}
	}

	public boolean hasComponent(Class<? extends TileEntityComponent> cls)
	{
		return this.components != null && this.components.containsKey(cls);
	}

	public <T extends TileEntityComponent> T getComponent(Class<T> cls)
	{
		return (T) (this.components == null ? null : this.components.get(cls));
	}

	public final Iterable<? extends TileEntityComponent> getComponents()
	{
		return this.components == null ? emptyComponents : this.components.values();
	}

	protected final void rerender()
	{
		BlockState state = this.getBlockState();
		Objects.requireNonNull(this.getLevel()).sendBlockUpdated(this.worldPosition, state, state, 2);
		if (this.teBlock.canActive() && this.level != null)
		{
			this.level.setBlockAndUpdate(this.worldPosition, state.setValue(Ic2rTileEntityBlock.ACTIVE, this.active));
		}
	}

}
