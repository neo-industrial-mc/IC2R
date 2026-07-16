package me.halfcooler.ic2r.core.recipe.v2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Helpers for migrating 1.20 RecipeSerializer fromJson/fromNetwork APIs to 1.21 MapCodec/StreamCodec.
 */
public final class JsonRecipeCodecs
{
	private JsonRecipeCodecs()
	{
	}

	/**
	 * Treats the whole recipe JSON object as input to {@code fromJson}.
	 * Encoding writes {@code toJson} when possible; empty object if encoder is null.
	 */
	public static <T> MapCodec<T> mapCodec(Function<JsonObject, T> fromJson, Function<T, JsonObject> toJson)
	{
		Codec<T> codec = Codec.PASSTHROUGH.comapFlatMap(
			dynamic ->
			{
				JsonElement json = dynamic.convert(JsonOps.INSTANCE).getValue();
				if (json == null || !json.isJsonObject())
				{
					return DataResult.error(() -> "Expected recipe JSON object");
				}
				try
				{
					return DataResult.success(fromJson.apply(json.getAsJsonObject()));
				} catch (Exception ex)
				{
					return DataResult.error(() -> ex.getMessage() != null ? ex.getMessage() : ex.toString());
				}
			},
			recipe ->
			{
				JsonObject out = toJson != null ? toJson.apply(recipe) : new JsonObject();
				if (out == null)
				{
					out = new JsonObject();
				}
				return new Dynamic<>(JsonOps.INSTANCE, out);
			}
		);
		return MapCodec.assumeMapUnsafe(codec);
	}

	public static <T> MapCodec<T> mapCodec(Function<JsonObject, T> fromJson)
	{
		return mapCodec(fromJson, recipe -> new JsonObject());
	}

	public static <T> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(
		Function<RegistryFriendlyByteBuf, T> reader,
		BiConsumer<RegistryFriendlyByteBuf, T> writer
	)
	{
		return StreamCodec.of(writer::accept, reader::apply);
	}
}
