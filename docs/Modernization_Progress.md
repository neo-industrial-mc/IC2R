# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G1.1  
**updated:** 2026-07-14  

> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。  
> **§A 原队列 W0.1–W3.5 已 done。** 当前专题：**G1 阶段 1 gap 迁移**（见 [phase1_closeout.md](spec/phase1_closeout.md)）。

## Queue（§A 历史）

| ID | status | last_notes |
|----|--------|------------|
| W0.1–W3.5 | done | 阶段收口含 G1/G2/G3 gap |

## Queue（G1 迁移）

| ID | status | last_notes |
|----|--------|------------|
| G1.1 | done | TeUpdate 写/读优先 BlockEntitySync；legacy 名兼容；标准机 guiProgress/active；test 69/69 |
| G1.2 | pending | 核心包覆盖率（随 G1.3/G1.4 抬升，收口再测） |
| G1.3 | pending | EnergyNet 主体可测切口 + Golden EN 测例 |
| G1.4 | pending | 标准机加工循环半纯逻辑测例（SM-*） |
| G1.5 | pending | snake_case 扩域（naming_audit P0 批） |
| G1.6 | pending | recipe 匹配器测例加深（W2.3 已部分覆盖，收口确认） |
| G1.7 | pending | Spotless/Checkstyle（可 skip） |
| G1.8 | pending | Blocks 拆分 / hygiene（P2） |

## Last session

- unit: G1.1
- result: done / PASS
- suggested_commit: `feat(network): TeUpdate prefers BlockEntitySync for standard-machine fields`
- verify_log: |
    - DoD: writeFieldData/tryGetValue + TeUpdate.apply/trySetValue ✅
    - legacy guiProgress/active 线名兼容；未映射仍反射
    - 仅 TileEntityStandardMachine 注册 Sync；test 69/69
