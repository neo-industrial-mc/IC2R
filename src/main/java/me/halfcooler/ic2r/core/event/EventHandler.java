package me.halfcooler.ic2r.core.event;

import me.halfcooler.ic2r.api.block.BreakableBlock;
import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.item.BlockBreakableItem;
import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.api.item.IEntityAttackableItem;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.api.sound.item.ISwingSoundItem;
import me.halfcooler.ic2r.core.ChunkLoaderLogic;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.apihelper.CoreAccessImpl;
import me.halfcooler.ic2r.platform.services.PlatformServices;
import me.halfcooler.ic2r.core.block.ChunkLoadAwareBlockHandler;
import me.halfcooler.ic2r.core.block.comp.Components;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntitySemifluidGenerator;
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityElectrolyzer;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityFermenter;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMatter;
import me.halfcooler.ic2r.core.block.inherit.Ic2rFenceBlock;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityRecycler;
import me.halfcooler.ic2r.core.crop.Ic2rCrops;
import me.halfcooler.ic2r.core.energy.EnergyNetMode;
import me.halfcooler.ic2r.core.energy.grid.EnergyNetGlobal;
import me.halfcooler.ic2r.core.init.BlocksItems;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.init.MainConfig;
import me.halfcooler.ic2r.core.init.Rezepte;
import me.halfcooler.ic2r.core.item.ElectricItemManager;
import me.halfcooler.ic2r.core.item.GatewayElectricItemManager;
import me.halfcooler.ic2r.core.item.armor.ItemArmorElectric;
import me.halfcooler.ic2r.core.item.armor.ItemArmorHazmat;
import me.halfcooler.ic2r.core.item.armor.jetpack.JetpackAttachmentRecipe;
import me.halfcooler.ic2r.core.item.armor.jetpack.JetpackHandler;
import me.halfcooler.ic2r.core.item.armor.ItemArmorNanoSuit;
import me.halfcooler.ic2r.core.item.armor.ItemArmorQuantumSuit;
import me.halfcooler.ic2r.core.recipe.input.RecipeInputFactory;
import me.halfcooler.ic2r.core.ref.Ic2rBlockTags;
import me.halfcooler.ic2r.core.ref.Ic2rBoatTypes;
import me.halfcooler.ic2r.core.ref.Ic2rEntities;
import me.halfcooler.ic2r.core.ref.Ic2rGameEvents;
import me.halfcooler.ic2r.core.ref.Ic2rItemTags;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.ref.Ic2rRecipeSerializers;
import me.halfcooler.ic2r.core.ref.Ic2rRecipeTypes;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.uu.UuIndex;
import me.halfcooler.ic2r.core.world.Ic2rWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public final class EventHandler
{
	public static void onInitGameEvents()
	{
		// SoundEvents are queued via DeferredRegister in FmlMod constructor (W1.7).
		Ic2rGameEvents.init();
	}

	public static void onInitEarly()
	{
		long startTime = System.nanoTime();
		IC2R.log.debug(LogCategory.General, "Starting pre-init.");
		MainConfig.load();
		CoreAccessImpl.init();
		Recipes.inputFactory = new RecipeInputFactory();
		EnergyNet.instance = EnergyNetGlobal.create();
		ElectricItem.manager = new GatewayElectricItemManager();
		ElectricItem.rawManager = new ElectricItemManager();
		Components.init();
		BlocksItems.init();
		Ic2rBlockTags.init();
		Ic2rItemTags.init();
		Ic2rBoatTypes.init();
		Ic2rEntities.init();
		Ic2rRecipeTypes.init();
		Ic2rRecipeSerializers.init();
		Ic2rWorldGen.init();
		TileEntityRecycler.init();
		TileEntityElectrolyzer.init();
		Rezepte.registerRecipes();
		Ic2rCrops.init();
		IC2R.sideProxy.preInit();
		IC2R.initialized = true;
		IC2R.log.debug(LogCategory.General, "Finished pre-init after %d ms.", (System.nanoTime() - startTime) / 1000000L);
	}

	public static void onInit()
	{
		EnergyNetGlobal.initCalculator();
		MainConfig.ignoreInvalidRecipes = IC2RConfig.recipes.ignoreInvalidRecipes.get();
		TileEntityMatter.init();
		TileEntitySemifluidGenerator.init();
		TileEntityFluidHeatGenerator.init();
		TileEntityLiquidHeatExchanger.init();
		TileEntityFermenter.init();
		JetpackHandler.init();
		JetpackAttachmentRecipe.init();
	}

	public static void onInitLate()
	{
		long startTime = System.nanoTime();
		// Register resolvers only; graph build needs a live RecipeManager (server start).
		UuIndex.instance.init();
		IC2R.sideProxy.onPostInit();
		// W3.2: first common call site on PlatformLifecycle (dual-track; other sites still use envProxy)
		IC2R.sideProxy.requestTick(!PlatformServices.lifecycle().isClient(), ChunkLoadAwareBlockHandler::init);
		IC2R.log.debug(LogCategory.General, "Finished post-init after %d ms.", (System.nanoTime() - startTime) / 1000000L);
	}

	private static boolean loadSubModule(String name)
	{
		IC2R.log.debug(LogCategory.SubModule, "Loading %s submodule: %s.", "ic2r", name);

		try
		{
			Class<?> subModuleClass = IC2R.class.getClassLoader().loadClass("me.halfcooler.ic2r." + name + ".SubModule");
			return (Boolean) subModuleClass.getMethod("init").invoke(null);
		} catch (Throwable t)
		{
			IC2R.log.debug(LogCategory.SubModule, "Submodule %s not loaded.", name);
			return false;
		}
	}

	public static void onServerStart(MinecraftServer server)
	{
		IC2R.sideProxy.onServerAvailable(server);
		// Recipes (and tags) are available here — build UU graph with crafting/smelting/machine edges.
		try
		{
			UuIndex.instance.refresh(true);
			IC2R.log.info(LogCategory.Uu, "UU graph rebuilt after server start.");
		} catch (Exception e)
		{
			IC2R.log.warn(LogCategory.Uu, e, "Failed to rebuild UU graph on server start.");
		}
	}

	public static void onPlayerLogout(Player player)
	{
		if (IC2R.sideProxy.isSimulating())
		{
			IC2R.sideProxy.getKeyboard().removePlayerReferences(player);
		}
	}

	public static void onPlayerLogin(Player player)
	{
		if (!(player instanceof ServerPlayer))
		{
			return;
		}

		EnergyNetMode mode = EnergyNetMode.fromConfig(IC2RConfig.misc.energyNetMode.get());
		// G3.6: player message via PlatformPlayerUi (Forge → SideProxy#messagePlayer)
		PlatformServices.playerUi().messagePlayer(
			player,
			Component.translatable("ic2r.energynet.mode", Component.translatable("ic2r.energynet.mode." + mode.name().toLowerCase()))
		);
	}

	public static void onWorldLoad(Level world)
	{
		if (!world.isClientSide)
		{
			ServerLevel serverWorld = (ServerLevel) world;
			ChunkLoaderLogic.onWorldLoad(serverWorld);
		}
	}

	public static void onWorldUnload(Level world)
	{
		ChunkLoadAwareBlockHandler.onWorldUnload(world);
		WorldData.onWorldUnload(world);
	}

	public static void onChunkDataLoad(LevelChunk chunk, CompoundTag data)
	{
	}

	public static void onChunkSave(LevelChunk chunk, CompoundTag data)
	{
	}

	public static void onChunkLoad(LevelChunk chunk)
	{
		ChunkLoadAwareBlockHandler.onChunkLoad(chunk);
	}

	public static void onChunkUnload(LevelChunk chunk)
	{
		ChunkLoadAwareBlockHandler.onChunkUnload(chunk);
		if (!chunk.getLevel().isClientSide)
		{
			ChunkLoaderLogic.onChunkUnload(chunk);
		}
	}

	public static InteractionResult onBlockStartBreak(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction)
	{
		if (player.getItemInHand(hand).getItem() instanceof BlockBreakableItem blockBreakableItem)
		{
			InteractionResult actionResult = blockBreakableItem.onBlockStartBreak(player, world, hand, pos, direction);
			if (actionResult != InteractionResult.PASS)
			{
				return actionResult;
			}
		}

		BlockState blockState = world.getBlockState(pos);
		return blockState.getBlock() instanceof BreakableBlock breakableBlock
			? breakableBlock.startBreak(player, world, hand, pos, blockState, direction)
			: InteractionResult.PASS;
	}

	public static boolean beforeBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
		Item item = player.getMainHandItem().getItem();
		return !(item instanceof BlockBreakableItem) || ((BlockBreakableItem) item).beforeBlockBreak(world, player, pos, state, blockEntity);
	}

	public static void afterBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
		Item item = player.getMainHandItem().getItem();
		if (item instanceof BlockBreakableItem)
		{
			((BlockBreakableItem) item).afterBlockBreak(world, player, pos, state, blockEntity);
		}
	}

	public static void onPlayerTickStart(Player player)
	{
		Ic2rFenceBlock.onPlayerTick(player);
	}

	public static void onPlayerTick(Player player)
	{
		JetpackHandler.onPlayerTick(player);
	}

	public static void onLivingSpecialSpawn(LivingEntity rawEntity)
	{
		if (IC2R.seasonal && (rawEntity instanceof Zombie || rawEntity instanceof Skeleton) && rawEntity.getCommandSenderWorld().random.nextFloat() < 0.1F)
		{
			Mob entity = (Mob) rawEntity;

			for (EquipmentSlot slot : EquipmentSlot.values())
			{
				entity.setDropChance(slot, Float.NEGATIVE_INFINITY);
			}
		}
	}

	public static boolean onLivingFall(LivingEntity entity, float distance)
	{
		if (entity.getCommandSenderWorld().isClientSide)
		{
			return false;
		} else
		{
			ItemStack armor = entity.getItemBySlot(EquipmentSlot.FEET);
			if (StackUtil.isEmpty(armor))
			{
				return false;
			} else
			{
				Item armorItem = armor.getItem();
				if (armorItem == Ic2rItems.RUBBER_BOOTS)
				{
					return ((ItemArmorHazmat) armorItem).absorbFall(armor, entity, distance);
				} else if (armorItem == Ic2rItems.NANO_BOOTS)
				{
					return ((ItemArmorNanoSuit) armorItem).absorbFall(armor, distance);
				} else
				{
					return armorItem == Ic2rItems.QUANTUM_BOOTS && ((ItemArmorQuantumSuit) armorItem).absorbFall(armor, distance);
				}
			}
		}
	}

	public static boolean onEntitySwingHand(LivingEntity entity, InteractionHand hand)
	{
		ItemStack stack = entity.getItemInHand(hand);
		if (stack.getItem() instanceof ISwingSoundItem swingSoundItem)
		{
			SoundEvent swingSound = swingSoundItem.getSwingSound(entity, hand);
			if (swingSound != null)
			{
				entity.playSound(swingSound, 1.0F, 1.0F);
			}

		}
		return false;
	}

	public static boolean onEntityInteract(Player player, InteractionHand hand, Entity target)
	{
		if (player.getCommandSenderWorld().isClientSide)
		{
			return false;
		}

		ItemStack stack = StackUtil.get(player, hand);
		StackUtil.isEmpty(stack);
		return false;
	}

	public static boolean onAttackEntity(Player player, Entity target)
	{
		Item item = player.getMainHandItem().getItem();
		return !(item instanceof IEntityAttackableItem) || ((IEntityAttackableItem) item).onAttackEntity(player, target);
	}

	public static float onEntityAttacked(LivingEntity victim, DamageSource source, float amount)
	{
		if (victim instanceof Player player)
		{
			if (ItemArmorHazmat.hazmatAbsorbs(source) && ItemArmorHazmat.hasCompleteHazmat(victim))
			{
				if (source.is(DamageTypeTags.IS_FIRE))
				{
					victim.setRemainingFireTicks(0);
				}

				return 0.0F;
			}

			return ItemArmorElectric.damageArmor(player, source, amount);
		}
		return amount;
	}
}
