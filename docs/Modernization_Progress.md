# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G3.2  
**updated:** 2026-07-14  

> **G3.2** 为非 Forge 最小集 **启动（partial）**——尚无可运行 NeoForge artifact；主构建仍 Forge 1.20.1。

## Queue（G3）

| ID | status | last_notes |
|----|--------|------------|
| G3.1 | done | Dist/OnlyIn 切片；core Forge residual 减半 |
| G3.2 | done | kickoff 文档 g3_2_neoforge_min_set.md；PlatformRegistryForge；getServer→SPI；test 绿；**无** NeoForge 产品线 |
| G3.3+ | pending | 见 phase3_closeout（覆盖率、Origin、EnvProxy E2…） |

## Last session

- unit: G3.2
- result: done / PASS（partial residual）
- suggested_commit: `feat(platform): G3.2 NeoForge min-set kickoff + PlatformRegistryForge`
- verify_log: |
    - DoD: kickoff 文档 ✅；Registry SPI 真实现 ✅；getServer 迁移 ✅
    - 主构建仍 Forge；无 neoforge artifact；test 绿
