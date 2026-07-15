
# IndustrialCraft 2: Refactored

[English](./README_EN.md) | [简体中文](./README.md)

## Attachments & Interactions

[IC++](https://github.com/HalfCooler/ICPlusPlus) Teleport Portal

Add this mod Gradle dependency: In `build.gradle`, adjust the imported packages as needed for `deobf` (if errors occur).

```gradle
repositories { mavenCentral() }

in dependencies { }:
compileOnly "io.github.halfcooler:ic2r-forge:{$ic2r_version}"
runtimeOnly "io.github.halfcooler:ic2r-forge:{$ic2r_version}"
```

## Write Before

IC2 is probably dead. There is no official response to the open source, and the successor developers are still absent.

But the community's voice has never stopped. Players have been waiting for too long, waiting for an IC2 that can run on higher versions and retain its original flavor.

Thus, someone stepped forward. [IC2CR](https://github.com/yu1745/ic2-fabric) pioneered the open-source effort, targeting the 1.20.1 Fabric platform with considerable momentum. It remains unclear how much the original work has been altered, but it is no longer a successor to IC2.

Our goal is: **To bring IC2, along with its attached and interacting mods, as complete and modernized as possible to 1.20.1 and beyond, while minimizing changes to existing functionality and values.**

It is still the IC2 you remember, but now it is IC2R.

However, we never intended to overshadow the original. The domestic public opinion environment is extremely poor, and I have suffered malicious attacks and threats. Once again, we emphasize that **we are not the official IC2 or its authorized successor**.

## Differences from the Origin

We understand that players choose this mod to restore the experience, so we have been extremely restrained in modifying the original version. Here are all the changes, without any concealment:

1. **Removed UU Matter in Item Form and Its Downstream Machines**.
    - IC2 Dev Team had already removed the recipe for the Mass Fabricator (`mass_fabricator`) in `2.8.222-ex112`, making the UU Matter replication function obsolete.
2. **Removed Purified Iron Ingot**.
    - This is an IC2C item and not part of IC2Exp. Smelting iron into purified iron ingot serves to replace steel ingots.
3. **Removed Ladder**.
    - Higher versions of Minecraft already come with ladders, making the IC2 ladder functionality redundant and unnecessary.
    - Additionally, for the reinforced scaffold spraying building foam into explosive stone generation method, we have reverted to a crafting design, i.e., 8 stones and 1 advanced alloy.
4. **Removed Pipes and Two Electric Pumps**.
    - The old version of pipes had incomplete functionality and compatibility issues.
    - We have removed the pipes to encourage players to use more modern logistics systems (such as AE2, Ender IO, etc.) as alternatives.
    - In the future, we may re-design or directly借鉴 GT pipes.
5. **Adjusted the Generation Parameters of Tin, Lead, and Uranium Ores**.
    - The original tin ore tends to generate at high altitudes, while players' demand for tin is much higher than for lead, and they prefer exploring deeper veins.
    - We have optimized its vertical distribution while keeping the total amount of tin slightly higher than lead, making the game pace more reasonable.
    - The generation quantity has been generally reduced based on the original values; the original design would make ore generation too dense.
6. **Adjusted the Wireless Charging Method of Charging Stations**.
    - The original charging stations could only charge items with a voltage level not exceeding that of the charging station.
    - 充电座可以为玩家身上任何电压等级的物品充电，但充电速度仍然受到充电座电压等级的限制。
7. **Added Mining Filter Card**.
    - Advanced mining machines can now use mining filter cards to specify the types of ores to mine, allowing players to customize their mining range.
8. **Added Optional GT-Style Power Grid**.
    - You can modify the power grid mode in the configuration file; the default remains the IC2 classic mode (no current verification, supply power packages as needed).
    - Under the GT power grid mode, cables have a current limit, and power packages are transmitted at integer currents and fixed voltages.
    - All text displays have been completely refactored into GT form. For example, `32 EU/t` will be displayed as `1A LV`.
9. **Added Beacon Base Tags to Bronze Blocks, Steel Blocks, Lead Blocks, and Uranium Blocks**.

## Copyright Statement

[IC2 Dev Team Quote](https://forum.industrial-craft.net/thread/9843-mc-1-7-ic%C2%B2-v-2-1-x-2-2-x-experimental/?postID=131008#post131008)

**This repository's resource files are copyrighted by the IC2 Dev Team.**

The repository license is GNU Affero General Public License v3.0 (AGPLv3). See [LICENSE](./LICENSE) for more information.

## Quick Start

Please use Java 17 to build and compile this branch.

For more information, please refer to [Release](./release.md).

```shell
gradlew build
gradlew runClient
gradlew runServer
gradlew syncVersion
```
