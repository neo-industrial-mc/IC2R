package me.halfcooler.ic2r.forge.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import me.halfcooler.ic2r.core.block.wiring.CableFoam;
import me.halfcooler.ic2r.core.block.wiring.CableType;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public final class CableModelLoader implements IGeometryLoader<Ic2rModel>
{
	public Ic2rModel read(JsonObject obj, JsonDeserializationContext context)
	{
		CableType type = CableType.valueOf(obj.get("type").getAsString());
		int insulation = obj.get("insulation").getAsInt();
		CableFoam foam = CableFoam.get(obj.get("foam").getAsString());
		boolean active = obj.get("active").getAsBoolean();
		return new DynamicCableModelForge(type, insulation, foam, active);
	}
}
