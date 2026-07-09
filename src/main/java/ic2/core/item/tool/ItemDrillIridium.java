package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
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

		// 1.21: replaces Tier#getLevel(); iridium out-tiers netherite so it can harvest everything.
		public @NotNull TagKey<Block> getIncorrectBlocksForDrops()
		{
			return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
		}

		public int getEnchantmentValue()
		{
			return 20;
		}

		public @NotNull Ingredient getRepairIngredient()
		{
			return Ingredient.of(Ic2Items.IRIDIUM);
		}
	};

	public ItemDrillIridium(Properties settings)
	{
		super(settings, 800, IRIDIUM_TOOL_MATERIAL, 300000, 1000, 3, 24.0F);
	}

	@Override
	protected ItemStack getItemStack(double charge)
	{
		ItemStack ret = super.getItemStack(charge);
		// 1.21: enchantments are datapack-registered; resolve Fortune via the server's registry access.
		MinecraftServer server = IC2.envProxy.getServer();
		if (server != null)
		{
			ret.enchant(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), 3);
		}

		return ret;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		if (!world.isClientSide && IC2.keyboard.isModeSwitchKeyDown(player))
		{
			HolderLookup.RegistryLookup<Enchantment> enchants = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			Holder<Enchantment> silkTouch = enchants.getOrThrow(Enchantments.SILK_TOUCH);
			Holder<Enchantment> fortune = enchants.getOrThrow(Enchantments.FORTUNE);
			ItemStack stack = StackUtil.get(player, hand);
			if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, stack) == 0)
			{
				EnchantmentHelper.updateEnchantments(stack, mutable ->
				{
					mutable.removeIf(e -> true);
					mutable.set(silkTouch, 1);
				});
				IC2.sideProxy.messagePlayer(player, "item.ic2.mining_laser.tooltip.mode", "item.ic2.mining_laser.tooltip.mode.silkTouch");
			} else
			{
				EnchantmentHelper.updateEnchantments(stack, mutable ->
				{
					mutable.removeIf(e -> true);
					mutable.set(fortune, 3);
				});
				IC2.sideProxy.messagePlayer(player, "item.ic2.mining_laser.tooltip.mode", "item.ic2.mining_laser.tooltip.mode.normal");
			}
		}

		return super.use(world, player, hand);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context)
	{
		return IC2.keyboard.isModeSwitchKeyDown(context.getPlayer()) ? InteractionResult.PASS : super.useOn(context);
	}
}
