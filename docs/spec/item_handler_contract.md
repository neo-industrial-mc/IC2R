# 标准机 / 库存 BE 对外 `ITEM_HANDLER` 契约（G2.1）

> **Work Unit**: G2.1  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §7.1；[phase2_closeout.md](phase2_closeout.md) G2.1  
> **实现主路径**: `InvSlot` 领域 API 保留；对外 Forge cap 经 `InvSlotItemHandler` + `TileEntityInventory` / `EventHandlerForge`  
> **测例**: `src/test/java/me/halfcooler/ic2r/inv/InvSlotHandlerMathTest.java`（纯逻辑，镜像 Handler 规则；**不**构造 `ItemStack` / 真 Handler）

---

## 1. 能力附着总览

| 查询 | 谁提供 | 视图 | 侧向过滤 |
|:---|:---|:---|:---|
| `getCapability(ITEM_HANDLER, **null**)` | `TileEntityInventory` → `getInvSlotItemHandlerCap()` | 全部 `InvSlot` 的 `InvSlotItemHandler` 经 `CombinedInvWrapper` **按注册顺序**拼接 | **无** side / `preferredSide` 过滤；仅 `Access` + `accepts` + 堆叠算术 |
| `getCapability(ITEM_HANDLER, **facing ≠ null**)` | 同一 cap provider 内 `SidedInvWrapper.create(teInv, …)` | 包装 `WorldlyContainer`（即 TE 自身） | `canPlaceItemThroughFace` / `canTakeItemThroughFace` + `preferredSide` 优先匹配 |
| 非 `TileEntityInventory` 的 `WorldlyContainer` | 旧路径 sided wrapper | 仅 **facing ≠ null** 时非 empty | null facing → empty |
| 普通 `Container` | `InvWrapper` | 无方向区分 | facing 忽略，凡 `ITEM_HANDLER` 均暴露 |

附着点：`EventHandlerForge`（`AttachCapabilitiesEvent<BlockEntity>`）。  
缓存：`TileEntityInventory` 的 combined `LazyOptional` 在 `addInventorySlot` / `invalidateCaps` 时 invalidate。

**重要差异（null vs facing）**：

- **null**：管道/AE2 若以「无侧」问 cap，看到的是**全槽组合**视图；升级槽 / 放电槽虽在索引空间内，但因 `Access.NONE` **双向拒** insert/extract。  
- **facing**：漏斗、带侧向的管道走 `SidedInvWrapper`；输入优先 `InvSide.TOP`、输出优先 `BOTTOM` 等 `preferredSide` 逻辑在 `TileEntityInventory` 中实现。

---

## 2. 单槽适配：`InvSlotItemHandler`

每个 `InvSlot` 懒创建一个 `InvSlotItemHandler`（`InvSlot#getItemHandler()`）。存储仍在 `InvSlot` 的 `contents[]`，**不是**独立 `ItemStackHandler` 副本。

| API | 门闩 / 行为 |
|:---|:---|
| `insertItem(index, stack, simulate)` | `allowsInsert(canInput, accepts(stack), stack.isEmpty)`；再 `insertableCount`（slotLimit、maxStack、可合并）；`simulate=true` **不** `put` |
| `extractItem(index, amount, simulate)` | `allowsExtract(canOutput, slotEmpty)`；再 `extractableCount`；`simulate=true` **不** 改槽 |
| `isItemValid` | 同 insert 门闩（不含堆叠空间） |
| `setStackInSlot` | **直写**，不重检 `accepts`（GUI/内部）；自动化应走 `insertItem` |
| `getSlotLimit` | `InvSlot.getStackSizeLimit()` |

纯算术与门闩抽到 `InvSlotTransferMath`（无 MC 类型），供单测镜像。

### 2.1 `InvSlot.Access` ↔ 自动化方向

| Access | `canInput` | `canOutput` | 典型用途 |
|:---|:---|:---|:---|
| `I` | ✓ | ✗（见 §2.2 例外） | 加工输入（`InvSlotConsumable` / Processable） |
| `O` | ✗ | ✓ | 产物（`InvSlotOutput`；且 `accepts` 恒 false） |
| `IO` | ✓ | ✓ | 双向槽 |
| `NONE` | ✗ | ✗ | 升级（`InvSlotUpgrade`）、标准机电放电（`InvSlotDischarge` 构造为 `NONE`） |

### 2.2 `InvSlotConsumable#canOutput` 例外

当 `access != NONE`、槽非空、且 **当前栈不再 `accepts`** 时，`canOutput()` 可为 true（允许把非法/拒收物品抽出）。  
`InvSlotItemHandler` 调用的是运行时 `slot.canOutput()`，故会继承该语义。  
**纯 Math 测例**只断言「有效 `canOutput` 标志」；不在无 bootstrap 下实例化 `InvSlotConsumable`。

---

## 3. 标准机组合索引布局（Macerator 试点）

构造注册顺序 = combined handler 索引顺序（`addInventorySlot`）：

| 顺序 | 组 | 大小 | 字段 | Access | preferredSide（sided 用） |
|:---:|:---:|:---:|:---|:---|:---|
| 0 | discharge | 1 | `TileEntityElectricMachine.dischargeSlot` | `NONE` | `ANY` |
| 1 | output | 1* | `TileEntityStandardMachine.outputSlot` | `O` | `BOTTOM` |
| 2 | upgrade | 4 | `upgradeSlot` | `NONE` | 默认 `ANY` |
| 3 | input | 1 | `TileEntityMacerator` 内 `inputSlot`（Processable） | `I` | `TOP` |

\* Macerator 构造 `outputSlots=1`（`super(..., 2, 300, 1)` → 能量参数后 output 槽数 1）。

**合计 slots = 7**。组合下标：

| combined | group | local | 角色 | 管道 insert | 管道 extract |
|:---:|:---:|:---:|:---|:---|:---|
| 0 | 0 | 0 | discharge | 拒 | 拒 |
| 1 | 1 | 0 | output | 拒（Access.O + accepts=false） | 允 |
| 2…5 | 2 | 0…3 | upgrade | 拒 | 拒 |
| 6 | 3 | 0 | input | 允（若 `accepts`） | 拒* |

\* 除非 §2.2 拒收抽出。

定位算法：`InvSlotTransferMath.totalSlots` / `locateCombinedIndex` / `unpackGroup` / `unpackLocal`（与 `TileEntityInventory#locateInvSlot` 同构）。

### 3.1 Macerator 输入 / 输出语义（自动化视角）

1. **输入（index 6，null 视图）**：只接收配方可接受物品；漏斗/管道不可从输入槽抽走仍合法的原料。  
2. **输出（index 1）**：机器逻辑经 `InvSlotOutput#add` 写入（绕过 `accepts`）；自动化 **只 extract、不 insert**。  
3. **升级 / 放电**：不在自动化路径暴露有效 I/O（Access.NONE）。玩家 GUI 仍可操作领域 API / `setStackInSlot`。  
4. **加工闭环（契约期望，非本 Unit e2e）**：外部 insert→input → 机器 tick 消耗 input 写入 output → 外部 extract←output。单测用虚拟槽序列镜像该闭环的 **门闩与余量**，不跑 TE tick。

---

## 4. 与 `InvSlotTransferMath` 对照（可测不变量）

| 不变量 | Math API | Handler 对应 |
|:---|:---|:---|
| 插入需 input + accepts + 非空栈 | `allowsInsert` | `insertItem` 早退原栈 |
| 抽出需 output + 非空槽 | `allowsExtract` | `extractItem` → EMPTY |
| 可插入量 = min(空间, 入量)，空间 = min(slotLimit, maxStack)−existing | `insertableCount` | 同上 |
| 抽出量 = min(existing, request, maxStack) | `extractableCount` | 同上 |
| simulate 不改状态 | 由测例虚拟槽断言；Math 本身无状态 | `simulate` 分支跳过 `put` |
| 组合下标 ↔ (group, local) | `locateCombinedIndex` | `CombinedInvWrapper` 拼接顺序 |

---

## 5. 测试策略与覆盖 cap

| 层 | 状态 | 说明 |
|:---|:---|:---|
| `InvSlotTransferMath` + `InvSlot.Access` | **测绿**（G2.1 加深序列/布局/simulate） | 无 bootstrap |
| 虚拟槽 insert→extract 序列 | **测绿**（同 `InvSlotHandlerMathTest`） | 镜像 Handler 算法，非类加载 `InvSlotItemHandler` |
| `InvSlotItemHandler` 本体行覆盖 | **0%（residual）** | 依赖 `ItemStack` / `ItemHandlerHelper` / Forge；CI 无 MC bootstrap |
| `TileEntityInventory` cap 附着 / `EventHandlerForge` | **0% 运行时（residual）** | 需 BE + capability 事件；文档以上述契约为准 |
| 真管道 / 漏斗 / AE2 e2e | **不做（residual gap）** | CI 无实机与 AE2 依赖硬测；禁止本 Unit 引入 AE2 测试依赖 |

---

## 6. Residual gap（诚实边界）

1. **无** 真实 AE2 导入总线 / 管道模组 / 原版漏斗的集成或 GameTest。  
2. **`InvSlotItemHandler` 与 cap 附着**仍依赖运行时；契约由 Math + 本文档固定，避免「假绿」覆盖率。  
3. Sided `preferredSide` 优先算法在 `TileEntityInventory`；本 Unit **未** 对其做无 Level 的纯逻辑抽出（后续 G2.4 可选）。  
4. `InvSlotConsumable` 拒收抽出与 `accepts` 的物品级行为需 Item/配方，不在本 Math 套件。  
5. 阶段 2 包级 inv 覆盖率门槛（§4.5 / G2.4）**不**因本 Unit 关闭。

---

## 7. 相关路径

| 路径 | 角色 |
|:---|:---|
| `core/block/invslot/InvSlotItemHandler.java` | 单槽 Forge 适配 |
| `core/block/invslot/InvSlotTransferMath.java` | 纯逻辑门闩与余量 |
| `core/block/invslot/InvSlot.java` | Access / storage / `getItemHandler` |
| `core/block/tileentity/TileEntityInventory.java` | 组合 cap、WorldlyContainer sided |
| `forge/EventHandlerForge.java` | null vs facing 分发 |
| `machine/tileentity/TileEntityMacerator.java` 等 | 标准机槽注册顺序样例 |
| `src/test/.../inv/InvSlotHandlerMathTest.java` | 文档化自动化测例 |
