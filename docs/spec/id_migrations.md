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

### 明确不在本表本批

- 反应堆 `TileEntityNuclearReactorElectric` 的 `energyBuffer`（P1，后续 Unit）
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

后续 Work Unit 在本文件追加小节（W1.x / W2.x…），勿静默改键。
