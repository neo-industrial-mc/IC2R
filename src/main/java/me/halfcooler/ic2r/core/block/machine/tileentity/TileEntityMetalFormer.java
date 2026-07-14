package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.network.INetworkClientTileEntityEventListener;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.block.invslot.InvSlotProcessableGeneric;
import me.halfcooler.ic2r.core.block.machine.container.ContainerMetalFormer;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityMetalFormer
	extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
	implements INetworkClientTileEntityEventListener
{
	public static final int EventSwitch = 0;
	private int mode;

	public TileEntityMetalFormer(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.METAL_FORMER, pos, state, 10, 200, 1);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.metalformerExtruding);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.setMode(nbt.getInt("mode"));
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("mode", this.mode);
	}

	@Override
	public ContainerBase<TileEntityMetalFormer> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerMetalFormer(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerMetalFormer(syncId, inventory, this);
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		switch (event)
		{
			case 0:
				this.cycleMode();
		}
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		super.onNetworkUpdate(field);
		if (field.equals("mode"))
		{
			this.setMode(this.mode);
		}
	}

	public int getMode()
	{
		return this.mode;
	}

	public void setMode(int mode1)
	{
		InvSlotProcessableGeneric slot = (InvSlotProcessableGeneric) this.inputSlot;
		switch (mode1)
		{
			case 0:
				slot.setRecipeManager(Recipes.metalformerExtruding);
				break;
			case 1:
				slot.setRecipeManager(Recipes.metalformerRolling);
				break;
			case 2:
				slot.setRecipeManager(Recipes.metalformerCutting);
				break;
			default:
				throw new RuntimeException("invalid mode: " + mode1);
		}

		this.mode = mode1;
	}

	private void cycleMode()
	{
		this.setMode((this.getMode() + 1) % 3);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.Processing,
			UpgradableProperty.Transformer,
			UpgradableProperty.EnergyStorage,
			UpgradableProperty.ItemConsuming,
			UpgradableProperty.ItemProducing
		);
	}
}
