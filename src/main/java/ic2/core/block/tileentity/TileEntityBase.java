package ic2.core.block.tileentity;

import ic2.core.IC2;
import ic2.core.sound.Sound;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TileEntityBase extends TileEntityInventory {
  protected Sound loopingSound;
  protected Sound subLoopingSound;
  protected Sound startSound;
  protected Sound stopSound;
  protected Sound interruptSound;
  private boolean clientLastActive;
  private boolean playInterruptOnDeactivate;

  public TileEntityBase(
      BlockEntityType<? extends TileEntityInventory> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    // Looping sound playback is driven client-side; on a dedicated server IC2.soundManager is a
    // no-op
    // so loopingSound is null and this restart logic never ran. updateEntityClient now handles
    // persistence.
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  protected void updateEntityClient() {
    super.updateEntityClient();
    this.syncLoopingSounds();
  }

  @OnlyIn(Dist.CLIENT)
  private void syncLoopingSounds() {
    this.initSound();
    boolean shouldPlay = this.shouldSoundActive();
    boolean shouldPlaySub = this.shouldSubSoundActive();

    if (this.loopingSound != null) {
      if (shouldPlay && this.isLoopingSoundIdling() && !this.isStartSoundPlaying()) {
        this.playLoopingSound(false);
      } else if (!shouldPlay && this.loopingSound.isPlaying()) {
        this.loopingSound.stop();
      }
    }

    if (this.subLoopingSound != null) {
      if (shouldPlaySub && !this.subLoopingSound.isPlaying()) {
        this.subLoopingSound.play();
      } else if (!shouldPlaySub && this.subLoopingSound.isPlaying()) {
        this.subLoopingSound.stop();
      }
    }
  }

  @OnlyIn(Dist.CLIENT)
  private void onActiveFieldUpdated() {
    this.initSound();
    boolean nowActive = this.shouldSoundActive();
    if (nowActive != this.clientLastActive) {
      this.clientLastActive = nowActive;
      if (nowActive) {
        this.startPlaySound(false);
      } else {
        this.handleClientDeactivation();
      }
    } else if (nowActive && this.isLoopingSoundIdling() && !this.isStartSoundPlaying()) {
      this.playLoopingSound(false);
    }
  }

  @Override
  public void onNetworkUpdate(String field) {
    super.onNetworkUpdate(field);
    if (this.level == null || !this.level.isClientSide) {
      return;
    }

    if (field.equals("active")) {
      this.onActiveFieldUpdated();
    } else if (field.equals("playInterruptOnDeactivate")
        && this.playInterruptOnDeactivate
        && !this.shouldSoundActive()) {
      this.handleClientDeactivation();
    }
  }

  @OnlyIn(Dist.CLIENT)
  private void handleClientDeactivation() {
    this.initSound();
    this.stopStartSound();
    this.stopLoopingSound();
    if (this.playInterruptOnDeactivate) {
      this.playInterruptSound();
      this.playInterruptOnDeactivate = false;
    } else {
      this.playStopSound();
    }
  }

  @Override
  protected void onUnloaded() {
    if (this.hasSound()) {
      IC2.soundManager.removeAllSound(this);
      this.clearSound();
    }

    super.onUnloaded();
  }

  @Override
  protected void onLoaded() {
    this.initSound();
    super.onLoaded();
  }

  protected boolean shouldSoundActive() {
    if (!this.teBlock.canActive()) {
      return false;
    }

    if (this.getActive()) {
      return true;
    }

    return this.getBlockState().getValue(Ic2TileEntityBlock.ACTIVE);
  }

  protected boolean shouldSubSoundActive() {
    return this.shouldSoundActive();
  }

  private boolean isStartSoundPlaying() {
    return this.startSound != null && this.startSound.isPlaying();
  }

  public void setActiveState(boolean active, boolean playSubSound) {
    if (active) {
      this.activate(playSubSound);
    } else {
      this.shutdown(false);
    }
  }

  public void activate(boolean playSubSound) {
    if (!this.getActive()) {
      this.teBlock.setActive(this.level, this.worldPosition, this.getBlockState(), true);
      if (this.level != null && this.level.isClientSide) {
        this.startPlaySound(playSubSound);
      }
    } else if (this.level != null && this.level.isClientSide && this.isLoopingSoundIdling()) {
      this.playLoopingSound(playSubSound);
    }
  }

  public void shutdown(boolean isInterrupted) {
    if (!this.getActive()) {
      return;
    }

    boolean useInterrupt = isInterrupted && this.getInterruptSoundEvent() != null;
    if (useInterrupt) {
      this.playInterruptOnDeactivate = true;
      if (this.level != null && !this.level.isClientSide) {
        IC2.network.get(true).updateTileEntityField(this, "playInterruptOnDeactivate");
      }
    } else {
      this.playInterruptOnDeactivate = false;
    }

    this.teBlock.setActive(this.level, this.worldPosition, this.getBlockState(), false);

    if (this.level != null && this.level.isClientSide) {
      this.handleClientDeactivation();
    }
  }

  public void startPlaySound(boolean playSubSound) {
    if (this.startSound != null) {
      if (this.loopingSound != null) {
        this.startSound.addOnFinishListener(() -> this.playLoopingSound(playSubSound));
      }

      this.startSound.playOnce();
    } else {
      this.playLoopingSound(playSubSound);
    }
  }

  public void playLoopingSound(boolean playSubSound) {
    if (this.loopingSound != null) {
      this.loopingSound.play();
      if (playSubSound && this.subLoopingSound != null) {
        this.subLoopingSound.play();
      }
    }
  }

  public void stopLoopingSound() {
    if (this.loopingSound != null) {
      this.loopingSound.stop();
    }

    if (this.subLoopingSound != null) {
      this.subLoopingSound.stop();
    }
  }

  public void stopStartSound() {
    if (this.startSound != null) {
      this.startSound.stop();
    }
  }

  public void playStopSound() {
    if (this.stopSound != null) {
      this.stopSound.playOnce();
    }
  }

  public void playInterruptSound() {
    if (this.interruptSound != null) {
      this.interruptSound.playOnce();
    }
  }

  protected boolean hasSound() {
    return this.startSound != null
        || this.loopingSound != null
        || this.stopSound != null
        || this.interruptSound != null;
  }

  protected void initSound() {
    this.updateStartSound();
    this.updateLoopingSound();
    this.updateSubLoopingSound();
    this.updateInterruptSound();
    this.updateStopSound();
  }

  protected void updateStartSound() {
    SoundEvent startSoundEvent = this.getStartSoundEvent();
    if (startSoundEvent != null && this.startSound == null) {
      this.startSound =
          IC2.soundManager.createSound(
              this, startSoundEvent, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
    }
  }

  protected void updateStopSound() {
    SoundEvent stopSoundEvent = this.getStopSoundEvent();
    if (stopSoundEvent != null && this.stopSound == null) {
      this.stopSound =
          IC2.soundManager.createSound(
              this, stopSoundEvent, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
    }
  }

  protected void updateLoopingSound() {
    SoundEvent loopingSoundEvent = this.getLoopingSoundEvent();
    if (loopingSoundEvent != null && this.loopingSound == null) {
      this.loopingSound =
          IC2.soundManager.createSound(
              this, loopingSoundEvent, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
    }
  }

  protected void updateSubLoopingSound() {
    SoundEvent loopingSoundEvent = this.getSubLoopingSoundEvent();
    if (loopingSoundEvent != null && this.subLoopingSound == null) {
      this.subLoopingSound =
          IC2.soundManager.createSound(
              this, loopingSoundEvent, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
    }
  }

  protected void updateInterruptSound() {
    SoundEvent interruptSoundEvent = this.getInterruptSoundEvent();
    if (interruptSoundEvent != null && this.interruptSound == null) {
      this.interruptSound =
          IC2.soundManager.createSound(
              this, interruptSoundEvent, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
    }
  }

  protected void clearSound() {
    this.startSound = null;
    this.loopingSound = null;
    this.subLoopingSound = null;
    this.stopSound = null;
    this.interruptSound = null;
  }

  protected boolean isLoopingSoundIdling() {
    return this.loopingSound != null && !this.loopingSound.isPlaying();
  }

  public SoundEvent getStartSoundEvent() {
    return null;
  }

  public SoundEvent getLoopingSoundEvent() {
    return null;
  }

  public SoundEvent getSubLoopingSoundEvent() {
    return null;
  }

  public SoundEvent getStopSoundEvent() {
    return null;
  }

  public SoundEvent getInterruptSoundEvent() {
    return null;
  }
}
