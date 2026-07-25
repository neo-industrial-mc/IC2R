package me.halfcooler.ic2r.forge;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import me.halfcooler.ic2r.core.event.EventHandlerClient;
import me.halfcooler.ic2r.core.fluid.FluidHandler;
import me.halfcooler.ic2r.forge.model.BeModelLoader;
import me.halfcooler.ic2r.forge.model.CableModelLoader;
import me.halfcooler.ic2r.forge.model.MaskOverlayItemLoader;
import me.halfcooler.ic2r.forge.model.WallModelLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public final class ClientModEventHandlerForge
{
	private static <T extends BlockEntity> void registerBer(ClientEnvProxyForge.BerRegistration<T> reg, EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(reg.blockEntityType(), reg.blockEntityRendererProvider());
	}

	private static <T extends Entity> void registerEntityRenderer(
		ClientEnvProxyForge.EntityRendererRegistration<T> reg, EntityRenderersEvent.RegisterRenderers event
	)
	{
		event.registerEntityRenderer(reg.type(), reg.factory());
	}

	private static <T extends BlockEntity> void registerBlockEntityRenderer(
		ClientEnvProxyForge.BlockEntityRendererRegistration<T> reg, EntityRenderersEvent.RegisterRenderers event
	)
	{
		event.registerBlockEntityRenderer(reg.type(), reg.factory());
	}

	private static <H extends AbstractContainerMenu> void registerScreen(
		ClientEnvProxyForge.ScreenRegistration<H> reg, RegisterMenuScreensEvent event
	)
	{
		event.register(reg.type(), reg.factory()::create);
	}

	@SubscribeEvent
	public void onRegisterBlockColorProviders(RegisterColorHandlersEvent.Block event)
	{
		for (ClientEnvProxyForge.BlockColorProviderRegistration reg : ClientEnvProxyForge.blockColorProviderRegistrations)
		{
			event.getBlockColors().register(reg.provider(), reg.blocks());
		}
	}

	@SubscribeEvent
	public void onRegisterItemColorProviders(RegisterColorHandlersEvent.Item event)
	{
		for (ClientEnvProxyForge.ItemColorProviderRegistration reg : ClientEnvProxyForge.itemColorProviderRegistrations)
		{
			event.getItemColors().register(reg.provider(), reg.items());
		}
	}

	@SubscribeEvent
	public void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		for (ClientEnvProxyForge.BerRegistration<?> reg : ClientEnvProxyForge.berRegistrations)
		{
			registerBer(reg, event);
		}

		for (ClientEnvProxyForge.EntityRendererRegistration<?> reg : ClientEnvProxyForge.entityRendererRegistrations)
		{
			registerEntityRenderer(reg, event);
		}

		for (ClientEnvProxyForge.BlockEntityRendererRegistration<?> reg : ClientEnvProxyForge.blockEntityRendererRegistrations)
		{
			registerBlockEntityRenderer(reg, event);
		}
	}

	@SubscribeEvent
	public void onModelRegistry(ModelEvent.RegisterGeometryLoaders event)
	{
		event.register(ResourceLocation.fromNamespaceAndPath("ic2r", "be"), new BeModelLoader());
		event.register(ResourceLocation.fromNamespaceAndPath("ic2r", "cable"), new CableModelLoader());
		event.register(ResourceLocation.fromNamespaceAndPath("ic2r", "mask_overlay"), new MaskOverlayItemLoader());
		event.register(ResourceLocation.fromNamespaceAndPath("ic2r", "wall"), new WallModelLoader());
	}

	/**
	 * Side-load FE converter port cubes so their parents/textures are resolved as top-level models.
	 * The world model uses {@code ic2r:be} and bakes these as face meshes; without this they can bake empty.
	 */
	@SubscribeEvent
	public void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event)
	{
		event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("ic2r", "block/wiring/fe_converter")));
		event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("ic2r", "block/wiring/fe_converter_active")));
		event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("ic2r", "block/wiring/fe_converter_eu")));
		event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("ic2r", "block/wiring/fe_converter_fe")));
	}

	@SubscribeEvent
	public void onRegisterMenuScreens(RegisterMenuScreensEvent event)
	{
		for (ClientEnvProxyForge.ScreenRegistration<?> reg : ClientEnvProxyForge.screenRegistrations)
		{
			registerScreen(reg, event);
		}
		ClientEnvProxyForge.screenRegistrations.clear();
	}

	@SubscribeEvent
	public void onRegisterKeybindings(RegisterKeyMappingsEvent event)
	{
		for (KeyMapping keybinding : ClientEnvProxyForge.keyBindingRegistrations)
		{
			event.register(keybinding);
		}
	}

	@SubscribeEvent
	public void onClientSetup(FMLClientSetupEvent event)
	{
		EventHandlerClient.onClientSetup();

		for (ClientEnvProxyForge.BlockLayerRegistration reg : ClientEnvProxyForge.blockLayerRegistrations)
		{
			for (var block : reg.blocks())
			{
				ItemBlockRenderTypes.setRenderLayer(block, reg.layer());
			}
		}
	}

	/**
	 * NeoForge 1.21+ replacement for {@code FluidType#initializeClient}: register
	 * textures, tint, and underwater fog per fluid type on the client mod bus.
	 */
	@SubscribeEvent
	public void onRegisterClientExtensions(RegisterClientExtensionsEvent event)
	{
		for (EnvFluidHandlerForge.PendingClientFluidExtensions pending : EnvFluidHandlerForge.pendingClientFluidExtensions)
		{
			FluidType fluidType = pending.fluidType().get();
			if (fluidType == null)
			{
				continue;
			}
			ResourceLocation still = pending.stillSpriteId();
			ResourceLocation flowing = pending.flowingSpriteId() != null ? pending.flowingSpriteId() : still;
			int color = pending.color();
			int density = pending.density();
			event.registerFluidType(new IClientFluidTypeExtensions()
			{
				@Override
				public int getTintColor()
				{
					return color;
				}

				@Override
				public @NotNull ResourceLocation getStillTexture()
				{
					return still;
				}

				@Override
				public @NotNull ResourceLocation getFlowingTexture()
				{
					return flowing;
				}

				@Override
				public @NotNull Vector3f modifyFogColor(
					@NotNull Camera camera,
					float partialTick,
					@NotNull ClientLevel level,
					int renderDistance,
					float darkenWorldAmount,
					@NotNull Vector3f fluidFogColor
				)
				{
					float[] rgb = FluidHandler.fogRgb(color);
					return new Vector3f(rgb[0], rgb[1], rgb[2]);
				}

				@Override
				public void modifyFogRender(
					@NotNull Camera camera,
					FogRenderer.@NotNull FogMode mode,
					float renderDistance,
					float partialTick,
					float nearDistance,
					float farDistance,
					@NotNull FogShape shape
				)
				{
					float fogEnd = FluidHandler.fogEndForDensity(density);
					RenderSystem.setShaderFogStart(-8.0F);
					RenderSystem.setShaderFogEnd(fogEnd);
					RenderSystem.setShaderFogShape(FogShape.SPHERE);
				}
			}, fluidType);
		}
		EnvFluidHandlerForge.pendingClientFluidExtensions.clear();
	}

	/**
	 * SoundEngineLoadEvent is IModBusEvent — must be on mod bus, not NeoForge.EVENT_BUS.
	 */
	@SubscribeEvent
	public void onSoundSetup(SoundEngineLoadEvent event)
	{
		EventHandlerClient.onSoundSetup();
	}
}
