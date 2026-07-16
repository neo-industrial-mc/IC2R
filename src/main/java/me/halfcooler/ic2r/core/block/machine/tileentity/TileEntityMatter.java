package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.energy.tile.IExplosionPowerOverride;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipeResult;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.comp.Redstone;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquid;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquidByList;
import me.halfcooler.ic2r.core.block.invslot.InvSlotOutput;
import me.halfcooler.ic2r.core.block.invslot.InvSlotProcessable;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.block.machine.container.ContainerMatter;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.recipe.MatterAmplifierRecipeManager;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;

@NotClassic
public class TileEntityMatter extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IExplosionPowerOverride
{
	public final InvSlotUpgrade upgradeSlot;
	public final InvSlotProcessable<IRecipeInput, Integer, ItemStack> amplifierSlot = new InvSlotProcessable<>(
		this, "scrap", 1, Recipes.matterFabricator
	)
	{
		protected ItemStack getInput(ItemStack stack)
		{
			return stack;
		}

		protected void setInput(ItemStack input)
		{
			this.put(input);
		}
	};
	public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 1);
	public final InvSlotConsumableLiquid containerSlot = new InvSlotConsumableLiquidByList(
		this, "container", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Fill, Ic2rFluids.UU_MATTER.still()
	);
	@GuiSynced
	public final Ic2rFluidTank fluidTank;
	protected final Redstone redstone;
	protected final Fluids fluids;
	public int scrap = 0;
	public boolean redstonePowered = false;
	private double lastEnergy;
	private int prevState = 0;

	public TileEntityMatter(BlockPos pos, BlockState state)
	{
		super(
			Ic2rBlockEntities.MATTER_GENERATOR, pos, state, Math.round(1000000.0F * IC2RConfig.balance.uuEnergyFactor.get().floatValue()), getDefaultTier()
		);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
		this.redstone = this.addComponent(new Redstone(this));
		this.redstone.subscribe(newLevel -> this.energy.setEnabled(newLevel == 0));
		this.fluids = this.addComponent(new Fluids(this));
		this.fluidTank = this.fluids.addTank("fluidTank", 8000, Fluids.fluidPredicate(Ic2rFluids.UU_MATTER.still()));
		this.comparator.setUpdate(() ->
		{
			int count = calcRedstoneFromInvSlots(this.amplifierSlot);
			if (count > 0)
			{
				return count;
			} else
			{
				return this.scrap > 0 ? 1 : 0;
			}
		});
	}

	private static int getDefaultTier()
	{
		return IC2RConfig.balance.matterFabricatorTier.get();
	}

	public static void init()
	{
		Recipes.matterAmplifier = new MatterAmplifierRecipeManager();
		addAmplifier(new ItemStack(Ic2rItems.SCRAP), 1, 5000);
		addAmplifier(new ItemStack(Ic2rItems.SCRAP_BOX), 1, 45000);
	}

	public static void addAmplifier(ItemStack input, int amount, int amplification)
	{
		addAmplifier(Recipes.inputFactory.forStack(input, amount), amplification);
	}

	public static void addAmplifier(IRecipeInput input, int amplification)
	{
	}

	private static int applyModifier(int base, int extra)
	{
		double ret = Math.round(((double) base + extra));
		return ret > 2.147483647E9 ? Integer.MAX_VALUE : (int) ret;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.scrap = nbt.getInt("scrap");
		this.lastEnergy = nbt.getDouble("lastEnergy");
	}

	@Override
	public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.saveAdditional(nbt, registries);
		nbt.putInt("scrap", this.scrap);
		nbt.putDouble("lastEnergy", this.lastEnergy);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide)
		{
			this.setUpgradestat();
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.redstonePowered = false;
		boolean needsInvUpdate = false;
		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		if (!this.redstone.hasRedstoneInput() && !(this.energy.getEnergy() <= 0.0))
		{
			boolean playSubSound = false;
			if (this.scrap > 0)
			{
				double bonus = Math.min(this.scrap, this.energy.getEnergy() - this.lastEnergy);
				if (bonus > 0.0)
				{
					this.energy.forceAddEnergy(5.0 * bonus);
					this.scrap -= (int) bonus;
				}

				playSubSound = true;
			}

			this.activate(playSubSound);
			if (this.getActive())
			{
				if (playSubSound && this.subLoopingSound != null && !this.subLoopingSound.isPlaying())
				{
					this.subLoopingSound.play();
				} else if (!playSubSound && this.subLoopingSound != null && this.subLoopingSound.isPlaying())
				{
					this.subLoopingSound.stop();
				}
			}

			if (this.scrap < 10000)
			{
				MachineRecipeResult<IRecipeInput, Integer, ItemStack> recipe = this.amplifierSlot.process();
				if (recipe != null)
				{
					this.amplifierSlot.consume(recipe);
					this.scrap = this.scrap + recipe.getOutput();
				}
			}

			if (this.isReadyToGenerate())
			{
				needsInvUpdate = this.attemptGeneration();
			}

			needsInvUpdate |= this.containerSlot.processFromTank(this.fluidTank, this.outputSlot);
			this.lastEnergy = this.energy.getEnergy();
			if (needsInvUpdate)
			{
				this.setChanged();
			}
		} else
		{
			this.setState();
			this.setActive(false);
		}
	}

	/**
	 * True when the buffer is full enough to produce 1 mB of UU-Matter.
	 * Under the GT energy net, transfer is quantized to whole voltage packets, so free space
	 * smaller than one packet can never be filled — treat that remainder as full.
	 */
	private boolean isReadyToGenerate()
	{
		if (this.energy.getEnergy() >= this.energy.getCapacity())
		{
			return true;
		}

		double free = this.energy.getFreeEnergy();
		if (free <= 0.0)
		{
			return this.energy.getEnergy() > 0.0;
		}

		int packetVoltage = this.energy.getSinkWorkingVoltage().getVoltage();
		return packetVoltage > 0 && free < packetVoltage && this.energy.getEnergy() > 0.0;
	}

	public boolean attemptGeneration()
	{
		if (this.fluidTank.getFluidAmount() + 1 > this.fluidTank.getCapacity())
		{
			return false;
		}

		if (!this.isReadyToGenerate())
		{
			return false;
		}

		this.fluidTank.fillMbUnchecked(Ic2rFluidStack.create(Ic2rFluids.UU_MATTER.still(), 1), false);
		// Drain up to capacity; if only a near-full remainder is present, drain all of it.
		this.energy.useEnergy(this.energy.getCapacity(), false);
		return true;
	}

	public String getProgressAsString()
	{
		if (this.isReadyToGenerate())
		{
			return "100%";
		}

		int p = (int) Math.min(100.0 * this.energy.getFillRatio(), 100.0);
		return p + "%";
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerMatter(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerMatter(syncId, inventory, this);
	}

	private void setState()
	{
		if (this.prevState != 0)
		{
			IC2R.network.get(true).updateTileEntityField(this, "state");
		}

		this.prevState = 0;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = new ArrayList<>();
		ret.add("state");
		ret.addAll(super.getNetworkedFields());
		return ret;
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if (IC2R.sideProxy.isSimulating())
		{
			this.setUpgradestat();
		}
	}

	public void setUpgradestat()
	{
		this.upgradeSlot.onChanged();
		this.energy.setSinkTier(applyModifier(getDefaultTier(), this.upgradeSlot.extraTier));
		// Capacity is EU per mB UU-Matter, not continuous EU/t. Keep sink-tier voltage and allow
		// high amperage so GT packet transfer can charge the buffer quickly.
		this.energy.configureEnergyBuffer(64);
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
		return EnumSet.of(
			UpgradableProperty.RedstoneSensitive,
			UpgradableProperty.Transformer,
			UpgradableProperty.ItemConsuming,
			UpgradableProperty.ItemProducing,
			UpgradableProperty.FluidProducing
		);
	}

	@Override
	public boolean shouldExplode()
	{
		return true;
	}

	@Override
	public float getExplosionPower(int tier, float defaultPower)
	{
		return 15.0F;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2rSoundEvents.MACHINE_MATTER_GENERATOR_LOOP.get();
	}

	@Override
	public SoundEvent getSubLoopingSoundEvent()
	{
		return Ic2rSoundEvents.MACHINE_MATTER_GENERATOR_SCRAP.get();
	}
}
