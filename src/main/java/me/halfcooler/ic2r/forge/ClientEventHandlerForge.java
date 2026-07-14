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
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClientEventHandlerForge
{
	@SubscribeEvent
	public void registerClientCommands(RegisterClientCommandsEvent event)
	{
		CommandIc2rc.register(event.getDispatcher());
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event)
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
	public void onRenderHotBar(RenderGuiOverlayEvent.Post event)
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
