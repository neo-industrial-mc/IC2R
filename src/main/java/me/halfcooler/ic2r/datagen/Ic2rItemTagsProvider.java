package me.halfcooler.ic2r.datagen;

import me.halfcooler.ic2r.core.ref.Ic2rItemTags;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Item-tag DataGen for IC2R-private tool tags under {@code data/ic2r/tags/item/}.
 * Common {@code c:} material tags remain handwritten under {@code data/c/tags/item/}.
 * Block-tag lookup comes from {@link Ic2rBlockTagsProvider} for future {@code copy()}.
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
	protected void addTags(HolderLookup.@NotNull Provider provider)
	{
		tag(Ic2rItemTags.WRENCHES)
			.add(Ic2rItems.WRENCH, Ic2rItems.ELECTRIC_WRENCH);

		tag(Ic2rItemTags.FORGE_HAMMERS)
			.add(Ic2rItems.FORGE_HAMMER);

		tag(Ic2rItemTags.WIRE_CUTTERS)
			.add(Ic2rItems.CUTTER);
	}
}
