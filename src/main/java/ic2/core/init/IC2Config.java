package ic2.core.init;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public class IC2Config {
  public static final ModConfigSpec SPEC;

  public static final Worldgen worldgen;
  public static final Protection protection;
  public static final Balance balance;
  public static final Recipes recipes;
  public static final Misc misc;
  public static final Debug debug;

  static {
    ModConfigSpec.Builder b = new ModConfigSpec.Builder();

    worldgen = new Worldgen(b);
    protection = new Protection(b);
    balance = new Balance(b);
    recipes = new Recipes(b);
    misc = new Misc(b);
    debug = new Debug(b);

    SPEC = b.build();
  }

  // -- worldgen ------------------------------------------------------------

  public static class Worldgen {
    public final ModConfigSpec.BooleanValue rubberTree;
    public final ModConfigSpec.ConfigValue<String> rubberTreeBlacklist;
    public final ModConfigSpec.DoubleValue oreDensityFactor;
    public final ModConfigSpec.DoubleValue treeDensityFactor;
    public final ModConfigSpec.BooleanValue normalizeHeight;
    public final ModConfigSpec.IntValue retrogenCheckLimit;
    public final ModConfigSpec.IntValue retrogenUpdateLimit;

    Worldgen(ModConfigSpec.Builder b) {
      b.push("worldgen");
      b.comment(
          "Enable generation of rubber trees in the world.",
          "If you want to control the rubber tree generation on NeoForge side,",
          "you need to modify the files under data/ic2/neoforge/biome_modifier.");
      rubberTree = b.define("rubberTree", true);
      b.comment(
          "Comma separated list of dimensions to not generate rubber trees in. Does nothing if rubber tree generation is disabled.");
      rubberTreeBlacklist = b.define("rubberTreeBlacklist", "");
      b.comment("Factor scaling the IC2 ore generation quantity.");
      oreDensityFactor = b.defineInRange("oreDensityFactor", 1.0, 0.0, Double.MAX_VALUE);
      b.comment("Factor scaling the IC2 tree generation quantity.");
      treeDensityFactor = b.defineInRange("treeDensityFactor", 1.0, 0.0, Double.MAX_VALUE);
      b.comment(
          "Enable scaling the generation y levels and quantities to the sea level according to the formula",
          "newValue = configuredValue * sealevel / 64.",
          "The ore density (ratio between stone and ores) will be independent on the world's sea level if enabled.");
      normalizeHeight = b.define("normalizeHeight", true);
      b.comment(
          "Maximum amount of chunks to check for retrogen viability each tick.",
          "Retrogen is the retroactive generation of terrain features, e.g. ores and trees.",
          "Set it to 0 to disable retrogen.",
          "When enabling this the recommended value is 16.");
      retrogenCheckLimit = b.defineInRange("retrogenCheckLimit", 0, 0, Integer.MAX_VALUE);
      b.comment(
          "Maximum amount of chunks to process for retrogen each tick. Values exceeding retrogenCheckLimit are being truncated.");
      retrogenUpdateLimit = b.defineInRange("retrogenUpdateLimit", 2, 0, Integer.MAX_VALUE);
      b.pop();
    }
  }

  // -- protection ----------------------------------------------------------

  public static class Protection {
    public final ModConfigSpec.BooleanValue wrenchLogging;
    public final ModConfigSpec.DoubleValue nukeExplosionPowerLimit;
    public final ModConfigSpec.DoubleValue reactorExplosionPowerLimit;
    public final ModConfigSpec.BooleanValue enableNuke;

    Protection(ModConfigSpec.Builder b) {
      b.push("protection");
      b.comment("Enable logging of players when they remove a machine using a wrench.");
      wrenchLogging = b.define("wrenchLogging", true);
      b.comment("Maximum Explosion power of a nuke, where TNT is 4.");
      nukeExplosionPowerLimit =
          b.defineInRange("nukeExplosionPowerLimit", 60.0, 0.0, Double.MAX_VALUE);
      b.comment("Maximum explosion power of a nuclear reactor, where TNT is 4.");
      reactorExplosionPowerLimit =
          b.defineInRange("reactorExplosionPowerLimit", 45.0, 0.0, Double.MAX_VALUE);
      b.comment("Enable the nuke.");
      enableNuke = b.define("enableNuke", true);
      b.pop();
    }
  }

  // -- balance -------------------------------------------------------------

  public static class Balance {
    public final ModConfigSpec.IntValue minerDischargeTier;
    public final ModConfigSpec.BooleanValue teleporterUseInventoryWeight;
    public final ModConfigSpec.DoubleValue energyRetainedInStorageBlockDrops;
    public final ModConfigSpec.IntValue massFabricatorTier;
    public final ModConfigSpec.IntValue matterFabricatorTier;
    public final ModConfigSpec.DoubleValue uuEnergyFactor;
    public final ModConfigSpec.BooleanValue disableEnderChest;
    public final ModConfigSpec.ConfigValue<String> recyclerBlacklist;
    public final ModConfigSpec.ConfigValue<String> recyclerWhitelist;
    public final ModConfigSpec.BooleanValue ignoreWrenchRequirement;
    public final ModConfigSpec.BooleanValue watermillAutomation;
    public final ModConfigSpec.DoubleValue euPerChunk;

    public final Energy energy;
    public final SteamKineticGenerator steamKineticGenerator;
    public final SteamGenerator steamGenerator;
    public final SteamRepressurizer steamRepressurizer;
    public final Fermenter fermenter;
    public final UuValues uuValues;

    Balance(ModConfigSpec.Builder b) {
      b.push("balance");
      b.comment(
          "Maximum battery tier usable by the miner. 1 = batteries, 2 = lead batteries, 3 = energy crystals, 4 = lapotron crystals.");
      minerDischargeTier = b.defineInRange("minerDischargeTier", 1, 1, 4);
      b.comment(
          "Increase the energy use by the player's inventory weight when going through a teleporter.");
      teleporterUseInventoryWeight = b.define("teleporterUseInventoryWeight", true);
      b.comment(
          "Ratio of energy retained inside energy storage block Items when wrenched. 0 (nothing) ... 1 (100%), default 0.8 (80%).");
      energyRetainedInStorageBlockDrops =
          b.defineInRange("energyRetainedInStorageBlockDrops", 0.8, 0.0, 1.0);
      b.comment("Power tier of the Mass Fabricator.");
      massFabricatorTier = b.defineInRange("massFabricatorTier", 3, 1, Integer.MAX_VALUE);
      b.comment("Power tier of the Matter Fabricator.");
      matterFabricatorTier = b.defineInRange("matterFabricatorTier", 3, 1, Integer.MAX_VALUE);
      b.comment("Factor to scale the UU-Matter production energy requirement.");
      uuEnergyFactor = b.defineInRange("uuEnergyFactor", 1.0, 0.0, Double.MAX_VALUE);
      b.comment("Disable the vanilla ender chest, removing existing ones from the world as well.");
      disableEnderChest = b.define("disableEnderChest", false);
      b.comment(
          "Comma separated list of blocks and items which should not be turned into scrap by the recycler.",
          "Format: <name>[@metadata], metadata * matches any.",
          "Ore dictionary entries can be specified with OreDict:<ore dict name> as the name.");
      recyclerBlacklist =
          b.define(
              "recyclerBlacklist",
              "minecraft:glass_pane, minecraft:stick, minecraft:snowball, minecraft:snow, minecraft:snow_block");
      b.comment(
          "Whitelist for blocks/items allowed to be recycled.",
          "The whitelist will be used instead of the blacklist approach if it's non-empty, disallowing everything else.",
          "The format is the same as the blacklist.");
      recyclerWhitelist = b.define("recyclerWhitelist", "");
      b.comment("Allow to pick blocks up using just a pickaxe instead of needing a wrench.");
      ignoreWrenchRequirement = b.define("ignoreWrenchRequirement", false);
      b.comment("Allow the water slot of water mills to be automated.");
      watermillAutomation = b.define("watermillAutomation", false);
      b.comment("How many EU the Chunkloader uses per chunk it loads.");
      euPerChunk = b.defineInRange("euPerChunk", 1.0, 0.0, Double.MAX_VALUE);

      energy = new Energy(b);
      steamKineticGenerator = new SteamKineticGenerator(b);
      steamGenerator = new SteamGenerator(b);
      steamRepressurizer = new SteamRepressurizer(b);
      fermenter = new Fermenter(b);
      uuValues = new UuValues(b);
      b.pop();
    }

    public static class Energy {
      public final Generator generator;
      public final HeatGenerator heatGenerator;
      public final KineticGenerator kineticGenerator;
      public final FluidConversion fluidConversion;
      public final FluidReactor fluidReactor;

      Energy(ModConfigSpec.Builder b) {
        b.push("energy");
        generator = new Generator(b);
        heatGenerator = new HeatGenerator(b);
        kineticGenerator = new KineticGenerator(b);
        fluidConversion = new FluidConversion(b);
        fluidReactor = new FluidReactor(b);
        b.pop();
      }

      public static class Generator {
        public final ModConfigSpec.DoubleValue generator;
        public final ModConfigSpec.DoubleValue geothermal;
        public final ModConfigSpec.DoubleValue water;
        public final ModConfigSpec.DoubleValue solar;
        public final ModConfigSpec.DoubleValue wind;
        public final ModConfigSpec.DoubleValue nuclear;
        /** Multiplier on base 32 EU/t for each semi-fluid fuel (0 disables registration). */
        public final ModConfigSpec.DoubleValue semiFluidBiogas;
        public final ModConfigSpec.DoubleValue semiFluidBiomass;
        public final ModConfigSpec.DoubleValue semiFluidHydrogen;
        public final ModConfigSpec.DoubleValue semiFluidCreosote;
        public final ModConfigSpec.DoubleValue stirling;
        public final ModConfigSpec.DoubleValue kinetic;
        public final ModConfigSpec.DoubleValue radioisotope;

        Generator(ModConfigSpec.Builder b) {
          b.push("generator");
          b.comment("Base energy generation factors - increase for higher energy yield.");
          generator = b.defineInRange("generator", 1.0, 0.0, Double.MAX_VALUE);
          geothermal = b.defineInRange("geothermal", 1.0, 0.0, Double.MAX_VALUE);
          water = b.defineInRange("water", 1.0, 0.0, Double.MAX_VALUE);
          solar = b.defineInRange("solar", 1.0, 0.0, Double.MAX_VALUE);
          wind = b.defineInRange("wind", 1.0, 0.0, Double.MAX_VALUE);
          nuclear = b.defineInRange("nuclear", 1.0, 0.0, Double.MAX_VALUE);
          b.comment("Semi-fluid generator fuel EU/t multipliers (base 32 EU/t; 0 disables that fuel).");
          semiFluidBiogas = b.defineInRange("semiFluidBiogas", 1.0, 0.0, Double.MAX_VALUE);
          semiFluidBiomass = b.defineInRange("semiFluidBiomass", 1.0, 0.0, Double.MAX_VALUE);
          semiFluidHydrogen = b.defineInRange("semiFluidHydrogen", 1.0, 0.0, Double.MAX_VALUE);
          semiFluidCreosote = b.defineInRange("semiFluidCreosote", 1.0, 0.0, Double.MAX_VALUE);
          stirling = b.defineInRange("Stirling", 1.0, 0.0, Double.MAX_VALUE);
          kinetic = b.defineInRange("Kinetic", 1.0, 0.0, Double.MAX_VALUE);
          radioisotope = b.defineInRange("radioisotope", 1.0, 0.0, Double.MAX_VALUE);
          b.pop();
        }
      }

      public static class HeatGenerator {
        /** Multiplier on fluid heat generator fuels (0 disables that fuel). */
        public final ModConfigSpec.DoubleValue semiFluidBiogas;
        public final ModConfigSpec.DoubleValue solid;
        public final ModConfigSpec.DoubleValue radioisotope;
        public final ModConfigSpec.DoubleValue electric;

        HeatGenerator(ModConfigSpec.Builder b) {
          b.push("heatgenerator");
          b.comment("Base heat generation factors - increase for higher heat yield.");
          semiFluidBiogas = b.defineInRange("semiFluidBiogas", 1.0, 0.0, Double.MAX_VALUE);
          solid = b.defineInRange("solid", 1.0, 0.0, Double.MAX_VALUE);
          radioisotope = b.defineInRange("radioisotope", 1.0, 0.0, Double.MAX_VALUE);
          electric = b.defineInRange("electric", 1.0, 0.0, Double.MAX_VALUE);
          b.pop();
        }
      }

      public static class KineticGenerator {
        public final ModConfigSpec.DoubleValue water;
        public final ModConfigSpec.DoubleValue wind;
        public final ModConfigSpec.DoubleValue manual;
        public final ModConfigSpec.DoubleValue steam;
        public final ModConfigSpec.DoubleValue electric;

        KineticGenerator(ModConfigSpec.Builder b) {
          b.push("kineticgenerator");
          b.comment("Base kinetic generation factors - increase for higher kinetic energy yield.");
          water = b.defineInRange("water", 1.0, 0.0, Double.MAX_VALUE);
          wind = b.defineInRange("wind", 1.0, 0.0, Double.MAX_VALUE);
          manual = b.defineInRange("manual", 1.0, 0.0, Double.MAX_VALUE);
          steam = b.defineInRange("steam", 1.0, 0.0, Double.MAX_VALUE);
          electric = b.defineInRange("electric", 1.0, 0.0, Double.MAX_VALUE);
          b.pop();
        }
      }

      public static class FluidConversion {
        public final ModConfigSpec.DoubleValue heatExchangerLava;
        public final ModConfigSpec.DoubleValue heatExchangerHotCoolant;
        public final ModConfigSpec.DoubleValue heatExchangerWater;

        FluidConversion(ModConfigSpec.Builder b) {
          b.push("fluidconversion");
          b.comment("Basically the amount of hU the conversion of one mB of Liquid takes/gives");
          b.comment("Lava -> PahoehoeLava");
          heatExchangerLava = b.defineInRange("heatExchangerLava", 1.0, 0.0, Double.MAX_VALUE);
          b.comment("Hot Coolant <-> Cold Coolant");
          heatExchangerHotCoolant =
              b.defineInRange("heatExchangerHotCoolant", 1.0, 0.0, Double.MAX_VALUE);
          b.comment("Hot Water <- Water");
          heatExchangerWater = b.defineInRange("heatExchangerWater", 1.0, 0.0, Double.MAX_VALUE);
          b.pop();
        }
      }

      public static class FluidReactor {
        public final ModConfigSpec.DoubleValue outputModifier;

        FluidReactor(ModConfigSpec.Builder b) {
          b.push("FluidReactor");
          b.comment("Base Coolant conversion rate for FluidReactors.");
          outputModifier = b.defineInRange("outputModifier", 1.0, 0.0, Double.MAX_VALUE);
          b.pop();
        }
      }
    }

    public static class SteamKineticGenerator {
      public final ModConfigSpec.IntValue rotorlivetime;

      SteamKineticGenerator(ModConfigSpec.Builder b) {
        b.push("SteamKineticGenerator");
        b.comment("Balance value for turbine lifetime in sec. Default 86400 sec = 24h.");
        rotorlivetime = b.defineInRange("rotorlivetime", 86400, 0, Integer.MAX_VALUE);
        b.pop();
      }
    }

    public static class SteamGenerator {
      public final Calcification calcification;

      SteamGenerator(ModConfigSpec.Builder b) {
        b.push("steamgenerator");
        calcification = new Calcification(b);
        b.pop();
      }

      public static class Calcification {
        public final ModConfigSpec.IntValue maxcalcification;

        Calcification(ModConfigSpec.Builder b) {
          b.push("calcification");
          b.comment("Balance values for calcification. Default 100,000 mB water to failure.");
          maxcalcification = b.defineInRange("maxcalcification", 100000, 0, Integer.MAX_VALUE);
          b.pop();
        }
      }
    }

    public static class SteamRepressurizer {
      public final ModConfigSpec.IntValue steamPerSteam;
      public final ModConfigSpec.IntValue steamPerSuperSteam;

      SteamRepressurizer(ModConfigSpec.Builder b) {
        b.push("steamRepressurizer");
        b.comment("Amount (in mb) of standard steam per 10mb of IC2 steam.");
        steamPerSteam = b.defineInRange("steamPerSteam", 16, 0, Integer.MAX_VALUE);
        b.comment("Amount (in mb) of standard steam per 10mb of IC2 super-heated steam.");
        steamPerSuperSteam = b.defineInRange("steamPerSuperSteam", 32, 0, Integer.MAX_VALUE);
        b.pop();
      }
    }

    public static class Fermenter {
      public final ModConfigSpec.IntValue needAmountBiomassPerRun;
      public final ModConfigSpec.IntValue outputAmountBiogasPerRun;
      public final ModConfigSpec.IntValue hUPerRun;
      public final ModConfigSpec.IntValue biomassPerFertilizier;

      Fermenter(ModConfigSpec.Builder b) {
        b.push("fermenter");
        b.comment("Balance values for Fermenter.");
        needAmountBiomassPerRun =
            b.defineInRange("need_amount_biomass_per_run", 20, 0, Integer.MAX_VALUE);
        outputAmountBiogasPerRun =
            b.defineInRange("output_amount_biogas_per_run", 400, 0, Integer.MAX_VALUE);
        hUPerRun = b.defineInRange("hU_per_run", 4000, 0, Integer.MAX_VALUE);
        biomassPerFertilizier =
            b.defineInRange("biomass_per_fertilizier", 500, 0, Integer.MAX_VALUE);
        b.pop();
      }
    }

    public static class UuValues {
      public final ModConfigSpec.ConfigValue<List<? extends String>> predefined;
      public final ModConfigSpec.ConfigValue<List<? extends String>> worldScan;

      UuValues(ModConfigSpec.Builder b) {
        b.push("uu-values");
        b.comment(
            "Additional initial UU values. Format: \"mod:item value\".",
            "A value of 1 equals cobblestone.",
            "Recipes may cause the final value to be lower than the one specified here.");
        predefined =
            b.defineListAllowEmpty(
                "predefined",
                () -> {
                  List<String> defaults = new ArrayList<>();
                  defaults.add("ic2:iridium_ore 12000");
                  defaults.add("ic2:iridium_shard 1333");
                  return defaults;
                },
                e -> e instanceof String);
        b.comment("UU world scan values (populated by /ic2 uu-world-scan command).");
        worldScan = b.defineListAllowEmpty("worldScan", ArrayList::new, e -> e instanceof String);
        b.pop();
      }
    }
  }

  // -- recipes -------------------------------------------------------------

  public static class Recipes {
    public final ModConfigSpec.ConfigValue<String> disable;
    public final ModConfigSpec.ConfigValue<String> purge;
    public final ModConfigSpec.ConfigValue<String> jetpackAttachmentBlacklist;
    public final ModConfigSpec.BooleanValue allowCoinCrafting;
    public final ModConfigSpec.BooleanValue requireIc2Circuits;
    public final ModConfigSpec.BooleanValue smeltToIc2Items;
    public final ModConfigSpec.BooleanValue ignoreInvalidRecipes;

    Recipes(ModConfigSpec.Builder b) {
      b.push("recipes");
      b.comment(
          "To modify recipes or add custom recipes, copy the corresponding .ini file to",
          "\"minecraft/config/ic2\" and modify it.",
          "",
          "Disable IC2 crafting recipes with the specified output, comma separated list.",
          "Non-IC2 recipes can be removed via the purge list below.",
          "Format: <name>[@metadata], e.g. minecraft:bucket to disable IC2's bucket recipe from tin.");
      disable = b.define("disable", "");
      b.comment(
          "Purge all non-IC2 crafting recipes with the specified output, comma separated list.",
          "IC2 recipes can be removed via the disable list above.",
          "Format: <name>[@metadata], e.g. minecraft:tnt to disable crafting tnt.");
      purge = b.define("purge", "");
      b.comment(
          "Blacklist for items which the jetpack attachment plate cannot be used on.",
          "The format: <name>, e.g. minecraft:leather_chestplate to disable attaching jetpacks to leather tunics.");
      jetpackAttachmentBlacklist = b.define("jetpackAttachmentBlacklist", "");
      b.comment(
          "Enable crafting of IC2 coins, otherwise they have to be spawned in and are thus limited.");
      allowCoinCrafting = b.define("allowCoinCrafting", true);
      b.comment("Allow only IC2 circuits to be used in IC2's recipes.");
      requireIc2Circuits = b.define("requireIc2Circuits", false);
      b.comment("Adjust smelting recipes to always output IC2 items if available.");
      smeltToIc2Items = b.define("smeltToIc2Items", false);
      b.comment("Ignore invalid recipes.");
      ignoreInvalidRecipes = b.define("ignoreInvalidRecipes", false);
      b.pop();
    }
  }

  // -- misc ----------------------------------------------------------------

  public static class Misc {
    public final ModConfigSpec.BooleanValue allowBurningScrap;
    public final ModConfigSpec.ConfigValue<String> additionalValuableOres;
    public final ModConfigSpec.BooleanValue useLinearTransferModel;
    public final ModConfigSpec.BooleanValue roundEnetLoss;
    public final ModConfigSpec.BooleanValue enableEnetExplosions;
    public final ModConfigSpec.BooleanValue enableEnetCableMeltdown;

    Misc(ModConfigSpec.Builder b) {
      b.push("misc");
      b.comment("Enable burning of scrap in a generator.");
      allowBurningScrap = b.define("allowBurningScrap", true);
      b.comment(
          "Comma separated list with ores the miner should harvest.",
          "Format: <name>[@metadata], e.g. minecraft:torch, minecraft:chest",
          "The metadata * will match any, e.g. minecraft:log@*.",
          "Ore dictionary entries can be specified with OreDict:<ore dict name> as the name.");
      additionalValuableOres = b.define("additionalValuableOres", "");
      b.comment(
          "Use the new highly experimental current + voltage energy net model with energy loss. Only set this to true if you know what you are doing.");
      useLinearTransferModel = b.define("useLinearTransferModel", false);
      b.comment("Configure whether energy loss is rounded down to the nearest whole EU or not.");
      roundEnetLoss = b.define("roundEnetLoss", true);
      b.comment(
          "If you really really really don't want to have your machines explode if you don't pay attention to voltage, you can disable that here.");
      enableEnetExplosions = b.define("enableEnetExplosions", true);
      b.comment("Same for cable meltdown.");
      enableEnetCableMeltdown = b.define("enableEnetCableMeltdown", true);
      b.pop();
    }
  }

  // -- debug ---------------------------------------------------------------

  public static class Debug {
    public final ModConfigSpec.BooleanValue logEmptyWrenchDrops;
    public final ModConfigSpec.BooleanValue logIncorrectItemDamaging;
    public final ModConfigSpec.BooleanValue logGridUpdateIssues;
    public final ModConfigSpec.BooleanValue logEnetApiAccesses;
    public final ModConfigSpec.BooleanValue logEnetApiAccessTraces;
    public final ModConfigSpec.BooleanValue logGridUpdatesVerbose;
    public final ModConfigSpec.BooleanValue logGridCalculationIssues;

    Debug(ModConfigSpec.Builder b) {
      b.push("debug");
      b.comment("Log whenever wrenching didn't produce any drops.");
      logEmptyWrenchDrops = b.define("logEmptyWrenchDrops", true);
      b.comment(
          "Log incorrect damage applications to items that aren't supposed to ever take damage.");
      logIncorrectItemDamaging = b.define("logIncorrectItemDamaging", false);
      b.comment(
          "Log problems occurring when processing connectivity updates in the energy network.");
      logGridUpdateIssues = b.define("logGridUpdateIssues", true);
      b.comment("Log EnergyNet API accesses that aren't read-only - use this to debug IC2 addons.");
      logEnetApiAccesses = b.define("logEnetApiAccesses", false);
      b.comment(
          "Log EnergyNet API accesses with stack traces that aren't read-only - use this to debug IC2 addons.");
      logEnetApiAccessTraces = b.define("logEnetApiAccessTraces", false);
      b.comment("Log all energy net connectivity updates in detail.");
      logGridUpdatesVerbose = b.define("logGridUpdatesVerbose", false);
      b.comment("Log problems occurring during energy network calculations.");
      logGridCalculationIssues = b.define("logGridCalculationIssues", true);
      b.pop();
    }
  }
}
