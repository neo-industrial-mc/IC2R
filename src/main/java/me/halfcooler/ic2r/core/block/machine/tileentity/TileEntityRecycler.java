package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.recipe.IBasicMachineRecipeManager;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import me.halfcooler.ic2r.api.recipe.MachineRecipeResult;
import me.halfcooler.ic2r.api.recipe.RecipeOutput;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.invslot.InvSlotProcessableGeneric;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.recipe.BasicListRecipeManager;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.util.ConfigUtil;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityRecycler extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public TileEntityRecycler(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.RECYCLER, pos, state, 1, 45, 1);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, w -> Recipes.recycler);
	}

	public static void init()
	{
		Recipes.recycler = new TileEntityRecycler.RecyclerRecipeManager();
		Recipes.recyclerWhitelist = new BasicListRecipeManager();
		Recipes.recyclerBlacklist = new BasicListRecipeManager();
	}

	public static void initLate()
	{
		try
		{
			for (IRecipeInput input : ConfigUtil.asRecipeInputList(IC2RConfig.balance.recyclerBlacklist.get()))
			{
				Recipes.recyclerBlacklist.add(input);
			}

			for (IRecipeInput input : ConfigUtil.asRecipeInputList(IC2RConfig.balance.recyclerWhitelist.get()))
			{
				Recipes.recyclerWhitelist.add(input);
			}
		} catch (ParseException pe)
		{
			throw new RuntimeException(pe);
		}
	}

	public static int recycleChance()
	{
		return 8;
	}

	public static boolean getIsItemBlacklisted(ItemStack aStack)
	{
		return Recipes.recyclerWhitelist.isEmpty() ? Recipes.recyclerBlacklist.contains(aStack) : !Recipes.recyclerWhitelist.contains(aStack);
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2rSoundEvents.MACHINE_RECYCLER_OPERATE;
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return Ic2rSoundEvents.MACHINE_INTERRUPT1;
	}

	@Override
	public void operateOnce(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result, Collection<ItemStack> processResult)
	{
		this.inputSlot.consume(result);
		if (IC2R.random.nextInt(recycleChance()) == 0)
		{
			this.outputSlot.add(processResult);
		}
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

	private static class RecyclerRecipeManager implements IBasicMachineRecipeManager
	{
		public RecyclerRecipeManager()
		{
		}

		private static Collection<ItemStack> getOutput(ItemStack input)
		{
			return TileEntityRecycler.getIsItemBlacklisted(input) ? Collections.emptyList() : Collections.singletonList(new ItemStack(Ic2rItems.SCRAP));
		}

		public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, CompoundTag metadata, boolean replace)
		{
			return false;
		}

		public boolean addRecipe(IRecipeInput input, CompoundTag metadata, boolean replace, ItemStack... outputs)
		{
			return false;
		}

		@Override
		public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput)
		{
			if (StackUtil.isEmpty(input))
			{
				return null;
			}

			RecipeOutput ret = new RecipeOutput(null, new ArrayList<>(getOutput(input)));
			if (adjustInput)
			{
				input.shrink(1);
			}

			return ret;
		}

		public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest)
		{
			return StackUtil.isEmpty(input)
				? null
				: new MachineRecipe<>(Recipes.inputFactory.forStack(input, 1), getOutput(input))
				  .getResult(StackUtil.copyWithSize(input, StackUtil.getSize(input) - 1));
		}

		@Override
		public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isIterable()
		{
			return false;
		}
	}
}
