
# Industrial Craft 2: Refactored

[English](./README_EN.md) | [简体中文](./README.md)

[IC++](https://github.com/HalfCooler/ICPlusPlus) Portal

This project's code is based on the official build version `2.9.40-ex119` and has migrated missing and erroneous features from `2.8.222-ex112`.

## Writing Before

IC2 is probably dead. No official response for open-source, successor developers are still absent.

But the community's voice has never stopped. Players have been waiting for too long——waiting for a version of IC2 that can run on higher versions and retain its original flavor.

So someone stepped up. [IC2CR](https://github.com/yu1745/ic2-fabric) led the way in open-sourcing, targeting 1.20.1 Fabric with considerable momentum. However, upon closer inspection: mechanisms have been altered, values adjusted, and the UI also changed——with much of the migration work relying heavily on AI assistance. While completion may be acceptable, it is no longer the same IC2. It has become something else under the name of IC2.

We are different, even with the sword of copyright hanging over our heads. Our goal has always been singular: **to bring IC2 fully to 1.20.1 without changing its core functionality and values**.

> *Although this might seem like an intentional slight against them. But under the spirit of open-source, we still stand on the same front.*

In the end, we made it happen. What reaches players' hands is a complete and playable version of IC2 running on 1.20.1 Forge——still the IC2 you remember.

However, we never intended to overshadow the original. The domestic public opinion environment is poor, and I have suffered malicious attacks and threats. Once again, I emphasize, **we are not the official successors of IC2**. We are simply players who love IC2 and want to continue experiencing it in higher versions.

## Differences from the Original

We understand that players choose this mod to recreate the original experience, so we have been extremely cautious with changes to the original. Here are all the modifications, none hidden:

- **Removed the item form of UU matter** (`uu_matter`, the pink sticky ball) and its downstream related machines. The IC2 Dev Team had already removed the crafting recipe for the mass fabricator in `2.8.222-ex112`, rendering the remaining UU matter duplication function meaningless.
- **Removed the refined iron ingot** (`refined_iron_ingot`). This is an IC2C item and not part of IC2Exp——its function is to replace steel ingots.
- **Removed the scaffold** (`scaffold`). Higher versions of Minecraft already come with scaffolding, making IC2's scaffolding functionality redundant and unnecessary. Additionally, for the generation method of enhanced scaffolds spraying building foam into explosive stone, we have reverted to a crafting design, i.e., 8 stones and 1 advanced alloy.
- **Removed the pipes**. The old version of pipes had incomplete functionality and compatibility issues. We removed the pipes to encourage players to use more modern logistics systems (such as AE2, Ender IO, etc.) as replacements.
- **Adjusted the generation parameters of tin, lead, and uranium ores** (from the value changes in `2.9.40-ex119`). The original tin ore tends to generate in mountainous areas, while players' demand for tin is much higher than for lead, and they prefer exploring deeper ore veins. We have optimized its vertical distribution while keeping the total amount of tin slightly higher than lead, making the game pace more reasonable.
- **Adjusted the wireless charging method of charging stations**. The original charging station could only charge items on the player that are not higher than the station's voltage level. We changed it to: charging stations can charge any voltage level items on the player, but the charging speed is still limited by the station's voltage level.
- **Added ore filtering cards**. Advanced miners can now use ore filtering cards to specify the types of ores to mine, making it easier for players to customize their mining range.
- **Added optional GT mode power grid**. You can modify the power grid mode in the configuration file; by default, it remains as IC2 classic mode (no current verification, supply power packages as needed). In GT grid mode, wires have current limits, and power packages are transmitted at integer currents and fixed voltages. All text displays have been completely refactored into GT form. For example, `32 EU/t` will be displayed as `1A LV`.
- **Added beacon base tags for bronze blocks, steel blocks, lead blocks, and uranium blocks**.

## Copyright Notice

[IC2 Dev Team Quote](https://forum.industrial-craft.net/thread/9843-mc-1-7-ic%C2%B2-v-2-1-x-2-2-x-experimental/?postID=131008#post131008)

**The code in this repository belongs to the IC2 Dev Team. Even though my collaborators and I have refactored and fixed the code, we theoretically own the copyright to any original code we have added.**

The repository license is All Rights Reserved / No License. The copyright of this project belongs to the original IC2 Dev Team. We do not own the copyright of IC2's code and resource files, nor do we claim to own the copyright of IC2's code and resource files. We are simply decompiling, fixing, migrating, and unofficially maintaining IC2.

Based on this - and possibly never obtaining authorization from the original author - we will focus on redoing the code architecture once there are no clear bugs in this branch. Of course, the textures and resource files are still unclear. We want to retain the feel of the original while avoiding copyright issues.

### Mod File

The code obtained through decompilation (this repository) still belongs to the derivative work of the original. It cannot independently determine the licensing approach for the entire project.

Whether commercial servers are allowed to use this mod, I cannot decide alone, as the original author still holds the copyright to the underlying code.

I cannot legally claim that **any commercial server can freely use this mod**, as this involves copyrights I have no authority to dispose of.

Though I may write *allow commercial use*, if the original author does not permit it, then this authorization does not actually take effect for the parts of the code that the original author holds copyright over.

From a copyright perspective, the **original author** can theoretically request the repository to be taken down, stop distribution, and even oppose any form of public dissemination, regardless of whether it is commercial or not.

### Modpack

According to the above, you need to add the source of the mod, attach English and other language descriptions, and include the links specified by the original development team: `https://industrial-craft.net/` or `https://forum.industrial-craft.net/`.

## Quick Start

Please use Java 17 to build and compile this branch.

For more information, please refer to [Release](./release.md).

```shell
gradlew build
gradlew runClient
gradlew runServer
```
