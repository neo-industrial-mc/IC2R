# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G2.1  
**updated:** 2026-07-14  

> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。  
> 当前专题：**G2 阶段 2 gap 迁移**。

## Queue（G1 迁移）

| ID | status | last_notes |
|----|--------|------------|
| G1.1–G1.6 | done | 见历史；G1.7/G1.8 仍 pending（P2） |

## Queue（G2 迁移）

| ID | status | last_notes |
|----|--------|------------|
| G2.1 | done | item_handler_contract.md + InvSlot 管道式测 +5；无 AE2 e2e residual；test 120/120 |
| G2.2 | pending | 配方更多全链路 / 直查评估 |
| G2.3 | pending | guidef 迁移简单机 |
| G2.4 | pending | inv/fluid 覆盖率有意义抬升 |
| G2.5+ | pending | 见 phase2_closeout G2.* |

## Last session

- unit: G2.1
- result: done / PASS
- suggested_commit: `test(docs): G2.1 item handler automation contract and pipeline tests`
- verify_log: |
    - DoD: 契约文档 ✅；≥4 新测（5）✅；AE2 residual 诚实
    - InvSlotHandlerMathTest 19/19；全量 test 绿
    - 未测 InvSlotItemHandler 本体（无 MC bootstrap）
