# A40.0 — core/api Forge import 基线清单

> **Work Unit**: A40.0  
> **日期**: 2026-07-14  
> **基线 `mod_version`**: `20.1.40`  
> **对照**: [After-20-1-40-Moderize.md](../After-20-1-40-Moderize.md) A40.2 DoD  

扫描命令（PowerShell）：对 `src/main/java/me/halfcooler/ic2r/{core,api}` 匹配 `import net.minecraftforge`。

---

## 1. 汇总

| 范围 | 含 import 的文件数 | import 行约数 |
|:---|---:|---:|
| `core/**` | **31** | **54** |
| `api/**` | **6** | **10** |
| **合计** | **37** | **64** |

目标（A40.2）：`core`+`api` → **0** 文件，或 ≤3 条已登记豁免。

---

## 2. core 文件清单

| 路径 | 主要 Forge 类型 | 迁出建议类别 |
|:---|:---|:---|
| `core/block/DynamicBeModel.java` | `ModelData`, `ForgeRegistries` | client/model → forge 或 BuiltInRegistries |
| `core/block/invslot/InvSlotItemHandler.java` | `IItemHandlerModifiable`, `ItemHandlerHelper` | 适配器 → forge；core 留抽象 |
| `core/block/machine/tileentity/TileEntitySteamRepressurizer.java` | `ForgeRegistries`, `ITag` | vanilla tags / BuiltInRegistries |
| `core/block/tileentity/Ic2rTileEntity.java` | `Dist`, `OnlyIn`, `ForgeRegistries` | client 切片 + vanilla registry |
| `core/block/tileentity/TileEntityInventory.java` | `LazyOptional`, `IItemHandler*`, `CombinedInvWrapper` | capability 附着 → forge |
| `core/command/CommandIc2r.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/crop/Ic2rCrops.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/crop/TileEntityCrop.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/event/EventHandlerClient.java` | `ForgeRegistries` | client + registry |
| `core/fluid/Ic2rFluidTankHandler.java` | `FluidStack`, `IFluidHandler` | forge cap 适配 |
| `core/init/IC2RClientConfig.java` | `ForgeConfigSpec` | Spec 构建 → forge；或豁免 |
| `core/init/IC2RConfig.java` | `ForgeConfigSpec` | 同上 |
| `core/init/IC2RUuScanConfig.java` | `ForgeConfigSpec` | 同上 |
| `core/item/Ic2rBucketItem.java` | `ForgeEventFactory` | SPI / forge 钩子 |
| `core/item/armor/jetpack/JetpackHandler.java` | Dist/OnlyIn/事件总线大量 | **整类事件** → forge |
| `core/item/tool/ItemElectricToolChainsaw.java` | `IForgeShearable` | forge 接口实现外置或 wrapper |
| `core/item/tool/ItemObscurator.java` | `ModelData` | client/forge |
| `core/item/tool/ItemToolPainter.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/item/tool/ItemToolWrench.java` | Dist/OnlyIn, ForgeRegistries | client + registry |
| `core/network/DataEncoder.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/ref/Ic2rSoundEvents.java` | DeferredRegister/RegistryObject/IEventBus | 注册总线 → forge/`FmlMod` |
| `core/ref/blocks/Ic2rBlocksBuilding.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/ref/blocks/Ic2rBlocksCrops.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/ref/blocks/Ic2rBlocksGenerators.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/ref/blocks/Ic2rBlocksMachines.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/ref/blocks/Ic2rBlocksReactor.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/ref/blocks/Ic2rBlocksResources.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/ref/blocks/Ic2rBlocksStorage.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/ref/blocks/Ic2rBlocksWiring.java` | `ForgeRegistries` | BuiltInRegistries |
| `core/util/PumpUtil.java` | `IFluidBlock` | forge 桥或 vanilla fluid |
| `core/util/Util.java` | `ForgeRegistries` | BuiltInRegistries |

---

## 3. api 文件清单

| 路径 | 主要 Forge 类型 | 迁出建议 |
|:---|:---|:---|
| `api/crops/CropCard.java` | Dist, OnlyIn | 去注解或 client 钩子 |
| `api/crops/Crops.java` | `Event` | 事件类型迁 forge 或自研总线 |
| `api/energy/ProfileEvent.java` | `Event` | 同上 |
| `api/energy/event/EnergyTileEvent.java` | `LevelEvent` | 同上 |
| `api/event/ExplosionEvent.java` | LevelEvent, Cancelable | 同上 |
| `api/event/RetextureEvent.java` | LevelEvent, Cancelable | 同上 |

---

## 4. 分类计数（便于 A40.2 分批）

| 类别 | 文件（约） |
|:---|---:|
| ForgeRegistries 查询 | 18 |
| Config Spec | 3 |
| Item/Fluid capability 类型 | 3 |
| Client Dist/OnlyIn/ModelData | 5 |
| 事件总线 / Event 基类（含 api） | 6+ |
| DeferredRegister 声音 | 1 |
| 其它（Shearable、ForgeEventFactory、IFluidBlock） | 3 |

---

## 5. A40.0 结论

- 基线版本确认：**20.1.40**  
- 清单已冻结于本文件，供 A40.2 对照清零。  
- 无玩法代码变更。  
