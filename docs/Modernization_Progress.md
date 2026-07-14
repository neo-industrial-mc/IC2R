# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G2.3  
**updated:** 2026-07-14  

> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。  
> 当前专题：**G2 阶段 2 gap 迁移**。

## Queue（G2 迁移）

| ID | status | last_notes |
|----|--------|------------|
| G2.1 | done | item_handler_contract.md + InvSlot 管道式测 +5 |
| G2.2 | done | extractor/compressor 全链路 + recipe_manager_query_eval.md |
| G2.3 | done | Storage Box 全档纯代码 GUI；删 6 guidef；余 41 XML；compile+test 绿 |
| G2.4 | pending | inv/fluid 覆盖率有意义抬升 |
| G2.5+ | pending | 见 phase2_closeout G2.* |

## Last session

- unit: G2.3
- result: done / PASS
- suggested_commit: `feat(gui): migrate storage boxes from guidef XML to code Menu/Screen`
- verify_log: |
    - DoD: ContainerStorageBox/GuiStorageBox；TE 非 DynamicContainer ✅
    - storage* guidef 已删；MenuType+Screen 注册 ✅
    - 其余 ~41 guidef 保留；compileJava+test 绿
