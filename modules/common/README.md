# `ic2r-common`（规划）

> G3.7 骨架。源码仍在仓库根 `src/main/java/me/halfcooler/ic2r/**`。

## 最终源码落点（目标）

| 现状包 | 迁入 common 条件 |
|:---|:---|
| `platform/services/**` | 已无 loader 实现 import；优先 |
| `api/**` | 去掉 loader 类型泄漏后 |
| `core/energy/**`、`core/recipe/**`、`core/network/sync/**` | domain / 纯逻辑优先 |
| `core/block/**`、`core/fluid/**`、`core/item/**` 等 | **须先**清除 `net.minecraftforge.*` 实现型 import（G3.1 residual） |
| 资源 `assets/ic2r`、`data/ic2r`（共享） | 与 loader overlay 策略一并规划 |

## 依赖

- **使用** `platform.services` SPI（同模块内接口）  
- **禁止** `net.minecraftforge` / `neoforged` / `fabricmc` 实现类型（§8.5 #1 终态）  
- 可依赖 JDK + Minecraft 官方 API + 本模组 domain 类型  

## 本目录现状

- **无** `build.gradle` / **无** `src/`  
- 启用 `include` 前须满足 [g3_7_module_split.md](../../docs/spec/g3_7_module_split.md) §6  
