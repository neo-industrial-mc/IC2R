# IC2R `api/` 面分类（G3.9 / §8.3）

> **Work Unit**: G3.9  
> **日期**: 2026-07-14  
> **性质**: **文档分类 only** — **不**强制搬包 / 改 visibility  
> **路径根**: `me.halfcooler.ic2r.api`（`src/main/java/me/halfcooler/ic2r/api/**`）  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §8.3「api 面仅保留外部模组真正需要的接口」  
> **收口**: [phase3_closeout.md](phase3_closeout.md) §17

---

## 0. 约定

| 标签 | 含义 |
|:---|:---|
| **稳定对外** | 外部模组**可能**依赖的公开契约（能量、配方、升级、作物卡注册等）；变更需兼容或迁移说明 |
| **内部迁移中** | 名义在 `api/` 但实为本模组内部 / 历史 IC 面；应**逐渐**收进 `core` 或标注兼容层，不鼓励新外部依赖 |
| **混合** | 同包内两类并存；以子路径为准 |

**本文件不**改变编译可见性；后续域重写时可对照迁移。

---

## 1. 稳定对外（优先保持契约稳定）

| 包 / 类型簇 | 说明 |
|:---|:---|
| `api/energy/**`（主体） | `IEnergySink` / `Source` / `Conductor` / `IEnergyNet` / Load·Unload 事件、`EnergyNet` 门面、tile 用电侧接口 — 电网互操作主面 |
| `api/energy/prefab/**`、`profile/**` | 外部实现电网节点的预制与档位展示 |
| `api/recipe/**`（主体） | `IMachineRecipeManager*`、`IRecipeInput*`、`Recipes` 门面、`MachineRecipe*` — 配方与机台对接 |
| `api/item/IElectricItem*`、`ElectricItem`、`ISpecialElectricItem`、`IBackupElectricItemManager` | 电量物品互操作 |
| `api/upgrade/**` | `IUpgradeItem` / `IUpgradableBlock` / `UpgradeRegistry` / 各升级能力接口 |
| `api/reactor/**` | `IReactor` / `IReactorComponent` / chamber — 外部反应堆组件 |
| `api/crops/**`（`CropCard`、`Crops`、`ICropTile`、`ICropSeed` 等） | 作物扩展注册与地块查询 |
| `api/tile/IEnergyStorage`、`IWrenchAble`、`ExplosionWhitelist` | 常见方块互操作 |
| `api/info/**` | 探针/信息提供（Jade 等集成侧可依赖） |

---

## 2. 内部迁移中（应逐渐 internal / 兼容层）

| 包 / 类型簇 | 说明 |
|:---|:---|
| `api/network/**` | `INetworkDataProvider` / `getNetworkedFields` 风格、GrowingBuffer、自定义编码器 — 与 TeUpdate 反射同步绑定；随 Sync 切主应收窄 |
| `api/util/CoreAccess*`、`IKeyboard`、`Keys` | 模组内核访问与按键；外部很少应依赖 |
| `api/entity/**` | 炸药/船等实体 API；偏实现泄漏 |
| `api/event/ExplosionEvent`、`RetextureEvent` | 事件面可保留少量；整体与 core 事件耦合 |
| `api/block/**`（`BreakableBlock`、`IIdProvider`、container 槽） | 内部方块/容器辅助 |
| `api/item` 中工具向接口 | `IMiningDrill`、`INanoSaberState`、`ITerraformingBP`、`IKineticRotor`、`IHazmatLike`、`IMetalArmor`、`IBoxable`、`IDebuggable`、`IEnhancedOverlayProvider`、`IEntityAttackableItem`、`IItemHud*`、`ItemWrapper`、`HudMode`、`BlockBreakableItem` 等 — 多为本模组物品实现面，外部扩展少 |
| `api/tile/RotorRegistry`、`RetexturableBlock`、`StainableBlock`、`IRotorProvider` | 本模组动能/贴图染色配套 |
| `api/sound/**` | 挥击音效等窄面 |
| `api/util/FluidContainerOutputMode` | 流体容器模式枚举；可随 fluid SPI 内聚 |

---

## 3. 混合 / 备注

| 项 | 说明 |
|:---|:---|
| `api/energy/tile/**` | 接口 **稳定对外**；部分 prefab 实现细节偏内部 |
| `api/recipe` 中 canner/fermenter 等专用 manager | 对外扩展可能用，但实现与 residual 管理器强绑 — 契约可留、实现侧收敛 |
| Origin | `api/energy`、`api/recipe`、`api/network` 等主判据仍 **residual**（见 [origin.md](origin.md)）；本分类是**工程对外策略**，不改 Origin 版权标注 |

---

## 4. 后续动作（非本 Unit）

1. 新外部文档 / 示例**只**引用「稳定对外」表。  
2. 域重写时把「内部迁移中」移出 `api` 或标 `@Deprecated` + 兼容桥。  
3. **禁止**一次搬空 `api/` 或为分类改包名引发大规模 break。  
