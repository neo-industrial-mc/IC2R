package me.halfcooler.ic2r.addons.csas.init;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.network.DataEncoder;
import ic2.core.network.GrowingBuffer;
import me.halfcooler.ic2r.addons.csas.CsasMod;
import me.halfcooler.ic2r.addons.csas.generator.container.ContainerCompactSolar;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.io.IOException;

public final class CsasScreenHandlers
{
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, CsasMod.MOD_ID);

	public static final RegistryObject<MenuType<ContainerCompactSolar>> COMPACT_SOLAR = MENUS.register("compact_solar",
		() -> IForgeMenuType.create((syncId, inventory, data) ->
		{
			try
			{
				GrowingBuffer buffer = GrowingBuffer.wrap(data);
				BlockEntity blockEntity = DataEncoder.getValue(DataEncoder.decode(buffer, DataEncoder.EncodedType.TileEntity), null);
				if (blockEntity instanceof IHasGui provider)
				{
					ContainerBase<?> container = provider.createClientScreenHandler(syncId, inventory, buffer);
					return container instanceof ContainerCompactSolar compactSolar ? compactSolar : null;
				}
			}
			catch (IOException exception)
			{
				throw new RuntimeException(exception);
			}

			return null;
		}));

	private CsasScreenHandlers()
	{
	}
}