# W1.4 Smell Exemptions

登记本 Unit 扫描后**有意保留**的命中，非未处理疏漏。

| 路径 | 模式 | 理由 |
|:---|:---|:---|
| `forge/model/DynamicBeModelForge.java` | `RandomSource.create(42L)` ×2 | 模型烘焙固定种子，保证 quad 稳定；非 tick 热路径 |
| `forge/model/MaskOverlayItemModel.java` | `RandomSource.create(42L)` ×2 | 同上 |
| `forge/model/WallModelForge.java` | `RandomSource.create(42L)` ×1 | 同上 |
| `core/item/tool/ItemObscurator.java` | `RandomSource.create(42L)` ×1 | 遮挡/模型相关固定种子取样；非 tick 热路径 |
| `core/proxy/SideProxyServer.java` | `e.printStackTrace(printWriter)` | 将堆栈写入 `StringWriter` 组装错误信息，非“唯一错误处理=printStackTrace”热路径 |
| `core/block/machine/tileentity/TileEntityChunkLoader.java` | `new RuntimeException(...).printStackTrace()` ×2 | 客户端误改状态的防御性诊断；非 tick 性能路径。后续可迁 `IC2R.log`，本 Unit 不扩 scope |

## 已清零类别（W1.4）

- `java.util.Vector` 配方/集合残留（含死字段删除）
- 无固定种子的 `RandomSource.create()`（tick、电网、工具方法、事件路径中有 `Level` 的调用）
