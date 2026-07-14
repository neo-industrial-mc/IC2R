# 流体 BE / Tank 对外 `FLUID_HANDLER` 契约（G2.5）

> **Work Unit**: G2.5  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §7.2；[phase2_closeout.md](phase2_closeout.md) G2.5；Golden [FL-\*](golden_suite.md)  
> **实现主路径**: `Ic2rFluidTank` 领域 API 保留；单罐一等 Forge 适配 `Ic2rFluidTankHandler`；多罐+侧向 BE cap 仍经 `Fluids` / `BlockFluidCapImpl`  
> **测例**: `src/test/java/me/halfcooler/ic2r/fluid/FluidTransferMathTest.java`、`FluidHandlerMathTest.java`（纯逻辑；**不**构造 `FluidStack` / 真 Handler）

---

## 1. 对外路径总览（统一 fill / empty）

| 调用方 | 路径 | 语义 |
|:---|:---|:---|
| **机器内部 / GUI / 配方** | `Ic2rFluidTank#fillMb` / `#drainMb` / `*Unchecked` | 领域 mB API；`external=true` 尊重 `canFill`/`canDrain`；unchecked 绕过访问门闩 |
| **单罐 IFluidHandler** | `Ic2rFluidTank#getFluidHandler()` → `Ic2rFluidTankHandler` | 一罐一 handler；`fill`/`drain` 委托 tank（再委托 `FluidTransferMath`）；**无** side 掩码 |
| **命名罐快捷** | `Fluids#getTankHandler(name)` | 同上，按 identifier 取 managed tank |
| **BE cap（管道）** | `getCapability(FLUID_HANDLER, facing)` → `BlockFluidCapImpl` / `LazyBlockFluidCapImpl` | **多罐** + **side mask**（`inputSides`/`outputSides`）；内部仍走 `Ic2rFluidBlock`/`Fluids#fillMb|drainMb` 聚合 |
| **物品流体** | `ItemFluidCapImpl` / `Ic2rFluidItem` | 物品 cap；不经 tank handler（本 Unit 不改） |
| **跨方块读第三方** | `EnvFluidHandlerForge` 查邻居 `FLUID_HANDLER` | 读外部；本模组对外写出以上表为准 |

附着点：`EventHandlerForge#onAttachBlockEntityCapabilities`（`FluidBeBridge` → 立即 `BlockFluidCapImpl`；否则 `LazyBlockFluidCapImpl` 解析 `Fluids` 组件）。

**与库存类比（W2.1 / G2.1）**：

| 库存 | 流体 |
|:---|:---|
| `InvSlot` | `Ic2rFluidTank` |
| `InvSlotItemHandler` | `Ic2rFluidTankHandler` |
| `InvSlotTransferMath` | `FluidTransferMath` |
| `CombinedInvWrapper` + sided | `BlockFluidCapImpl` multi-tank + side masks |
| `item_handler_contract.md` | 本文档 |

---

## 2. 单罐适配：`Ic2rFluidTankHandler`

每个 `Ic2rFluidTank` 懒创建 `Ic2rFluidTankHandler`（`getFluidHandler()`）。存储仍在 tank 的 `fluidStack`，**不是**独立 `FluidTank` 副本。

| API | 门闩 / 行为 |
|:---|:---|
| `getTanks()` | 恒为 **1** |
| `getFluidInTank(0)` | 当前内容 → Forge `FluidStack`（空 → `EMPTY`；NBT 尽量保留） |
| `getTankCapacity(0)` | `tank.getCapacity()` |
| `isFluidValid(0, stack)` | 非空 offer 且 `tank.canFill(fluid)`（**不**检查剩余空间） |
| `fill(resource, action)` | 空 resource → 0；否则 `tank.fillMb(domain, simulate)`（external 门闩 + 兼容 + 容量） |
| `drain(max, action)` | `max≤0` → empty；否则 `tank.drainMb(max, simulate)` |
| `drain(resource, action)` | 空 resource → empty；流体匹配 + `canDrain` 后按 request 量抽出 |

纯算术与门闩：`FluidTransferMath`（无 MC 类型），供单测镜像。

### 2.1 simulate vs execute

| `FluidAction` | tank 参数 | 存储 |
|:---|:---|:---|
| `SIMULATE` | `simulate=true` | **不**改 `fluidStack` / amount |
| `EXECUTE` | `simulate=false` | 提交 fill 增 / drain 减；全抽空 → 清空 |

### 2.2 external 门闩（默认 cap / 管道路径）

| 操作 | 条件 | 结果 |
|:---|:---|:---|
| fill | offer 空 | 0 |
| fill | 非空罐且不同流体 | 0 |
| fill | `canFill(fluid)==false` | 0 |
| fill | 满罐 | 0 |
| drain amount | `canDrain()==false` | empty / 0 |
| drain stack | 罐空 / request 空 / 流体不匹配 | empty / 0 |
| 内部 `*Unchecked` | `external=false` | 绕过 canFill/canDrain（**不**经 Handler；Handler 始终 external） |

`Fluids.InternalFluidTank` 覆盖 `canFill(Fluid)`（accepted predicate）；side 过滤在 **组件聚合层**，不在单罐 Handler。

---

## 3. 多罐 + 侧向：`BlockFluidCapImpl`

| 行为 | 说明 |
|:---|:---|
| `getTanks()` | 当前 facing 上 `drainSideMask \| fillSideMask` 非零的罐数 |
| `fill` / `drain` | 按注册顺序扫罐；仅 `canFill(fluid, side)` / `canDrain(side)` 的罐参与 |
| null facing | 使用 `sides[0]`（DOWN）视图 — **历史行为**，管道宜传真实 facing |
| 与单罐 Handler 关系 | 聚合仍走 `Fluids` 领域 fill/drain（保证 `setChanged` 与 side）；**未**逐罐替换为 `Ic2rFluidTankHandler` 组合（避免双路径行为漂移；见 residual） |

---

## 4. 与 `FluidTransferMath` 对照（可测不变量）

| 不变量 | Math API | Handler / Tank 对应 |
|:---|:---|:---|
| 兼容：空罐或同流体 | `tankFluidsCompatible` | fill 早退 0 |
| fill 访问 | `fillAccessAllowed` | external + canFill |
| drain 访问 | `drainAccessAllowed` / `drainByStackAccessAllowed` | external + canDrain + match |
| 可填量 | `fillableMb` / `fillMbDelegated` | `fill` 返回值 |
| 可抽量 | `drainableMb` / `drainMbDelegated` / `drainMbByStackDelegated` | `drain` 返回 amount |
| 残余 offer | `remainingOfferAfterFill` | 管道未灌入量 |
| simulate 不改存 | `storedAfterFill` / `storedAfterDrain` | SIMULATE 分支 |
| 空 resource | offer/request ≤0 | Handler 早退（Math 亦 0） |

---

## 5. 测试策略与覆盖 cap

| 层 | 状态 | 说明 |
|:---|:---|:---|
| `FluidTransferMath` 委托门闩 | **测绿**（W2.2 / G2.4） | `FluidTransferMathTest` |
| 虚拟罐 fill→drain 管道序列 / Handler 门闩 | **测绿**（G2.5） | `FluidHandlerMathTest` 镜像适配器；不加载 `Ic2rFluidTankHandler` |
| `Ic2rFluidTankHandler` 本体行覆盖 | **0%（residual）** | 依赖 `FluidStack` / Forge / ENV 工厂；CI 无 MC bootstrap |
| `BlockFluidCapImpl` / Lazy 附着 | **0% 运行时（residual）** | 需 BE + capability 事件；契约以上表为准 |
| 真管道 / 流体导管 e2e | **不做（residual gap）** | 禁止本 Unit 引入管道模组硬测 |

---

## 6. Residual gap（诚实边界）

1. **无** 真实管道模组 / 原版水桶对 BE 的集成或 GameTest。  
2. **`Ic2rFluidTankHandler` 与 cap 附着**依赖运行时；契约由 Math + 本文档固定。  
3. **多罐 BE cap 未**改为「若干 `Ic2rFluidTankHandler` 拼接」；仍走 `Fluids` 聚合，行为与 side mask 与历史一致。  
4. null facing 落在 `sides[0]` 的历史语义未改。  
5. 物品 `FLUID_HANDLER_ITEM` 与世界流体抽取不在本 Unit 范围。  
6. 阶段 2 包级 fluid 覆盖率门槛（G2.4）**不**因本 Unit 关闭。

---

## 7. 相关路径

| 路径 | 角色 |
|:---|:---|
| `core/fluid/Ic2rFluidTankHandler.java` | 单罐 Forge 适配 |
| `core/fluid/Ic2rFluidTank.java` | 领域罐 + `getFluidHandler` |
| `core/fluid/FluidTransferMath.java` | 纯逻辑门闩与余量 |
| `core/block/comp/Fluids.java` | 多罐组件 + side + `getTankHandler` |
| `forge/BlockFluidCapImpl.java` | BE multi-tank cap |
| `forge/LazyBlockFluidCapImpl.java` | 构造期延迟解析 Fluids |
| `forge/EventHandlerForge.java` | cap 附着 |
| `src/test/.../fluid/FluidHandlerMathTest.java` | Handler 镜像测例 |
| `src/test/.../fluid/FluidTransferMathTest.java` | Math 测例 |
