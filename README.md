
# IndustrialCraft 2: Refactored

<img src="https://img.shields.io/badge/Minecraft-1.21.1-brightgreen" alt="Minecraft 1.21.1">
<img src="https://img.shields.io/badge/NeoForge-21.1.234-orange" alt="NeoForge 21.1.234">
<img src="https://img.shields.io/badge/Version-21.1.0-blue" alt="Version 21.1.0">
<img src="https://img.shields.io/badge/License-AGPL--3.0-blue" alt="License AGPL-3.0">

> [!WARNING]
> This repository is an **experimental fork** used for porting and testing changes.
> For general use, prefer the upstream project: **[HalfCooler/IC2R](https://github.com/HalfCooler/IC2R)**.

This project's code was obtained by decompiling the official build `2.9.40-ex119`, with missing and broken functionality migrated over from `2.8.222-ex112`.

## Foreword

IC2 is probably dead. No official word on open-sourcing it, and the successor developers remain absent.

But the community's call never went quiet. Players have waited too long — waited for an authentic IC2 that runs on modern versions.

So people stepped up. [IC2CR](https://github.com/yu1745/ic2-fabric) open-sourced first, targeting 1.20.1 Fabric, with considerable fanfare. But open it up and look: the mechanics changed, the values changed, the UI changed — much of the migration was even delegated to AI. The level of completeness may be acceptable, but it is no longer IC2. It is something else wearing IC2's name.

We are different, even with the sword of copyright hanging over our heads. From the very beginning we have had exactly one goal: **bring IC2 to modern Minecraft in its entirety, while changing the original functionality and values as little as possible.**

In the end, we did it. What reached players' hands was a complete, playable IC2 — originally on 1.20.1 Forge, now tracking NeoForge 1.21.1 — and it is still the IC2 you remember.

Yet we never intended to upstage the original. The public discourse around this has been very hostile; I have received malicious attacks and threats. To stress it once more: **we are not IC2's official successors.** We are simply players who love IC2 and want to keep playing it on modern versions.

## Differences from the original

We know players choose this mod for the faithful experience, so changes to the original are extremely restrained. Below is the complete list of changes, with nothing hidden:

- **Removed item-form UU-Matter** (`uu_matter`, the pink slimeball) and its downstream machines. The IC2 Dev Team already removed the Mass Fabricator (`mass_fabricator`) crafting recipe back in `2.8.222-ex112`, so the leftover UU-Matter replication features no longer had a reason to exist.
- **Removed the Refined Iron Ingot** (`refined_iron_ingot`). It is an IC2 Classic item, not part of IC2 Experimental — its role is filled by the Steel Ingot.
- **Removed the Scaffold** (`scaffold`). Modern Minecraft ships its own scaffolding, making IC2's redundant and unnecessary. In addition, obtaining Reinforced Stone by spraying Construction Foam onto Iron Scaffolds has been reverted to a crafting recipe — 8 Stone plus 1 Advanced Alloy.
- **Adjusted ore generation parameters for tin, lead, and uranium** (carried over from the `2.9.40-ex119` value changes). Vanilla-IC2 tin skews toward mountain generation, yet players need far more tin than lead and prefer exploring deep veins. While keeping the total amount of tin slightly above lead, we optimized its vertical distribution for a more reasonable game pace.
- **Adjusted the charge pad's wireless charging.** Originally a charge pad could only charge items on the player at or below the pad's own voltage tier. Now the pad charges items of any voltage tier, but the charging speed is still limited by the pad's tier.
- **Added the Mining Filter Card.** The Advanced Miner can now use a filter card to specify which ores to mine, letting players customize the mining scope.

## Copyright notice

[IC2 Dev Team citation](https://forum.industrial-craft.net/thread/9843-mc-1-7-ic%C2%B2-v-2-1-x-2-2-x-experimental/?postID=131008#post131008)

**The code in this repository belongs to the IC2 Dev Team. Even though my collaborators and I have refactored and fixed the code, in theory we hold copyright only over the original code we ourselves added.**

This repository is licensed under AGPL-3.0 (see [LICENSE](./LICENSE)), which applies to our own original contributions. The copyright of the underlying project belongs to the original IC2 Dev Team. We do not own — and do not claim to own — the copyright to IC2's code and asset files. We merely decompile, fix, port, and maintain IC2.

Given this — and authorization from the original authors may never come — once this branch has no known bugs, we will focus on rebuilding the code architecture. Textures and asset files remain an open question: we want to keep the original feel while avoiding copyright problems.

### Mod file

The decompiled code (this repository) is still a derivative work of the original. It cannot unilaterally determine the licensing of the project as a whole.

Whether commercial servers may use it is not something I can decide alone, because the original authors still own the copyright to the underlying code.

I cannot legally declare that **any commercial server may freely use this mod**, since that involves rights I have no authority to grant.

Even if I wrote *commercial use permitted*, if the original authors do not allow it, that grant would simply have no effect on the parts they hold copyright to.

From a copyright standpoint, the **original authors** could in theory demand that the repository be taken down, that distribution stop, or oppose any form of public distribution, commercial or not.

### Modpacks

In addition to the above, you must credit the mod's origin, include the English description and descriptions in other languages, and the links designated by the original dev team: `https://industrial-craft.net/` or `https://forum.industrial-craft.net/`.

## Quick start

Build this branch with Java 21.

For more information, see [Release](./release.md).

```shell
gradlew build
gradlew runClient
gradlew runServer
```
