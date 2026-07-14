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
| `naming_audit.md` | **待建**（W0.5） | W0.5 | 命名扫描报告与修复优先级 |
| `origin.md` | **待建**（W0.6） | W0.6 | 模块 Origin：residual / rewritten / original |
| `id_migrations.md` | **可选**（按需） | 命名/NBT 迁移相关 Unit | 注册 ID / NBT 键 / 网络字段迁移表 |

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
| 命名审计 / Origin | **W0.5 / W0.6** |
