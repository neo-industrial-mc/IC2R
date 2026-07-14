# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** W0.5  
**updated:** 2026-07-14  

> 由主 Agent 在每个 Work Unit 结束后更新。用户手动 commit。  
> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。

## Queue

| ID | status | last_notes |
|----|--------|------------|
| W0.1 | done | JUnit5 + JaCoCo 已接；SmokeTest 1/1 通过；gradlew test SUCCESS |
| W0.2 | done | docs/spec/README.md + golden_suite.md 骨架；Progress 文件就位 |
| W0.3 | done | golden_suite 条目表：EN-IC/GT + SM + RC + NS 共 44+ 可引用 ID |
| W0.4 | done | EnergyTransferMath 可测切口 + 12 energy 测试；gradlew test 13/13 绿 |
| W0.5 | done | naming_audit.md：网络/NBT camelCase 大面积 + 注册/lang 抽样 + P0–P2 修复序 |
| W0.6 | pending | Origin 初版 |
| W1.1 | pending | Sync 抽象骨架 |
| W1.2 | pending | 标准机同步试点 |
| W1.3 | pending | 去反射 Tick |
| W1.4 | pending | 清理致命味道 |
| W1.5 | pending | NBT/网络字面量 snake_case 试点 |
| W1.6 | pending | 注册拆分 |
| W1.7 | pending | Deferred/Holder 试点 |
| W1.8 | pending | 阶段 1 收口 |
| W2.1 | pending | InvSlot → Handler 委托试点 |
| W2.2 | pending | 流体适配收窄试点 |
| W2.3 | pending | 配方 RecipeManager 试点 |
| W2.4 | pending | 冻结 XML / 代码 GUI 样板 |
| W2.5 | pending | DataGen 起步 |
| W2.6 | pending | 阶段 2 收口 |
| W3.1 | pending | platform SPI 草案 |
| W3.2 | pending | 迁移 1 个调用点到 SPI |
| W3.3 | pending | EnvProxy 瘦身切片 |
| W3.4 | pending | NeoForge 骨架或计划 |
| W3.5 | pending | 阶段 3 收口 |

## Last session

- unit: W0.5
- result: done / PASS
- suggested_commit: `docs: add naming audit report for snake_case literals`
- verify_log: |
    - DoD: naming_audit.md 非空含路径/分类/P0-P2 ✅
    - 抽查 guiProgress / fluidReactorLookup / energyBuffer 源码命中 ✅
    - git 仅 docs；无业务重命名
