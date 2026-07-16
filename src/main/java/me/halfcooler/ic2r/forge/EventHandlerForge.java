package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.event.EnergyTileLoadEvent;
import me.halfcooler.ic2r.api.energy.event.EnergyTileUnloadEvent;
import me.halfcooler.ic2r.api.event.RetextureEvent;
import me.halfcooler.ic2r.api.tile.RetexturableBlock;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.forge.block.tileentity.TileEntityInventoryCap;
import me.halfcooler.ic2r.core.command.CommandIc2r;
import me.halfcooler.ic2r.core.event.EventHandler;
import me.halfcooler.ic2r.core.event.TickHandler;
import me.halfcooler.ic2r.core.item.armor.jetpack.JetpackHandler;
import me.halfcooler.ic2r.core.fluid.FluidBeBridge;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidBlock;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidItem;
import me.halfcooler.ic2r.core.item.tool.AbstractItemNanoSaber;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.NotNull;

public final class EventHandlerForge {

    /**
     * Pre-20.1.40 registry namespace. Worlds still store {@code ic2:*} ids until remapped.
     */
    private static final String LEGACY_NAMESPACE = "ic2";

    private static final String CURRENT_NAMESPACE = "ic2r";

    private static final ResourceLocation fluidCapId = IC2R.getIdentifier("fluid");

    private static final ResourceLocation itemCapId = IC2R.getIdentifier("item");

    private static final ResourceLocation nanoSaberCapId = IC2R.getIdentifier("nano_saber_state");

    @SubscribeEvent
    public void serverStart(ServerStartingEvent event) {
        EventHandler.onServerStart(event.getServer());
    }

    /**
     * World save migration for registry renames.
     * <ul>
     *   <li>{@code ic2:*} → {@code ic2r:*} (same path) for every Forge registry that fires this event</li>
     *   <li>{@code ic2r:empty_cell} → {@link Ic2rItems#FACADE_CELL} (earlier internal rename)</li>
     * </ul>
     * Fired once per registry that has missing entries (Forge 1.20.1).
     */
    @SubscribeEvent
    public void onMissingMappings(MissingMappingsEvent event) {
        remapLegacyNamespace(event);
        for (MissingMappingsEvent.Mapping<Item> mapping : event.getMappings(BuiltInRegistries.ITEM.key(), CURRENT_NAMESPACE)) {
            if ("empty_cell".equals(mapping.getKey().getPath())) {
                mapping.remap(Ic2rItems.FACADE_CELL);
            }
        }
    }

    /**
     * Remap every missing {@code ic2:path} entry to {@code ic2r:path} when the new id is registered.
     */
    @SuppressWarnings("unchecked")
    private static <T> void remapLegacyNamespace(MissingMappingsEvent event) {
        ResourceKey<? extends Registry<T>> registryKey = (ResourceKey<? extends Registry<T>>) event.getKey();
        Registry<T> registry = (Registry<T>) event.getRegistry();
        for (MissingMappingsEvent.Mapping<T> mapping : event.getMappings(registryKey, LEGACY_NAMESPACE)) {
            ResourceLocation newId = ResourceLocation.fromNamespaceAndPath(CURRENT_NAMESPACE, mapping.getKey().getPath());
            if (!registry.containsKey(newId)) {
                continue;
            }
            T replacement = registry.getValue(newId);
            if (replacement != null) {
                mapping.remap(replacement);
            }
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandIc2r.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EventHandler.onPlayerLogin(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        EventHandler.onPlayerLogout(event.getEntity());
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {
        EventHandler.onWorldLoad((Level) event.getLevel());
    }

    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload event) {
        EventHandler.onWorldUnload((Level) event.getLevel());
    }

    @SubscribeEvent
    public void onChunkDataLoad(ChunkDataEvent.Load event) {
        ChunkAccess chunk = event.getChunk();
        if (chunk instanceof LevelChunk) {
            EventHandler.onChunkDataLoad((LevelChunk) chunk, event.getData());
        }
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save event) {
        ChunkAccess chunk = event.getChunk();
        if (chunk instanceof LevelChunk) {
            EventHandler.onChunkSave((LevelChunk) chunk, event.getData());
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        ChunkAccess chunk = event.getChunk();
        if (chunk instanceof LevelChunk) {
            EventHandler.onChunkLoad((LevelChunk) chunk);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkEvent.Unload event) {
        ChunkAccess chunk = event.getChunk();
        if (chunk instanceof LevelChunk) {
            EventHandler.onChunkUnload((LevelChunk) chunk);
        }
    }

    @SubscribeEvent
    public void onWorldTick(LevelTickEvent.Post event) {
        Level world = event.getLevel();
        if (event.phase == TickEvent.Phase.START) {
            TickHandler.onWorldTickStart(world);
        } else {
            TickHandler.onWorldTickEnd(world);
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (event.phase == TickEvent.Phase.START) {
            TickHandler.onServerTick();
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.phase == TickEvent.Phase.START) {
            EventHandler.onPlayerTickStart(event.getEntity());
        } else {
            EventHandler.onPlayerTick(event.getEntity());
        }
    }

    @SubscribeEvent
    public void onLivingSpecialSpawn(FinalizeSpawnEvent event) {
        EventHandler.onLivingSpecialSpawn(event.getEntity());
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player && event.getSource() == player.level().damageSources().fall() && JetpackHandler.hasJetpack(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST))) {
            IC2R.grantAdvancement(player, "ic2r/build_generator/build_batbox/build_jetpack/fall_with_jetpack");
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingFall(LivingFallEvent event) {
        if (EventHandler.onLivingFall(event.getEntity(), event.getDistance())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (EventHandler.onEntitySwingHand(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (EventHandler.onEntitySwingHand(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (EventHandler.onEntityInteract(event.getEntity(), event.getHand(), event.getTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityAttacked(LivingDamageEvent event) {
        float remaining = EventHandler.onEntityAttacked(event.getEntity(), event.getSource(), event.getAmount());
        if (remaining <= 0.0F) {
            event.setCanceled(true);
        } else {
            event.setAmount(remaining);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onAttackEntity(AttackEntityEvent event) {
        if (!EventHandler.onAttackEntity(event.getEntity(), event.getTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockStartBreak(PlayerInteractEvent.LeftClickBlock event) {
        if (EventHandler.onBlockStartBreak(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace()) == InteractionResult.FAIL) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void beforeBlockBreak(BlockEvent.BreakEvent event) {
        Level world = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!EventHandler.beforeBlockBreak(world, event.getPlayer(), pos, event.getState(), blockEntity)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGetBurnTime(FurnaceFuelBurnTimeEvent event) {
        Item item = event.getItemStack().getItem();
        if (EnvProxyForge.burnTimeRecord.containsKey(item)) {
            event.setBurnTime(EnvProxyForge.burnTimeRecord.get(item));
        }
    }

    @SubscribeEvent
    public void onRetexture(RetextureEvent event) {
        Block block = event.state.getBlock();
        if (block instanceof RetexturableBlock && ((RetexturableBlock) block).retexture(event.state, (Level) event.getLevel(), event.pos, event.side, event.getEntity(), event.refState, event.refVariant, event.refSide, event.refColorMultipliers)) {
            event.applied = true;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEnergyTileLoad(EnergyTileLoadEvent event) {
        if (event.getLevel().isClientSide()) {
            IC2R.log.warn(LogCategory.EnergyNet, "EnergyTileLoadEvent: posted for %s client-side, aborting", Util.toString(event.tile, event.getLevel(), EnergyNet.instance.getPos(event.tile)));
        } else {
            EnergyNet.instance.addTileUnchecked(event.tile);
        }
    }

    @SubscribeEvent
    public void onEnergyTileUnload(EnergyTileUnloadEvent event) {
        if (event.getLevel().isClientSide()) {
            IC2R.log.warn(LogCategory.EnergyNet, "EnergyTileUnloadEvent: posted for %s client-side, aborting", Util.toString(event.tile, event.getLevel(), EnergyNet.instance.getPos(event.tile)));
        } else {
            EnergyNet.instance.removeTile(event.tile);
        }
    }

    @SubscribeEvent
    public void onAttachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        final BlockEntity be = event.getObject();
        if (be instanceof Ic2rTileEntity) {
            if (be instanceof FluidBeBridge bridge) {
                Ic2rFluidBlock fb = bridge.getFluidBlock();
                if (fb != null && fb.isFluidBlock(null, null, null, be)) {
                    event.addCapability(fluidCapId, new BlockFluidCapImpl(fb, be));
                }
            } else {
                event.addCapability(fluidCapId, new LazyBlockFluidCapImpl(be));
            }
            // W2.1 / G2.1: TileEntityInventory (standard machines e.g. Macerator) expose ITEM_HANDLER via
            // InvSlotItemHandler combined view (facing == null; Access-only, no preferredSide) +
            // sided WorldlyContainer wrappers (facing != null). See docs/spec/item_handler_contract.md.
            if (be instanceof TileEntityInventory teInv) {
                event.addCapability(itemCapId, new ICapabilityProvider() {

                    private final LazyOptional<IItemHandlerModifiable>[] sided = SidedInvWrapper.create(teInv, Util.ALL_DIRS);

                    @Override
                    @NotNull
                    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction facing) {
                        if (capability != Capabilities.ItemHandler.BLOCK) {
                            return LazyOptional.empty();
                        }
                        if (facing == null) {
                            return TileEntityInventoryCap.createCap(teInv).cast();
                        }
                        return (LazyOptional<T>) this.sided[facing.ordinal()];
                    }
                });
            } else if (be instanceof WorldlyContainer) {
                event.addCapability(itemCapId, new ICapabilityProvider() {

                    private final LazyOptional<IItemHandlerModifiable>[] caps = SidedInvWrapper.create((WorldlyContainer) be, Util.ALL_DIRS);

                    @Override
                    @NotNull
                    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction facing) {
                        return (LazyOptional<T>) (facing != null && capability == Capabilities.ItemHandler.BLOCK ? this.caps[facing.ordinal()] : LazyOptional.empty());
                    }
                });
            } else if (be instanceof Container) {
                event.addCapability(itemCapId, new ICapabilityProvider() {

                    private final LazyOptional<IItemHandler> cap = LazyOptional.of(() -> new InvWrapper((Container) be));

                    @Override
                    @NotNull
                    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction facing) {
                        return (LazyOptional<T>) (capability == Capabilities.ItemHandler.BLOCK ? this.cap : LazyOptional.empty());
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onAttachItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        Item item = stack.getItem();
        if (item instanceof Ic2rFluidItem) {
            event.addCapability(fluidCapId, new ItemFluidCapImpl(stack));
        } else if (item instanceof AbstractItemNanoSaber) {
            event.addCapability(nanoSaberCapId, new ItemNanoSaberCapImpl(stack));
        }
    }
}
