package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.BlockDynamite;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Dynamite-O-Mote: links to placed dynamite sticks and detonates them remotely.
 */
public class ItemRemote extends Item
{
	private static final String COORDS_KEY = "coords";

	public ItemRemote(Properties properties)
	{
		super(properties.stacksTo(1));
	}

	@NotNull
	public InteractionResult useOn(UseOnContext context)
	{
		Level level = context.getLevel();
		if (level.isClientSide)
		{
			return InteractionResult.SUCCESS;
		}

		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		if (!state.is(Ic2rBlocks.DYNAMITE))
		{
			// Consume the click so use() does not fire and detonate by accident.
			return InteractionResult.SUCCESS;
		}

		Player player = context.getPlayer();
		if (player == null)
		{
			return InteractionResult.SUCCESS;
		}

		ItemStack stack = StackUtil.get(player, context.getHand());
		if (!state.getValue(BlockDynamite.LINKED))
		{
			addRemote(pos, stack);
			level.setBlock(pos, state.setValue(BlockDynamite.LINKED, true), 3);
		}
		else
		{
			int index = hasRemote(pos, stack);
			if (index > -1)
			{
				level.setBlock(pos, state.setValue(BlockDynamite.LINKED, false), 3);
				removeRemote(index, stack);
			}
			else
			{
				IC2R.sideProxy.messagePlayer(player, "ic2r.remote.cannot_unlink");
			}
		}

		return InteractionResult.SUCCESS;
	}

	@NotNull
	public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (level.isClientSide)
		{
			return InteractionResultHolder.success(stack);
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(), Ic2rSoundEvents.ITEM_REMOTE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
		launchRemotes(level, stack, player);
		return InteractionResultHolder.success(stack);
	}

	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
	{
		int linked = getLinkedCount(stack);
		if (linked > 0)
		{
			Ic2rTooltip.add(tooltip, Component.translatable("ic2r.remote.tooltip.linked", linked));
		}
	}

	public static void addRemote(BlockPos pos, ItemStack freq)
	{
		CompoundTag compound = StackUtil.getOrCreateNbtData(freq);
		ListTag coords = compound.contains(COORDS_KEY, Tag.TAG_LIST)
			? compound.getList(COORDS_KEY, Tag.TAG_COMPOUND)
			: new ListTag();

		CompoundTag coord = new CompoundTag();
		coord.putInt("x", pos.getX());
		coord.putInt("y", pos.getY());
		coord.putInt("z", pos.getZ());
		coords.add(coord);
		compound.put(COORDS_KEY, coords);
	}

	public static void launchRemotes(Level level, ItemStack freq, Player player)
	{
		CompoundTag compound = StackUtil.getOrCreateNbtData(freq);
		if (!compound.contains(COORDS_KEY, Tag.TAG_LIST))
		{
			return;
		}

		ListTag coords = compound.getList(COORDS_KEY, Tag.TAG_COMPOUND);
		int i = 0;
		while (i < coords.size())
		{
			CompoundTag coord = coords.getCompound(i);
			BlockPos pos = new BlockPos(coord.getInt("x"), coord.getInt("y"), coord.getInt("z"));
			if (level.isLoaded(pos))
			{
				BlockState state = level.getBlockState(pos);
				if (state.is(Ic2rBlocks.DYNAMITE) && state.getValue(BlockDynamite.LINKED))
				{
					((BlockDynamite) state.getBlock()).detonate(level, pos, player);
				}

				coords.remove(i);
			}
			else
			{
				i++;
			}
		}

		if (coords.isEmpty())
		{
			compound.remove(COORDS_KEY);
		}
		else
		{
			compound.put(COORDS_KEY, coords);
		}
	}

	public static int hasRemote(BlockPos pos, ItemStack freq)
	{
		CompoundTag compound = StackUtil.getOrCreateNbtData(freq);
		if (!compound.contains(COORDS_KEY, Tag.TAG_LIST))
		{
			return -1;
		}

		ListTag coords = compound.getList(COORDS_KEY, Tag.TAG_COMPOUND);
		for (int i = 0; i < coords.size(); i++)
		{
			CompoundTag coord = coords.getCompound(i);
			if (coord.getInt("x") == pos.getX() && coord.getInt("y") == pos.getY() && coord.getInt("z") == pos.getZ())
			{
				return i;
			}
		}

		return -1;
	}

	public static void removeRemote(int index, ItemStack freq)
	{
		CompoundTag compound = StackUtil.getOrCreateNbtData(freq);
		if (!compound.contains(COORDS_KEY, Tag.TAG_LIST))
		{
			return;
		}

		ListTag coords = compound.getList(COORDS_KEY, Tag.TAG_COMPOUND);
		ListTag newCoords = new ListTag();
		for (int i = 0; i < coords.size(); i++)
		{
			if (i != index)
			{
				newCoords.add(coords.get(i));
			}
		}

		if (newCoords.isEmpty())
		{
			compound.remove(COORDS_KEY);
		}
		else
		{
			compound.put(COORDS_KEY, newCoords);
		}
	}

	public static int getLinkedCount(ItemStack freq)
	{
		CompoundTag compound = StackUtil.getOrCreateNbtData(freq);
		if (!compound.contains(COORDS_KEY, Tag.TAG_LIST))
		{
			return 0;
		}

		return compound.getList(COORDS_KEY, Tag.TAG_COMPOUND).size();
	}
}
