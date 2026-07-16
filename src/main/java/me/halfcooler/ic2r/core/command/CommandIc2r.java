package me.halfcooler.ic2r.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.halfcooler.ic2r.api.crops.CropCard;
import me.halfcooler.ic2r.api.crops.Crops;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.state.BlockStateUtil;
import me.halfcooler.ic2r.core.energy.grid.EnergyNetGlobal;
import me.halfcooler.ic2r.core.energy.grid.EnergyNetSettings;
import me.halfcooler.ic2r.core.energy.grid.GridInfo;
import me.halfcooler.ic2r.core.item.ItemCropSeed;
import me.halfcooler.ic2r.core.uu.UuGraph;
import me.halfcooler.ic2r.core.uu.UuIndex;
import me.halfcooler.ic2r.core.util.ConfigUtil;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;
import me.halfcooler.ic2r.platform.services.PlatformServices;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;

public final class CommandIc2r
{
	private static final SimpleCommandExceptionType UNKNOWN_COMMAND = new SimpleCommandExceptionType(Component.literal("Unknown Command."));
	private static final SuggestionProvider<CommandSourceStack> DEBUG_ACTION_SUGGESTIONS = (ctx, builder) -> SharedSuggestionProvider.suggest(new String[] { "dumpUuValues", "resolveIngredient", "dumpTextures", "dumpLargeGrids", "enet" }, builder);
	private static final SuggestionProvider<CommandSourceStack> ENET_OPTION_SUGGESTIONS = (ctx, builder) -> SharedSuggestionProvider.suggest(new String[] { "logIssues", "logUpdates" }, builder);
	private static final SuggestionProvider<CommandSourceStack> BOOL_SUGGESTIONS = (ctx, builder) -> SharedSuggestionProvider.suggest(new String[] { "true", "false" }, builder);
	private static final SuggestionProvider<CommandSourceStack> ITEM_SUGGESTIONS = (ctx, builder) -> {
		String remaining = builder.getRemaining().toLowerCase();

		for (Item item : BuiltInRegistries.ITEM)
		{
			String name = Util.getName(item).toString();
			if (name.toLowerCase().contains(remaining))
			{
				builder.suggest(name);
			}
		}

		if ("tag:".startsWith(remaining) || remaining.startsWith("tag:"))
		{
			builder.suggest("Tag:");
		}

		for (var fluid : BuiltInRegistries.FLUID)
		{
			String suggestion = "Fluid:" + Util.getName(fluid);
			if (suggestion.toLowerCase().contains(remaining))
			{
				builder.suggest(suggestion);
			}
		}

		return builder.buildFuture();
	};
	private static final SuggestionProvider<CommandSourceStack> TEXTURE_SIZE_SUGGESTIONS = (ctx, builder) -> {
		for (int size = 512; size > 8; size >>= 1)
		{
			builder.suggest(Integer.toString(size));
		}

		return builder.buildFuture();
	};

	private CommandIc2r()
	{
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(
			Commands.literal("ic2r")
				.requires(source -> source.hasPermission(4))
				.then(
					Commands.literal("debug")
						.then(Commands.literal("dumpUuValues").executes(ctx -> cmdDumpUuValues(ctx.getSource())))
						.then(Commands.literal("rebuildUuGraph").executes(ctx -> cmdRebuildUuGraph(ctx.getSource())))
						.then(
							Commands.literal("resolveIngredient")
								.then(
									Commands.argument("name", StringArgumentType.greedyString())
										.suggests(ITEM_SUGGESTIONS)
										.executes(ctx -> cmdDebugResolveIngredient(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
								)
						)
						.then(
							Commands.literal("dumpTextures")
								.then(
									Commands.argument("name", StringArgumentType.greedyString())
										.suggests(ITEM_SUGGESTIONS)
										.then(
											Commands.argument("size", IntegerArgumentType.integer(8, 512))
												.suggests(TEXTURE_SIZE_SUGGESTIONS)
												.executes(ctx -> cmdDebugDumpTextures(
													ctx.getSource(),
													StringArgumentType.getString(ctx, "name"),
													IntegerArgumentType.getInteger(ctx, "size")
												))
										)
								)
						)
						.then(Commands.literal("dumpLargeGrids").executes(ctx -> dumpLargeGrids(ctx.getSource())))
						.then(
							Commands.literal("enet")
								.then(
									Commands.argument("option", StringArgumentType.word())
										.suggests(ENET_OPTION_SUGGESTIONS)
										.then(
											Commands.argument("value", BoolArgumentType.bool())
												.suggests(BOOL_SUGGESTIONS)
												.executes(ctx -> cmdDebugEnet(
													ctx.getSource(),
													StringArgumentType.getString(ctx, "option"),
													BoolArgumentType.getBool(ctx, "value")
												))
										)
								)
						)
				)
				.then(Commands.literal("currentItem").executes(ctx -> cmdCurrentItem(ctx.getSource())))
				.then(Commands.literal("itemNameWithVariant").executes(ctx -> cmdItemNameWithVariant(ctx.getSource())))
				.then(
					Commands.literal("giveCrop")
						.then(
							Commands.argument("owner", StringArgumentType.word())
								.then(
									Commands.argument("name", StringArgumentType.word())
										.then(
											Commands.argument("growth", IntegerArgumentType.integer(1, 31))
												.then(
													Commands.argument("gain", IntegerArgumentType.integer(1, 31))
														.then(
															Commands.argument("resistance", IntegerArgumentType.integer(1, 31))
																.executes(ctx -> cmdGiveCrop(
																	ctx.getSource(),
																	StringArgumentType.getString(ctx, "owner"),
																	StringArgumentType.getString(ctx, "name"),
																	IntegerArgumentType.getInteger(ctx, "growth"),
																	IntegerArgumentType.getInteger(ctx, "gain"),
																	IntegerArgumentType.getInteger(ctx, "resistance")
																))
														)
												)
										)
								)
						)
				)
		);
	}

	private static int cmdDumpUuValues(CommandSourceStack source)
	{
		List<Entry<ItemStack, Double>> list = new ArrayList<>();
		int infinite = 0;

		for (var it = UuGraph.iterator(); it.hasNext();)
		{
			Entry<ItemStack, Double> entry = it.next();
			if (entry.getValue() == null || entry.getValue() >= Double.POSITIVE_INFINITY)
			{
				infinite++;
			} else
			{
				list.add(entry);
			}
		}

		list.sort(Comparator.comparingDouble(Entry::getValue));
		msg(source, String.format("UU Values: %d finite, %d infinite (showing finite, sorted by value):", list.size(), infinite));

		for (Entry<ItemStack, Double> entry : list)
		{
			String line = String.format("  %s: %s", entry.getKey().getHoverName().getString(), entry.getValue());
			msg(source, line);
			IC2R.log.info(LogCategory.Uu, line);
		}

		return 1;
	}

	private static int cmdRebuildUuGraph(CommandSourceStack source)
	{
		try
		{
			UuIndex.instance.refresh(true);
			msg(source, "UU graph rebuild started/finished (see log for node counts).");
			return 1;
		} catch (Exception e)
		{
			msg(source, "UU graph rebuild failed: " + e.getMessage());
			IC2R.log.warn(LogCategory.Uu, e, "Manual UU graph rebuild failed.");
			return 0;
		}
	}

	private static int cmdDebugResolveIngredient(CommandSourceStack source, String arg)
	{
		try
		{
			IRecipeInput input = ConfigUtil.asRecipeInput(arg);
			if (input == null)
			{
				msg(source, "No match");
			} else
			{
				List<ItemStack> inputs = input.getInputs();
				msg(source, inputs.size() + " matches:");

				for (ItemStack stack : inputs)
				{
					if (stack == null)
					{
						msg(source, " null");
					} else
					{
						msg(
							source,
							String.format(
								" %s (%s, tags: %s, name: %s)",
								StackUtil.toStringSafe(stack),
								Util.getName(stack.getItem()),
								getTagNames(stack),
								stack.getHoverName().getString()
							)
						);
					}
				}
			}
		} catch (Exception e)
		{
			msg(source, "Error: " + e);
		}

		return 1;
	}

	private static String getTagNames(ItemStack stack)
	{
		StringBuilder ret = new StringBuilder();

		stack.getTags().forEach(tag -> {
			if (!ret.isEmpty())
			{
				ret.append(", ");
			}

			ret.append(tag.location());
		});

		return ret.isEmpty() ? "<none>" : ret.toString();
	}

	private static int cmdDebugDumpTextures(CommandSourceStack source, String name, int size)
	{
		// Physical dedicated server has no client texture atlas (G3.1: via PlatformLifecycle, not FMLEnvironment).
		if (!PlatformServices.lifecycle().isClient())
		{
			msg(source, "Can't dump textures on the dedicated server.");
		} else
		{
			// Client-only texture dumping relies on legacy OpenGL capture and has not been ported yet.
			msg(source, "Texture dumping is not yet available on this version.");
		}

		return 1;
	}

	private static int dumpLargeGrids(CommandSourceStack source)
	{
		MinecraftServer server = source.getServer();
		List<GridInfo> allGrids = new ArrayList<>();

		for (ServerLevel level : server.getAllLevels())
		{
			allGrids.addAll(EnergyNetGlobal.getLocal(level).getGridInfos());
		}

		allGrids.sort((a, b) -> b.complexNodeCount() - a.complexNodeCount());
		msg(source, "found " + allGrids.size() + " grids overall");

		for (int i = 0; i < 8 && i < allGrids.size(); i++)
		{
			GridInfo grid = allGrids.get(i);
			if (grid.nodeCount() == 0)
			{
				msg(source, "grid " + grid.id() + " is empty");
			} else
			{
				msg(
					source,
					String.format(
						"%d complex / %d total nodes in grid %d (%d/%d/%d - %d/%d/%d)",
						grid.complexNodeCount(),
						grid.nodeCount(),
						grid.id(),
						grid.minX(),
						grid.minY(),
						grid.minZ(),
						grid.maxX(),
						grid.maxY(),
						grid.maxZ()
					)
				);
			}
		}

		return 1;
	}

	private static int cmdDebugEnet(CommandSourceStack source, String option, boolean value) throws CommandSyntaxException
	{
		if ("logIssues".equals(option))
		{
			msg(source, "setting logGridUpdateIssues to " + value);
			EnergyNetSettings.logGridUpdateIssues = value;
		} else if ("logUpdates".equals(option))
		{
			msg(source, "setting logGridUpdatesVerbose to " + value);
			EnergyNetSettings.logGridUpdatesVerbose = value;
		} else
		{
			throw invalidUsage(source);
		}

		return 1;
	}

	static int cmdCurrentItem(CommandSourceStack source) throws CommandSyntaxException
	{
		if (!(source.getEntity() instanceof Player player))
		{
			msg(source, "Not applicable for non-player");
			return 0;
		}

		ItemStack stack = player.getMainHandItem();
		if (StackUtil.isEmpty(stack))
		{
			msg(source, "empty: " + StackUtil.toStringSafe(stack));
		} else
		{
			msg(
				source,
				String.format(
					"ID: %s, Damage: %d/%d, NBT: %s",
					Util.getName(stack.getItem()),
					stack.getDamageValue(),
					stack.getMaxDamage(),
					stack.getTag()
				)
			);
			msg(source, "Current Item excluding amount: " + ConfigUtil.fromStack(stack));
			msg(source, "Current Item including amount: " + ConfigUtil.fromStackWithAmount(stack));
		}

		return 1;
	}

	private static int cmdItemNameWithVariant(CommandSourceStack source) throws CommandSyntaxException
	{
		if (!(source.getEntity() instanceof Player player))
		{
			throw new SimpleCommandExceptionType(Component.literal("Not applicable for non-player")).create();
		}

		ItemStack stack = player.getMainHandItem();
		if (StackUtil.isEmpty(stack))
		{
			msg(source, "empty: " + StackUtil.toStringSafe(stack));
		} else if (!stack.getItem().getClass().getCanonicalName().startsWith("me.halfcooler.ic2r.core"))
		{
			msg(source, "Not an IC2R Item.");
		} else
		{
			String name = Util.getName(stack.getItem()).getPath();
			String variant = null;
			if (stack.getItem() instanceof BlockItem blockItem)
			{
				BlockState state = blockItem.getBlock().defaultBlockState();
				variant = BlockStateUtil.getVariantString(state);
			}

			msg(source, "Name: " + name + (variant == null || "normal".equals(variant) ? "" : " Variant: " + variant));
		}

		return 1;
	}

	private static int cmdGiveCrop(CommandSourceStack source, String owner, String name, int growth, int gain, int resistance) throws CommandSyntaxException
	{
		if (!(source.getEntity() instanceof Player player))
		{
			throw new SimpleCommandExceptionType(Component.literal("Not applicable for non-player")).create();
		}

		if (!StackUtil.isEmpty(player.getMainHandItem()))
		{
			msg(source, "The currently selected slot needs to be empty.");
			return 0;
		}

		CropCard crop = Crops.instance.getCropCard(owner, name);
		if (crop == null)
		{
			msg(source, "The crop you specified does not exist.");
			return 0;
		}

		player.getInventory().add(ItemCropSeed.generateItemStackFromValues(crop, growth, gain, resistance, 4));
		return 1;
	}

	public static void msg(CommandSourceStack source, String text)
	{
		source.sendSuccess(() -> Component.literal(text), false);
	}

	private static CommandSyntaxException invalidUsage(CommandSourceStack source)
	{
		return new SimpleCommandExceptionType(Component.literal(getUsage())).create();
	}

	public static String getUsage()
	{
		return "/ic2r debug (dumpUuValues | rebuildUuGraph | resolveIngredient <name> | dumpTextures <name> <size> | dumpLargeGrids | enet (logIssues | logUpdates) (true|false)) | currentItem | itemNameWithVariant | giveCrop <owner> <name> <growth (1-31)> <gain (1-31)> <resistance (1-31)>";
	}
}