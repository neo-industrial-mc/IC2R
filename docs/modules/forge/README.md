# `ic2r-forge`（规划）

> G3.7 骨架。今日主产品线仍在**仓库根**单模块（`archivesName=…-forge`），**不是**本目录子工程。

## 最终源码落点（目标）

| 现状包 / 文件 | 说明 |
|:---|:---|
| `forge/**` | `FmlMod`、`ForgePlatformServices`、全部 `Platform*Forge`、Env*Forge、capabilities、client model |
| `core/proxy/**` | EnvProxy / SideProxy（E3–E6 退役前迁移期） |
| `datagen/**`、`mixin/**` | 加载器侧 DataGen / Mixin |
| `integration/**` | JEI / Jade / AE2（可再拆，非 G3.7） |
| `META-INF/mods.toml`、`data/forge/**` | Forge 元数据与 tag |

迁入本模块后：根工程变为 thin 聚合或直接以本模块为默认 `runClient`/`test` 宿主。

## 依赖

```text
ic2r-forge  ──implements──►  platform.services（in common）
ic2r-forge  ──depends on──►  ic2r-common
ic2r-forge  ──uses────────►  Forge API / ForgeGradle
```

## 本目录现状

- **无** `build.gradle` / **无** `src/`  
- **禁止**在 residual 未收敛时强行搬走整棵 `src/main`（会打断 FG runs 与单测）  
