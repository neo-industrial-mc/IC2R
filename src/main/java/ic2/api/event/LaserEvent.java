package ic2.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

public class LaserEvent extends LevelEvent implements ICancellableEvent {
  public final Entity lasershot;
  public LivingEntity owner;
  public float range;
  public float power;
  public int blockBreaks;
  public boolean explosive;
  public boolean smelt;

  public LaserEvent(
      Level world,
      Entity lasershot,
      LivingEntity owner,
      float range,
      float power,
      int blockBreaks,
      boolean explosive,
      boolean smelt) {
    super(world);
    this.lasershot = lasershot;
    this.owner = owner;
    this.range = range;
    this.power = power;
    this.blockBreaks = blockBreaks;
    this.explosive = explosive;
    this.smelt = smelt;
  }

  public static class LaserHitsEntityEvent extends LaserEvent {
    public Entity hitEntity;
    public boolean passThrough;

    public LaserHitsEntityEvent(
        Level world,
        Entity lasershot,
        LivingEntity owner,
        float range,
        float power,
        int blockBreaks,
        boolean explosive,
        boolean smelt,
        Entity hitEntity) {
      super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
      this.hitEntity = hitEntity;
    }
  }

  public static class LaserHitsBlockEvent extends LaserEvent {
    public BlockPos pos;
    public final Direction side;
    public boolean removeBlock;
    public boolean dropBlock;
    public float dropChance;

    public LaserHitsBlockEvent(
        Level world,
        Entity lasershot,
        LivingEntity owner,
        float range,
        float power,
        int blockBreaks,
        boolean explosive,
        boolean smelt,
        BlockPos pos,
        Direction side,
        float dropChance,
        boolean removeBlock,
        boolean dropBlock) {
      super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
      this.pos = pos;
      this.side = side;
      this.dropChance = dropChance;
      this.removeBlock = removeBlock;
      this.dropBlock = dropBlock;
    }
  }

  public static class LaserExplodesEvent extends LaserEvent {
    public float explosionPower;
    public float explosionDropRate;
    public float explosionEntityDamage;

    public LaserExplodesEvent(
        Level world,
        Entity lasershot,
        LivingEntity owner,
        float range,
        float power,
        int blockBreaks,
        boolean explosive,
        boolean smelt,
        float explosionPower,
        float explosionDropRate,
        float explosionEntityDamage) {
      super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
      this.explosionPower = explosionPower;
      this.explosionDropRate = explosionDropRate;
      this.explosionEntityDamage = explosionEntityDamage;
    }
  }

  public static class LaserShootEvent extends LaserEvent {
    public final ItemStack laserItem;

    public LaserShootEvent(
        Level world,
        Entity lasershot,
        LivingEntity owner,
        float range,
        float power,
        int blockBreaks,
        boolean explosive,
        boolean smelt,
        ItemStack laserItem) {
      super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
      this.laserItem = laserItem;
    }
  }
}
