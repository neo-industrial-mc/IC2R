package me.halfcooler.ic2r.core.block.misc;

import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class FoamBlock extends Block
{
    public static final com.mojang.serialization.MapCodec<FoamBlock> CODEC = simpleCodec(FoamBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.Block> codec() {
        return CODEC;
    }

	public static final EnumProperty<FoamBlock.FoamType> typeProperty = EnumProperty.create("type", FoamBlock.FoamType.class);

	public FoamBlock(Properties settings)
	{
		super(settings);
		this.registerDefaultState(this.defaultBlockState().setValue(typeProperty, FoamType.normal));
	}

	public static float getHardenChance(Level world, BlockPos pos, BlockState state, FoamBlock.FoamType type)
	{
		int light = world.getMaxLocalRawBrightness(pos);
		if (state.getLightBlock(world, pos) == 0)
		{
			for (Direction side : Util.ALL_DIRS)
			{
				light = Math.max(light, world.getMaxLocalRawBrightness(pos.relative(side)));
			}
		}

		int avgTime = type.hardenTime * (16 - light);
		return 1.0F / (avgTime * 20);
	}

	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(typeProperty);
	}

	public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context)
	{
		return Shapes.empty();
	}

	public void randomTick(@NotNull BlockState state, ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource random)
	{
		int tickSpeed = world.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
		if (tickSpeed <= 0)
		{
			throw new IllegalStateException("Foam was randomly ticked when world " + world + " is not ticking?");
		}

		FoamBlock.FoamType type = state.getValue(typeProperty);
		float chance = getHardenChance(world, pos, state, type) * 4096.0F / tickSpeed;
		if (random.nextFloat() < chance)
		{
			world.setBlockAndUpdate(pos, state.getValue(typeProperty).getResult());
		}
	}

	@Override
	protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit)
	{
		// Sand-in-hand path: useItemOn default PASS_TO_DEFAULT then useWithoutItem (main hand only)
		if (StackUtil.consume(player, InteractionHand.MAIN_HAND, StackUtil.sameItem(Blocks.SAND), 1))
		{
			world.setBlockAndUpdate(pos, state.getValue(typeProperty).getResult());
			return InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	@Override
	protected @NotNull net.minecraft.world.ItemInteractionResult useItemOn(
		@NotNull ItemStack stack,
		@NotNull BlockState state,
		@NotNull Level world,
		@NotNull BlockPos pos,
		@NotNull Player player,
		@NotNull InteractionHand hand,
		@NotNull BlockHitResult hit)
	{
		if (StackUtil.consume(player, hand, StackUtil.sameItem(Blocks.SAND), 1))
		{
			world.setBlockAndUpdate(pos, state.getValue(typeProperty).getResult());
			return net.minecraft.world.ItemInteractionResult.sidedSuccess(world.isClientSide);
		}
		return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	public enum FoamType implements StringRepresentable
	{
		normal(300),
		reinforced(600);

		public final int hardenTime;

		FoamType(int hardenTime)
		{
			this.hardenTime = hardenTime;
		}

		public @NotNull String getSerializedName()
		{
			return this.name();
		}

		public List<ItemStack> getDrops()
		{
			return switch (this)
			{
				case normal, reinforced -> new ArrayList<>();
			};
		}

		public BlockState getResult()
		{
			return switch (this)
			{
				case normal -> WallBlock.get(WallBlock.DEFAULT_COLOR).defaultBlockState();
				case reinforced -> Ic2rBlocks.REINFORCED_STONE.get().defaultBlockState();
			};
		}
	}
}
