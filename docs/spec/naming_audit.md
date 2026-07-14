# IC2R 命名审计报告（W0.5）

> **Work Unit**: W0.5  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §3 命名规范、§3.4 命名审计清单、§5 阶段 0  
> **范围**: 只读扫描 `src/main/**`（Java + 主要资源 lang/注册相关）  
> **性质**: **抽样审计**，非全库穷尽；用于排序后续修复（W1.5 等），**本 Unit 不批量改名**  
> **扫描日期**: 2026-07-14  

---

## 1. 元信息

| 项 | 值 |
|:---|:---|
| 规范目标 | 跨边界字面量（注册 path、NBT 键、网络逻辑名、翻译键、配置键）→ `snake_case` |
| 非本审计范围 | Java 类型/方法/字段命名（`PascalCase`/`camelCase` 合法）；类型去 IC2 同源见 §3.3 / W0.6 Origin |
| 破坏性重命名约定 | 须写入 `docs/spec/id_migrations.md` + 读档兼容或 changelog（§3.2） |
| 相关后续 Unit | **W1.1–W1.2** Sync 抽象；**W1.5** NBT/网络字面量试点（标准机 + Energy）；注册拆分 W1.6 |

### 1.1 扫描方法

| 类别 | 方法 | 近似命中量（启发式） |
|:---|:---|:---|
| 注册 path / `register("...")` | 扫 `core/ref/*`、`Components.register`、含大写字母的 register 字面量 | 物品/方块注册 path **基本合规**；组件 ID 有 camelCase（约 3+） |
| `getNetworkedFields()` | 扫 `ret.add` / `fields.add` 中含内部大写的标识符 | **~139** 处 camelCase `add(...)` 行（含重复键跨类） |
| NBT `put*` / `get*` / `contains` 键 | 正则匹配 camelCase 字符串键 | **~534** 处（含 get/put 双写、非 NBT 误伤需人工复核） |
| 配置键 | `IC2RConfig` `define` / `defineInRange` | **~83** 处 camelCase |
| lang JSON | `en_us.json` 中 `ic2r.PascalCase...` 前缀 | **~362** 条 Pascal 段键；`block.`/`item.`/`container.` 段多数 snake |

判定规则（字面量）：

- **违规**：含内部大写的标识符（`guiProgress`、`energyBuffer`），或段内 Pascal（`ic2r.AdvMiner...`）
- **合规单段**：`active`、`energy`、`storage`、`progress`、`crop`（全小写、无分隔，可保留）
- **异常**：`EmitHeat`（首字母大写）、`teBlk=` 伪字段协议、`maxHeatEmitpeerTick` 拼写残留

---

## 2. 按类别抽样表

优先级：

| 级 | 含义 |
|:---|:---|
| **P0** | 试点域 / 高耦合同步或存档；W1.1–W1.5 应优先处理 |
| **P1** | 广泛存在；需迁移表或兼容层，可分批 |
| **P2** | 低风险、展示/配置/文案；可后置或随 UI 重写 |

路径相对于仓库根；行号为扫描时源码位置，后续编辑可能漂移。

### 2.1 注册 path / ResourceLocation / 内部 register ID

| 类别 | 示例字面量 | 文件路径:行或类 | 建议新名 | 优先级 | 备注 |
|:---|:---|:---|:---|:---|:---|
| 方块注册 | `"geo_generator"` 等 | `src/main/java/me/halfcooler/ic2r/core/ref/Ic2rBlocks.java`（如 L225） | （保持） | — | **抽查结论：合规**。`register("snake_case", ...)` + `IC2R.getIdentifier(name)` |
| 物品注册 | `"lead_ore"` 等 | `src/main/java/me/halfcooler/ic2r/core/ref/Ic2rItems.java` | （保持） | — | **抽查结论：合规** |
| 配方序列化 | `"centrifuge"` | `src/main/java/me/halfcooler/ic2r/core/ref/Ic2rRecipeSerializers.java:30` | （保持） | — | path 合规；同文件 `intMeta("minHeat")` 为 **配方 meta 键** camelCase → 见 NBT/配方表 |
| 组件 ID | `"fluidReactorLookup"` | `src/main/java/me/halfcooler/ic2r/core/block/comp/Components.java:18` | `fluid_reactor_lookup` | P1 | 写入 TE 组件 NBT map 时作 ID；改名需兼容 |
| 组件 ID | `"redstoneEmitter"` | `Components.java:22` | `redstone_emitter` | P1 | 同上 |
| 组件 ID | `"comparatorEmitter"` | `Components.java:23` | `comparator_emitter` | P1 | 同上 |
| 组件 ID | `"energy"` / `"fluid"` / `"process"` | `Components.java:16–21` | （保持） | — | 单段小写，合规 |
| ResourceLocation 纹理 | `"blocks/crop/" + id + "_" + size` | `core/crop/Ic2rCropCard.java` 等 | （保持） | — | 动态拼接；依赖 crop `getId()` 为小写 |
| 方块/物品 RL | 未见 camelCase path 注册 | `Ic2rBlocks` / `Ic2rItems` 全表抽查 | — | — | **未发现** `register("geoGenerator")` 类违规 |

### 2.2 `getNetworkedFields()` 网络字段名（反射字段名直出）

> 规范：禁止 Java 字段名字符串直出；目标为 `snake_case` 逻辑键 + 编解码（§3.1、附录 A：`rotationSpeed` → `rotation_speed`）。  
> 机制：`NetworkManager` 按字符串反射读写；改名与 **W1.1 Sync 抽象** 绑定更安全。

| 类别 | 示例字面量 | 文件路径:行或类 | 建议新名 | 优先级 | 备注 |
|:---|:---|:---|:---|:---|:---|
| 标准机 GUI | `"guiProgress"` | `.../machine/container/ContainerStandardMachine.java:55` | `gui_progress` | **P0** | W1.2/W1.5 试点；附录 A 同类 |
| 批处理 | `"guiProgress"`, `"recipeOutput"` | `.../ContainerBatchCrafter.java:80–81` | `gui_progress`, `recipe_output` | **P0** | 与标准机同模式 |
| 动能 | `"rotationSpeed"`, `"rotorSlot"` | `.../TileEntityWindKineticGenerator.java:147–148` 等 | `rotation_speed`, `rotor_slot` | P1 | 文档示例字段 |
| 动能 | `"rotationSpeed"`, `"rotorSlot"` | `.../TileEntityWaterKineticGenerator.java:233–234` | 同上 | P1 | |
| 热机 | `"transmitHeat"`, `"maxHeatEmitpeerTick"` | `ContainerRTHeatGenerator` / `Fluid` / `Electric` / `LiquidHeatExchanger` | `transmit_heat`, `max_heat_emit_per_tick` | P1 | 拼写 `Emitpeer` 一并修正 |
| 变压器 | `"configuredMode"`, `"transformMode"`, `"inputFlow"`, `"outputFlow"` | `.../wiring/ContainerTransformer.java:22–25` | `configured_mode` 等 | P1 | |
| 储能 | `"redstoneMode"` | `ContainerElectricBlock.java:33`, `ContainerChargepadBlock.java:25` | `redstone_mode` | P1 | NBT 同名键 |
| 反应堆 | `"maxHeat"`, `"EmitHeat"`, `"inputTank"`, `"outputTank"`, `"fluidCooled"` | `.../ContainerNuclearReactor.java:42–46` | `max_heat`, `emit_heat`, … | P1 | `EmitHeat` 大小写异常 |
| 作物 | `"currentAge"`, `"statGrowth"`, `"storageNutrients"`, … | `.../crop/TileEntityCrop.java:138–151` | `current_age`, `stat_growth`, … | P1 | 与 NBT 键同源 |
| 高级矿机 | `"mineTarget"`, `"silkTouch"` | `.../ContainerAdvMiner.java:37–39` | `mine_target`, `silk_touch` | P1 | |
| 蒸汽机 | `"distilledWaterTank"`, `"kuOutput"`, `"ventingSteam"`, `"isTurbineFilledWithWater"` | `.../ContainerSteamKineticGenerator.java:25–29` | snake_case | P1 | |
| 复制机 | `"uuProcessed"`, `"patternUu"`, `"patternEu"`, `"maxIndex"` | `ContainerReplicator` / `PatternStorage` / `Scanner` | snake_case | P1 | |
| 蒸汽发生器 | `"heatInput"`, `"inputMB"`, `"outputMB"`, `"systemHeat"`, `"outputFluid"` | `ContainerSteamGenerator.java` | snake_case | P1 | `MB` 建议 `input_mb` |
| O-Mat | `"paidFor"`, `"euBuffer"`, `"euOffer"`, `"totalTradeCount"`, `"usingPlayerCount"` | personal containers / TE | snake_case | P1 | |
| 基类协议 | `"teBlk=" + key` | `Ic2rTileEntity.java:341` | （协议重构，非简单改名） | P1 | 非纯字段名；与网络协议耦合 |
| 基类 | `"active"` | `Ic2rTileEntity.java:342` | （保持） | — | 已小写；附录 A 可保留 |
| 单段小写 | `"energy"`, `"progress"`, `"mode"`, `"heat"`, `"crop"` 等 | 多处 Container | （保持或语义化） | P2 | 合规；`energy` 若歧义可后改 `energy_buffer` |

**抽查结论**：网络面 **大面积 camelCase 字段名反射**；标准机 `guiProgress` 为 P0 切入点。单段小写字段无需为“改而改”。

### 2.3 NBT 键（CompoundTag put/get/contains）

> 规范：新键 `snake_case`；读档兼容旧键一版（`LegacyNbt`，§3.2）。

| 类别 | 示例字面量 | 文件路径:行或类 | 建议新名 | 优先级 | 备注 |
|:---|:---|:---|:---|:---|:---|
| Energy 组件 | `"storage"` | `.../block/comp/Energy.java:127,134` | （保持）或语义化 `energy`/`energy_stored` | **P0** | 已小写；W1.5 试点域，确认是否统一语义名 |
| Process 组件 | `"progress"` | `.../block/comp/Process.java:202,207` | （保持） | **P0** | 合规；试点对照 |
| 转换发电机 | `"energyBuffer"` | `TileEntityConversionGenerator.java:51,58` | `energy_buffer` | **P0**/P1 | 与附录 A 一致 |
| 反应堆 | `"energyBuffer"` | `TileEntityNuclearReactorElectric.java:222,231` | `energy_buffer` | P1 | |
| 作物 TE | `"statGrowth"`, `"storageWeedEX"`, `"currentAge"`, `"growthPoints"`, `"scanLevel"`, `"customData"`, … | `TileEntityCrop.java:93–129` | snake_case | P1 | **世界存档敏感** |
| 动能 | `"rotationSpeed"` | Wind/Water KineticGenerator | `rotation_speed` | P1 | 与网络字段一致 |
| 交易/私人 | `"ownerGameProfile"`, `"totalTradeCount"`, `"euOffer"`, `"paidFor"`, `"euBuffer"` | TradeOMat / EnergyOMat / PersonalChest | snake_case | P1 | |
| 高级矿机 | `"mineTargetX/Y/Z"`, `"silkTouch"` | `TileEntityAdvMiner.java:92–113` | `mine_target_x` 等 | P1 | 可合并为 `mine_target` 复合标签（破坏性更大） |
| 矿机 | `"lastMode"`, `"pumpMode"` | `TileEntityMiner.java` | `last_mode`, `pump_mode` | P1 | |
| 发酵机 | `"inputTank"`, `"outputTank"`, `"heatBuffer"` | `TileEntityFermenter.java` | snake_case | P1 | 流体罐名常兼网络字段 |
| 扫描器 | `"currentStack"` | `TileEntityScanner.java` | `current_stack` | P1 | |
| 复制机 | `"extraUuStored"`, `"uuProcessed"` | `TileEntityReplicator.java` | snake_case | P1 | |
| 传送器 | `"targetX/Y/Z"` | `TileEntityTeleporter.java` | `target_x` 等 | P1 | 物品 FrequencyTransmitter 同模式 |
| 发电机 | `"totalFuel"` | `TileEntityGenerator` / IronFurnace | `total_fuel` | P1 | |
| 储能 | `"redstoneMode"` | `TileEntityElectricBlock.java:79,87` | `redstone_mode` | P1 | |
| 遮挡 | `"colorMuls"` | `Obscuration.java:83` | `color_muls` | P2 | |
| 炸药实体 | `"inGround"`, `"stickX/Y/Z"` | `DynamiteEntity.java` | snake_case | P2 | 实体存档 |
| 喷气背包 | `"hasIC2RJetpack"` | `JetpackHandler.java:94,100` | `has_ic2r_jetpack` | P1 | 物品 NBT |
| 升级 | `"normalComp"`, `"extraComp"`, `"nbtSettings"` | HandHeldValueConfig / UpgradeSettings | snake_case | P1 | 物品 NBT |
| 电锯 | `"disableShear"` | `ItemElectricToolChainsaw` | `disable_shear` | P2 | |
| 频率发射器 | `"targetSet"`, `"targetJustSet"`, `"targetX/Y/Z"` | `ItemFrequencyTransmitter` | snake_case | P1 | |
| 遮挡工具 | `"refBlock"`, `"refVariant"`, `"refSide"`, `"refColorMuls"` | `ItemObscurator` | snake_case | P2 | |
| 配方 meta | `"minHeat"` | `TileEntityCentrifuge` + `Ic2rRecipeSerializers` | `min_heat` | P1 | **数据包/配方 JSON 可能引用** → 需 `id_migrations` |
| 流体罐槽位名 | `"inputTank"`, `"fluidTank"`, `"fuelSlot"`, `"CoilSlot"` | 多 TE `InvSlot` / `addTank` | snake_case | P1 | 常进 NBT；`CoilSlot` 大小写混用 |
| 反应堆槽 | `"coolantinputSlot"`, `"hotcoolinputSlot"`, `"coolantoutputSlot"` | `TileEntityNuclearReactorElectric` | 规范 snake + 下划线分段 | P2 | 全小写粘连，非 camel 但可读性差 |

**抽查结论**：NBT camelCase **遍布机器/作物/物品**；Energy/Process 核心键已是简单小写。W1.5 建议：标准机相关 + Energy 读写路径建立 `LegacyNbt` 样板，再推广。

### 2.4 配置键（TOML / Forge Config）

| 类别 | 示例字面量 | 文件路径:行或类 | 建议新名 | 优先级 | 备注 |
|:---|:---|:---|:---|:---|:---|
| 世界生成 | `"rubberTree"`, `"oreDensityFactor"`, `"treeDensityFactor"` | `core/init/IC2RConfig.java` | `rubber_tree`, `ore_density_factor` | P2 | 改名破坏用户 config；需默认迁移或双读 |
| 安全 | `"nukeExplosionPowerLimit"`, `"enableNuke"` | 同上 | snake_case | P2 | |
| 机器 | `"minerDischargeTier"`, `"energyRetainedInStorageBlockDrops"`, `"uuEnergyFactor"` | 同上 | snake_case | P2 | |
| 燃料表 | `"semiFluidOil"`, `"heatExchangerHotCoolant"` | 同上 | snake_case | P2 | |
| 段名 | `"steamRepressurizer"` | `IC2RConfig` push | `steam_repressurizer` | P2 | |

**抽查结论**：配置面几乎全 camelCase（~80+）；**不阻塞** 代码现代化核心，可单独 config 迁移 PR。

### 2.5 翻译键（lang JSON）抽样

| 类别 | 示例字面量 | 文件路径:行或类 | 建议新名 | 优先级 | 备注 |
|:---|:---|:---|:---|:---|:---|
| 物品/方块 | `block.ic2r.geo_generator` 等 | `assets/ic2r/lang/en_us.json` | （保持） | — | **抽查结论：path 段合规** |
| 容器标题 | `container.ic2r.wind_kinetic_generator` | 同上 | （保持） | — | snake |
| GUI 残留 IC2 风格 | `ic2r.AdvMiner.gui.switch.mode` | en_us.json ~L471+ | `gui.ic2r.advanced_miner.switch.mode` 等 | P2 | ~362 条 `ic2r.PascalCase...` |
| GUI | `ic2r.Canner.gui.switch.BottleLiquid` | 同上 | `gui.ic2r.canner.switch.bottle_liquid` | P2 | 段内混 Pascal + camel |
| GUI | `ic2r.EUStorage.gui.mod.redstone0` | 同上 | `gui.ic2r.eu_storage.mod.redstone_0` | P2 | |
| 死亡消息 | `death.attack.ic2r.reactorExplosion` | en_us.json:449 | `death.attack.ic2r.reactor_explosion` | P2 | camel 段 |
| 正向对照 | `ic2r.KineticGenerator.tooltip.max_output_voltage` | 同上 | 前缀仍 Pascal | P2 | 尾段已 snake，风格混杂 |

**抽查结论**：注册对齐的 `block.`/`item.`/`container.` **好**；历史 GUI 键 **大量** `ic2r.<ClassName>.*` 与现代 `gui.ic2r.*` 不一致。改键需同步 Java `Component.translatable` 与各语言文件。

### 2.6 其它字面量（可选）

| 类别 | 示例 | 位置 | 建议 | 优先级 | 备注 |
|:---|:---|:---|:---|:---|:---|
| InvSlot 名 | `"fuelSlot"`, `"CoilSlot"`, `"canInputSlot"` | 多 TE | snake_case | P1 | 常序列化进库存 NBT |
| 网络伪字段 | `"teBlk=" + ResourceLocation` | `Ic2rTileEntity` | 专用协议字段 | P1 | 勿当普通字段重命名 |

---

## 3. 修复优先级说明

### 3.1 建议顺序

| 顺序 | 内容 | 对应 Unit / 动作 | 是否需 `id_migrations` |
|:---|:---|:---|:---|
| 1 | 建立 Sync 键抽象，停止新增反射字段名 | **W1.1** | 否（新路径） |
| 2 | 标准机 `guiProgress` 等试点同步 | **W1.2** | 若 wire 名变更：是（网络侧，通常无档） |
| 3 | Energy + 标准机相关 NBT：`snake_case` 新键 + 旧键可读 | **W1.5** | **是**（存档） |
| 4 | 动能/反应堆/作物等高价值 TE：网络 + NBT 成对改 | 后续命名 Unit | **是** |
| 5 | 组件 ID（`Components`）、InvSlot/Tank 标识 | 组件/库存迁移 | **是**（TE NBT） |
| 6 | 配方 meta（`minHeat`） | 配方/DataGen Unit | **是**（数据包） |
| 7 | 配置键 camelCase → snake | 独立 config PR | 建议双读一版 |
| 8 | lang `ic2r.Pascal...` → `gui.ic2r.snake...` | UI/文案批 | 旧键可双写或弃用表 |

### 3.2 必须写 `id_migrations` 的情况

- 注册 path 变更（当前 **几乎无** 压力）
- **NBT 键** 变更（世界/物品存档）
- **组件 ID**、槽位/流体罐 identifier 变更
- **配方 JSON meta** 键变更
- 配置键若做破坏性更名（可选：仅文档 + 双读）

### 3.3 通常不需要迁移表

- 仅客户端 GUI 网络字段改名（无世界持久化）— 仍需两端版本一致
- 纯展示 lang 键（若旧键无人引用可直接替换；多语言文件需同步）

### 3.4 试点域建议（对齐 W1.5）

1. `ContainerStandardMachine` / `guiProgress`  
2. `Energy` 组件 NBT（`storage` 是否重命名为更清晰的 snake 名，**二选一并写迁移**）  
3. 1 台子类机（如 Macerator）端到端：同步 + 存档往返测试  

---

## 4. 规模概览（启发式，非精确 dedupe）

| 面 | 粗计量 | 合规印象 |
|:---|:---|:---|
| 注册 path（方块/物品） | 数百 | **优**（snake_case） |
| 网络字段 camelCase add | ~139 行级命中 | **差**（反射字段名） |
| NBT camelCase put/get | ~534 行级命中 | **差**（机器/作物/物品） |
| 配置 camelCase | ~83 | **差**（低优先） |
| lang Pascal 前缀 GUI | ~362 | **差**（展示层） |
| 组件 register camelCase | 3 明确 + 若干小写合规 | 中 |

---

## 5. 风险与验证

### 5.1 风险

- **存档损坏**：NBT/组件 ID/槽位名未做旧键回读  
- **协议不同步**：仅改服务端或仅改客户端网络字段名  
- **配方包**：`minHeat` 等 meta 改名未同步 JSON  
- **过度改名单词**：`active`/`energy`/`progress` 无收益却增加兼容成本  
- **与反射耦合**：在 W1.1 完成前硬改字段名字符串易漏改 `onNetworkUpdate` 分支  

### 5.2 如何验证本报告

1. 文件存在且非空：`docs/spec/naming_audit.md`  
2. 表中路径可打开，例如：  
   - `src/main/java/me/halfcooler/ic2r/core/block/machine/container/ContainerStandardMachine.java`  
   - `src/main/java/me/halfcooler/ic2r/core/block/comp/Components.java`  
   - `src/main/java/me/halfcooler/ic2r/core/block/comp/Energy.java`  
3. 本地可复现扫描（PowerShell 示例）：  
   ```powershell
   Select-String -Path (Get-ChildItem src\main\java -Recurse -Filter *.java).FullName `
     -Pattern 'ret\.add\("[a-z]+[A-Z]|fields\.add\("[a-z]+[A-Z]'
   ```  
4. 对照 §3.4 清单：本报告覆盖 register / getNetworkedFields / NBT / lang 四类。

---

## 6. §3.4 清单勾选（审计产出态）

- [x] 扫描 `register("...")` / 注册 path 非 snake_case → **抽查基本合规；组件 ID 例外**  
- [x] 扫描 `getNetworkedFields()` 字符串 → **大量 camelCase，已抽样**  
- [x] 扫描 NBT put/get 键 → **大量 camelCase，已抽样**  
- [x] 扫描 lang 键风格不一致 → **block/item 好，GUI Pascal 差**  
- [x] 产出 `docs/spec/naming_audit.md` + 修复优先级（批量修复 **不在** 本 Unit）

---

## 7. 修订记录

| 日期 | 说明 |
|:---|:---|
| 2026-07-14 | W0.5 初版：抽样报告 + 优先级，无代码改名 |
