package me.halfcooler.ic2r.forge.item.armor.jetpack;

import me.halfcooler.ic2r.core.item.armor.jetpack.JetpackHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;


/**
 * Forge-side event listeners for {@link JetpackHandler}.
 * All @SubscribeEvent wiring lives here so core stays forge-free (A40.2).
 */
public final class JetpackHandlerForge
{
	private JetpackHandlerForge() {}

	public static void register()
	{
		NeoForge.EVENT_BUS.register(new JetpackHandlerForge());
	}

	// Render layer injection moved from core; simplified to avoid generics issues.
	// TODO: restore jetpack render overlay in Track B render refactor.


	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Post event)
	{
		JetpackHandler.onPlayerTick(event.getEntity());
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	@OnlyIn(Dist.CLIENT)
	public void onTooltip(ItemTooltipEvent event)
	{
		JetpackHandler.addJetpackTooltip(event.getItemStack(), event.getToolTip());
	}

	@SubscribeEvent
	public void onEquipmentChange(LivingEquipmentChangeEvent event)
	{
		JetpackHandler.onEquipmentChanged(event.getEntity());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void onLivingAttack(LivingIncomingDamageEvent event)
	{
		if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player)
		{
			JetpackHandler.bufferChestArmor(player);
		}
	}
}
