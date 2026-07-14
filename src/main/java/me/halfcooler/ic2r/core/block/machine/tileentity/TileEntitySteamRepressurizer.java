package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.energy.tile.IHeatSource;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rFluidTags;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.core.util.Util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.Objects;

@NotClassic
public class TileEntitySteamRepressurizer extends TileEntityInventory implements IHasGui
{
	private static Fluid detectedSteamFluid;
	@GuiSynced
	protected final Ic2rFluidTank output;
	@GuiSynced
	protected final Ic2rFluidTank input;
	protected final Fluids fluids = this.addComponent(new Fluids(this));
	protected int currentHeat;

	public TileEntitySteamRepressurizer(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.STEAM_REPRESSURIZER, pos, state);
		this.input = this.fluids.addTankInsert("input", 10000, Fluids.fluidPredicate(Ic2rFluids.STEAM.still(), Ic2rFluids.SUPERHEATED_STEAM.still()));
		this.output = this.fluids.addTankExtract("output", 10000);
	}

	public static boolean hasSteam()
	{
		return getSteam() != null;
	}

	private static Fluid getSteam()
	{
		Fluid ret = detectedSteamFluid;
		if (ret == null)
		{
			ITag<Fluid> tag = Objects.requireNonNull(ForgeRegistries.FLUIDS.tags()).getTag(Ic2rFluidTags.STEAM);
			for (Fluid entry : tag)
			{
				detectedSteamFluid = ret = entry;
				break;
			}

		}

		return ret;
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.currentHeat = nbt.getInt("heat");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("heat", this.currentHeat);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		Fluid steam = getSteam();
		if (steam != null)
		{
			if (this.input.getFluidAmount() >= 10)
			{
				if (this.currentHeat < this.input.getFluidAmount() / 10)
				{
					this.getHeat();
				}

				int amount = this.getOutput();

				while (this.currentHeat > 0 && this.input.getFluidAmount() >= 10 && this.canOutput(amount))
				{
					this.currentHeat--;
					this.input.drainMbUnchecked(10, false);
					this.output.fillMbUnchecked(Ic2rFluidStack.create(steam, amount), false);
				}
			}
		}
	}

	protected void getHeat()
	{
		int aim = this.input.getFluidAmount() / 10;
		if (aim > 0)
		{
			Level world = this.getLevel();
			int targetHeat = aim;

			for (Direction dir : Util.ALL_DIRS)
			{
				if (world.getBlockEntity(this.worldPosition.relative(dir)) instanceof IHeatSource hs)
				{
					int request = hs.drawHeat(dir.getOpposite(), targetHeat, true);
					if (request > 0)
					{
						targetHeat -= hs.drawHeat(dir.getOpposite(), request, false);
						if (targetHeat <= 0)
						{
							break;
						}
					}
				}
			}

			this.currentHeat += aim - targetHeat;
		}
	}

	protected int getOutput()
	{
		assert !this.input.isEmpty();
		Fluid fluid = this.input.getFluidStack().getFluid();
		if (fluid == Ic2rFluids.STEAM.still())
		{
			return IC2RConfig.balance.steamRepressurizer.steamPerSteam.get();
		} else if (fluid == Ic2rFluids.SUPERHEATED_STEAM.still())
		{
			return IC2RConfig.balance.steamRepressurizer.steamPerSuperSteam.get();
		} else
		{
			throw new IllegalStateException("Unknown tank contents: " + fluid);
		}
	}

	protected boolean canOutput(int amount)
	{
		return this.output.fillMbUnchecked(Ic2rFluidStack.create(getSteam(), amount), true) == amount;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	@Override
	public boolean getGuiState(String name)
	{
		return "valid".equals(name) ? hasSteam() : super.getGuiState(name);
	}
}
