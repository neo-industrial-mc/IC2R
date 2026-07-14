package me.halfcooler.ic2r.core.item.armor.jetpack;

import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.api.item.IBackupElectricItemManager;
import me.halfcooler.ic2r.api.item.IElectricItem;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class JetpackHandler implements IBackupElectricItemManager
{
	private static ItemStack jetpackCache;
	private static final Map<Player, ItemStack> playerArmorBuffer = new WeakHashMap<>();

	static ItemStack getJetpackStack()
	{
		if (jetpackCache == null)
		{
			jetpackCache = new ItemStack(Ic2rItems.JETPACK_ELECTRIC);
		}
		return jetpackCache;
	}
	public static JetpackHandler instance;
	private boolean internalHandlesCheck = false;

	private JetpackHandler()
	{
		ElectricItem.registerBackupManager(this);
	}

	public static void init()
	{
		if (instance == null)
		{
			instance = new JetpackHandler();
		}
	}

	public static void onPlayerTick(Player player)
	{
		ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
		if (hasJetpack(stack))
		{
			JetpackLogic.onArmorTick(player.level(), player, stack, getJetpack(stack));
		} else
		{
			// Only the local client player owns the jetpack loop sound.
			JetpackLogic.stopJetpackSound(player);
		}
	}

	public static void setJetpackAttached(ItemStack stack, boolean value)
	{
		if (StackUtil.isEmpty(stack))
		{
			return;
		}

		if (!value)
		{
			if (!stack.hasTag())
			{
				return;
			}

			stack.getTag().remove("hasIC2RJetpack");
			if (stack.getTag().isEmpty())
			{
				stack.setTag(null);
			}
		} else if (Mob.getEquipmentSlotForItem(stack) == EquipmentSlot.CHEST)
		{
			StackUtil.getOrCreateNbtData(stack).putBoolean("hasIC2RJetpack", true);
		}
	}

	public static boolean hasJetpackAttached(ItemStack stack)
	{
		return !StackUtil.isEmpty(stack) && Mob.getEquipmentSlotForItem(stack) == EquipmentSlot.CHEST && stack.hasTag() && stack.getTag().getBoolean("hasIC2RJetpack");
	}

	public static boolean hasJetpack(ItemStack stack)
	{
		return !StackUtil.isEmpty(stack) && (hasJetpackAttached(stack) || stack.getItem() instanceof IJetpack);
	}

	public static IJetpack getJetpack(ItemStack stack)
	{
		assert hasJetpack(stack);
		return stack.getItem() instanceof IJetpack ? (IJetpack) stack.getItem() : (IJetpack) getJetpackStack().getItem();
	}

	public static double getTransferLimit()
	{
		return ((IElectricItem) getJetpackStack().getItem()).getTransferLimit(getJetpackStack());
	}

	@Override
	public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		if (this.getTier(stack) > tier)
		{
			return 0.0;
		}

		if (!ignoreTransferLimit)
		{
			amount = Math.min(amount, getTransferLimit());
		}

		double charge = stack.hasTag() ? stack.getTag().getDouble("charge") : 0.0;
		amount = Math.min(amount, this.getMaxCharge(stack) - charge);
		if (!simulate)
		{
			StackUtil.getOrCreateNbtData(stack).putDouble("charge", charge + amount);
		}

		return amount;
	}

	@Override
	public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate)
	{
		if (!externally && this.getTier(stack) <= tier && stack.hasTag())
		{
			if (!ignoreTransferLimit)
			{
				amount = Math.min(amount, getTransferLimit());
			}

			double charge = stack.getTag().getDouble("charge");
			amount = Math.min(amount, charge);
			if (!simulate)
			{
				charge -= amount;
				if (charge == 0.0)
				{
					stack.getTag().remove("charge");
					if (stack.getTag().isEmpty())
					{
						stack.setTag(null);
					}
				} else
				{
					stack.getTag().putDouble("charge", charge);
				}
			}

			return amount;
		} else
		{
			return 0.0;
		}
	}

	@Override
	public double getCharge(ItemStack stack)
	{
		return this.discharge(stack, Double.MAX_VALUE, Integer.MAX_VALUE, true, false, true);
	}

	@Override
	public double getStackCharge(ItemStack stack)
	{
		return this.getCharge(stack);
	}

	@Override
	public double getMaxCharge(ItemStack stack)
	{
		return ElectricItem.manager.getMaxCharge(getJetpackStack().copy());
	}

	@Override
	public boolean canUse(ItemStack stack, double amount)
	{
		return ElectricItem.rawManager.canUse(stack, amount);
	}

	@Override
	public boolean use(ItemStack stack, double amount, LivingEntity entity)
	{
		return ElectricItem.rawManager.use(stack, amount, entity);
	}

	@Override
	public void chargeFromArmor(ItemStack stack, LivingEntity entity)
	{
	}

	@Override
	public String getToolTip(ItemStack stack)
	{
		return ElectricItem.rawManager.getToolTip(stack);
	}

	@Override
	public int getTier(ItemStack stack)
	{
		return ElectricItem.manager.getTier(getJetpackStack().copy());
	}

	@Override
	public synchronized boolean handles(ItemStack stack)
	{
		if (this.internalHandlesCheck)
		{
			return false;
		}

		this.internalHandlesCheck = true;
		boolean handle = hasJetpackAttached(stack) && ElectricItem.manager.getMaxCharge(stack) <= 0.0;
		this.internalHandlesCheck = false;
		return handle;
	}

	/** Called by JetpackHandlerForge on player tick START. */
	public static void restoreArmorIfBuffered(Player player)
	{
		if (playerArmorBuffer.containsKey(player))
		{
			ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
			ItemStack lastStack = playerArmorBuffer.get(player);
			if (!StackUtil.isEmpty(lastStack) && hasJetpackAttached(lastStack) && StackUtil.isEmpty(stack))
			{
				ItemStack newJetpack = getJetpackStack().copy();
				double oldCharge = ElectricItem.manager.getCharge(lastStack);
				ElectricItem.manager.charge(newJetpack, oldCharge, Integer.MAX_VALUE, true, false);
				player.setItemSlot(EquipmentSlot.CHEST, newJetpack);
			}
			playerArmorBuffer.remove(player);
		}
	}

	/** Called by JetpackHandlerForge for jetpack tooltip injection. */
	public static void addJetpackTooltip(ItemStack stack, java.util.List<Component> tip)
	{
		if (hasJetpackAttached(stack))
		{
			Ic2rTooltip.add(tip, Component.translatable("ic2r.jetpackAttached").withStyle(ChatFormatting.YELLOW));
			String energyTooltip = ElectricItem.manager.getToolTip(stack);
			if (energyTooltip != null && !energyTooltip.trim().isEmpty())
			{
				Ic2rTooltip.add(tip, Component.literal(energyTooltip));
			}
		}
	}

	/** Called by JetpackHandlerForge on equipment slot change. */
	public static void onEquipmentChanged(LivingEntity entity)
	{
		if (!(entity instanceof Player player))
		{
			return;
		}
		JetpackLogic.stopJetpackSound(player);
	}

	/** Called by JetpackHandlerForge before damage; buffers chest armor for potential restore. */
	public static void bufferChestArmor(Player player)
	{
		ItemStack currentArmor = player.getItemBySlot(EquipmentSlot.CHEST);
		if (hasJetpackAttached(currentArmor))
		{
			playerArmorBuffer.put(player, currentArmor);
		}
	}


}
