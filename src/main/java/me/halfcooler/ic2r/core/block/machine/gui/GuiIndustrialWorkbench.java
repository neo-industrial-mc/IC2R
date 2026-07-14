package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerIndustrialWorkbench;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import me.halfcooler.ic2r.core.block.personal.IPersonalBlock;
import me.halfcooler.ic2r.core.gui.Area;
import me.halfcooler.ic2r.core.gui.CustomButton;
import me.halfcooler.ic2r.core.gui.GuiElement;
import me.halfcooler.ic2r.core.gui.IClickHandler;
import me.halfcooler.ic2r.core.gui.Image;
import me.halfcooler.ic2r.core.gui.MouseButton;
import me.halfcooler.ic2r.core.gui.VanillaButton;
import me.halfcooler.ic2r.core.proxy.SideProxyClient;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;

import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GuiIndustrialWorkbench extends Ic2rGui<ContainerIndustrialWorkbench>
{
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guiindustrialworkbench.png");
	public static Predicate<Screen> jeiScreenRecipesGuiCheck;

	public GuiIndustrialWorkbench(ContainerIndustrialWorkbench container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 194, 228);
		this.addElement((new Area(this, 173, 3, 18, 108)
		{
			@Override
			protected boolean suppressTooltip(int mouseX, int mouseY)
			{
				for (GuiElement<?> element : GuiIndustrialWorkbench.this.elements)
				{
					if (element.isEnabled() && element != this && element.contains(mouseX, mouseY))
					{
						return true;
					}
				}

				return false;
			}
		}).withTooltip("ic2r.IndustrialWorkbench.gui.adjacent"));

		for (final Direction side : Util.ALL_DIRS)
		{
			this.addElement(new CustomButton(this, 173, 3 + (side.get3DDataValue() + 5) % 6 * 18, 18, 18, new IClickHandler()
			{
				private boolean firstOpen = true;
				private boolean jei = false;

				@Override
				public void onClick(MouseButton button)
				{
					TileEntityIndustrialWorkbench base = GuiIndustrialWorkbench.this.menu.base;
					assert base.hasLevel();
					BlockEntity neighbour = base.getLevel().getBlockEntity(base.getBlockPos().relative(side));
					assert neighbour instanceof IHasGui;
					if (neighbour instanceof IPersonalBlock && !((IPersonalBlock) neighbour).permitsAccess(GuiIndustrialWorkbench.this.menu.player.getGameProfile()))
					{
						IC2R.sideProxy.messagePlayer(GuiIndustrialWorkbench.this.menu.player, "Owned by " + ((IPersonalBlock) neighbour).getOwner().getName());
					} else
					{
						GuiIndustrialWorkbench.closeHandler = this::onScreenClose;
						IC2R.network.get(false).requestGUI((IHasGui) neighbour);
					}
				}

				private void onScreenClose()
				{
					if (!this.keepOpen(SideProxyClient.mc.screen))
					{
						if (!this.firstOpen)
						{
							GuiIndustrialWorkbench.closeHandler = null;
							IC2R.network.get(false).requestGUI(GuiIndustrialWorkbench.this.menu.base);
						} else
						{
							this.firstOpen = false;
						}
					}
				}

				private boolean keepOpen(Screen screen)
				{
					if (GuiIndustrialWorkbench.jeiScreenRecipesGuiCheck == null)
					{
						return false;
					} else if (GuiIndustrialWorkbench.jeiScreenRecipesGuiCheck.test(screen))
					{
						this.jei = true;
						return true;
					} else if (this.jei)
					{
						this.jei = false;
						return true;
					} else
					{
						return false;
					}
				}
			}).withEnableHandler(() ->
			{
				TileEntityIndustrialWorkbench base = GuiIndustrialWorkbench.this.menu.base;
				return base.hasLevel() && base.getLevel().getBlockEntity(base.getBlockPos().relative(side)) instanceof IHasGui;
			}).withIcon(() ->
			{
				TileEntityIndustrialWorkbench base = GuiIndustrialWorkbench.this.menu.base;
				assert base.hasLevel();
				BlockPos pos = base.getBlockPos().relative(side);
				BlockState state = base.getLevel().getBlockState(pos);
				return StackUtil.getPickStack(base.getLevel(), pos, state, GuiIndustrialWorkbench.this.menu.player);
			}).withTooltip(new Supplier<>()
			{
				private String getSideName()
				{
					return switch (side)
					{
						case WEST -> "ic2r.dir.West";
						case EAST -> "ic2r.dir.East";
						case DOWN -> "ic2r.dir.Bottom";
						case UP -> "ic2r.dir.Top";
						case NORTH -> "ic2r.dir.North";
						case SOUTH -> "ic2r.dir.South";
					};
				}

				public String get()
				{
					TileEntityIndustrialWorkbench base = GuiIndustrialWorkbench.this.menu.base;
					assert base.hasLevel();
					BlockEntity neighbour = base.getLevel().getBlockEntity(base.getBlockPos().relative(side));
					assert neighbour instanceof IHasGui;
					return IHasGui.getBeName(neighbour).getString() + "\n" + ChatFormatting.DARK_GRAY + Component.translatable(this.getSideName()).getString();
				}
			}));
		}
		this.addElement(new VanillaButton(this, 93, 42, 16, 16, button -> IC2R.network.get(false).sendContainerEvent(GuiIndustrialWorkbench.this.menu, "clear")).withTooltip("Clear"));
		this.addElement(Image.create(this, 94, 43, 14, 14, GuiElement.commonTexture, 256, 256, 210, 47, 224, 61));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return TEXTURE;
	}
}
