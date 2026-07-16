package me.halfcooler.ic2r.core.recipe.v2;

import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import me.halfcooler.ic2r.core.recipe.BasicMachineRecipeManager;
import me.halfcooler.ic2r.core.recipe.MachineRecipeMatchMath;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.Collection;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

/**
 * Vanilla {@link RecipeManager} ↔ IC2R basic machine managers (W2.3 pilot; G2.2 multi-type evidence).
 * <p>
 * <b>Full chain (all basic types share this bridge)</b> — documented for macerator + extractor + compressor:
 * <ol>
 *   <li>JSON under {@code data/ic2r/recipes/<path>/*.json} with {@code "type":"ic2r:<id>"}
 *       (e.g. {@code macerator}, {@code extractor}, {@code compressor})</li>
 *   <li>{@link me.halfcooler.ic2r.core.ref.Ic2rRecipeTypes} +
 *       {@link me.halfcooler.ic2r.core.ref.Ic2rRecipeSerializers}
 *       ({@code BasicMachineRecipeSerializer} or weighted for macerator)</li>
 *   <li>Vanilla loads into {@link RecipeManager}</li>
 *   <li>{@link #loadBasic} materializes a {@link BasicMachineRecipeManager} (cached per manager via
 *       {@link RecipeManagerGetter}); wired in {@code Rezepte#basicRecipe} for every basic type</li>
 *   <li>Machines query via {@code Recipes.macerator|extractor|compressor|…}.get(level)}</li>
 * </ol>
 * <p>
 * <b>Runtime match path today</b>: materialize-once then {@link BasicMachineRecipeManager} scan
 * (not per-tick {@link #findMatching} on {@code RecipeManager}). Direct-query helpers exist for
 * evaluation / future cutover — see {@code docs/spec/recipe_manager_query_eval.md}.
 * <p>
 * <b>Fallback</b>: when no {@code Level}/{@code RecipeManager} is available (unit tests, early init),
 * callers must not use {@link RecipeManagerGetter}; pure match rules live in
 * {@link MachineRecipeMatchMath}. Dynamic/runtime {@code addRecipe} on the materialized manager still
 * works after load (does not write back into the datapack {@code RecipeManager}).
 */
public final class RecipeManagerMachineBridge
{
	private RecipeManagerMachineBridge()
	{
	}

	/**
	 * Materialize all recipes of {@code type} from the vanilla manager into a basic machine manager.
	 */
	public static BasicMachineRecipeManager loadBasic(
		RecipeManager recipeManager,
		RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> type
	)
	{
		BasicMachineRecipeManager manager = new BasicMachineRecipeManager();

		for (var vanillaHolder : recipeManager.getAllRecipesFor(type))
		{
			manager.addRecipe(vanillaHolder.value().recipe(), false);
		}

		return manager;
	}

	/**
	 * Direct RecipeManager path: first holder whose input matches and passes amount gates.
	 * Order = {@link RecipeManager#getAllRecipesFor} iteration (first-wins).
	 *
	 * @param hasRecipeRemainder same meaning as {@link MachineRecipeMatchMath#canApplyInput}
	 */
	@Nullable
	public static MachineRecipe<IRecipeInput, Collection<ItemStack>> findMatching(
		Iterable<RecipeHolder<IRecipeInput, Collection<ItemStack>>> holders,
		ItemStack input,
		boolean hasRecipeRemainder
	)
	{
		if (StackUtil.isEmpty(input) || holders == null)
		{
			return null;
		}

		int count = StackUtil.getSize(input);
		RecipeHolder<IRecipeInput, Collection<ItemStack>> matched = MachineRecipeMatchMath.firstMatch(holders, holder ->
		{
			MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = holder.recipe();
			IRecipeInput recipeInput = recipe.getInput();
			return MachineRecipeMatchMath.acceptsMatchedInput(
				recipeInput.matches(input),
				count,
				recipeInput.getAmount(),
				hasRecipeRemainder
			);
		});
		return matched != null ? matched.recipe() : null;
	}

	/**
	 * Convenience: query {@code recipeManager} for {@code type} then {@link #findMatching}.
	 */
	@Nullable
	public static MachineRecipe<IRecipeInput, Collection<ItemStack>> findMatching(
		RecipeManager recipeManager,
		RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> type,
		ItemStack input,
		boolean hasRecipeRemainder
	)
	{
		List<RecipeHolder<IRecipeInput, Collection<ItemStack>>> holders = recipeManager.getAllRecipesFor(type).stream()
			.map(net.minecraft.world.item.crafting.RecipeHolder::value)
			.toList();
		return findMatching(holders, input, hasRecipeRemainder);
	}
}
