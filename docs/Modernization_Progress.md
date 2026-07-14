# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G2.4  
**updated:** 2026-07-14  

> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。  
> 当前专题：**G2 阶段 2 gap 迁移**。

## Queue（G2 迁移）

| ID | status | last_notes |
|----|--------|------------|
| G2.1 | done | item_handler_contract.md + InvSlot 管道式测 +5 |
| G2.2 | done | extractor/compressor 全链路 + recipe_manager_query_eval.md |
| G2.3 | done | Storage Box 全档纯代码 GUI；删 6 guidef；余 41 XML |
| G2.4 | done | Fluid/Inv/Serializer 纯函数加深；窄口径 3.53%→**6.51%** 仍≪70% gap；test 136 绿 |
| G2.5+ | pending | 见 phase2_closeout G2.5+（流体 IFluidHandler、DataGen 扩类等） |

## Last session

- unit: G2.4
- result: done / PASS
- suggested_commit: `test: deepen inv/fluid/serializer pure math for G2.4 coverage lift`
- verify_log: |
    - DoD: ≥4 新测（12）✅；窄口径 6.51% 有提升仍 gap
    - FluidTransferMath 100%；RecipeSerializerMath 100%
    - test 136/136 绿
