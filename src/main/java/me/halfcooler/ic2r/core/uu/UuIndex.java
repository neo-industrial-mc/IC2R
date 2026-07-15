package me.halfcooler.ic2r.core.uu;

import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.init.IC2RUuScanConfig;
import me.halfcooler.ic2r.core.util.ConfigUtil;
import me.halfcooler.ic2r.core.util.LogCategory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;

/**
 * Entry point for UU values used by scanner / replicator / pattern storage.
 * <p>
 * Seeds come from config; finite values for craftable items come from recipe resolvers
 * registered in {@link #init()} and applied during {@link #refresh(boolean)}.
 * Refresh must run after the server (and its {@link net.minecraft.world.item.crafting.RecipeManager}) is available.
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
	 * Load seed values from config and rebuild the UU graph (including recipe propagation).
	 * Call when a server RecipeManager is available (e.g. {@code ServerStarting}).
	 */
	public void refresh(boolean reset)
	{
		if (!this.resolversRegistered)
		{
			this.init();
		}

		List<? extends String> worldScanList = IC2RUuScanConfig.values.get();
		if (worldScanList.isEmpty())
		{
			IC2R.log.warn(LogCategory.Uu, "No UU world scan values configured in ic2r-uu-scan-values.toml.");
		} else
		{
			IC2R.log.debug(LogCategory.Uu, "Loading UU world scan values from ic2r-uu-scan-values.toml.");
			for (String entry : worldScanList)
			{
				parseUuEntry(entry, "world scan");
			}
		}

		for (String entry : IC2RConfig.balance.uuValues.predefined.get())
		{
			parseUuEntry(entry, "predefined");
		}

		UuGraph.build(reset);
	}

	private void parseUuEntry(String entry, String category)
	{
		int splitPos = entry.lastIndexOf(' ');
		if (splitPos <= 0) return;

		String itemName = entry.substring(0, splitPos);
		String valueStr = entry.substring(splitPos + 1);

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
			IC2R.log.warn(LogCategory.Uu, "UU %s config: Can't find ItemStack for %s.", category, itemName);
			return;
		}

		try
		{
			this.add(stack, Double.parseDouble(valueStr));
		} catch (NumberFormatException e)
		{
			IC2R.log.warn(LogCategory.Uu, "UU %s config: Invalid value %s for %s.", category, valueStr, itemName);
		}
	}
}
