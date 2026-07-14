# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** A40.1  
**updated:** 2026-07-14  

> **G3 阶段 3 gap 队列（G3.1–G3.11，G3.8=skipped）已全部收口。**  
> 多项标准仍 partial/gap（覆盖率、TeUpdate 帧、guidef residual、无第二 loader 产品、Origin 核心 residual 等）。  
>
> **下一队列（20.1.40 之后）**：见 **[After-20-1-40-Moderize.md](After-20-1-40-Moderize.md)**  
> — Track A 发版前：residual（IC 电网）+ core 去 Forge + Golden EN-IC；用户 release 后 Track B 其余现代化。  
> 执行会话请读 After 文档 §0 / §7，不要在本文件重复开 G3 队列。

## After-20.1.40 — Track A Queue (Pre-release)

| ID | status | last_notes |
|:---|:---|:---|
| A40.0 | done | 基线 20.1.40；core 31 + api 6 = 37 文件 forge import；清单已冻结 |
| A40.1 | done | EN-IC-001…010 GWT 摘要 + EnergyNetIcSolverTest(11 宿主边界测) + EN-GT-010 对照测绿 |
| A40.2 | in_progress | core 31→7 files; 22 de-forged+2 exemptions; 5 forge/ adapters created; test PASS |
| A40.3 | done | IcEnergySolver 默认 IC 求解器；EnergyTransferMath 单一真源；Unified @Deprecated；origin.md 已回写 |
| A40.4 | pending | Release Gate 预检文档 |

**release_gate:** not_run  
**基线 mod_version:** 20.1.40  
**forge import 快照:** [a40_core_forge_imports.md](spec/a40_core_forge_imports.md)

## Queue（G3）

| ID | status | last_notes |
|----|--------|------------|
| G3.1 | done | Dist/OnlyIn 切片 |
| G3.2 | done | NeoForge min-set kickoff |
| G3.3 | done | common-ish ~1.79% 仍 gap |
| G3.4 | done | origin 回写；§8.5 #4 仍 gap |
| G3.5 | done | EnvProxy E2 |
| G3.6 | done | SPI 8 facet 真实现 |
| G3.7 | done | 多模块骨架 |
| G3.8 | skipped | Architectury 有意延期 |
| G3.9 | done | CropGrowthMath + api/mixin 清单 |
| G3.10 | done | 继承 G1 交叉 |
| G3.11 | done | 继承 G2 交叉：§7.7 未勾满；G2.4 gap；无真 e2e；guidef ~41 |

## Last session

- unit: G3.11
- result: done / PASS
- suggested_commit: `docs: G3.11 cross-check inherited G2 gaps from phase 3 view`
- verify_log: |
    - phase3 §19 + phase2 §9 对照 G2.1–G2.8 ✅
    - 诚实 residual/gap；仅 docs
