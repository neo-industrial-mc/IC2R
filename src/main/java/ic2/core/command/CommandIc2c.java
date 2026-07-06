package ic2.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class CommandIc2c
{
	private static final SuggestionProvider<CommandSourceStack> SIDE_SUGGESTIONS = (ctx, builder) -> SharedSuggestionProvider.suggest(new String[] { "XN", "XP", "YN", "YP", "ZN", "ZP" }, builder);

	private CommandIc2c()
	{
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(
			Commands.literal("ic2c")
				.then(
					Commands.literal("rightClick")
						.then(
							Commands.argument("x", IntegerArgumentType.integer())
								.then(
									Commands.argument("y", IntegerArgumentType.integer())
										.then(
											Commands.argument("z", IntegerArgumentType.integer())
												.executes(ctx -> cmdRightClick(
													ctx.getSource(),
													IntegerArgumentType.getInteger(ctx, "x"),
													IntegerArgumentType.getInteger(ctx, "y"),
													IntegerArgumentType.getInteger(ctx, "z"),
													CommandIc2c.ClickSide.YP
												))
												.then(
													Commands.argument("side", StringArgumentType.word())
														.suggests(SIDE_SUGGESTIONS)
														.executes(ctx -> cmdRightClick(
															ctx.getSource(),
															IntegerArgumentType.getInteger(ctx, "x"),
															IntegerArgumentType.getInteger(ctx, "y"),
															IntegerArgumentType.getInteger(ctx, "z"),
															parseSide(StringArgumentType.getString(ctx, "side"))
														))
												)
										)
								)
						)
				)
				.then(Commands.literal("currentItem").executes(ctx -> CommandIc2.cmdCurrentItem(ctx.getSource())))
		);
	}

	private static int cmdRightClick(CommandSourceStack source, int x, int y, int z, CommandIc2c.ClickSide side) throws CommandSyntaxException
	{
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.gameMode == null)
		{
			throw new SimpleCommandExceptionType(Component.literal("Not in a world.")).create();
		}

		BlockPos pos = new BlockPos(x, y, z);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), side.facing, pos, false);
		mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
		CommandIc2.msg(source, "Right click executed.");
		return 1;
	}

	private static CommandIc2c.ClickSide parseSide(String name) throws CommandSyntaxException
	{
		try
		{
			return CommandIc2c.ClickSide.valueOf(name);
		} catch (IllegalArgumentException e)
		{
			throw new SimpleCommandExceptionType(Component.literal("Invalid side: " + name)).create();
		}
	}

	public static String getUsage()
	{
		return "/ic2c (rightClick <x> <y> <z> [XN|XP|YN|YP|ZN|ZP]) | currentItem";
	}

	private enum ClickSide
	{
		XN(Direction.WEST),
		XP(Direction.EAST),
		YN(Direction.DOWN),
		YP(Direction.UP),
		ZN(Direction.NORTH),
		ZP(Direction.SOUTH);

		final Direction facing;

		ClickSide(Direction facing)
		{
			this.facing = facing;
		}
	}
}