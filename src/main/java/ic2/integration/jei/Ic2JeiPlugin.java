package ic2.integration.jei;

import ic2.api.recipe.IRecipeInput;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.block.machine.container.ContainerIndustrialWorkbench;
import ic2.core.block.machine.container.ContainerMetalFormer;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.gui.RecipeButton;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.recipe.v2.RecipeHolder;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2RecipeTypes;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import ic2.integration.jei.recipe.machine.CannerBottleCategory;
import ic2.integration.jei.recipe.machine.CannerBottleLiquidCategory;
import ic2.integration.jei.recipe.machine.CannerBottleLiquidRecipeWrapper;
import ic2.integration.jei.recipe.machine.CannerBottleRecipeWrapper;
import ic2.integration.jei.recipe.machine.CannerEmptyLiquidCategory;
import ic2.integration.jei.recipe.machine.CannerEmptyLiquidRecipeWrapper;
import ic2.integration.jei.recipe.machine.CannerEnrichCategory;
import ic2.integration.jei.recipe.machine.CannerEnrichRecipeWrapper;
import ic2.integration.jei.recipe.machine.DynamicCategory;
import ic2.integration.jei.recipe.machine.IORecipeWrapper;
import ic2.integration.jei.recipe.machine.MetalFormerCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JeiPlugin
public class Ic2JeiPlugin implements IModPlugin
{
	private final RecipeType<IORecipeWrapper> BLAST_FURNACE = RecipeType.create("ic2", "blast_furnace", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> BLOCK_CUTTER = RecipeType.create("ic2", "block_cutter", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> CENTRIFUGE = RecipeType.create("ic2", "centrifuge", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> COMPRESSOR = RecipeType.create("ic2", "compressor", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> EXTRACTOR = RecipeType.create("ic2", "extractor", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> MACERATOR = RecipeType.create("ic2", "macerator", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> METAL_FORMER_EXTRUDING = RecipeType.create("ic2", "metal_former_extruding", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> METAL_FORMER_ROLLING = RecipeType.create("ic2", "metal_former_rolling", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> METAL_FORMER_CUTTING = RecipeType.create("ic2", "metal_former_cutting", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> ORE_WASHER = RecipeType.create("ic2", "ore_washer", IORecipeWrapper.class);
	private final RecipeType<CannerBottleRecipeWrapper> CANNER_BOTTLE = RecipeType.create("ic2", "canner_bottle", CannerBottleRecipeWrapper.class);
	private final RecipeType<CannerEnrichRecipeWrapper> CANNER_ENRICH = RecipeType.create("ic2", "canner_enrich", CannerEnrichRecipeWrapper.class);
	private final RecipeType<CannerBottleLiquidRecipeWrapper> CANNER_BOTTLE_LIQUID = RecipeType.create("ic2", "canner_bottle_liquid", CannerBottleLiquidRecipeWrapper.class);
	private final RecipeType<CannerEmptyLiquidRecipeWrapper> CANNER_EMPTY_LIQUID = RecipeType.create("ic2", "canner_empty_liquid", CannerEmptyLiquidRecipeWrapper.class);

	private static void generateCannerFluidContainerRecipes(List<CannerEmptyLiquidRecipeWrapper> emptyLiquidRecipes, List<CannerBottleLiquidRecipeWrapper> bottleLiquidRecipes, Map<String, CannerBottleLiquidRecipeWrapper> bottleLiquidDedup)
	{
		for (Item item : ForgeRegistries.ITEMS)
		{
			ItemStack stack = new ItemStack(item);
			if (!LiquidUtil.isDrainableFluidContainer(stack))
			{
				continue;
			}

			ItemStack filledStack = stack.copy();
			LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(filledStack, null, Integer.MAX_VALUE, FluidContainerOutputMode.EmptyFullToOutput);
			if (result == null)
			{
				continue;
			}

			List<ItemStack> drainedContainers = new ArrayList<>();
			if (!StackUtil.isEmpty(result.inPlaceOutput))
			{
				drainedContainers.add(result.inPlaceOutput.copy());
			}
			if (result.extraOutput != null && !StackUtil.isEmpty(result.extraOutput))
			{
				drainedContainers.add(result.extraOutput.copy());
			}

			Ic2FluidStack fluid = result.fluidChange;
			if (fluid == null || fluid.isEmpty())
			{
				continue;
			}

			emptyLiquidRecipes.add(new CannerEmptyLiquidRecipeWrapper(stack.copy(), drainedContainers, fluid.copy()));

			for (ItemStack drainedContainer : drainedContainers)
			{
				String dedupKey = ForgeRegistries.ITEMS.getKey(drainedContainer.getItem()) + "|" + ForgeRegistries.FLUIDS.getKey(fluid.getFluid());
				if (!bottleLiquidDedup.containsKey(dedupKey))
				{
					List<ItemStack> emptyInputs = List.of(drainedContainer.copy());
					CannerBottleLiquidRecipeWrapper wrapper = new CannerBottleLiquidRecipeWrapper(emptyInputs, fluid.copy(), stack.copy());
					bottleLiquidRecipes.add(wrapper);
					bottleLiquidDedup.put(dedupKey, wrapper);
				}
			}
		}
	}

	public @NotNull ResourceLocation getPluginUid()
	{
		return IC2.getIdentifier("plugin");
	}

	public void registerCategories(IRecipeCategoryRegistration registration)
	{
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.BLAST_FURNACE, this.BLAST_FURNACE, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.BLOCK_CUTTER, this.BLOCK_CUTTER, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.CENTRIFUGE, this.CENTRIFUGE, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.COMPRESSOR, this.COMPRESSOR, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.EXTRACTOR, this.EXTRACTOR, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.MACERATOR, this.MACERATOR, guiHelper));
		registration.addRecipeCategories(new MetalFormerCategory(this.METAL_FORMER_EXTRUDING, 0, guiHelper));
		registration.addRecipeCategories(new MetalFormerCategory(this.METAL_FORMER_ROLLING, 1, guiHelper));
		registration.addRecipeCategories(new MetalFormerCategory(this.METAL_FORMER_CUTTING, 2, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.ORE_WASHING_PLANT, this.ORE_WASHER, guiHelper));
		registration.addRecipeCategories(new CannerBottleCategory((ic2.core.block.tileentity.Ic2TileEntityBlock) Ic2Blocks.SOLID_CANNER, this.CANNER_BOTTLE, guiHelper));
		registration.addRecipeCategories(new CannerEnrichCategory(this.CANNER_ENRICH, guiHelper));
		registration.addRecipeCategories(new CannerBottleLiquidCategory(this.CANNER_BOTTLE_LIQUID, guiHelper));
		registration.addRecipeCategories(new CannerEmptyLiquidCategory(this.CANNER_EMPTY_LIQUID));
	}

	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.BLAST_FURNACE), this.BLAST_FURNACE);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.BLOCK_CUTTER), this.BLOCK_CUTTER);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.CENTRIFUGE), this.CENTRIFUGE);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.COMPRESSOR), this.COMPRESSOR);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.EXTRACTOR), this.EXTRACTOR);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.MACERATOR), this.MACERATOR);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.METAL_FORMER), this.METAL_FORMER_CUTTING, this.METAL_FORMER_EXTRUDING, this.METAL_FORMER_ROLLING);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.ORE_WASHING_PLANT), this.ORE_WASHER);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.SOLID_CANNER), this.CANNER_BOTTLE);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.CANNER), this.CANNER_BOTTLE, this.CANNER_ENRICH, this.CANNER_BOTTLE_LIQUID, this.CANNER_EMPTY_LIQUID);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.INDUSTRIAL_WORKBENCH), RecipeTypes.CRAFTING);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.BATCH_CRAFTER), RecipeTypes.CRAFTING);

		// Furnace-class machines use vanilla smelting (and fuel for the iron furnace firebox).
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.IRON_FURNACE), RecipeTypes.SMELTING, RecipeTypes.FUELING);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.ELECTRIC_FURNACE), RecipeTypes.SMELTING);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.INDUCTION_FURNACE), RecipeTypes.SMELTING);
	}

	/**
	 * Wires {@link RecipeButton} overlays (guidef {@code type="recipe"} / metal former & canner GUIs)
	 * so clicking a progress bar opens the matching JEI recipe category.
	 */
	@Override
	public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime)
	{
		IRecipesGui recipesGui = jeiRuntime.getRecipesGui();
		Map<String, RecipeType<?>> aliases = this.createRecipeButtonAliases();

		RecipeButton.jeiRecipeListOpener = categories -> button ->
		{
			if (categories == null || categories.length == 0)
			{
				return;
			}

			Set<RecipeType<?>> types = new LinkedHashSet<>();
			for (String key : categories)
			{
				RecipeType<?> type = resolveRecipeButtonKey(key, aliases);
				if (type != null)
				{
					types.add(type);
				}
			}

			if (!types.isEmpty())
			{
				recipesGui.showTypes(List.copyOf(types));
			}
		};
	}

	@Override
	public void onRuntimeUnavailable()
	{
		RecipeButton.jeiRecipeListOpener = null;
	}

	/**
	 * Maps IC2 GUI recipe-button event names (guidef / hard-coded GUIs) to JEI recipe types.
	 * Keys are the historical short names from 1.12-era IC2, not always equal to modern {@link RecipeType} uids.
	 */
	private Map<String, RecipeType<?>> createRecipeButtonAliases()
	{
		Map<String, RecipeType<?>> aliases = new HashMap<>();

		// Processing machines (guidef event names)
		aliases.put("macerator", this.MACERATOR);
		aliases.put("compressor", this.COMPRESSOR);
		aliases.put("extractor", this.EXTRACTOR);
		aliases.put("centrifuge", this.CENTRIFUGE);
		aliases.put("blast_furnace", this.BLAST_FURNACE);
		aliases.put("block_cutter", this.BLOCK_CUTTER);
		aliases.put("ore_washing_plant", this.ORE_WASHER);
		aliases.put("ore_washer", this.ORE_WASHER);
		aliases.put("solid_canner", this.CANNER_BOTTLE);

		// Metal former modes (GuiMetalFormer): 0 extrude, 1 roll, 2 cut
		aliases.put("metal_former0", this.METAL_FORMER_EXTRUDING);
		aliases.put("metal_former1", this.METAL_FORMER_ROLLING);
		aliases.put("metal_former2", this.METAL_FORMER_CUTTING);
		aliases.put("metal_former_extruding", this.METAL_FORMER_EXTRUDING);
		aliases.put("metal_former_rolling", this.METAL_FORMER_ROLLING);
		aliases.put("metal_former_cutting", this.METAL_FORMER_CUTTING);

		// Fluid/solid canner modes (GuiCanner): canner_<Mode.name()>
		aliases.put("canner_BottleSolid", this.CANNER_BOTTLE);
		aliases.put("canner_EmptyLiquid", this.CANNER_EMPTY_LIQUID);
		aliases.put("canner_BottleLiquid", this.CANNER_BOTTLE_LIQUID);
		aliases.put("canner_EnrichLiquid", this.CANNER_ENRICH);
		aliases.put("canner_bottle", this.CANNER_BOTTLE);
		aliases.put("canner_empty_liquid", this.CANNER_EMPTY_LIQUID);
		aliases.put("canner_bottle_liquid", this.CANNER_BOTTLE_LIQUID);
		aliases.put("canner_enrich", this.CANNER_ENRICH);

		// Vanilla-style furnaces (guidef uses dotted 1.12-era category ids)
		aliases.put("minecraft.smelting", RecipeTypes.SMELTING);
		aliases.put("minecraft.fuel", RecipeTypes.FUELING);
		aliases.put("minecraft:smelting", RecipeTypes.SMELTING);
		aliases.put("minecraft:furnace", RecipeTypes.SMELTING);
		aliases.put("minecraft:fuel", RecipeTypes.FUELING);
		aliases.put("smelting", RecipeTypes.SMELTING);
		aliases.put("fuel", RecipeTypes.FUELING);

		// Also accept modern "modid:path" uids for every IC2 type we own
		putUidAliases(aliases, this.MACERATOR);
		putUidAliases(aliases, this.COMPRESSOR);
		putUidAliases(aliases, this.EXTRACTOR);
		putUidAliases(aliases, this.CENTRIFUGE);
		putUidAliases(aliases, this.BLAST_FURNACE);
		putUidAliases(aliases, this.BLOCK_CUTTER);
		putUidAliases(aliases, this.ORE_WASHER);
		putUidAliases(aliases, this.METAL_FORMER_EXTRUDING);
		putUidAliases(aliases, this.METAL_FORMER_ROLLING);
		putUidAliases(aliases, this.METAL_FORMER_CUTTING);
		putUidAliases(aliases, this.CANNER_BOTTLE);
		putUidAliases(aliases, this.CANNER_ENRICH);
		putUidAliases(aliases, this.CANNER_BOTTLE_LIQUID);
		putUidAliases(aliases, this.CANNER_EMPTY_LIQUID);

		return aliases;
	}

	private static void putUidAliases(Map<String, RecipeType<?>> aliases, RecipeType<?> type)
	{
		ResourceLocation uid = type.getUid();
		aliases.put(uid.toString(), type);
		aliases.put(uid.getPath(), type);
	}

	@Nullable
	private static RecipeType<?> resolveRecipeButtonKey(String key, Map<String, RecipeType<?>> aliases)
	{
		if (key == null || key.isEmpty())
		{
			return null;
		}

		RecipeType<?> direct = aliases.get(key);
		if (direct != null)
		{
			return direct;
		}

		// guidef sometimes uses dots: ic2.macerator / minecraft.smelting
		String normalized = key.indexOf('.') >= 0 && key.indexOf(':') < 0 ? key.replace('.', ':') : key;
		direct = aliases.get(normalized);
		if (direct != null)
		{
			return direct;
		}

		if (normalized.indexOf(':') >= 0)
		{
			ResourceLocation id = ResourceLocation.tryParse(normalized);
			if (id != null)
			{
				direct = aliases.get(id.toString());
				if (direct != null)
				{
					return direct;
				}
				return aliases.get(id.getPath());
			}
		}

		return aliases.get("ic2:" + key);
	}

	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
	{
		// Industrial workbench: 3x3 grid + 2x9 buffer + forge-hammer / wire-cutter dedicated rows.
		registration.addRecipeTransferHandler(new IndustrialWorkbenchTransferInfo());
		// Batch crafter: recipe slots are inputs only (JEI maps by index to recipe INPUT views).
		registration.addRecipeTransferHandler(new CraftingTransferInfo<>(ContainerBatchCrafter.class, Ic2ScreenHandlers.BATCH_CRAFTER, 37, 45, 0, 36));

		registration.addRecipeTransferHandler(new IOTransferInfo<>(ContainerMetalFormer.class, Ic2ScreenHandlers.METAL_FORMER, this.METAL_FORMER_EXTRUDING, List.of(37), List.of(38), 0, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(ContainerMetalFormer.class, Ic2ScreenHandlers.METAL_FORMER, this.METAL_FORMER_ROLLING, List.of(37), List.of(38), 0, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(ContainerMetalFormer.class, Ic2ScreenHandlers.METAL_FORMER, this.METAL_FORMER_CUTTING, List.of(37), List.of(38), 0, 36));

		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.MACERATOR, List.of(0), List.of(1), 7, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.COMPRESSOR, List.of(0), List.of(1), 7, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.EXTRACTOR, List.of(0), List.of(1), 7, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.CENTRIFUGE, List.of(0), List.of(2, 3, 4), 9, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.BLOCK_CUTTER, List.of(0), List.of(1), 8, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.BLAST_FURNACE, List.of(0), List.of(1, 2), 7, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.ORE_WASHER, List.of(0), List.of(1, 2, 3), 11, 36));

		registration.addRecipeTransferHandler(new IOTransferInfo<>(ContainerCanner.class, Ic2ScreenHandlers.CANNER, this.CANNER_BOTTLE, List.of(37, 43), List.of(38), 0, 36));

		IRecipeTransferHandlerHelper transferHelper = registration.getTransferHelper();
		registration.addRecipeTransferHandler(new EnrichLiquidTransferHandler(this.CANNER_ENRICH, transferHelper), this.CANNER_ENRICH);
		registration.addRecipeTransferHandler(new BottleLiquidTransferHandler(this.CANNER_BOTTLE_LIQUID, transferHelper), this.CANNER_BOTTLE_LIQUID);
		registration.addRecipeTransferHandler(new EmptyLiquidTransferHandler(this.CANNER_EMPTY_LIQUID, transferHelper), this.CANNER_EMPTY_LIQUID);
	}

	public void registerRecipes(@NotNull IRecipeRegistration registration)
	{
		RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
		BiConsumer<RecipeType<IORecipeWrapper>, net.minecraft.world.item.crafting.RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>>> registerBasic = (id, type) ->
		{
			List<IORecipeWrapper> recipeList = recipeManager.getAllRecipesFor(type).stream().map(r -> new IORecipeWrapper(r.recipe())).toList();
			registration.addRecipes(id, recipeList);
		};
		registerBasic.accept(this.BLAST_FURNACE, Ic2RecipeTypes.BLAST_FURNACE);
		registerBasic.accept(this.BLOCK_CUTTER, Ic2RecipeTypes.BLOCK_CUTTER);
		registerBasic.accept(this.CENTRIFUGE, Ic2RecipeTypes.CENTRIFUGE);
		registerBasic.accept(this.COMPRESSOR, Ic2RecipeTypes.COMPRESSOR);
		registerBasic.accept(this.EXTRACTOR, Ic2RecipeTypes.EXTRACTOR);
		registerBasic.accept(this.MACERATOR, Ic2RecipeTypes.MACERATOR);
		registerBasic.accept(this.METAL_FORMER_CUTTING, Ic2RecipeTypes.METAL_FORMER_CUTTING);
		registerBasic.accept(this.METAL_FORMER_EXTRUDING, Ic2RecipeTypes.METAL_FORMER_EXTRUDING);
		registerBasic.accept(this.METAL_FORMER_ROLLING, Ic2RecipeTypes.METAL_FORMER_ROLLING);
		registerBasic.accept(this.ORE_WASHER, Ic2RecipeTypes.ORE_WASHER);

		List<CannerBottleRecipeWrapper> cannerBottleRecipes = recipeManager.getAllRecipesFor(Ic2RecipeTypes.CANNER_BOTTLE).stream().map(r -> new CannerBottleRecipeWrapper(r.recipe())).toList();
		registration.addRecipes(this.CANNER_BOTTLE, cannerBottleRecipes);

		List<CannerEnrichRecipeWrapper> cannerEnrichRecipes = recipeManager.getAllRecipesFor(Ic2RecipeTypes.CANNER_ENRICH).stream().map(r -> new CannerEnrichRecipeWrapper(r.recipe())).toList();
		registration.addRecipes(this.CANNER_ENRICH, cannerEnrichRecipes);

		List<CannerEmptyLiquidRecipeWrapper> emptyLiquidRecipes = new ArrayList<>();
		List<CannerBottleLiquidRecipeWrapper> bottleLiquidRecipes = new ArrayList<>();
		Map<String, CannerBottleLiquidRecipeWrapper> bottleLiquidDedup = new HashMap<>();
		generateCannerFluidContainerRecipes(emptyLiquidRecipes, bottleLiquidRecipes, bottleLiquidDedup);
		registration.addRecipes(this.CANNER_EMPTY_LIQUID, emptyLiquidRecipes);
		registration.addRecipes(this.CANNER_BOTTLE_LIQUID, bottleLiquidRecipes);
	}

	/**
	 * JEI pairs recipe INPUT views with {@link #getRecipeSlots} by index.
	 * Do not include the result slot — only crafting inputs.
	 */
	private record CraftingTransferInfo<C extends AbstractContainerMenu>(
		Class<C> containerClass,
		MenuType<C> menuType,
		int inputStart,
		int inputEndInclusive,
		int inventoryStart,
		int inventoryCount) implements IRecipeTransferInfo<C, CraftingRecipe>
		{

			@Override
			public @NotNull Class<? extends C> getContainerClass()
			{
				return this.containerClass;
			}

			@Override
			public @NotNull Optional<MenuType<C>> getMenuType()
			{
				return Optional.of(this.menuType);
			}

			@Override
			public @NotNull RecipeType<CraftingRecipe> getRecipeType()
			{
				return RecipeTypes.CRAFTING;
			}

			@Override
			public boolean canHandle(@NotNull C container, @NotNull CraftingRecipe recipe)
			{
				return true;
			}

			@Override
			public @NotNull List<Slot> getRecipeSlots(@NotNull C container, @NotNull CraftingRecipe recipe)
			{
				List<Slot> slots = new ArrayList<>(this.inputEndInclusive - this.inputStart + 1);
				for (int i = this.inputStart; i <= this.inputEndInclusive; i++)
				{
					slots.add(container.getSlot(i));
				}
				return slots;
			}

			@Override
			public @NotNull List<Slot> getInventorySlots(@NotNull C container, @NotNull CraftingRecipe recipe)
			{
				List<Slot> slots = new ArrayList<>(this.inventoryCount);
				for (int i = 0; i < this.inventoryCount; i++)
				{
					slots.add(container.getSlot(this.inventoryStart + i));
				}
				return slots;
			}
		}

	/**
	 * Industrial workbench transfer:
	 * <ul>
	 *   <li>3x3 grid slots map 1:1 to JEI crafting inputs (no result slot)</li>
	 *   <li>2x9 buffer is treated as extra inventory for pull/stow</li>
	 *   <li>2-ingredient forge-hammer / wire-cutter recipes target dedicated tool rows</li>
	 * </ul>
	 * Slot layout (player inv first via {@link ic2.core.ContainerFullInv}):
	 * 0-35 player, 36 craft result, 37-45 grid, 46-63 buffer,
	 * 64 left tool, 65 left input, 66 left result, 67 right tool, 68 right input, 69 right result.
	 */
	private static final class IndustrialWorkbenchTransferInfo implements IRecipeTransferInfo<ContainerIndustrialWorkbench, CraftingRecipe>
	{
		private record ToolCombo(boolean hammer, int toolIngredientIndex)
		{
			boolean isHammer()
			{
				return this.hammer;
			}
		}

		@Override
		public @NotNull Class<? extends ContainerIndustrialWorkbench> getContainerClass()
		{
			return ContainerIndustrialWorkbench.class;
		}

		@Override
		public @NotNull Optional<MenuType<ContainerIndustrialWorkbench>> getMenuType()
		{
			return Optional.of(Ic2ScreenHandlers.INDUSTRIAL_WORKBENCH);
		}

		@Override
		public @NotNull RecipeType<CraftingRecipe> getRecipeType()
		{
			return RecipeTypes.CRAFTING;
		}

		@Override
		public boolean canHandle(@NotNull ContainerIndustrialWorkbench container, @NotNull CraftingRecipe recipe)
		{
			return true;
		}

		@Override
		public @NotNull List<Slot> getRecipeSlots(@NotNull ContainerIndustrialWorkbench container, @NotNull CraftingRecipe recipe)
		{
			ToolCombo combo = detectToolCombo(recipe);
			if (combo != null)
			{
				return getToolComboRecipeSlots(container, combo);
			}
			return getGridRecipeSlots(container);
		}

		@Override
		public @NotNull List<Slot> getInventorySlots(@NotNull ContainerIndustrialWorkbench container, @NotNull CraftingRecipe recipe)
		{
			List<Slot> slots = new ArrayList<>(36 + 18 + 4);
			// Player inventory
			for (int i = 0; i < 36; i++)
			{
				slots.add(container.getSlot(i));
			}
			// 2x9 temporary storage buffer
			for (int i = container.indexBufferStart; i < container.indexBufferEnd; i++)
			{
				slots.add(container.getSlot(i));
			}

			ToolCombo combo = detectToolCombo(recipe);
			if (combo == null)
			{
				// For normal crafting, dedicated tool rows can supply materials/tools.
				addToolRowInventorySlots(container, slots, true, true);
			} else if (combo.isHammer())
			{
				// Hammer row is recipe target; cutter row can still supply items.
				addToolRowInventorySlots(container, slots, false, true);
			} else
			{
				addToolRowInventorySlots(container, slots, true, false);
			}
			return slots;
		}

		private static List<Slot> getGridRecipeSlots(ContainerIndustrialWorkbench container)
		{
			List<Slot> slots = new ArrayList<>(9);
			for (int i = container.indexGridStart; i < container.indexGridEnd; i++)
			{
				slots.add(container.getSlot(i));
			}
			return slots;
		}

		/**
		 * JEI always exposes 9 crafting input views. Two-ingredient shapeless recipes only fill
		 * views 0 and 1 (via CraftingGridHelper); remaining views are empty and skipped.
		 * Extra grid slots are included so transfer also clears leftover items from the 3x3.
		 */
		private static List<Slot> getToolComboRecipeSlots(ContainerIndustrialWorkbench container, ToolCombo combo)
		{
			Slot toolSlot = container.getSlot(combo.isHammer() ? container.indexToolHammer : container.indexToolCutter);
			Slot inputSlot = container.getSlot(combo.isHammer() ? container.indexInputHammer : container.indexInputCutter);

			List<Slot> slots = new ArrayList<>(11);
			// Map JEI ingredient order: tool ingredient view → tool slot, material → input slot.
			if (combo.toolIngredientIndex() == 0)
			{
				slots.add(toolSlot);
				slots.add(inputSlot);
			} else
			{
				slots.add(inputSlot);
				slots.add(toolSlot);
			}
			for (int i = container.indexGridStart; i < container.indexGridEnd; i++)
			{
				slots.add(container.getSlot(i));
			}
			return slots;
		}

		private static void addToolRowInventorySlots(
			ContainerIndustrialWorkbench container,
			List<Slot> slots,
			boolean includeHammerRow,
			boolean includeCutterRow)
		{
			if (includeHammerRow)
			{
				slots.add(container.getSlot(container.indexToolHammer));
				slots.add(container.getSlot(container.indexInputHammer));
			}
			if (includeCutterRow)
			{
				slots.add(container.getSlot(container.indexToolCutter));
				slots.add(container.getSlot(container.indexInputCutter));
			}
		}

		/**
		 * Recipes that are exactly {@code material + forge_hammer} or {@code material + wire_cutter}.
		 * These match the industrial workbench's 2-slot dedicated crafting rows.
		 */
		@Nullable
		private static ToolCombo detectToolCombo(CraftingRecipe recipe)
		{
			NonNullList<Ingredient> ingredients = recipe.getIngredients();
			if (ingredients.size() != 2)
			{
				return null;
			}

			int hammerIndex = -1;
			int cutterIndex = -1;
			for (int i = 0; i < ingredients.size(); i++)
			{
				Ingredient ingredient = ingredients.get(i);
				if (ingredient.isEmpty())
				{
					continue;
				}
				if (ingredientMatchesTag(ingredient, Ic2ItemTags.FORGE_HAMMERS))
				{
					hammerIndex = i;
				} else if (ingredientMatchesTag(ingredient, Ic2ItemTags.WIRE_CUTTERS))
				{
					cutterIndex = i;
				}
			}

			// Exactly one tool ingredient + one material.
			if (hammerIndex >= 0 && cutterIndex < 0)
			{
				return new ToolCombo(true, hammerIndex);
			}
			if (cutterIndex >= 0 && hammerIndex < 0)
			{
				return new ToolCombo(false, cutterIndex);
			}
			return null;
		}

		private static boolean ingredientMatchesTag(Ingredient ingredient, TagKey<Item> tag)
		{
			for (ItemStack stack : ingredient.getItems())
			{
				if (!stack.isEmpty() && stack.is(tag))
				{
					return true;
				}
			}
			return false;
		}
	}

	private record IOTransferInfo<R>(
		Class<? extends AbstractContainerMenu> containerClass,
		MenuType<?> menuType,
		RecipeType<R> recipeType,
		List<Integer> inputSlots,
		List<Integer> outputSlots,
		int inventoryStart,
		int inventoryCount) implements IRecipeTransferInfo<AbstractContainerMenu, R>
		{

			@Override
			public @NotNull Class<? extends AbstractContainerMenu> getContainerClass()
			{
				return this.containerClass;
			}
	
			@Override
			public @NotNull Optional<MenuType<AbstractContainerMenu>> getMenuType()
			{
				return Optional.of((MenuType<AbstractContainerMenu>) this.menuType);
			}
	
			@Override
			public @NotNull RecipeType<R> getRecipeType()
			{
				return this.recipeType;
			}
	
			@Override
			public boolean canHandle(@NotNull AbstractContainerMenu container, @NotNull R recipe)
			{
				return true;
			}
	
			@Override
			public @NotNull List<Slot> getRecipeSlots(@NotNull AbstractContainerMenu container, @NotNull R recipe)
			{
				List<Slot> slots = new ArrayList<>(this.inputSlots.size() + this.outputSlots.size());
				for (int idx : this.inputSlots)
				{
					slots.add(container.getSlot(idx));
				}
				for (int idx : this.outputSlots)
				{
					slots.add(container.getSlot(idx));
				}
				return slots;
			}
	
			@Override
			public @NotNull List<Slot> getInventorySlots(@NotNull AbstractContainerMenu container, @NotNull R recipe)
			{
				List<Slot> slots = new ArrayList<>(this.inventoryCount);
				for (int i = this.inventoryStart; i < this.inventoryStart + this.inventoryCount; i++)
				{
					slots.add(container.getSlot(i));
				}
				return slots;
			}
		}

	private abstract static class CannerTransferHandler<R> implements IRecipeTransferHandler<AbstractContainerMenu, R>
	{
		protected final RecipeType<R> recipeType;
		protected final IRecipeTransferHandlerHelper transferHelper;

		CannerTransferHandler(RecipeType<R> recipeType, IRecipeTransferHandlerHelper transferHelper)
		{
			this.recipeType = recipeType;
			this.transferHelper = transferHelper;
		}

		@Override
		public @NotNull Class<? extends AbstractContainerMenu> getContainerClass()
		{
			return ContainerCanner.class;
		}

		@Override
		public @NotNull Optional<MenuType<AbstractContainerMenu>> getMenuType()
		{
			return Optional.of((MenuType<AbstractContainerMenu>) (MenuType<?>) Ic2ScreenHandlers.CANNER);
		}

		@Override
		public @NotNull RecipeType<R> getRecipeType()
		{
			return this.recipeType;
		}
		
		protected TileEntityCanner getCanner(AbstractContainerMenu container)
		{
			return (TileEntityCanner) ((ic2.core.ContainerBase<?>) container).base;
		}

		protected boolean transferItemToSlotIfNot(AbstractContainerMenu container, IRecipeSlotView slotView, int targetSlotIndex, boolean doTransfer)
		{
			Slot targetSlot = container.getSlot(targetSlotIndex);
			if (!targetSlot.getItem().isEmpty())
			{
				return false;
			}
			List<ItemStack> possibleStacks = slotView.getItemStacks().toList();

			// Find matching item in player inventory
			for (int invIdx = 0; invIdx < 36; invIdx++)
			{
				ItemStack invStack = container.getSlot(invIdx).getItem();
				for (ItemStack needed : possibleStacks)
				{
					if (ItemStack.isSameItemSameTags(invStack, needed))
					{
						if (doTransfer)
						{
							ItemStack toMove = invStack.copyWithCount(1);
							container.getSlot(invIdx).remove(1);
							targetSlot.set(toMove);
						}
						return false;
					}
				}
			}
			return true;
		}
	}

	private static final class EnrichLiquidTransferHandler extends CannerTransferHandler<CannerEnrichRecipeWrapper>
	{
		EnrichLiquidTransferHandler(RecipeType<CannerEnrichRecipeWrapper> recipeType, IRecipeTransferHandlerHelper transferHelper)
		{
			super(recipeType, transferHelper);
		}

		@Override
		public IRecipeTransferError transferRecipe(@NotNull AbstractContainerMenu container, @NotNull CannerEnrichRecipeWrapper recipe, @NotNull IRecipeSlotsView recipeSlots, @NotNull Player player, boolean maxTransfer, boolean doTransfer)
		{
			if (!(container instanceof ContainerCanner))
			{
				return this.transferHelper.createInternalError();
			}

			TileEntityCanner canner = this.getCanner(container);
			List<IRecipeSlotView> slotViews = recipeSlots.getSlotViews();

			// Slot 0: fluid input — validate tank has matching fluid
			IRecipeSlotView fluidInView = slotViews.get(0);
			FluidStack neededFluid = fluidInView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).orElse(null);
			if (neededFluid != null && !canner.inputTank.isEmpty())
			{
				Ic2FluidStack tankFluid = canner.inputTank.getFluidStack();
				if (tankFluid != null && tankFluid.getFluid() != neededFluid.getFluid())
				{
					return this.transferHelper.createUserErrorWithTooltip(
						Component.translatable("ic2.jei.transfer.error.fluid_mismatch", neededFluid.getDisplayName())
					);
				}
			}

			// Slot 1: additive item → inputSlot (37)
			if (this.transferItemToSlotIfNot(container, slotViews.get(1), 37, doTransfer))
			{
				return this.transferHelper.createUserErrorForMissingSlots(
					Component.translatable("ic2.jei.transfer.error.missing_additive"), List.of(slotViews.get(1))
				);
			}

			return null;
		}
	}

	private static final class BottleLiquidTransferHandler extends CannerTransferHandler<CannerBottleLiquidRecipeWrapper>
	{
		BottleLiquidTransferHandler(RecipeType<CannerBottleLiquidRecipeWrapper> recipeType, IRecipeTransferHandlerHelper transferHelper)
		{
			super(recipeType, transferHelper);
		}

		@Override
		public IRecipeTransferError transferRecipe(@NotNull AbstractContainerMenu container, @NotNull CannerBottleLiquidRecipeWrapper recipe, @NotNull IRecipeSlotsView recipeSlots, @NotNull Player player, boolean maxTransfer, boolean doTransfer)
		{
			if (!(container instanceof ContainerCanner))
			{
				return this.transferHelper.createInternalError();
			}

			List<IRecipeSlotView> slotViews = recipeSlots.getSlotViews();

			// Slot 0: fluid input — handled by machine's inputTank
			// Slot 1: empty container → canInputSlot (43)
			if (this.transferItemToSlotIfNot(container, slotViews.get(1), 43, doTransfer))
			{
				return this.transferHelper.createUserErrorForMissingSlots(
					Component.translatable("ic2.jei.transfer.error.missing_container"), List.of(slotViews.get(1))
				);
			}

			return null;
		}
	}

	private static final class EmptyLiquidTransferHandler extends CannerTransferHandler<CannerEmptyLiquidRecipeWrapper>
	{
		EmptyLiquidTransferHandler(RecipeType<CannerEmptyLiquidRecipeWrapper> recipeType, IRecipeTransferHandlerHelper transferHelper)
		{
			super(recipeType, transferHelper);
		}

		@Override
		public IRecipeTransferError transferRecipe(@NotNull AbstractContainerMenu container, @NotNull CannerEmptyLiquidRecipeWrapper recipe, @NotNull IRecipeSlotsView recipeSlots, @NotNull Player player, boolean maxTransfer, boolean doTransfer)
		{
			if (!(container instanceof ContainerCanner))
			{
				return this.transferHelper.createInternalError();
			}

			List<IRecipeSlotView> slotViews = recipeSlots.getSlotViews();

			// Slot 0: filled container → canInputSlot (43)
			if (this.transferItemToSlotIfNot(container, slotViews.get(0), 43, doTransfer))
			{
				return this.transferHelper.createUserErrorForMissingSlots(
					Component.translatable("ic2.jei.transfer.error.missing_filled_container"), List.of(slotViews.get(0))
				);
			}

			return null;
		}
	}
}
