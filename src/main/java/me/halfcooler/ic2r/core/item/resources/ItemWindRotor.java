package me.halfcooler.ic2r.core.item.resources;

import me.halfcooler.ic2r.api.item.IKineticRotor;
import me.halfcooler.ic2r.core.block.kineticgenerator.gui.GuiWaterKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.gui.GuiWindKineticGenerator;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;

import java.util.List;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

@NotClassic
public class ItemWindRotor extends Item implements IKineticRotor
{
	private final int maxWindStrength;
	private final int minWindStrength;
	private final int radius;
	private final float efficiency;
	private final ResourceLocation renderTexture;
	private final boolean water;

	public ItemWindRotor(
		Properties settings, int Radius, boolean water, float efficiency, int minWindStrength, int maxWindStrength, ResourceLocation RenderTexture
	)
	{
		super(settings);
		this.radius = Radius;
		this.efficiency = efficiency;
		this.renderTexture = RenderTexture;
		this.minWindStrength = minWindStrength;
		this.maxWindStrength = maxWindStrength;
		this.water = water;
	}

	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		Ic2rTooltip.add(tooltip, Component.translatable("ic2r.itemrotor.wind.info", this.minWindStrength, this.maxWindStrength));
		IKineticRotor.GearboxType type = null;
		if (Minecraft.getInstance().screen instanceof GuiWaterKineticGenerator)
		{
			type = IKineticRotor.GearboxType.WATER;
		} else if (Minecraft.getInstance().screen instanceof GuiWindKineticGenerator)
		{
			type = IKineticRotor.GearboxType.WIND;
		}

		if (type != null)
		{
			Ic2rTooltip.add(tooltip, Component.translatable("ic2r.itemrotor.fitsin." + this.isAcceptedType(stack, type)));
		}
	}

	@Override
	public int getDiameter(ItemStack stack)
	{
		return this.radius;
	}

	@Override
	public ResourceLocation getRotorRenderTexture(ItemStack stack)
	{
		return this.renderTexture;
	}

	@Override
	public float getEfficiency(ItemStack stack)
	{
		return this.efficiency;
	}

	@Override
	public int getMinWindStrength(ItemStack stack)
	{
		return this.minWindStrength;
	}

	@Override
	public int getMaxWindStrength(ItemStack stack)
	{
		return this.maxWindStrength;
	}

	@Override
	public boolean isAcceptedType(ItemStack stack, IKineticRotor.GearboxType type)
	{
		return type == IKineticRotor.GearboxType.WIND || this.water;
	}
}
