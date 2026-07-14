# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G3.6  
**updated:** 2026-07-14  

## Queue（G3）

| ID | status | last_notes |
|----|--------|------------|
| G3.1–G3.5 | done | 见历史 |
| G3.6 | done | 8 SPI facet 全 Forge 真实现（去 Stub）；IHasGui/StackUtil/FluidHandler 等迁 SPI；extract 有意 EMPTY residual |
| G3.7 | pending | 物理多模块（ic2r-common / neoforge） |
| G3.8+ | pending | Architectury 延期；巨型 BE 等 |

## Last session

- unit: G3.6
- result: done / PASS
- suggested_commit: `feat(platform): implement remaining SPI facets as Forge thin adapters`
- verify_log: |
    - Network/UI/Config/Item/Fluid Forge 实现；install 无 Stub ✅
    - ≥2 调用点迁 SPI；test 绿
