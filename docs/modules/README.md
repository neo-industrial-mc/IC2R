# IC2R 多模块物理骨架（G3.7）

> **状态**: **skeleton only** — 目录与文档占位；**默认构建仍是仓库根单模块**（Forge 1.20.1 + ForgeGradle）。  
> **规格**: [docs/spec/g3_7_module_split.md](../docs/spec/g3_7_module_split.md)  
> **禁止**: 在未满足该文档 §6 前置条件前，将本目录子项目 `include` 为默认 Gradle 多项目。

## 目标 artifact

| 目录 | 未来 Gradle 名 | 职责 |
|:---|:---|:---|
| [common/](common/) | `:ic2r-common` | 领域逻辑 + `platform.services` SPI 接口 |
| [forge/](forge/) | `:ic2r-forge` | Forge 入口与 SPI 实现（今日主线收拢目标） |
| [neoforge/](neoforge/) | `:ic2r-neoforge` | NeoForge 实验线（G3.2 最小集） |
| [fabric/](fabric/) | `:ic2r-fabric` | 可选；后置 M9 |

## 与当前源码的关系

```text
仓库根
├── src/main/...          ← 今日全部源码与资源（单模块）
├── build.gradle          ← 今日默认构建（勿因本骨架破坏）
├── settings.gradle       ← 仅有注释掉的未来 include 示例
└── modules/              ← 本骨架（README only；无 build.gradle 子工程）
```

源码**最终**落点见各子目录 README 与 [g3_7_module_split.md](../docs/spec/g3_7_module_split.md) §3。  
**G3.7 不搬迁** `src/main`。

## 依赖方向（目标）

```text
neoforge / forge / fabric  ──implements──►  platform.services（in common）
common                     ──uses────────►  platform.services
```

详见 [platform_spi.md](../docs/spec/platform_spi.md)。
