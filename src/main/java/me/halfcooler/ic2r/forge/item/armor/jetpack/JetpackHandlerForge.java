package me.halfcooler.ic2r.forge.item.armor.jetpack;

import me.halfcooler.ic2r.core.item.armor.jetpack.JetpackHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;


/**
 * Forge-side event listeners for {@link JetpackHandler}.
 * All @SubscribeEvent wiring lives here so core stays forge-free (A40.2).
 */
public final class JetpackHandlerForge
{
	private JetpackHandlerForge() {}

	public static void register()
	{
		MinecraftForge.EVENT_BUS.register(new JetpackHandlerForge());
	}

	// Render layer injection moved from core; simplified to avoid generics issues.
	// TODO: restore jetpack render overlay in Track B render refactor.


	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		JetpackHandler.onPlayerTick(event.player);
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
	public void onLivingAttack(LivingAttackEvent event)
	{
		if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player)
		{
			JetpackHandler.bufferChestArmor(player);
		}
	}
}
