package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.item.IBlockCuttingBlade;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipeResult;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableClass;
import me.halfcooler.ic2r.core.block.invslot.InvSlotProcessableGeneric;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityBlockCutter extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public final InvSlotConsumableClass cutterSlot;
	@GuiSynced
	private boolean bladeTooWeak = false;

	public TileEntityBlockCutter(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.BLOCK_CUTTER, pos, state, 4, 450, 1);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.block_cutter);
		this.cutterSlot = new InvSlotConsumableClass(this, "cutter_input_slot", 1, IBlockCuttingBlade.class);
	}

	@Override
	public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getRecipeResult()
	{
		if (this.cutterSlot.isEmpty())
		{
			if (!this.bladeTooWeak)
			{
				this.bladeTooWeak = true;
			}

			return null;
		} else
		{
			if (this.bladeTooWeak)
			{
				this.bladeTooWeak = false;
			}

			MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> ret = super.getRecipeResult();
			if (ret != null && ret.recipe().getMetaData() != null)
			{
				ItemStack bladeStack = this.cutterSlot.get();
				IBlockCuttingBlade blade = (IBlockCuttingBlade) bladeStack.getItem();
				if (ret.recipe().getMetaData().getInt("hardness") > blade.getHardness(bladeStack))
				{
					if (!this.bladeTooWeak)
					{
						this.bladeTooWeak = true;
					}

					return null;
				} else
				{
					if (this.bladeTooWeak)
					{
						this.bladeTooWeak = false;
					}

					return ret;
				}
			} else
			{
				return null;
			}
		}
	}

	@Override
	public boolean getGuiState(String name)
	{
		return "isBladeTooWeak".equals(name) ? this.bladeTooWeak : super.getGuiState(name);
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
