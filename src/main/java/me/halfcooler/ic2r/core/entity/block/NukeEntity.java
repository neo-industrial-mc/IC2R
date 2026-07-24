package me.halfcooler.ic2r.core.entity.block;

import me.halfcooler.ic2r.api.entity.block.ExplosiveEntity;
import me.halfcooler.ic2r.core.item.tool.ItemToolWrench;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rEntities;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class NukeEntity extends ExplosiveEntity
{
	public NukeEntity(Level world, double x, double y, double z, float power, int radiationRange)
	{
		super(Ic2rEntities.NUKE, world, x, y, z, 300, power, 0.05F, 1.5F, Ic2rBlocks.NUKE.get().defaultBlockState(), radiationRange);
	}

	public NukeEntity(EntityType<? extends NukeEntity> type, Level world)
	{
		this(world, 0.0, 0.0, 0.0, 1.0F, 1);
	}

	public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof ItemToolWrench wrench)
		{
			if (wrench.canTakeDamage())
			{
				if (this.level().isClientSide)
				{
					return InteractionResult.PASS;
				}

				wrench.damage(stack, 1, player, hand);
				this.discard();
				return InteractionResult.CONSUME;
			}
		}

		return InteractionResult.PASS;
	}
}
