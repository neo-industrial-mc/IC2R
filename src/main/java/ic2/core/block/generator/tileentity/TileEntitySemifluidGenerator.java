package ic2.core.block.generator.tileentity;

import ic2.api.recipe.ISemiFluidFuelManager;
import ic2.api.recipe.Recipes;
import ic2.core.SemiFluidFuelManager;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.init.IC2Config;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.ModConfigSpec;

@NotClassic
public class TileEntitySemifluidGenerator extends TileEntityBaseGenerator {
  /** Base EU/t while any registered fuel is burning (scaled by per-fuel config multiplier). */
  public static final int BASE_PRODUCTION_EU = 32;

  public final InvSlotConsumableLiquid fluidSlot;
  public final InvSlotOutput outputSlot;
  @GuiSynced protected final Ic2FluidTank fluidTank;
  protected final Fluids fluids = this.addComponent(new Fluids(this));

  public TileEntitySemifluidGenerator(BlockPos pos, BlockState state) {
    super(Ic2BlockEntities.SEMIFLUID_GENERATOR, pos, state, BASE_PRODUCTION_EU, 1, 32000);
    this.fluidTank =
        this.fluids.addTankInsert(
            "fluid", 10000, Fluids.fluidPredicate(Recipes.semiFluidGenerator));
    this.fluidSlot =
        new InvSlotConsumableLiquidByManager(this, "fluidSlot", 1, Recipes.semiFluidGenerator);
    this.outputSlot = new InvSlotOutput(this, "output", 1);
  }

  public static void init() {
    Recipes.semiFluidGenerator = new SemiFluidFuelManager();
    var gen = IC2Config.balance.energy.generator;
    // amount = mB drained per fuel cycle (= burn ticks); power = EU/t (base 32 × config)
    registerFuel(Ic2Fluids.BIOGAS.still(), 20, gen.semiFluidBiogas);
    registerFuel(Ic2Fluids.BIOMASS.still(), 40, gen.semiFluidBiomass);
    registerFuel(Ic2Fluids.HYDROGEN.still(), 4, gen.semiFluidHydrogen);
    registerFuel(Ic2Fluids.CREOSOTE.still(), 8, gen.semiFluidCreosote);
  }

  private static void registerFuel(Fluid fluid, int amountMb, ModConfigSpec.DoubleValue multiplier) {
    float mult = multiplier.get().floatValue();
    if (mult > 0.0F) {
      addFuel(fluid, amountMb, Math.round(BASE_PRODUCTION_EU * mult));
    }
  }

  public static void addFuel(Fluid fluid, int amount, int eu) {
    Recipes.semiFluidGenerator.addFluid(fluid, amount, eu);
  }

  @Override
  public void updateEntityServer() {
    super.updateEntityServer();
    if (this.fluidSlot.processIntoTank(this.fluidTank, this.outputSlot)) {
      this.setChanged();
    }
  }

  @Override
  public boolean gainFuel() {
    boolean dirty = false;
    Ic2FluidStack ret = this.fluidTank.drainMbUnchecked(Integer.MAX_VALUE, true);
    if (ret != null) {
      ISemiFluidFuelManager.BurnProperty property =
          Recipes.semiFluidGenerator.getBurnProperty(ret.getFluid());
      if (property != null && ret.getAmountMb() >= property.amount()) {
        this.fluidTank.drainMbUnchecked(property.amount(), false);
        this.production = property.power();
        this.fuel = this.fuel + property.amount();
        dirty = true;
      }
    }

    return dirty;
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return Ic2SoundEvents.GENERATOR_GEOTHERMAL_LOOP;
  }
}
