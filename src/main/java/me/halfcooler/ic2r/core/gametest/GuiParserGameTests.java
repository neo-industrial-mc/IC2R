package me.halfcooler.ic2r.core.gametest;

import me.halfcooler.ic2r.core.block.personal.TileEntityPersonalChest;
import me.halfcooler.ic2r.core.gui.dynamic.GuiParser;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r")
@PrefixGameTestTemplate(false)
public class GuiParserGameTests {
    private static final String TEMPLATE = "gametest/empty3x3x3";

    private static final ResourceLocation PERSONAL_CHEST =
        ResourceLocation.fromNamespaceAndPath("ic2r", "personal_chest");

    // GUI definitions resolve through baseClass first so addon guidef XMLs (in the addon's own
    // module) are found; module resource lookups don't cross mod boundaries. The GuiParser.class
    // fallback keeps XMLs loadable when baseClass lives in another module.
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void guiDefsResolveThroughBaseClassAndFallback(GameTestHelper helper) {
        // same-module path: baseClass is an ic2r class, the first lookup must hit
        GuiParser.GuiNode viaOwnModule =
            GuiParser.parse(PERSONAL_CHEST, TileEntityPersonalChest.class);
        helper.assertTrue(
            viaOwnModule != null, "guidef must parse with a baseClass from the owning module");

        // cross-module miss: a minecraft-module class cannot see ic2r assets, so this only
        // succeeds if the GuiParser.class fallback still runs
        GuiParser.GuiNode viaFallback = GuiParser.parse(PERSONAL_CHEST, ChestBlockEntity.class);
        helper.assertTrue(
            viaFallback != null, "guidef must still parse via the GuiParser.class fallback");

        helper.succeed();
    }
}
