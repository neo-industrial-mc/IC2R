package me.halfcooler.ic2r.core.gui;

import com.mojang.authlib.GameProfile;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.StringUtils;

public class PlayerHead extends ItemImage
{
	private static final Map<GameProfile, ItemStack> IMAGE_MAKER = Collections.synchronizedMap(new WeakHashMap<>());
	private final GameProfile player;

	public PlayerHead(Ic2rGui<?> gui, int x, int y, GameProfile player)
	{
		super(gui, x, y, new PlayerHead.PlayerHeadSupplier(player));
		this.player = player;
	}

	@Override
	protected List<Component> getToolTip()
	{
		List<Component> tooltip = super.getToolTip();
		if (StringUtils.isNotBlank(this.player.getName()))
		{
			tooltip.add(Component.literal(this.player.getName()));
		}

		return tooltip;
	}

	private record PlayerHeadSupplier(GameProfile profile) implements Supplier<ItemStack>
		{

			public ItemStack get()
			{
				CompletableFuture<GameProfile> future = new CompletableFuture<>();
				SkullBlockEntity.updateGameprofile(this.profile, future::complete);
	
				try
				{
					return PlayerHead.IMAGE_MAKER.computeIfAbsent(future.get(), resolvedProfile ->
					{
						ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
						StackUtil.getOrCreateNbtData(skull).put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), resolvedProfile));
						return skull;
					});
				} catch (InterruptedException | ExecutionException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
}
