package me.halfcooler.ic2r.core.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Migrates pre-1.20.5 ItemStack NBT ({@code Count}/{@code tag}/{@code Damage}) into the 1.21
 * codec shape ({@code count}/{@code components}) before {@link ItemStack#parseOptional}.
 * <p>
 * World DFU only rewrites typed {@code ITEM_STACK} references. IC2R stores stacks inside custom
 * compounds ({@code InvSlots}, covers, patterns, …) that never pass through that fixer, so machines
 * loaded from 1.20.1 Forge worlds would otherwise drop inventory NBT (charged tools, fluid cells,
 * filters) even when registry aliases resolve item ids.
 * <p>
 * Structural only — pure tag rewrite for unit tests; no registry bootstrap required for
 * {@link #normalize(CompoundTag)}.
 */
public final class LegacyItemStackNbt
{
	/** Pre-componentization count key (byte or int). */
	public static final String LEGACY_COUNT = "Count";

	/** Modern ItemStack codec count key. */
	public static final String COUNT = "count";

	/** Pre-componentization free-form NBT compound. */
	public static final String LEGACY_TAG = "tag";

	/** Modern data-component map key on the stack. */
	public static final String COMPONENTS = "components";

	/** Free-form NBT surviving as {@code minecraft:custom_data}. */
	public static final String CUSTOM_DATA = "minecraft:custom_data";

	/** Pre-componentization damage (root or inside {@code tag}). */
	public static final String LEGACY_DAMAGE = "Damage";

	/** Modern damage component. */
	public static final String DAMAGE_COMPONENT = "minecraft:damage";

	private LegacyItemStackNbt()
	{
	}

	/**
	 * True when the compound still carries pre-1.20.5 stack fields that the modern codec ignores.
	 */
	public static boolean needsNormalize(CompoundTag tag)
	{
		if (tag == null || tag.isEmpty())
		{
			return false;
		}
		if (tag.contains(LEGACY_COUNT) && !tag.contains(COUNT, Tag.TAG_ANY_NUMERIC))
		{
			return true;
		}
		if (tag.contains(LEGACY_TAG, Tag.TAG_COMPOUND))
		{
			return true;
		}
		if (tag.contains(LEGACY_DAMAGE, Tag.TAG_ANY_NUMERIC) && !hasDamageComponent(tag))
		{
			return true;
		}
		return false;
	}

	/**
	 * Returns a copy of {@code tag} rewritten to the 1.21 ItemStack codec shape.
	 * Extra sibling keys ({@code Index}, {@code facing}, …) are preserved.
	 * Input is never mutated.
	 */
	public static CompoundTag normalize(CompoundTag tag)
	{
		if (tag == null)
		{
			return new CompoundTag();
		}
		if (!needsNormalize(tag))
		{
			return tag.copy();
		}

		CompoundTag out = tag.copy();

		if (!out.contains(COUNT, Tag.TAG_ANY_NUMERIC) && out.contains(LEGACY_COUNT))
		{
			Tag countTag = out.get(LEGACY_COUNT);
			if (countTag instanceof NumericTag numeric)
			{
				out.putInt(COUNT, numeric.getAsInt());
			}
			out.remove(LEGACY_COUNT);
		}
		else if (out.contains(LEGACY_COUNT))
		{
			out.remove(LEGACY_COUNT);
		}

		CompoundTag components = out.contains(COMPONENTS, Tag.TAG_COMPOUND)
			? out.getCompound(COMPONENTS)
			: new CompoundTag();

		if (out.contains(LEGACY_TAG, Tag.TAG_COMPOUND))
		{
			CompoundTag legacyTag = out.getCompound(LEGACY_TAG);
			out.remove(LEGACY_TAG);

			// Lift Damage out of free-form tag into the damage component (vanilla DFU shape).
			if (legacyTag.contains(LEGACY_DAMAGE, Tag.TAG_ANY_NUMERIC) && !components.contains(DAMAGE_COMPONENT))
			{
				components.putInt(DAMAGE_COMPONENT, legacyTag.getInt(LEGACY_DAMAGE));
				legacyTag.remove(LEGACY_DAMAGE);
			}

			if (!legacyTag.isEmpty())
			{
				if (components.contains(CUSTOM_DATA, Tag.TAG_COMPOUND))
				{
					components.getCompound(CUSTOM_DATA).merge(legacyTag);
				}
				else
				{
					components.put(CUSTOM_DATA, legacyTag);
				}
			}
		}

		if (out.contains(LEGACY_DAMAGE, Tag.TAG_ANY_NUMERIC) && !components.contains(DAMAGE_COMPONENT))
		{
			components.putInt(DAMAGE_COMPONENT, out.getInt(LEGACY_DAMAGE));
		}
		out.remove(LEGACY_DAMAGE);

		if (!components.isEmpty())
		{
			out.put(COMPONENTS, components);
		}

		return out;
	}

	/**
	 * Parse a stack from possibly-legacy NBT. Uses {@link RegistryAccess#EMPTY} when
	 * {@code registries} is null (item codec binds {@code BuiltInRegistries.ITEM} directly).
	 */
	public static ItemStack parseOptional(HolderLookup.Provider registries, CompoundTag tag)
	{
		if (tag == null || tag.isEmpty())
		{
			return ItemStack.EMPTY;
		}
		HolderLookup.Provider lookup = registries != null ? registries : RegistryAccess.EMPTY;
		return ItemStack.parseOptional(lookup, normalize(tag));
	}

	/**
	 * Save a non-empty stack into {@code output}, preserving extra sibling keys already present.
	 */
	public static void saveInto(HolderLookup.Provider registries, ItemStack stack, CompoundTag output)
	{
		if (stack == null || stack.isEmpty() || output == null)
		{
			return;
		}
		HolderLookup.Provider lookup = registries != null ? registries : RegistryAccess.EMPTY;
		Tag saved = stack.save(lookup);
		if (saved instanceof CompoundTag savedCompound)
		{
			for (String key : savedCompound.getAllKeys())
			{
				output.put(key, savedCompound.get(key));
			}
		}
	}

	private static boolean hasDamageComponent(CompoundTag tag)
	{
		return tag.contains(COMPONENTS, Tag.TAG_COMPOUND)
			&& tag.getCompound(COMPONENTS).contains(DAMAGE_COMPONENT);
	}
}
