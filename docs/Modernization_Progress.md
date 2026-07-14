# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G3.5  
**updated:** 2026-07-14  

## Queue（G3）

| ID | status | last_notes |
|----|--------|------------|
| G3.1 | done | Dist/OnlyIn 切片 |
| G3.2 | done | NeoForge min-set kickoff + Registry SPI |
| G3.3 | done | common-ish ~1.79% 仍 gap |
| G3.4 | done | origin 回写；§8.5 #4 仍 gap |
| G3.5 | done | E2：删除 EnvProxy isForge/isFabric/getServer；标签→getLoaderKind；test 绿 |
| G3.6+ | pending | SPI 非 stub（Network/UI/…）、E3 注册族、多模块… |

## Last session

- unit: G3.5
- result: done / PASS
- suggested_commit: `refactor: EnvProxy E2 remove isForgeEnv/isFabricEnv/getServer for SPI`
- verify_log: |
    - DoD: 三方法从 EnvProxy 删除；tags 走 LoaderKind ✅
    - getServer 已走 SPI；compile+test 绿
