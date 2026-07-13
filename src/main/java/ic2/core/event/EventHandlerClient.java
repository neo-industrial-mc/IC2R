package ic2.core.event;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.item.ElectricItem;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.api.tile.IWrenchAble;
import ic2.core.GuiOverlayer;
import ic2.core.IC2;
import ic2.core.fluid.FluidHandler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.StandardFluidItem;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.armor.jetpack.JetpackLogic;
import ic2.core.item.tool.AbstractItemNanoSaber;
import ic2.core.item.tool.ContainerToolbox;
import ic2.core.item.upgrade.ItemUpgradeModule;
import ic2.core.network.RpcHandler;
import ic2.core.block.tileentity.TileEntityBase;
import ic2.core.event.TickHandler;
import ic2.core.proxy.SideProxyClient;
import ic2.core.ref.Ic2Items;
import ic2.core.sound.SoundManagerClient;
import ic2.core.util.EnhancedOverlayRenderer;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class EventHandlerClient
{
	public static void onClientSetup()
	{
		List<Block> fluidBlocks = new ArrayList<>();
		for (Block block : ForgeRegistries.BLOCKS.getValues())
		{
			ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
			if (id != null && "ic2".equals(id.getNamespace()) && id.getPath().startsWith("fluid_block_"))
			{
				fluidBlocks.add(block);
			}
		}

		if (!fluidBlocks.isEmpty())
		{
			SideProxyClient.envProxy.registerBlockLayer(RenderType.translucent(), fluidBlocks.toArray(Block[]::new));
		}

		SideProxyClient.envProxy.registerModelPredicateProvider(IC2.getIdentifier("charge"), (stack, world, entity, seed) -> (float) ElectricItem.manager.getChargeLevel(stack));
		SideProxyClient.envProxy.registerModelPredicateProvider(
			Ic2Items.FACADE_CELL,
			IC2.getIdentifier("has_fluid"),
			(stack, world, entity, seed) ->
			{
				Ic2FluidStack stored = StandardFluidItem.getFs(stack);
				return stored != null && !stored.isEmpty() ? 1.0F : 0.0F;
			}
		);

		for (Direction direction : Util.ALL_DIRS)
		{
			SideProxyClient.envProxy.registerModelPredicateProvider(IC2.getIdentifier(direction.getName()), (stack, world, entity, seed) -> stack.getItem() instanceof ItemUpgradeModule && ItemUpgradeModule.getDirection(stack) == direction ? 1.0F : 0.0F);
		}

		SideProxyClient.envProxy.registerModelPredicateProvider(IC2.getIdentifier("is_activated"), (stack, world, entity, seed) -> stack.getItem() instanceof AbstractItemNanoSaber nanoSaber ? nanoSaber.getActiveData(stack, world) : 0f);
		SideProxyClient.envProxy.registerModelPredicateProvider(IC2.getIdentifier("tool_box_open"), (stack, world, entity, seed) ->
			{
				if (!(entity instanceof Player player))
				{
					return 0.0F;
				} else
				{
					ItemStack mainHandItem;
					boolean open = player.containerMenu instanceof ContainerToolbox && (
						StackUtil.checkItemEquality(mainHandItem = player.getMainHandItem(), stack) || StackUtil.checkItemEquality(player.getOffhandItem(), stack) && !(mainHandItem.getItem() instanceof IHandHeldInventory)
					);
					return open ? 1.0F : 0.0F;
				}
			}
		);
	}

	public static void onSoundSetup()
	{
	}

	public static void livingEntityPreRender(LivingEntity entity, LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer)
	{
	}

	public static void livingEntityPostRender(LivingEntity entity, LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer)
	{
	}

	public static float onSetupFogDensity(BlockState state)
	{
		Fluid fluid = FluidHandler.getWorldFluid(state);
		if (fluid != null && "ic2".equals(Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid)).getNamespace()))
		{
			int density = FluidHandler.getDensity(fluid);
			return (float) Util.map(Math.abs(density), 20000.0, 2.0);
		} else
		{
			return -1.0F;
		}
	}

	public static int onRenderFogColor(BlockState state)
	{
		Fluid fluid = FluidHandler.getWorldFluid(state);
		return fluid != null && "ic2".equals(ForgeRegistries.FLUIDS.getKey(fluid).getNamespace()) ? FluidHandler.getColor(fluid) : -1;
	}

	/**
	 * Draw GT-style face grid when the held item implements {@link IEnhancedOverlayProvider}.
	 *
	 * @return {@code true} to cancel the vanilla block outline (we drew a replacement)
	 */
	public static boolean onDrawBlockHighlight(Player player, BlockHitResult target, float partialTicks, PoseStack matrix, MultiBufferSource buffers)
	{
		assert target.getType() == Type.BLOCK;

		ItemStack inHand = StackUtil.get(player, InteractionHand.MAIN_HAND);
		if (!(inHand.getItem() instanceof IEnhancedOverlayProvider provider))
		{
			return false;
		}

		Level world = player.level();
		BlockPos pos = target.getBlockPos();
		if (!world.getWorldBorder().isWithinBounds(pos))
		{
			return false;
		}

		Direction side = target.getDirection();
		if (!provider.providesEnhancedOverlay(world, pos, side, player, inHand))
		{
			return false;
		}

		Direction currentFacing = resolveFacingForOverlay(world, pos);
		return EnhancedOverlayRenderer.render(world, target, matrix, buffers, currentFacing);
	}

	@Nullable
	private static Direction resolveFacingForOverlay(Level world, BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof IWrenchAble wrenchAble)
		{
			return wrenchAble.getFacing(world, pos);
		}

		var property = state.getBlock().getStateDefinition().getProperty("facing");
		if (property != null && property.getValueClass() == Direction.class)
		{
			@SuppressWarnings("unchecked")
			var facingProperty = (net.minecraft.world.level.block.state.properties.Property<Direction>) property;
			return state.getValue(facingProperty);
		}

		return null;
	}

	public static boolean onDrawBlockHighlightLast(Player player, BlockHitResult target, float partialTicks, PoseStack matrix, MultiBufferSource buffers)
	{
		return false;
	}

	public static void onGuiCreate(Screen screen, List<GuiEventListener> widgets, Consumer<GuiEventListener> widgetAdder)
	{
	}

	private static final GuiOverlayer guiOverlayer = new GuiOverlayer(SideProxyClient.mc);

	public static void onRenderHotBar(GuiGraphics guiGraphics)
	{
		guiOverlayer.render(guiGraphics);
	}

	public static SoundInstance onSoundPlayed(SoundInstance sound)
	{
		return SoundManagerClient.onSoundPlayed(sound);
	}

	public static void onDisconnect()
	{
		RpcHandler.onDisconnect();
		// Ensure looping jetpack audio cannot survive into the next multiplayer session.
		JetpackLogic.stopJetpackSound(null);
	}

	public static void onClientPlayerJoin(Player player)
	{
		Level world = player.level();
		if (!world.isClientSide)
		{
			return;
		}

		TickHandler.requestContinuousWorldTick(world, new IWorldTickCallback()
		{
			private int age = 0;

			@Override
			public void onTick(Level level)
			{
				if (player.isRemoved() || ++this.age > 30)
				{
					TickHandler.removeContinuousWorldTick(level, this);
					return;
				}

				if (this.age < 3)
				{
					return;
				}

				ChunkPos center = player.chunkPosition();
				for (int dx = -4; dx <= 4; dx++)
				{
					for (int dz = -4; dz <= 4; dz++)
					{
						if (!level.hasChunk(center.x + dx, center.z + dz))
						{
							continue;
						}

						LevelChunk chunk = level.getChunk(center.x + dx, center.z + dz);
						for (BlockEntity blockEntity : chunk.getBlockEntities().values())
						{
							if (blockEntity instanceof TileEntityBase tileEntity)
							{
								tileEntity.requestSoundResume();
							}
						}
					}
				}

				TickHandler.removeContinuousWorldTick(level, this);
			}
		});
	}
}
