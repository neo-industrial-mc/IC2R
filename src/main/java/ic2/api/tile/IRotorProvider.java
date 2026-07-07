package ic2.api.tile;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public interface IRotorProvider
{
	int getRotorDiameter();

	Direction getFacing();

	float getAngle();

	default float getRotorAnimationSpeed()
	{
		return 0.0F;
	}

	ResourceLocation getRotorRenderTexture();
}
