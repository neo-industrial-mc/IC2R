
# 工业时代 2：重构

<img src="https://img.shields.io/badge/Minecraft-1.20.1-green" alt="Minecraft 1.20.1">
<img src="https://img.shields.io/badge/Forge-47.4.20-orange" alt="Forge 47.4.20">
<img src="https://img.shields.io/badge/Version-20.1.41-blue" alt="Version 20.1.41">
<img src="https://img.shields.io/badge/License-AGPLv3-yellow" alt="License AGPLv3">

[English](./README_EN.md) | [简体中文](./README.md)

## 附属与联动

[IC++](https://github.com/HalfCooler/ICPlusPlus) 传送门

添加本模组 Gradle 依赖：在 `build.gradle`，按需对导入的包进行 `deobf`（如果报错的话）。

```gradle
repositories { mavenCentral() }

in dependencies { }:
compileOnly "io.github.halfcooler:ic2r-forge:{$ic2r_version}"
runtimeOnly "io.github.halfcooler:ic2r-forge:{$ic2r_version}"
```

## 写在前面

IC2 大概是死了。没有官方回复的开源，继任开发者仍在缺席。

但社区的呼声从未消停。玩家们等待了太久，等待一个能在高版本运行的、原汁原味的 IC2。

于是有人站了出来。[IC2CR](https://github.com/yu1745/ic2-fabric) 率先开源，目标平台是 1.20.1 Fabric，声势不小。尚不清楚对原作开刀多少，但那早已不再是 IC2 的后继。

我们的目标是：**在尽可能不改变原有功能与数值的前提下，将 IC2，以及附属联动模组，尽量完整、现代化地带到 1.20.1 和更高版本。**

它仍旧是那个你记忆中的 IC2，只不过现在它是 IC2R。

然而我们从未打算喧宾夺主。国内的舆论环境极差，我曾遭受恶意攻击和威胁。再次强调，**我们不是 IC2 官方或官方授权的继任者**。

## 与原版的差异

我们深知玩家选择本 mod 是为了还原体验，因此对原版的改动极为克制。以下是全部变更，无一隐瞒：

1. **移除了物品形态的 UU 物质及其下游相关机器**。 
    - IC2 Dev Team 早在 `2.8.222-ex112` 中便已删除物质生成机（`mass_fabricator`）的合成配方，遗留的 UU 物质复制功能已无存在意义。
2. **移除了精炼铁锭**。 
    - 这是 IC2C 的物品，并非 IC2Exp 的组成部分。烧铁变成精炼铁锭，它的职能是替代钢锭。
3. **移除了脚手架**。
    - 高版本 Minecraft 已经自带了脚手架，IC2 的脚手架功能重复且不必要。
    - 另外对于强化脚手架喷淋建筑泡沫变成防爆石的生成方式，我们回退为合成设计，即 8 个石头和 1 个高级合金。
4. **移除了管道与两个电动泵**。
    - 旧版管道功能不全且存在兼容性问题。
    - 我们删除管道，以鼓励玩家使用更现代的物流系统（如 AE2、Ender IO 等）来替代。
    - 今后我们可能会重新设计或直接借鉴 GT 管道。
5. **调整了锡、铅、铀矿石的生成参数**。
    - 原版锡矿偏向高山生成，而玩家对锡的需求远高于铅、且更倾向于探索深层矿脉。
    - 我们在保持锡总量略多于铅的前提下，优化了其竖直分布，使游戏节奏更为合理。
    - 在原数值的基础上普遍降低了生成数量，原数值设计将矿石生成过分密集。
6. **调整了充电座的无线充电方式**。
    - 原版充电座只能为玩家身上不高于充电座电压等级的物品充电。
    - 充电座可以为玩家身上任何电压等级的物品充电，但充电速度仍然受到充电座电压等级的限制。
7. **新增了矿石过滤卡**。
    - 高级采矿机现在可以使用矿石过滤卡来指定采矿的矿石种类，方便玩家自定义采矿范围。
8. **新增了可选的 GT 模式电网**。
   - 您可以在配置文件中修改电网模式，默认仍为 IC2 经典模式（不验证电流，按需供给电力包）。
   - GT 电网模式下，导线具有电流上限，且电力包是以整数电流、固定电压收发。
   - 所有文本显示已完全重构为 GT 的形式。例如，`32 EU/t` 将显示为 `1A LV`。
9. **为青铜块、钢块、铅块和铀块添加了信标基座标签**。

## 版权声明

[IC2 Dev Team 引文](https://forum.industrial-craft.net/thread/9843-mc-1-7-ic%C2%B2-v-2-1-x-2-2-x-experimental/?postID=131008#post131008)

**本仓库的资源文件版权归属 IC2 Dev Team。**

仓库许可证为 GNU Affero General Public License v3.0（AGPLv3）。查看 [LICENSE](./LICENSE) 了解更多。

## 快速开始

请使用 Java 17 构建和编译本分支。

其他信息请查看 [Release](./release.md)。

```shell
gradlew build
gradlew runClient
gradlew runServer
gradlew syncVersion
```
