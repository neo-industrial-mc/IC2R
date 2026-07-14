# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G1.5  
**updated:** 2026-07-14  

> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。  
> 当前专题：**G1 阶段 1 gap 迁移**。

## Queue（G1 迁移）

| ID | status | last_notes |
|----|--------|------------|
| G1.1 | done | TeUpdate 写/读优先 BlockEntitySync；legacy 名兼容 |
| G1.2 | done | 宽口径覆盖率 **3.76%**（317/8442）仍 ≪60% **gap**；相对 W1.8 +1.1pp；phase1_closeout §8 |
| G1.3 | done | EnergyTransferMath 扩；16 energy 测 |
| G1.4 | done | StandardMachineCycleMath + SM 测 10 条 |
| G1.5 | done | 反应堆 energy_buffer；BatchCrafter gui_progress Sync；Electric redstone_mode；测绿 |
| G1.6 | pending | recipe 匹配器测例加深 |
| G1.7 | pending | Spotless/Checkstyle（可 skip） |
| G1.8 | pending | Blocks 拆分 / hygiene（P2） |

## Last session

- unit: G1.2 + G1.5（用户指定顺序）
- result: both done / PASS
- suggested_commits: |
    1) `docs: G1.2 remeasure core coverage still under 60%`
    2) `feat(nbt/sync): G1.5 snake_case batch for reactor electric and batchcrafter`
- verify_log: |
    G1.2: jacoco 宽口径 3.76% gap；89 tests（当时）
    G1.5: energy_buffer / redstone_mode / gui_progress 迁移 + LegacyNbt/Sync alias；test 绿；非全库
