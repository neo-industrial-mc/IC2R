package me.halfcooler.ic2r.core.item.upgrade;

import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicHandHeldContainer;
import me.halfcooler.ic2r.core.gui.dynamic.GuiParser;
import me.halfcooler.ic2r.core.gui.dynamic.IGuiConditionProvider;
import me.halfcooler.ic2r.core.gui.dynamic.IHolographicSlotProvider;
import me.halfcooler.ic2r.core.item.ContainerHandHeldInventory;
import me.halfcooler.ic2r.core.item.tool.HandHeldInventory;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HandHeldAdvancedUpgrade extends HandHeldInventory implements IHolographicSlotProvider, IGuiConditionProvider
{
	public static final int ENERGY_GUI = 2;
	private static final int ORE_GUI = 3;
	private static final ResourceLocation GUI_XML = IC2R.getIdentifier("advanced_upgrade");
	@GuiSynced
	protected boolean nbtMatch;
	@GuiSynced
	protected boolean energy;

	public HandHeldAdvancedUpgrade(Player player, InteractionHand hand, ItemStack containerStack)
	{
		super(player, hand, checkContainerStack(player, containerStack), 9);
		CompoundTag nbt = StackUtil.getOrCreateNbtData(containerStack);
		this.nbtMatch = NbtSettings.getFromNBT(getTag(nbt, "nbt").getByte("type")).enabled();
		this.energy = readTag(nbt);
	}

	private static ItemStack checkContainerStack(Player player, ItemStack containerStack)
	{
		if (!player.getCommandSenderWorld().isClientSide
			&& player.containerMenu instanceof ContainerHandHeldInventory
			&& ((ContainerHandHeldInventory<?>) player.containerMenu).base instanceof HandHeldUpgradeOption)
		{
			addMaintainedPlayer(player);
			return ((ContainerHandHeldInventory<?>) player.containerMenu).base.getContainerStack();
		} else
		{
			return containerStack;
		}
	}

	public static CompoundTag getTag(CompoundTag nbt, String name)
	{
		return nbt.getCompound(name + "Settings");
	}

	protected static boolean readTag(CompoundTag nbt)
	{
		return getTag(nbt, "energy").getBoolean("active");
	}
	protected static void writeEnergyTag(CompoundTag nbt, boolean active)
	{
		CompoundTag tag = getTag(nbt, "energy");
		tag.putBoolean("active", active);
		if (active && !tag.contains("type", 1))
		{
			tag.putByte("type", ComparisonType.DIRECT.getForNBT());
		}
		nbt.put("energySettings", tag);
	}

	protected static void writeNbtMatchTag(CompoundTag nbt, boolean exactMatch)
	{
		NbtSettings setting = exactMatch ? NbtSettings.EXACT : NbtSettings.IGNORED;
		CompoundTag tag = getTag(nbt, "nbt");
		tag.putBoolean("active", setting.enabled());
		tag.putByte("type", setting.getForNBT());
		nbt.put("nbtSettings", tag);
	}
	public static boolean isEnergyMatchEnabled(ItemStack stack)
	{
		return readTag(StackUtil.getOrCreateNbtData(stack));
	}
	public static void openEnergyConfig(Player player, InteractionHand hand, ItemStack stack)
	{
		new HandHeldValueConfig(new HandHeldAdvancedUpgrade(player, hand, stack), "energy")
			.openManagedItem(player, hand, ENERGY_GUI);
	}

	static IHasGui delegate(Player player, InteractionHand hand, ItemStack stack, int ID)
	{
		return switch (ID)
		{
			case ENERGY_GUI -> new HandHeldValueConfig(new HandHeldAdvancedUpgrade(player, hand, stack), "energy");
			case ORE_GUI -> new HandHeldOre(new HandHeldAdvancedUpgrade(player, hand, stack));
			default ->
			{
				IC2R.log.warn(LogCategory.Network, "Unexpected delegate ID: " + ID);
				yield null;
			}
		};
	}

	@Override
	protected void save()
	{
		super.save();
		if (IC2R.sideProxy.isSimulating())
		{
			CompoundTag nbt = StackUtil.getTag(this.containerStack);
			assert nbt != null;
			// Drop legacy meta settings if present
			nbt.remove("metaSettings");
			writeNbtMatchTag(nbt, this.nbtMatch);
			writeEnergyTag(nbt, this.energy);
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicHandHeldContainer.create(Ic2rScreenHandlers.ADVANCED_UPGRADE, syncId, player.getInventory(), this, this.getNode());
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicHandHeldContainer.create(Ic2rScreenHandlers.ADVANCED_UPGRADE, syncId, this.player.getInventory(), this, this.getNode());
	}

	protected GuiParser.GuiNode getNode()
	{
		return GuiParser.parse(GUI_XML, HandHeldAdvancedUpgrade.class);
	}

	Player getPlayer()
	{
		return this.player;
	}

	InteractionHand getHand()
	{
		return this.hand;
	}

	@Override
	public ItemStack[] getStacksForName(String name)
	{
		if ("filter".equals(name))
		{
			return this.inventory;
		} else
		{
			throw new IllegalArgumentException("Unexpected stack array name requested: " + name);
		}
	}

	@Override
	public boolean getGuiState(String name)
	{
		// NBT indicator only when NBT is on AND EU is off (EU makes NBT inactive)
		if ("nbt".equals(name))
		{
			return this.nbtMatch && !this.energy;
		}

		if ("energy".equals(name))
		{
			return this.energy;
		}

		// E button: open advanced EU comparison while EU Match is enabled
		if ("energyAdvanced".equals(name))
		{
			return this.energy;
		}

		if ("dev".equals(name))
		{
			return Util.inDev();
		}

		throw new IllegalArgumentException("Unexpected conditional name requested: " + name);
	}

	@Override
	public void onEvent(String event)
	{
		boolean dev = false;
		if (Util.inDev() && event.endsWith("Dev"))
		{
			dev = true;
			event = event.substring(0, event.lastIndexOf("Dev"));
		}

		// DynamicGui invokes onEvent on both client (optimistic UI) and server (via container event).
		// openManagedItem must only run on the server — LocalPlayer cannot open a MenuProvider.
		boolean server = IC2R.sideProxy.isSimulating();

		if ("nbt".equals(event))
		{
			// While EU Match is on, NBT Match is inactive — do not toggle it from the main UI
			if (this.energy)
			{
				return;
			}
			this.nbtMatch = !this.nbtMatch;
			if (server)
			{
				this.save();
			}
		} else if ("energyAdvanced".equals(event))
		{
			// "E" button: advanced EU comparison (only while EU Match is on)
			if (server && this.energy)
			{
				new HandHeldValueConfig(this, "energy").openManagedItem(this.player, this.hand, ENERGY_GUI);
			}
		} else if ("energy".equals(event))
		{
			if (dev)
			{
				if (server)
				{
					new HandHeldValueConfig(this, "energy").openManagedItem(this.player, this.hand, ENERGY_GUI);
				}
			} else
			{
				this.energy = !this.energy;
				if (server)
				{
					this.save();
				}
			}
		} else if ("ore".equals(event))
		{
			if (server)
			{
				assert dev;
				new HandHeldOre(this).openManagedItem(this.player, this.hand, ORE_GUI);
			}
		} else
		{
			super.onEvent(event);
		}
	}
}
