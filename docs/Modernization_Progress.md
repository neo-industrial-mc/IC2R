# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G3.9  
**updated:** 2026-07-14  

## Queue（G3）

| ID | status | last_notes |
|----|--------|------------|
| G3.1–G3.7 | done | 见历史 |
| G3.8 | skipped | Architectury **有意延期**（g3_8_architectury_decision.md）；非缺陷 |
| G3.9 | done | Mixin 仅 RecipeManagerMixin；api_surface.md；CropGrowthMath 切片 + 10 测；partial 未拆全 BE |
| G3.10–G3.11 | pending | 继承 G1/G2 交叉登记（多为已部分推进的债） |

## Last session

- units: G3.8 (view+skip) → G3.9
- result: PASS
- suggested_commits: |
    1) `docs: G3.8 defer Architectury by design`
    2) `feat: CropGrowthMath extract + api/mixin inventory for G3.9`
- verify_log: |
    - G3.8: 无 Architectury 依赖 ✅
    - G3.9: CropGrowthMath 回接；test 绿；巨型 BE 未全拆
