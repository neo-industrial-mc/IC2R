# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** W3.5  
**updated:** 2026-07-14  

> 由主 Agent 在每个 Work Unit 结束后更新。用户手动 commit。  
> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。  
>  
> **§A Work Unit 队列 W0.1–W3.5 已全部 `done`。**  
> 这不等于 §6.3 / §7.7 / §8.5 全部勾满——阶段收口文档已登记 **G1.\* / G2.\* / G3.\*** 等 gap 与延期项。  
> 后续工作：按 gap 列表追加新 Unit，或用户指定方向；默认不再有本表 `pending` 项。

## Queue

| ID | status | last_notes |
|----|--------|------------|
| W0.1 | done | JUnit5 + JaCoCo 已接；SmokeTest 1/1 通过；gradlew test SUCCESS |
| W0.2 | done | docs/spec/README.md + golden_suite.md 骨架；Progress 文件就位 |
| W0.3 | done | golden_suite 条目表：EN-IC/GT + SM + RC + NS 共 44+ 可引用 ID |
| W0.4 | done | EnergyTransferMath 可测切口 + 12 energy 测试；gradlew test 13/13 绿 |
| W0.5 | done | naming_audit.md：网络/NBT camelCase 大面积 + 注册/lang 抽样 + P0–P2 修复序 |
| W0.6 | done | origin.md：核心包 residual/rewritten/original/mixed 初版 |
| W1.1 | done | SyncKey/Codec/BlockEntitySync 骨架 + BE 空钩子；反射路径保留；18 tests 绿 |
| W1.2 | done | 标准机 gui_progress/active 双写注册；NS-005 往返测；test 24/24 绿；TeUpdate 未切主 |
| W1.3 | done | ServerTicker/ClientTicker 显式接口；移除 getDeclaredMethod 探测；test 24/24 绿 |
| W1.4 | done | 清零 Vector 配方死字段 + 热路径 RandomSource.create()；create(42L)/printStackTrace 豁免登记 |
| W1.5 | done | Energy storage/energyBuffer→energy_buffer + LegacyNbt；标准机 progress；迁移测 38 tests 绿 |
| W1.6 | done | Ic2rItems 按 8 域拆分 + 门面保留；511 字段；compile+test 绿；Blocks 未拆 |
| W1.7 | done | SoundEvent 全类 DeferredRegister+RegistryObject；FmlMod bus 挂载；test 38/38 绿 |
| W1.8 | done | phase1_closeout：§6.3 对照；覆盖率 ~2.7% 记 gap；test+jacoco 绿 |
| W2.1 | done | InvSlotItemHandler 适配 + TileEntityInventory ITEM_HANDLER；14 测；test 52/52 绿 |
| W2.2 | done | FluidTransferMath fill/empty + Ic2rFluidTank 委托；8 测；test 60/60 绿 |
| W2.3 | done | macerator Type/Serializer/JSON + RecipeManagerMachineBridge + MatchMath 测；test 66/66 绿 |
| W2.4 | done | gui_modernization.md 冻结 XML；CodeGuiSample Menu/Screen 注册；compile+test 绿 |
| W2.5 | done | GatherDataEvent + ItemTagsProvider；3 tool tags → src/generated；runData+test 绿 |
| W2.6 | done | phase2_closeout：§7.7 partial/gap；窄口径~3.5%≪70%；test 66/66+jacoco 绿 |
| W3.1 | done | platform.services 8 SPI + PlatformServices；platform_spi.md；compile+test 绿 |
| W3.2 | done | EventHandler.onInitLate → PlatformLifecycle.isClient；ForgePlatformServices.install；双轨 EnvProxy |
| W3.3 | done | 删除 EnvProxy.isClientEnv；全库改 PlatformLifecycle.isClient；compile+test 绿 |
| W3.4 | done | neoforge_migration_plan.md 文档级计划；主构建仍 Forge 1.20.1 |
| W3.5 | done | phase3_closeout：§8.5 gap/deferred；common~1%≪75%；test 66/66+jacoco；队列名义收官 |

## Last session

- unit: W3.5
- result: done / PASS
- suggested_commit: `docs: phase 3 closeout and complete modernization work unit queue`
- verify_log: |
    - DoD: phase3_closeout.md 含 §8.5 四条 gap/deferred/partial + G3.1–G3.11 ✅
    - 非 Forge 最小集 deferred；覆盖率 ~1% 诚实 gap
    - test 66/66 + jacocoTestReport 绿；仅 docs 变更
    - §A 队列 W0.1–W3.5 全部 done（标准层仍有 gap）
