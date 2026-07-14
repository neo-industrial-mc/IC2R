# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G1.6  
**updated:** 2026-07-14  

> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。  
> 当前专题：**G1 阶段 1 gap 迁移**。

## Queue（G1 迁移）

| ID | status | last_notes |
|----|--------|------------|
| G1.1 | done | TeUpdate 写/读优先 BlockEntitySync；legacy 名兼容 |
| G1.2 | done | 宽口径覆盖率 3.76% 仍 ≪60% gap |
| G1.3 | done | EnergyTransferMath 扩；16 energy 测 |
| G1.4 | done | StandardMachineCycleMath + SM 测 10 条 |
| G1.5 | done | 反应堆 energy_buffer；BatchCrafter gui_progress；Electric redstone_mode |
| G1.6 | done | MachineRecipeMatchMath 加深 RC-001…005；Recycler/Bridge 回接；13 recipe 测绿 |
| G1.7 | pending | Spotless/Checkstyle（可 skip） |
| G1.8 | pending | Blocks 拆分 / hygiene（P2） |

## Last session

- unit: G1.6
- result: done / PASS
- suggested_commit: `test: deepen recipe match math for RC-001..005 and recycler gates`
- verify_log: |
    - DoD: ≥4 新 RC 测（7 新 + 既有 6 = 13）✅
    - acceptsMatchedInput / recycler reject 纯函数 + 回接
    - gradlew test 全绿
