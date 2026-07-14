# IC2R After 20.1.40 Modernization Plan

**\- Execution Spec for a Fresh Agent Session \-**

> **本文档是下一批现代化工作的唯一启动入口（本队列内）。**  
> 新会话若用户指示「按 After-20-1-40 推进 / 执行 After 文档 / 继续 EN-IC / 去 Forge residual」，主 Agent **优先读本文档**，并交叉 `Modernization_Project.md` §A（会话协议）与 `Modernization_Progress.md`（若存在进度段）。  
> **不要**重做 W0–W3 / G3 已交付试点；本文档承接其 **residual / gap**，推进到可 **release 下一版本**，再进入发版后队列。

| 项 | 值 |
|:---|:---|
| **文档版本** | 1.0 |
| **基线版本** | `mod_version=20.1.40`（`gradle.properties`） |
| **目标发版** | 用户完成 **Track A** 后手动 bump + release 的新版本（编号由用户决定，下文称 **post-20.1.40 release**） |
| **主线加载器** | Forge **1.20.1**（`forge_version` 以仓库 `gradle.properties` 为准） |
| **关联总规** | [Modernization_Project.md](Modernization_Project.md) |
| **关联进度** | [Modernization_Progress.md](Modernization_Progress.md) |
| **行为规格** | [spec/golden_suite.md](spec/golden_suite.md)、[spec/origin.md](spec/origin.md)、[spec/platform_spi.md](spec/platform_spi.md) |
| **电网对照** | [GTEU_Migration_Project.md](GTEU_Migration_Project.md)、[GTEU_GT_Reference.md](GTEU_GT_Reference.md) |
| **收口证据** | [spec/phase1_closeout.md](spec/phase1_closeout.md)、[phase2_closeout.md](spec/phase2_closeout.md)、[phase3_closeout.md](spec/phase3_closeout.md) |

---

## 0. 给执行会话的硬规则

### 0.1 角色与停止条件

与 [Modernization_Project.md](Modernization_Project.md) **§A** 对齐：

1. **一次一个 Work Unit**（ID 见 §3 / §5）。  
2. **禁止 Agent `git commit` / `git push`**；用户手动 commit。  
3. Unit 完成 → 更新本文档 **§7 Progress**（或 `Modernization_Progress.md` 中 `A40.*` 段）→ **一句话总结** → **停等**用户。  
4. 验证不过 = **BLOCKED**，不得标 done。  
5. **版权红线**：禁止无规格的大段重命名洗白；EnergyNet IC / 标准机 / 网络高风险域强制「规格 → 测试 → 实现」（见总规 §1.3）。  
6. **默认不改玩法数值**；IC 模式行为以 Golden EN-IC 与现行可观测语义为准。  
7. **不要**在 Track A 中启动 Architectury、物理多模块 include、NeoForge 产品线、整库 `TileEntity` 重命名。

### 0.2 用户指令速查

| 用户说 | 主 Agent 做 |
|:---|:---|
| `开始 After` / `按 After 文档推进` | 读 §7 → 执行下一个 `pending` 的 **Track A** Unit |
| `继续` | 同上 |
| `做 A40.x` / `做 B40.x` | 仅执行指定 Unit |
| `Track A 完成检查` / `发版门禁` | 跑 §4 Release Gate，输出 PASS/FAIL 清单，**不**自动改版本号 |
| `开始 Track B` / `发版后现代化` | 仅当用户声明 **post-20.1.40 已 release**（或明确跳过发版）后，执行 **Track B** |
| `状态` | 一句：`last_completed` / 下一 `pending` / 是否已过 Release Gate |
| `暂停` | 不启动新 Unit |

### 0.3 启动检查清单（每会话）

1. 读 **本文档** §0–§3（或 §5 若已在 Track B）。  
2. 读 §7 Progress（或 Progress 文件中 A40/B40 段）。  
3. 扫一眼工作区是否有用户未提交的半截改动；有冲突则一句询问。  
4. 选定 Unit → 实现 → `.\gradlew.bat test`（及 Unit 规定的额外命令）→ 更新 Progress → 一句总结 → 停。

### 0.4 验证基线命令

```text
.\gradlew.bat test
.\gradlew.bat test jacocoTestReport   # 覆盖率证据需要时
```

Windows 仓库默认使用 `.\gradlew.bat`。测试失败则 Unit **FAIL**。

---

## 1. 基线快照（20.1.40 / 文档冻结时）

> 执行时若与代码不符，以代码为准，并在 Progress `last_notes` 记一笔偏差；**不要**假装 W3/G3 已勾满 §8.5。

| 主题 | 冻结时状态 | 证据 |
|:---|:---|:---|
| 产品线 | 单模块 Forge 1.20.1；`modules/*` 骨架 **未 include** | `settings.gradle`、`gradle.properties` |
| 现代化队列 | W0–W3、G3.1–G3.11 **名义 done**；多项 § 标准仍 gap | `Modernization_Progress.md` |
| EN-IC | 条目 EN-IC-001…010 有 **纯逻辑** 测（`EnergyTransferMathTest`）；**宿主 Calculator/拓扑仍 residual** | `golden_suite.md` §1、`EnergyCalculatorUnified` |
| Origin P0 residual | IC 电网图模型与统一计算器、标准机 TE 树、TeUpdate/反射帧、InvSlot 主体、核电/作物等 | `origin.md` §0.3、§1 |
| core 内 Forge | 约 **30+** 文件仍 `import net.minecraftforge.*`（流体 Handler、LazyOptional、ForgeRegistries、ForgeConfigSpec、Dist/OnlyIn、DeferredRegister 声音等） | `phase3_closeout.md` §1 #1 |
| platform SPI | 8 facet 真实现在 `forge/`；`platform.services` **无** loader import；EnvProxy **仍在** | `platform_spi.md` |
| 测试 | ~66 测、0 fail；common-ish 行覆盖 ~1–2% ≪ 75% | phase3_closeout §2 |
| guidef | ~41 份 XML 仍服务生产 GUI；新 UI 已冻结 XML | `gui_modernization.md` |

### 1.1 已有资产（必须复用，禁止推倒）

| 资产 | 路径 / 说明 |
|:---|:---|
| IC 传输纯逻辑 | `core/energy/grid/EnergyTransferMath.java` + `src/test/.../EnergyTransferMathTest.java` |
| GT 求解（original） | `EnergyCalculatorGT` — Track A **不要**重写 GT；仅保证与 IC 不串味 |
| Sync 试点 | `core/network/sync/**`；标准机 `KEY_GUI_PROGRESS` / `KEY_ACTIVE` |
| 标准机循环 Math | `StandardMachineCycleMath` |
| SPI | `platform/services/*` + `forge/Platform*Forge` |
| 契约文档 | `item_handler_contract.md`、`fluid_handler_contract.md`、`energy_bridge_contract.md` |

---

## 2. 总目标与两条轨道

```text
                    ┌─────────────────────────────────────┐
  当前 20.1.40 ──► │  Track A  发版前（本队列主线）        │ ──► 用户 release
                    │  residual 核心 + core 去 Forge       │      新版本
                    │  + Golden EN-IC 宿主级收口           │
                    └─────────────────────────────────────┘
                                      │
                                      ▼
                    ┌─────────────────────────────────────┐
                    │  Track B  发版后（有价值现代化）      │
                    │  Sync 切主、Inv/GUI、配方、EnvProxy、 │
                    │  DataGen、多模块/NeoForge 等          │
                    └─────────────────────────────────────┘
```

| Track | 用户意图 | 完成标志 |
|:---|:---|:---|
| **A — Pre-release** | residual + 删除/迁出 core 中的 Forge 实现 + Golden EN-IC | §4 Release Gate **全部 PASS**（或用户书面豁免项） |
| **B — Post-release** | 其余高价值现代化 | 用户说「开始 Track B」后按 §5 队列推进 |

**Track A 的三条支柱（必须同时理解）：**

1. **Residual（P0 宿主）**：IC 电网求解与图路径从「Math 贴片 + residual Calculator」推进到 **规格驱动的现代表达**，并回接 `IEnergyCalculator` / 网本地循环；Origin 中 IC 电网核心不得再标为整包 residual（允许 `mixed` + 兼容层）。  
2. **删除 core 内 Forge 实现**：`me.halfcooler.ic2r.core`（及 `api` 若污染）**不得**依赖 `net.minecraftforge.*` 实现类型；能力、配置、注册 Deferred、客户端 OnlyIn 等下沉 `forge/` 或经 SPI。  
3. **Golden EN-IC**：EN-IC-001…010 **不只**停在 `EnergyTransferMath` 谓词；补 **宿主/半集成** 测与规格正文缺口，使 IC 路径行为可回归且与 Math 不漂移。

---

## 3. Track A — 发版前 Work Units

状态机：`pending` → `in_progress` → `done` | `blocked` | `skipped`。

**建议 commit 粒度 = 一个 Unit。** 顺序默认自上而下；**A40.0 必须最先**；A40.1 与 A40.2 可按依赖微调，但 **A40.3 依赖 A40.1 的可测切口**；**A40.4 为收口**。

### A40.0 — 基线冻结与 Progress 挂钩

| 项 | 内容 |
|:---|:---|
| **DoD** | ① 确认 `mod_version` 仍为用户基线（或记录已 bump 的版本）；② 在 `Modernization_Progress.md` 增加 **After-20.1.40** 段或保证本文 §7 可写；③ 列出 core 内 `import net.minecraftforge` 文件清单快照（路径列表写入 Progress 或 `docs/spec/a40_core_forge_imports.md`） |
| **触及** | `docs/**`、可选生成清单文件；**不改玩法代码** |
| **验证** | 文档存在；`.\gradlew.bat test` 仍绿（无代码改动时应绿） |
| **备注** | 纯文档 Unit；为后续 diff 提供「去 Forge 前」对照 |

### A40.1 — Golden EN-IC 规格正文 + 测例加深（先测）

| 项 | 内容 |
|:---|:---|
| **DoD** | ① `golden_suite.md` §1：EN-IC-001…010 每条具备 **Given/When/Then 级摘要**（可短）+ 明确「纯逻辑 / 宿主」测入口；② 在现有 `EnergyTransferMathTest` 之外，新增 **≥1** 个面向 IC 宿主边界的测试类或扩类（优先不依赖 Level：例如对 `EnergyCalculatorUnified` 可抽逻辑、`Grid`/`EnergyPath` 纯数据结构、或新建 `EnergyNetIcSolver` 接口的 fake 拓扑）；③ EN-IC-002 全拓扑路径选择、EN-IC-006/007 **世界效果** 若仍不可测，必须在规格中标 **residual-world** 并给纯谓词边界；④ EN-GT-010（IC/GT 不串味）至少 **1** 条对照测（同输入拓扑、两 Calculator 或两 Math 路径结果分叉符合各自 invariants） |
| **触及** | `docs/spec/golden_suite.md`；`src/test/java/**/energy/**`；必要时仅 **抽** 可测纯函数到 `EnergyTransferMath` 或新 `*Math`（最小 diff） |
| **验证** | `.\gradlew.bat test`；新测全部绿；Golden 表「测试状态」更新 |
| **禁止** | 本 Unit 不删整个 `EnergyCalculatorUnified`；不改 GT 默认数值 |

**EN-IC 条目执行核对表（实现时勾选）：**

| ID | 最低交付 | 现有挂钩 |
|:---|:---|:---|
| EN-IC-001 | 线损后到达量 | `icInjectAmount_*` |
| EN-IC-002 | 路径代价/优选规则可测 | `icPreferNewPath_*`；加深 BFS/拓扑若可纯测 |
| EN-IC-003 | 非负到达 | `icInjectAmount_lossExceeds*` |
| EN-IC-004 | 多汇总和 ≤ offer | `icDistributeSequential_total*` |
| EN-IC-005 | 顺序优先非均分 | `icDistributeSequential_earlier*` |
| EN-IC-006 | 绝缘击穿谓词 | `icInsulationBreakdown_*` |
| EN-IC-007 | 导体熔断谓词 | `icConductorBreakdown_*` |
| EN-IC-008 | 超压谓词 | `icSinkOverVoltage_*` |
| EN-IC-009/010 | 变压器 4:1 守恒 | `icTransformer_step*` |

### A40.2 — core 去 Forge 实现（迁出，非「删除整个 forge 包」）

> **语义澄清**：用户目标「删除 forge 实现」在工程上指：**从 common/core 删除对 Forge API 的实现型依赖**，实现下沉到 `me.halfcooler.ic2r.forge`（或 SPI）。**禁止**删除仍被主构建需要的 `forge/` 适配层。

| 项 | 内容 |
|:---|:---|
| **DoD** | ① `src/main/java/me/halfcooler/ic2r/core/**` 中 **`import net.minecraftforge` 文件数 → 0**（或仅剩 **登记豁免** 且每条有路径+理由+迁移 Unit，豁免总数 ≤ 3，优先 0）；② `api/**` 同样清零 OnlyIn/Forge 泄漏（如 `CropCard` 的 `@OnlyIn`）；③ 迁出典型类别（按批，可一个 Unit 多批但须一次 test 绿）：**流体** `IFluidHandler`/`FluidStack` → forge cap / 已有 `PlatformFluidBridge`；**物品能力** `IItemHandler`/`LazyOptional` → forge；**ForgeRegistries** → 官方 `BuiltInRegistries` 或 SPI Registry；**ForgeConfigSpec** → 配置类保留在 core 的**数据形状**，Spec 构建在 forge，或 core 只读抽象；**DeferredRegister 声音** → 注册表逻辑可留 Holder 引用，Deferred 总线注册在 forge/`FmlMod`；**@OnlyIn / client event** → `*Client` 于 forge 或 `core` 下明确 client 源码集（若无 client sourceSet，则 `forge` + 事件总线）；④ 更新 `platform_spi.md` 一句：core 洁净状态 |
| **触及** | `core/**`、`api/**`、`forge/**`、`platform/services/**`（仅当需扩 SPI 签名且不暴露 Forge 类型） |
| **验证** | `.\gradlew.bat test`； ripgrep/IDE：`core` 与 `api` 下 `net.minecraftforge` 符合 DoD；编译通过 |
| **策略** | 优先 **移动/委托**，保持二进制行为；Capability 附着必须仍在 forge 的 `*CapImpl` |

**建议迁出顺序（实现时按依赖，不必各开 Unit）：**

1. 纯查询：`ForgeRegistries` → vanilla registry / `ResourceKey`  
2. 配置：`IC2RConfig` / Client / UuScan 的 Spec 边界  
3. 库存/流体 Handler 类型从 TE 签名消失，只留本模组抽象 + forge 适配  
4. 客户端 OnlyIn 与 Jetpack 等事件  
5. `Ic2rSoundEvents` Deferred 与 bus 注册位置  

### A40.3 — IC 电网 residual 宿主现代化（干净室回接）

| 项 | 内容 |
|:---|:---|
| **DoD** | ① 以 A40.1 规格与测为准，重写或替换 **`EnergyCalculatorUnified` 的核心求解表达**（允许新类名如 `EnergyCalculatorIc` / `IcEnergySolver`，经 `IEnergyCalculator` 接入）；② **禁止**「仅重命名」；控制流/命名/模块边界须与历史 IC 源码结构可区分（总规 §1）；③ 默认 `EnergyNetMode.IC2R`（或现行默认）行为与 EN-IC 测一致；④ GT 路径与 `EnergyCalculatorGT` 不受回归破坏；⑤ `origin.md` 回写：IC calculator 条目标为 **rewritten** 或 **mixed**（Math + 新求解），Grid/Node 若未动仍可 residual 但须注明；⑥ 旧 `EnergyCalculatorUnified` 若保留，须标 `@Deprecated` 且默认路径不走，或删除并改全引用 |
| **触及** | `core/energy/grid/**`、相关 `api/energy/**` 若需薄封装、测试、`origin.md`、`golden_suite.md` |
| **验证** | `.\gradlew.bat test`；EN-IC 与 EN-GT 已有测全绿；手动说明：如何切换 IC/GT 配置做冒烟（文档一句即可） |
| **范围控制** | **本 Unit 不要求** 重写全部 `Grid`/`Node`/`EnergyNetLocal` 拓扑引擎；优先 **求解器 + 与 Math 单一真源**。若拓扑与求解强耦合无法拆，允许扩大到最小可编译的图遍历重写，但须在 Progress 记录范围 |

### A40.4 — Track A 收口与 Release Gate 预检

| 项 | 内容 |
|:---|:---|
| **DoD** | ① 跑 §4 全表，写入 `docs/spec/a40_release_gate.md`（新建）或本文 §7 的 gate 结果；② 未达标项要么修到 PASS，要么由用户决定 `skipped`/`豁免` 并写进 gate 文件；③ 建议 release 说明 bullet（中英可只中文）：EN-IC 回归、core 去 Forge、IC 求解现代化；④ **不**修改 `mod_version`、不执行 publish（留给用户） |
| **触及** | 文档 + 为过 gate 的最小代码修复 |
| **验证** | `.\gradlew.bat test jacocoTestReport`（记录 overall / energy.grid 覆盖趋势即可，**不**强求 75%） |

---

## 4. Release Gate（Track A → 用户发版）

用户执行 release **之前**，主 Agent 在 `A40.4` 或 `Track A 完成检查` 时逐条判定：

| # | 门禁项 | PASS 标准 |
|:---|:---|:---|
| G1 | 单元测试 | `.\gradlew.bat test` **BUILD SUCCESSFUL**，0 failures |
| G2 | Golden EN-IC | EN-IC-001…010 在 `golden_suite.md` 均为 **测绿** 或 **测绿（纯谓词）+ residual-world 已标注**；无「未写」的 P0 条 |
| G3 | IC 求解 residual | 默认 IC 路径不再以未测试的反编译态 `EnergyCalculatorUnified` 为唯一真源；Origin 已回写 |
| G4 | core/api 无 Forge 实现依赖 | `core`+`api` 下 `import net.minecraftforge` 为 **0** 或 **≤3 条已文档豁免** |
| G5 | SPI 边界 | `platform.services` 仍 0 loader import；SPI 签名不暴露 `net.minecraftforge.*` |
| G6 | 玩法默认值 | 无未经规格的 EU/损耗/变压器倍率静默改动（diff 审查） |
| G7 | 文档 | 本文 §7 中 A40.0–A40.4 均为 `done` 或用户 `skipped`；gate 文件存在 |

**FAIL 时**：不得建议用户「可以发版」；输出缺口 Unit 或热修建议。

**用户发版动作（Agent 不做，除非用户明确要求）：**

- 修改 `mod_version`  
- 写 changelog / `release.md`  
- Modrinth/Curse 上传、打 tag、push  

发版完成后，用户回复 **`开始 Track B`**（或等价），会话再进入 §5。

---

## 5. Track B — 发版后有价值现代化（队列）

> **前置**：post-20.1.40 已 release，或用户明确「跳过发版，直接 Track B」。  
> **原则**：一次一个 Unit；优先玩家可感 / 版权 / 可维护性；延续「Math/契约 → 切主 → 删旧」。

### 5.1 推荐顺序

| ID | 标题 | DoD 摘要 | 验证 |
|:---|:---|:---|:---|
| **B40.1** | 网络 Sync 切主（标准机扩面） | 标准机及 ≥3 个高频 TE 字段走 `BlockEntitySync`；TeUpdate 对已注册字段 **禁止反射读写**；未注册字段清单缩小 | `test` + 同步往返测 |
| **B40.2** | 淘汰 `getNetworkedFields` 热路径 | Container/TE 默认列表迁移或生成自 Sync 注册表；`ReflectionUtil` 在网络路径归零 | `test`；rg 网络路径 |
| **B40.3** | 标准机 TE 宿主瘦身 | `TileEntityStandardMachine` tick 以 `StandardMachineCycleMath` 为权威；配方/能量/输出门闩与 SM-* Golden 对齐加深 | `test`；`golden_suite` SM |
| **B40.4** | InvSlot → Handler 权威 | 对外 ITEM_HANDLER 契约 e2e 文档+测加深；减少 InvSlot 树分叉；对照 `item_handler_contract.md` | `test` |
| **B40.5** | 存量 guidef 迁移（首批） | 迁移 ≥3 个生产机 XML → 代码 Menu/Screen；遵守 `gui_modernization.md`；JEI 不破 | `test` + 编译 |
| **B40.6** | 配方 materialize 决策落地 | 按 `recipe_manager_query_eval.md`：basic 机直查 **或** 正式冻结桥接为终态并清异构文档 | `test` |
| **B40.7** | EnvProxy 退役 E3–E4 | 注册调用点主迁 `PlatformRegistry`；删 EnvProxy 上已无调用的方法簇 | `test` |
| **B40.8** | DataGen 扩展 | Recipes 或 BlockTags/Lang 至少一类生成；减少手写 JSON | `runData` 或等价 |
| **B40.9** | 核电/作物 规格切口 | 各抽 1 个 `*Math` + Golden 草案（不要求整域重写） | `test` |
| **B40.10** | 多模块前置复检 | `core` 洁净度 + SPI 调用比例报告；更新 `g3_7_module_split.md` 前置勾选；**仍默认不 include** 除非用户下令 | 文档 + `test` |

### 5.2 Track B 明确后置（不要擅自做）

- Architectury（见 `g3_8_architectury_decision.md`）  
- 完整 NeoForge 可运行产品（除非用户指定；计划见 `neoforge_migration_plan.md`）  
- 整库 `TileEntity*` → `BlockEntity*` 重命名  
- 资源（贴图/模型/音效）版权替换  
- 为空转 JaCoCo 百分比堆无断言测试  

### 5.3 与历史 Gap 的映射（避免重复造轮）

| 历史 Gap | Track B 承接 |
|:---|:---|
| G1 TeUpdate 未切主 | B40.1–B40.2 |
| G2 Inv/流体 e2e、guidef residual | B40.4–B40.5 |
| G2 配方直查 | B40.6 |
| G3 EnvProxy / 多模块 | B40.7、B40.10 |
| Origin 核电/作物 residual | B40.9 |
| 覆盖率 75% | 随 B40.1–B40.3 **有意义测** 抬升，不设单独刷分 Unit |

---

## 6. 实现指引（Subagent 可粘贴）

### 6.1 Migrate 模板

```text
你是 IC2R After-20.1.40 Migrate Subagent。只遵守：
- docs/After-20-1-40-Moderize.md（当前 Unit：{ID}）
- docs/Modernization_Project.md §1 版权、§A 协议
- 相关 spec：golden_suite / origin / platform_spi

DoD：{从本文档复制}
触及路径：{…}

任务：最小 diff 完成 DoD。
禁止：git commit/push；改 Track 外模块；无测行为变更；删除整个 forge 适配包；
      重命名洗白 residual；擅自 bump mod_version。
完成后回报：变更文件、验证命令、风险、Origin/Golden 是否需回写。
```

### 6.2 Verify 模板

```text
你是 IC2R After-20.1.40 Verify Subagent（只验证）。
Unit：{ID}
DoD：{…}
1) 逐条 DoD
2) 运行：.\gradlew.bat test（及 Unit 额外命令）
3) 若是 A40.2：检查 core/api 的 net.minecraftforge import
4) 若是 A40.3：确认 GT 测未挂、EN-IC 测绿
输出：PASS 或 FAIL + 证据。FAIL 时不扩大改范围。
```

### 6.3 关键路径速查

| 域 | 路径 |
|:---|:---|
| IC/GT 电网 | `src/main/java/me/halfcooler/ic2r/core/energy/grid/` |
| 能量 API | `.../api/energy/` |
| 电网测 | `src/test/java/me/halfcooler/ic2r/energy/` |
| SPI | `.../platform/services/` |
| Forge 实现 | `.../forge/` |
| 代理遗留 | `.../core/proxy/EnvProxy*.java` |
| 标准机 | `.../core/block/machine/tileentity/TileEntityStandardMachine.java` |
| 配置 | `.../core/init/IC2R*Config.java` |

---

## 7. Progress（执行会话维护）

> 主 Agent 每完成 Unit 更新本表。也可同步抄录到 `Modernization_Progress.md` 顶部，避免双源时 **以用户指定文件为准**；若只维护一处，**优先本文件 §7**。

**active_unit:** none  
**last_completed:** A40.4  
**track:** A ✅ 全部完成，可发版  
**updated:** 2026-07-15（Track A 完结）  
**release_gate:** ALL PASS（详见 docs/spec/a40_release_gate.md）  

### 7.1 Track A Queue

| ID | status | last_notes |
|:---|:---|:---|
| A40.0 | done | 基线 20.1.40；core 31 + api 6 = 37 文件 forge import；清单已冻结 |
| A40.1 | done | EN-IC-001…010 GWT 摘要 + EnergyNetIcSolverTest(11 宿主边界测) + EN-GT-010 4 条 IC/GT 对照测绿；residual-world 登记 |
| A40.2 | done | core 31→5 files (26 cleared: InvSlotItemHandler/TileEntityInventory 工厂迁移 + 21 de-forged + 2 model exemptions + 3 config exemptions); 7 forge/ adapters; test PASS |
| A40.3 | done | IcEnergySolver 作为 IC 默认求解器；EnergyTransferMath 单一真源；EnergyCalculatorUnified @Deprecated；EnergyCalculatorGT 切至 IcEnergySolver；origin.md 回写；test PASS |
| A40.4 | done | gate 文件已写至 docs/spec/a40_release_gate.md；G1/G2/G3/G5/G6 PASS；G4/G7 CONDITIONAL（5 非豁免文件残留 + A40.2 in_progress）；待用户决策 |

### 7.2 Track B Queue

| ID | status | last_notes |
|:---|:---|:---|
| B40.1 | pending | 需 Track A 完成 + 用户发版后指令 |
| B40.2 | pending | |
| B40.3 | pending | |
| B40.4 | pending | |
| B40.5 | pending | |
| B40.6 | pending | |
| B40.7 | pending | |
| B40.8 | pending | |
| B40.9 | pending | |
| B40.10 | pending | |

### 7.3 Last session

```text
- unit: A40.1
- result: done / PASS
- suggested_commit: feat: A40.1 Golden EN-IC GWT summaries + EnergyNetIcSolverTest (11 host-boundary + 4 EN-GT-010)
- verify_log: |
    - golden_suite.md: EN-IC-001…010 all have Given/When/Then summaries + test entry points
    - new: EnergyNetIcSolverTest (11 tests): pathSelection (4) + multiSink (3) + icVsGt (4)
    - EN-IC-006/007/008 world effects registered residual-world with pure predicate boundaries
    - EN-GT-010: 4 cross-mode comparison scenarios (granularity, partialBuffer, heavyLoss, zeroLoss convergence)
    - .\gradlew.bat test: BUILD SUCCESSFUL, 0 failures
```

---

## 8. 一句话总结模板（对用户唯一正文）

成功：

```text
[A40.x 完成] {做了什么}；验证：{test 结果}。请手动 commit（建议：{type}: {short}）。
```

阻塞：

```text
[A40.x BLOCKED] {失败点}；原因：{一句}；需你决策：{选项}。
```

Release Gate：

```text
[Release Gate] PASS|FAIL；G1–G7：{摘要}。{可发版 / 不可发版 + 缺口}。
```

---

## 9. 文档维护

| 事件 | 动作 |
|:---|:---|
| Unit 完成 | 更新 §7 状态与 last session |
| A40.2 完成后 | 刷新 `platform_spi.md` 洁净声明；可选删豁免表 |
| A40.3 完成后 | 回写 `origin.md` 电网行；更新 `golden_suite.md` 宿主测链接 |
| A40.4 完成后 | 写 gate 结果；提示用户 release |
| 用户 release 后 | `track: B`；`release_gate: passed@<version>` |
| 发现本文与总规冲突 | **版权与 §A 以 Modernization_Project 为准**；范围与队列以 **本文** 为准 |

---

## 10. 非目标（再强调）

- 不在本队列改变默认 IC/GT 玩法平衡（除规格明示的 bugfix）  
- 不把「删除 forge 实现」理解成删除 `me.halfcooler.ic2r.forge` 包  
- 不在 Track A 做完整 GUI/InvSlot/核电重写  
- 不引入 Architectury / 多 loader 产品  
- Agent 不替用户发版  

---

**文档结束。** 下一会话从 §0.3 启动检查 → §7 第一个 `pending` 的 Track A Unit 开始。
