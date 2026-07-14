# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G2.5  
**updated:** 2026-07-14  

> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。  
> 当前专题：**G2 阶段 2 gap 迁移**。

## Queue（G2 迁移）

| ID | status | last_notes |
|----|--------|------------|
| G2.1 | done | item_handler_contract.md + InvSlot 管道式测 |
| G2.2 | done | multi-type basic recipe chain + query eval |
| G2.3 | done | Storage Box 纯代码 GUI |
| G2.4 | done | 窄口径 3.53%→6.51% 仍 gap |
| G2.5 | done | Ic2rFluidTankHandler + fluid_handler_contract.md + 7 测；多罐 cap 仍 Fluids 聚合 residual |
| G2.6 | pending | DataGen 扩类（P2） |
| G2.7–G2.8 | pending | 见 phase2_closeout（G1 遗留 / FE 桥） |

## Last session

- unit: G2.5
- result: done / PASS
- suggested_commit: `feat(fluid): Ic2rFluidTankHandler IFluidHandler adapter and contract`
- verify_log: |
    - DoD: 适配器 + 契约 + ≥4 测（7）✅
    - getFluidHandler/getTankHandler 工厂；BE 多罐未拼装改动
    - test 绿
