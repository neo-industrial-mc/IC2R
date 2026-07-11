package ic2.core.entity;

import ic2.api.event.LaserEvent;
import ic2.core.IC2;
import ic2.core.Ic2Explosion;
import ic2.core.Ic2Player;
import ic2.core.ref.Ic2Entities;
import ic2.core.util.StackUtil;
import ic2.core.util.Vector3;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

public class LaserBulletEntity extends ThrowableProjectile {
  public LivingEntity owner;
  public boolean isSmeltMode = false;
  public boolean removeBlock = false;
  public float range = 0.0F;
  public float power = 0.0F;
  public int blockBreaks = 0;
  public boolean isExplosiveMode = false;
  private boolean hitThisTick = false;

  public LaserBulletEntity(Level world) {
    super(Ic2Entities.LASER_BULLET, world);
  }

  public LaserBulletEntity(EntityType<? extends LaserBulletEntity> arg, Level arg2) {
    super(arg, arg2);
  }

  public LaserBulletEntity(Level world, LivingEntity owner) {
    super(Ic2Entities.LASER_BULLET, owner, world);
  }

  public LaserBulletEntity(
      Level world,
      Vector3 start,
      LivingEntity owner,
      float range,
      float power,
      int blockBreaks,
      boolean isExplosiveMode) {
    this(world, owner);
    this.owner = owner;
    this.absMoveTo(start.x, start.y, start.z);
    this.range = range;
    this.power = power;
    this.blockBreaks = blockBreaks;
    this.isExplosiveMode = isExplosiveMode;
  }

  protected double getDefaultGravity() {
    return 0.0;
  }

  protected void defineSynchedData(SynchedEntityData.Builder builder) {}

  public void tick() {
    if (IC2.sideProxy.isSimulating()
        && (this.range < 1.0F || this.power <= 0.0F || this.blockBreaks <= 0)) {
      if (this.isExplosiveMode) {
        this.explode();
      }

      this.remove(RemovalReason.DISCARDED);
      return;
    }

    double oldX = this.getX();
    double oldY = this.getY();
    double oldZ = this.getZ();
    this.hitThisTick = false;
    super.tick();

    if (!this.isRemoved()) {
      double distance =
          this.position().distanceTo(new net.minecraft.world.phys.Vec3(oldX, oldY, oldZ));
      this.range = (float) (this.range - distance);
      if (!this.hitThisTick) {
        this.power -= 0.5F;
      }

      if (this.isInWater()) {
        this.remove(RemovalReason.DISCARDED);
      }
    }
  }

  protected void onHitBlock(@NotNull BlockHitResult blockHitResult) {
    super.onHitBlock(blockHitResult);
    this.handleHit(blockHitResult);
  }

  protected void onHitEntity(@NotNull EntityHitResult entityHitResult) {
    super.onHitEntity(entityHitResult);
    this.handleHit(entityHitResult);
  }

  protected void handleHit(HitResult hitResult) {
    this.hitThisTick = true;
    if (this.isExplosiveMode) {
      this.explode();
      this.remove(RemovalReason.DISCARDED);
    } else {
      switch (hitResult.getType()) {
        case ENTITY:
          if (this.hitEntity(((EntityHitResult) hitResult).getEntity())) {
            this.remove(RemovalReason.DISCARDED);
          } else {
            this.power -= 0.5F;
          }
          break;
        case BLOCK:
          assert hitResult instanceof BlockHitResult;
          BlockHitResult blockHitResult = (BlockHitResult) hitResult;
          if (!this.hitBlock(blockHitResult.getBlockPos(), blockHitResult.getDirection())) {
            this.power -= 0.5F;
          }
          break;
        default:
          throw new RuntimeException("invalid hit type: " + hitResult.getType());
      }
    }
  }

  private void explode() {
    Level world = this.getCommandSenderWorld();
    LaserEvent.LaserExplodesEvent event =
        NeoForge.EVENT_BUS.post(
            new LaserEvent.LaserExplodesEvent(
                world,
                this,
                this.owner,
                this.range,
                this.power,
                this.blockBreaks,
                this.isExplosiveMode,
                this.isSmeltMode,
                5.0F,
                0.85F,
                0.55F));
    if (event.isCanceled()) {
      return;
    }

    this.copyDataFromEvent(event);
    Ic2Explosion explosion =
        new Ic2Explosion(
            world,
            this,
            this.getX(),
            this.getY(),
            this.getZ(),
            event.explosionPower,
            event.explosionDropRate);
    explosion.doExplosion();
  }

  private boolean hitEntity(Entity entity) {
    LaserEvent.LaserHitsEntityEvent event =
        NeoForge.EVENT_BUS.post(
            new LaserEvent.LaserHitsEntityEvent(
                this.level(),
                this,
                this.owner,
                this.range,
                this.power,
                this.blockBreaks,
                this.isExplosiveMode,
                this.isSmeltMode,
                entity));
    if (event.isCanceled()) {
      return !event.passThrough;
    }

    this.copyDataFromEvent(event);
    entity = event.hitEntity;
    int damage = (int) this.power;
    if (this.isBossEntity(entity)) {
      damage = Math.min(damage, 5);
    }

    if (damage > 0) {
      entity.igniteForSeconds(damage * (this.isSmeltMode ? 2 : 1));
      return entity.hurt(this.level().damageSources().mobProjectile(this, this.owner), damage);
    } else {
      return true;
    }
  }

  private boolean isBossEntity(Entity entity) {
    return entity instanceof EnderDragon || entity instanceof WitherBoss;
  }

  private boolean hitBlock(BlockPos pos, Direction side) {
    Level world = this.getCommandSenderWorld();
    LaserEvent.LaserHitsBlockEvent event =
        NeoForge.EVENT_BUS.post(
            new LaserEvent.LaserHitsBlockEvent(
                world,
                this,
                this.owner,
                this.range,
                this.power,
                this.blockBreaks,
                this.isExplosiveMode,
                this.isSmeltMode,
                pos,
                side,
                0.9F,
                true,
                true));
    if (event.isCanceled()) {
      this.remove(RemovalReason.DISCARDED);
      return true;
    }

    this.copyDataFromEvent(event);
    pos = event.pos;
    Player playerOwner = this.owner instanceof Player ? (Player) this.owner : Ic2Player.get(world);
    if (playerOwner == null) {
      return false;
    }

    if (playerOwner.blockActionRestricted(
        world, pos, Objects.requireNonNull(playerOwner.getServer()).getDefaultGameType())) {
      return false;
    }

    BlockState state = world.getBlockState(pos);
    Block block = state.getBlock();
    if (NeoForge.EVENT_BUS
        .post(new BlockEvent.BreakEvent(world, pos, state, playerOwner))
        .isCanceled()) {
      this.remove(RemovalReason.DISCARDED);
      return true;
    }

    if (world.getBlockState(pos).isAir()
        || block == Blocks.GLASS
        || block == Blocks.GLASS_PANE
        || block instanceof StainedGlassPaneBlock
        || block instanceof StainedGlassBlock) {
      return false;
    }

    if (world.isClientSide) {
      return true;
    }

    float hardness = state.getDestroySpeed(world, pos);
    if (hardness < 0.0F) {
      this.remove(RemovalReason.DISCARDED);
      return true;
    }

    this.power -= hardness / 1.5F;
    if (this.power < 0.0F) {
      return true;
    }

    List<ItemStack> replacements = new ArrayList<>();
    if (block == Blocks.TNT) {
      block.wasExploded(
          world,
          pos,
          new Explosion(
              world,
              this,
              pos.getX() + 0.5,
              pos.getY() + 0.5,
              pos.getZ() + 0.5,
              1.0F,
              false,
              BlockInteraction.DESTROY));
    } else if (this.isSmeltMode) {
      if (state.isFlammable(world, pos, side)) {
        event.dropBlock = false;
      } else {
        for (ItemStack isa : StackUtil.getDrops(world, pos, state, block, 0)) {
          this.appendSmeltItemStack(block, isa, replacements);
        }

        event.dropBlock = replacements.isEmpty();
      }
    }

    if (event.removeBlock) {
      if (event.dropBlock) {
        this.dropBlockWithChance(world, pos, state, block, event.dropChance);
      }

      world.removeBlock(pos, false);

      for (ItemStack replacement : replacements) {
        if (!StackUtil.placeBlock(replacement, world, pos)) {
          StackUtil.dropAsEntity(world, pos, replacement);
        }

        this.power = 0.0F;
      }

      if (world.random.nextInt(10) == 0 && state.isFlammable(world, pos, Direction.UP)) {
        world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
      }
    }

    this.blockBreaks--;
    return true;
  }

  private void dropBlockWithChance(
      Level world, BlockPos pos, BlockState state, Block block, float dropChance) {
    for (ItemStack drop : StackUtil.getDrops(world, pos, state, block, 0)) {
      if (world.random.nextFloat() <= dropChance) {
        StackUtil.dropAsEntity(world, pos, drop);
      }
    }
  }

  private void appendSmeltItemStack(
      Block targetBlock, ItemStack inputItemStack, List<ItemStack> replacementList) {
    if (inputItemStack.getItem() instanceof BlockItem
        && ((BlockItem) inputItemStack.getItem()).getBlock() != targetBlock) {
      inputItemStack = new ItemStack(targetBlock.asItem());
    }

    SmeltingRecipe recipe =
        IC2.sideProxy
            .getRecipeManager()
            .getRecipeFor(
                RecipeType.SMELTING,
                new net.minecraft.world.item.crafting.SingleRecipeInput(inputItemStack),
                this.getCommandSenderWorld())
            .map(net.minecraft.world.item.crafting.RecipeHolder::value)
            .orElse(null);
    if (recipe != null) {
      ItemStack replacementStack =
          recipe.getResultItem(this.getCommandSenderWorld().registryAccess());
      if (!StackUtil.isEmpty(replacementStack)) {
        replacementList.add(replacementStack);
      }
    }
  }

  public void init(
      LivingEntity owner,
      float range,
      float power,
      int blockBreaks,
      boolean explosive,
      boolean smelt,
      boolean removeBlock) {
    this.owner = owner;
    this.range = range;
    this.power = power;
    this.blockBreaks = blockBreaks;
    this.removeBlock = removeBlock;
    this.isExplosiveMode = explosive;
    this.isSmeltMode = smelt;
  }

  public void copyDataFromEvent(LaserEvent event) {
    this.owner = event.owner;
    this.range = event.range;
    this.power = event.power;
    this.blockBreaks = event.blockBreaks;
    this.isExplosiveMode = event.explosive;
    this.isSmeltMode = event.smelt;
  }
}
