# Golden Suite — 行为黄金用例规格

> **状态**：条目表大纲（W0.3）  
> **下一步**：W0.4+ 补用例正文（Given/When/Then）、可测切入点与测试链接；实现侧首批 EnergyNet 测试。  
> **依据**：[Modernization_Project.md §4.4](../Modernization_Project.md)  
> **索引**：[docs/spec/README.md](README.md)  
> **阶段 1 摘要（W1.8）**：NS-001…003/005 与部分 energy 切口已绿；EN/SM/RC 主体仍 draft；覆盖率与 TeUpdate 切主见 [phase1_closeout.md](phase1_closeout.md)。  
> **阶段 2 摘要（W2.6）**：inv/fluid 适配纯逻辑测 + macerator RecipeManager 试点 + GUI 冻结样板 + Tags DataGen；§7.7 partial/gap 见 [phase2_closeout.md](phase2_closeout.md)。  
> **G2.2**：extractor/compressor 与 macerator 共用 bridge 全链路文档 + 直查 vs materialize 评估（[recipe_manager_query_eval.md](recipe_manager_query_eval.md)）；RC-006 JSON 烟测加深。  
> **阶段 3 摘要（W3.5）**：platform SPI + lifecycle 首迁 + EnvProxy 切片 + NeoForge 文档计划；§8.5 多为 gap/deferred、无新 Golden 绿项；见 [phase3_closeout.md](phase3_closeout.md)。

本文件冻结「必须先规格与测试、再动实现」的模块与用例大纲。条目 ID 建议格式：`{域}-{子域}-{序号}`（例：`EN-IC-001`），测试可用 `@Spec("EN-IC-001")` 对齐（§4.7）。

---

## 0. 元信息

| 项 | 值 |
|:---|:---|
| 规格版本 | 0.3-a40.1 |
| 冻结目标 | 各域条目 ID 稳定可引用；EN-IC-001…010 均具备 GWT 摘要与测入口 |
| 覆盖范围 | EnergyNet IC/GT、标准机、配方、NBT/同步；§4.4.6 高风险域预留 |
| 非目标（本文件） | 完整用例正文、实现代码、完整命名审计 |

### 条目表模板（各节复用）

| ID | 标题 | 不变量 / 期望摘要 | 优先级 | 测试状态 | 备注 |
|:---|:---|:---|:---|:---|:---|
| `{域}-{序号}` | 短标题 | 一句话不变量 | P0/P1/P2 | 未写 / 红 / 绿 | 可选：引用文档、废弃说明 |

### 测试状态标记约定

| 标记 | 含义 |
|:---|:---|
| 测绿 | 至少纯逻辑测通过；world 效果可能未覆盖 |
| 测绿（纯谓词） | 仅纯函数/谓词测试通过；世界效果/方块操作标 `residual-world` |
| 测绿（宿主） | 含 solver-level / 拓扑级测试 |
| residual-world | 该条目 world 效果（方块移除/爆炸/实体伤害）因需 Level 而暂未自动化，以纯谓词边界约束 |

---

## 1. EnergyNet（IC 模式）

**规格域前缀**：`EN-IC-*`  
**实现对照提示**：现行 IC 路径 / `EnergyCalculatorUnified` 等（以代码检索为准，不在此抄实现）

### 1.1 主题清单

| 主题 | 规格要点（摘要，非完整用例） | 条目表 |
|:---|:---|:---|
| 单源单汇 | 路径选择；线损后 EU 到达量 | EN-IC-001 … 003 |
| 多汇分配 | 与现行统一计算器策略对齐的规格描述 | EN-IC-004 … 005 |
| 绝缘 / 导体 | 击穿、熔断、过压爆炸条件 | EN-IC-006 … 008 |
| 变压器 | packet 倍率（升压 / 降压） | EN-IC-009 … 010 |

### 1.2 条目表

| ID | 标题 | Given / When / Then（精简摘要） | 优先级 | 测试状态 | 测入口 |
|:---|:---|:---|:---|:---|:---|
| EN-IC-001 | 单源单汇基本传输 | **Given** 唯一源提供 offer EU，经唯一路径（累计损耗 loss）连接唯一汇；**When** 线损后 EU 到达汇侧；**Then** 到达量 = offer − loss（≥0） | P0 | 测绿 | 纯逻辑：`EnergyTransferMathTest.icInjectAmount_*` |
| EN-IC-002 | 最短/优选路径选择 | **Given** 源→汇存在多条并联路径（各路径损耗不同）；**When** BFS 探索所有候选路径并按 `icPreferNewPath` 比较；**Then** 严格更小损耗的路径替换当前最优；全拓扑 BFS 最终收敛到全局最小损耗路径 | P0 | 测绿（宿主） | 纯逻辑：`icPreferNewPath_*`；宿主：`EnergyNetIcSolverTest.pathSelection_*` |
| EN-IC-003 | 线损后到达量边界 | **Given** 源提供 offer EU，路径累计损耗 loss；**When** loss ≥ offer 或 offer 被 loss 耗尽；**Then** 到达量 = 0，绝不出现负 EU 进入汇 | P0 | 测绿 | 纯逻辑：`icInjectAmount_lossExceeds*` |
| EN-IC-004 | 多汇可分配性 | **Given** 单源提供 offer EU，N 个汇各有路径损耗 loss[i] 与需求 demand[i]；**When** 按固定顺序依次分配；**Then** Σ delivered[i] ≤ offer（各步源消耗 = delivered[i] + loss[i]） | P0 | 测绿（宿主） | 纯逻辑：`icDistributeSequential_total*`；宿主：`multiSink_heterogeneousLosses_*` |
| EN-IC-005 | 多汇分配策略 | **Given** offer 不足以同时满足所有汇；**When** 按固定路径顺序填满（前序路径按需取，后序吃 residual）；**Then** 前序先满足、后序可能被饿死（非均分） | P0 | 测绿（宿主） | 纯逻辑：`icDistributeSequential_earlier*`；宿主：`multiSink_earlierPriority_*` |
| EN-IC-006 | 绝缘击穿 | **Given** 传输 packet 能量为 amount；**When** amount > insulation 且 amount ≤ conductor（未达熔断）；**Then** 绝缘 strip，电缆降级；**world 效果 residual-world**：方块状态变更需 Level 未自动化 | P0 | 测绿（纯谓词） | 纯谓词：`icInsulationBreakdown_*`；world 效果标记 `residual-world` |
| EN-IC-007 | 导体熔断/过载 | **Given** 传输 packet 能量 amount > conductorBreakdownEnergy（= capacity + 1）；**When** 触发 cable meltdown 判定；**Then** 导体移除/方块销毁；**world 效果 residual-world**：方块移除需 Level 未自动化 | P0 | 测绿（纯谓词） | 纯谓词：`icConductorBreakdown_*`；world 效果标记 `residual-world` |
| EN-IC-008 | 过压对设备 | **Given** packet 电压 > sink tier 额定最大功率；**When** `icSinkOverVoltage` 判定；**Then** 设备标记超压（触发爆炸路径） | P1 | 测绿（纯谓词） | 纯谓词：`icSinkOverVoltage_*`；world 爆炸效果未自动化 |
| EN-IC-009 | 变压器升压 packet | **Given** 低压侧 4× 输入 packet；**When** 升压模式运行；**Then** 输出 1× 高压 packet；`lowVoltage × 4 == highVoltage × 1`（能量守恒，忽略传输损耗） | P0 | 测绿 | 纯逻辑：`icTransformer_stepUp_*` |
| EN-IC-010 | 变压器降压 packet | **Given** 高压侧 1× 输入 packet；**When** 降压模式运行；**Then** 输出 4× 低压 packet；`highVoltage × 1 == lowVoltage × 4`（能量守恒） | P0 | 测绿 | 纯逻辑：`icTransformer_stepDown_*` |

### 1.3 测入口汇总

| 测试类 | 覆盖条目 | 类型 | 说明 |
|:---|:---|:---|:---|
| `EnergyTransferMathTest` | EN-IC-001…010, EN-GT-001/002/006/007/009 | 纯逻辑（JUnit） | 所有纯函数/谓词，不依赖 Level |
| `EnergyNetIcSolverTest` | EN-IC-002, EN-IC-004/005, EN-GT-010 | 宿主边界（JUnit） | 多路径拓扑选择、异构分配、IC/GT 对照；仅用 fake 数据 + Math |
| `ElectricalProfileMaxAmpsTest` | EN-GT-003/004/005 | 纯逻辑（JUnit） | GT maxAmps 公式 |

### 1.4 residual-world 登记

| 条目 | 不可测 world 效果 | 纯谓词边界 |
|:---|:---|:---|
| EN-IC-006 | 绝缘 strip 后方块状态变更 | `icInsulationBreakdown(amount, insulationLimit, conductorLimit)` |
| EN-IC-007 | 导体熔断后方块移除 | `icConductorBreakdown(amount, conductorBreakdownEnergy)` |
| EN-IC-008 | 超压爆炸/实体伤害 | `icSinkOverVoltage(packetVoltage, sinkMaxVoltage)` |
| EN-IC-002 | 全 BFS 图遍历拓扑选择（需 Grid/Node/Level） | `icPreferNewPath(prevLoss, candidateLoss)` + 多候选路径纯数据模拟 |

---

## 2. EnergyNet（GT 模式）

**规格域前缀**：`EN-GT-*`  
**外部依据**：[GTEU_Migration_Project.md](../GTEU_Migration_Project.md) invariants；[GTEU_GT_Reference.md](../GTEU_GT_Reference.md)

### 2.1 主题清单

| 主题 | 规格要点（摘要，非完整用例） | 条目表 |
|:---|:---|:---|
| 1A 包语义 | 1A 包不可拆分；空闲 1A 请求 | EN-GT-001 … 003 |
| maxAmps | 公式与边界 | EN-GT-004 … 005 |
| 保护 | 超压 / 超流熔断 | EN-GT-006 … 007 |
| 方向 | 方向优先级 | EN-GT-008 |
| 与 IC 模式隔离 | 双 Calculator / 模式切换不串味（若适用） | EN-GT-009 … 010 |

### 2.2 条目表

| ID | 标题 | 不变量 / 期望摘要 | 优先级 | 测试状态 | 备注 |
|:---|:---|:---|:---|:---|:---|
| EN-GT-001 | 1A 包不可拆分 | 1A 在给定电压等级携带固定 EU（线损前）；不得拆成多份小数安注入多个汇 | P0 | 测绿 | `gtDeliverableAmps_*` |
| EN-GT-002 | 线损后整包注入 | 线损只减少包内 EU；仍按整安交付，不可把损耗「拆安」 | P0 | 测绿 | `gtPacketEuAfterPathLoss_*` |
| EN-GT-003 | 空闲仅请求 1A | 机器未处理配方（空闲）时，缓冲有空位最多请求 1A 维持 | P0 | 测绿 | `ElectricalProfileMaxAmpsTest` idle |
| EN-GT-004 | maxAmps 公式 | 工作时 `maxAmps = ⌊2 × recipeEU/t / tierVoltage⌋ + 1`（与 GTEU 文档一致） | P0 | 测绿 | `ElectricalProfileMaxAmpsTest` |
| EN-GT-005 | maxAmps 下界 | 极低 EU/t 配方仍至少可请求 1A（公式结果 ≥1） | P1 | 测绿 | `ElectricalProfileMaxAmpsTest` |
| EN-GT-006 | 导线超压熔断 | 包电压 > 导线 maxVoltage → 熔断（删方块或等价），与 IC 模式「仅 EU 包 capacity」语义区分 | P0 | 测绿（纯谓词） | `gtCableOverVoltage_*` |
| EN-GT-007 | 导线超流熔断 | 路径安培超过导线 maxAmps → 熔断 | P0 | 测绿（纯谓词） | `gtCableOverCurrent_*` |
| EN-GT-008 | 方向优先级推送 | 源侧按方向优先级（目标：D-U-N-S-W-E；可分阶段用 BFS 近似，但须文档化）分配整安，非「最近机器优先」 | P1 | 未写 | PR-4 可近似；见 GTEU 参考 |
| EN-GT-009 | 源满 1A 才输出 | GT 模式发电机/储电输出：内部攒满 1A 才 `offer`；与 IC「可吐全部 storage」隔离 | P0 | 测绿 | `gtOfferAmps_partialBufferBelowOneAmp_*` |
| EN-GT-010 | IC/GT Calculator 不串味 | **Given** 同一源缓冲（EU）与同一拓扑参数（电压/损耗/需求）；**When** 分别经 IC Math（连续 EU）与 GT Math（离散 1A packet）计算；**Then** 结果各自符合模式 invariants：IC 以 `offer − loss` 连续交付、GT 以 `floor(buffer/V)` 整安交付、loss 语义不同（IC 一次扣减 / GT 逐安扣减）→ 输出数值差异正确且无跨模式状态泄漏 | P0 | 测绿 | `EnergyNetIcSolverTest.icVsGt_*` (4 条对照场景) |

---

## 3. 标准处理机

**规格域前缀**：`SM-*`（Standard Machine）

### 3.1 主题清单

| 主题 | 规格要点（摘要，非完整用例） | 条目表 |
|:---|:---|:---|
| 进度与耗电 | 进度推进、EU 消耗、中断 | SM-001 … 003 |
| 阻塞 | 输出满阻塞；输入不足 | SM-004 … 005 |
| 升级 | 超频、变压器、储能、拉取 / 推送 | SM-006 … 009 |
| 配方交互 | 匹配与消耗顺序（与 §4 交叉引用） | SM-010 … 011 |

### 3.2 条目表

| ID | 标题 | 不变量 / 期望摘要 | 优先级 | 测试状态 | 备注 |
|:---|:---|:---|:---|:---|:---|
| SM-001 | 进度推进 | 满足输入、能量与输出空间时，每 tick 进度按规格速率增加直至完成 | P0 | 绿 | G1.4：`StandardMachineCycleMath.tick` + guiProgress |
| SM-002 | 耗电与进度一致 | 推进进度时所耗 EU 与配方/机器功率设定一致（含 operationsPerTick 导出） | P0 | 绿 | G1.4：tick 扣 `energyConsume`；ops/length 公式可测 |
| SM-003 | 能量中断 | 缓冲 EU 不足以维持本 tick 工作时，进度不非法前进；恢复供电后**保留**进度再继续 | P0 | 绿 | G1.4：不足时保留 progress；恢复后 +1 |
| SM-004 | 输出满阻塞 | 输出槽无法放入产物时不消耗输入、不推进（或按冻结的阻塞语义），不得丢产物 | P0 | 绿 | G1.4：`recipeReady=false`（canAdd 失败）不耗电、进度清零 |
| SM-005 | 输入不足 | 输入不满足当前配方时停止处理，不产生部分非法输出 | P0 | 部分 | G1.4：同 recipeReady=false 门闩；未测真实 InvSlot 消耗 |
| SM-006 | 超频升级 | 超频提升处理速度/功率的关系符合冻结公式，且受机器上限约束 | P1 | 部分 | G1.4：0.7/1.6 单枚 length+demand+rescale；未测多枚/上限拉满 |
| SM-007 | 变压器升级 | 升级改变可接受输入电压/包规格，与 Energy 组件 tier 一致 | P1 | 未写 | draft |
| SM-008 | 储能升级 | 升级扩大能量缓冲上限，不影响配方本身 EU/t 定义 | P1 | 未写 | draft |
| SM-009 | 拉取/推送升级 | 自动 I/O 升级只影响物品搬运，不改变配方匹配结果 | P2 | 未写 | draft |
| SM-010 | 配方匹配顺序 | 多配方候选时选择顺序确定且与 Recipe 层约定一致（见 RC-*） | P0 | 未写 | 交叉 RC |
| SM-011 | 消耗时机 | 输入消耗与输出写入的 tick 时序与规格一致（完成时一次结算或逐步，正文钉死） | P0 | 未写 | draft |

---

## 4. 配方系统

**规格域前缀**：`RC-*`（Recipe）

### 4.1 主题清单

| 主题 | 规格要点（摘要，非完整用例） | 条目表 |
|:---|:---|:---|
| 输入匹配 | `IRecipeInput`：item / tag / ore 语义 | RC-001 … 003 |
| 黑白名单 | 回收机等过滤规则 | RC-004 … 005 |
| 序列化 | JSON 反序列化稳定性 | RC-006 … 007 |

### 4.2 条目表

| ID | 标题 | 不变量 / 期望摘要 | 优先级 | 测试状态 | 备注 |
|:---|:---|:---|:---|:---|:---|
| RC-001 | item 精确匹配 | `IRecipeInput` item 模式只接受指定物品（及约定的 NBT/组件规则） | P0 | 部分绿 | G1.6：`matchesExactItem`/`matchesRequiredKeys`/`acceptsMatchedInput` 纯测；运行时 `RecipeInputItemStack` 仍绑 registries |
| RC-002 | tag 匹配 | tag 输入接受该 tag 下任意成员；非成员拒绝 | P0 | 部分绿 | G1.6：`matchesAnyCandidate` 纯 any-of；tag→候选列表仍由 Ingredient/注册表解析 |
| RC-003 | ore/等价语义 | ore 字典或项目约定的等价输入语义与加载数据一致 | P1 | 部分绿 | G1.6：与 tag 共用 any-of 门闩；完整 ore 桥加载一致性未测 |
| RC-004 | 白名单接受 | 白名单内输入可匹配；名单外拒绝 | P0 | 部分绿 | G1.6：`isRecyclerRejected` 白名单模式；`TileEntityRecycler#getIsItemBlacklisted` 接线 |
| RC-005 | 黑名单拒绝 | 黑名单内输入永不匹配，即使满足其它输入描述 | P0 | 部分绿 | G1.6：`isRecyclerRejected` 黑名单模式（whitelist empty） |
| RC-006 | JSON 反序列化往返 | 合法配方 JSON → 内存模型 → 再序列化关键字段稳定（或规范化后相等） | P0 | 部分绿 | W2.3 macerator + **G2.2** extractor/compressor 数据包 JSON type 烟测；`findMatchingIndex` 对齐 bridge 直查门闩；完整 codec 往返需 RecipeManager boot |
| RC-007 | 非法 JSON 失败 | 缺字段/类型错误时加载失败且不注册半残配方 | P1 | 未写 | draft |

---

## 5. NBT / 同步

**规格域前缀**：`NS-*`（NBT / Sync）

### 5.1 主题清单

| 主题 | 规格要点（摘要，非完整用例） | 条目表 |
|:---|:---|:---|
| 旧键兼容 | 读旧 NBT 键不丢数据 | NS-001 … 002 |
| 新键写出 | 新键 `snake_case` 写出策略 | NS-003 … 004 |
| 同步往返 | 同步字段编解码 round-trip | NS-005 … 006 |

### 5.2 条目表

| ID | 标题 | 不变量 / 期望摘要 | 优先级 | 测试状态 | 备注 |
|:---|:---|:---|:---|:---|:---|
| NS-001 | 旧键兼容读 | 仅含旧 NBT 键的存档读入后，能量/进度等关键状态不丢失 | P0 | 绿 | W1.5：`Energy`/`storage`、`ConversionGenerator`/`energyBuffer` via `LegacyNbt` |
| NS-002 | 新旧键并存读 | 同时存在旧键与新键时，优先级规则确定（通常新键优先）且结果可测 | P1 | 绿 | W1.5：`EnergyNbtMigrationTest` 新键优先 |
| NS-003 | 新键 snake_case 写出 | 新写出 NBT 使用 `snake_case` 键（如 `energy_buffer`），不再只写旧驼峰键 | P0 | 绿 | W1.5：仅写 `energy_buffer` / 标准机 `progress` |
| NS-004 | 兼容期双写策略 | 若规格要求兼容期双写，读路径仍以迁移表为准；双写集合可枚举 | P1 | 未写 | 可选阶段策略 |
| NS-005 | 同步字段编解码往返 | 标准机关键同步字段（进度、active、能量显示等）encode→decode 后相等 | P0 | 绿 | W1.2：`gui_progress`+`active` via `TileEntityStandardMachine.bindStandardMachineSync`；能量显示待后续 |
| NS-006 | 同步不破坏服务端权威 | 客户端解码结果不得在无校验下写回篡改服务端权威状态（契约级） | P1 | 未写 | draft |

---

## 6. 其他高风险域（预留）

> §4.4.6：阶段 0 大纲占位；条目优先级可低于 §1–§5，但结构预留以便后续 Unit 扩展。

**规格域前缀建议**：`RX-*`（Reactor）、`CR-*`（Crop）、`UU-*`（UuGraph）、`FL-*`（Fluid）

### 6.1 主题清单（骨架）

| 主题 | 规格要点（摘要，非完整用例） | 条目表 |
|:---|:---|:---|
| 核电 | 热与爆炸阈值（至少纯函数部分） | RX-001（预留） |
| 作物 | 生长关键公式（可测部分） | CR-001（预留） |
| UU 图 | `UuGraph` 算法不变量 | UU-001（预留） |
| 流体容器 | fill / empty 语义 | FL-001…FL-003 |

### 6.2 条目表（预留 ID）

| ID | 标题 | 不变量 / 期望摘要 | 优先级 | 测试状态 | 备注 |
|:---|:---|:---|:---|:---|:---|
| RX-001 | 核电热平衡（占位） | 纯函数热输入/输出与爆炸阈值边界待后续 Unit 冻结 | P2 | 未写 | 预留 |
| CR-001 | 作物生长关键式（占位） | 可测生长/产率公式待后续冻结 | P2 | 未写 | 预留 |
| UU-001 | UuGraph 不变量（占位） | 图算法可达性/代价不变量待后续冻结 | P2 | 未写 | 预留 |
| FL-001 | 流体 fill/empty 容量与残余 | 可填量=min(空间,offer)；残余 offer；满罐/不兼容/canFill 拒；simulate 不提交 | P1 | 测绿 | `FluidTransferMathTest` + `FluidHandlerMathTest`；契约 [fluid_handler_contract.md](fluid_handler_contract.md) |
| FL-002 | IFluidHandler 单罐适配门闩 | 空 resource→0；isFluidValid↔canFill；input-only 拒 drain、output-only 拒 fill | P1 | 测绿 | G2.5 `Ic2rFluidTankHandler` 镜像；本体 0% residual |
| FL-003 | 管道式 fill→drain 序列 | IO 罐 fill 后可 drain；全抽空 stored=0；多步余量一致 | P1 | 测绿 | `FluidHandlerMathTest#pipe_sequence_*` |

---

## 7. 追溯与维护

| 动作 | 约定 |
|:---|:---|
| 新增条目 | 分配稳定 ID；补优先级与测试状态 |
| 行为有意变更 | 先改本规格与测试，再改实现；IC2R 差异写明 |
| 实现删除/替换 | 对应条目测试仍绿，或条目标记废弃并说明替代 |
| 与代码对照 | 不在本文件粘贴大段源码；用路径 + 测试名引用 |

---

## 变更记录

| 日期 | 版本 | 说明 |
|:---|:---|:---|
| 2026-07-14 | 0.1-skeleton | W0.2：章节骨架就位，条目表留空待 W0.3 |
| 2026-07-14 | 0.2-outline | W0.3：填入 EN-IC/EN-GT/SM/RC/NS 条目表与高风险预留 ID |
| 2026-07-14 | 0.3-g1.3 | G1.3：EN-IC 多汇/保护/变压器 + EN-GT-009 纯逻辑测绿；`EnergyTransferMath` 切口回接 |
| 2026-07-14 | 0.3-a40.1 | A40.1：全 EN-IC-001…010 GWT 摘要 + 测入口明确；新增 `EnergyNetIcSolverTest`（宿主边界 11 测）；EN-IC-002 加深至拓扑级；EN-IC-006/007/008 world 效果登记 `residual-world`；EN-GT-010 IC/GT 不串味 4 条对照测绿 |
