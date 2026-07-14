# G3.2 — 非 Forge 最小可运行集 **启动**（kickoff）

> **Work Unit**: G3.2  
> **日期**: 2026-07-14  
> **状态**: **partial / started** — 文档 kickoff + SPI 前置实质化；**尚无可运行 NeoForge artifact**  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §8.1 / §8.5 #2；[neoforge_migration_plan.md](neoforge_migration_plan.md)；[phase3_closeout.md](phase3_closeout.md) G3.2；[platform_spi.md](platform_spi.md)  
> **性质**: 启动专项，**非**一次完整 NeoForge 产品；**不**切换主构建  

---

## 1. 本 Unit 明确边界

| 做 | 不做 |
|:---|:---|
| 文档定义最小集目标、版本选项、SPI 就绪度、阻断项、M8 前里程碑 | 把 `build.gradle` 主依赖切到 NeoForge |
| `PlatformRegistry` Forge 真实现（薄委托 EnvProxy） | 全仓库搬迁 / 物理 `ic2r-neoforge` 源集（本 Unit 可后置） |
| 迁 `getServer` 调用点 → `PlatformLifecycle`（E2 切片） | 完整 EnvProxy 退役 / 注册族全切 SPI |
| 登记 G3.2 **partial/started** | git commit/push |
| `.\gradlew.bat compileJava test` 绿 | 声称 §8.5 #2 done 或可运行非 Forge 产物 |

**主线仍为 Forge 1.20.1 单模块**（`archivesName=…-forge`）。本 Unit **不**切换主构建。

---

## 2. 最小可运行集定义（目标清单）

§8.5 #2 原文：至少一条**非 Forge**加载器可运行最小集（**物品注册 + 一台机器 + 电网**）。

G3.2 将此拆为可验收目标清单（**M8 交付**；本 Unit 仅启动）：

| # | 能力 | 说明 | 依赖 SPI / 域 |
|:---|:---|:---|:---|
| M1 | **Mod 入口** | NeoForge 入口 + `NeoForgePlatformServices.install()` 幂等 | Lifecycle + install 约定 |
| M2 | **物品注册** | ≥1 个 IC2R 物品（或机器方块物品）经 `PlatformRegistry` 注册 | **PlatformRegistry** |
| M3 | **1 台机器** | 建议 **Macerator**（或其它标准机）：放置、tick、配方消耗/产出（可无完整 GUI） | Registry + 可选 Network/PlayerUi；ItemTransfer |
| M4 | **电网（EU）** | 同世界内 ≥1 源 + 线缆/直连 + 消耗端，EnergyNet 路径可跑（IC 或 GT 模式其一） | common `core.energy`；**无** loader 电网实现 |
| M5 | **可启动客户端/专用服** | 实验 artifact 能 load；崩溃级缺失 facet 为 0 | 全部最小路径非 stub |
| M6 | **主构建隔离** | 默认 `gradlew test` / assemble 仍只打 Forge 产物 | 子项目默认不 include 或 profile |

**最小集明确不含**（后置）：全模组 port、AE2/管道 integration、Client SPI 全量、DataGen 全量、Origin 清零、覆盖率 75%。

### 2.1 伪模块树（目标，非本 Unit 落地）

```text
ic2r/                          # 仓库根（今日单模块）
├── ic2r-common/               # 领域：机器、EU 电网、配方；只依赖 platform.services
├── ic2r-forge/                # 现状主线收拢：FmlMod + ForgePlatformServices + EnvProxy*
├── ic2r-neoforge/             # 实验：NeoForge 入口 + NeoForgePlatformServices
│   └── … min: registry + 1 machine + energy tick
└── (optional) ic2r-fabric/    # M9
```

依赖方向：

```text
ic2r-neoforge ──implements──► platform.services
ic2r-forge    ──implements──► platform.services
ic2r-common   ──uses────────► platform.services
```

---

## 3. 版本选项（A vs B）

对照 [neoforge_migration_plan.md](neoforge_migration_plan.md) §2：

| 选项 | MC / Loader | 用途 | G3.2 建议 |
|:---|:---|:---|:---|
| **A. 同版 NeoForge 1.20.1** | MC 1.20.1 + NeoForge 1.20.1 线 | 与稳定 Forge 线对照；映射/玩法差最小；**验证 SPI 单向依赖** | **推荐先做** |
| **B. 升级 MC 再上 NeoForge**（如 1.21.x） | 更高 MC + 对应 NeoForge | 长期主产品线；生态/Java 21 | **M8 再评估**；勿与 SPI 验证绑死 |
| C. 双线并行 | 稳定 Forge + 实验 NeoForge | 不打断发布 | 骨架 Unit 后可选 |

**决策（G3.2 kickoff）**：

1. **先 A**：在 SPI facet 实质化后，用同版线做最小集，证明 common 不绑 Forge 类型。  
2. **主产品线是否升 B**：M8 前单独评估（成本 = 映射 + 能力 + 依赖模组 + Golden）。  
3. **否决**：SPI 未满时「主线 FG→NeoGradle 换依赖」。

---

## 4. SPI 就绪度表（对照代码，G3.2 后）

| Facet | 接口包 | Forge 实现 | 状态 | 备注 |
|:---|:---|:---|:---|:---|
| **PlatformLifecycle** | `platform.services` | `PlatformLifecycleForge` | **真实现** | `isClient` / `getLoaderKind` / `getServer` / tick / bootstrap；G3.2 迁 `getServer` 调用点 |
| **PlatformRegistry** | 同上 | **`PlatformRegistryForge`（G3.2 新增）** | **真实现（薄委托）** | 委托 `EnvProxy`/`EnvProxyForge`；注册**调用点**仍多走 EnvProxy（E3） |
| **PlatformEnergyBridge** | 同上 | `PlatformEnergyBridgeForge` | **真实现** | G2.8 FE 桥；EU 电网在 common，不依赖此 facet |
| **PlatformNetwork** | 同上 | stub | **stub** | 仍 `ForgeNetworkHandler` / `NetworkManager` |
| **PlatformPlayerUi** | 同上 | stub | **stub** | 仍 EnvProxy/SideProxy 开菜单/消息 |
| **PlatformConfig** | 同上 | stub | **stub** | 仍 `ForgeConfigSpec` 路径 |
| **PlatformItemTransfer** | 同上 | stub | **stub** | 仍 `EnvItemHandlerForge` |
| **PlatformFluidBridge** | 同上 | stub | **stub** | 仍 `EnvFluidHandlerForge` |

**安装**：`ForgePlatformServices.install()`（`FmlMod` + `IC2R` static，幂等）现装入 Registry + EnergyBridge + Lifecycle 真实现；其余 stub 调用 → UOE。

**NeoForge 最小集阻塞（摘要）**：

- Registry 实现已有 Forge 侧；**缺** NeoForge 对等实现与物理模块。  
- Network / ItemTransfer（机器 IO）/ 可选 PlayerUi 仍 stub → 最小机需补或绕过。  
- EnvProxy 主路径仍在；common 仍含 Forge 实现型 import（G3.1 residual）。

---

## 5. 阻断项（诚实清单）

| ID | 阻断 | 严重度 | 缓解 / 后续 |
|:---|:---|:---|:---|
| B1 | **EnvProxy 双轨**：注册/流体/物品工厂/环境位仍上帝代理；仅 `isClientEnv` 已删，G3.2 仅再迁 `getServer` 调用点 | P0 | E2 续（`isForgeEnv`/`isFabricEnv`）→ E3 注册族 → E4+ |
| B2 | **core residual Forge import**（G3.1 后仍 ~31 文件 / ~54 行） | P0 | Registry/cap/config/event 下沉 SPI 或 forge 包 |
| B3 | **无物理模块** `ic2r-common` / `ic2r-neoforge` | P0 | 逻辑边界稳后再拆 Gradle；默认不切主 assemble |
| B4 | **5/8 SPI facet 仍 stub**（Network/UI/Config/Item/Fluid） | P0–P1 | 按最小集路径补委托实现 |
| B5 | **尚无 NeoForge 依赖 / 入口 / artifact** | P0 | 选项 A 骨架 Unit（默认不启用） |
| B6 | EnergyNet / 标准机 **Origin residual** + 低覆盖率 | P1 | 不阻塞骨架；阻塞「玩法宣称对齐」 |
| B7 | Architectury **未**引入 | P2 | **有意**；手写 thin platform 优先 |

---

## 6. G3.2 已交付代码前置（本 Unit）

| 项 | 路径 / 动作 |
|:---|:---|
| **PlatformRegistryForge** | `me.halfcooler.ic2r.forge.PlatformRegistryForge` — 薄委托 `IC2R.envProxy` |
| **ForgePlatformServices** | 用 `PlatformRegistryForge` 替换 `StubRegistry` |
| **调用点迁移（E2 切片）** | `SideProxyServer`（`requestTick` / `getRecipeManager`）、`ItemDrill#getPlayerHoldingItem`：`envProxy.getServer()` → `PlatformServices.lifecycle().getServer()` |
| **未迁** | `EnvProxy#getServer` 方法本身仍保留（`EnvProxyForge` 实现）；`isForgeEnv`/`isFabricEnv` 调用点（标签等） |

---

## 7. 下一实现里程碑（M8 前）

| 序 | 里程碑 | 交付 | 非交付 |
|:---|:---|:---|:---|
| **G3.2（本 Unit）** | kickoff | 本文档 + Registry 真实现 + getServer 调用点切片 | 无可运行 NeoForge |
| **E2 续** | Lifecycle 环境位 | 迁/删 `isForgeEnv`/`isFabricEnv`；可选删 `EnvProxy#getServer` 表面 | 全 EnvProxy 删除 |
| **E3–E4** | Registry/网络/handler | 注册主路径经 SPI；Network/Item 委托非 stub | 全模组无 EnvProxy |
| **骨架 Unit** | 可选 Gradle | 占位 `ic2r-neoforge` **或** 独立 include 且默认关闭 | 不切稳定产品线 |
| **M7 续** | Platform 抽取 | SPI 主路径可用；EnvProxy 退役过半；core Forge import 趋零 | 完整多 loader CI |
| **M8** | 最小可运行集 | §2 M1–M5 在选项 **A**（或已选定 B）上可运行 | 全模组 1:1 port |

建议顺序：**E2 续 → SPI 非 stub（Network/Item）→ 骨架 → 最小机+电网 → 再谈 B**。

---

## 8. 验证

| 命令 | 期望 |
|:---|:---|
| `.\gradlew.bat compileJava test` | BUILD SUCCESSFUL；既有测试不破 |
| 主构建 loader | 仍为 Forge 1.20.1（未改 `build.gradle` loader） |
| NeoForge artifact | **无**（诚实 partial） |

---

## 9. 修订记录

| 日期 | Unit | 说明 |
|:---|:---|:---|
| 2026-07-14 | G3.2 | kickoff：最小集定义、版本 A 优先、SPI 表、阻断项、Registry 实现 + getServer 调用点；主线仍 Forge |
