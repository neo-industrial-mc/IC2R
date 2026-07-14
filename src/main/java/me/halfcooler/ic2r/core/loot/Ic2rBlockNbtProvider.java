package me.halfcooler.ic2r.core.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;

import java.util.Set;

import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import org.jetbrains.annotations.Nullable;

public class Ic2rBlockNbtProvider implements NbtProvider
{
	public static final Ic2rBlockNbtProvider BLOCK_NBT = new Ic2rBlockNbtProvider();

	@Nullable
	public Tag get(LootContext context)
	{
		BlockEntity blockEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
		BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
		if (state == null)
		{
			return null;
		}

		if (blockEntity instanceof Ic2rTileEntity tileEntity)
		{
			ItemStack stack = tileEntity.adjustDrop(state.getBlock().asItem().getDefaultInstance(), false);
			if (stack.hasTag())
			{
				return stack.getTag();
			}
		}

		return null;
	}

	public Set<LootContextParam<?>> getReferencedContextParams()
	{
		return ImmutableSet.of();
	}

	public LootNbtProviderType getType()
	{
		return Ic2rLootNbtProviderTypes.BLOCK_NBT;
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<Ic2rBlockNbtProvider>
	{
		public void serialize(JsonObject jsonObject, Ic2rBlockNbtProvider ic2BlockNbtProvider, JsonSerializationContext jsonSerializationContext)
		{
		}

		public Ic2rBlockNbtProvider deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext)
		{
			return new Ic2rBlockNbtProvider();
		}
	}
}
