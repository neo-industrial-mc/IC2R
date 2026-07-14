package me.halfcooler.ic2r.core.ref;

import com.google.gson.JsonObject;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.item.armor.jetpack.JetpackAttachmentRecipe;
import me.halfcooler.ic2r.core.recipe.AdvRecipe;
import me.halfcooler.ic2r.core.recipe.AdvShapelessRecipe;
import me.halfcooler.ic2r.core.recipe.GradualRecipe;
import me.halfcooler.ic2r.core.recipe.v2.BasicMachineRecipeSerializer;
import me.halfcooler.ic2r.core.recipe.v2.CannerBottleRecipeSerializer;
import me.halfcooler.ic2r.core.recipe.v2.CannerEnrichRecipeSerializer;
import me.halfcooler.ic2r.core.recipe.v2.IntegerOutputRecipeSerializer;
import me.halfcooler.ic2r.core.recipe.v2.WeightedMachineRecipeSerializer;

import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class Ic2rRecipeSerializers
{
	public static final RecipeSerializer<AdvRecipe> SHAPED = register("shaped", new AdvRecipe.Serializer());
	public static final RecipeSerializer<AdvShapelessRecipe> SHAPELESS = register("shapeless", new AdvShapelessRecipe.Serializer());
	public static final RecipeSerializer<GradualRecipe> GRADUAL = register("gradual", new GradualRecipe.Serializer());
	public static final RecipeSerializer<JetpackAttachmentRecipe> JETPACK_ATTACHMENT = register("jetpack_attachment", new JetpackAttachmentRecipe.Serializer());
	public static final WeightedMachineRecipeSerializer MACERATOR = register("macerator", new WeightedMachineRecipeSerializer(Ic2rRecipeTypes.MACERATOR, null));
	public static final BasicMachineRecipeSerializer EXTRACTOR = register("extractor", new BasicMachineRecipeSerializer(Ic2rRecipeTypes.EXTRACTOR, null));
	public static final BasicMachineRecipeSerializer COMPRESSOR = register("compressor", new BasicMachineRecipeSerializer(Ic2rRecipeTypes.COMPRESSOR, null));
	public static final BasicMachineRecipeSerializer CENTRIFUGE = register("centrifuge", new BasicMachineRecipeSerializer(Ic2rRecipeTypes.CENTRIFUGE, intMeta("minHeat")));
	public static final BasicMachineRecipeSerializer BLOCK_CUTTER = register("block_cutter", new BasicMachineRecipeSerializer(Ic2rRecipeTypes.BLOCK_CUTTER, intMeta("hardness")));
	public static final BasicMachineRecipeSerializer BLAST_FURNACE = register("blast_furnace", new BasicMachineRecipeSerializer(Ic2rRecipeTypes.BLAST_FURNACE, twoIntsMeta()));
	public static final BasicMachineRecipeSerializer METAL_FORMER_EXTRUDING = register("metal_former_extruding", new BasicMachineRecipeSerializer(Ic2rRecipeTypes.METAL_FORMER_EXTRUDING, null));
	public static final BasicMachineRecipeSerializer METAL_FORMER_CUTTING = register("metal_former_cutting", new BasicMachineRecipeSerializer(Ic2rRecipeTypes.METAL_FORMER_CUTTING, null));
	public static final BasicMachineRecipeSerializer METAL_FORMER_ROLLING = register("metal_former_rolling", new BasicMachineRecipeSerializer(Ic2rRecipeTypes.METAL_FORMER_ROLLING, null));
	public static final BasicMachineRecipeSerializer ORE_WASHER = register("ore_washer", new BasicMachineRecipeSerializer(Ic2rRecipeTypes.ORE_WASHER, intMeta("amount")));
	public static final IntegerOutputRecipeSerializer MATTER_FABRICATOR = register("matter_fabricator", new IntegerOutputRecipeSerializer(Ic2rRecipeTypes.MATTER_FABRICATOR, null));
	public static final CannerBottleRecipeSerializer CANNER_BOTTLE = register("canner_bottle", new CannerBottleRecipeSerializer());
	public static final CannerEnrichRecipeSerializer CANNER_ENRICH = register("canner_enrich", new CannerEnrichRecipeSerializer());

	public static void init()
	{
	}

	private static <T extends RecipeSerializer<?>> T register(String name, T serializer)
	{
		IC2R.envProxy.registerRecipeSerializer(IC2R.getIdentifier(name), serializer);
		return serializer;
	}

	private static Function<JsonObject, CompoundTag> intMeta(String name)
	{
		return json ->
		{
			int metaValue = GsonHelper.getAsInt(json, name);
			CompoundTag nbt = new CompoundTag();
			nbt.putInt(name, metaValue);
			return nbt;
		};
	}

	private static Function<JsonObject, CompoundTag> twoIntsMeta()
	{
		return json ->
		{
			int metaValue1 = GsonHelper.getAsInt(json, "fluid");
			int metaValue2 = GsonHelper.getAsInt(json, "duration");
			CompoundTag nbt = new CompoundTag();
			nbt.putInt("fluid", metaValue1);
			nbt.putInt("duration", metaValue2);
			return nbt;
		};
	}
}
