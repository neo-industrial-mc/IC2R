package me.halfcooler.ic2r.forge;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import me.halfcooler.ic2r.core.block.misc.AirBlock;
import me.halfcooler.ic2r.core.block.misc.ConstructionFoamBlock;
import me.halfcooler.ic2r.core.block.misc.HotCoolantBlock;
import me.halfcooler.ic2r.core.block.misc.HotWaterBlock;
import me.halfcooler.ic2r.core.block.misc.HydrogenBlock;
import me.halfcooler.ic2r.core.block.misc.PahoehoeLavaBlock;
import me.halfcooler.ic2r.core.block.misc.SteamBlock;
import me.halfcooler.ic2r.core.block.misc.UUMatterBlock;
import me.halfcooler.ic2r.core.fluid.EnvFluidHandler;
import me.halfcooler.ic2r.core.fluid.FluidHandler;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.forge.fluid.Ic2rFlowingFluid;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.mutable.Mutable;
import org.joml.Vector3f;

class EnvFluidHandlerForge implements EnvFluidHandler {

    static final DeferredRegister<Fluid> fluidRegistry = DeferredRegister.create(Registries.FLUID, "ic2r");

    static final DeferredRegister<FluidType> fluidTypeRegistry = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, "ic2r");

    private static final java.util.List<Runnable> pendingFluidTypeRegistrations = new java.util.ArrayList<>();

    private static final java.util.List<Runnable> pendingFluidRegistrations = new java.util.ArrayList<>();

    private static IFluidHandlerItem getFluidHandler(ItemStack stack) {
        // NeoForge 1.21: ItemCapability returns nullable, not LazyOptional.
        return stack.getCapability(Capabilities.FluidHandler.ITEM);
    }

    private static void updateResultStack(Mutable<ItemStack> out, IFluidHandlerItem handler) {
        if (out != null) {
            assert out.getValue() != null;
            ItemStack container = handler.getContainer();
            out.setValue(container);
        }
    }

    private static IFluidHandler getFluidHandler(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side) {
        if (be == null) {
            if (state.hasBlockEntity()) {
                be = world.getBlockEntity(pos);
            }
        }
        // NeoForge 1.21: query via Level#getCapability, not BlockEntity#getCapability.
        return world.getCapability(Capabilities.FluidHandler.BLOCK, pos, state, be, side);
    }

    private static IFluidHandler.FluidAction getAction(boolean simulate) {
        return simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
    }

    static FluidStack getForgeFs(Ic2rFluidStack fs) {
        if (fs == null || fs.isEmpty()) {
            return FluidStack.EMPTY;
        } else {
            return fs instanceof Ic2rFluidStackImpl ? ((Ic2rFluidStackImpl) fs).parent() : new FluidStack(fs.getFluid(), fs.getAmountMb());
        }
    }

    static void registerPendingFluidTypes() {
        for (Runnable r : pendingFluidTypeRegistrations) {
            r.run();
        }
        pendingFluidTypeRegistrations.clear();
    }

    static void registerPendingFluids() {
        for (Runnable r : pendingFluidRegistrations) {
            r.run();
        }
        pendingFluidRegistrations.clear();
    }

    private static LiquidBlock createFluidBlock(String fluidName, FlowingFluid fluid, Block.Properties properties) {
        return switch(fluidName) {
            case "hot_coolant" ->
                new HotCoolantBlock(fluid, properties);
            case "air" ->
                new AirBlock(fluid, properties);
            case "hydrogen" ->
                new HydrogenBlock(fluid, properties);
            case "hot_water" ->
                new HotWaterBlock(fluid, properties);
            case "uu_matter" ->
                new UUMatterBlock(fluid, properties);
            case "construction_foam" ->
                new ConstructionFoamBlock(fluid, properties);
            case "steam", "superheated_steam" ->
                new SteamBlock(fluid, properties);
            case "pahoehoe_lava" ->
                new PahoehoeLavaBlock(fluid, properties);
            default ->
                new LiquidBlock(fluid, properties);
        };
    }

    @Override
    public EnvFluidHandler.FluidRefs createFluid(ResourceLocation id, int density, int viscosity, int luminosity, int temperature, ResourceLocation stillSpriteId, ResourceLocation flowingSpriteId, int color) {
        EnvFluidHandler.FluidRefs ret = new EnvFluidHandler.FluidRefs(null, null, null, null);
        AtomicReference<FluidType> fluidTypeRef = new AtomicReference<>();
        pendingFluidTypeRegistrations.add(() -> {
            FluidType.Properties attributesBuilder = FluidType.Properties.create().density(density).viscosity(viscosity).lightLevel(luminosity).temperature(temperature);
            FluidType fluidType = new FluidType(attributesBuilder) {

                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    // Custom fluids are FogType.NONE for vanilla, so without these hooks the clear
                    // color stays sky-colored and interior faces cull → "透视天空".
                    consumer.accept(new IClientFluidTypeExtensions() {

                        @Override
                        public int getTintColor() {
                            return color;
                        }

                        @Override
                        public ResourceLocation getStillTexture() {
                            return stillSpriteId;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return flowingSpriteId != null ? flowingSpriteId : stillSpriteId;
                        }

                        @Override
                        public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                            float[] rgb = FluidHandler.fogRgb(color);
                            return new Vector3f(rgb[0], rgb[1], rgb[2]);
                        }

                        @Override
                        public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                            // Default is no-op; set planes like water (start -8) with density-scaled end.
                            float fogEnd = FluidHandler.fogEndForDensity(density);
                            RenderSystem.setShaderFogStart(-8.0F);
                            RenderSystem.setShaderFogEnd(fogEnd);
                            RenderSystem.setShaderFogShape(FogShape.SPHERE);
                        }
                    });
                }
            };
            Registry.register(NeoForgeRegistries.FLUID_TYPES, id, fluidType);
            fluidTypeRef.set(fluidType);
        });
        AtomicReference<LiquidBlock> fluidBlockRef = new AtomicReference<>();
        pendingFluidRegistrations.add(() -> {
            // Ic2rFlowingFluid refuses foreign displacement (BaseFlowingFluid would let water
            // above overwrite these sources in oceans).
            BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(fluidTypeRef::get, ret::still, ret::flowing).bucket(ret::bucket);
            properties.block(fluidBlockRef::get);
            Fluid still = new Ic2rFlowingFluid.Source(properties);
            Fluid flowing = new Ic2rFlowingFluid.Flowing(properties);
            Registry.register(BuiltInRegistries.FLUID, id, still);
            ret.still(still);
            ret.flowing(flowing);
            Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "flowing_" + id.getPath()), flowing);
            Block.Properties fluidBlockProperties = BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable().noCollission().randomTicks().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
            LiquidBlock fluidBlock = createFluidBlock(id.getPath(), (FlowingFluid) ret.still(), fluidBlockProperties);
            Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "fluid_block_" + id.getPath()), fluidBlock);
            fluidBlockRef.set(fluidBlock);
        });
        ResourceLocation bucketId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_bucket");
        EnvProxyForge.pendingItemRegistrations.add(() -> {
            BucketItem bucket = new BucketItem(ret.still(), new Properties().craftRemainder(Items.BUCKET).stacksTo(1));
            Registry.register(BuiltInRegistries.ITEM, bucketId, bucket);
            ret.bucket(bucket);
        });
        return ret;
    }

    @Override
    public Collection<Fluid> getAllFluids() {
        return BuiltInRegistries.FLUID.stream().toList();
    }

    @Override
    public int getDensity(Fluid fluid) {
        return fluid.getFluidType().getDensity();
    }

    @Override
    public int getTemperature(Fluid fluid) {
        return fluid.getFluidType().getTemperature();
    }

    @Override
    public boolean isGaseous(Fluid fluid) {
        return fluid.getFluidType().isLighterThanAir();
    }

    @Override
    public ResourceLocation getStillSpriteId(Fluid fluid) {
        throw new UnsupportedOperationException("client only");
    }

    @Override
    public ResourceLocation getFlowingSpriteId(Fluid fluid) {
        throw new UnsupportedOperationException("client only");
    }

    @Override
    public int getColor(Fluid fluid) {
        throw new UnsupportedOperationException("client only");
    }

    @Override
    public Ic2rFluidStack createFluidStackMb(Fluid fluid, int amount, CompoundTag nbt) {
        // NeoForge 1.21 FluidStack no longer takes CompoundTag; NBT was replaced by data components.
        // Domain layer does not keep fluid tags, so ignore nbt here.
        return new Ic2rFluidStackImpl(new FluidStack(fluid, amount));
    }

    @Override
    public Ic2rFluidStack getFluidStack(ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return null;
        }
        if (stack.getCount() != 1) {
            stack = StackUtil.copyWithSize(stack, 1);
        }
        IFluidHandlerItem handler = getFluidHandler(stack);
        if (handler == null) {
            return null;
        }
        FluidStack fs;
        if (handler.getTanks() <= 0 || (fs = handler.getFluidInTank(0)) == null) {
            fs = handler.drain(Integer.MAX_VALUE, getAction(true));
        }
        return !fs.isEmpty() ? new Ic2rFluidStackImpl(fs) : Ic2rFluidStack.EMPTY;
    }

    @Override
    public Ic2rFluidStack[] getFluidStacks(ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return null;
        }
        if (stack.getCount() != 1) {
            stack = StackUtil.copyWithSize(stack, 1);
        }
        IFluidHandlerItem handler = getFluidHandler(stack);
        if (handler == null) {
            return null;
        }
        int tanks = handler.getTanks();
        Ic2rFluidStack[] ret;
        if (tanks > 0) {
            ret = new Ic2rFluidStack[tanks];
            int writeIdx = 0;
            for (int i = 0; i < tanks; i++) {
                FluidStack fs = handler.getFluidInTank(i);
                ret[writeIdx++] = !fs.isEmpty() ? new Ic2rFluidStackImpl(fs) : Ic2rFluidStack.EMPTY;
            }
            if (writeIdx < ret.length) {
                ret = Arrays.copyOf(ret, writeIdx);
            }
            return ret;
        }
        FluidStack fs = handler.drain(Integer.MAX_VALUE, getAction(true));
        ret = new Ic2rFluidStack[1];
        ret[0] = !fs.isEmpty() ? new Ic2rFluidStackImpl(fs) : Ic2rFluidStack.EMPTY;
        return ret;
    }

    @Override
    public Ic2rFluidStack readFluidStack(CompoundTag nbt) {
        // Legacy Forge fluid NBT: FluidName + Amount (+ optional Tag). Components path not used by domain yet.
        String id = nbt.contains("FluidName") ? nbt.getString("FluidName") : nbt.getString("id");
        int amount = nbt.contains("Amount") ? nbt.getInt("Amount") : nbt.getInt("amount");
        if (id == null || id.isEmpty() || amount < 0) {
            return null;
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(id));
        return fluid != null ? Ic2rFluidStack.create(fluid, amount) : null;
    }

    @Override
    public CompoundTag getFluidStackNbt(Ic2rFluidStack fs) {
        // Fluid NBT tags were replaced by data components; IC2R paths no longer rely on fluid tags.
        return null;
    }

    @Override
    public Ic2rFluidStack drainMb(ItemStack stack, int amount, boolean simulate, Mutable<ItemStack> newStack) {
        if (newStack != null) {
            newStack.setValue(stack);
        }
        if (amount < 0) {
            throw new IllegalArgumentException("negative amount");
        } else if (amount == 0) {
            return Ic2rFluidStack.EMPTY;
        } else {
            if (stack.getCount() != 1) {
                stack = StackUtil.copyWithSize(stack, 1);
            }
            IFluidHandlerItem handler = getFluidHandler(stack);
            if (handler == null) {
                return Ic2rFluidStack.EMPTY;
            }
            FluidStack drained = handler.drain(amount, getAction(simulate));
            if (!drained.isEmpty()) {
                updateResultStack(newStack, handler);
                return new Ic2rFluidStackImpl(drained);
            } else {
                return Ic2rFluidStack.EMPTY;
            }
        }
    }

    @Override
    public int drainMb(ItemStack stack, Ic2rFluidStack drainFs, boolean simulate, Mutable<ItemStack> newStack) {
        if (newStack != null) {
            newStack.setValue(stack);
        }
        if (drainFs == null) {
            throw new IllegalArgumentException("invalid drain medium");
        } else if (drainFs.isEmpty()) {
            return 0;
        } else {
            if (stack.getCount() != 1) {
                stack = StackUtil.copyWithSize(stack, 1);
            }
            IFluidHandlerItem handler = getFluidHandler(stack);
            if (handler == null) {
                return 0;
            }
            FluidStack drained = handler.drain(getForgeFs(drainFs), getAction(simulate));
            if (!drained.isEmpty()) {
                updateResultStack(newStack, handler);
                return drained.getAmount();
            } else {
                return 0;
            }
        }
    }

    @Override
    public int fillMb(ItemStack stack, Ic2rFluidStack fillFs, boolean simulate, Mutable<ItemStack> newStack) {
        if (newStack != null) {
            newStack.setValue(stack);
        }
        if (fillFs == null) {
            throw new IllegalArgumentException("invalid fill medium");
        }
        if (fillFs.isEmpty()) {
            return 0;
        }
        if (stack.getCount() != 1) {
            stack = StackUtil.copyWithSize(stack, 1);
        }
        IFluidHandlerItem handler = getFluidHandler(stack);
        if (handler == null) {
            return 0;
        }
        FluidStack fillMedium = getForgeFs(fillFs);
        int ret = handler.fill(fillMedium, getAction(simulate));
        if (ret <= 0) {
            return 0;
        }
        updateResultStack(newStack, handler);
        return ret;
    }

    @Override
    public boolean isFluidBlock(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side) {
        return getFluidHandler(state, world, pos, be, side) != null;
    }

    @Override
    public Ic2rFluidStack drainMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, int amount, boolean simulate) {
        if (amount < 0) {
            throw new IllegalArgumentException("negative amount");
        }
        if (amount == 0) {
            return Ic2rFluidStack.EMPTY;
        }
        IFluidHandler handler = getFluidHandler(state, world, pos, be, side);
        if (handler == null) {
            return null;
        }
        FluidStack drained = handler.drain(amount, getAction(simulate));
        return !drained.isEmpty() ? new Ic2rFluidStackImpl(drained) : Ic2rFluidStack.EMPTY;
    }

    @Override
    public int drainMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, Ic2rFluidStack drainFs, boolean simulate) {
        if (drainFs == null) {
            throw new IllegalArgumentException("invalid drain medium");
        }
        if (drainFs.isEmpty()) {
            return 0;
        }
        if (!state.hasBlockEntity()) {
            return 0;
        }
        IFluidHandler handler = getFluidHandler(state, world, pos, be, side);
        if (handler == null) {
            return 0;
        }
        FluidStack drained = handler.drain(getForgeFs(drainFs), getAction(simulate));
        return !drained.isEmpty() ? drained.getAmount() : 0;
    }

    @Override
    public int fillMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, Ic2rFluidStack fillFs, boolean simulate) {
        if (fillFs == null) {
            throw new IllegalArgumentException("invalid fill medium");
        }
        if (fillFs.isEmpty()) {
            return 0;
        }
        if (!state.hasBlockEntity()) {
            return 0;
        }
        IFluidHandler handler = getFluidHandler(state, world, pos, be, side);
        if (handler == null) {
            return 0;
        }
        FluidStack fillMedium = getForgeFs(fillFs);
        int ret = handler.fill(fillMedium, getAction(simulate));
        return Math.max(ret, 0);
    }

    @Override
    public Fluid getWorldFluid(BlockState state, Level world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof LiquidBlock) {
            return state.getFluidState().getType();
        }
        return null;
    }

    @Override
    public int getWorldFluidLevel(BlockState state, Level world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof LiquidBlock) {
            return state.getValue(LiquidBlock.LEVEL);
        }
        return -1;
    }

    @Override
    public Ic2rFluidStack drainWorldFluid(BlockState state, Level world, BlockPos pos, boolean simulate) {
        Block block = state.getBlock();
        if (block instanceof LiquidBlock) {
            FluidState fluidState = state.getFluidState();
            if (!fluidState.isSource()) {
                return null;
            }
            Fluid fluid = fluidState.getType();
            if (!simulate) {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
            return Ic2rFluidStack.create(fluid, 1000);
        }
        return null;
    }
}
