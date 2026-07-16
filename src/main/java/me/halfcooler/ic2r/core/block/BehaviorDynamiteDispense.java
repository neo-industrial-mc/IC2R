package me.halfcooler.ic2r.core.block;

import me.halfcooler.ic2r.core.entity.DynamiteEntity;
import me.halfcooler.ic2r.core.entity.StickyDynamiteEntity;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;

public final class BehaviorDynamiteDispense
{
	private BehaviorDynamiteDispense()
	{
	}

	public static void register(Item dynamite, Item stickyDynamite)
	{
		DispenserBlock.registerBehavior(dynamite, new SimpleDynamiteDispense(false));
		DispenserBlock.registerBehavior(stickyDynamite, new SimpleDynamiteDispense(true));
	}

	private static final class SimpleDynamiteDispense extends DefaultDispenseItemBehavior
	{
		private final boolean sticky;

		private SimpleDynamiteDispense(boolean sticky)
		{
			this.sticky = sticky;
		}

		@NotNull
		protected ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack)
		{
			Level level = source.level();
			Position position = DispenserBlock.getDispensePosition(source);
			Direction direction = source.state().getValue(DispenserBlock.FACING);

			DynamiteEntity entity = this.sticky
				? new StickyDynamiteEntity(level, position.x(), position.y(), position.z())
				: new DynamiteEntity(level, position.x(), position.y(), position.z());

			entity.shoot(
				direction.getStepX(),
				direction.getStepY() + 0.1F,
				direction.getStepZ(),
				1.1F,
				6.0F
			);
			level.addFreshEntity(entity);
			stack.shrink(1);
			return stack;
		}
	}
}
