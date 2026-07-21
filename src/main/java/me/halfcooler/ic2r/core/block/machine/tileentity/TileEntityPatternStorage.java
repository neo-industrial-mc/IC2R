package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.network.INetworkClientTileEntityEventListener;
import me.halfcooler.ic2r.api.recipe.IPatternStorage;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableId;
import me.halfcooler.ic2r.core.block.machine.container.ContainerPatternStorage;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.item.ItemCrystalMemory;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.uu.UuIndex;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;
import me.halfcooler.ic2r.core.util.LegacyItemStackNbt;

@NotClassic
public class TileEntityPatternStorage extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener, IPatternStorage
{
	public final InvSlotConsumableId diskSlot;
	private final List<ItemStack> patterns = new ArrayList<>();
	public int index = 0;
	public int maxIndex;
	public ItemStack pattern;
	public double patternUu;
	public double patternEu;

	public TileEntityPatternStorage(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.PATTERN_STORAGE, pos, state);
		this.diskSlot = new InvSlotConsumableId(this, "SaveSlot", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, Ic2rItems.CRYSTAL_MEMORY);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.readContents(nbt);
	}

	@Override
	public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.saveAdditional(nbt, registries);
		this.writeContentsAsNbtList(nbt);
	}

	@Override
	public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing)
	{
		super.onPlaced(stack, placer, facing);
		if (!this.getLevel().isClientSide)
		{
			CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
			this.readContents(nbt);
		}
	}

	@Override
	public ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		drop = super.adjustDrop(drop, wrench);
		if (wrench || this.teBlock.getDefaultDrop() == Ic2rTileEntityBlock.DefaultDrop.Self)
		{
			CompoundTag nbt = StackUtil.getOrCreateNbtData(drop);
			this.writeContentsAsNbtList(nbt);
		}

		return drop;
	}

	public void readContents(CompoundTag nbt)
	{
		ListTag patternList = nbt.getList("patterns", 10);

		for (int i = 0; i < patternList.size(); i++)
		{
			CompoundTag contentTag = patternList.getCompound(i);
			ItemStack Item = LegacyItemStackNbt.parseOptional(null, contentTag);
			this.addPattern(Item);
		}

		this.refreshInfo();
	}

	private void writeContentsAsNbtList(CompoundTag nbt)
	{
		ListTag list = new ListTag();

		for (ItemStack stack : this.patterns)
		{
			CompoundTag contentTag = new CompoundTag();
			LegacyItemStackNbt.saveInto(null, stack, contentTag);
			list.add(contentTag);
		}

		nbt.put("patterns", list);
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerPatternStorage(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerPatternStorage(syncId, inventory, this);
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		switch (event)
		{
			case 0:
				if (!this.patterns.isEmpty())
				{
					if (this.index <= 0)
					{
						this.index = this.patterns.size() - 1;
					} else
					{
						this.index--;
					}

					this.refreshInfo();
				}
				break;
			case 1:
				if (!this.patterns.isEmpty())
				{
					if (this.index >= this.patterns.size() - 1)
					{
						this.index = 0;
					} else
					{
						this.index++;
					}

					this.refreshInfo();
				}
				break;
			case 2:
				if (this.index >= 0 && this.index < this.patterns.size() && !this.diskSlot.isEmpty())
				{
					ItemStack crystalMemory = this.diskSlot.get();
					if (crystalMemory.getItem() instanceof ItemCrystalMemory)
					{
						((ItemCrystalMemory) crystalMemory.getItem()).writeContentsTag(crystalMemory, this.patterns.get(this.index));
					}
				}
				break;
			case 3:
				if (!this.diskSlot.isEmpty())
				{
					ItemStack crystalMemory = this.diskSlot.get();
					if (crystalMemory.getItem() instanceof ItemCrystalMemory)
					{
						ItemStack record = ((ItemCrystalMemory) crystalMemory.getItem()).readItemStack(crystalMemory);
						if (record != null)
						{
							this.addPattern(record);
						}
					}
				}
		}
	}

	public void refreshInfo()
	{
		if (this.index < 0 || this.index >= this.patterns.size())
		{
			this.index = 0;
		}

		this.maxIndex = this.patterns.size();
		if (this.patterns.isEmpty())
		{
			this.pattern = null;
		} else
		{
			this.pattern = this.patterns.get(this.index);
			this.patternUu = UuIndex.instance.getInBuckets(this.pattern);
			this.patternEu = UuIndex.instance.getReplicationEu(this.pattern);
		}
	}

	@Override
	public boolean addPattern(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			throw new IllegalArgumentException("empty stack: " + StackUtil.toStringSafe(stack));
		}

		for (ItemStack pattern : this.patterns)
		{
			if (StackUtil.checkItemEquality(pattern, stack))
			{
				return false;
			}
		}

		this.patterns.add(stack);
		this.refreshInfo();
		return true;
	}

	@Override
	public List<ItemStack> getPatterns()
	{
		return this.patterns;
	}
}
