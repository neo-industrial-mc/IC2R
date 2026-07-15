package ic2.core.sound;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public class SoundClient extends Sound {
  /** Ticks to wait for streamed/looping sounds to become active before treating them as dead. */
  private static final int PLAY_GRACE_TICKS = 40;

  private final List<SoundInstance> startedSoundList = new CopyOnWriteArrayList<>();
  public net.minecraft.client.sounds.SoundManager vanillaManager =
      Minecraft.getInstance().getSoundManager();
  private RepeatablePositionedSoundInstance repeatInstance = null;
  private PositionedSoundInstance onceInstance = null;
  private EntityTrackingSoundInstance entityTrackingInstance = null;
  private SoundEvent soundEvent;
  private SoundSource soundCategory;
  private BlockPos pos;
  private LivingEntity entity;
  private float volume;
  private float pitch;
  private Item sourceItem;
  private boolean isStarted = false;
  private int playGraceTicks = 0;

  protected SoundClient() {}

  public SoundClient(
      SoundEvent soundEvent, SoundSource soundCategory, BlockPos pos, float volume, float pitch) {
    this();
    this.soundEvent = soundEvent;
    this.soundCategory = soundCategory;
    this.pos = pos;
    this.volume = volume;
    this.pitch = pitch;
    this.repeatInstance =
        new RepeatablePositionedSoundInstance(soundEvent, soundCategory, volume, pitch, pos);
    this.onceInstance = new PositionedSoundInstance(soundEvent, soundCategory, volume, pitch, pos);
  }

  public SoundClient(
      SoundEvent soundEvent,
      SoundSource soundCategory,
      LivingEntity entity,
      float volume,
      float pitch) {
    this();
    this.soundEvent = soundEvent;
    this.soundCategory = soundCategory;
    this.entity = entity;
    this.volume = volume;
    this.pitch = pitch;
    this.entityTrackingInstance =
        new EntityTrackingSoundInstance(soundEvent, soundCategory, volume, pitch, entity);
  }

  public void setSourceItem(Item item) {
    this.sourceItem = item;
    if (this.entityTrackingInstance != null) {
      this.entityTrackingInstance.setSourceItem(item);
    }
  }

  @Override
  public void play() {
    super.play();
    this.isStarted = true;
    DeferredSoundOps.run(
        () -> {
          if (this.repeatInstance != null) {
            if (this.shouldKeepCurrentInstance(this.repeatInstance)) {
              return;
            }

            this.stopAllStarted();
            this.repeatInstance =
                new RepeatablePositionedSoundInstance(
                    this.soundEvent, this.soundCategory, this.volume, this.pitch, this.pos);
            this.vanillaManager.play(this.repeatInstance);
            this.startedSoundList.add(this.repeatInstance);
            this.playGraceTicks = PLAY_GRACE_TICKS;
          }

          if (this.entityTrackingInstance != null) {
            if (this.shouldKeepCurrentInstance(this.entityTrackingInstance)) {
              return;
            }

            this.stopAllStarted();
            this.entityTrackingInstance =
                new EntityTrackingSoundInstance(
                    this.soundEvent, this.soundCategory, this.volume, this.pitch, this.entity);
            if (this.sourceItem != null) {
              this.entityTrackingInstance.setSourceItem(this.sourceItem);
            }

            this.vanillaManager.play(this.entityTrackingInstance);
            this.startedSoundList.add(this.entityTrackingInstance);
            this.playGraceTicks = PLAY_GRACE_TICKS;
          }
        });
  }

  /**
   * Keep the current instance when it is already active, or still within the post-play grace window
   * (streamed sounds may report inactive while buffering). Never leave untracked orphans.
   */
  private boolean shouldKeepCurrentInstance(SoundInstance instance) {
    if (instance == null) {
      return false;
    }

    if (this.vanillaManager.isActive(instance)) {
      return true;
    }

    return this.playGraceTicks > 0 && this.startedSoundList.contains(instance);
  }

  @Override
  public void playOnce() {
    super.playOnce();
    this.isStarted = true;
    DeferredSoundOps.run(
        () -> {
          if (this.onceInstance != null) {
            this.stopInstance(this.onceInstance);
            this.onceInstance =
                new PositionedSoundInstance(
                    this.soundEvent, this.soundCategory, this.volume, this.pitch, this.pos);
            this.vanillaManager.play(this.onceInstance);
            this.startedSoundList.add(this.onceInstance);
          }

          if (this.entityTrackingInstance != null) {
            this.entityTrackingInstance.playOnce();
            this.startedSoundList.add(this.entityTrackingInstance);
          }
        });
  }

  @Override
  public void stop() {
    super.stop();
    this.isStarted = false;
    this.playGraceTicks = 0;
    DeferredSoundOps.run(this::stopAllStarted);
  }

  private void stopAllStarted() {
    for (SoundInstance instance : this.startedSoundList) {
      if (instance != null) {
        this.vanillaManager.stop(instance);
      }
    }
    this.startedSoundList.clear();
    this.stopInstance(this.repeatInstance);
    this.stopInstance(this.onceInstance);
    this.stopInstance(this.entityTrackingInstance);
  }

  private void stopInstance(SoundInstance instance) {
    if (instance != null) {
      this.vanillaManager.stop(instance);
      this.startedSoundList.remove(instance);
    }
  }

  private boolean isPlayingSound(SoundInstance instance) {
    return instance != null && this.vanillaManager.isActive(instance);
  }

  @Override
  public boolean isPlaying() {
    if (this.isPlayingSound(this.onceInstance)
        || this.isPlayingSound(this.repeatInstance)
        || this.isPlayingSound(this.entityTrackingInstance)) {
      return true;
    }

    // Treat grace-window instances as playing so callers do not spam play() and spawn orphans.
    return this.isStarted && this.playGraceTicks > 0 && !this.startedSoundList.isEmpty();
  }

  private void addOnFinishListener(ListenableSoundInstance instance, Runnable then) {
    if (instance != null) {
      instance.addOnFinishListener(then);
    }
  }

  private void onFinishSound(ListenableSoundInstance instance, Runnable then) {
    if (instance != null) {
      instance.onFinish(then);
    }
  }

  private void checkSoundFinished(SoundInstance instance) {
    if (!(instance instanceof ListenableSoundInstance listenableSoundInstance)) {
      return;
    }

    if (!this.startedSoundList.contains(instance) || this.vanillaManager.isActive(instance)) {
      return;
    }

    // Streamed / looping sounds may not report active immediately after play().
    if (this.playGraceTicks > 0) {
      return;
    }

    boolean looping =
        instance instanceof RepeatablePositionedSoundInstance
            || instance instanceof EntityTrackingSoundInstance;
    if (looping && this.isStarted) {
      // Engine dropped the loop while we still want it — clear tracking so play() can restart.
      // Do not run finish listeners (those are for intentional start→loop transitions).
      this.startedSoundList.remove(instance);
      return;
    }

    listenableSoundInstance.finish();
    this.startedSoundList.remove(instance);
  }

  @Override
  public void addOnFinishListener(Runnable then) {
    this.addOnFinishListener(this.onceInstance, then);
    this.addOnFinishListener(this.repeatInstance, then);
    this.addOnFinishListener(this.entityTrackingInstance, then);
  }

  @Override
  public void onFinish(Runnable then) {
    this.onFinishSound(this.onceInstance, then);
    this.onFinishSound(this.repeatInstance, then);
    this.onFinishSound(this.entityTrackingInstance, then);
  }

  public void tick() {
    super.tick();
    if (this.playGraceTicks > 0) {
      this.playGraceTicks--;
    }

    if (this.isStarted) {
      this.checkSoundFinished(this.onceInstance);
      this.checkSoundFinished(this.repeatInstance);
      this.checkSoundFinished(this.entityTrackingInstance);
      if (this.startedSoundList.isEmpty()
          && !this.isPlayingSound(this.onceInstance)
          && !this.isPlayingSound(this.repeatInstance)
          && !this.isPlayingSound(this.entityTrackingInstance)
          && this.playGraceTicks <= 0) {
        this.isStarted = false;
      }
    }
  }
}
