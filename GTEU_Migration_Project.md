# GregTech EnergyNet Migration

**\- Project Specification \-**

> **参考手册**（GT/GTNH 机制 + IC2R 代码映射）：[GTEU_GT_Reference.md](./GTEU_GT_Reference.md)  
> SubAgent / 开发者实现 GT Mode 前请先阅读该文档。

## 0 项目目标

本项目计划将 IC2R 的 EnergyNet 重构为可切换的双模式电网。

Config 中提供：

- IC2 Mode（兼容传统 IC2）
- GT Mode（兼容 GT 风格）

两种模式共享同一套机器代码，只允许 EnergyNet 求解方式不同。

迁移目标：

- 尽量减少重复代码，保持 API 统一。
- Config 不建议中途修改。如果修改，后果玩家自负。

## 1 Invariants

### 1.1 （非闭合电路的）欧姆定律

所有机器必须统一拥有：

- 工作电压（枚举电压等级，Working Voltage）：ULV(8V)、LV(32V)、MV(128V)、HV(512V)、EV(2048V)、IV(8192V)，再大的电压 IC 不涉及。
- 工作电流（整数，Working Current）：单位为 A，表示机器在工作时的电流消耗。若机器不消耗电流，则为 0；若机器每数个 tick 消耗电流，则显示为平均浮点数电流。

不允许出现 EU/t 作为独立配置。

### 1.2 功率

EU/t 只能且永远由 Working Voltage 与 Working Current 计算得出。

### 1.3 导线

所有导线必须拥有：最大电压（V）、最大电流（A）和线内阻（V/米，1米就是1块）。

### 1.4 电网求解

IC Mode 忽略电流限制。GT Mode 检查所有电流。如果电流过大，导线熔断。

### 1.5 机器 API

不允许在机器层面出现区分 IC2R 与 GT 的逻辑。

## 2 名词定义

| Terminology |         Definition         |
|:-----------:|:--------------------------:|
|   Voltage   |      Working Voltage       |
|   Current   |      Working Current       |
|    Power    |            EU/t            |
|   Packet    |       Energy Packet        |
|    Loss     |        Wiring Loss         |

## 3 参考：GT/GTNH 电网机制摘要

> 完整机制、公式、代码映射与 SubAgent 指引见 **[GTEU_GT_Reference.md](./GTEU_GT_Reference.md)**。本节仅保留规格级摘要。

以下机制来自 GTNH Wiki 与 GregTech 原版实现，作为 GT Mode 的行为基准。IC Mode 保持 IC2R 现有 `EnergyCalculatorUnified` 语义。

### 3.0.1 电压、电流与功率

- **电压 (V)**：能量包的"尺寸"，由电压等级枚举决定，不消耗、不存储。
- **电流 (A)**：每 tick 传输的整数能量包数量。每个包携带 `V` 个 EU。
- **功率 (EU/t)**：`P = V × A`，永远由电压与电流导出，不可独立配置。
- **电压等级**：每升一级 ×4（ULV 8 → LV 32 → MV 128 → HV 512 → EV 2048 → IV 8192）。

### 3.0.2 能量包与内部缓冲

- 发电器每 tick 尝试输出整数安培；单方块发电机默认 **1A**，电压等于其工作电压等级。
- 用电器拥有内部 EU 缓冲；**配方从缓冲扣 EU**，而非每 tick 直接向电网拉流。
- 缓冲有空位时"请求"安培；每个安培注入 `V` EU（扣除线损后）。
- 安培不可拆分：1A LV 永远携带 32 EU（线损前）。
- 空闲时机器仅请求 **1A** 以维持缓冲；工作时最大可请求：

  `maxAmps = ⌊2 × recipeEU/t / tierVoltage⌋ + 1`

  例：16 EU/t LV 配方 → 2A；2 EU/t LV 配方 → 1A（平均每 16 tick 才真正需要 1 包）。

### 3.0.3 导线、线损与熔断

- 导线属性：**最大电压**、**最大安培**、**线损 (V/米/安培)**。
- 每经过一个方块，每个安培损失 `lossPerMeter` 伏特对应的 EU（即每包减少 `loss` EU）。
- 当包内 EU 降至 0，该安培"死亡"，不再传递。
- **超电压**：包电压 > 导线最大电压 → 导线起火熔断。
- **超电流**：路径上总安培 > 导线最大安培 → 导线起火熔断。
- 推送优先级（GT 原版）：Down → Up → North → South → West → East。

### 3.0.4 爆炸与过载

- **机器爆炸**：注入包的电压 > 机器最大工作电压 → 爆炸（`Ic2Explosion.Type.Electrical`）。
- **导线熔断**：超电压或超电流（GT Mode 主要熔断机制）。
- IC Mode 保留现有绝缘击穿/导体熔断/电击逻辑（基于 `IEnergyConductor` 阈值）。
- 变压器运行时切换模式（GT）会爆炸；IC2R 变压器可在红石模式下安全切换，GT Mode 下若带电切换应爆炸。

### 3.0.5 发电机与储电

- 固定功率发电机：工作电压固定（IC 发电机为 LV），内部攒够 1A 才输出。
- 输出损耗（GT 原版）：从缓冲扣除 `8×4^tier + 2^max(0,tier-1)` EU，实际输出 `8×4^tier` EU（= 1A 满包）。IC2R 可选择性实现；首版可省略。
- 储电箱/电池盒：输入侧可吸收多安培（GT 电池盒 2A/电池），输出侧 1A；IC2R 首版统一 **1A 输出**。
- 变压器：升压 4A 低压 → 1A 高压；降压 1A 高压 → 4A 低压（与现有 `TileEntityTransformer` 的 `setPacketOutput(4/1)` 一致）。

### 3.0.6 配方功率

- 机器耗电由**配方/运行状态**决定，与机器外壳电压等级无关（外壳等级只决定"能承受的最大 V"）。
- 超频升级：功率 ×2/次，时间 ÷2/次（与 GT 2/4 超频一致）；电压等级随变压器升级提升时，**电流按恒定功率重算**。

---

## 4 架构设计

### 4.1 分层原则

```
机器层（TileEntity + Energy 组件）
  └─ 只暴露 ElectricalProfile（V、A、缓冲、方向），不含 IC/GT 分支
电网层（IEnergyCalculator 实现）
  ├─ EnergyCalculatorUnified  → IC Mode（现有逻辑，轻微适配）
  └─ EnergyCalculatorGT       → GT Mode（新建）
配置层（IC2Config.misc.energyNetMode）
  └─ 世界加载时选定 Calculator，运行中切换需重载世界
```

### 4.2 核心类型（新建 `ic2.core.energy.profile`）

**VoltageTier**（枚举）：

| 枚举 | 电压 (V) | 对应 IC tier |
|:----:|:--------:|:------------:|
| ULV  | 8        | 0            |
| LV   | 32       | 1            |
| MV   | 128      | 2            |
| HV   | 512      | 3            |
| EV   | 2048     | 4            |
| IV   | 8192     | 5            |

提供 `fromIcTier(int)`、`getVoltage()`、`getIcTier()`，替代散落的 `EnergyNet.getPowerFromTier` 魔法数。

**ElectricalProfile**（组件或 `Energy` 内嵌字段）：

- `workingVoltage: VoltageTier`
- `workingCurrent: int`（运行中）/ 平均电流 `double`（用于 Tooltip 显示亚安培负载）
- `recipePower: int`（EU/t，由 `energyConsume × operationsPerTick` 导出，仅用于计算 maxAmps）
- `bufferCapacity / bufferFill`（储电与用电器；纯用电器缓冲默认 `tierVoltage × 64`）

功率恒等式：`recipePower = workingVoltage.voltage × displayCurrent`（显示电流允许小数）。

**CableSpec**（`CableType` 扩展或并行记录）：

- `maxVoltage: VoltageTier`
- `maxAmperage: int`
- `lossPerMeterPerAmp: int`（V/米/安培，整数化 GT 线损）

**IElectricalNode**（新 API，由 `Energy` 委托实现）：

```java
VoltageTier getWorkingVoltage();
int getWorkingCurrent();          // 0 = 不用电
double getAverageCurrent();       // Tooltip 用
int getMaxSourceAmperage();       // 发电机/储电输出
int getMaxSinkAmperage();         // 用电器/储电输入，由 recipePower 导出
double getEnergyBufferCapacity();
double getEnergyBufferFree();
```

电网 Calculator 只读此接口，不读 `IEnergySink/IEnergySource` 的原始 EU 字段。

### 4.3 现有 IC2R 电网（IC Mode 基线）

当前 `EnergyCalculatorUnified` 行为摘要：

- 基于 Grid 节点图（Source / Sink / Conductor），BFS 最短路径分配。
- 发包大小 = `min(offer, tierPower × packetCount)`，连续 EU（double）。
- 线损 = 路径 `NodeLink.loss` 累加（导体 `CableType.loss`，单位 EU/块）。
- 熔断/爆炸：按 IC 阈值（`capacity+1`、`getInsulationBreakdownEnergy` 等）。
- 多包：`IMultiEnergySource`（变压器 4 包降压）。

IC Mode 迁移时：**机器侧改为 V+A 描述，Calculator 将 A 折算回 EU 包大小**，对外行为与现网一致，仍忽略导线安培限制。

---

## 5 系统迁移约束

### 5.1 耗电机器迁移

**范围**：`TileEntityElectricMachine` 子类、`TileEntityStandardMachine`、`Process` 驱动机器、泵/矿机等工作时耗电方块。

**约束**：

1. 删除构造函数中的裸 `energyPerTick` 作为唯一真源；改为 `defaultRecipePower`（EU/t）+ `defaultVoltage`（通常 LV）。
2. `workingCurrent` 运行时计算：

   ```
   displayCurrent = recipePower / workingVoltage.voltage
   workingCurrent = recipePower > 0 ? max(1, ceil(recipePower / V)) : 0   // 电网用整数
   maxSinkAmps    = recipePower > 0 ? floor(2 * recipePower / V) + 1 : 1
   ```

3. 每 tick 仍调用 `energy.useEnergy(recipePower)` 从**内部缓冲**扣电；电网只负责填缓冲。
4. 超频升级（`InvSlotUpgrade.setOverclockRates`）：
   - `recipePower` 按现有 `energyDemandMultiplier` 变化；
   - `workingVoltage` 按 `getTier()` 变化；
   - **必须**同步重算 `workingCurrent`，保证功率不变式。
5. 非标准机器（矿机、泵、扫描仪等）各自将动态耗电写入 `recipePower`，逻辑同上。
6. `getDemandedEnergy()` / `getSinkTier()` 从 `Energy` 委托移除模式分支，改由 Calculator 读取 `IElectricalNode`。

**IC2R 机器默认可表**（`defaultVoltage = LV`，`recipePower = energyPerTick`）：

| 机器 | recipePower (EU/t) | 默认电压 | 工作电流 (显示) |
|:-----|:------------------:|:--------:|:---------------:|
| 打粉机 | 2 | LV | 0.0625 A |
| 压缩机 | 2 | LV | 0.0625 A |
| 提取机 | 2 | LV | 0.0625 A |
| 电炉 | 3 | LV | 0.09375 A |
| 固体装罐机 | 2 | LV | 0.0625 A |
| 金属成型机 | 10 | LV | 0.3125 A |
| 洗矿机 | 16 | LV | 0.5 A |
| 离心机 | 48 | MV | 0.375 A |
| 感应炉 | (动态) | MV | 按运行时功率 |
| 其他 | 查各 `TileEntity*` 构造函数 | 见 `defaultTier` | `P/V` |

### 5.2 储电箱机器迁移

**范围**：`TileEntityElectricBlock` 系列（BatBox/CESU/MFE/MFSU 及充电板）。

**约束**：

1. 工作电压 = 储电箱额定输出电压等级；输出电流固定 **1A**（首版）。
2. `fullEnergy = true` 语义保留：内部攒满 1A 才对外提供（与 GT 电池盒一致）。
3. 输入 `maxSinkAmps`：首版 **2A**（GT 电池盒基准），后续可通过升级/多单元储电箱扩展。
4. 缓冲容量（EU）不变；仅改变描述方式。

| 方块 | 电压 | 输出 | 最大输入 | 缓冲 (EU) |
|:-----|:----:|:----:|:--------:|:---------:|
| BatBox | LV 32V | 1A (32 EU/t) | 2A | 40,000 |
| CESU | MV 128V | 1A (128 EU/t) | 2A | 300,000 |
| MFE | HV 512V | 1A (512 EU/t) | 2A | 4,000,000 |
| MFSU | EV 2048V | 1A (2048 EU/t) | 2A | 40,000,000 |

5. `output` 字段（`TileEntityElectricBlock.output`）标记为 deprecated，改读 `VoltageTier × sourceAmps`。

### 5.3 电力工具迁移

**范围**：`ItemElectricTool`、`ElectricItem.manager` 充放电。

**约束**：

1. 工具保持 `operationEnergyCost`（EU/次）不变，不引入安培传输。
2. 显示：`工作电压：LV`，`单次耗电：xxx EU`；充放电功率仍用 `tier` 限制最大 V。
3. 充放电槽（`InvSlotCharge`/`InvSlotDischarge`）的 `tier` 与 `VoltageTier` 对齐。
4. 不在工具层模拟电网安培；与 GT 工具行为一致。

### 5.4 发电机机器迁移

**A. 恒定功率发电机**（`TileEntityBaseGenerator` 子类：燃煤、太阳能、地热、半流体、RTG 等）

1. 工作电压固定 **LV**（即使产出 < 32 EU/t）。
2. `production`（EU/t）→ `sourceAmps = 1`，`workingVoltage = LV`；实际每 tick 向缓冲写入 `production` EU。
3. 对外输出：仅当 `storage ≥ 32`（1A LV）时 `getOfferedEnergy()` 返回 32（GT Mode）或全部 storage（IC Mode）。
4. 燃料发电逻辑（`gainEnergy`）不变。

**B. 动态发电机**（`TileEntityConversionGenerator`：动能、斯特林等）

1. `workingVoltage = VoltageTier.fromPower(maxProduction)`，动态随产出变化。
2. 输出 1A，包电压 = 当前工作电压；产出不足 1A 时内部积攒。
3. `getSourceTier()` 改为读 `workingVoltage.getIcTier()`。

**C. 核反应堆 EU 模式**（`TileEntityNuclearReactorElectric`）

1. `output`（float EU/t）→ 动态 `workingVoltage = fromPower(output)`，`sourceAmps = 1`。
2. 高热输出时电压可达 EV/IV；需保留 `IExplosionPowerOverride`。
3. 与热反应堆模式（HU）完全解耦，HU 不受本迁移影响。

**D. 创意发电机**：保持无限功率，IC Mode 语义；GT Mode 下可配置为禁用或限额。

### 5.5 电网迁移

**新建 `EnergyCalculatorGT`**，实现 `IEnergyCalculator`。

#### 5.5.1 每 tick 流程

```
1. Sync（EnergyNetLocal）
   对每个 Source 节点：
     if buffer ≥ workingVoltage:
       offerAmps = min(maxSourceAmps, floor(buffer / V))
       记录 (node, offerAmps, V)
2. Sync（Grid）
   对每个 Sink 节点：
     demandAmps = min(maxSinkAmps, floor(bufferFree / V))
     if demandAmps > 0: 加入需求队列
3. 分配（按 GT 方向优先级推送）
   对每个 Source，按 D-U-N-S-W-E 邻居推送安培：
     对每个方向上的路径，传输整数安培
     每块导线：perAmpLoss = cableSpec.lossPerMeterPerAmp
     包 EU = V - accumulatedLoss；EU ≤ 0 则该安培死亡
     检测导线：totalAmps > maxAmperage → 熔断
                 packetV > maxVoltage → 熔断
4. 注入 Sink
     inject EU = survivingAmps × packetEU
     若 packetV > sink.workingVoltage → 标记爆炸
5. 副作用
     熔断 → removeConductor()
     爆炸 → explodeTile()
```

#### 5.5.2 与 IC Mode 的差异对照

| 项目 | IC Mode | GT Mode |
|:-----|:--------|:--------|
| 分配算法 | BFS 最短 EU 损耗路径 | 方向优先级推送 |
| 传输单位 | 连续 EU | 整数安培 × 电压 |
| 导线限流 | 仅 EU 包大小（capacity） | 电压 + 安培双限制 |
| 线损 | EU/块（固定） | V/块/安培 |
| 用电器需求 | 全部空余缓冲 | 按 maxSinkAmps 分批 |
| 发电机提供 | 全部 storage | 满 1A 才输出 |

#### 5.5.3 共享基础设施

- `Grid`、`Node`、`NodeLink`、`GridUpdater`、`ChangeHandler` **复用**，不重复建图。
- `EnergyNetGlobal.create()` 根据 Config 注入 Calculator：

  ```java
  calculator = IC2Config.misc.energyNetMode.get() == GT
      ? new EnergyCalculatorGT()
      : new EnergyCalculatorUnified();
  ```

- `IEnergyCalculator` 可增加 `default` 方法读取 `IElectricalNode`，旧 Tile 通过 `Energy` 适配器提供。

#### 5.5.4 IC Mode 适配要点

- `EnergyCalculatorUnified.emit()` 发包大小改为 `min(offer, workingCurrent × V × packetCount)` 而非 `tierPower`。
- 仍忽略 `maxAmperage`；`CableSpec.maxAmperage` 在 IC Mode 下不生效。
- 爆炸判定保留 `amount > getPowerFromTier(sinkTier)`，与现网一致。

### 5.6 玩家显示迁移

**Tooltip**（`Ic2TileEntity.appendItemTooltip`、`CableType` 物品）：

- 机器：`电压：LV (32V)` / `功率：2 EU/t (0.0625 A)`；有升级时显示实时值。
- 储电箱：`电压：LV` / `输出：32 EU/t (1 A)` / `容量：40,000 EU`。
- 导线：`最大电压：LV (32V)` / `最大电流：1 A` / `线损：1 V/m/A`。
- 移除单独显示 `电压等级：1` 的数字 tier。

**GUI**（`EnergyGauge`、储电箱/变压器 GUI）：

- 能量条保留 EU 数值；副文本显示 V、A。
- 变压器：输入 `4×128 EU/t (4 A MV)` → 输出 `512 EU/t (1 A HV)`。

**调试**（`ItemToolMeter`）：

- GT Mode 额外显示：路径安培、线损伏特、导线负载率。

### 5.7 接口迁移

**保留并标记 legacy**（addon 兼容）：

- `IEnergySink` / `IEnergySource` / `IEnergyConductor` 保持不变。
- `getSinkTier()` / `getSourceTier()` 映射 `VoltageTier.getIcTier()`。

**新增**：

- `ic2.api.energy.profile.VoltageTier`
- `ic2.api.energy.profile.IElectricalNode`
- `ic2.api.energy.profile.ICableSpec`

**`Energy` 组件**：实现 `IElectricalNode`；`EnergyNetDelegate` 的 `getDemandedEnergy()` 在 GT Calculator 中不再被直接调用，改由 Calculator 轮询 `IElectricalNode`。

**Addon 迁移指南**：

- 只实现 `IEnergySink` 的第三方机器：提供默认 `maxSinkAmps = 1`，电压由 `getSinkTier()` 导出。
- 建议逐步迁移到 `IElectricalNode`。

### 5.8 数据迁移

**机器**：

- 不需要改存档结构；`Energy.storage` 仍为 EU 双精度。
- 构造函数常量表更新（见 5.1 表格）；NBT 无变化。
- 世界加载后 `workingVoltage/Current` 由运行时状态导出，不写入 NBT。

**导线**（`CableType` → `CableSpec`）：

| 导线 | 最大电压 | 最大电流 (A) | 线损 (V/m/A) | 备注 |
|:-----|:--------:|:------------:|:------------:|:-----|
| tin | LV 32V | 1 | 1 | 对应 GT 1x Tin |
| copper | MV 128V | 2 | 1 | |
| gold | HV 512V | 3 | 2 | |
| iron | EV 2048V | 4 | 3 | |
| glass | IV 8192V | 8 | 0 | 无损；仅 IC 有 |
| detector/splitter | IV 8192V | ∞ | 0 | IC Mode 专用，GT Mode 下 maxAmps = 64 上限 |

> IC Mode 线损仍用现有 `CableType.loss`（EU/块）；GT Mode 用 `CableSpec.lossPerMeterPerAmp`。两种线损表并存，由 Calculator 选用。

**Config 新增**（`IC2Config.Misc`）：

```toml
[energyNet]
# IC2 = 经典 EU 包（默认）；GT = 电压/电流/安培限制
mode = "IC2"   # 或 "GT"
```

## 6 不需要修改的内容

配方、材质、模型、物品、方块、进度、数据包。

## 7 迁移启动检查清单

### 7.0 能否启动？

**结论：可以启动。** 规格、行为基准、PR 顺序、测试矩阵均已就绪；代码库中尚未有迁移实现（符合预期），从 **PR-1** 开工无阻塞项。

| 检查项 | 状态 | 说明 |
|:-------|:----:|:-----|
| 电压/电流/功率 Invariants | ✅ | §1 已定义，无矛盾 |
| GT 行为基准文档 | ✅ | `GTEU_GT_Reference.md` |
| IC2R 现状与映射 | ✅ | 参考手册 §9 + 迁移文档 §4 |
| 双 Calculator 架构 | ✅ | §4.1、§5.5 |
| 机器/导线数据表 | ✅ | §5.1、§5.8 |
| PR 顺序与验收标准 | ✅ | §7.1 |
| 代码实现 | ⬜ | 待 PR-1 起 |
| 自动化测试 | ⬜ | 建议 PR-3 起补 GameTest 或对比日志；不阻塞 PR-1 |

**启动前约定**：

1. 默认 Config `energyNetMode = IC2`，确保旧存档/旧行为不受影响。
2. 每 PR 独立可合并；GT Mode 在 PR-4 之前仅基础设施，玩家不可见。
3. SubAgent 任务描述须引用 `GTEU_Migration_Project.md` 的 PR 编号 + `GTEU_GT_Reference.md` 对应章节。

**非阻塞待定项**（见参考手册 §10）：GT 输出损耗公式、雨天爆炸、方向优先级精确实现可延后。

### 7.1 实施阶段（PR 计划）

建议按依赖顺序分 6 个 PR，每个 PR 可独立测试，降低回归风险。

| 阶段 | 内容 | 验收标准 |
|:----:|:-----|:---------|
| PR-1 | `VoltageTier`、`ElectricalProfile`、`CableSpec`；`Energy` 组件实现 `IElectricalNode`；Config `energyNetMode` | 编译通过；`/ic2 debug` 可打印 V/A |
| PR-2 | 机器/储电/发电机常量表迁移；`setOverclockRates` 重算电流 | 所有标准机器功率恒等式成立；超频后 P=V×A |
| PR-3 | `EnergyCalculatorUnified` 适配 V+A 发包；IC Mode 回归测试 | 现有电网行为与迁移前一致（自动化测试或对比日志） |
| PR-4 | 新建 `EnergyCalculatorGT`：安培推送、线损、熔断 | GT 单线 1A LV 传输；超流/超压熔断；方向优先级 |
| PR-5 | 爆炸/过载统一；变压器 GT 模式带电切换爆炸；核反应堆动态电压 | 高压注入低阶机器爆炸；变压器安全规则 |
| PR-6 | Tooltip/GUI/电表/语言文件 | 显示 V、A、线损；储电箱显示 1A 输出 |

**测试矩阵**（每个 PR 至少覆盖）：

1. 1 台 LV 发电机 → 1 台打粉机（2 EU/t），单线 1A tin 线 16 格。
2. 4 台打粉机并联 1 台发电机（GT：应轮流获得 1A）。
3. 变压器升压/降压 4A↔1A 往返。
4. MFE 1A 输出 → HV 机器；低压导线超压熔断。
5. 超频升级 + 变压器升级同时存在时功率显示正确。
6. IC Mode 与 GT Mode 切换（新世界）后各自行为符合预期。

## 8 迁移构想（玩家公告草案）

供玩家查阅的迁移构想。

电力机器的配方消耗功率是确定的。例如 2 EU/t，就像 GT 一样。传输电流时，永远为整数。电压等级是枚举值，电流是整数值。功率由电压等级与电流计算得出。

迁移前，机器（例如打粉机）的电压等级是 1，每 tick 消耗 2 EU。打粉机方块物品的 Tooltip 原来为：`电压等级：1`。改为：`电压等级：LV`，下一行：`功率：2 EU/t`。

如果玩家加入了超频升级，那么超频升级不在机器上时，仍然显示原来的 Tooltip。一旦在机器内部，就应该显示为：`功率消耗：xxx EU/t（xA LV）`，下一行：`处理时间：xxx tick（xxx %）`。对应的数值和电压等级和机器相关。

注意，若机器内同时拥有高压升级，那么该机器的工作电压等级随着变化，工作电流应随着电压变化重新根据功率来算。（放到最高为 IV 8192EU/t，再多也没用了）

储电箱、CESU、MFE 和 MFSU 从 32 EU/t、128 EU/t、512 EU/t、2048 EU/t 改为 32 EU/t（1A LV）、128 EU/t（1A MV）、512 EU/t（1A HV）、2048 EU/t（1A EV）。并且在 Tooltip 以及 GUI 中显示为：`电压等级：LV`，下一行：`功率：32 EU/t（1A LV）`。

以后会加入像 GT 里的多安培输出储电箱，以及 **兰波顿超级电容库**！

固定功率发电机的电压等级均为 LV。总是整数输出。EU 不够 1A LV 时，攒在发电机内部，直到足够 1A 再输出。

动态功率发电机和核反应堆的电压等级随着输出功率变化而变化。

### 8.1 GT Mode 与 IC Mode 玩家差异速查

| 场景 | IC Mode | GT Mode |
|:-----|:--------|:--------|
| 电线过载 | 包过大则烧线/掉绝缘 | 超电压或超电流即熔断 |
| 发电机 | 有多少 EU 输出多少 | 攒够 1 整包才输出 |
| 长线损耗 | EU 固定减少/块 | 每安培每块损失固定伏特 |
| 多机并联 | 近似均分 EU | 轮流获得整包，低耗电机可多台共用 1A |
| 配置 | `energyNetMode = IC2`（默认） | `energyNetMode = GT`（新世界推荐） |

> Config 不建议在已有存档中途切换；切换后缓冲电量保留，但输电行为可能非预期。
