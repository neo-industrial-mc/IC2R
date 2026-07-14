package me.halfcooler.ic2r.core.gui.dynamic;

public interface IGuiValueProvider
{
	double getGuiValue(String var1);

	interface IActiveGuiValueProvider extends IGuiValueProvider
	{
		boolean isGuiValueActive(String var1);
	}
}
