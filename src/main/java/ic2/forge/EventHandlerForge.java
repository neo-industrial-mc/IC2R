package ic2.forge;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.event.RetextureEvent;
import ic2.api.tile.RetexturableBlock;
import ic2.core.IC2;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.event.EventHandler;
import ic2.core.event.TickHandler;
import ic2.core.item.armor.jetpack.JetpackHandler;
import ic2.core.fluid.FluidBeBridge;
import ic2.core.fluid.Ic2FluidBlock;
import ic2.core.fluid.Ic2FluidItem;
import ic2.api.item.ElectricItem;
import ic2.core.item.tool.AbstractItemNanoSaber;
import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.item.armor.ItemArmorNanoSuit;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import ic2.core.util.LogCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
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
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;

public final class EventHandlerForge {

    @SubscribeEvent
    public void serverStart(ServerStartingEvent event) {
        EventHandler.onServerStart(event.getServer());
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
    public void onPlayerTick(PlayerTickEvent.Post event) {
        EventHandler.onPlayerTick(event.getEntity());
    }

    @SubscribeEvent
    public void onLivingSpecialSpawn(FinalizeSpawnEvent event) {
        EventHandler.onLivingSpecialSpawn(event.getEntity());
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player && event.getSource() == player.level().damageSources().fall() && JetpackHandler.hasJetpack(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST))) {
            IC2.grantAdvancement(player, "ic2/build_generator/build_batbox/build_jetpack/fall_with_jetpack");
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
        // LeftClickEmpty is not cancelable in NeoForge 1.21.1; invoke for side effects only.
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
    public void onEntityAttacked(LivingIncomingDamageEvent event) {
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
        if (block instanceof RetexturableBlock && ((RetexturableBlock) block).retexture(event.state, (Level) event.getLevel(), event.pos, event.side, event.player, event.refState, event.refVariant, event.refSide, event.refColorMultipliers)) {
            event.applied = true;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEnergyTileLoad(EnergyTileLoadEvent event) {
        if (event.getLevel().isClientSide()) {
            IC2.log.warn(LogCategory.EnergyNet, "EnergyTileLoadEvent: posted for %s client-side, aborting", Util.toString(event.tile, event.getLevel(), EnergyNet.instance.getPos(event.tile)));
        } else {
            EnergyNet.instance.addTileUnchecked(event.tile);
        }
    }

    @SubscribeEvent
    public void onEnergyTileUnload(EnergyTileUnloadEvent event) {
        if (event.getLevel().isClientSide()) {
            IC2.log.warn(LogCategory.EnergyNet, "EnergyTileUnloadEvent: posted for %s client-side, aborting", Util.toString(event.tile, event.getLevel(), EnergyNet.instance.getPos(event.tile)));
        } else {
            EnergyNet.instance.removeTile(event.tile);
        }
    }

    private static final ResourceLocation NANO_SABER_DAMAGE = ResourceLocation.fromNamespaceAndPath("ic2", "nano_saber_damage");
    private static final ResourceLocation NANO_SABER_SPEED = ResourceLocation.fromNamespaceAndPath("ic2", "nano_saber_speed");
    private static final ResourceLocation CHARGED_ARMOR = ResourceLocation.fromNamespaceAndPath("ic2", "charged_armor");

    // Dynamic per-stack attribute modifiers (1.21 replacement for the removed Forge getAttributeModifiers hook).
    @SubscribeEvent
    public void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        if (item instanceof AbstractItemNanoSaber) {
            int dmg = 4;
            float speed = -3.0F;
            if (ElectricItem.manager.canUse(stack, 400.0) && AbstractItemNanoSaber.isActive(stack)) {
                dmg = 20;
                speed = 0.0F;
            }
            event.removeAllModifiersFor(Attributes.ATTACK_DAMAGE);
            event.removeAllModifiersFor(Attributes.ATTACK_SPEED);
            event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(NANO_SABER_DAMAGE, dmg, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(NANO_SABER_SPEED, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        } else if (item instanceof ItemArmorElectric electric) {
            int[] protection = item instanceof ItemArmorNanoSuit ? ItemArmorNanoSuit.CHARGED_PROTECTION
                : item instanceof ItemArmorQuantumSuit ? ItemArmorQuantumSuit.CHARGED_PROTECTION : null;
            if (protection != null && ElectricItem.manager.getCharge(stack) >= electric.getEnergyPerDamage()) {
                EquipmentSlot slot = electric.getEquipmentSlot();
                event.removeAllModifiersFor(Attributes.ARMOR);
                event.addModifier(Attributes.ARMOR, new AttributeModifier(CHARGED_ARMOR, protection[slot.getIndex()], AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(slot));
            }
        }
    }

}
