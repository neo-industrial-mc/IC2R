# Phase 2 Closeout（W2.6）

> **Work Unit**: W2.6 阶段 2 收口  
> **日期**: 2026-07-14  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §7.7、§4.5、§A  
> **性质**: 文档与证据收口；**不**开始 W3.\* 功能实现  
> **与阶段 1 关系**: [phase1_closeout.md](phase1_closeout.md) 登记了 §6.3 基础设施缺口（Sync 未切主、覆盖率 ≪60% 等）；本文件登记 **§7.7 Forge 生态对齐** 试点交付与仍 open 的 G2.\*  
> **验证命令**: `.\gradlew.bat test jacocoTestReport` → **BUILD SUCCESSFUL**（**66** tests, 0 failures）

---

## 1. §7.7 完成标准对照

| # | §7.7 标准 | 判定 | 证据摘要 | 关联 Unit |
|:---|:---|:---|:---|:---|
| 1 | 管道模组/AE2/常见自动化可稳定与机器交互（**文档化测例**） | **partial** | **W2.1**：`TileEntityInventory` 对外 `ITEM_HANDLER`（null facing → `InvSlotItemHandler` 组合视图；sided → `SidedInvWrapper`）；`InvSlotTransferMath` + `InvSlot.Access` 文档化测例 **14** 条（`InvSlotHandlerMathTest`），镜像漏斗/管道 insert-extract 门闩（输入槽不可抽、输出槽不可塞、升级 `NONE` 双向拒、stack 余量）。**W2.2**：`FluidTransferMath` fill/empty **8** 测（`FluidTransferMathTest`），`Ic2rFluidTank.fillMb/drainMb` 已委托。**诚实边界**：无真实管道/AE2 集成测；`InvSlotItemHandler`/`Ic2rFluidTank` 本体 0% 行覆盖（无 MC bootstrap）；`integration/ae2` 包 0%；流体 BE 走既有 `BlockFluidCapImpl`，非本阶段重写 | W2.1, W2.2 |
| 2 | 配方主路径走 `RecipeManager` | **partial** | **basic 全链路（G2.2 加深）**：macerator + **extractor** + **compressor** JSON（`type: ic2r:<id>`）→ `Ic2rRecipeTypes`/`Serializers` → vanilla `RecipeManager` → `RecipeManagerMachineBridge.loadBasic` → `RecipeManagerGetter` → `Recipes.*`（共用 `Rezepte#basicRecipe`）。直查 API `findMatching` 已备；评估结论见 [recipe_manager_query_eval.md](recipe_manager_query_eval.md)（**推荐暂保持 materialize**）。**未全量**：运行时匹配仍经 materialize 后的 `BasicMachineRecipeManager`，非每次 tick 直查；`RecipeManagerMachineBridge` 类本身 0%（需 Level/RM）；`MachineRecipeMatchMath` 纯逻辑+JSON 烟测；非 basic / 特殊管理器路径仍异构 | W2.3, G2.2 |
| 3 | 新 UI 零 XML | **partial** | **W2.4 冻结 + 样板 done**：`gui_modernization.md` 禁止新增 `guidef/**/*.xml`；`CodeGuiSampleMenu`/`Screen` + `CODE_GUI_SAMPLE` 注册，纯代码槽位/控件。**现有 guidef 仍在**：约 **47** 份 `assets/ic2r/guidef/*.xml` + `GuiParser`/`Dynamic*` 兼容层未删；生产机台 GUI **未** 迁移（约定明确另开 Unit） | W2.4 |
| 4 | 覆盖率达 §4.5 阶段 2 门槛（≥ **70%** inventory/fluid 适配相关） | **gap** | 见 §2。纯数学适配类近满（`InvSlotTransferMath` 97%、`FluidTransferMath` 100%、`MachineRecipeMatchMath` 100%）；**包级** invslot/fluid/recipe 仍个位数 %；窄口径适配聚合 **~3.5%** ≪ 70% | W2.1–W2.3 + 历史 |

### 1.1 勾选视图（阶段 2 名义完成度）

```text
[partial] 管道/AE2/自动化与机器交互（文档化测例）  ← ITEM_HANDLER + 纯逻辑测；无实机/AE2 e2e
[partial] 配方主路径走 RecipeManager                 ← macerator/basic 试点；非全机型直查
[partial] 新 UI 零 XML                               ← 冻结+样板；存量 guidef 仍在
[gap]     覆盖率 ≥70%（§4.5 阶段 2，inv/fluid 适配）
```

**阶段 2 结论**：Work Unit **W2.1–W2.5** 均已按 DoD 交付试点；§7.7 **未全部勾满**。  
允许以 **gap 登记** 作为 W2.6 DoD 完成（本文件），缺口进入阶段 3 专项或后续迁移 Unit，**不在本 Unit 为凑覆盖率堆无意义测试**，**不** 开始 W3.\*。

**DataGen（§7.6 / W2.5，非 §7.7 勾选项）**：`Ic2rDataGenerators` + `Ic2rItemTagsProvider`；`runData` → `src/generated/resources/data/ic2r/tags/items/{forge_hammers,wire_cutters,wrenches}.json`。Recipes/BlockState/Lang DataGen **未** 起步。

---

## 2. 覆盖率证据（JaCoCo）

| 项 | 值 |
|:---|:---|
| 命令 | `.\gradlew.bat test jacocoTestReport` |
| 结果 | BUILD SUCCESSFUL；**66** tests, 0 failures / errors |
| 报告 | `build/reports/jacoco/test/html/index.html` |
| XML | `build/reports/jacoco/test/jacocoTestReport.xml` |
| 工具 | JaCoCo 0.8.11（`build.gradle`） |

### 2.1 §4.5 阶段 2 核心包定义对照

文档定义（§4.5）：

| 阶段 | 目标 | 核心包定义 |
|:---|:---|:---|
| 阶段 2 结束 | ≥ **70%** 行覆盖率 | 阶段 1 核心 **+ inventory/fluid 适配** |

本收口采用：

| 口径 | 包集合 | 用途 |
|:---|:---|:---|
| **窄口径（阶段 2 主门槛）** | `core.block.invslot` + `core.fluid` + `core.recipe` + `core.recipe.v2` | inventory/fluid/recipe 适配相关 |
| **宽口径（延续阶段 1）** | 窄 + `energy*` + `network*` + `block.comp` + `machine.tileentity` + `block.tileentity` + `recipe.input` | 趋势对照 |
| **纯数学亮点** | `InvSlotTransferMath` + `FluidTransferMath` + `MachineRecipeMatchMath` | 试点可测切口质量 |

### 2.2 包级行覆盖率（实测）

| 包（JaCoCo name） | 行覆盖 | covered/total | 备注 |
|:---|:---|:---|:---|
| `me...core.block.invslot` | **4.93%** | 40/811 | 主要为 `InvSlotTransferMath` + `InvSlot.Access` |
| `me...core.fluid` | **5.23%** | 15/287 | 仅为 `FluidTransferMath` |
| `me...core.recipe` | **2.64%** | 20/759 | 仅为 `MachineRecipeMatchMath` |
| `me...core.recipe.v2` | **0%** | 0/270 | Bridge/Getter/Serializer 需 RM bootstrap |
| `me...core.recipe.input` | **0%** | 0/180 | 未测 |
| `me...core.block.tileentity` | **0.44%** | 4/918 | `TileEntityInventory` 能力路径未测 |
| `me...core.block.machine.tileentity` | **0.21%** | 8/3850 | 同阶段 1 |
| `me...core.network.sync` | **91.40%** | 85/93 | 阶段 1 遗产 |
| `me...core.network` | **5.51%** | 86/1560 | 同阶段 1 |
| `me...core.energy.grid` | **0.87%** | 17/1957 | 同阶段 1 |
| `me...core.energy.profile` | **20.00%** | 23/115 | 同阶段 1 |
| `me...core.gui.code` | **0%** | 0/7 | 样板无单测（编译验证） |
| `me...datagen` | **0%** | 0/17 | runData 非 jacoco 单元路径 |
| `me...forge` | **0%** | 0/1071 | capability 附着未测 |
| `me...integration.ae2` | **0%** | 0/180 | 无 AE2 交互测 |
| **窄口径聚合（阶段 2 主门槛）**\* | **~3.53%** | 75/2127 | ≪ 70% → **gap** |
| **宽口径聚合**† | **~2.61%** | 302/11560 | 仍远低于门槛 |
| 全工程 overall LINE | **0.90%** | 355/39354 | 背景，非门槛 |

\*窄 = `invslot` + `fluid` + `recipe` + `recipe.v2`。  
†宽 = 阶段 1 宽口径 + invslot + fluid + recipe + recipe.v2 + recipe.input + `block.tileentity`。

### 2.3 高价值类（阶段 2 适配相关）

| 类 | 行覆盖 | 关联测试 / 说明 |
|:---|:---|:---|
| `InvSlotTransferMath` | **97.06%** (33/34) | `InvSlotHandlerMathTest`（14 测） |
| `InvSlot.Access` | **100%** (7/7) | 同上 |
| `FluidTransferMath` | **100%** (15/15) | `FluidTransferMathTest`（8 测） |
| `MachineRecipeMatchMath` | **100%** (20/20) | `MachineRecipeMatchMathTest`（6 测） |
| **三纯数学合计** | **98.55%** (68/69) | 阶段 2 可测切口达标精神 |
| `InvSlotItemHandler` | **0%** (0/54) | 需 ItemStack/Handler；规则由 Math 镜像 |
| `Ic2rFluidTank` | **0%** (0/70) | fill/drain 委托 Math；本体未单测 |
| `RecipeManagerMachineBridge` | **0%** (0/16) | 需 `RecipeManager`；文档 + JSON 烟测路径在 MatchMath |
| `InvSlot` | **0%** (0/130) | 领域槽位主体未拆测 |
| `CodeGuiSampleMenu`/`Screen` | 0% | 样板；编译级验证 |
| `BlockEntitySync` 等 | ~阶段 1 | 见 phase1_closeout |

**结论**：阶段 2 **试点纯逻辑** 覆盖优秀；**包级 inventory/fluid 适配门槛 70% 未达**（整包 InvSlot 树、Fluid 栈、v2 bridge 拉低）。记 **gap**，后续有意义测例优先于凑行。

### 2.4 测试套件计数（相对阶段 1）

| 阶段收口 | tests | 阶段 2 新增域测 |
|:---|:---|:---|
| W1.8 | 38 | — |
| W2.6 | **66** | inv 14 + fluid 8 + recipe 6 = **+28** |

---

## 3. Gap 列表与建议后续（G2.*）

| ID | Gap | 严重度 | 建议后续（**非本 W2.6**） |
|:---|:---|:---|:---|
| G2.1 | 无真实管道/漏斗/AE2 端到端；`InvSlotItemHandler` 与 capability 附着 0% | P0 | **G2.1 已推进（文档+Math 加深）**：契约见 [item_handler_contract.md](item_handler_contract.md)；`InvSlotHandlerMathTest` 扩序列/布局/simulate（仍无 Handler 本体行覆盖）。**仍 residual**：真管道/AE2 e2e、cap 附着运行时测、可选 GameTest |
| G2.2 | 配方：仅 basic 机 materialize 主路径；运行时非直查 `RecipeManager`；非 basic 机型异构 | P0 | **G2.2 已推进**：extractor/compressor 与 macerator 共用 `Rezepte#basicRecipe`→`loadBasic` 全链路已文档化（[recipe_manager_query_eval.md](recipe_manager_query_eval.md)）；推荐保持 materialize、中期对齐 `findMatching` 语义；JSON 烟测 + `findMatchingIndex` 纯测加深。**仍 residual**：tick 未切直查；非 basic 异构 |
| G2.3 | 新 UI 零 XML 仅「新增冻结」；**~47** guidef 仍服务生产 GUI | P1 | **已推进**：storage_box 全档纯代码 Menu/Screen（`ContainerStorageBox`/`GuiStorageBox`），删 6 XML；**仍 residual**：其余 ~41 guidef + Dynamic 兼容层 |
| G2.4 | 覆盖率 inv/fluid/recipe 包级 ≪ 70%（窄口径 ~3.5%） | P0 | **勿空转**：补 `Ic2rFluidTank` 委托分支测、InvSlot accept 边界、Serializer 无 Level 切口；抬窄口径有意义行 |
| G2.5 | `Ic2rFluidTank` 非 `IFluidHandler` 一等公民；管道流体仍经 forge cap 包装 | P1 | 统一 fill/empty 服务对外；fluid handler 适配单测（对齐 FL-\*） |
| G2.6 | DataGen 仅 3 item tags；Recipes/Models/Lang 未生成 | P2 | 后续 DataGen Unit：至少 1 类 recipe 或 block tags |
| G2.7 | 阶段 1 遗留：TeUpdate 仍默认、energy.grid/标准机循环覆盖极低（G1.\*） | P0 | 不阻塞阶段 2 名义收口；阶段 3 前建议专项抬测/切主 |
| G2.8 | FE/RF 能量桥、§7.5 对外能量未在阶段 2 Work Unit 推进 | P2 | platform 适配与阶段 3 SPI 一并规划 |

### 3.1 建议测试优先级（抬阶段 2 门槛用）

1. **`Ic2rFluidTank` fill/drain 委托**：simulate/external 分支（仍可尽量少依赖 registry）。  
2. **`InvSlotItemHandler` 轻量 mock**：或继续扩 `InvSlotTransferMath` 边界 + 文档对照表（管道期望）。  
3. **Recipe JSON/type 常量与 MatchMath**：已有；补 first-match 顺序契约。  
4. **不要**：为 `gui/code` 样板、巨型 TE 构造、完整 AE2 模组加载做无断言覆盖。

---

## 4. 阶段 2 Work Unit 交付快照（W2.1–W2.5）

| ID | 状态 | 交付要点 |
|:---|:---|:---|
| W2.1 | done | `InvSlotItemHandler` + `InvSlotTransferMath`；`TileEntityInventory` 组合 cap；`EventHandlerForge` null-facing 路径；`InvSlotHandlerMathTest` 14 测 |
| W2.2 | done | `FluidTransferMath`；`Ic2rFluidTank` fill/drain 委托；`FluidTransferMathTest` 8 测 |
| W2.3 | done | macerator `RecipeType`/Serializer/JSON 族；`RecipeManagerMachineBridge`；`Rezepte.basicRecipe` 走 bridge；`MachineRecipeMatchMathTest` 6 测 |
| W2.4 | done | `gui_modernization.md` 冻结 guidef；`CodeGuiSampleMenu`/`Screen` + 注册；存量 XML 保留 |
| W2.5 | done | `GatherDataEvent` + ItemTags（3 tools）→ `src/generated/resources`；`runData` 绿 |
| W2.6 | **本文件** | §7.7 判定 + JaCoCo 证据 + G2.\* |

---

## 5. 阶段 2 明确不做 / 未越界确认

| 项 | 状态 |
|:---|:---|
| 完整拆除 InvSlot 领域 API | 未做（仅 Handler 委托） |
| 完整拆除 XML GUI / GuiParser | 未做（冻结新增 + 样板） |
| 全配方类型 RecipeManager 直查 | 未做（macerator/basic 试点） |
| platform SPI / NeoForge 骨架 | 未做（阶段 3） |
| 本 Unit 生产代码功能改动 | **无**（文档 + 验证） |
| W3.\* 实现 | **无** |
| git commit/push | **无** |

---

## 6. 相关规格与产物索引

| 文档 / 路径 | 角色 |
|:---|:---|
| [phase1_closeout.md](phase1_closeout.md) | 阶段 1 §6.3 与 G1.\*；本阶段继承未关闭项 |
| [gui_modernization.md](gui_modernization.md) | W2.4 新 UI 零 XML 约定 |
| [golden_suite.md](golden_suite.md) | RC/SM 等；W2.3 部分 RC 部分绿 |
| [origin.md](origin.md) | invslot/recipe/gui residual 标注 |
| `src/test/.../inv/InvSlotHandlerMathTest.java` | 自动化库存交互文档化测例（W2.1 + G2.1 序列/布局/simulate） |
| [item_handler_contract.md](item_handler_contract.md) | G2.1 标准机对外 ITEM_HANDLER 契约与 residual gap |
| `src/test/.../fluid/FluidTransferMathTest.java` | fill/empty 文档化测例 |
| `src/test/.../recipe/MachineRecipeMatchMathTest.java` | 配方匹配纯逻辑测例 |
| `src/main/resources/data/ic2r/recipes/macerator/**` | macerator 数据包主路径 |
| `src/main/resources/data/ic2r/recipes/extractor/**` | extractor 数据包（G2.2 第二 type 证据） |
| `src/main/resources/data/ic2r/recipes/compressor/**` | compressor 数据包（G2.2 第三 type 证据） |
| [recipe_manager_query_eval.md](recipe_manager_query_eval.md) | G2.2：tick 直查 vs materialize 评估与推荐 |
| `src/generated/resources/data/ic2r/tags/items/**` | W2.5 DataGen 产物 |
| [Modernization_Progress.md](../Modernization_Progress.md) | Work Unit 队列 |

---

## 7. 变更范围（W2.6）

- **本文件**（新建）  
- `docs/spec/README.md` 索引 + 阶段 2 进度一句  
- 可选：`golden_suite.md` 阶段 2 摘要一句  
- **无**生产代码功能改动；**无** W3.\* 实现；**无** git commit/push  
