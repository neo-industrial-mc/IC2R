package me.halfcooler.ic2r.core.ref.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import me.halfcooler.ic2r.core.item.ItemTinCan;
import me.halfcooler.ic2r.core.item.tfbp.Chilling;
import me.halfcooler.ic2r.core.item.tfbp.Cultivation;
import me.halfcooler.ic2r.core.item.tfbp.Desertification;
import me.halfcooler.ic2r.core.item.tfbp.Flatification;
import me.halfcooler.ic2r.core.item.tfbp.Irrigation;
import me.halfcooler.ic2r.core.item.tfbp.Mushroom;
import me.halfcooler.ic2r.core.item.tfbp.Tfbp;
import me.halfcooler.ic2r.core.item.upgrade.ItemUpgradeModule;
import me.halfcooler.ic2r.core.ref.Ic2rItems;

/** Domain item registrations: Machine upgrades and TFBP */
public final class Ic2rItemsUpgrades
{
	private Ic2rItemsUpgrades()
	{
	}

	public static final Item OVERCLOCKER_UPGRADE = Ic2rItems.register("overclocker_upgrade", new ItemUpgradeModule(new Properties(), ItemUpgradeModule.UpgradeType.overclocker));
	public static final Item TRANSFORMER_UPGRADE = Ic2rItems.register("transformer_upgrade", new ItemUpgradeModule(new Properties(), ItemUpgradeModule.UpgradeType.transformer));
	public static final Item ENERGY_STORAGE_UPGRADE = Ic2rItems.register("energy_storage_upgrade", new ItemUpgradeModule(new Properties(), ItemUpgradeModule.UpgradeType.energy_storage));
	public static final Item REDSTONE_INVERTER_UPGRADE = Ic2rItems.register("redstone_inverter_upgrade", new ItemUpgradeModule(new Properties(), ItemUpgradeModule.UpgradeType.redstone_inverter));
	public static final Item EJECTOR_UPGRADE = Ic2rItems.register("ejector_upgrade", new ItemUpgradeModule(new Properties(), ItemUpgradeModule.UpgradeType.ejector));
	public static final Item ADVANCED_EJECTOR_UPGRADE = Ic2rItems.register("advanced_ejector_upgrade", new ItemUpgradeModule(new Properties(), ItemUpgradeModule.UpgradeType.advanced_ejector));
	public static final Item PULLING_UPGRADE = Ic2rItems.register("pulling_upgrade", new ItemUpgradeModule(new Properties(), ItemUpgradeModule.UpgradeType.pulling));
	public static final Item ADVANCED_PULLING_UPGRADE = Ic2rItems.register("advanced_pulling_upgrade", new ItemUpgradeModule(new Properties(), ItemUpgradeModule.UpgradeType.advanced_pulling));
	public static final Item FLUID_EJECTOR_UPGRADE = Ic2rItems.register("fluid_ejector_upgrade", new ItemUpgradeModule(new Properties(), ItemUpgradeModule.UpgradeType.fluid_ejector));
	public static final Item FLUID_PULLING_UPGRADE = Ic2rItems.register("fluid_pulling_upgrade", new ItemUpgradeModule(new Properties(), ItemUpgradeModule.UpgradeType.fluid_pulling));
	public static final Item REMOTE_INTERFACE_UPGRADE = Ic2rItems.register("remote_interface_upgrade", new ItemUpgradeModule(new Properties(), ItemUpgradeModule.UpgradeType.remote_interface));
	public static final Item FILLED_TIN_CAN = Ic2rItems.register("filled_tin_can", new ItemTinCan(new Properties()));
	public static final Item BLANK_TFBP = Ic2rItems.register("blank_tfbp", new Tfbp(new Properties().stacksTo(1), 0.0, 0, null));
	public static final Item CHILLING_TFBP = Ic2rItems.register("chilling_tfbp", new Tfbp(new Properties().stacksTo(1), 2000.0, 50, new Chilling()));
	public static final Item CULTIVATION_TFBP = Ic2rItems.register("cultivation_tfbp", new Tfbp(new Properties().stacksTo(1), 4000.0, 40, new Cultivation()));
	public static final Item DESERTIFICATION_TFBP = Ic2rItems.register("desertification_tfbp", new Tfbp(new Properties().stacksTo(1), 2500.0, 40, new Desertification()));
	public static final Item FLATIFICATION_TFBP = Ic2rItems.register("flatification_tfbp", new Tfbp(new Properties().stacksTo(1), 4000.0, 40, new Flatification()));
	public static final Item IRRIGATION_TFBP = Ic2rItems.register("irrigation_tfbp", new Tfbp(new Properties().stacksTo(1), 3000.0, 60, new Irrigation()));
	public static final Item MUSHROOM_TFBP = Ic2rItems.register("mushroom_tfbp", new Tfbp(new Properties().stacksTo(1), 8000.0, 25, new Mushroom()));
}
