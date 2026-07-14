# Phase 3 Closeout（W3.5）

> **Work Unit**: W3.5 阶段 3 收口  
> **日期**: 2026-07-14  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §8.5、§4.5、§A  
> **性质**: 文档与证据收口；**最小代码 diff**；**不**启动大规模 SPI/NeoForge 重构  
> **与前序阶段**: [phase1_closeout.md](phase1_closeout.md)（§6.3 / G1.\*）、[phase2_closeout.md](phase2_closeout.md)（§7.7 / G2.\*）登记的缺口**仍 open**，本文件登记 **§8.5 架构瘦身 / 多加载器** 试点交付与 G3.\*  
> **验证命令**: `.\gradlew.bat test jacocoTestReport` → **BUILD SUCCESSFUL**（**66** tests, 0 failures）

---

## 1. §8.5 完成标准对照

| # | §8.5 标准 | 判定 | 证据摘要 | 关联 Unit |
|:---|:---|:---|:---|:---|
| 1 | common 源码无 `net.minecraftforge.*` / `net.neoforged.*` / `net.fabricmc.*` **实现型** import | **gap** | 尚无物理 `ic2r-common` 模块；以 `core` + `platform.services` 作 common 代理口径抽查：**platform 包 0 条** loader import（达标切片）。**core 仍大量残留**：约 **57** 个文件、**114** 条 `import net.minecraftforge.*`（抽样主导：`Dist`/`OnlyIn`×37 对、`ForgeRegistries`×13、`IItemHandler*`/`LazyOptional` 于库存 TE、`ForgeConfigSpec` 等）。`net.neoforged.*` / `net.fabricmc.*` **全库 0**。W3.2 仅迁 1 调用点（`PlatformLifecycle.isClient`）；W3.3 删 `EnvProxy#isClientEnv` 但 `isForgeEnv`/`isFabricEnv` 等仍双轨 | W3.1–W3.3 |
| 2 | 至少一条**非 Forge**加载器可运行最小集（物品注册 + 一台机器 + 电网） | **deferred**（G3.2 **started**） | **W3.4** 文档计划 + **G3.2** kickoff：[g3_2_neoforge_min_set.md](g3_2_neoforge_min_set.md)；`PlatformRegistryForge` 真实现 + `getServer` 调用点迁 SPI。主构建仍 **Forge 1.20.1**；**无**可运行 NeoForge artifact。选项 **A** 优先验证 SPI；M8 再谈完整最小集 | W3.4 / G3.2 |
| 3 | 覆盖率达 §4.5 阶段 3 门槛（**≥ 75%** common 全量，不含 client 渲染） | **gap** | W3.5 见 §2（common-ish **~1.06%**）。**G3.3 复测**见 **§12**：common-ish **~1.79%**（609/34053）仍 ≪ 75%；需再覆盖约 **2.5e4** 行才达门槛 | W0.1+ / G3.3 |
| 4 | Origin 文档中「移植残留」**核心模块清零**或仅剩标注兼容层 | **partial / gap** | **G3.4 已回写** [origin.md](origin.md)（v0.2）：按 G1–G3 标 mixed / rewritten **切片**（Sync、\*Math、Handler、SPI、Sound/域拆等）；**P0 宿主仍 residual**（EnergyNet IC、标准机 TE、TeUpdate 帧/反射、InvSlot 树、reactor/crop…）。**核心 residual 未清零** → 诚实 **partial**（表已反映增量）/ **gap**（§8.5 #4 清零未达成）。见 **§13** | W0.6 + G3.4 |

### 1.1 勾选视图（阶段 3 名义完成度）

```text
[gap]      common 无 loader 实现型 import     ← platform 洁净；core ~57 文件仍含 Forge
[deferred] 非 Forge 最小可运行集             ← G3.2 **started**（文档+SPI 前置）；仍无 NeoForge artifact；主线 Forge 1.20.1
[gap]      覆盖率 ≥75% common（§4.5 阶段 3）
[partial]  Origin residual 核心清零          ← G3.4 表已回写切片；P0 宿主仍 residual（#4 未勾满）
```

**阶段 3 结论**：Work Unit **W3.1–W3.4** 均已按各自 DoD 交付（SPI 草案、1 调用点迁移、EnvProxy 切片瘦身、NeoForge **文档**计划）；§8.5 **未全部勾满**。  
允许以 **gap / deferred 登记** 作为 W3.5 DoD 完成（本文件）。缺口进入 **G3.\*** 与继承的 G1/G2，**后续工作不走本 Progress 队列 pending**（队列已空），而走专项 gap Unit 或后续追加 `W{x.y}`。

**本 Unit 明确不做**：为凑 §8.5 启动 NeoForge 多模块、全库去 Forge import、Origin 全表重标、堆无意义测试抬覆盖率。

---

## 2. 覆盖率证据（JaCoCo）

| 项 | 值 |
|:---|:---|
| 命令 | `.\gradlew.bat test jacocoTestReport` |
| 结果 | BUILD SUCCESSFUL；**66** tests, 0 failures / errors |
| 报告 | `build/reports/jacoco/test/html/index.html` |
| XML | `build/reports/jacoco/test/jacocoTestReport.xml` |
| 工具 | JaCoCo 0.8.11（`build.gradle`） |

### 2.1 §4.5 阶段 3 核心包定义对照

| 阶段 | 目标 | 核心包定义 |
|:---|:---|:---|
| 阶段 3 结束 | ≥ **75%** 行覆盖率 | **common 全量**（不含 client 渲染） |

仓库尚无拆分的 `ic2r-common`，本收口采用：

| 口径 | 包集合 | 用途 |
|:---|:---|:---|
| **阶段 3 主门槛（common-ish）** | 全工程 LINE，**排除** `forge*`、`integration*`、`**/gui*`、`**/render*`、`core/model`、`datagen`、`mixin`、`compat` | 近似「common 全量 − client/loader 壳」 |
| **overall** | 全工程 | 背景 |
| **阶段 1 宽口径** | energy* + network* + block.comp + machine.tileentity | 趋势对照 |
| **阶段 2 窄口径** | invslot + fluid + recipe + recipe.v2 | 趋势对照 |
| **platform SPI** | `platform.services` | 阶段 3 新增域 |

### 2.2 包级 / 聚合行覆盖率（实测）

| 包或聚合 | 行覆盖 | covered/total | 备注 |
|:---|:---|:---|:---|
| `me...core.network.sync` | **91.40%** | 85/93 | 阶段 1 遗产 |
| `me...core.energy.profile` | **20.00%** | 23/115 | 同阶段 1 |
| `me...core.network` | **5.51%** | 86/1560 | 同阶段 1 |
| `me...core.fluid` | **5.23%** | 15/287 | 阶段 2 `FluidTransferMath` |
| `me...core.block.invslot` | **4.93%** | 40/811 | 阶段 2 Math |
| `me...core.recipe` | **2.64%** | 20/759 | 阶段 2 MatchMath |
| `me...core.energy.grid` | **0.87%** | 17/1957 | 仅 `EnergyTransferMath` 切口 |
| `me...core.block.comp` | **0.53%** | 4/756 | NBT 边角 |
| `me...core.block.machine.tileentity` | **0.21%** | 8/3850 | 标准机循环本体未测 |
| `me...platform.services` | **0%** | 0/30 | SPI 接口 + `PlatformServices` 未单测 |
| `me...core.proxy` | **0%** | 0/327 | EnvProxy 上帝代理仍在 |
| `me...forge` / `forge.model` | **0%** | 0/1753+ | loader 实现层（G3.2 增 PlatformRegistryForge） |
| **阶段 2 窄口径**\* | **~3.53%** | 75/2127 | 与 W2.6 一致 |
| **阶段 1 宽口径**† | **~2.68%** | 223/8335 | 与 W1.8 一致量级 |
| **common-ish（阶段 3 主门槛）**‡ | **~1.06%** | 355/33429 | ≪ 75% → **gap** |
| 全工程 overall LINE | **0.90%** | 355/39464 | 背景，非门槛 |

\*窄 = `invslot` + `fluid` + `recipe` + `recipe.v2`。  
†宽 = energy + energy.grid + energy.profile + network + network.sync + block.comp + machine.tileentity。  
‡common-ish = overall 排除 forge/integration/gui/render/model/datagen/mixin/compat（见 §2.1）。

### 2.3 高价值类（跨阶段亮点，未变）

| 类 | 行覆盖 | 关联 |
|:---|:---|:---|
| `InvSlotTransferMath` / `FluidTransferMath` / `MachineRecipeMatchMath` | ~98–100% | 阶段 2 纯逻辑 |
| `BlockEntitySync` / `SyncCodecs` 等 | ~90%+ | 阶段 1 Sync |
| `EnergyTransferMath` | ~89% | W0.4 |
| `PlatformServices` / SPI 接口 | **0%** | W3.1–W3.3 未加测（编译级验证） |

**结论**：阶段 3 **未**引入抬覆盖率的新测例；门槛 75% **gap**。后续优先有意义行为测（电网/标准机/切主 Sync），**勿**为空转覆盖 SPI 接口样板。

### 2.4 测试套件计数（相对前序收口）

| 阶段收口 | tests | 备注 |
|:---|:---|:---|
| W1.8 | 38 | 基础设施 + Sync/NBT |
| W2.6 | **66** | +inv/fluid/recipe 适配测 |
| W3.5 | **66** | 阶段 3 无新增单元测试；回归保持绿 |

---

## 3. common / loader import 抽查（§8.5 #1 证据）

| 范围 | 结果 |
|:---|:---|
| `platform/services/**` | **0** `net.minecraftforge` import（SPI 边界洁净） |
| `core/**` 含 Forge import 的 `.java` 文件 | **~57** |
| `core/**` Forge import 行 | **~114** |
| 非 `forge/**` 包含 Forge import 文件 | **~74**（含 core/integration/datagen/compat/mixin 等） |
| `net.neoforged.*` / `net.fabricmc.*` | **全库 0** |

**core 内高频类型（抽样）**：

| 类型簇 | 约计 | 含义 |
|:---|:---|:---|
| `api.distmarker.Dist` / `OnlyIn` | 37+37 | 侧向注解散落 common 业务/GUI |
| `registries.ForgeRegistries` | 13 | 注册表直依赖 |
| `items.IItemHandler*` / wrappers / `LazyOptional` | 数处 | W2.1 库存 cap 路径仍在 core TE |
| `common.ForgeConfigSpec` | 3 | 配置类型泄漏 |
| 其它 event/client/fluid | 散落 | 未抽到 SPI |

**诚实边界**：`forge/**` 内大量 Forge import **符合** platform-impl 角色，**不**计为 §8.5 common 违规；违规主体是 **core 业务** 与尚未下沉的适配。

---

## 4. Gap 列表与建议后续（G3.*）

| ID | Gap | 严重度 | 建议后续（**非本 W3.5**） |
|:---|:---|:---|:---|
| G3.1 | common/core 仍大量 `net.minecraftforge.*` 实现型 import | P0 | **partial（G3.1 切片 done，见 §10）**：core **65→31** 文件 / **123→54** import 行；仅 `Dist`/`OnlyIn` 文件已清零。**仍 residual**：`ForgeRegistries`、cap/handler、config、event 等；禁止一次清零。后续 E2–E6 / 下沉 SPI/forge 包 |
| G3.2 | 非 Forge 最小可运行集 **已启动 / partial**（§8.5 #2 仍 **未** done） | P0 | **partial/started（见 §11）**：kickoff 文档 + `PlatformRegistryForge` + `getServer` 调用点迁 SPI；**尚无**可运行 NeoForge artifact；主构建仍 Forge 1.20.1。后续：E2 续 → 非 stub Network/Item → 骨架 → M8 最小集 |
| G3.3 | 覆盖率 common ≪ 75%（G3.3 复测 common-ish **~1.79%**） | P0 | **open / gap（见 §12）**：相对 W3.5 有增量（66→**153** tests；common-ish 1.06%→**1.79%**）仍远低于 75%。继承 G1.2/G2.4；优先电网/标准机本体/切主 Sync e2e；**禁止**空转堆 SPI |
| G3.4 | Origin residual **核心未清零**（energy IC、标准机、network 反射、InvSlot、reactor/crop…） | P0 | **partial（G3.4 文档回写 done，见 §13）**：origin.md 已按 G1–G3 刷新切片状态；**§8.5 #4 清零仍 gap**。后续域干净室重写后再回写；清零前禁止宣称 #4 done |
| G3.5 | EnvProxy 上帝代理仍在；**E2 done**：`isClientEnv`/`isForgeEnv`/`isFabricEnv`/`getServer` 已从 EnvProxy 删并迁 SPI；注册族/流体物品工厂未切 | P0 | **partial（E2 done）**：见 platform_spi.md G3.5 切片；下一 E3 注册族 → E4+（neoforge_migration_plan §4.2） |
| G3.6 | ~~SPI facet 多为 stub~~ → **done**（见 **§14**）：8 facet Forge 薄委托；≥4 调用点迁 SPI；`extract` 有意 EMPTY 缺口已文档 | P1 | **done**；后续 E3 注册主路径 + 可选 Network 发包替换；勿堆 SPI 空测 |
| G3.7 | 物理多模块 **partial/skeleton**（文档 + `modules/` 骨架；运行时仍单模块） | P1 | **partial/skeleton（见 §15）**：映射文档 + 目录 README + `settings.gradle` 注释 include；**未**搬迁 `src/main`、**未**默认多项目。下一 Unit 满足 g3_7 §6 前置后再启用 include |
| G3.8 | Architectury **未**引入 | P2 | **skipped / deferred by design（见 §16）**：[g3_8_architectury_decision.md](g3_8_architectury_decision.md)；非缺陷；再评估前置 = NeoForge 最小集 + SPI 稳定 |
| G3.9 | 巨型 BE / api 面 / Mixin 瘦身（§8.3） | P2 | **partial（见 §17）**：Mixin 清单 done；[api_surface.md](api_surface.md)；`CropGrowthMath` 1 BE 切片 + 测；**未**拆完全部巨型 BE |
| G3.10 | **继承 G1.\***：TeUpdate 仍默认、snake_case 仅试点、阶段 1 覆盖率 gap | P0 | **partial（交叉对照 done，见 §18）**：G1.6/G1.7/G1.8 **done**；G1.1/G1.3–G1.5 **partial**；**G1.2 仍 gap**。TeUpdate **帧仍默认**；宽口径 ≪60%。**禁止**宣称 G1 全清 |
| G3.11 | **继承 G2.\***：管道/AE2 e2e 无、配方非全机型直查、~47 guidef、DataGen 窄 | P0–P2 | **partial（交叉对照 done，见 §19）**：G2 队列 Unit **均已推进**；**无**真管道/AE2 e2e；basic materialize 非 tick 直查；guidef **~41**（非 ~47）；DataGen 仍 tags 窄；窄口径 **~5.90% ≪70%**。**禁止**宣称 §7.7 全勾满 |

### 4.1 与 G1 / G2 继承关系

```text
G1.*（阶段 1 基础设施缺口）— G3.10 交叉见 §18
  └─ 仍 open：G1.1 TeUpdate 帧默认、G1.2 覆盖率 gap、G1.3–G1.4 包级极低、G1.5 全库命名
  └─ 已关：G1.6 recipe 匹配器、G1.7 Spotless N/A、G1.8 Blocks 域拆
G2.*（阶段 2 Forge 生态试点缺口）— G3.11 交叉见 §19
  └─ 仍 open / residual：G2.1 e2e、G2.2 非 basic/非直查、G2.3 存量 guidef、G2.4 ≪70%、
     G2.5 流体 cap residual、G2.6 DataGen 窄、G2.7→G1 债、G2.8 FE e2e/暴露
  └─ 队列交付 done（文档+切片）：G2.1–G2.8 均有推进产物；**不等于** §7.7 勾满
G3.*（本文件）
  └─ 叠加：loader 隔离、多 loader 最小集、common 覆盖率 75%、Origin 清零、EnvProxy 退役
```

阶段 3 **不能**关闭 G1/G2；关闭条件仍在各自专项。G3.3 与 G1.2/G2.4 **同一覆盖率债**的不同门槛表述。G3.10 / G3.11 仅**登记** G1/G2 债在阶段 3 视角下的真实状态，**不**勾满 §6.3 / §7.7。

### 4.2 建议优先级（阶段 3 后，非队列强制序）

1. **E2–E3 EnvProxy 切片** → 减 core Forge import（服务 G3.1/G3.5）。  
2. **有意义覆盖率**（电网/标准机/Sync 切主）→ 抬 G3.3 / G1.2。  
3. **SPI facet 非 stub** + 可选 common 模块边界 → 解锁 G3.2 NeoForge 最小集。  
4. **域干净室重写** 时回写 Origin → G3.4。  
5. **不要**：本收口后再开「为勾 §8.5 而空转」的全库 rename 或无测大重构。

---

## 5. 阶段 3 Work Unit 交付快照（W3.1–W3.4）

| ID | 状态 | 交付要点 |
|:---|:---|:---|
| W3.1 | done | `platform.services` **8** SPI + `PlatformServices`；[platform_spi.md](platform_spi.md)；依赖方向说明 |
| W3.2 | done | `EventHandler.onInitLate` → `PlatformLifecycle.isClient`；`ForgePlatformServices.install`；lifecycle 实现 + 其余 stub；双轨 EnvProxy |
| W3.3 | done | 删除 `EnvProxy#isClientEnv`；全库 client 判定改 `PlatformLifecycle.isClient`；其余 EnvProxy 方法仍双轨 |
| W3.4 | done | [neoforge_migration_plan.md](neoforge_migration_plan.md) 文档级计划；版本线 A/B/C、退役序 E1–E6、里程碑；**主构建仍 Forge 1.20.1** |
| W3.5 | **本文件** | §8.5 判定 + JaCoCo 证据 + G3.\* + 队列收口说明 |

---

## 6. 阶段 3 明确不做 / 未越界确认

| 项 | 状态 |
|:---|:---|
| 完整拆除 EnvProxy / SideProxy | 未做（仅 isClientEnv 切片） |
| common 全库去 Forge import | 未做 |
| NeoForge / Fabric 可运行产物 | 未做（仅计划） |
| 物理多模块 Gradle | **G3.7 skeleton only**（目录/文档；默认仍单模块） |
| Architectury | **未引入**（G3.8 skipped/deferred by design；见 §16） |
| Origin 全表 residual 清零 | 未做 |
| 为 75% 门槛堆测 | 未做 |
| 本 Unit 生产代码功能改动 | **无**（文档 + 验证） |
| git commit/push | **无** |

---

## 7. 相关规格与产物索引

| 文档 / 路径 | 角色 |
|:---|:---|
| [phase1_closeout.md](phase1_closeout.md) | §6.3 / G1.\* |
| [phase2_closeout.md](phase2_closeout.md) | §7.7 / G2.\* |
| [platform_spi.md](platform_spi.md) | W3.1–W3.3 SPI 与 EnvProxy 映射 |
| [neoforge_migration_plan.md](neoforge_migration_plan.md) | W3.4 多 loader 计划与延期预填 |
| [origin.md](origin.md) | residual/rewritten/original/mixed（W0.6 初版 → **G3.4** G1–G3 回写） |
| [golden_suite.md](golden_suite.md) | 行为规格；阶段 3 无新 Golden 条目 |
| `me.halfcooler.ic2r.platform.services.*` | SPI 接口与访问器 |
| `me.halfcooler.ic2r.forge.ForgePlatformServices` / `Platform*Forge`（8 facet，G3.6） | Forge 安装与全部 SPI 薄委托 |
| [g3_2_neoforge_min_set.md](g3_2_neoforge_min_set.md) | G3.2 最小集 kickoff |
| [g3_7_module_split.md](g3_7_module_split.md) | G3.7 物理多模块映射 + 为何不默认 include |
| `modules/**/README.md` | G3.7 目录级骨架（无子工程 build） |
| [Modernization_Progress.md](../Modernization_Progress.md) | Work Unit 队列 |

---

## 8. 变更范围（W3.5）

- **本文件**（新建）  
- `docs/spec/README.md` 索引 + 阶段 3 进度  
- 可选：`golden_suite.md` 阶段 3 摘要一句  
- **无**生产代码功能改动；**无**大规模重构；**无** git commit/push  

---

## 9. Work Unit 队列收口说明

截至 W3.5，[Modernization_Progress.md](../Modernization_Progress.md) 中 **§A.5 既定队列 W0.1–W3.5 全部应为 `done`**（本收口登记后由主 Agent 写回）。

| 含义 | 说明 |
|:---|:---|
| **队列 done** | 阶段 0–3 规划的 **Work Unit 表项**已交付（含以 gap/deferred 完成的收口 Unit） |
| **不等于** | 主文档 §6.3 / §7.7 / §8.5 **完成标准已全部勾满** |
| **后续工作** | 走 **G1.\* / G2.\* / G3.\*** 与 Progress 中**新追加**的 `W{x.y}`（如 EnvProxy E2、NeoForge 骨架、覆盖率专项）；**非**本表 `pending` 项 |
| **协议** | 仍遵守 §A：一次一个 Unit、禁止 Agent commit、用户「继续」再开下一项 |

**阶段 3 名义完成度**：试点与计划 **done**；§8.5 硬门槛 **多数 gap/deferred** — 诚实记录，不假装勾满。

---

## 10. G3.1 core Forge 实现型 import 收敛切片

> **Work Unit**: G3.1  
> **日期**: 2026-07-14  
> **状态**: **partial / open**（有意义一切片 **done**；§8.5 #1 **未**清零）  
> **模式**: 仿 W3.3 `isClientEnv` 退役 + Dist/OnlyIn 注解去 loader 依赖（**非**全库去 Forge）  
> **验证**: `.\gradlew.bat compileJava test` → BUILD SUCCESSFUL  

### 10.1 切片内容

| 动作 | 说明 |
|:---|:---|
| **A. Dist/OnlyIn 仅含文件清零** | **34** 个 core 文件原先 **仅** `import net.minecraftforge.api.distmarker.{Dist,OnlyIn}` + `@OnlyIn(Dist.CLIENT)`；删除注解与 import，行为依赖既有 `level.isClientSide` / client 类分离，**不**改玩法 |
| **B. FMLEnvironment → SPI** | `CommandIc2r#cmdDebugDumpTextures`：`FMLEnvironment.dist.isDedicatedServer()` → `!PlatformServices.lifecycle().isClient()`（与 `PlatformLifecycleForge` 物理 dist 一致） |
| **未做** | 一次清零全部 residual；Registry/Handler/Config/Event 下沉；物理 `ic2r-common`；git commit/push |

**仍保留 Dist/OnlyIn 的 core 文件（另有其它 Forge import，未本切片清）**：`Ic2rTileEntity`、`JetpackHandler`、`ItemToolWrench`（与 cap/event/registry 混杂）。

### 10.2 Residual 计数（前后对比）

| 口径 | G3.1 前（本切片实测） | G3.1 后 | Δ |
|:---|:---|:---|:---|
| `core/**` 含 `import net.minecraftforge.*` 的 `.java` 文件 | **65** | **31** | **−34** |
| `core/**` Forge import 行 | **123** | **54** | **−69** |
| 其中 **仅** Dist/OnlyIn 的文件 | **34** | **0** | **−34** |
| `core` 内 `FMLEnvironment` 实现型用法 | 1（`CommandIc2r`） | **0** | **−1** |
| W3.5 文档登记（历史） | ~57 文件 / ~114 行 | — | 与本切片前实测口径略差（域拆分等增文件）；**以本表实测为准** |

**residual 主导簇（core，切片后）**：`ForgeRegistries`（含 `ref/blocks/*` 与注册/序列化）、`IItemHandler*`/`LazyOptional`、`ForgeConfigSpec`、`FluidStack`/`IFluidHandler`、event/client model 等 — 需后续 SPI 或 forge 适配下沉。

### 10.3 变更范围（G3.1）

- 上述 **34** 个 Dist/OnlyIn-only core 源文件（注解/import 删除）  
- `core/command/CommandIc2r.java`（FMLEnvironment → `PlatformServices.lifecycle()`）  
- 本文件 §4 G3.1 行 + **§10**  
- [Modernization_Progress.md](../Modernization_Progress.md) G3.1  
- **无** git commit/push  

---

## 11. G3.2 非 Forge 最小可运行集 — 启动

> **Work Unit**: G3.2  
> **日期**: 2026-07-14  
> **状态**: **partial / started**（kickoff + SPI 前置 **done**；§8.5 #2 可运行最小集 **未**达成）  
> **规格**: [g3_2_neoforge_min_set.md](g3_2_neoforge_min_set.md)  
> **验证**: `.\gradlew.bat compileJava test` → BUILD SUCCESSFUL  

### 11.1 交付

| 动作 | 说明 |
|:---|:---|
| **文档 kickoff** | 最小集定义（物品+1 机+电网）、版本 A 优先 / B 后置、SPI 就绪度 8 facet、阻断项、主构建不切换、M8 前里程碑 |
| **PlatformRegistryForge** | 薄委托 `EnvProxy` 注册族；`ForgePlatformServices` 替换 `StubRegistry` |
| **调用点（E2 切片）** | `SideProxyServer`、`ItemDrill`：`envProxy.getServer()` → `PlatformServices.lifecycle().getServer()` |
| **未做** | NeoForge 依赖/模块/入口；主构建切换；全库注册迁 SPI；Network/Item/Fluid/UI/Config 非 stub；git commit/push |

### 11.2 SPI 就绪（G3.2 后 → **G3.6 全 facet**）

| 真实现 | stub |
|:---|:---|
| Lifecycle、Registry、EnergyBridge、**Network / PlayerUi / Config / ItemTransfer / FluidBridge（G3.6）** | **无**（G3.6 已清 stub；`ItemTransfer#extract` 仍 EMPTY 缺口见 §14） |

### 11.3 Residual / 诚实边界

- **无可运行 NeoForge artifact** → §8.5 #2 仍 **deferred**  
- EnvProxy 双轨仍在（`getServer` 方法表面保留；`isForgeEnv`/`isFabricEnv` 未切）  
- 注册**调用点**仍多走 `IC2R.envProxy`（Registry SPI 可调用，E3 再迁主路径）  
- 主线 **Forge 1.20.1** 不变  

### 11.4 变更范围（G3.2）

- [g3_2_neoforge_min_set.md](g3_2_neoforge_min_set.md)（新建）  
- `forge/PlatformRegistryForge.java`（新建）  
- `forge/ForgePlatformServices.java`  
- `core/proxy/SideProxyServer.java`、`core/item/tool/ItemDrill.java`  
- 本文件 §1 / §4 G3.2 + **§11**；[docs/spec/README.md](README.md) 索引  
- **无** git commit/push  

---

## 12. G3.3 复测 — common 覆盖率（§4.5 阶段 3 ≥75%）

> **Work Unit**: G3.3  
> **日期**: 2026-07-14  
> **状态**: **open / gap**（有意义纯逻辑边界测 **done**；**未**达 75% common）  
> **命令**: `.\gradlew.bat test jacocoTestReport` → **BUILD SUCCESSFUL**；**153** tests, 0 failures  
> **报告**: `build/reports/jacoco/test/html/index.html` / `jacocoTestReport.xml`  
> **口径**: 与 §2.1 一致（common-ish / overall / 阶段 1 宽 / 阶段 2 窄）

### 12.1 对照 75% 与 W3.5

| 聚合口径 | W3.5（§2.2） | G3.3 复测 | Δ covered | 相对 75% |
|:---|:---|:---|:---|:---|
| **common-ish（阶段 3 主门槛）** | **~1.06%** 355/33429 | **~1.79%** 609/34053 | +254 行 | **gap**（差约 **24930** 行至 75%） |
| 全工程 overall LINE | **0.90%** 355/39464 | **1.52%** 609/40087 | +254 | 背景，非门槛 |
| **阶段 1 宽口径**† | **~2.68%** 223/8335 | **~4.47%** 380/8499 | +157 | 仍 ≪ 75% |
| **阶段 2 窄口径**\* | **~3.53%** 75/2127 | **~5.90%** 143/2425 | +68 | 仍 ≪ 75% |
| 测试计数 | **66** | **153** | +87 | 含 W3.5 后累计 + 本 Unit 边界测 |

\*窄 = `invslot` + `fluid` + `recipe` + `recipe.v2`。  
†宽 = `core/energy*` + `core/network*` + `block.comp` + `machine.tileentity`。  
‡common-ish = overall 排除 `forge` / `integration` / `gui` / `render` / `model` / `datagen` / `mixin` / `compat` 包路径段。

**结论（诚实）**：**未达 75%**。common-ish 仍 **~1.8%** 量级；相对 W3.5 有可见增量，但**无**覆盖率跃升到门槛附近。后续须覆盖电网/标准机/InvSlot 等**大本体**，而非继续只测已近 100% 的 `*Math` 切片。

### 12.2 包级快照（G3.3 实测，节选）

| 包或聚合 | 行覆盖 | covered/total | 备注 |
|:---|:---|:---|:---|
| `core.network.sync` | **96.69%** | 117/121 | Sync 边界测加深 |
| `core.energy`（含 EnergyBridgeMath） | **91.30%** | 42/46 | Bridge 溢出/残差 |
| `core.energy.grid` | **3.01%** | 60/1994 | 几乎仅 TransferMath |
| `core.block.machine.tileentity` | **1.23%** | 48/3901 | CycleMath 切片；TE 本体未测 |
| `core.fluid` / `invslot` / `recipe` | ~6–9% | — | 阶段 2 Math 遗产 |
| `platform.services` | **0%** | 0/30 | 刻意不堆 SPI 空测 |
| **common-ish** | **~1.79%** | 609/34053 | **gap** |

高价值纯逻辑类（本 Unit 后）：`EnergyBridgeMath` / `FluidTransferMath` / `MachineRecipeMatchMath` / `RecipeSerializerMath` **~100%**；`EnergyTransferMath` **~98%**；`StandardMachineCycleMath` **~97%**；`BlockEntitySync` **~98%**。亮点已饱和，**拉不动** common 全量门槛。

### 12.3 本 Unit 新增有意义测（≥3，非空转）

| 文件 | 场景 | 规格精神 |
|:---|:---|:---|
| `EnergyTransferMathTest` | 高 tier 阶梯 / distribute 非法输入 / 线损杀 inject；GT 死包、null loss、V≤0 offer | EN-IC / EN-GT 边界 |
| `StandardMachineCycleMathTest` | defaultLength=0→64ops/len1；rescale 非法长度；多 OC 与 applyModifier 饱和；length0 tick clamp | SM-001/006 边界 |
| `EnergyBridgeMathTest` | 有限大数 FE 饱和 Long.MAX；Infinity 拒为 0；部分 FE 接受残差 | G2.8 bridge 溢出安全 |
| `SyncCodecRoundTripTest` | isEmpty；legacy alias lookup；tryGet/trySet 回落；未知 wire decode 硬失败 | NS-005 / TeUpdate 别名 |

**未做**：伪造 75%；对 SPI 接口 getter 空测抬点；改生产玩法逻辑。

### 12.4 变更范围（G3.3）

- 上述 **4** 个 `src/test` 文件（边界测）  
- 本文件 §1 #3 / §4 G3.3 行 + **§12**  
- **无** git commit/push  

---

## 13. G3.4 Origin residual 回写

> **Work Unit**: G3.4  
> **日期**: 2026-07-14  
> **状态**: **partial**（origin.md **已诚实回写**；§8.5 #4 **核心 residual 清零未达成**）  
> **规格**: [origin.md](origin.md) v0.2-g3.4  
> **验证**: 文档对照 G1–G3 收口证据；**无**强制代码改动；`test` 可保持绿  

### 13.1 交付

| 动作 | 说明 |
|:---|:---|
| **刷新核心包表** | 按 G1–G3 实际交付标注 residual / rewritten / original / mixed；切片类写明「切片」 |
| **§0.3 诚实结论** | 明确 §8.5 #4 **未**达成；禁止全表改 original |
| **§3 证据索引** | EnergyNet / network / 标准机 / Inv·Fluid / recipe / platform SPI / Sound·域拆 对照 Unit |
| **未做** | 域干净室重写；假装 P0 residual 清零；git commit/push |

### 13.2 关键状态变更（摘要）

| 包/切片 | W0.6 | G3.4 |
|:---|:---|:---|
| `core/energy/` 整体 | mixed | **mixed**（不变主判据；+ Math 切片行） |
| `EnergyCalculatorGT` / `EnergyNetMode` | original | **original** |
| IC 路径 Calculator + Grid 主体 | residual | **residual** |
| `EnergyTransferMath` / `EnergyBridgeMath` | （未单列） | **rewritten 切片** |
| `core/network/**` | residual | **mixed**（sync rewritten；TeUpdate 帧/反射 residual） |
| `core/network/sync/**` | （并入 network） | **rewritten** |
| 标准机 TE / 主体 | residual | **residual**；+ `StandardMachineCycleMath` **rewritten 切片** |
| `Ic2rTileEntity` Tick | residual | **mixed**（W1.3 去反射 Tick） |
| `invslot` / `fluid` | residual / mixed | **mixed**（Math + Handler 切片 rewritten） |
| `recipe` / `recipe/v2` | residual / rewritten | **mixed**（MatchMath/v2/bridge） |
| `platform/services/**` | （无） | **original** |
| SoundEvents Deferred、Items/Blocks 域拆 | rewritten（init/ref） | **rewritten**（证据加细） |
| reactor / crop / UU 等 | residual | **residual**（未动） |
| **§8.5 #4 清零** | gap | **仍 gap** |

### 13.3 变更范围（G3.4）

- [origin.md](origin.md)（v0.2-g3.4 全文回写）  
- 本文件 §1 #4 / §4 G3.4 行 + **§13** + 相关索引句  
- [Modernization_Progress.md](../Modernization_Progress.md) G3.4（主 Agent）  
- **无**生产代码功能改动；**无** git commit/push  

---

## 14. G3.6 SPI facet 去 stub

> **Work Unit**: G3.6  
> **日期**: 2026-07-14  
> **状态**: **done**（8 facet 真实现 + 调用点试点；**未**全库迁 EnvProxy）  
> **验证**: `.\gradlew.bat compileJava test` → BUILD SUCCESSFUL  

### 14.1 交付

| 动作 | 说明 |
|:---|:---|
| **PlatformNetworkForge** | `registerChannel` 幂等 no-op（`FmlMod` 已注册）；send→vanilla custom payload（对齐 `NetworkManager`） |
| **PlatformPlayerUiForge** | `openMenu`→`EnvProxy#openHandledScreen`；message/error→`SideProxy` |
| **PlatformConfigForge** | `FMLPaths.CONFIGDIR`；register* no-op；`isCommonConfigLoaded`→`IC2RConfig.SPEC.isLoaded()` |
| **PlatformItemTransferForge** | `createHandler`→`EnvProxy`；`insert`→`deposit`；**`extract`→EMPTY**（无公开 BE extract，javadoc 说明） |
| **PlatformFluidBridgeForge** | `createHandler`→`EnvProxy`；drain/fill/getContained→`EnvFluidHandler` API |
| **ForgePlatformServices** | 安装上述实现；**删除** StubFluid/Item/Network/PlayerUi/Config |
| **调用点（≥2，实际 4）** | `IHasGui` openMenu；`StackUtil.ENV`；`FluidHandler.ENV_HANDLER`；`EventHandler.onPlayerLogin` messagePlayer |
| **文档** | [platform_spi.md](platform_spi.md) facet 表 + G3.6 切片；本 §14 |

### 14.2 诚实边界

- EnvProxy / SideProxy **双轨仍在**（大量 messagePlayer / 注册等未迁）  
- `PlatformItemTransfer#extract` **有意** stub 返回 EMPTY（domain 缺口，非偷懒整 facet）  
- Network 通道注册 / Config 文件注册 **仍**在 `FmlMod`；SPI 面为幂等入口  
- **无**物理多模块 / NeoForge 实现 / git commit/push  

### 14.3 变更范围（G3.6）

- 新建：`forge/PlatformNetworkForge.java`、`PlatformPlayerUiForge.java`、`PlatformConfigForge.java`、`PlatformItemTransferForge.java`、`PlatformFluidBridgeForge.java`  
- 改：`forge/ForgePlatformServices.java`；`core/IHasGui.java`、`util/StackUtil.java`、`fluid/FluidHandler.java`、`event/EventHandler.java`；`platform/services/PlatformServices.java` javadoc  
- 文档：本文件 §4 G3.6 + **§14**；[platform_spi.md](platform_spi.md)；[Modernization_Progress.md](../Modernization_Progress.md)  

---

## 15. G3.7 物理多模块 — 文档 + 安全骨架

> **Work Unit**: G3.7  
> **日期**: 2026-07-14  
> **状态**: **partial / skeleton**（目录与映射 **done**；Gradle 多项目默认构建 / 源码搬迁 **未**做）  
> **规格**: [g3_7_module_split.md](g3_7_module_split.md)  
> **验证**: `.\gradlew.bat compileJava test` → BUILD SUCCESSFUL  

### 15.1 交付

| 动作 | 说明 |
|:---|:---|
| **映射文档** | 目标 `ic2r-common` / `ic2r-forge` / `ic2r-neoforge`（+ 可选 fabric）；源码迁移映射；SPI 依赖方向 |
| **为何不默认多项目** | core residual Forge import；单测/FG runs 成本；SPI 双轨；主产品线稳定；无 NeoForge 依赖线 |
| **启用 include 前置** | g3_7 §6（P1–P6）：common 洁净、E3+ SPI、FG 接线、主构建隔离等 |
| **目录骨架** | `modules/{common,forge,neoforge,fabric}/README.md` + `modules/README.md`；**无**子 `build.gradle`、**无** `src` 搬迁 |
| **settings.gradle** | **注释掉**的 `include 'ic2r-*'` + `projectDir = modules/…` 示例；默认不激活 |
| **未做** | 切主依赖 NeoForge；默认多模块；整棵 `src/main` 搬走；可运行 NeoForge artifact；git commit/push |

### 15.2 诚实边界

- **运行时仍单模块**（根 FG + `src/main`）  
- **无**参与构建的子项目；注释 include **不得**被误开（无 build.gradle 会炸）  
- 逻辑边界靠 SPI（G3.6）；物理边界仅骨架  
- §8.5 #1 / #2 仍 gap / deferred  

### 15.3 变更范围（G3.7）

- [g3_7_module_split.md](g3_7_module_split.md)（新建）  
- `modules/**/README.md`（新建骨架）  
- `settings.gradle`（注释 include 示例）  
- 本文件 §4 G3.7 行 + **§15**；[docs/spec/README.md](README.md) 索引  
- **无**生产 Java 搬迁；**无** git commit/push  

---

## 16. G3.8 Architectury — skipped / deferred by design

> **Work Unit**: G3.8  
> **日期**: 2026-07-14  
> **状态**: **skipped / deferred by design**（**非**缺陷）  
> **规格**: [g3_8_architectury_decision.md](g3_8_architectury_decision.md)  
> **验证**: 文档 only；**禁止**引入 Architectury 依赖  

### 16.1 决策摘要

| 项 | 结论 |
|:---|:---|
| Architectury / `@ExpectPlatform` | **不引入** |
| 理由 | W3.1–G3.6 手写 SPI 已落地；G3.2/G3.7 无第二 loader 产品线；框架增依赖与锁成本 |
| 再评估 | `ic2r-neoforge` 最小集落地 **且** SPI 稳定后，**可选**评估 ExpectPlatform 是否替换手写 install |
| 默认倾向 | 继续 thin platform SPI（与 neoforge 计划一致） |

### 16.2 变更范围（G3.8）

- [g3_8_architectury_decision.md](g3_8_architectury_decision.md)（新建）  
- 本文件 §4 G3.8 行 + **§16**；[docs/spec/README.md](README.md) 索引  
- **无** `build.gradle` Architectury 依赖；**无** git commit/push  

---

## 17. G3.9 §8.3 架构瘦身切片 — partial

> **Work Unit**: G3.9  
> **日期**: 2026-07-14  
> **状态**: **partial**（Mixin 清单 + API 文档 + **1** 巨型 BE 纯逻辑切片；**未**拆完全部巨型 BE）  
> **规格**: [api_surface.md](api_surface.md)；主文档 §8.3  
> **验证**: `.\gradlew.bat compileJava test` → BUILD SUCCESSFUL  

### 17.1 Mixin 清单（最小集合）

| Mixin | 配置 | 作用 | 事件可替代？ | 结论 |
|:---|:---|:---|:---|:---|
| `RecipeManagerMixin` | `ic2r.mixins.json` → `mixins: ["RecipeManagerMixin"]` | 在 `RecipeManager.fromJson`（含 Forge `ICondition` 重载）HEAD 打配方 ID 调试日志（历史：排查 Create 等不兼容） | Forge 配方加载事件**不能**无损覆盖同一注入点语义；且当前仅为 debug/info 日志，无玩法改写 | **暂保留**；生产可后续改为配置开关或删除，**不**为本 Unit 扩大 Mixin 面 |

**最小集合确认**：仓库 **仅** `me.halfcooler.ic2r.mixin.RecipeManagerMixin` 一枚；`ic2r.mixins.json` 无 client 列表条目。§8.3「Mixin 维持最小集合；能事件则事件」→ 清单 done，替代评估结论：**暂保留**。

### 17.2 API 面文档

| 交付 | 说明 |
|:---|:---|
| [api_surface.md](api_surface.md) | `api/` 粗分 **稳定对外**（energy/recipe/upgrade/crops…）与 **内部迁移中**（network 反射面、CoreAccess、多数 item 工具向接口…） |
| 搬包 | **未做**（DoD：只文档） |

### 17.3 巨型 BE 切片：`CropGrowthMath`

| 项 | 说明 |
|:---|:---|
| 宿主 | `TileEntityCrop`（~1.3k 行，§8.3 优先作物） |
| 抽出 | `core/crop/CropGrowthMath`：base/minimum/provided quality、充足/亏缺 totalGrowth、reset 判定、growthPoints 累加、age-up、cross 资格 base/roll、storage accept（水/WeedEX） |
| 回接 | `performGrowthTick`、`readyToAgeUp`、`checkCrossingAvailability` / `attemptSpreading`、`applyHydration` / `applyWeedEx`；**RNG 调用序保持**（仅 `aux > 100` 时 `nextInt(32)`） |
| 测 | `CropGrowthMathTest`（**≥3**，实际 **10** 条有意义场景） |
| **未做** | 核电 / 采矿机 TE 拆分；整棵 crop TE 干净室重写 |

### 17.4 诚实边界

- G3.9 = **partial**：§8.3 清单三项各有切片，巨型 BE **禁止一次拆光**  
- Origin：`CropGrowthMath` **rewritten 切片**；`core/crop/**` 宿主仍 **residual**  
- **无** Architectury；**无** 全库 BE 拆分；**无** git commit/push  

### 17.5 变更范围（G3.9）

- 新建：`core/crop/CropGrowthMath.java`；`src/test/.../crop/CropGrowthMathTest.java`  
- 改：`core/crop/TileEntityCrop.java`（委托纯逻辑）  
- 文档：本文件 §4 G3.9 + **§17**；[api_surface.md](api_surface.md)；[origin.md](origin.md) 切片行；[README.md](README.md) 索引  

---

## 18. G3.10 — 继承 G1.* 交叉对照（阶段 3 视角）

> **Work Unit**: G3.10  
> **日期**: 2026-07-14  
> **状态**: **partial**（交叉对照 **done**；所描述 G1 债 **未全部清零**）  
> **性质**: 只读交叉对照 + 本文件 / 可选 phase1 指针；**无**强制生产代码；**不**为凑覆盖率堆测  
> **对照对象**: 本文件 §4 **G3.10 原始断言** vs G1.\* 迁移 + 阶段 2/3 后续后的真实状态  
> **前序交叉**: [phase2_closeout.md §8](phase2_closeout.md)（G2.7）；[phase1_closeout.md](phase1_closeout.md) §3 / §8 / §9  

### 18.1 W3.5 时的 G3.10 原始断言（冻结）

| 断言（W3.5 登记） | 含义 |
|:---|:---|
| **TeUpdate 仍默认** | 运行时协议帧仍走 `getNetworkedFields()` + 字符串字段名 + `TeUpdate`/`DataEncoder`；反射 R/W 仍为未注册字段路径 |
| **snake_case 仅试点** | 核心命名面未全库收口；W1.5 / G1.5 为试点+扩域，非 §6.3 #3 勾满 |
| **阶段 1 覆盖率 gap** | §4.5 阶段 1 核心包宽口径 **≪ 60%**（G1.2） |
| 关联 | 整组 **G1.1–G1.8**；不阻塞阶段 3 名义收口（W3.5 已以 gap 登记） |

### 18.2 原始断言 vs 真实状态（阶段 3 视角）

| 原始断言 / Gap | G1 迁移 + 后续（含 G2.7 / G3.3）真实状态 | 关闭判定 |
|:---|:---|:---|
| **TeUpdate 仍默认**（G1.1） | **帧仍默认**：`NetworkManager` 仍按 `getNetworkedFields()` 列表编码；`ContainerBase`/`INetworkDataProvider` 仍字符串字段表。**已推进（G1.1）**：`readFieldValueForNetwork` / `TeUpdate` apply 对 **已注册 Sync** 优先 `tryGetValue` / `trySetValue`（legacy alias）；绑定面含标准机 `gui_progress`/`active`、BatchCrafter、ElectricBlock `redstone_mode` 等。未注册仍反射 | **部分关闭** — Sync **值路径优先**；**协议帧 / 默认路径未切主** |
| **snake_case 仅试点**（G1.5） | 试点 **done**（`energy_buffer`、标准机 `progress`、wire `gui_progress`/`active`）+ **G1.5 扩域**（反应堆 NBT、BatchCrafter Sync、ElectricBlock `redstone_mode`）。**全库未收口**：作物/配置/大量 Container 网络名、组件 ID 等仍 camelCase（`naming_audit`） | **partial** — 非「仅试点」字面已过时，但是**仍远未全库** |
| **阶段 1 覆盖率 gap**（G1.2） | G1.2 复测宽口径 **3.76%**（317/8442）；G3.3 阶段 1 宽口径 **~4.47%**（380/8499）仍 ≪ **60%**。common-ish（阶段 3）**~1.79%** 亦远低于 75%（G3.3） | **仍 open（gap）** — **禁止**宣称 60% / G1 覆盖率 done |
| energy.grid 主体（G1.3） | `EnergyTransferMath` 高覆盖 + EN-\* 部分绿 + G3.3 边界加深；包级 G1.2 **2.81%** → G3.3 **~3.01%**。Calculator/路径/爆炸主体仍 ~0% | **partial** — 切口有；**包级仍极低** |
| 标准机循环（G1.4） | `StandardMachineCycleMath` + SM-001…004 绿 + TE 接线 + G3.3 边界；包级 G1.2 **0.98%** → G3.3 **~1.23%**。巨型 TE 分母主导 | **partial** — 行为单测已有；**包级仍极低** |

### 18.3 G1.1–G1.8 逐条状态（阶段 3 登记）

| ID | 主题 | 状态 | 证据一句话 |
|:---|:---|:---|:---|
| G1.1 | TeUpdate / Sync 优先 | **partial** | 已注册字段 Sync 优先 R/W（`NetworkManager`/`TeUpdate`）；**TeUpdate 帧仍默认**；未注册反射 |
| G1.2 | 核心包 ≥60%（阶段 1） | **gap** | 宽口径 G1.2 **3.76%** / G3.3 宽 **~4.47%** ≪ 60%；**禁止**勾 done |
| G1.3 | energy.grid 可测端口 | **partial** | `EnergyTransferMath`(+Test) 切口；包级仍 ~3% |
| G1.4 | 标准机循环行为测 | **partial** | `StandardMachineCycleMath`(+Test)；SM-001…004；包级仍 ~1% |
| G1.5 | snake_case 全库 | **partial** | 试点 + G1.5 扩域；全库网络/NBT/配置未收口 |
| G1.6 | recipe 匹配器 | **done** | `MachineRecipeMatchMath` + Test；RC 部分绿（运行时仍绑 registries） |
| G1.7 | Spotless/Checkstyle | **done(N/A) / skipped** | phase1 §10：本阶段**不**启用 |
| G1.8 | Blocks 按域拆分 | **done** | `Ic2rBlocks` 门面 + `core/ref/blocks/*` |

### 18.4 一句话总判（G3.10）

| 项 | 值 |
|:---|:---|
| **G3.10 状态** | **partial**（交叉对照 **done**；G1 债 **未全清**） |
| 相对 G3.10 原始三断言 | TeUpdate 默认 **仍真**；snake_case「仅试点」→ **试点+扩域仍 partial**；阶段 1 覆盖率 **仍 gap** |
| 相对 G2.7（阶段 2 交叉） | **一致**：G1.1/3/4/5 partial、G1.2 gap、G1.6–1.8 已关；阶段 3 仅多了 G3.3 覆盖率小幅抬升证据，**未改变**关闭判定 |
| 已关闭（G1 子集） | G1.6、G1.7（N/A skip）、G1.8 |
| **仍 open** | G1.1 帧默认；**G1.2 ≪60%**；G1.3/G1.4 包级；G1.5 全库命名 |
| 覆盖率诚实句 | 阶段 1 门槛 **未达**；宽口径约 **4%** 量级，**不是** 60%；**不得**宣称 G1 全清 |

### 18.5 与 §8.5 / G3.3 关系

- G3.10 **不**重新跑 JaCoCo；覆盖数字引用 [phase1 §8](phase1_closeout.md) 与本文件 **§12（G3.3）**。  
- G3.3 抬 common-ish / 宽口径 **不**关闭 G1.2；G1.2 关闭条件仍是阶段 1 宽口径 **≥60%**（或项目另行改口径）。  
- §6.3 #1（反射网络不再默认）仍 **partial/gap** — 与 G1.1 同债。  

### 18.6 建议后续（**非本 G3.10**；勿空转）

1. **G1.1 真切主**（若产品需要）：更多 TE 注册 Sync；去掉未注册反射默认或 Sync 为唯一写出（wire 策略另开 Unit）。  
2. **G1.3 / G1.4 加深** 有意义行为行 → 再谈 **G1.2** 复测；在 ~4% 量级 **禁止**勾 G1.2 done。  
3. **G1.5** 按 `naming_audit` 分批，与 Sync 切主同步，避免双名地狱。  
4. **不要**：为勾 G3.10 / §6.3 堆无断言测或全库 rename。  

### 18.7 变更范围（G3.10）

- 本文件 §4 G3.10 行 + §4.1 + **§18**  
- 可选：[phase1_closeout.md](phase1_closeout.md) 末尾 G3.10 指针  
- [Modernization_Progress.md](../Modernization_Progress.md) G3.10  
- **无**生产代码功能改动；**无**为凑覆盖率新测；**无** git commit/push  

---

## 19. G3.11 — 继承 G2.* 交叉对照（阶段 3 视角）

> **Work Unit**: G3.11  
> **日期**: 2026-07-14  
> **状态**: **partial**（交叉对照 **done**；所描述 G2 债 **未全部清零**；**§7.7 未勾满**）  
> **性质**: 只读交叉对照 + 本文件 / 可选 phase2 指针；**无**强制生产代码；**不**为凑覆盖率堆测；**不**伪造 e2e  
> **对照对象**: 本文件 §4 **G3.11 原始断言** vs G2.\* 迁移后真实状态  
> **前序**: [phase2_closeout.md](phase2_closeout.md) §1 / §3 / §8；契约 [item_handler_contract.md](item_handler_contract.md) / [fluid_handler_contract.md](fluid_handler_contract.md) / [energy_bridge_contract.md](energy_bridge_contract.md) / [recipe_manager_query_eval.md](recipe_manager_query_eval.md)

### 19.1 W3.5 时的 G3.11 原始断言（冻结）

| 断言（W3.5 登记） | 含义 |
|:---|:---|
| **管道 / AE2 e2e 无** | 无真实漏斗/管道模组/AE2 导入总线集成测或 GameTest；仅 Math 文档化门闩 |
| **配方非全机型直查** | 运行时非每 tick `RecipeManager` 直查；非 basic / 特殊管理器路径异构 |
| **~47 guidef** | 存量 `assets/ic2r/guidef/*.xml` 仍服务生产 GUI；新 UI 仅冻结+样板 |
| **DataGen 窄** | 仅 Tags 类起步；Recipes / BlockState / Lang DataGen 未起 |
| 关联 | 整组 **G2.1–G2.8**；不阻塞阶段 3 名义收口（W3.5 已以 gap 登记） |

### 19.2 原始断言 vs 真实状态（阶段 3 视角）

| 原始断言 / Gap | G2 迁移 + 后续真实状态 | 关闭判定 |
|:---|:---|:---|
| **管道 / AE2 e2e 无** | **仍真**。`InvSlotTransferMath` / `InvSlotHandlerMathTest` + [item_handler_contract.md](item_handler_contract.md)；`FluidHandlerMathTest` 镜像 `Ic2rFluidTankHandler`；**无** `src/test` 下 AE2/管道/GameTest。`integration/ae2` 仍无 e2e | **仍 open（residual）** — 文档化测例 **partial**（§7.7 #1）；**禁止**宣称管道/AE2 稳定 e2e |
| **配方非全机型直查** | **仍大体真**。basic 三 type（macerator / extractor / compressor）JSON→`RecipeManager`→`loadBasic` **materialize** 全链路已文档（[recipe_manager_query_eval.md](recipe_manager_query_eval.md)）；`findMatching` API 已备但 **推荐暂保持 materialize**。tick **未**切直查；centrifuge / ore_washer / metal_former 等非 basic 异构 | **partial** — basic 主路径试点加深；**非**「全机型直查」；§7.7 #2 **未**勾满 |
| **~47 guidef** | **数字过时**。G2.3 迁 storage_box 全档纯代码 Menu/Screen，删 **6** XML；现存 guidef **41** 份（`assets/ic2r/guidef/*.xml` 枚举）。`GuiParser` / `Dynamic*` 兼容层仍在；生产机台 GUI **多数**仍 XML | **partial** — 冻结+样板+storage_box 切片 done；**存量 ~41 residual** |
| **DataGen 窄** | **仍真（略扩）**。W2.5：3 item tags；G2.6：+ `Ic2rBlockTagsProvider` → `mineable/wrench` block tag。产物：`src/generated/resources/data/ic2r/tags/{items,blocks}/**`。**Recipes / Models / Lang DataGen 未起步**（配方 JSON 仍手写 `src/main/resources`） | **partial** — tags 两类；**仍窄**；非全 DataGen |
| **覆盖率 ≪70%**（G2.4 / 断言隐含） | W2.6 窄口径 **~3.53%**；G3.3 窄 **~5.90%**（143/2425）仍 ≪ **70%**。common-ish **~1.79%**。纯 Math 近满，整包 InvSlot/Fluid/v2 拉低 | **仍 open（gap）** — **禁止**宣称阶段 2 适配门槛 70% 或 §7.7 全勾满 |

### 19.3 G2.1–G2.8 逐条状态（阶段 3 登记）

| ID | 主题 | 状态 | 证据一句话 |
|:---|:---|:---|:---|
| G2.1 | ITEM_HANDLER / 管道-AE2 交互 | **partial + residual** | 契约+`InvSlotHandlerMathTest` 文档化门闩；**无**真管道/AE2 e2e；Handler/cap 附着本体 0% 路径仍 residual |
| G2.2 | 配方 RecipeManager 主路径 | **partial + residual** | basic×3 materialize 全链路 + 直查评估文档；tick 非直查；非 basic 异构 |
| G2.3 | 新 UI 零 XML / guidef | **partial + residual** | 冻结+`CodeGuiSample`+storage_box 代码 GUI；**~41** guidef + Dynamic 仍服务生产 |
| G2.4 | inv/fluid/recipe 覆盖率 ≥70% | **gap** | 窄口径 G3.3 **~5.90% ≪70%**；与 G3.3/G1.2 同债族 |
| G2.5 | 流体 FLUID_HANDLER 一等 | **partial + residual** | [fluid_handler_contract.md](fluid_handler_contract.md)+`Ic2rFluidTankHandler`+`FluidHandlerMathTest`；真流体管道 e2e **无** |
| G2.6 | DataGen 扩面 | **partial + residual** | 3 item tags + 1 block wrench tag；Recipes/Models/Lang **未** DataGen |
| G2.7 | 继承 G1.\*（TeUpdate/覆盖率） | **partial**（交叉 **done**） | 与 G3.10 / phase2 §8 **一致**：G1.1/3/4/5 partial、**G1.2 gap**、G1.6–1.8 已关 |
| G2.8 | FE/RF 能量桥 | **partial + residual** | `EnergyBridgeMath`+`PlatformEnergyBridgeForge`+契约+测；config 开关 / IC 机暴露 FE / FE e2e **无** |

### 19.4 一句话总判（G3.11）

| 项 | 值 |
|:---|:---|
| **G3.11 状态** | **partial**（交叉对照 **done**；G2 债 **未全清**） |
| 相对 G3.11 原始四断言 | 管道/AE2 e2e **仍无**；配方全机型直查 **仍无**；guidef **~47→~41**（部分关闭字面）；DataGen **仍窄** |
| 相对 phase2 §3 G2.\* | G2 队列 Unit **均已交付推进**；状态多为 **partial + residual**；**仅 G2.4 为明确 gap**；无一条可宣称 §7.7 全标 done |
| 已缓解（切片） | ITEM/FLUID Math 契约与测；basic×3 RecipeManager 链路；GUI 冻结+storage_box；Tags DataGen；FE bridge Math；G2.7 交叉 |
| **仍 open** | 真 e2e；tick 直查 / 非 basic；~41 guidef；**窄口径 ≪70%**；DataGen 配方/模型/语言；FE 暴露与 e2e |
| 覆盖率诚实句 | 阶段 2 门槛 **未达**；窄口径约 **6%** 量级，**不是** 70%；common-ish **~1.79%**；**不得**宣称 §7.7 全勾满或 G2 全清 |

### 19.5 与 §7.7 / §8.5 / G3.3 关系

| §7.7 标准 | 阶段 3 视角判定 | 说明 |
|:---|:---|:---|
| #1 管道/AE2 文档化测例 | **partial** | Math 测例有；真 e2e **无** |
| #2 配方主路径 RecipeManager | **partial** | basic materialize 试点；非全机型直查 |
| #3 新 UI 零 XML | **partial** | 新增冻结；存量 guidef **~41** |
| #4 覆盖率 ≥70% 适配 | **gap** | 窄 **~5.90%** |
| DataGen（§7.6，非 §7.7 勾选项） | **partial / 窄** | tags only |

- G3.11 **不**重新跑 JaCoCo；覆盖数字引用本文件 **§12（G3.3）** 与 [phase2_closeout.md](phase2_closeout.md) §2。  
- G3.3 抬窄口径 **3.53%→5.90% 不**关闭 G2.4；关闭条件仍是阶段 2 窄口径 **≥70%**（或项目另行改口径）。  
- §8.5 **未**因 G3.11 勾满；G3.11 **不**替代 G3.1–G3.9 架构债。  

### 19.6 建议后续（**非本 G3.11**；勿空转 / 勿伪造 e2e）

1. **G2.1 / G2.5**：可选 GameTest 或轻量 mock Handler 行覆盖；**禁止**本交叉 Unit 引入 AE2 硬依赖测。  
2. **G2.2**：非 basic 管理器对齐 materialize 或事件化动态配方后，再评估 tick 直查。  
3. **G2.3**：按机台分批迁代码 Menu/Screen；删 guidef 与 Dynamic 需产品节奏。  
4. **G2.4**：在有意义 InvSlot/Fluid/Serializer 行累积后再复测；**~6% 禁止勾 done**。  
5. **G2.6**：至少 1 类 recipe 或 models DataGen（非手写复制）。  
6. **G2.8**：config 比率 + 可选 IC 机 FE cap 暴露；e2e 后置。  
7. **不要**：为勾 G3.11 / §7.7 堆无断言测、伪造 AE2 e2e、或宣称 70% 已达。  

### 19.7 变更范围（G3.11）

- 本文件 §4 G3.11 行 + §4.1 + **§19**  
- 可选：[phase2_closeout.md](phase2_closeout.md) 末尾 G3.11 指针  
- [Modernization_Progress.md](../Modernization_Progress.md) G3.11  
- **无**生产代码功能改动；**无**为凑覆盖率新测；**无**伪造 e2e；**无** git commit/push  

