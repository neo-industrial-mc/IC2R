package me.halfcooler.ic2r.forge.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public final class BeModelLoader implements IGeometryLoader<Ic2rModel>
{
	public Ic2rModel read(JsonObject obj, JsonDeserializationContext context)
	{
		ResourceLocation id = ResourceLocation.parse(obj.get("id").getAsString());
		return new DynamicBeModelForge(id);
	}
}
