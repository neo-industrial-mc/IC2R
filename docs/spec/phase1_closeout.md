# Phase 1 Closeout（W1.8）

> **Work Unit**: W1.8 阶段 1 收口  
> **日期**: 2026-07-14  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §6.3、§4.5、§A  
> **性质**: 文档与证据收口；**不**开始 W2.\* 功能实现  
> **验证命令**: `.\gradlew.bat test jacocoTestReport` → **BUILD SUCCESSFUL**（38 tests, 0 failures）

---

## 1. §6.3 完成标准对照

| # | §6.3 标准 | 判定 | 证据摘要 | 关联 Unit |
|:---|:---|:---|:---|:---|
| 1 | 反射网络同步**不再是默认路径** | **partial / gap** | 骨架 + 双写 + **G1.1 Sync 优先**：已注册字段经 `BlockEntitySync` get/set（legacy alias）；**TeUpdate 帧仍默认**（`getNetworkedFields()` + 字符串名）；未注册字段仍反射。见 §9 / [phase2_closeout.md](phase2_closeout.md) §8 | W1.1, W1.2, G1.1 |
| 2 | Tick 反射探测移除 | **done** | `ServerTicker` / `ClientTicker` 显式接口；`Ic2rTileEntity` 以 `instanceof` 决定是否 tick；源码中**无** `getDeclaredMethod("updateEntityServer/Client")` 探测（仅接口 Javadoc 提及历史路径） | W1.3 |
| 3 | 核心命名面字面量 snake_case 化完成（或剩余仅兼容层） | **partial** | **试点域 done**：Energy `energy_buffer` + `LegacyNbt`、标准机 `progress`、Sync wire `gui_progress`/`active`（见 `id_migrations.md`）。**全库未完成**：网络字段 camelCase、作物/反应堆/配置/组件 ID 等仍大量残留（见 `naming_audit.md`） | W1.5 + W0.5 |
| 4 | 单元测试覆盖率达 §4.5 阶段 1 门槛（核心包行覆盖率 ≥ **60%**） | **gap** | 见 §2；核心包聚合远低于 60%。`network.sync` 子包可达 91%，但 `energy.grid` / 标准机循环整体未覆盖 | W0.1, W0.4, W1.x tests |
| 5 | Spotless/Checkstyle（若启用）在 CI 通过 | **N/A** | 仓库未启用；**G1.7 done(N/A)**：书面决定本阶段不启用（§10） | G1.7 |

### 1.1 勾选视图（阶段 1 名义完成度）

```text
[partial] 反射网络同步不再是默认路径     ← 骨架+双写+G1.1 Sync 优先；TeUpdate 帧仍默认
[x]       Tick 反射探测移除
[partial] 核心命名面 snake_case          ← 试点+兼容层；全库未收口
[gap]     覆盖率 ≥60%（§4.5 阶段 1）
[N/A]     Spotless/Checkstyle
```

**阶段 1 结论**：基础设施 Work Unit（W1.1–W1.7）均已交付；§6.3 **未全部勾满**。  
允许以 **gap 登记** 作为 W1.8 DoD 完成（本文件），缺口进入阶段 2+ 或专项测试/切主 Unit，**不在本 Unit 为凑覆盖率堆无意义测试**。

---

## 2. 覆盖率证据（JaCoCo）

| 项 | 值 |
|:---|:---|
| 命令 | `.\gradlew.bat test jacocoTestReport` |
| 结果 | BUILD SUCCESSFUL；**38** tests, 0 failures / errors |
| 报告 | `build/reports/jacoco/test/html/index.html` |
| XML | `build/reports/jacoco/test/jacocoTestReport.xml` |
| 工具 | JaCoCo 0.8.11（`build.gradle`） |

### 2.1 §4.5 阶段 1 核心包定义对照

文档定义（§4.5）：

| 阶段 | 目标 | 核心包定义 |
|:---|:---|:---|
| 阶段 1 结束 | ≥ **60%** 行覆盖率 | `energy.grid` + recipe 匹配器（阶段 0）**+** machine 标准循环 **+** network codec |

### 2.2 包级行覆盖率（实测）

| 包（JaCoCo name） | 行覆盖 | covered/total | 备注 |
|:---|:---|:---|:---|
| `me...core.network.sync` | **91.40%** | 85/93 | Sync 抽象 + codec；阶段 1 亮点 |
| `me...core.energy.profile` | **20.00%** | 23/115 | `ElectricalProfile` 部分测到 |
| `me...core.network`（含 TeUpdate 等） | **5.51%** | 86/1560 | 旧反射网络主体几乎未测 |
| `me...core.energy.grid` | **0.87%** | 17/1957 | 仅 `EnergyTransferMath` 等切口 |
| `me...core.block.comp` | **0.53%** | 4/756 | 仅 Energy NBT 写路径边角 |
| `me...core.block.machine.tileentity` | **0.21%** | 8/3850 | 标准机循环本体未测 |
| `me...core.energy` | **0%** | 0/4 | 空/薄包 |
| **§4.5 宽口径聚合**\* | **~2.7%** | 223/8335 | 远低于 60% → **gap** |
| 全工程 overall LINE | **0.72%** | 280/39156 | 仅作背景，非门槛 |

\*宽口径 = `energy` + `energy.grid` + `energy.profile` + `network` + `network.sync` + `block.comp` + `block.machine.tileentity`。

### 2.3 高价值类（测试触及）

| 类 | 行覆盖 | 关联测试 |
|:---|:---|:---|
| `EnergyTransferMath` | 89.47% (17/19) | `EnergyTransferMathTest` |
| `ElectricalProfile` | 35.48% (11/31) | `ElectricalProfileMaxAmpsTest` |
| `BlockEntitySync` | 92.59% (25/27) | `StandardMachineSyncRoundTripTest` 等 |
| `SyncKey` / `SyncedField` / `SyncCodecs`（主） | ~100% | `SyncCodecRoundTripTest` |
| `LegacyNbt` | 50% (15/30) | `LegacyNbtTest` / NBT migration |
| `Energy`（组件） | 1.67% (4/240) | NBT 键迁移测触及写路径 |
| `TileEntityStandardMachine` | 6.25% (8/128) | bind sync 静态绑定测 |

**结论**：`network.sync` 已达阶段 1 精神上的 codec 门槛；`energy.grid` 与 **machine 标准循环** 包级均 **未** 达 60%。整体核心包 **gap**。

---

## 3. Gap 列表与建议后续测试

| ID | Gap | 严重度 | 建议后续（测试或实现 Unit，**非本 W1.8**） |
|:---|:---|:---|:---|
| G1.1 | 反射 TeUpdate **帧仍默认**；Sync 表曾仅标准机双写骨架 | P0 | **partial（G1.1 已推进）**：TeUpdate 读/写对已注册字段 **优先 Sync**（标准机/BatchCrafter/ElectricBlock 等）；未注册仍反射。**仍 open**：全库切主 / 去反射默认；NS-005 能量显示未扩。交叉：[phase2 §8](phase2_closeout.md) |
| G1.2 | 核心包行覆盖率 ≪ 60%（宽口径 **3.76%**；W1.8 时 ~2.7%） | P0 | **仍 gap**（G1.2 复测 §8）；**未**达 60%。优先有意义测例，非凑行：见 §3.1 / §8.5 |
| G1.3 | `energy.grid` 主体（`EnergyCalculatorUnified`/`GT`、路径、爆炸）0% | P0 | **partial（G1.3 done 切口）**：`EnergyTransferMath` 扩测 + EN-\* 部分绿；包级 **2.81%**。主体 calculator/路径/爆炸仍 ~0% |
| G1.4 | 标准机加工循环（进度、耗电、升级）无行为单测 | P0 | **partial（G1.4 done 切口）**：`StandardMachineCycleMath` + SM-001…004 绿；TE 已接线；包级 **0.98%**；升级/真实 Inv 部分仍 residual |
| G1.5 | snake_case 仅试点；全库网络/NBT/配置 camelCase | P1 | 按 `naming_audit` P0→P2 分批 + `id_migrations`；与切主同步避免双名地狱 |
| G1.6 | recipe 匹配器覆盖率未建立（阶段 0 定义的一部分） | P1 | **done（G1.6）**：`MachineRecipeMatchMath` 加深 item/tag/名单纯门闩 + `MachineRecipeMatchMathTest`（13 测）；RC-001…005 部分绿；运行时 ItemStack 匹配仍依赖 registries |
| G1.7 | Spotless/Checkstyle 未启用 | P2 | **done(N/A) / skipped**（G1.7 收口 §10）：本阶段**不**启用；理由见 §10 |
| G1.8 | Blocks 未按域拆分（W1.6 仅 Items）；import `*` / 现代 Java 风格未强制 | P2 | **done（G1.8）**：`Ic2rBlocks` 门面 + `core/ref/blocks/*` 按域拆分；对外 `Ic2rBlocks.XXX` / 注册 path 不变 |

### 3.1 建议测试优先级（抬覆盖率用，勿空转）

1. **EnergyNet 纯逻辑**：在现有 `EnergyTransferMath` 模式上拆 `loss`/packet 边界（EN-IC-001…003 草稿落地）。  
2. **标准机循环半纯逻辑**：`progress`/`energy` 在无 `Level` 下的步进（需小切口，避免启动 Forge）。  
3. **LegacyNbt / Energy 读写**：补满 `LegacyNbt` 分支，抬 `Energy` NBT 路径覆盖（NS-001…003 已绿，可加深）。  
4. **Sync 切主后**：端到端 encode 与（模拟）TeUpdate payload 对齐回归。  
5. **不要**：对 GUI、渲染、巨型 TE 构造做无断言覆盖。

---

## 4. 阶段 1 Work Unit 交付快照（W1.1–W1.7）

| ID | 状态 | 交付要点 |
|:---|:---|:---|
| W1.1 | done | `SyncKey`/`Codec`/`BlockEntitySync`；反射路径保留 |
| W1.2 | done | 标准机 `gui_progress`/`active` 双写；NS-005 往返测；TeUpdate **未**切主 |
| W1.3 | done | `ServerTicker`/`ClientTicker`；移除 tick 反射探测 |
| W1.4 | done | Vector 死字段 + 热路径 Random 清零；豁免见 `smell_exemptions_w1_4.md` |
| W1.5 | done | `energy_buffer` + LegacyNbt；标准机 progress；迁移测 |
| W1.6 | done | `Ic2rItems` 八域拆分 + 门面；Blocks 由 **G1.8** 补齐 |
| W1.7 | done | `SoundEvent` 全类 DeferredRegister + FmlMod bus |
| W1.8 | **本文件** | §6.3 判定 + JaCoCo 证据 + gap |

---

## 5. 阶段 1 明确不做（§6.2）— 确认未越界

| 项 | 状态 |
|:---|:---|
| 完整拆除 InvSlot | 未做（留给 W2.1+） |
| 完整拆除 XML GUI | 未做（留给 W2.4+） |
| 多加载器落地 | 未做（阶段 3） |

---

## 6. 相关规格与产物索引

| 文档 | 角色 |
|:---|:---|
| [golden_suite.md](golden_suite.md) | EN/SM/RC/NS 条目；阶段 1 相关 NS/部分测绿 |
| [naming_audit.md](naming_audit.md) | 命名 gap 抽样与优先级 |
| [id_migrations.md](id_migrations.md) | W1.5 键迁移表 |
| [origin.md](origin.md) | 模块 residual/rewritten 初版 |
| [smell_exemptions_w1_4.md](smell_exemptions_w1_4.md) | W1.4 豁免 |
| [Modernization_Progress.md](../Modernization_Progress.md) | Work Unit 队列 |

---

## 7. 变更范围（W1.8）

- **本文件**（新建）  
- 可选：`docs/spec/README.md` 索引、`golden_suite.md` 阶段 1 状态一句  
- **无**生产代码功能改动；**无** W2.\* 实现；**无** git commit/push  

---

## 8. G1.2 复测（G1.3 / G1.4 之后）

> **Work Unit**: G1.2 核心包覆盖率复测收口  
> **日期**: 2026-07-14  
> **前提**: G1.3（EnergyTransferMath 扩测）与 G1.4（StandardMachineCycleMath + SM 测）已 done  
> **验证命令**: `.\gradlew.bat test jacocoTestReport` → **BUILD SUCCESSFUL**（**89** tests, 0 failures）  
> **性质**: 只读复测 + 本小节登记；**未**为凑覆盖率堆无意义测试；**无**生产功能改动  

### 8.1 门槛判定（§4.5 阶段 1）

| 项 | 值 |
|:---|:---|
| 门槛 | 核心包行覆盖率 ≥ **60%** |
| 宽口径定义 | `energy` + `energy.grid` + `energy.profile` + `network` + `network.sync` + `block.comp` + `block.machine.tileentity` |
| **实测聚合** | **3.76%**（**317 / 8442** LINE） |
| **判定** | **仍 gap**（≪ 60%，G1.2 **未**勾 done） |
| 报告 | `build/reports/jacoco/test/html/index.html` |
| XML | `build/reports/jacoco/test/jacocoTestReport.xml` |

### 8.2 包级行覆盖率（复测 vs W1.8）

| 包（短名） | W1.8 行覆盖 | G1.2 复测 | covered/total（G1.2） | Δ covered |
|:---|:---|:---|:---|:---|
| `network.sync` | 91.40% (85/93) | **90.91%** | 110/121 | +25 cov / +28 total |
| `energy.profile` | 20.00% (23/115) | **20.00%** | 23/115 | 0 |
| `network` | 5.51% (86/1560) | **5.49%** | 86/1567 | 0 / +7 total |
| `energy.grid` | 0.87% (17/1957) | **2.81%** | 56/1994 | **+39**（G1.3） |
| `block.comp` | 0.53% (4/756) | **0.53%** | 4/755 | ~0 |
| `block.machine.tileentity` | 0.21% (8/3850) | **0.98%** | 38/3886 | **+30**（G1.4 `StandardMachineCycleMath`） |
| `energy` | 0% (0/4) | **0%** | 0/4 | 0 |
| **§4.5 宽口径聚合** | **~2.7%** (223/8335) | **3.76%** (317/8442) | 317/8442 | **+94** covered / +107 total |
| 全工程 overall LINE | 0.72% (280/39156) | **1.14%** (450/39565) | 450/39565 | 背景，非门槛 |

### 8.3 高价值类（复测）

| 类 | 行覆盖 | 关联 |
|:---|:---|:---|
| `EnergyTransferMath` | **91.80%** (56/61) | G1.3：`EnergyTransferMathTest` 16 条 |
| `StandardMachineCycleMath` | **87.88%** (29/33) | G1.4：`StandardMachineCycleMathTest` 10 条 |
| `BlockEntitySync` | 90.91% (50/55) | Sync 往返 + G1.1 切主相关路径 |
| `SyncKey` / `SyncedField` / `SyncCodecs` | ~100% | 既有 codec 测 |
| `ElectricalProfile` | 35.48% (11/31) | 未变 |
| `LegacyNbt` | 50% (15/30) | 不在宽口径包内，仅对照 |
| `Energy`（组件） | 1.67% (4/240) | 未变 |
| `TileEntityStandardMachine` | 6.15% (8/130) | 本体仍几乎未测；循环纯逻辑已外提 |

### 8.4 相对 W1.8 变化摘要

- **方向正确、幅度不足**：宽口径 **2.7% → 3.76%**（约 **+1.1 pp**），距 60% 仍差 **~56 pp**。  
- **增益来源（有意义测例）**：G1.3 抬 `energy.grid` 切口类；G1.4 在 `machine.tileentity` 包内新增可测纯逻辑并高覆盖，但包内巨型 TE 行数仍主导分母。  
- **结构瓶颈未变**：`energy.grid` 主体（calculator/路径/爆炸等）、`network` TeUpdate 树、`block.comp`、`TileEntity*` 海量行仍 ~0%；仅靠切口类无法单独跨过 60%。  
- **测试体量**：W1.8 **38** → 本复测 **89** tests（含阶段 2 数学测 + G1.3/G1.4）。  
- **G1.2 状态**：**仍 gap**；关闭条件仍是宽口径 ≥60% 或项目另行改口径，**禁止**无断言/GUI 堆行。

### 8.5 建议（抬覆盖率，继承 §3.1）

1. 继续 **EnergyNet 可测端口**（loss/分配/GT 求解器），把 `energy.grid` 从切口扩到行为主体。  
2. 标准机：**将** `StandardMachineCycleMath` 接到 TE 或加深 SM 边界；避免巨型 `TileEntity*` 无 Level 硬测。  
3. `LegacyNbt` / `Energy` NBT 读写加深（抬 `block.comp`）。  
4. Sync 切主后 e2e 对齐（抬 `network` 有意义路径，非反射死枝）。  
5. **不要**：为凑 60% 对 GUI、渲染、完整 TE 构造写无断言测试。

---

## 9. G2.7 交叉指针（阶段 1 遗留 vs G1 迁移后）

> **Work Unit**: G2.7（文档交叉，**非**新实现）  
> **日期**: 2026-07-14  
> **全文对照表**: [phase2_closeout.md §8](phase2_closeout.md)  

| G1.* | G2.7 后登记状态 | 一句话 |
|:---|:---|:---|
| G1.1 | **partial** | Sync **优先**已接 TeUpdate 值路径；**帧/默认协议**仍 TeUpdate |
| G1.2 | **gap** | 宽口径 **~3.76%**，**不**达 60% |
| G1.3 | **partial** | 电网纯逻辑切口 + 测；包级仍极低 |
| G1.4 | **partial** | 标准机循环纯逻辑 + 测 + 部分接线；包级仍极低 |
| G1.5 | **partial** | 命名试点/扩域；全库未收口 |
| G1.6 | **done** | recipe 匹配器纯逻辑 |
| G1.7 | **done(N/A) / skipped** | 书面决定：本阶段不启用 Spotless/Checkstyle（§10） |
| G1.8 | **done** | `Ic2rBlocks` 按域拆分（仿 W1.6 Items） |

**禁止**：将 G1.2 标为 done，或宣称阶段 1 覆盖率门槛已满足。

---

## 10. G1.7 Spotless/Checkstyle 收口（决策：不启用）

> **Work Unit**: G1.7  
> **日期**: 2026-07-14  
> **状态**: **done(N/A) / skipped**（**非**「已启用并过 CI」）  
> **性质**: 书面决策收口；**无** Spotless/Checkstyle 插件落地；**无**全库格式大改  

### 10.1 决策

**本阶段（含 G1 收尾）不在仓库启用 Spotless 或 Checkstyle**，亦不将「风格检查任务」挂入强制构建/CI。

### 10.2 理由

| 点 | 说明 |
|:---|:---|
| §6.3 已 N/A | [§1 #5](#1-63-完成标准对照)：标准写明「**若启用**」才要求 CI 通过；未启用 → 门槛 **N/A**，不构成阶段 1 阻塞 |
| 维护成本 | 对全库历史风格强制 format/checkstyle 会制造超大 diff，与现代化功能/架构 Unit 争抢 review 带宽 |
| 无 CI 强制风格工具 | 仓库当前**无** Spotless/Checkstyle 插件配置，也**无** CI job 绑定风格检查；为「勾 gap」而临时加插件无增量质量保证 |
| 优先 skip 文档 | 强行引入并 fail-build 等于本阶段范围外的 hygiene 爆炸；明确 **skip** 优于假装已启用 |

### 10.3 明确未做

- 未添加 Spotless / Checkstyle Gradle 插件  
- 未改全库缩进/import 排序/行宽  
- 未声称「风格门禁已绿」  

### 10.4 何时再开

若后续阶段需要统一风格：先轻量、**default 不 fail build** 的插件配置 + 增量/新文件策略，再单独 Work Unit；与 G1.7 本收口解耦。

### 10.5 变更范围（G1.7）

- 本文件 §3 / §9 / **§10**  
- **无**生产代码；**无** git commit/push  

---

## 11. G1.8 Ic2rBlocks 按域拆分

> **Work Unit**: G1.8  
> **日期**: 2026-07-14  
> **状态**: **done**  
> **模式**: 仿 W1.6 `Ic2rItems` — 门面保留 `public static final Block XXX = Ic2rBlocksDomain.XXX`，实现下沉 `core/ref/blocks/*`  
> **验证**: `.\gradlew.bat compileJava test`  

### 11.1 域划分

| 域类 | 内容 |
|:---|:---|
| `Ic2rBlocksResources` | 矿石/深板岩矿、原矿块、金属块、强化石、耐火砖、机器外壳、反应堆容器等 |
| `Ic2rBlocksBuilding` | 橡胶木系、栅栏/片材/玻璃/泡沫、采矿管、强化门、炸药、彩色墙等 |
| `Ic2rBlocksGenerators` | 发电/产热/动能类发电机、创造发电机 |
| `Ic2rBlocksReactor` | 核反应堆、舱室/端口、RCI |
| `Ic2rBlocksMachines` | 加工/流体/物流/个人/焦炉/工作台等机器 TE 方块 |
| `Ic2rBlocksWiring` | 线缆（含泡沫线）、储电/充电板、变压器、灯 |
| `Ic2rBlocksStorage` | 储物箱与金属储罐 |
| `Ic2rBlocksCrops` | 作物架/各类作物方块 |

### 11.2 不变量

- 对外仍 `Ic2rBlocks.XXX`；注册 path（`register("snake_case", …)`）不变  
- `Ic2rBlocks.register` 供域类调用（与 Items 一致）  
- **未**做 Deferred 全迁移；**未**改玩法数值  

### 11.3 变更范围（G1.8）

- `Ic2rBlocks.java`（门面化）  
- `core/ref/blocks/Ic2rBlocks*.java`（新建域实现）  
- 本文件本节  

---

## 12. G3.10 交叉指针（阶段 3 视角 vs G1 债）

> **Work Unit**: G3.10（文档交叉，**非**新实现）  
> **日期**: 2026-07-14  
> **全文对照表**: [phase3_closeout.md §18](phase3_closeout.md)  
> **前序**: 本文件 §9（G2.7 指针）；[phase2_closeout.md §8](phase2_closeout.md)  

| G1.* | G3.10 后登记状态 | 一句话 |
|:---|:---|:---|
| G1.1 | **partial** | Sync **优先**已接 TeUpdate 值路径；**帧/默认协议**仍 TeUpdate |
| G1.2 | **gap** | 宽口径 ~3.76%（G1.2）/ ~4.47%（G3.3 宽）**不**达 60% |
| G1.3 | **partial** | 电网纯逻辑切口 + 测；包级仍极低 |
| G1.4 | **partial** | 标准机循环纯逻辑 + 测 + 接线；包级仍极低 |
| G1.5 | **partial** | 命名试点 + 扩域；全库未收口 |
| G1.6 | **done** | recipe 匹配器纯逻辑 |
| G1.7 | **done(N/A) / skipped** | 本阶段不启用 Spotless/Checkstyle（§10） |
| G1.8 | **done** | `Ic2rBlocks` 按域拆分 |

**禁止**：将 G1.2 标为 done、宣称阶段 1 覆盖率门槛已满足，或宣称 **G1 全清**。  
 
