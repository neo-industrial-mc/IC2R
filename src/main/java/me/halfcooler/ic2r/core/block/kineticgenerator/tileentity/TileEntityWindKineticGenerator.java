package me.halfcooler.ic2r.core.block.kineticgenerator.tileentity;

import me.halfcooler.ic2r.api.item.IKineticRotor;
import me.halfcooler.ic2r.api.tile.IRotorProvider;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableClass;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableKineticRotor;
import me.halfcooler.ic2r.core.block.kineticgenerator.container.ContainerWindKineticGenerator;
import me.halfcooler.ic2r.core.event.WorldData;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@NotClassic
public class TileEntityWindKineticGenerator extends TileEntityAbstractKineticGenerator implements IRotorProvider, IHasGui
{
	public static final float outputModifier = 10.0F * IC2RConfig.balance.energy.kineticGenerator.wind.get().floatValue();
	private static final ResourceLocation woodenRotorTexture = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/item/rotor/wood_rotor_model.png");
	public final InvSlotConsumableClass rotorSlot;
	private double windStrength;
	private int obstructedCrossSection;
	private int crossSection;
	private float rotationSpeed;
	private float angle = 0.0F;

	public TileEntityWindKineticGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.WIND_KINETIC_GENERATOR, pos, state);
		this.updateTicker = IC2R.random.nextInt(this.getTickRate());
		this.rotorSlot = new InvSlotConsumableKineticRotor(this, "rotorslot", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, IKineticRotor.GearboxType.WIND, "rotorSlot");
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.rotationSpeed = nbt.getFloat("rotationSpeed");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putFloat("rotationSpeed", this.rotationSpeed);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		// Re-sync animation fields so clients that load the TE after a world re-entry
		// (or chunk re-watch) receive the current speed even if it does not change again.
		if (this.getLevel() != null && !this.getLevel().isClientSide)
		{
			IC2R.network.get(true).updateTileEntityField(this, "rotationSpeed");
			IC2R.network.get(true).updateTileEntityField(this, "rotorSlot");
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.updateTicker++ % this.getTickRate() == 0)
		{
			boolean needsInvUpdate = false;
			boolean nextActive = this.hasRotor() && this.rotorHasSpace();

			if (nextActive != this.getActive())
			{
				needsInvUpdate = true;
			}

			if (nextActive)
			{
				this.crossSection = Util.square(this.getRotorDiameter() / 2 * 2 * 2 + 1);
				this.obstructedCrossSection = this.checkSpace(this.getRotorDiameter() * 3, false);
				if (this.obstructedCrossSection > 0 && this.obstructedCrossSection <= (this.getRotorDiameter() + 1) / 2)
				{
					this.obstructedCrossSection = 0;
				}

				if (this.obstructedCrossSection < 0)
				{
					this.windStrength = 0.0;
					this.setRotationSpeed(0.0F);
				} else
				{
					this.windStrength = this.calcWindStrength();
					float speed = (float) Util.limit((this.windStrength - this.getMinWindStrength()) / this.getMaxWindStrength(), 0.0, 2.0);
					this.setRotationSpeed(speed);
					if (this.windStrength >= this.getMinWindStrength())
					{
						if (this.windStrength <= this.getMaxWindStrength())
						{
							this.rotorSlot.damage(1, false);
						} else
						{
							this.rotorSlot.damage(4, false);
						}

						needsInvUpdate = true;
					}
				}
			} else
			{
				this.windStrength = 0.0;
				this.setRotationSpeed(0.0F);
			}

			this.setActive(nextActive);

			if (needsInvUpdate)
			{
				this.setChanged();
			}
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("rotationSpeed");
		ret.add("rotorSlot");
		return ret;
	}

	@Override
	public ContainerBase<TileEntityWindKineticGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerWindKineticGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerWindKineticGenerator(syncId, inventory, this);
	}

	public boolean facingMatchesDirection(Direction direction)
	{
		return direction == this.getFacing();
	}

	public String getRotorHealth()
	{
		return !this.rotorSlot.isEmpty() ? Component.translatable("ic2r.wind_kinetic_generator.gui.rotorhealth", (int) (100.0F - (float) this.rotorSlot.get().getDamageValue() / this.rotorSlot.get().getMaxDamage() * 100.0F), "%").getString() : "";
	}

	@Override
	public int getConnectionBandwidth(Direction side)
	{
		return this.facingMatchesDirection(side.getOpposite()) ? this.getKuOutput() : 0;
	}

	@Override
	public int drawKineticEnergy(Direction side, int request, boolean simulate)
	{
		return this.facingMatchesDirection(side.getOpposite()) ? Math.min(request, this.getKuOutput()) : 0;
	}

	public int checkSpace(int length, boolean onlyRotor)
	{
		int box = this.getRotorDiameter() / 2;
		int lentemp = 0;
		if (onlyRotor)
		{
			length = 1;
			lentemp = length + 1;
		}

		if (!onlyRotor)
		{
			box *= 2;
		}

		Direction fwdDir = this.getFacing();
		Direction rightDir = fwdDir.getClockWise(Axis.Y);
		int xMaxDist = Math.abs(length * fwdDir.getStepX() + box * rightDir.getStepX());
		int zMaxDist = Math.abs(length * fwdDir.getStepZ() + box * rightDir.getStepZ());
		PathNavigationRegion chunkCache = new PathNavigationRegion(this.getLevel(), this.worldPosition.offset(-xMaxDist, -box, -zMaxDist), this.worldPosition.offset(xMaxDist, box, zMaxDist));
		int ret = 0;
		int xCord = this.worldPosition.getX();
		int yCord = this.worldPosition.getY();
		int zCord = this.worldPosition.getZ();
		MutableBlockPos pos = new MutableBlockPos();

		for (int up = -box; up <= box; up++)
		{
			int y = yCord + up;

			for (int right = -box; right <= box; right++)
			{
				boolean occupied = false;

				for (int fwd = lentemp - length; fwd <= length; fwd++)
				{
					int x = xCord + fwd * fwdDir.getStepX() + right * rightDir.getStepX();
					int z = zCord + fwd * fwdDir.getStepZ() + right * rightDir.getStepZ();
					pos.set(x, y, z);
					assert Math.abs(x - xCord) <= xMaxDist;
					assert Math.abs(z - zCord) <= zMaxDist;
					BlockState state = chunkCache.getBlockState(pos);
					if (!state.isAir())
					{
						occupied = true;
						if ((up != 0 || right != 0 || fwd != 0) && chunkCache.getBlockEntity(pos) instanceof TileEntityWindKineticGenerator && !onlyRotor)
						{
							return -1;
						}
					}
				}

				if (occupied)
				{
					ret++;
				}
			}
		}

		return ret;
	}

	public boolean hasRotor()
	{
		return !this.rotorSlot.isEmpty();
	}

	public boolean rotorHasSpace()
	{
		return this.checkSpace(1, true) == 0;
	}

	private void setRotationSpeed(float speed)
	{
		if (this.rotationSpeed != speed)
		{
			this.rotationSpeed = speed;
			IC2R.network.get(true).updateTileEntityField(this, "rotationSpeed");
		}
	}

	public int getTickRate()
	{
		return 32;
	}

	public double calcWindStrength()
	{
		double windStr = WorldData.get(this.getLevel()).windSim.getWindAt(this.worldPosition.getY());
		windStr *= 1.0 - Math.pow((double) this.obstructedCrossSection / this.crossSection, 2.0);
		return Math.max(0.0, windStr);
	}

	@Override
	public float getRotorAnimationSpeed()
	{
		return this.rotationSpeed;
	}

	@Override
	public float getAngle()
	{
		return this.angle;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		if (this.rotationSpeed != 0.0F)
		{
			this.angle = (this.angle + this.rotationSpeed * 50.0F) % 360.0F;
		}
	}

	public float getEfficiency()
	{
		ItemStack stack = this.rotorSlot.get();
		return !StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor ? ((IKineticRotor) stack.getItem()).getEfficiency(stack) : 0.0F;
	}

	public int getMinWindStrength()
	{
		ItemStack stack = this.rotorSlot.get();
		return !StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor ? ((IKineticRotor) stack.getItem()).getMinWindStrength(stack) : 0;
	}

	public int getMaxWindStrength()
	{
		ItemStack stack = this.rotorSlot.get();
		return !StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor ? ((IKineticRotor) stack.getItem()).getMaxWindStrength(stack) : 0;
	}

	@Override
	public int getRotorDiameter()
	{
		ItemStack stack = this.rotorSlot.get();
		return !StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor ? ((IKineticRotor) stack.getItem()).getDiameter(stack) : 0;
	}

	@Override
	public ResourceLocation getRotorRenderTexture()
	{
		ItemStack stack = this.rotorSlot.get();
		return !StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor ? ((IKineticRotor) stack.getItem()).getRotorRenderTexture(stack) : woodenRotorTexture;
	}

	public boolean isRotorOverloaded()
	{
		return this.hasRotor() && this.rotorHasSpace() && this.isWindStrongEnough() && this.windStrength > this.getMaxWindStrength();
	}

	public boolean isWindStrongEnough()
	{
		return this.windStrength >= this.getMinWindStrength();
	}

	public int getKuOutput()
	{
		return this.windStrength >= this.getMinWindStrength() ? (int) (this.windStrength * outputModifier * this.getEfficiency()) : 0;
	}

	public int getObstructions()
	{
		return this.obstructedCrossSection;
	}

	@Override
	public void setActive(boolean active)
	{
		if (active != this.getActive())
		{
			IC2R.network.get(true).updateTileEntityField(this, "rotorSlot");
		}

		super.setActive(active);
	}
}
