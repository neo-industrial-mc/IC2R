package me.halfcooler.ic2r.core.block.inherit;

import me.halfcooler.ic2r.core.block.tileentity.Ic2rSignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.NotNull;

public class Ic2rSignBlock extends StandingSignBlock
{
	public Ic2rSignBlock(Properties settings, WoodType signType)
	{
		super(settings, signType);
	}

	public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
	{
		return new Ic2rSignBlockEntity(pos, state);
	}
}
