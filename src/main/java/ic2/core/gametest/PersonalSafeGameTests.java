package ic2.core.gametest;

import com.mojang.authlib.GameProfile;
import ic2.core.block.personal.TileEntityPersonalChest;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.item.tool.ItemToolWrench;
import ic2.core.ref.Ic2Blocks;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class PersonalSafeGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final BlockPos SAFE_POS = new BlockPos(1, 1, 1);

  private static final UUID OWNER_ID = UUID.fromString("81e6e380-2b21-46ff-8672-52b530e2373f");

  // pre-1.20.5 saves stored the owner as NbtUtils.writeGameProfile data ("Id"/"Name"); dropping
  // it on load leaves the safe ownerless, so the first wrench click — anyone's — claims and
  // removes it, while the real owner has no other way to break the -1-hardness block
  @GameTest(template = EMPTY)
  public static void legacyOwnerNbtKeepsSafeOwnedAndStrangerProof(GameTestHelper helper) {
    TileEntityPersonalChest safe = placeSafe(helper);
    CompoundTag saved = safe.saveWithFullMetadata(helper.getLevel().registryAccess());
    CompoundTag legacyOwner = new CompoundTag();
    legacyOwner.putString("Id", OWNER_ID.toString());
    legacyOwner.putString("Name", "SafeOwner");
    saved.put("ownerGameProfile", legacyOwner);

    safe.loadWithComponents(saved, helper.getLevel().registryAccess());

    GameProfile owner = safe.getOwner();
    helper.assertTrue(owner != null, "loading legacy owner NBT must not lose the owner");
    helper.assertValueEqual(owner.getId(), OWNER_ID, "owner id after legacy load");

    ServerPlayer stranger = helper.makeMockServerPlayerInLevel();
    ItemToolWrench.WrenchResult result = wrenchSafe(helper, stranger);

    helper.assertValueEqual(result, ItemToolWrench.WrenchResult.Nothing, "stranger wrench result");
    helper.assertBlockPresent(Ic2Blocks.PERSONAL_CHEST, SAFE_POS);
    helper.assertValueEqual(
        safe.getOwner().getId(), OWNER_ID, "owner id after stranger wrench attempt");
    helper.succeed();
  }

  // fake players such as "[SomeMod]" can claim a safe by accessing it, and their names fail the
  // strict codec's player-name validation — saving must not throw and must keep the owner
  @GameTest(template = EMPTY)
  public static void fakePlayerOwnerSurvivesSaveAndReload(GameTestHelper helper) {
    TileEntityPersonalChest safe = placeSafe(helper);
    safe.setOwner(new GameProfile(OWNER_ID, "[SomeModFakePlayer]"));

    CompoundTag saved = safe.saveWithFullMetadata(helper.getLevel().registryAccess());
    safe.setOwner(null);
    safe.loadWithComponents(saved, helper.getLevel().registryAccess());

    GameProfile owner = safe.getOwner();
    helper.assertTrue(owner != null, "fake-player owner must survive save and reload");
    helper.assertValueEqual(owner.getId(), OWNER_ID, "owner id after reload");
    helper.assertValueEqual(owner.getName(), "[SomeModFakePlayer]", "owner name after reload");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void ownerCanWrenchEmptySafeAfterReload(GameTestHelper helper) {
    TileEntityPersonalChest safe = placeSafe(helper);
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    safe.setOwner(owner.getGameProfile());

    CompoundTag saved = safe.saveWithFullMetadata(helper.getLevel().registryAccess());
    safe.setOwner(null);
    safe.loadWithComponents(saved, helper.getLevel().registryAccess());

    ItemToolWrench.WrenchResult result = wrenchSafe(helper, owner);

    helper.assertValueEqual(result, ItemToolWrench.WrenchResult.Removed, "owner wrench result");
    helper.succeedWhen(
        () -> {
          helper.assertBlockPresent(Blocks.AIR, SAFE_POS);
          helper.assertItemEntityPresent(Ic2Blocks.PERSONAL_CHEST.asItem(), SAFE_POS, 2.0);
        });
  }

  private static TileEntityPersonalChest placeSafe(GameTestHelper helper) {
    helper.setBlock(SAFE_POS, Ic2Blocks.PERSONAL_CHEST);
    BlockEntity be = helper.getBlockEntity(SAFE_POS);
    if (!(be instanceof TileEntityPersonalChest)) {
      helper.fail("expected a personal safe at " + SAFE_POS + ", found " + be);
    }

    return (TileEntityPersonalChest) be;
  }

  private static ItemToolWrench.WrenchResult wrenchSafe(
      GameTestHelper helper, ServerPlayer player) {
    BlockPos absolutePos = helper.absolutePos(SAFE_POS);
    // clicking the side the safe already faces skips the rotation step and attempts removal
    Direction facing =
        ((Ic2TileEntityBlock) Ic2Blocks.PERSONAL_CHEST).getFacing(helper.getLevel(), absolutePos);
    return ItemToolWrench.wrenchBlock(helper.getLevel(), absolutePos, facing, player, true);
  }
}
