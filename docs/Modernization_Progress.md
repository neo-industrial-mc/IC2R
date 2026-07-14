# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** W2.6  
**updated:** 2026-07-14  

> 由主 Agent 在每个 Work Unit 结束后更新。用户手动 commit。  
> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。  
> **阶段 2 Work Unit（W2.1–W2.6）已全部 done**；§7.7 为 partial/gap（见 `docs/spec/phase2_closeout.md`）。下一 pending：**W3.1**。

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
| W3.1 | pending | platform SPI 草案 |
| W3.2 | pending | 迁移 1 个调用点到 SPI |
| W3.3 | pending | EnvProxy 瘦身切片 |
| W3.4 | pending | NeoForge 骨架或计划 |
| W3.5 | pending | 阶段 3 收口 |

## Last session

- unit: W2.6
- result: done / PASS
- suggested_commit: `docs: phase 2 closeout with §7.7 gaps and jacoco evidence`
- verify_log: |
    - DoD: phase2_closeout.md 含 §7.7 逐条 + G2.1–G2.8 ✅
    - 3× partial + 覆盖率 gap（窄口径 ~3.53% ≪ 70%）诚实登记
    - test 66/66 + jacocoTestReport 绿；仅 docs 变更；无 W3 实现
