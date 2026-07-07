package me.halfcooler.ic2r.addons.csas.init;

import me.halfcooler.ic2r.addons.csas.CsasMod;
import me.halfcooler.ic2r.addons.csas.blockentity.TileEntityCompactSolar;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CsasBlockEntities
{
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CsasMod.MOD_ID);

	public static final RegistryObject<BlockEntityType<TileEntityCompactSolar>> COMPACT_SOLAR = BLOCK_ENTITIES.register("compact_solar",
		() -> BlockEntityType.Builder.of(TileEntityCompactSolar::new,
			CsasBlocks.LOW_VOLTAGE_SOLAR_ARRAY.get(),
			CsasBlocks.MEDIUM_VOLTAGE_SOLAR_ARRAY.get(),
			CsasBlocks.HIGH_VOLTAGE_SOLAR_ARRAY.get()
		).build(null));

	private CsasBlockEntities()
	{
	}
}