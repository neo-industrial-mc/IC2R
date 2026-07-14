# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G1.4  
**updated:** 2026-07-14  

> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。  
> 当前专题：**G1 阶段 1 gap 迁移**。

## Queue（G1 迁移）

| ID | status | last_notes |
|----|--------|------------|
| G1.1 | done | TeUpdate 写/读优先 BlockEntitySync；legacy 名兼容；标准机 guiProgress/active |
| G1.2 | pending | 核心包覆盖率（随 G1.3/G1.4 抬升，收口再测） |
| G1.3 | done | EnergyTransferMath 扩：多汇/保护/变压器/GT offer；16 energy 测；test 79/79 绿 |
| G1.4 | done | StandardMachineCycleMath + SM-001…006 测 10 条；test 89/89 绿 |
| G1.5 | pending | snake_case 扩域（naming_audit P0 批） |
| G1.6 | pending | recipe 匹配器测例加深 |
| G1.7 | pending | Spotless/Checkstyle（可 skip） |
| G1.8 | pending | Blocks 拆分 / hygiene（P2） |

## Last session

- unit: G1.4
- result: done / PASS
- suggested_commit: `test: standard machine cycle pure math for SM-001..006`
- verify_log: |
    - DoD: ≥4 SM 测（10 用例）✅；StandardMachineCycleMath 切口 + 回接
    - 超频 0.7/1.6 未改；gradlew test 89/89 绿
