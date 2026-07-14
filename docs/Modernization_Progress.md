# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** W1.8  
**updated:** 2026-07-14  

> 由主 Agent 在每个 Work Unit 结束后更新。用户手动 commit。  
> 协议见 [Modernization_Project.md §A](Modernization_Project.md)。  
> **阶段 1 Work Unit（W1.1–W1.8）已全部 done**；§6.3 多项为 partial/gap（见 `docs/spec/phase1_closeout.md`）。下一 pending：**W2.1**。

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

- unit: W1.8
- result: done / PASS
- suggested_commit: `docs: phase 1 closeout with coverage gaps`
- verify_log: |
    - DoD: phase1_closeout.md 含 §6.3 / 覆盖率 / G1.1–G1.8 gaps ✅
    - jacoco：宽口径 ~2.7% ≪ 60%，已诚实记 gap
    - test 38/38；仅 docs 变更；无 W2 实现
