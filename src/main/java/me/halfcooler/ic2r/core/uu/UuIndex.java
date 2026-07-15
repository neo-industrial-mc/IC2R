package me.halfcooler.ic2r.core.uu;

import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.init.IC2RUuMatterConfig;
import me.halfcooler.ic2r.core.util.ConfigUtil;
import me.halfcooler.ic2r.core.util.LogCategory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.ItemStack;

/**
 * Entry point for UU values used by scanner / replicator / pattern storage.
 * <p>
 * Seeds come solely from {@code config/ic2r/ic2r-uu-matter.toml}; finite values for craftable
 * items also come from recipe resolvers registered in {@link #init()} and applied during
 * {@link #refresh(boolean)}. Refresh must run after the server (and its
 * {@link net.minecraft.world.item.crafting.RecipeManager}) is available.
 */
public class UuIndex
{
	public static final UuIndex instance = new UuIndex();
	protected final List<IRecipeResolver> resolvers = new ArrayList<>();
	protected final List<ILateRecipeResolver> lateResolvers = new ArrayList<>();
	private boolean resolversRegistered;

	private UuIndex()
	{
	}

	public void addResolver(IRecipeResolver resolver)
	{
		this.resolvers.add(resolver);
	}

	public void addResolver(ILateRecipeResolver resolver)
	{
		this.lateResolvers.add(resolver);
	}

	public void add(ItemStack stack, double value)
	{
		if (stack != null)
		{
			UuGraph.set(stack, value);
		} else
		{
			throw new NullPointerException("invalid item stack to add");
		}
	}

	public double get(ItemStack request)
	{
		return UuGraph.get(request);
	}

	public double getInBuckets(ItemStack request)
	{
		double ret = UuGraph.get(request);
		return ret * 1.0E-5;
	}

	public double getReplicationEu(ItemStack request)
	{
		return UuGraph.get(request);
	}

	/**
	 * Register recipe resolvers once. Safe to call before recipes are loaded;
	 * actual graph build is {@link #refresh(boolean)}.
	 */
	public void init()
	{
		if (this.resolversRegistered)
		{
			return;
		}

		// Vanilla crafting & smelting
		this.addResolver(new RecipeResolver());
		this.addResolver(new VanillaSmeltingResolver());

		// IC2 machine recipes (resolved lazily via RecipeManager when a Level exists)
		this.addResolver(new MachineRecipeResolver(Recipes.macerator));
		this.addResolver(new MachineRecipeResolver(Recipes.extractor));
		this.addResolver(new MachineRecipeResolver(Recipes.compressor));
		this.addResolver(new MachineRecipeResolver(Recipes.centrifuge));
		this.addResolver(new MachineRecipeResolver(Recipes.block_cutter));
		this.addResolver(new MachineRecipeResolver(Recipes.blast_furnace));
		this.addResolver(new MachineRecipeResolver(Recipes.metalformerExtruding));
		this.addResolver(new MachineRecipeResolver(Recipes.metalformerCutting));
		this.addResolver(new MachineRecipeResolver(Recipes.metalformerRolling));
		this.addResolver(new MachineRecipeResolver(Recipes.oreWashing));

		// Explicit non-JSON transforms (e.g. fuel rod depletion)
		this.addResolver(new ManualRecipeResolver());

		// Late: scrap from recyclable items already present in the graph
		this.addResolver(new RecyclerResolver());

		// Scrap box drops intentionally omitted: Recipes.scrapboxDrops is not initialized in this port,
		// and scrap-box edges can collapse rare-item UU values unreasonably.

		this.resolversRegistered = true;
		IC2R.log.debug(LogCategory.Uu, "Registered %d UU recipe resolvers (+ %d late).", this.resolvers.size(), this.lateResolvers.size());
	}

	/**
	 * Load seed values from {@code ic2r-uu-matter.toml} and rebuild the UU graph (including recipe propagation).
	 * Call when a server RecipeManager is available (e.g. {@code ServerStarting}).
	 */
	public void refresh(boolean reset)
	{
		if (!this.resolversRegistered)
		{
			this.init();
		}

		IC2RUuMatterConfig.load();
		Map<String, Double> matterValues = IC2RUuMatterConfig.getValues();
		if (matterValues.isEmpty())
		{
			IC2R.log.warn(LogCategory.Uu, "No UU matter values configured in %s.", IC2RUuMatterConfig.RELATIVE_PATH);
		}
		else
		{
			IC2R.log.debug(LogCategory.Uu, "Loading %d UU matter values from %s.", matterValues.size(), IC2RUuMatterConfig.RELATIVE_PATH);
			for (Map.Entry<String, Double> entry : matterValues.entrySet())
			{
				applyUuValue(entry.getKey(), entry.getValue());
			}
		}

		UuGraph.build(reset);
	}

	private void applyUuValue(String itemName, double value)
	{
		ItemStack stack;
		try
		{
			stack = ConfigUtil.asStack(itemName);
		} catch (ParseException e)
		{
			throw new RuntimeException(e);
		}

		if (stack == null)
		{
			IC2R.log.warn(LogCategory.Uu, "UU matter config: Can't find ItemStack for %s.", itemName);
			return;
		}

		if (Double.isNaN(value) || value < 0.0)
		{
			IC2R.log.warn(LogCategory.Uu, "UU matter config: Invalid value %s for %s.", value, itemName);
			return;
		}

		this.add(stack, value);
	}
}
