package ic2.core.uu;

import ic2.core.IC2;
import ic2.core.init.IC2Config;
import ic2.core.init.IC2UuScanConfig;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;

public class UuIndex
{
	public static final UuIndex instance = new UuIndex();
	protected final List<IRecipeResolver> resolvers = new ArrayList<>();
	protected final List<ILateRecipeResolver> lateResolvers = new ArrayList<>();

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

	public void init()
	{
	}

	public void refresh(boolean reset)
	{
		List<? extends String> worldScanList = IC2UuScanConfig.values.get();
		if (worldScanList.isEmpty())
		{
			IC2.log.warn(LogCategory.Uu, "No UU world scan values configured in ic2-uu-scan-values.toml.");
		} else
		{
			IC2.log.debug(LogCategory.Uu, "Loading UU world scan values from ic2-uu-scan-values.toml.");
			for (String entry : worldScanList)
			{
				parseUuEntry(entry, "world scan");
			}
		}

		for (String entry : IC2Config.balance.uuValues.predefined.get())
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
			IC2.log.warn(LogCategory.Uu, "UU %s config: Can't find ItemStack for %s.", category, itemName);
			return;
		}

		try
		{
			this.add(stack, Double.parseDouble(valueStr));
		} catch (NumberFormatException e)
		{
			IC2.log.warn(LogCategory.Uu, "UU %s config: Invalid value %s for %s.", category, valueStr, itemName);
		}
	}

}
