package me.halfcooler.ic2r.core.block.wiring;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityTransformer;
import me.halfcooler.ic2r.core.gui.ItemImage;
import me.halfcooler.ic2r.core.gui.TextLabel;
import me.halfcooler.ic2r.core.gui.VanillaButton;
import me.halfcooler.ic2r.core.gui.dynamic.TextProvider;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiTransformer extends Ic2rGui<ContainerTransformer>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guitransfomer.png");
	public String[] mode = new String[] { "", "", "", "" };

	public GuiTransformer(ContainerTransformer container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 219);
		this.addElement(TextLabel.create(this, 8, 28, TextProvider.ofTranslated("ic2r.Transformer.gui.Output"), 4210752, true));
		this.addElement(TextLabel.create(this, 8, 44, TextProvider.ofTranslated("ic2r.Transformer.gui.Input"), 4210752, true));
		this.addElement(TextLabel.create(this, 52, 28, TextProvider.of(() -> this.menu.base.getOutputFlowDisplay().getString()), 2157374, true));
		this.addElement(TextLabel.create(this, 52, 44, TextProvider.of(() -> this.menu.base.getInputFlowDisplay().getString()), 2157374, true));
		this.addElement(new VanillaButton(this, 7, 65, 144, 20, this.createEventSender(0)).withText(Component.translatable("ic2r.Transformer.gui.switch.mode1").getString()));
		this.addElement(new VanillaButton(this, 7, 85, 144, 20, this.createEventSender(1)).withText(Component.translatable("ic2r.Transformer.gui.switch.mode2").getString()));
		this.addElement(new VanillaButton(this, 7, 105, 144, 20, this.createEventSender(2)).withText(Component.translatable("ic2r.Transformer.gui.switch.mode3").getString()));
		this.addElement(new ItemImage(this, 152, 67, () -> new ItemStack(Ic2rItems.WRENCH)).withEnableHandler(() -> this.menu.base.getMode() == TileEntityTransformer.Mode.redstone));
		this.addElement(new ItemImage(this, 152, 87, () -> new ItemStack(Ic2rItems.WRENCH)).withEnableHandler(() -> this.menu.base.getMode() == TileEntityTransformer.Mode.stepDown));
		this.addElement(new ItemImage(this, 152, 107, () -> new ItemStack(Ic2rItems.WRENCH)).withEnableHandler(() -> this.menu.base.getMode() == TileEntityTransformer.Mode.stepUp));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
