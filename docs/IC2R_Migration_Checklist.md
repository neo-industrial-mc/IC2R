# IC2 → IC2R 三步迁移清单

> **用途**：交给新会话作为唯一执行规格。新会话应拆成多个 subagent 并行落地，每步结束必须通过对应验收门禁后再进入下一步。  
> **仓库根目录**：`D:\Files\Codes\JavaSources\IC2R`  
> **分支**：`forge/1.20.1`  
> **目标 Minecraft / Forge**：1.20.1 / 47.x

---

## 0. 总目标与最终态

| 维度 | 旧值 | 新值 |
|------|------|------|
| 游戏 mod id / 命名空间 | `ic2` | `ic2r` |
| ResourceLocation / 注册键前缀 | `ic2:` | `ic2r:` |
| 翻译键段 / 点分 id | `ic2.`（如 `item.ic2.*`、`ic2.jade.*`） | `ic2r.` |
| 资源目录 | `assets/ic2`、`data/ic2` | `assets/ic2r`、`data/ic2r` |
| 类型/字面量品牌 | `IC2`、`Ic2` | `IC2R`、`Ic2r`（见 §2 规则表） |
| Java 包根 | `ic2.*` | `me.halfcooler.ic2r.*` |
| Gradle `mod_id` | `ic2` | `ic2r` |
| Gradle `mod_group_id` | `ic2.core` | `me.halfcooler.ic2r` |
| Mixin 配置文件 | `ic2.mixins.json` / `ic2.refmap.json` | `ic2r.mixins.json` / `ic2r.refmap.json` |

**三步顺序（硬性）**：

1. **游戏内命名空间**（资源 + 字符串命名空间）  
2. **代码字面量 / 类型名**（`IC2` / `Ic2` → `IC2R` / `Ic2r`）  
3. **软件包重构**（`ic2.*` → `me.halfcooler.ic2r.*` + 目录移动）

不要颠倒：步骤 3 会大面积改 `package`/`import`，与步骤 1–2 的字符串替换混在一起极易漏改或误改。

---

## 1. 规模基线（开工前对照）

统计于编写本清单时（仅 `src/`，不含 `build/`、`run/`）：

| 范围 | 规模 |
|------|------|
| Java 源文件 | **938**（`package ic2` 各 1 次） |
| `import ic2.*` | **4252** |
| `src` 文本内 `ic2:` | **11300**（以 JSON 为主；assets JSON ~6682，data/ic2 ~4290，forge/minecraft tags ~300） |
| `src` 文本内 `"ic2"` | **139**（Java 命名空间字面量约占多数；另有 `fromNamespaceAndPath("ic2", …)` **~81**） |
| Java 中 `\bIC2\b` | **6421**（含类型引用、日志、注释；步骤 2 不可无脑全替，按规则表） |
| Java 中 `Ic2` 前缀命中 | **9383**（同上，需按符号重命名而非盲目替换） |
| `class IC2` / `class Ic2` 子串 | 各约 **51**（含 `IC2Config` 等，非 51 个独立顶层类） |
| `assets/ic2` 文件 | **4293**（JSON **2132** + 贴图/音效等） |
| `data/ic2` 文件 | **1470**（几乎全是 JSON） |
| 语言文件 | **3**（`en_us` / `zh_cn` / `ru_ru`）；键 **~1347**，含 `.ic2.` 约 **929** |
| `Ic2*` / `IC2*` 类文件 | **~65**（文件名与类型名） |
| Mixin 配置引用 | `ic2.mixins` **1** 处（`build.gradle` / `mods.toml` 需对齐） |

**顶层 Java 包分布**（步骤 3 目录映射时用）：

```
src/main/java/ic2/
  api/           138 .java
  core/          745 .java
  forge/          28 .java
  integration/    25 .java
  compat/          1 .java
  mixin/           1 .java
  profiles/        （png，随目录移动）
  sounds/          （ogg，随目录移动；正常资源应在 assets，勿改内容）
```

---

## 2. 全局约定

### 2.1 允许修改的范围

- `src/main/java/**`
- `src/main/resources/**`
- `build.gradle`、`gradle.properties`、`settings.gradle`（仅必要时）
- 可选：`docs/**`、`README.md`、`README_EN.md`、`release.md` 中与**新 id**相关的技术说明（历史叙述可保留 “IC2” 品牌名，见 §2.3）

### 2.2 禁止修改 / 勿当源码处理

- `build/**`（构建产物；改完后 `clean` 即可）
- `run/**`（本地配置、存档、日志；迁移后应用新 mod id 重新生成配置）
- `.git/**`
- 第三方依赖源码 / 反编译缓存
- **二进制资源内容**（`.png`、`.ogg`、`.jar`）：只允许**随目录重命名**，不改文件字节
- `net.minecraft.*`、`net.minecraftforge.*`、`mezz.jei.*`、`snownee.jade.*`、`appeng.*` 等**外部包名**

### 2.3 不要误替换的文本

| 保留 | 原因 |
|------|------|
| 用户可见显示名 `IndustrialCraft 2: Refactored` / `mod_name` | 产品显示名，不是 namespace |
| README 中对原版 IC2 历史的叙述 | 语义是 “原版工业时代 2”，不是本模组 id |
| 外部 URL 中的 `ic2-fabric` 等路径 | 他人仓库 |
| 注释里说明 “兼容旧 IC2 行为” 的自然语言（可选保留） | 语义不等于代码 id |
| `IndustrialCraft2_Refactored.png` 文件名 | 除非另开任务统一资源命名 |

### 2.4 步骤 2 类型重命名规则（强制统一）

| 模式 | 替换为 | 示例 |
|------|--------|------|
| 全大写标识 `IC2`（类名、常量段、全大写前缀） | `IC2R` | `class IC2` → `class IC2R`；`IC2Config` → `IC2RConfig`；`IC2Material` → `IC2RMaterial` |
| Pascal 前缀 `Ic2` | `Ic2r` | `Ic2Blocks` → `Ic2rBlocks`；`Ic2JeiPlugin` → `Ic2rJeiPlugin` |
| 字段/方法中的 `Ic2` 段 | `Ic2r` | `tabIc2General` → `tabIc2rGeneral`；`isIc2Available` → `isIc2rAvailable` |
| 日志/线程名里的 `ic2-` 前缀（步骤 1 已处理的配置文件名除外） | `ic2r-` | `ic2-poolthread-` → `ic2r-poolthread-` |

**禁止**：把 `ic2` 包名在步骤 2 里改成 `ic2r`（那是步骤 3）；步骤 2 只动 **类型/成员/文件名中的品牌前缀**，以及尚未被步骤 1 覆盖的展示性字符串（如日志里的 `"[IC2 Recipe Debug]"` → `"[IC2R Recipe Debug]"`）。

### 2.5 工具策略建议

- 大规模文本：优先脚本化（PowerShell / Python），对 **JSON/lang 全量** 做机械替换；Java 用更谨慎的规则。
- 类型重命名：优先 IDE Refactor 或等价 “rename symbol + rename file”；禁止只改类名不改文件名。
- 包移动：优先 IDE Move / 脚本 `git mv` + 批量改 `package`/`import`。
- 每步后：`.\gradlew.bat clean compileJava`（至少）；能跑则 `runClient` 冒烟。

### 2.6 存档与兼容性（明确默认策略）

- **默认：破坏性换 id**。旧世界中 `ic2:*` 物品/方块会丢失或变成空气；配置文件从 `ic2-*.toml` 变为 `ic2r-*.toml`。
- **可选增强（本清单默认不做，单独立项）**：在 `MissingMappingsEvent` 中把旧 `ic2` 映射到 `ic2r`（已有钩子：`EventHandlerForge` 对 `"ic2"` 的 missing mapping 循环，迁移后应变为处理旧 id 或删除）。
- **Create 等硬编码 `ic2:` 的模组**仍会不兼容（见 `release.md`）；换 id 后 Create 问题可能缓解或变成另一类缺失注册，需实机验证。

---

## 3. 步骤 1：游戏内命名空间 `ic2` → `ic2r`

### 3.1 目标

凡是进入注册表、资源包、数据包、网络、配置文件名、翻译键中的 **mod 命名空间**，一律从 `ic2` 变为 `ic2r`。

### 3.2 子任务划分（可并行 subagent）

#### S1-A：Gradle / 模组元数据（先做，串行小任务）

| # | 文件 | 动作 |
|---|------|------|
| 1 | `gradle.properties` | `mod_id=ic2` → `mod_id=ic2r`；**暂不改** `mod_group_id`（留给步骤 3） |
| 2 | `src/main/resources/META-INF/mods.toml` | 检查 `modId="${mod_id}"` 已间接生效；`[[mixin]] config=` 改为 `ic2r.mixins.json` |
| 3 | `build.gradle` | Mixin：`ic2.refmap.json` → `ic2r.refmap.json`；`ic2.mixins.json` → `ic2r.mixins.json` |
| 4 | `src/main/resources/ic2.mixins.json` | 重命名为 `ic2r.mixins.json`；内容中 `"refmap": "ic2.refmap.json"` → `ic2r.refmap.json`；**`package` 仍为 `ic2.mixin`（步骤 3 再改）** |

验收：`mod_id` 全仓库检索为 `ic2r`（源码配置侧）。

#### S1-B：资源目录重命名（机械、可单独 agent）

| # | 动作 |
|---|------|
| 1 | `src/main/resources/assets/ic2` → `assets/ic2r`（`git mv` 或等价） |
| 2 | `src/main/resources/data/ic2` → `data/ic2r` |
| 3 | 若存在 `data/ic2r/advancements/ic2/`，目录段也改为 `data/ic2r/advancements/ic2r/`（路径内嵌命名空间文件夹） |
| 4 | 扫描是否还有路径段 `/ic2/` 残留在 `src/main/resources` 下（除文档外） |

**不要**重命名 `data/forge`、`data/minecraft` 目录本身，只改其**文件内容**中的引用。

#### S1-C：资源文件内容替换（最大量，建议脚本）

对 `src/main/resources/**/*.{json,mcmeta,toml,lang,txt}`（实际以 json 为主）：

| 替换规则（建议顺序） | 说明 |
|----------------------|------|
| `#ic2:` → `#ic2r:` | 标签引用 |
| `ic2:` → `ic2r:` | 物品/方块/配方/音效/模型 parent 等 |
| 翻译键中的命名空间段：见下表 | 与 Java `Component.translatable` 必须一致 |

**翻译键（lang + 所有引用处）必须同步**：

| 旧前缀/模式 | 新 |
|-------------|-----|
| `item.ic2.` | `item.ic2r.` |
| `block.ic2.` | `block.ic2r.` |
| `container.ic2.` | `container.ic2r.` |
| `fluid_type.ic2.` / `fluid.ic2.`（若有） | `*.ic2r.` |
| `entity.ic2.` | `entity.ic2r.` |
| `effect.ic2.` | `effect.ic2r.` |
| `itemGroup.ic2.` / creative tab 相关键 | `*.ic2r.` |
| `subtitle.ic2.` | `subtitle.ic2r.` |
| `advancements.ic2.` | `advancements.ic2r.` |
| `death.attack.` 等中嵌入的 `ic2` 段 | 按实际键名改为 `ic2r` |
| `config.ic2.`（若有） | `config.ic2r.` |
| 以 `ic2.` 开头的自定义键（`ic2.jade.*`、`ic2.electric.*`、`ic2.EUStorage.*` 等） | `ic2r.*` |

`en_us.json` / `zh_cn.json` / `ru_ru.json` 三份 lang **键名**全改；**值**中的 “IC2” 展示文案可保留或按产品要求改为 “IC2R”（建议展示文案统一为 IC2R，历史叙述除外）。

**重点资源入口文件**（改完后人工抽查）：

- `assets/ic2r/sounds.json`
- `assets/ic2r/lang/*.json`
- `data/ic2r/recipes/**`（~796）
- `data/ic2r/loot_tables/**`
- `data/ic2r/advancements/**`
- `data/ic2r/loot_modifiers/**`
- `data/forge/loot_modifiers/global_loot_modifiers.json`（含 `ic2:inject`）
- `data/forge/tags/**/*.json`
- `data/minecraft/tags/**/*.json`

#### S1-D：Java 中的命名空间字符串（与 S1-C 并行，但需规则更严）

**权威常量（优先改，并尽量让后续引用走常量）**：

- `ic2.api.info.Info.MOD_ID = "ic2"` → `"ic2r"`

**硬编码点位（编写清单时已确认，执行时全量再扫）**：

| 区域 | 文件线索 | 内容 |
|------|----------|------|
| 入口 | `ic2.forge.FmlMod` | `@Mod("ic2")` → `@Mod("ic2r")`；配置文件名 `ic2-uu-scan-values.toml` → `ic2r-uu-scan-values.toml` |
| 核心 | `ic2.core.IC2` | `LogManager.getLogger("ic2")`；`fromNamespaceAndPath("ic2", …)` |
| Forge 注册 | `EnvProxyForge`、`EnvFluidHandlerForge`、`Ic2LootModifier` | `DeferredRegister.create(..., "ic2")`；loot RL |
| 客户端 | `EventHandlerClient`、`SideProxyClient`、`GuiParser` | namespace 比较 `"ic2".equals(...)`；boat 模型域 |
| 伤害 | `Ic2DamageSource` | damage type RL |
| 物品/贴图 | `Ic2Items`、`GuiElement`、`Ic2Gui`、`GuiOverlayer`、`Ic2CropCard` | 纹理 RL |
| 集成 | `Ic2JeiPlugin` | `RecipeType.create("ic2", …)` |
| 其它 | `DropScan`、`EventHandler`、`MissingMappings` 中的 `"ic2"` | 日志/映射命名空间 |
| 线程名 | `PriorityExecutor` | `"ic2-poolthread-"` → `"ic2r-poolthread-"` |

**Java 替换规则**：

1. `ResourceLocation.fromNamespaceAndPath("ic2",` → `...("ic2r",`  
2. 字面量 `"ic2"` 仅在表示 **mod 命名空间 / mod id** 时改为 `"ic2r"`  
3. 字符串中的 `ic2:` → `ic2r:`  
4. `Component.translatable("…ic2…")` 中命名空间段 → `ic2r`（与 lang 键一致）  
5. 配置文件名：`ic2-client.toml` / `ic2-common.toml` / `ic2-uu-scan-values.toml` 的**代码引用与注释** → `ic2r-…`（Forge 默认 common/client 配置名随 `mod_id` 变为 `ic2r-common.toml` 等）

**不要在步骤 1 改**：

- `package ic2...` / `import ic2...`
- 类名 `IC2` / `Ic2*`

#### S1-E：验收门禁（步骤 1 完成标准）

在 `src/` 下执行（可用 IDE / ripgrep / 脚本）：

```
必须为 0（误报需人工确认）：
  - 资源路径仍存在 assets/ic2 或 data/ic2
  - JSON 中残留 ic2: 或 #ic2:（作为本模组命名空间）
  - Java 中 fromNamespaceAndPath("ic2"
  - @Mod("ic2")
  - Info.MOD_ID 仍为 "ic2"
  - mods.toml / build.gradle 仍指向 ic2.mixins.json

允许残留：
  - package/import 的 ic2.
  - 类名 IC2 / Ic2*
  - README 历史叙述
  - 注释中的“原版 IC2”自然语言
```

构建：`.\gradlew.bat clean compileJava` 必须通过。

---

## 4. 步骤 2：代码字面量 `IC2` / `Ic2` → `IC2R` / `Ic2r`

### 4.1 目标

品牌前缀从 IC2 系切换到 IC2R 系；**类型名、文件名、成员名**一致；**不**移动包路径。

### 4.2 已知类型清单（起步列表，执行时用搜索补全）

以下类型在步骤 2 必须完成 **类/接口/枚举重命名 + 同名 `.java` 文件重命名 + 全引用更新**：

**核心入口与工具**

- `IC2` → `IC2R`
- `IC2Config` → `IC2RConfig`
- `IC2ClientConfig` → `IC2RClientConfig`
- `IC2UuScanConfig` → `IC2RUuScanConfig`
- `IC2Material` → `IC2RMaterial`
- `Ic2Constants` → `Ic2rConstants`
- `Ic2Gui`、`Ic2Player`、`Ic2Potion`、`Ic2Explosion`、`Ic2DamageSource`、`Ic2ItemGroupType`、`Ic2Tooltip`、`Ic2Color` → `Ic2r*`

**`ic2.core.ref.*`**

- `Ic2Blocks`、`Ic2Items`、`Ic2Fluids`、`Ic2Entities`、`Ic2BlockEntities`
- `Ic2BlockTags`、`Ic2ItemTags`、`Ic2FluidTags`
- `Ic2RecipeTypes`、`Ic2RecipeSerializers`、`Ic2ScreenHandlers`
- `Ic2SoundEvents`、`Ic2GameEvents`、`Ic2BoatTypes`、`Ic2SignType`
- `Ic2ArmorMaterials`、`Ic2ToolMaterials`

**方块 / 流体 / 作物 / loot 等**（凡 `Ic2*` 类型）

- `Ic2TileEntity`、`Ic2TileEntityBlock`、`Ic2SignBlockEntity`
- `Ic2FenceBlock`、`Ic2GlassBlock`、`Ic2SheetBlock`、`Ic2SignBlock`、`Ic2WallSignBlock`
- `Ic2FluidTank`、`Ic2FluidStack`、`Ic2FluidItem`、`Ic2FluidBlock`
- `Ic2BucketItem`、`Ic2Pickaxe`、`Ic2Axe`、`Ic2Hoe`
- `Ic2CropCard`、`Ic2Crops`、`Ic2CropType`
- `Ic2WorldGen`、`Ic2LootModifier`、`Ic2LootNbtProviderTypes`、`Ic2BlockNbtProvider`
- `Ic2CraftingResultSlot`、`Ic2CraftingRecipe`、`Ic2Model`

**集成**

- `Ic2JeiPlugin`、`Ic2JadePlugin`、`Ic2JadePluginConfigs`
- `Ic2EnergyProvider`、`Ic2MachineTooltipProvider`、`Ic2ProgressProvider`
- `Ic2Ae2Plugin`

执行时命令思路：`Get-ChildItem -Recurse -Filter "Ic2*.java"` 与 `IC2*.java` 应在步骤 2 后**全部变为** `Ic2r*` / `IC2R*`。

### 4.3 子任务划分

#### S2-A：高频核心类型（串行优先）

先改被全局引用的符号，降低并行冲突：

1. `IC2` → `IC2R`（`ic2.core.IC2`）  
2. `Ic2Blocks` / `Ic2Items` / `Ic2Fluids` / `Ic2Entities` / `Ic2BlockEntities`  
3. 三个 Config 类 + `Info.isIc2Available` 等 API 表面  

#### S2-B：按包并行重命名

| Agent | 范围 |
|-------|------|
| B1 | `ic2.core.block.**`、`ic2.core.item.**` |
| B2 | `ic2.core.ref.**` 剩余、`ic2.core.crop.**`、`ic2.core.fluid.**`、`ic2.core.loot.**` |
| B3 | `ic2.forge.**`、`ic2.compat.**` |
| B4 | `ic2.integration.**`、`ic2.api.**` 中含 Ic2/IC2 的符号 |

每个 agent 负责：**定义处 + 全仓库引用 + 文件名**。

#### S2-C：非类型字符串

- 日志：`"[IC2 Recipe Debug]"` → `"[IC2R Recipe Debug]"`
- 用户消息 / 异常：`"IC2 is not loaded"` → `"IC2R is not loaded"`
- Jade / JEI 展示名中的 “IC2” 插件名 → “IC2R”（若有）
- **不要**把包名 `ic2` 改成 `ic2r`

### 4.4 验收门禁（步骤 2）

```
src/main/java 内：
  - 无 class/interface/enum 名仍为 \bIC2\b 或 \bIc2[A-Z]
  - 无文件名 Ic2*.java / IC2*.java（应已是 Ic2r* / IC2R*）
  - 成员名 tabIc2*、isIc2* 等已迁移
  - package/import 仍为 ic2.*（尚未步骤 3）

compileJava 通过
```

---

## 5. 步骤 3：软件包 `ic2.*` → `me.halfcooler.ic2r.*`

### 5.1 目标

```
ic2.<rest>  →  me.halfcooler.ic2r.<rest>
```

目录：

```
src/main/java/ic2/...
  → src/main/java/me/halfcooler/ic2r/...
```

### 5.2 映射表（完整）

| 旧包 | 新包 |
|------|------|
| `ic2.api.**` | `me.halfcooler.ic2r.api.**` |
| `ic2.core.**` | `me.halfcooler.ic2r.core.**` |
| `ic2.forge.**` | `me.halfcooler.ic2r.forge.**` |
| `ic2.integration.**` | `me.halfcooler.ic2r.integration.**` |
| `ic2.compat.**` | `me.halfcooler.ic2r.compat.**` |
| `ic2.mixin.**` | `me.halfcooler.ic2r.mixin.**` |

`profiles/`、`sounds/` 若仍挂在 `java/ic2/` 下，一并移到 `java/me/halfcooler/ic2r/` 对应相对路径（或后续任务挪回 `resources`；本步骤只保证不丢文件）。

### 5.3 必须同步修改的非 Java 文件

| 文件 | 动作 |
|------|------|
| `gradle.properties` | `mod_group_id=ic2.core` → `mod_group_id=me.halfcooler.ic2r` |
| `ic2r.mixins.json` | `"package": "ic2.mixin"` → `"package": "me.halfcooler.ic2r.mixin"` |
| 任意服务加载 / 反射字符串中的 FQCN（全库搜索 `ic2.core` / `ic2.forge` 等） | 改为新 FQCN |
| `docs/GTEU_Migration_Project.md` 等文档中的包路径 | 更新为新包（避免后续误导） |

### 5.4 执行顺序（推荐）

1. 创建目标根目录 `src/main/java/me/halfcooler/ic2r/`  
2. **`git mv`** 移动 `ic2` 树到 `me/halfcooler/ic2r`（保留历史）  
3. 批量替换：  
   - `^package ic2.` → `package me.halfcooler.ic2r.`  
   - `import ic2.` → `import me.halfcooler.ic2r.`  
   - 字符串中的 FQCN（若有 `Class.forName("ic2....")`）  
4. 更新 mixin package、`mod_group_id`  
5. `clean compileJava`  
6. 删除空的旧 `src/main/java/ic2` 目录（若有残留）

### 5.5 子任务划分

| Agent | 职责 |
|-------|------|
| S3-Root | 目录 `git mv` + 全量 `package`/`import` 脚本 + mixin/gradle |
| S3-Scan | 扫 FQCN 字符串、SPI、文档、注释中的旧包名 |
| S3-Verify | 验收与编译 |

包移动**不建议**多 agent 同时改 import（冲突高）；适合 **一个 agent 做机械迁移**，另一个只做扫描验收。

### 5.6 验收门禁（步骤 3 / 总完成）

```
必须为 0：
  - src/main/java 下仍存在顶层包目录 ic2/
  - 任意 .java 含 package ic2 或 import ic2.
  - 资源命名空间仍为 ic2（步骤 1 回归）
  - 类型名仍为 IC2/Ic2 前缀（步骤 2 回归）

构建：
  - .\gradlew.bat clean compileJava 成功
  - 推荐：.\gradlew.bat runClient 能进主菜单并看到模组 ic2r
  - 抽查：创造栏、任意机器 GUI 贴图、一条配方、JEI 分类 uid 为 ic2r:...
```

---

## 6. 新会话执行剧本（给 Orchestrator）

### 6.1 启动指令模板

```text
你是 IC2R 迁移执行会话。严格按 docs/IC2R_Migration_Checklist.md 三步顺序执行。
规则：
1. 只修改清单允许的范围；禁止动 build/、run/、.git。
2. 每一步拆 subagent；步骤完成必须跑清单中的验收门禁，失败则修复，不得进入下一步。
3. 步骤 2 类型重命名遵循 IC2→IC2R、Ic2→Ic2r；不要在步骤 1/2 改 package。
4. 步骤 3 使用 git mv 移动目录，package/import 全部改为 me.halfcooler.ic2r.*。
5. 大规模 JSON 替换用脚本；不要手工改数千文件。
6. 完成后给出：改动摘要、验收命令输出、已知残留风险（存档不兼容、Create 等）。
```

### 6.2 推荐 subagent 拓扑

```
Phase 1 (namespace)
  S1-A (meta) ──► S1-B (dirs) ──► ┬─ S1-C (resources content)
                                  └─ S1-D (java strings)
                      └─► S1-E verify + compile

Phase 2 (symbols)
  S2-A (core symbols) ──► S2-B1..B4 (parallel renames) ──► S2-C strings ──► verify + compile

Phase 3 (packages)
  S3-Root (mv + rewrite) ──► S3-Scan ──► S3-Verify + compile (+ optional runClient)
```

### 6.3 每步建议提交（若用户要求 commit）

1. `chore(namespace): rebrand game id ic2 → ic2r`  
2. `refactor(naming): rename IC2/Ic2 types to IC2R/Ic2r`  
3. `refactor(package): move ic2.* to me.halfcooler.ic2r.*`  

未要求 commit 则不要擅自提交。

---

## 7. 风险清单与决策记录

| 风险 | 影响 | 默认决策 |
|------|------|----------|
| 旧存档物品/方块 id | 世界物品消失 | 接受破坏性迁移；MissingMappings 另开任务 |
| 配置文件改名 | 玩家需重配 | 接受；`ic2-*.toml` → `ic2r-*.toml` |
| Create 硬编码 `ic2:` | 曾崩溃 | 换 id 后需重测；更新 `release.md` |
| JEI recipe category uid | 收藏/排序配置失效 | 接受 |
| 外部 addon 依赖 `ic2` 包/id | 编译失败 | 本仓库 `addons/` 若启用需同步；默认 `dev_addons` 可能为空 |
| API 包名变更 | 第三方 addon 源码不兼容 | 预期；版本号已是 IC2R 产品线 |
| 误替换 README 历史 “IC2” | 文案错误 | 步骤 2 默认**不**全库替换自然语言 “IC2” |
| `mod_group_id` 影响 maven 坐标 | 发布坐标变化 | 步骤 3 改为 `me.halfcooler.ic2r` |

---

## 8. 快速检查命令（Windows PowerShell）

在仓库根目录：

```powershell
# 步骤 1 回归：源码中是否还当命名空间使用 ic2
Select-String -Path (Get-ChildItem src -Recurse -Include *.java,*.json,*.toml,*.gradle,*.properties | % FullName) `
  -Pattern '@Mod\("ic2"\)|fromNamespaceAndPath\(\s*"ic2"|mod_id=ic2\b|#ic2:|(?<![\w])ic2:' `
  -ErrorAction SilentlyContinue

# 步骤 2 回归：旧类型文件名
Get-ChildItem src\main\java -Recurse -Include Ic2*.java,IC2.java,IC2Config.java,IC2ClientConfig.java,IC2UuScanConfig.java,IC2Material.java

# 步骤 3 回归：旧包
Select-String -Path (Get-ChildItem src\main\java -Recurse -Filter *.java | % FullName) `
  -Pattern '^package ic2\.|^import ic2\.' 

# 编译
.\gradlew.bat clean compileJava
```

---

## 9. 关键文件速查（优先人工复核）

| 优先级 | 路径 |
|--------|------|
| P0 | `gradle.properties` |
| P0 | `build.gradle`（mixin 段） |
| P0 | `src/main/resources/META-INF/mods.toml` |
| P0 | `src/main/resources/ic2r.mixins.json`（最终名） |
| P0 | `src/main/java/.../api/info/Info.java`（`MOD_ID`） |
| P0 | `.../forge/FmlMod.java`（`@Mod`、config 文件名） |
| P0 | `.../core/IC2R.java`（原 `IC2.java`） |
| P0 | `.../forge/EnvProxyForge.java`、`EnvFluidHandlerForge.java` |
| P1 | `.../integration/jei/Ic2rJeiPlugin.java` |
| P1 | `assets/ic2r/lang/en_us.json`、`zh_cn.json` |
| P1 | `assets/ic2r/sounds.json` |
| P1 | `data/forge/loot_modifiers/global_loot_modifiers.json` |
| P1 | `data/minecraft/tags/**`、`data/forge/tags/**` |
| P2 | `release.md`（Create / 命名空间说明） |

---

## 10. 完成定义（Definition of Done）

- [ ] 游戏内命名空间与资源目录均为 `ic2r`
- [ ] 代码中 mod id / RL / 翻译键命名空间均为 `ic2r`
- [ ] 类型与文件名为 `IC2R` / `Ic2r` 体系
- [ ] 全部 Java 包为 `me.halfcooler.ic2r.*`，目录一致
- [ ] `mod_id=ic2r`，`mod_group_id=me.halfcooler.ic2r`
- [ ] Mixin 配置与 package 指向新路径
- [ ] `clean compileJava` 通过
- [ ] （推荐）客户端进游戏，模组列表显示 IC2R，物品 id 为 `ic2r:...`
- [ ] 清单 §7 风险已在 PR/说明中写明

---

*本清单仅描述迁移工程本身；不包含 GTEU 能量线等玩法迁移（见 `docs/GTEU_Migration_Project.md`）。*
