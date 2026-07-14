# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G2.2  
**updated:** 2026-07-14  

> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。  
> 当前专题：**G2 阶段 2 gap 迁移**。

## Queue（G2 迁移）

| ID | status | last_notes |
|----|--------|------------|
| G2.1 | done | item_handler_contract.md + InvSlot 管道式测 +5；无 AE2 e2e residual |
| G2.2 | done | extractor/compressor 与 macerator 同 bridge 证据；recipe_manager_query_eval.md；JSON 烟测+findMatching；test 124 绿 |
| G2.3 | pending | guidef 迁移简单机 |
| G2.4 | pending | inv/fluid 覆盖率有意义抬升 |
| G2.5+ | pending | 见 phase2_closeout G2.* |

## Last session

- unit: G2.2
- result: done / PASS
- suggested_commit: `docs+test: G2.2 multi-type basic recipe chain and RM query eval`
- verify_log: |
    - DoD: extractor/compressor 全链路证据 ✅；materialize 推荐文档 ✅
    - ≥2 新测（实际 4）✅；test 124/124
    - 未改非 basic 机型；未切 tick 直查
