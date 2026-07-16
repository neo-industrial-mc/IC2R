package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.event.EnergyTileLoadEvent;
import me.halfcooler.ic2r.api.energy.event.EnergyTileUnloadEvent;
import me.halfcooler.ic2r.api.event.RetextureEvent;
import me.halfcooler.ic2r.api.tile.RetexturableBlock;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.command.CommandIc2r;
import me.halfcooler.ic2r.core.event.EventHandler;
import me.halfcooler.ic2r.core.event.TickHandler;
import me.halfcooler.ic2r.core.item.armor.jetpack.JetpackHandler;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

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

    // MissingMappingsEvent was removed in NeoForge 1.21; registry remaps need DataFixers / custom world upgrade later.

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
    public void onWorldTickStart(LevelTickEvent.Pre event) {
        TickHandler.onWorldTickStart(event.getLevel());
    }

    @SubscribeEvent
    public void onWorldTickEnd(LevelTickEvent.Post event) {
        TickHandler.onWorldTickEnd(event.getLevel());
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Pre event) {
        TickHandler.onServerTick();
    }

    @SubscribeEvent
    public void onPlayerTickStart(PlayerTickEvent.Pre event) {
        EventHandler.onPlayerTickStart(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerTickEnd(PlayerTickEvent.Post event) {
        EventHandler.onPlayerTick(event.getEntity());
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
        // LeftClickEmpty is not cancellable on NeoForge 1.21; still run side effects.
        EventHandler.onEntitySwingHand(event.getEntity(), event.getHand());
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
    public void onEntityAttacked(LivingDamageEvent.Pre event) {
        float remaining = EventHandler.onEntityAttacked(event.getEntity(), event.getSource(), event.getNewDamage());
        if (remaining <= 0.0F) {
            event.setNewDamage(0.0F);
        } else {
            event.setNewDamage(remaining);
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
        if (block instanceof RetexturableBlock && ((RetexturableBlock) block).retexture(event.state, (Level) event.getLevel(), event.pos, event.side, event.player, event.refState, event.refVariant, event.refSide, event.refColorMultipliers)) {
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

    // Capabilities are registered in Ic2rCapabilities via RegisterCapabilitiesEvent (NeoForge 1.21).
}
