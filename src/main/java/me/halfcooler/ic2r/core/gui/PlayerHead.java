package me.halfcooler.ic2r.core.gui;

import com.mojang.authlib.GameProfile;
import me.halfcooler.ic2r.core.Ic2rGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

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
		private static GameProfile resolve(GameProfile profile) throws InterruptedException, ExecutionException
		{
			if (profile.getId() != null)
			{
				Optional<GameProfile> byId = SkullBlockEntity.fetchGameProfile(profile.getId()).get();
				if (byId.isPresent())
				{
					return byId.get();
				}
			}
			if (StringUtils.isNotBlank(profile.getName()))
			{
				Optional<GameProfile> byName = SkullBlockEntity.fetchGameProfile(profile.getName()).get();
				if (byName.isPresent())
				{
					return byName.get();
				}
			}
			return profile;
		}

		public ItemStack get()
		{
			try
			{
				GameProfile resolved = resolve(this.profile);
				return PlayerHead.IMAGE_MAKER.computeIfAbsent(resolved, resolvedProfile ->
				{
					ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
					skull.set(DataComponents.PROFILE, new ResolvableProfile(resolvedProfile));
					return skull;
				});
			} catch (InterruptedException | ExecutionException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
