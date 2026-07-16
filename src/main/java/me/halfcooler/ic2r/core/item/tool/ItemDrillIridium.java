package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.StackUtil;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

@NotClassic
public class ItemDrillIridium extends ItemDrill
{
	private static final Tier IRIDIUM_TOOL_MATERIAL = new Tier()
	{
		public int getUses()
		{
			return 3000;
		}

		public float getSpeed()
		{
			return 15.0F;
		}

		public float getAttackDamageBonus()
		{
			return 5.0F;
		}

		@Override
		public TagKey<Block> getIncorrectBlocksForDrops()
		{
			return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
		}

		public int getEnchantmentValue()
		{
			return 20;
		}

		public @NotNull Ingredient getRepairIngredient()
		{
			return Ingredient.of(Ic2rItems.IRIDIUM);
		}
	};

	public ItemDrillIridium(Properties settings)
	{
		super(settings, 800, IRIDIUM_TOOL_MATERIAL, 300000, 1000, 3, 24.0F);
	}

	private static HolderLookup.RegistryLookup<Enchantment> enchantments(Level level)
	{
		return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
	}

	private static HolderLookup.RegistryLookup<Enchantment> enchantments(ItemStack stack)
	{
		// Prefer stack's level if present via components is not available; use client/server player world when using.
		// For creative-tab stack construction without a level, fall back to empty enchantments.
		return null;
	}

	private static void setExclusiveEnchant(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup, Holder<Enchantment> enchantment, int level)
	{
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		mutable.set(enchantment, level);
		EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());
	}

	@Override
	protected ItemStack getItemStack(double charge)
	{
		ItemStack ret = super.getItemStack(charge);
		// Enchantments require registry holders; applied when first used in a level context.
		// Keep unenchanted here to avoid needing a world during item registration.
		return ret;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand)
	{
		if (!world.isClientSide && IC2R.keyboard.isModeSwitchKeyDown(player))
		{
			ItemStack stack = StackUtil.get(player, hand);
			HolderLookup.RegistryLookup<Enchantment> lookup = enchantments(world);
			Holder<Enchantment> silk = lookup.getOrThrow(Enchantments.SILK_TOUCH);
			Holder<Enchantment> fortune = lookup.getOrThrow(Enchantments.FORTUNE);

			if (EnchantmentHelper.getItemEnchantmentLevel(silk, stack) == 0)
			{
				setExclusiveEnchant(stack, lookup, silk, 1);
				IC2R.sideProxy.messagePlayer(player, Component.translatable("item.ic2r.mining_laser.tooltip.mode", Component.translatable("item.ic2r.mining_laser.tooltip.mode.silkTouch")));
			} else
			{
				setExclusiveEnchant(stack, lookup, fortune, 3);
				IC2R.sideProxy.messagePlayer(player, Component.translatable("item.ic2r.mining_laser.tooltip.mode", Component.translatable("item.ic2r.mining_laser.tooltip.mode.normal")));
			}
		}

		return super.use(world, player, hand);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context)
	{
		return IC2R.keyboard.isModeSwitchKeyDown(context.getPlayer()) ? InteractionResult.PASS : super.useOn(context);
	}
}
