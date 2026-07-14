# IC2R 行为规格目录（`docs/spec/`）

> 本目录存放**现代化工程的规格交付物**：行为黄金用例、命名审计、模块 Origin、可选 ID 迁移表等。  
> 实现代码以本目录规格为先（见主文档 §1「规格先于代码」），测试与重写应对齐此处，而非对着反编译边抄边改。

---

## 用途

| 类别 | 说明 |
|:---|:---|
| **行为规格 / Golden Suite** | 冻结可回归的玩法不变量与用例大纲（电网、标准机、配方、NBT/同步等） |
| **命名审计** | 非 `snake_case` 字面量、网络字段、NBT 键等抽样与修复优先级 |
| **Origin** | 核心包/模块标注 residual / rewritten / original，支撑版权审计 |
| **索引与约定** | 本 README 作为规格集入口；详细条目随 Work Unit 逐步填入 |

本目录**证明实现来自规格而非抄写**（见 [Modernization_Project.md](../Modernization_Project.md) §1.5）。

---

## 与 `Modernization_Project.md` 的关系

| 主文档章节 | 本目录对应 |
|:---|:---|
| §A Work Unit **W0.2–W0.6** | 目录建立、Golden 大纲、命名审计、Origin 初版 |
| §1 版权策略 / 干净室工作流 | 规格与测试先于重写 |
| §4.4 Golden Suite | `golden_suite.md` 的章节与模块划分 |
| §5 阶段 0 护栏 | 规格基线产出物 |
| 后续阶段测试 / 迁移 | 用例细化、`id_migrations` 等按需追加 |

主文档保留**索引级**说明；**可执行的规格正文**放在本目录，避免主文档无限膨胀。

关联文档（不在本目录，但规格编写时常引用）：

- [Modernization_Project.md](../Modernization_Project.md) — 总规与协议  
- [Modernization_Progress.md](../Modernization_Progress.md) — Work Unit 进度  
- [GTEU_Migration_Project.md](../GTEU_Migration_Project.md) — GT 电网不变量（Golden GT 章节应对齐）  
- [GTEU_GT_Reference.md](../GTEU_GT_Reference.md) — GT 参考  

---

## 文件清单约定

下列文件为本目录约定产出；**未列出的文件默认不应随意新增**，确有需要时在 PR/Progress 中说明。

| 文件 | 状态约定 | 负责 Work Unit | 内容概要 |
|:---|:---|:---|:---|
| [README.md](README.md) | **本文件**（W0.2） | W0.2 | 目录用途、与主文档关系、文件约定 |
| [golden_suite.md](golden_suite.md) | 条目表大纲（W0.3）→ 用例正文（后续） | W0.2 / W0.3+ | 行为黄金用例大纲与条目 ID 表 |
| [naming_audit.md](naming_audit.md) | **已产出**（W0.5）：抽样 + 优先级 | W0.5 | 命名扫描报告与修复优先级 |
| [origin.md](origin.md) | **已产出**（W0.6）→ **G3.4 回写** | W0.6 / G3.4 | 模块 Origin：residual / rewritten / original / mixed；§8.5 #4 核心 residual **未**清零 |
| [id_migrations.md](id_migrations.md) | **已起步**（W1.5 试点） | 命名/NBT 迁移相关 Unit | 注册 ID / NBT 键 / 网络字段迁移表 |
| [phase1_closeout.md](phase1_closeout.md) | **已产出**（W1.8） | W1.8 | 阶段 1 §6.3 勾选、JaCoCo 覆盖率证据与 gap 列表 |
| [phase2_closeout.md](phase2_closeout.md) | **已产出**（W2.6） | W2.6 | 阶段 2 §7.7 勾选、inv/fluid/recipe 覆盖率证据与 G2.\* gap 列表 |
| [item_handler_contract.md](item_handler_contract.md) | **已产出**（G2.1） | G2.1 | 标准机/库存 BE 对外 `ITEM_HANDLER`：null vs facing、Access 门控、Macerator 布局；Math 测例与 residual gap |
| [fluid_handler_contract.md](fluid_handler_contract.md) | **已产出**（G2.5） | G2.5 | 流体 BE/Tank 对外 `FLUID_HANDLER`：单罐 `Ic2rFluidTankHandler`、多罐 side、fill/empty 路径表；Math 测例与 residual gap |
| [energy_bridge_contract.md](energy_bridge_contract.md) | **已产出**（G2.8） | G2.8 | FE/RF 对外能量桥：EU 权威、默认 2.0 FE/EU、`PlatformEnergyBridgeForge`、与 AE2 对齐；Math 测例 |
| [recipe_manager_query_eval.md](recipe_manager_query_eval.md) | **已产出**（G2.2） | G2.2 | basic 多 type 全链路证据；tick 直查 RecipeManager vs 缓存 materialize 利弊与推荐 |
| [phase3_closeout.md](phase3_closeout.md) | **已产出**（W3.5） | W3.5 | 阶段 3 §8.5 勾选（gap/deferred）、common 覆盖率证据与 G3.\* gap 列表 |
| [smell_exemptions_w1_4.md](smell_exemptions_w1_4.md) | **已产出**（W1.4） | W1.4 | 味道清理豁免登记 |
| [gui_modernization.md](gui_modernization.md) | **已产出**（W2.4） | W2.4 | 冻结 guidef XML；新 UI 代码 Menu/Screen 约定与样板 |
| （入口说明见本表下） | **已起步**（W2.5） | W2.5 | DataGen：`Ic2rDataGenerators` + Tags provider；`.\gradlew.bat runData` → `src/generated/resources` |
| [platform_spi.md](platform_spi.md) | **已起步**（W3.1–W3.3） | W3.1+ | Platform SPI 接口、依赖方向、EnvProxy 映射与瘦身记录 |
| [neoforge_migration_plan.md](neoforge_migration_plan.md) | **已产出**（W3.4） | W3.4 | NeoForge 文档级迁移计划：版本线选项、模块/SPI、退役序、里程碑与风险 |
| [g3_2_neoforge_min_set.md](g3_2_neoforge_min_set.md) | **已起步**（G3.2 partial） | G3.2 | 非 Forge 最小可运行集 kickoff：目标清单、版本 A/B、SPI 就绪度、阻断项；Registry 真实现 + getServer 切片 |
| [g3_7_module_split.md](g3_7_module_split.md) | **已起步**（G3.7 partial/skeleton） | G3.7 | 物理多模块：目标 common/forge/neoforge(+fabric)、源码映射、SPI 方向、不默认 include 原因与前置；`modules/` 骨架 |

### 编写约定（摘要）

1. **先大纲后正文**：Golden 先章节与条目 ID，再补断言与测例链接。  
2. **可测优先**：每条规格应能映射到测试名或 `@Spec("…")`（见主文档 §4.7）。  
3. **IC2R 差异须标注**：与原版 IC2 有意不同的行为（如 GT 模式、充电座等）在条目中标明来源为 IC2R。  
4. **最小 diff**：各 Work Unit 只填自己负责的文件/章节，不抢做后续 Unit 的完整正文。  

---

## 当前进度（规格侧）

| 产出 | 状态 |
|:---|:---|
| 目录 + README | 已完成（W0.2） |
| Golden 大纲骨架 | 已完成（W0.2）：见 `golden_suite.md` |
| Golden 条目表 | **已完成（W0.3）**：EN-IC/GT、SM、RC、NS 可引用 ID；正文后续 |
| 命名审计 | **已完成（W0.5）**：见 `naming_audit.md` |
| Origin | **已完成（W0.6）**：见 `origin.md`（初版；随 W1+ 更新） |
| 阶段 1 收口 | **已完成（W1.8）**：见 `phase1_closeout.md`（§6.3 partial/gap 已登记；覆盖率未达 60%） |
| 阶段 2 收口 | **已完成（W2.6）**：见 `phase2_closeout.md`（§7.7 均为 partial/gap；W2.1–W2.5 试点已交付；窄口径 inv/fluid/recipe 覆盖 ~3.5% ≪70%） |
| 阶段 3 收口 | **已完成（W3.5）**：见 `phase3_closeout.md`（§8.5：common loader import **gap**、非 Forge 最小集 **deferred**、覆盖率 ~1% **gap**、Origin residual **partial/gap**；W3.1–W3.4 试点/计划已交付） |

阶段 1 收基础设施与命名/Sync 试点缺口（G1.\*）；阶段 2 在其上叠加 Forge 生态适配试点（Handler/流体/RecipeManager/GUI 冻结/DataGen），缺口见 G2.\*；阶段 3 叠加 platform SPI / EnvProxy 切片 / NeoForge 文档计划，缺口见 G3.\*。§A 既定 Work Unit 队列（W0.1–W3.5）名义已全部交付；后续走 G\* 与新追加 Unit，而非假装 §8.5 勾满。
