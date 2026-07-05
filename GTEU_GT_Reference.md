# GregTech / GTNH 电网参考手册

> **读者**：IC2R 开发者、SubAgent、代码审查者。  
> **用途**：实现 GT Mode 时的行为基准；与 IC Mode 对照。  
> **配套文档**：[GTEU_Migration_Project.md](./GTEU_Migration_Project.md)（迁移规格与 PR 计划）。

---

## 1. 核心概念

### 1.1 三个量

| 量 | 符号 | 单位 | 含义 |
|:---|:----:|:----:|:-----|
| 电压 | V | Volt | 能量包的"尺寸"；由电压等级枚举决定；不消耗、不存储 |
| 电流 | A | Amp | 每 tick 传输的**整数**能量包数量 |
| 功率 | P | EU/t | 时间上的能量流率；**永远** `P = V × A` |

**Invariant**：IC2R 不允许把 EU/t 作为独立配置项存盘；它只能从 `Working Voltage × Working Current` 导出。

### 1.2 电压等级（IC2R 范围）

| 等级 | 缩写 | 电压 (V) | IC tier (`getPowerFromTier`) |
|:----:|:----:|:--------:|:----------------------------:|
| ULV  | ULV  | 8        | 0 |
| LV   | LV   | 32       | 1 |
| MV   | MV   | 128      | 2 |
| HV   | HV   | 512      | 3 |
| EV   | EV   | 2048     | 4 |
| IV   | IV   | 8192     | 5 |

每升一级电压 ×4。IC2R 不涉及 LuV 及以上。

**IC2R 现有换算**（`EnergyNetGlobal`）：

```java
power = 8 << (tier * 2);           // tier < 14
tier  = ceil(log(power / 8) / log(4));
```

### 1.3 能量包（Packet / Amp）

- GT 中 **1 安培 = 1 个能量包**。
- 每个包携带 `V` 个 EU（线损前）。
- **安培不可拆分**：1A LV 永远注入 32 EU（或线损后的剩余值），不能拆成 2×16 EU。
- 代码层面"推送安培"，玩家视角可理解为"机器在缓冲有空位时拉取整包"。

---

## 2. 用电器（Sink）

### 2.1 内部缓冲

- 所有耗电机器有内部 EU 缓冲（buffer）。
- **配方耗电从缓冲扣除**，不是每 tick 从导线"拉 EU"。
- 缓冲容量与 GT 类似，约为 `tierVoltage × 64`（LV = 2048 EU）；IC2R 现有机器用 `energyPerTick × operationLength` 作为缓冲，迁移时保持 EU 容量不变，仅改描述。

### 2.2 请求安培

缓冲有空位时请求安培；每注入 1A 获得 `V` EU（扣除线损后）。

**空闲**（不处理配方）：最多请求 **1A** 维持缓冲。

**工作**（recipePower = 配方 EU/t）时，最大可请求：

```
maxSinkAmps = floor(2 × recipePower / tierVoltage) + 1
```

| recipePower | tierVoltage | maxSinkAmps |
|:-----------:|:-----------:|:-----------:|
| 2 EU/t      | 32 (LV)     | 1 |
| 16 EU/t     | 32 (LV)     | 2 |
| 32 EU/t     | 32 (LV)     | 3 |
| 48 EU/t     | 128 (MV)    | 1 |

实际每 tick 请求：

```
demandAmps = min(maxSinkAmps, floor(bufferFree / tierVoltage))
demandEU   = demandAmps × tierVoltage
```

### 2.3 过载爆炸

当注入包的**电压** > 机器最大工作电压 → 电气爆炸。

```
if (packetVoltage > sink.workingVoltage) → explode
```

与 GTNH Wiki [Explosion#Voltage](https://wiki.gtnewhorizons.com/wiki/Explosion) 一致。

### 2.4 显示电流

- 电网运算用**整数安培**。
- Tooltip 可用**平均电流**：`displayCurrent = recipePower / tierVoltage`（如打粉机 2/32 = 0.0625 A）。

---

## 3. 发电器（Source）

### 3.1 单方块发电机

- 默认每 tick 输出 **1A**，电压 = 工作电压等级。
- 输出前检查内部缓冲是否 ≥ `V`；不足则**积攒**，不对外发包（GT 风格）。

```
if (buffer >= workingVoltage):
    offerAmps = min(maxSourceAmps, floor(buffer / V))   // 单方块默认 maxSourceAmps = 1
    offerEU   = offerAmps × V
```

### 3.2 固定功率 IC 发电机（燃煤等）

- IC2R 规定：工作电压固定 **LV (32V)**，即使 `production < 32` EU/t。
- 每 tick 向缓冲写入 `production` EU；对外仍按 1A LV 规则输出。

### 3.3 动态发电机（动能、斯特林、核反应堆 EU 模式）

- 工作电压随产出变化：`workingVoltage = VoltageTier.fromPower(currentOutput)`。
- 仍输出 1A；包电压 = 当前工作电压。

### 3.4 输出损耗（GT 原版，IC2R 首版可省略）

GT 单方块输出时从缓冲额外扣除：

```
loss   = 8 × 4^tier + 2^max(0, tier-1)
output = 8 × 4^tier    // = 1A 满包
```

例：LV 发电机扣 40 EU，输出 32 EU。IC2R PR-4 可不实现；在本文档标记为 **Deferred**。

### 3.5 多发电机并联

- 每个发电机独立尝试清空缓冲。
- 先被 Minecraft tick 顺序处理的发电机优先耗尽；顺序在 chunk 重载后会变（GTNH 已知行为）。

---

## 4. 储电箱 / 变压器

### 4.1 储电箱（BatBox / CESU / MFE / MFSU）

| 方块 | 工作电压 | 输出 | 输入（首版） | 缓冲 EU |
|:-----|:--------:|:----:|:------------:|:-------:|
| BatBox | LV 32V | 1A | 2A | 40,000 |
| CESU | MV 128V | 1A | 2A | 300,000 |
| MFE | HV 512V | 1A | 2A | 4,000,000 |
| MFSU | EV 2048V | 1A | 2A | 40,000,000 |

- `fullEnergy = true`：攒满 1A 才输出（与现有 `Energy.getSourceEnergy()` 一致）。
- 功率显示：`32 EU/t (1 A LV)` 等。

### 4.2 变压器

与 IC2R 现有 `TileEntityTransformer` 一致：

| 模式 | 输入 | 输出 |
|:-----|:-----|:-----|
| 升压 (stepUp) | 4A × 低压 V | 1A × 高压 V |
| 降压 (stepDown) | 1A × 高压 V | 4A × 低压 V |

- 实现手段：`IMultiEnergySource`，`setPacketOutput(4)` 或 `1`。
- **GT 规则**：带电切换升/降压模式 → 爆炸。IC2R 红石自动切换在 GT Mode 下若带电应爆炸（PR-5）。

能量守恒：4 × 128 = 512，4 × 32 = 128。

---

## 5. 导线

### 5.1 属性

| 属性 | 含义 |
|:-----|:-----|
| maxVoltage | 可承载包的最大电压；超则熔断 |
| maxAmperage | 路径上可同时通过的安培数上限；超则熔断 |
| lossPerMeterPerAmp | 每块每安培损失的伏特数（EU/包/块） |

### 5.2 线损计算

对路径上每个导体方块、每个通过的安培：

```
packetEU_after_block = packetEU_before_block - lossPerMeterPerAmp
```

若 `packetEU ≤ 0`，该安培**死亡**，不再前进。

**例**：1A LV (32 EU) 经 16 格 1x Tin（loss=1 V/m/A）→ 到达时 16 EU。

与 GTNH [Snagger's Guide §5](https://wiki.gtnewhorizons.com/wiki/Snagger%27s_Electricity_Guide_for_New_Players) 一致。

### 5.3 熔断条件

```
if (packetVoltage > cable.maxVoltage)  → melt
if (pathTotalAmps > cable.maxAmperage) → melt
```

熔断 = 导线方块破坏（对应 IC2R `IEnergyConductor.removeConductor()`）。

### 5.4 IC2R 导线映射表（GT Mode）

| CableType | maxVoltage | maxAmps | loss (V/m/A) | IC Mode loss (EU/块) |
|:----------|:----------:|:-------:|:------------:|:--------------------:|
| tin | LV 32V | 1 | 1 | 0.2 |
| copper | MV 128V | 2 | 1 | 0.2 |
| gold | HV 512V | 3 | 2 | 0.4 |
| iron | EV 2048V | 4 | 3 | 0.8 |
| glass | IV 8192V | 8 | 0 | 0.025 |
| detector/splitter | IV | 64 cap | 0 | 0.5 |

IC Mode **只**用 `CableType.loss`（EU/块），**忽略** maxAmperage。

### 5.5 未绝缘导线

GT 规则：裸线线损 ×2。IC2R 已有绝缘层系统；GT Mode 可对 `insulation == 0` 的导线将 `lossPerMeterPerAmp × 2`。

---

## 6. 电网分配算法

### 6.1 GT：方向优先级推送

电缆从源向邻居推送，方向优先级：

```
Down → Up → North → South → West → East
```

**不是**"最近机器优先"。同一方向上的多个 Sink 按遍历顺序分配。

### 6.2 IC2R 现状（IC Mode）

`EnergyCalculatorUnified`：

- 建图：`Grid` / `Node` / `NodeLink`
- BFS 按**最低 EU 线损路径**分配
- 连续 EU 包（double），非整数安培
- 多包：`distributeMultiple` + `IMultiEnergySource`

文件入口：

- `ic2/core/energy/grid/EnergyCalculatorUnified.java`
- `ic2/core/energy/grid/Grid.java`
- `ic2/core/energy/grid/EnergyPath.java`
- `ic2/core/block/comp/Energy.java`

### 6.3 GT Mode 目标算法（伪代码）

```
for each Grid:
  collect sources with (offerAmps, V)
  collect sinks with (demandAmps, V, bufferFree)

  for each source in tick_order:
    remaining = offerAmps
    for dir in [DOWN, UP, NORTH, SOUTH, WEST, EAST]:
      for each sink reachable via dir-first walk:
        send = min(remaining, sink.demandAmps, after_loss_surviving)
        apply cable amp/voltage checks
        inject to sink buffer
        remaining -= send
```

PR-4 可实现简化版：复用 `Grid` 拓扑，替换 `distribute`/`emit` 为安培语义。

---

## 7. 爆炸、电击与熔断对照

| 事件 | GT Mode | IC Mode（现有） |
|:-----|:--------|:----------------|
| 导线超流/超压 | 熔断（删方块） | 不检查安培；检查 EU 包 > capacity |
| 绝缘击穿 | 可选保留 IC 逻辑 | `removeInsulation()` |
| 机器过压 | `packetV > sinkTier` → 爆炸 | `amount > getPowerFromTier(sinkTier)` |
| 电击 | 可选：高 EU 包近导线 | `applyCableEffects` 已有 |
| 雨天/火焰 | GT 原版有；IC2R 未实现 | 未实现 |
| 变压器带电切换 | 爆炸 | 当前允许红石切换 |

配置开关：`IC2Config.misc.enableEnetCableMeltdown`、`enableEnetExplosions`（现有）。

---

## 8. 配方功率与超频

### 8.1 配方功率

- 机器耗电由**配方/运行状态**决定，与外壳最高电压无关。
- 外壳电压 = 能承受的最大 V；配方功率可以低于 `V`（低电流运行）。

### 8.2 超频升级（IC2R 现有逻辑）

`InvSlotUpgrade.setOverclockRates()` / `TileEntityStandardMachine`：

```
energyConsume ↑ (×2 per overclocker)
operationLength ↓ (÷2)
tier ↑ (transformer upgrade)
```

迁移后必须同步：

```
recipePower = energyConsume × operationsPerTick
workingVoltage = VoltageTier.fromIcTier(tier)
displayCurrent = recipePower / V
maxSinkAmps = floor(2 × recipePower / V) + 1
```

### 8.3 GT 超频（参考，IC2R 不实现多格超频）

GT 多方块 2/4 超频：速度 ×2、总 EU ×2、EU/t ×4。IC2R 仅单方块超频升级，行为已够用。

---

## 9. IC2R 代码映射速查

| 概念 | 现有位置 | 迁移后 |
|:-----|:---------|:-------|
| 电网单例 | `EnergyNetGlobal` | 按 Config 选 Calculator |
| IC 计算器 | `EnergyCalculatorUnified` | IC Mode，轻微适配 V+A |
| GT 计算器 | （无） | `EnergyCalculatorGT` 新建 |
| 机器能量 | `Energy` 组件 | + `IElectricalNode` |
| 导线 | `AbstractCableBlock.Conductor` | + `CableSpec` |
| Tier 换算 | `getPowerFromTier` / `getTierFromPower` | `VoltageTier` 枚举 |
| 储电输出 | `TileEntityElectricBlock.output` | deprecated → V×A |
| 变压器 | `TileEntityTransformer` | 已有 4A/1A，补 GT 带电切换爆炸 |
| 标准机器 | `TileEntityStandardMachine` | `recipePower` + 电流重算 |
| 动态发电 | `TileEntityConversionGenerator` | 动态 `workingVoltage` |
| 核反应堆 | `TileEntityNuclearReactorElectric` | 动态 V，1A 输出 |

---

## 10. 已知简化与 Deferred 项

首版迁移**允许省略**，不阻塞 PR-1～PR-3：

| 项目 | 说明 |
|:-----|:-----|
| GT 输出损耗公式 | 8×4^tier 扣除；首版发电机可直接输出 1A |
| 方向优先级 | PR-4 可用 BFS 近似，PR-4b 再换 D-U-N-S-W-E |
| 雨天/火焰爆炸 | GT 环境爆炸；IC2R 无此机制 |
| 兰波顿 / 多安培储电 | 后续内容 |
| AE2 / 第三方 EU | `Ic2Ae2Plugin` 保持 IC 语义至 PR-7+ |
| superconductor 导线 | IC2R 无对应材质 |

---

## 11. 参考资料

- [GTNH Wiki: Electricity](https://wiki.gtnewhorizons.com/wiki/Electricity)
- [GTNH Wiki: Snagger's Electricity Guide](https://wiki.gtnewhorizons.com/wiki/Snagger%27s_Electricity_Guide_for_New_Players)
- [GTNH Wiki: Explosion](https://wiki.gtnewhorizons.com/wiki/Explosion)
- IC2R 源码：`src/main/java/ic2/core/energy/grid/`
- 迁移规格：`GTEU_Migration_Project.md`

---

## 12. SubAgent 工作指引

执行迁移任务时：

1. **先读** `GTEU_Migration_Project.md` §1 Invariants 与 §7 PR 计划，确认当前 PR 范围。
2. **再读**本文档对应章节（Sink/Source/Cable/算法）。
3. **禁止**在 `TileEntity*` 内写 `if (gtMode)`；模式分支只在 `EnergyCalculator*` 和 `EnergyNetGlobal.create()`。
4. **保持** `IEnergySink`/`IEnergySource` 向后兼容；新逻辑走 `IElectricalNode`。
5. **验收**：对应 PR 的测试矩阵（迁移文档 §7）+ `P = V × A` 恒等式。