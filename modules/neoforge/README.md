# `ic2r-neoforge`（规划 / 实验）

> G3.7 骨架。**无可运行源码或依赖**；最小集目标见 [g3_2_neoforge_min_set.md](../../docs/spec/g3_2_neoforge_min_set.md)。

## 最终源码落点（目标）

| 内容 | 说明 |
|:---|:---|
| NeoForge Mod 入口 | 对标今日 `FmlMod` |
| `NeoForgePlatformServices` | 实现 8 个 SPI facet |
| 最小集接线 | 物品注册 + 1 台机器 + EU 电网 tick（M1–M5） |
| loader 元数据 | NeoForge 对应 mods meta / mixin（若需要） |

**不**复制整棵 `core`；编译依赖 `ic2r-common`。

## 版本线

- 短期验证：**选项 A** 同版 NeoForge 1.20.1（对照 Forge 稳定线）  
- 长期主线是否升 **选项 B**（更高 MC）：M8 再评估  
- 见 [neoforge_migration_plan.md](../../docs/spec/neoforge_migration_plan.md) §2  

## 依赖

```text
ic2r-neoforge  ──implements──►  platform.services
ic2r-neoforge  ──depends on──►  ic2r-common
ic2r-neoforge  ──uses────────►  NeoForge API（未来）
```

默认 **不**参与根 `gradlew test`（G3.2 M6 主构建隔离）。

## 本目录现状

- **无** `build.gradle` / **无** `src/` / **无** NeoForge 依赖  
