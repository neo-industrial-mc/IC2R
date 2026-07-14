package me.halfcooler.ic2r.forge.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public final class WallModelLoader implements IGeometryLoader<Ic2rModel>
{
	@Override
	public Ic2rModel read(JsonObject obj, JsonDeserializationContext context)
	{
		return new WallModelForge();
	}
}