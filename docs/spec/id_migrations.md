# ID / NBT / 网络键迁移表

> **依据**: [Modernization_Project.md](../Modernization_Project.md) §3.2  
> **性质**: 破坏性或兼容期重命名登记；读档须兼容旧键一版（`LegacyNbt`）  
> **首次登记**: W1.5 NBT 试点  

---

## 1. W1.5 — NBT 试点（标准机 + Energy）

| 种类 | 旧键 | 新键 | 作用域 | 读策略 | 写策略 | 备注 |
|:---|:---|:---|:---|:---|:---|:---|
| NBT | `storage` | `energy_buffer` | `core.block.comp.Energy` | 优先 `energy_buffer`，缺失则 `storage` | **仅** `energy_buffer` | 语义化单段小写 → snake 语义名 |
| NBT | `energyBuffer` | `energy_buffer` | `TileEntityConversionGenerator` | 优先 `energy_buffer`，缺失则 `energyBuffer` | **仅** `energy_buffer` | camelCase → snake_case |
| NBT | `progress` | `progress` | `TileEntityStandardMachine` | 读 `progress` | 写 `progress` | **已是**单段小写，保留；无 camelCase 旧键 |
| 网络 | `guiProgress`（反射字段名） | wire `gui_progress` | 标准机 Sync（W1.2） | 双写至 TeUpdate 切主 | 现代 Sync 表 + 旧反射字段 | **非本 Unit 新增**；网络 LEGACY 字段名保持 |

### 明确不在本表本批（W1.5 时）

- ~~反应堆 `TileEntityNuclearReactorElectric` 的 `energyBuffer`~~ → **见 §3 G1.5**
- 作物 / 动能 / 配置键 / 组件 ID 等全库 camelCase
- `BasicEnergyTile` 物品式 `energy` 键（API prefab，未纳入试点）

### 实现入口

| 工具 / 类 | 路径 |
|:---|:---|
| `LegacyNbt` | `src/main/java/me/halfcooler/ic2r/core/util/LegacyNbt.java` |
| Energy 组件 | `Energy.NBT_ENERGY_BUFFER` / `LEGACY_NBT_STORAGE` + `readEnergyBuffer` / `writeEnergyBuffer` |
| 转换发电机 | `TileEntityConversionGenerator.NBT_ENERGY_BUFFER` / `LEGACY_NBT_ENERGY_BUFFER` |
| 标准机进度 | `TileEntityStandardMachine.NBT_PROGRESS` |
| 测试 | `src/test/java/me/halfcooler/ic2r/nbt/**` |

---

## 2. 后续追加

后续 Work Unit 在本文件追加小节（W1.x / W2.x / G1.x…），勿静默改键。

---

## 3. G1.5 — snake_case 扩域（naming_audit P0 邻接）

> **范围**: W1.5 试点外的 3 处跨边界 camelCase；**非**全库 rename。  
> **策略**: 写只写新键；读兼容旧键；网络 live 继续发 legacy 名（G1.1），取值走 Sync。

| 种类 | 旧键 | 新键 | 作用域 | 读策略 | 写策略 | 备注 |
|:---|:---|:---|:---|:---|:---|:---|
| NBT | `energyBuffer` | `energy_buffer` | `TileEntityNuclearReactorElectric` | 优先 `energy_buffer`，缺失则 `energyBuffer` | **仅** `energy_buffer` | 与 ConversionGenerator / Energy 一致 |
| 网络 | `guiProgress`（反射字段名） | wire `gui_progress` | `TileEntityBatchCrafter` Sync | TeUpdate 名仍 `guiProgress`；Sync alias | Sync getter/setter | `recipeOutput` **未**上 Sync（无 ItemStack SyncCodec）；仍反射 |
| NBT | `progress` | `progress` | `TileEntityBatchCrafter` | 读 `progress` | 写 `progress` | 已是单段小写；经 `NBT_PROGRESS` 常量化 |
| NBT | `redstoneMode` | `redstone_mode` | `TileEntityElectricBlock`（含 Chargepad 子类） | 优先 `redstone_mode`，缺失则 `redstoneMode` | **仅** `redstone_mode` | 存档敏感 |
| 网络 | `redstoneMode`（反射字段名） | wire `redstone_mode` | `TileEntityElectricBlock` Sync | TeUpdate 名仍 `redstoneMode`；Sync alias | Sync getter/setter | ContainerElectric / Chargepad 仍 list legacy 名 |

### 明确不在本批

- BatchCrafter `recipeOutput` → `recipe_output`（待 ItemStack `SyncCodec` / DataEncoder 桥）
- 反应堆其它网络字段（`maxHeat` / `EmitHeat` / tanks 等）
- 作物 / 动能 / 组件 ID / 配置键全库

### 实现入口

| 工具 / 类 | 路径 |
|:---|:---|
| 反应堆 NBT | `TileEntityNuclearReactorElectric.NBT_ENERGY_BUFFER` / `readEnergyBufferNbt` / `writeEnergyBufferNbt` |
| BatchCrafter Sync | `TileEntityBatchCrafter.KEY_GUI_PROGRESS` / `bindBatchCrafterSync` / `LEGACY_GUI_PROGRESS_FIELD` |
| ElectricBlock NBT+Sync | `TileEntityElectricBlock.NBT_REDSTONE_MODE` / `KEY_REDSTONE_MODE` / `bindElectricBlockSync` |
| `LegacyNbt.getByte` | `LegacyNbt.java`（G1.5 新增） |
| 测试 | `EnergyNbtMigrationTest`（反应堆追加）、`ElectricBlockNbtMigrationTest`、`BatchCrafterSyncRoundTripTest`、`ElectricBlockSyncRoundTripTest` |
