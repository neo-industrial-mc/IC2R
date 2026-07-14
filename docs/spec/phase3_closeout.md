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
| G3.10 | **继承 G1.\***：TeUpdate 仍默认、snake_case 仅试点、阶段 1 覆盖率 gap | P0 | 见 phase1_closeout；不阻塞本收口登记 |
| G3.11 | **继承 G2.\***：管道/AE2 e2e 无、配方非全机型直查、~47 guidef、DataGen 窄 | P0–P2 | 见 phase2_closeout |

### 4.1 与 G1 / G2 继承关系

```text
G1.*（阶段 1 基础设施缺口）
  └─ 仍 open：G1.1 TeUpdate 默认、G1.2–G1.4 覆盖/电网/标准机、G1.5 命名…
G2.*（阶段 2 Forge 生态试点缺口）
  └─ 仍 open：G2.1–G2.6 适配/配方/GUI/DataGen…
G3.*（本文件）
  └─ 叠加：loader 隔离、多 loader 最小集、common 覆盖率 75%、Origin 清零、EnvProxy 退役
```

阶段 3 **不能**关闭 G1/G2；关闭条件仍在各自专项。G3.3 与 G1.2/G2.4 **同一覆盖率债**的不同门槛表述。

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

