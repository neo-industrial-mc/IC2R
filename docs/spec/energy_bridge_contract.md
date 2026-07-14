# FE/RF 能量桥契约（G2.8）

> **Work Unit**: G2.8  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §7.5；[phase2_closeout.md](phase2_closeout.md) G2.8；[platform_spi.md](platform_spi.md) `PlatformEnergyBridge`  
> **实现**:
> - 纯转换：`me.halfcooler.ic2r.core.energy.EnergyBridgeMath`
> - Forge 桥：`me.halfcooler.ic2r.forge.PlatformEnergyBridgeForge`（`IEnergyStorage`）
> - 安装：`ForgePlatformServices` → `PlatformServices.energy()`
> **测例**: `src/test/java/me/halfcooler/ic2r/energy/EnergyBridgeMathTest.java`（纯逻辑；**不**构造 BE / cap）

---

## 1. 权威单位与分层

| 层 | 单位 | 位置 | 职责 |
|:---|:---|:---|:---|
| **IC 电网（权威）** | **EU** | `core.energy` / EnergyNet | 机器、电缆、sink/source 全链路 |
| **转换库** | EU ↔ FE（double/long） | `EnergyBridgeMath` | 比率、取整、残余；无 MC/Forge 类型 |
| **Platform SPI** | **外部 FE**（`long`） | `PlatformEnergyBridge` | 对邻居 cap 的 can/insert/extract；**不**含 EU 电网逻辑 |
| **Forge 实现** | FE (`int` via clamp) | `PlatformEnergyBridgeForge` | `ForgeCapabilities.ENERGY` → `IEnergyStorage` |

**硬规则（§7.5）**：

1. **EU 为内部权威** — 不把 IC 电网默认改成 FE/RF。  
2. **FE/RF 仅 platform 适配** — common 业务不 `import` Forge energy 类型做电网计算。  
3. **比例与开关配置化预留** — 转换函数均接受 `fePerEu` 参数；默认常量可被配置/调用方覆盖。  
4. **默认不破坏 IC 电网语义** — 本 Unit 不改 EnergyNet 拓扑、不挂 FE cap 到 IC 机器。

```text
IC EU (EnergyNet)  ──optional convert──►  EnergyBridgeMath  ──FE long──►  PlatformEnergyBridge
                                                                              │
                                                                              ▼
                                                                    IEnergyStorage (Forge)
```

---

## 2. 默认比率（已选定并冻结玩法对齐）

| 符号 | 值 | 说明 |
|:---|:---|:---|
| **`DEFAULT_FE_PER_EU`** | **`2.0`** | 1 EU = **2 FE**（发送路径常用） |
| 来源 | `Ic2rAe2Plugin.EU_TO_AE_RATIO` | 与现有 AE2 集成路径一致，**非**静默改 AE2 玩法 |
| 备选未采用 | 4 FE/EU | 常见于部分模组；本库**不**作默认，以免与 AE2 双轨 |

**AE2 关系**：

- AE2 插件保留 `EU_TO_AE_RATIO` 别名，值为 `EnergyBridgeMath.DEFAULT_FE_PER_EU`（同源）。  
- `injectFePower` / `getFeDemand`（及同类算术）走 `EnergyBridgeMath`，行为与原先 `* ratio` / `/ ratio` / `Math.ceil` 等价。  
- AE grid 直注（`injectPower` AE units）仍用同一比率；**不**经 `PlatformEnergyBridge`（grid API ≠ FE cap）。

**开关预留（未实现配置项）**：

- 调用方可传自定义 `fePerEu`；未来 `IC2RConfig` 可暴露 `fePerEu` / `enableFeBridge`。  
- `enableFeBridge=false` 时：不调用 SPI insert/extract 即可；SPI 本身保持无状态。  
- **禁止**在未文档化的情况下改 `DEFAULT_FE_PER_EU`。

---

## 3. `EnergyBridgeMath` API 契约

| 方法 | 语义 | 边界 |
|:---|:---|:---|
| `euToFeCeil(eu, ratio)` | EU→FE **向上取整**（推送路径，对齐 AE2 `Math.ceil`） | `eu≤0` 或非法 ratio → `0` |
| `euToFeFloor(eu, ratio)` | EU→FE **向下取整**（保守显示/库存） | 同上 |
| `feToEu(fe, ratio)` | FE→EU 双除法 | `fe≤0` 或非法 ratio → `0` |
| `residualFe(offered, transferred)` | 外部单位残余 | 负/过冲 → 夹到 `≥0` |
| `residualEuAfterFeTransfer(euOffer, feAccepted, ratio)` | 推 FE 后 EU 残余（`euOffer - min(fe/ratio, offer)`） | offer≤0 → 0；未接受 → 全残余 |
| `clampToIntEnergy(long)` | 适配 Forge `int` API | ≤0 → 0；`>MAX` → `Integer.MAX_VALUE` |
| `isValidRatio` | finite 且 `> 0` | NaN/Inf/≤0 → false |

默认重载均使用 `DEFAULT_FE_PER_EU`。

### 3.1 取整选择

| 场景 | 推荐 |
|:---|:---|
| EU 包向 FE 机器 **送电** | `euToFeCeil`（与 AE2 一致，避免欠送） |
| FE 存量 **折算显示 EU** | `feToEu` / 或 `euToFeFloor` 反向展示 |
| simulate 后再 execute | 先 `insert(..., simulate=true)` 得 accepted FE，再 `residualEuAfterFeTransfer` |

---

## 4. `PlatformEnergyBridge`（SPI）

签名单位：**外部 FE `long`**（见接口 Javadoc）。

| 方法 | Forge 映射 |
|:---|:---|
| `canReceive(be, side)` | cap 存在且 `IEnergyStorage#canReceive()` |
| `canExtract(be, side)` | cap 存在且 `IEnergyStorage#canExtract()` |
| `insert(be, side, amount, simulate)` | `receiveEnergy(clamp(amount), simulate)`；无 cap / 不可收 → `0` |
| `extract(be, side, max, simulate)` | `extractEnergy(clamp(max), simulate)`；无 cap / 不可抽 → `0` |

实现类：`PlatformEnergyBridgeForge`。

- `side`：传给 `getCapability(ENERGY, side)`；可为 `null`（由目标 BE 解释）。  
- **不**把 IC 机器注册为 FE 源/汇；仅查询/操作**已有** `ENERGY` cap 的邻居。  
- 无 cap 或 `be == null` → receive/extract 能力 false / 传输 0（**不**抛 `UnsupportedOperationException`）。

---

## 5. 与 AE2 / 其它集成

| 路径 | 是否用 SPI | 转换 |
|:---|:---|:---|
| AE2 grid `injectPower` | 否（AE API） | `EU_TO_AE_RATIO` = `DEFAULT_FE_PER_EU` |
| AE2 FE fallback `IEnergyStorage` | 可选未来改走 SPI；G2.8 先共享 **Math** | `euToFeCeil` / `feToEu` / `residualEuAfterFeTransfer` |
| 通用邻接 FE 探查 | **是** `PlatformServices.energy()` | 调用方先 Math 再 SPI，或只做 FE 侧 |

---

## 6. 测例映射

| 测例（`EnergyBridgeMathTest`） | 覆盖 |
|:---|:---|
| `euToFe_defaultRatio_ceilAndFloor` | 默认 2.0、ceil/floor、自定义 ratio |
| `feToEu_defaultRatio_andRoundTrip` | FE→EU、往返、奇数 FE 小数 EU |
| `residual_simulatePartialAccept` | FE/EU 残余、部分接受、全接受、拒绝 |
| `boundary_zeroAndInvalid` | 0/负、非法 ratio、int clamp |

---

## 7. 非目标与风险

**非目标（本 Unit 禁止）**：

- 将 IC 电缆/发电机/用电设备默认暴露为 FE  
- 大改 EnergyNet 拓扑或 calculator  
- 实现完整 `IC2RConfig` 能量桥 UI（仅文档预留）  
- Fabric transfer / NeoForge 多模块实现（SPI 已可接）

| 风险 | 说明 | 缓解 |
|:---|:---|:---|
| 比率误改 | 改默认 2.0 会动 AE2 吞吐 | 常量注释 + 本文档；测例钉死 2.0 |
| 双轨转换 | AE2 直算 vs Math 分叉 | AE2 已接 Math；新增路径只调 Math |
| int 溢出 | Forge energy 为 int | `clampToIntEnergy` |
| 误用 SPI 当电网 | 把 EU 当 FE 传入 | SPI Javadoc + 本文 §1 |
