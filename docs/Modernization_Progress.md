# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G2.6  
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
| G2.5 | done | Ic2rFluidTankHandler + fluid_handler_contract.md |
| G2.6 | done | BlockTags DataGen：`mineable/wrench`；runData+test 绿；Recipes 未扩 |
| G2.7 | pending | 阶段 1 遗留交叉（G1 已多推进，可文档关闭/对照） |
| G2.8 | pending | FE/RF 能量桥（P2） |

## Last session

- unit: G2.6
- result: done / PASS
- suggested_commit: `data: migrate mineable/wrench block tag to DataGen`
- verify_log: |
    - DoD: BlockTags 新一类 ✅；runData 写出；main 手写已删无双源
    - compile+test 绿；无 recipe 全量 DataGen
