package me.halfcooler.ic2r.forge;

import com.mojang.blaze3d.shaders.FogShape;
import me.halfcooler.ic2r.core.command.CommandIc2rc;
import me.halfcooler.ic2r.core.event.EventHandlerClient;
import me.halfcooler.ic2r.core.event.TickHandler;
import me.halfcooler.ic2r.core.sound.DeferredSoundOps;
import me.halfcooler.ic2r.core.proxy.SideProxyClient;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class ClientEventHandlerForge
{
	@SubscribeEvent
	public void registerClientCommands(RegisterClientCommandsEvent event)
	{
		CommandIc2rc.register(event.getDispatcher());
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent.Post event)
	{
		if (event.phase == TickEvent.Phase.START)
		{
			TickHandler.onClientTick();
		} else if (event.phase == TickEvent.Phase.END)
		{
			DeferredSoundOps.flush();
		}
	}

	@SubscribeEvent
	public void onSoundSetup(SoundEngineLoadEvent event)
	{
		EventHandlerClient.onSoundSetup();
	}

	@SubscribeEvent
	public void livingEntityPreRender(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> event)
	{
		EventHandlerClient.livingEntityPreRender(event.getEntity(), event.getRenderer());
	}

	@SubscribeEvent
	public void livingEntityPostRender(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event)
	{
		EventHandlerClient.livingEntityPostRender(event.getEntity(), event.getRenderer());
	}

	@SubscribeEvent
	public void onSetupFogDensity(ViewportEvent.RenderFog event)
	{
		float newDensity = EventHandlerClient.onSetupFogDensity(event.getCamera().getBlockAtCamera());
		if (newDensity >= 0.0F)
		{
			event.setCanceled(true);
			event.setNearPlaneDistance(-8.0F);
			event.setFarPlaneDistance(newDensity * 0.5F);
			event.setFogShape(FogShape.SPHERE);
		}
	}

	@SubscribeEvent
	public void onRenderFogColor(ViewportEvent.ComputeFogColor event)
	{
		int color = EventHandlerClient.onRenderFogColor(event.getCamera().getBlockAtCamera());
		if (color >= 0)
		{
			event.setRed((color >>> 16 & 0xFF) / 255.0F);
			event.setGreen((color >>> 8 & 0xFF) / 255.0F);
			event.setBlue((color & 0xFF) / 255.0F);
		}
	}

	@SubscribeEvent
	public void onDrawBlockHighlight(RenderHighlightEvent.Block event)
	{
		if (EventHandlerClient.onDrawBlockHighlight(
			SideProxyClient.mc.player, event.getTarget(), event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource()
		))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onDrawBlockHighlightLast(RenderHighlightEvent.Block event)
	{
		if (EventHandlerClient.onDrawBlockHighlightLast(
			SideProxyClient.mc.player, event.getTarget(), event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource()
		))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onGuiCreate(ScreenEvent.Init event)
	{
		EventHandlerClient.onGuiCreate(event.getScreen(), event.getListenersList(), event::addListener);
	}

	@SubscribeEvent
	public void onRenderHotBar(RenderGuiLayerEvent.Post event)
	{
		if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type())
		{
			EventHandlerClient.onRenderHotBar(event.getGuiGraphics());
		}
	}

	@SubscribeEvent
	public void onSoundPlayed(PlaySoundEvent event)
	{
		SoundInstance sound = event.getSound();
		SoundInstance newSound = EventHandlerClient.onSoundPlayed(sound);
		if (newSound != sound)
		{
			event.setSound(newSound);
		}
	}

	@SubscribeEvent
	public void onDisconnect(PlayerEvent.PlayerLoggedOutEvent event)
	{
		EventHandlerClient.onDisconnect();
	}

	@SubscribeEvent
	public void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event)
	{
		// More reliable than PlayerLoggedOutEvent when leaving a dedicated server.
		EventHandlerClient.onDisconnect();
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if (event.getEntity().level().isClientSide)
		{
			EventHandlerClient.onClientPlayerJoin(event.getEntity());
		}
	}

	@SubscribeEvent
	public void onWorldLoad(LevelEvent.Load event)
	{
		Level world = (Level) event.getLevel();
		if (!world.isClientSide)
		{
			return;
		}

		TickHandler.requestSingleWorldTick(world, loadedWorld ->
		{
			if (SideProxyClient.mc.player != null && SideProxyClient.mc.player.level() == loadedWorld)
			{
				EventHandlerClient.onClientPlayerJoin(SideProxyClient.mc.player);
			}
		});
	}
}
