package me.halfcooler.ic2r.core.item.armor.jetpack;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.item.armor.ItemArmorJetpack;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.sound.Sound;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class JetpackLogic
{
	private static Sound jetpackSound;
	private static SoundEvent jetpackSoundEvent;

	public static boolean useJetpack(Player player, boolean hoverMode, IJetpack jetpack, ItemStack stack)
	{

		if (jetpack.getChargeLevel(stack) <= 0.0)
		{
			return false;
		}

		IBoostingJetpack iBoostingJetpack = jetpack instanceof IBoostingJetpack ? (IBoostingJetpack) jetpack : null;
		float power = jetpack.getPower(stack);
		float dropPercentage = jetpack.getDropPercentage(stack);
		if (jetpack.getChargeLevel(stack) <= dropPercentage)
		{
			power = (float) (power * (jetpack.getChargeLevel(stack) / dropPercentage));
		}

		Level world = player.level();
		Vec3 motion = player.getDeltaMovement();
		double motionX = motion.x;
		double motionY = motion.y;
		double motionZ = motion.z;

		if (IC2R.keyboard.isForwardKeyDown(player))
		{
			float thruster, boost;
			if (iBoostingJetpack != null)
			{
				thruster = iBoostingJetpack.getBaseThrust(stack, hoverMode);
				boost = iBoostingJetpack.getBoostThrust(player, stack, hoverMode);
			} else
			{
				thruster = hoverMode ? 1.0F : 0.15F;
				boost = 0.0F;
			}

			float forwarder = power * thruster * 2.0F;
			if (forwarder > 0.0F)
			{
				float yaw = player.getYRot() * (float) Math.PI / 180.0F;
				float forward = 0.4F * forwarder + boost;
				float thrust = forward * (0.02F + boost);
				motionX += -Math.sin(yaw) * thrust;
				motionZ += Math.cos(yaw) * thrust;
				if (boost != 0.0F && !player.onGround())
				{
					iBoostingJetpack.useBoostPower(stack, boost);
				}
			}
		}

		int worldHeight = world.getMaxBuildHeight();
		int maxFlightHeight = (int) (worldHeight / jetpack.getWorldHeightDivisor(stack));
		double y = player.getY();
		if (y > maxFlightHeight - 25)
		{
			if (y > maxFlightHeight)
			{
				y = maxFlightHeight;
			}

			power = (float) (power * ((maxFlightHeight - y) / 25.0));
		}

		double prevMotionY = motionY;
		motionY = Math.min(motionY + power * 0.2F, 0.6F);
		if (hoverMode)
		{
			float maxHoverY = 0.0F;
			if (IC2R.keyboard.isJumpKeyDown(player))
			{
				maxHoverY += jetpack.getHoverMultiplier(stack, true);
				if (iBoostingJetpack != null)
				{
					maxHoverY *= iBoostingJetpack.getHoverBoost(player, stack, true);
				}
			}

			if (IC2R.keyboard.isSneakKeyDown(player))
			{
				maxHoverY -= jetpack.getHoverMultiplier(stack, false);
				if (iBoostingJetpack != null)
				{
					maxHoverY *= iBoostingJetpack.getHoverBoost(player, stack, false);
				}
			}

			if (motionY > maxHoverY)
			{
				motionY = maxHoverY;
				if (prevMotionY > motionY)
				{
					motionY = prevMotionY;
				}
			}
		}

		player.setDeltaMovement(motionX, motionY, motionZ);

		int consume = hoverMode ? 1 : 2;
		if (!player.onGround())
		{
			jetpack.drainEnergy(stack, consume);
		}

		player.fallDistance = 0.0F;
		player.resetFallDistance();
		return true;
	}

	public static void onArmorTick(Level world, Player player, ItemStack stack, IJetpack jetpack)
	{
		if (stack != null && jetpack.isJetpackActive(stack))
		{
			CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
			boolean hoverMode = nbtData.getBoolean("hover_mode");
			byte toggleTimer = nbtData.getByte("toggle_timer");
			boolean jetpackUsed = false;

			if (IC2R.keyboard.isJumpKeyDown(player) && IC2R.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0)
			{
				toggleTimer = 10;
				hoverMode = !hoverMode;
				if (!world.isClientSide())
				{
					nbtData.putBoolean("hover_mode", hoverMode);
					if (hoverMode)
					{
						IC2R.sideProxy.messagePlayer(player, "ic2r.hover_mode.enabled");
					} else
					{
						IC2R.sideProxy.messagePlayer(player, "ic2r.hover_mode.disabled");
					}
				}
			}

			if (IC2R.keyboard.isJumpKeyDown(player) || hoverMode)
			{
				jetpackUsed = useJetpack(player, hoverMode, jetpack, stack);
				if (player.onGround() && hoverMode && !world.isClientSide())
				{
					nbtData.putBoolean("hover_mode", false);
					IC2R.sideProxy.messagePlayer(player, "ic2r.hover_mode.disabled");
				}
			}

			if (!world.isClientSide() && toggleTimer > 0)
			{
				nbtData.putByte("toggle_timer", --toggleTimer);
			}

			updateJetpackSound(player, jetpackUsed, jetpack);

			if (jetpackUsed)
			{
				player.inventoryMenu.broadcastChanges();
			}
		} else
		{
			stopJetpackSound(player);
		}
	}

	private static void updateJetpackSound(Player player, boolean jetpackUsed, IJetpack jetpack)
	{
		if (!IC2R.sideProxy.isRendering() || player != IC2R.sideProxy.getPlayerInstance())
		{
			return;
		}

		if (jetpackUsed)
		{
			SoundEvent soundEvent = jetpack instanceof ItemArmorJetpack ? Ic2rSoundEvents.ITEM_JETPACK_FIRE : Ic2rSoundEvents.ITEM_JETPACK_LOOP;
			if (jetpackSound == null || jetpackSoundEvent != soundEvent)
			{
				stopJetpackSound(player);
				jetpackSoundEvent = soundEvent;
				jetpackSound = IC2R.soundManager.createSound(player, soundEvent, SoundSource.PLAYERS, player, 1.0F, 1.0F);
				if (jetpackSound != null)
				{
					jetpackSound.play();
				}
			}
			else if (jetpackSound != null && !jetpackSound.isPlaying())
			{
				// Resume only if the engine dropped the instance; avoid re-play every tick.
				jetpackSound.play();
			}
		} else
		{
			stopJetpackSound(player);
		}
	}

	/**
	 * Stops the local jetpack loop. Safe to call for non-local players (no-op) and on disconnect.
	 */
	public static void stopJetpackSound(Player player)
	{
		if (!IC2R.sideProxy.isRendering())
		{
			return;
		}

		// Jetpack sound is a single client-global instance for the local player only.
		// Remote player ticks must not stop or clear it (multiplayer orphan / stuck-loop bug).
		Player local = IC2R.sideProxy.getPlayerInstance();
		if (player != null && local != null && player != local)
		{
			return;
		}

		if (jetpackSound != null)
		{
			Object owner = local != null ? local : player;
			if (owner != null)
			{
				IC2R.soundManager.removeSound(owner, jetpackSound);
			}
			else
			{
				jetpackSound.stop();
			}
			jetpackSound = null;
		}

		jetpackSoundEvent = null;
	}
}
