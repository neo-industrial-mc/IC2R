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
| 2 | 至少一条**非 Forge**加载器可运行最小集（物品注册 + 一台机器 + 电网） | **deferred** | **W3.4** 仅落地 [neoforge_migration_plan.md](neoforge_migration_plan.md) **文档级**计划；主构建仍 **Forge 1.20.1** 单模块；**无** `ic2r-neoforge` / `ic2r-fabric` 源集或 artifact；无可运行非 Forge 最小集。计划明确：M8 前再评估选项 A/B | W3.4 |
| 3 | 覆盖率达 §4.5 阶段 3 门槛（**≥ 75%** common 全量，不含 client 渲染） | **gap** | 见 §2。overall LINE **~0.90%**（355/39464）；common-ish（排除 forge/integration/gui/render）**~1.06%**（355/33429）≪ 75%。阶段 1/2 窄/宽口径与收口时一致量级，**无**覆盖率跃升 | W0.1+ 历史；阶段 3 未加测 |
| 4 | Origin 文档中「移植残留」**核心模块清零**或仅剩标注兼容层 | **partial / gap** | [origin.md](origin.md) 仍为 W0.6 初版表；P0 residual（EnergyNet IC 路径、标准机、`network` 反射同步、InvSlot、reactor/crop 等）**未**升为 rewritten/original。阶段 1–3 **局部现代化**（Sync 骨架、显式 Tick、Handler 委托、macerator RecipeManager、SPI 草案+首迁）降低部分路径风险，但 **Origin 核心 residual 未清零** → 诚实 **partial**（有增量）/ **gap**（未达 §8.5 清零） | W0.6 + W1–W3 局部 |

### 1.1 勾选视图（阶段 3 名义完成度）

```text
[gap]      common 无 loader 实现型 import     ← platform 洁净；core ~57 文件仍含 Forge
[deferred] 非 Forge 最小可运行集             ← W3.4 仅计划；主线仍 Forge 1.20.1
[gap]      覆盖率 ≥75% common（§4.5 阶段 3）
[partial]  Origin residual 核心清零          ← 有现代化切片；表内 P0 仍 residual
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
| `me...forge` / `forge.model` | **0%** | 0/1753 | loader 实现层 |
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
| G3.2 | 非 Forge 最小可运行集 **未启动**（§8.5 #2） | P0 | SPI facet 实质实现 + EnvProxy 主路径退役过半后，追加 Unit：`ic2r-neoforge` 骨架 → 物品+1 机+电网最小集（计划 M8） |
| G3.3 | 覆盖率 common ≪ 75%（~1%） | P0 | **继承 G1.2/G2.4**；优先 EnergyNet / 标准机循环 / Sync 切主后 e2e；勿堆 SPI 接口空测 |
| G3.4 | Origin residual **核心未清零**（energy IC、标准机、network 反射、InvSlot、reactor/crop…） | P0 | 域重写 Unit + 回写 origin.md；清零前禁止宣称 §8.5 #4 done |
| G3.5 | EnvProxy 上帝代理仍在；仅 `isClientEnv` 退役；`isForgeEnv`/`isFabricEnv`/`getServer`/注册族/流体物品工厂未切 | P0 | 延续 W3.3 切片模式（E2→E6，见 neoforge_migration_plan §4.2） |
| G3.6 | `PlatformServices` 除 lifecycle 外 facet 多为 **stub**；`platform.services` 0% 测 | P1 | 实现 Registry/Network/… 委托；关键路径可测时再补测 |
| G3.7 | 无物理多模块（`ic2r-common` / `ic2r-neoforge` / `ic2r-fabric`） | P1 | 逻辑边界稳定后拆 Gradle；主线默认仍可只 assemble Forge |
| G3.8 | Architectury **未**引入 | P2 | **有意延期**（主文档：先手写 thin platform）；非缺陷 |
| G3.9 | 巨型 BE / api 面瘦身 / Mixin 最小集（§8.3）未作为 W3.\* 推进 | P2 | 后置域 Unit；与 G3.4 Origin 联动 |
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
| 物理多模块 Gradle | 未做 |
| Architectury | 未引入（有意） |
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
| [origin.md](origin.md) | residual/rewritten/original 初版（G3.4） |
| [golden_suite.md](golden_suite.md) | 行为规格；阶段 3 无新 Golden 条目 |
| `me.halfcooler.ic2r.platform.services.*` | SPI 接口与访问器 |
| `me.halfcooler.ic2r.forge.ForgePlatformServices` / `PlatformLifecycleForge` | Forge 安装与 lifecycle 实现 |
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
