package me.halfcooler.ic2r.datagen;

import java.util.concurrent.CompletableFuture;

import me.halfcooler.ic2r.core.ref.Ic2rItemTags;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Item-tag DataGen (W2.5): tool tags previously handwritten under
 * {@code data/ic2r/tags/items/}. Content is intentionally identical to the old JSON.
 * Block-tag lookup comes from {@link Ic2rBlockTagsProvider} (G2.6) for future {@code copy()}.
 */
public final class Ic2rItemTagsProvider extends ItemTagsProvider
{
	public Ic2rItemTagsProvider(
		PackOutput output,
		CompletableFuture<HolderLookup.Provider> lookupProvider,
		CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
		ExistingFileHelper existingFileHelper
	)
	{
		super(output, lookupProvider, blockTags, "ic2r", existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider)
	{
		// data/ic2r/tags/items/wrenches.json
		tag(Ic2rItemTags.WRENCHES)
			.add(Ic2rItems.WRENCH, Ic2rItems.ELECTRIC_WRENCH);

		// data/ic2r/tags/items/forge_hammers.json
		tag(Ic2rItemTags.FORGE_HAMMERS)
			.add(Ic2rItems.FORGE_HAMMER);

		// data/ic2r/tags/items/wire_cutters.json
		tag(Ic2rItemTags.WIRE_CUTTERS)
			.add(Ic2rItems.CUTTER);
	}
}
