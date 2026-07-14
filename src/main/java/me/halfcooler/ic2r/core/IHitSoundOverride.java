package me.halfcooler.ic2r.core;


import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IHitSoundOverride
{
	SoundEvent getHitSoundForBlock(LocalPlayer var1, Level var2, BlockPos var3, ItemStack var4);

	SoundEvent getBreakSoundForBlock(LocalPlayer var1, Level var2, BlockPos var3, ItemStack var4);
}
