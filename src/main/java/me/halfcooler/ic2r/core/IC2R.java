package me.halfcooler.ic2r.core;

import me.halfcooler.ic2r.core.proxy.EnvProxy;
import me.halfcooler.ic2r.core.proxy.SideProxy;
import me.halfcooler.ic2r.core.proxy.SideProxyClient;
import me.halfcooler.ic2r.core.proxy.SideProxyServer;
import me.halfcooler.ic2r.core.sound.SoundManager;
import me.halfcooler.ic2r.core.util.Keyboard;
import me.halfcooler.ic2r.core.util.Log;
import me.halfcooler.ic2r.core.util.PriorityExecutor;
import me.halfcooler.ic2r.core.util.SideGateway;
import me.halfcooler.ic2r.forge.EnvProxyForge;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;

public class IC2R
{
	public static final EnvProxy envProxy;
	public static final SideProxy sideProxy;
	public static final Log log;
	public static final SideGateway network;
	public static final Keyboard keyboard;
	public static final SoundManager soundManager;
	public static final CreativeModeTab tabIc2rGeneral;
	public static final CreativeModeTab tabIc2rGeneratorsAndWiring;
	public static final CreativeModeTab tabIc2rReactor;
	public static final CreativeModeTab tabIc2rMachines;
	public static final CreativeModeTab tabIc2rToolsAndUtilities;
	public static final CreativeModeTab tabIc2rFluidCells;
	public static final CreativeModeTab tabIc2rCombat;
	public static final CreativeModeTab tabIc2rFarming;
	public static final CreativeModeTab tabIc2rMaterials;
	public static final PriorityExecutor threadPool;
	public static final RandomSource random;
	public static boolean initialized;
	public static boolean suddenlyHoes;
	public static boolean seasonal;

	static
	{
		envProxy = createEnvProxy();
		sideProxy = createSideProxy();
		log = new Log(LogManager.getLogger("ic2r"));
		network = new SideGateway();
		keyboard = sideProxy.getKeyboard();
		soundManager = sideProxy.getSoundManager();
		tabIc2rGeneral = envProxy.createItemGroup(getIdentifier("general"), new ItemGroupIconSupplier(Ic2rItemGroupType.GENERAL), Ic2rItemGroupType.GENERAL);
		tabIc2rGeneratorsAndWiring = envProxy.createItemGroup(getIdentifier("generators_and_wiring"), new ItemGroupIconSupplier(Ic2rItemGroupType.GENERATORS_AND_WIRING), Ic2rItemGroupType.GENERATORS_AND_WIRING);
		tabIc2rReactor = envProxy.createItemGroup(getIdentifier("reactor"), new ItemGroupIconSupplier(Ic2rItemGroupType.REACTOR), Ic2rItemGroupType.REACTOR);
		tabIc2rMachines = envProxy.createItemGroup(getIdentifier("machines"), new ItemGroupIconSupplier(Ic2rItemGroupType.MACHINES), Ic2rItemGroupType.MACHINES);
		tabIc2rToolsAndUtilities = envProxy.createItemGroup(getIdentifier("tools_and_utilities"), new ItemGroupIconSupplier(Ic2rItemGroupType.TOOLS_AND_UTILITIES), Ic2rItemGroupType.TOOLS_AND_UTILITIES);
		tabIc2rFluidCells = envProxy.createItemGroup(getIdentifier("fluid_cells"), new ItemGroupIconSupplier(Ic2rItemGroupType.FLUID_CELLS), Ic2rItemGroupType.FLUID_CELLS);
		tabIc2rCombat = envProxy.createItemGroup(getIdentifier("combat"), new ItemGroupIconSupplier(Ic2rItemGroupType.COMBAT), Ic2rItemGroupType.COMBAT);
		tabIc2rFarming = envProxy.createItemGroup(getIdentifier("farming"), new ItemGroupIconSupplier(Ic2rItemGroupType.FARMING), Ic2rItemGroupType.FARMING);
		tabIc2rMaterials = envProxy.createItemGroup(getIdentifier("materials"), new ItemGroupIconSupplier(Ic2rItemGroupType.MATERIALS), Ic2rItemGroupType.MATERIALS);
		threadPool = new PriorityExecutor(Math.max(Runtime.getRuntime().availableProcessors(), 2));
		random = RandomSource.createNewThreadLocalInstance();
		initialized = false;
		suddenlyHoes = false;
		seasonal = false;
	}

	public static int getSeaLevel(Level world)
	{
		return world.getSeaLevel();
	}

	public static int getWorldMaxHeight(Level world)
	{
		return world.getHeight();
	}

	public static int getWorldMinHeight(Level world)
	{
		return world.getMinBuildHeight();
	}

	public static ResourceLocation getIdentifier(String name)
	{
		return ResourceLocation.fromNamespaceAndPath("ic2r", name);
	}

	public static void grantAdvancement(Player player, String path)
	{
		if (player instanceof ServerPlayer sp)
		{
			Advancement adv = sp.server.getAdvancements().getAdvancement(ResourceLocation.fromNamespaceAndPath("ic2r", path));
			if (adv != null)
			{
				sp.getAdvancements().award(adv, "impossible");
			}
		}
	}

	private static EnvProxy createEnvProxy()
	{
		return new EnvProxyForge();
	}

	private static SideProxy createSideProxy()
	{
		return envProxy.isClientEnv() ? new SideProxyClient() : new SideProxyServer();
	}
}
