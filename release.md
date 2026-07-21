# 21.1.43

欢迎来到制作和反馈清单。这里记录了下版本的计划和已知问题。

## 已知问题和 TODO

- 创造模式物品栏目前是按照注册键 A - Z 排序，后续会调整为功能分类顺序。
- 已经修了一万遍了：拟态板的物品模型不显示，功能正常。
- 磁化机耗电，穿着金属鞋却不升空。
- 模式扫描机 UU 物品和对应消耗重写。现在能复制的物品局限在了 1.12。
- 原版 IC2 没有配方：撬棍。用于拆卸覆盖板。但没有覆盖板。
- 迁移至 GTEU 后，导线必须重写一套
- 超频过的泵稳定性极差，极差极差特别差很差非常差
- 燃料棒（锂）和燃料棒（氚）：原版无功能。
- 新增采矿过滤卡的贴图
- 新增 FE 能源转换器的贴图
- LazyDFU 1.20.1 兼容性验证
- （特性）建筑泡沫和硬化墙、导线防爆
- （特性）ITNT 和核弹在激活后，实体具有碰撞箱
- （特性）批量工作台默认需要手动分拣

## 可能暂时不兼容的 Mods

- 高清修复 / Optifine。可能因为 Optifine 某些破坏性修改导致无法找到 `ResouceLocation.fromNamespaceAndPath(String, String)` 方法而崩溃。

## 其他特性

- 代码、文件、注册键正一步步全部遵循现代 Minecraft 的命名规范。
- 翻译：只保留中文和英文, 以中文为基准, 重做翻译。全部扁平化。
- 您可以直接在 NeoForge Mods 配置中使用 GUI 调整 IC2 的配置文件。

---

## 材料小堆 / 小撮体系全面重构（计划）

> **状态**: 计划中，尚未实现  
> **目标版本**: 21.1.43+  
> **原则**: 凡现有涉及「小撮 9 合 1」或将引入「小堆 4 合 1」的材料，统一改为 **整单位（锭或粉）+ 小堆粉 + 小撮粉** 三形态。

### 1. 背景与问题

| 现状 | 说明 |
|:---|:---|
| 只有一级碎片 | 物品 id 为 `small_*`，中文叫「小撮」，英文却是 Tiny Pile |
| 比例全是 9:1 | 有序合成 `3×3`、压缩机 `count: 9`、shapeless 拆 9 份；**没有** 4 合 1 的小堆 |
| 命名与语义错位 | `small` 实际是 tiny（1/9），与 GTEU / 常见工业模组惯例冲突 |
| 核材料形态 | `plutonium` / `uranium_235` / `uranium_238` 为球状核材料；`c:ingots/plutonium` 标签壳已存在但 `values` 为空 |
| 配方双轨 | 如 MOX / RTG 同时存在 `item: ic2r:plutonium` 与 `tag: c:ingots/plutonium` 两套，tag 版实际接不到本模物品 |

### 2. 目标形态（GT 风格）

| 形态 | 中文 | 英文建议 | 对整粉/锭的比例 | 合成 |
|:---|:---|:---|:---|:---|
| 整单位 | 锭 **或** 粉 | Ingot / Dust | 1 | 基准 |
| 小堆粉 | 小堆 {材料}粉 | Small Pile of {Material} Dust | **1/4** | 4 小堆 → 1 整粉；1 整粉 → 4 小堆 |
| 小撮粉 | 小撮 {材料}粉 | Tiny Pile of {Material} Dust | **1/9** | 9 小撮 → 1 整粉；1 整粉 → 9 小撮 |

补充关系（可选配方，便于互转）：

- 9 小撮 = 1 整粉  
- 4 小堆 = 1 整粉  
- **不强制** 小堆 ↔ 小撮整数互转（4 与 9 不整除）；若加互转须走压缩机/有序合成并明确损耗规则，默认 **不提供** 非整数互转。

整单位选择规则：

| 材料类型 | 整单位 | 小堆 / 小撮 |
|:---|:---|:---|
| 已有金属/宝石粉 | 保留 `*_dust` | `small_*_dust`（4:1）+ `tiny_*_dust`（9:1） |
| 核同位素（现为球） | 改为 **锭**（可贴锭贴图） | 对应 **粉** 的小堆 / 小撮：`small_*_dust` / `tiny_*_dust` |
| 非材料组件 | **不在范围**（如 `small_power_unit`） | — |

### 3. 命名与 ID 约定

#### 3.1 物品注册键

| 形态 | ID 模式 | 示例 |
|:---|:---|:---|
| 整粉 | `{mat}_dust` | `iron_dust` |
| 小堆粉 | `small_{mat}_dust` | `small_iron_dust`（**语义改为 4:1**） |
| 小撮粉 | `tiny_{mat}_dust` | `tiny_iron_dust`（承接现 9:1 的 `small_*`） |
| 核整锭 | `{mat}_ingot` | `plutonium_ingot`、`uranium_235_ingot`、`uranium_238_ingot` |
| 核整粉（若需要粉碎线） | `{mat}_dust` | `plutonium_dust` 等（见 §5，可二期） |

> **破坏性重命名**: 当前所有「9 合 1 的 `small_*`」在语义上变为 **tiny**。  
> 为避免「小堆」占着 `small_*` 却仍是 9:1，必须：  
> 1. 旧 `small_X`（9:1）→ 新 `tiny_X`  
> 2. 新注册真正的 `small_X`（4:1）

#### 3.2 显示名（中文为基准）

| 形态 | zh_cn | en_us |
|:---|:---|:---|
| 整粉 | `{材料}粉` | `{Material} Dust` |
| 小堆粉 | `小堆{材料}粉` | `Small Pile of {Material} Dust` |
| 小撮粉 | `小撮{材料}粉` | `Tiny Pile of {Material} Dust` |
| 核整锭 | `{材料}锭` | `{Material} Ingot` |

#### 3.3 Common 标签（NeoForge 层级）

| 形态 | Tag | 写入物品 |
|:---|:---|:---|
| 整粉 | `c:dusts/{mat}` | `{mat}_dust` |
| 小堆粉 | `c:small_dusts/{mat}` | `small_{mat}_dust` |
| 小撮粉 | `c:tiny_dusts/{mat}` | `tiny_{mat}_dust` |
| 整锭 | `c:ingots/{mat}` | `{mat}_ingot` |

父标签：`c:dusts` / `c:small_dusts` / `c:tiny_dusts` / `c:ingots` 分别 include 各子标签。  
Java 侧在 `Ic2rItemTags` 增补 `SMALL_*_DUSTS` / `TINY_*_DUSTS` / 核 `*_INGOTS`。

### 4. 范围清单（仅「现有小撮线」材料）

#### 4.1 粉系（已有整粉 + 现 `small_*_dust`）

| 材料 mat | 整粉（保留） | 现 small（→ tiny） | 新 small（4:1） | 主要产出机 |
|:---|:---|:---|:---|:---|
| bronze | `bronze_dust` | `small_bronze_dust` → `tiny_bronze_dust` | `small_bronze_dust` | 合成/压缩机 |
| copper | `copper_dust` | → `tiny_copper_dust` | 新 `small_copper_dust` | 洗矿、离心 |
| gold | `gold_dust` | → `tiny_gold_dust` | 新 `small_gold_dust` | 洗矿、离心、矿渣 |
| iron | `iron_dust` | → `tiny_iron_dust` | 新 `small_iron_dust` | 洗矿、离心 |
| lapis | `lapis_dust` | → `tiny_lapis_dust` | 新 `small_lapis_dust` | 合成/压缩机 |
| lead | `lead_dust` | → `tiny_lead_dust` | 新 `small_lead_dust` | 洗矿、离心 |
| lithium | `lithium_dust` | → `tiny_lithium_dust` | 新 `small_lithium_dust` | 离心（石英） |
| obsidian | `obsidian_dust` | → `tiny_obsidian_dust` | 新 `small_obsidian_dust` | 合成/压缩机 |
| silver | `silver_dust` | → `tiny_silver_dust` | 新 `small_silver_dust` | 洗矿、离心 |
| sulfur | `sulfur_dust` | → `tiny_sulfur_dust` | 新 `small_sulfur_dust` | 提取、洗矿 |
| tin | `tin_dust` | → `tiny_tin_dust` | 新 `small_tin_dust` | 洗矿、离心 |
| diamond | `diamond_dust` | → `tiny_diamond_dust` | 新 `small_diamond_dust` | 作物掉落等 |

#### 4.2 核同位素（现球 + 小撮球 → 锭 + 粉小堆/小撮）

| 现整球 | 现 small（9:1） | 新整单位 | 新小堆粉 | 新小撮粉 | 标签 |
|:---|:---|:---|:---|:---|:---|
| `plutonium` | `small_plutonium` | `plutonium_ingot` | `small_plutonium_dust` | `tiny_plutonium_dust` | `c:ingots/plutonium` 填入锭 |
| `uranium_235` | `small_uranium_235` | `uranium_235_ingot` | `small_uranium_235_dust` | `tiny_uranium_235_dust` | `c:ingots/uranium_235`（新建） |
| `uranium_238` | `small_uranium_238` | `uranium_238_ingot` | `small_uranium_238_dust` | `tiny_uranium_238_dust` | `c:ingots/uranium_238`（新建） |

说明：

- `uranium_ingot`（精炼铀 / Refined Uranium）**保持独立**，与同位素锭无关。  
- 浓缩铀核燃料 `uranium`、MOX `mox`、燃料棒等 **配方用料** 改为引用新锭/tag，不拆成小堆小撮。  
- `small_uranium_238` 目前几乎无配方引用，仍一并规范化，避免创造栏半吊子形态。

#### 4.3 明确不在范围

- `small_power_unit` / `power_unit`（机械组件，非材料堆）
- 无 small/tiny 线的粉：`clay_dust`、`coal_dust`、`coal_fuel_dust`、`energium_dust`、`silicon_dioxide_dust`、`stone_dust`、`netherrack_dust`、`hydrated_tin_dust` 等（本期不强制补全）
- 板 / 致密板 / casing / 矿石 / 粗矿

### 5. 转换配方标准

每种在范围材料统一提供：

| 方向 | 类型 | 数量 |
|:---|:---|:---|
| 9 小撮 → 1 整粉/锭 | shaped `3×3` + compressor | 9 tiny → 1 full |
| 1 整粉/锭 → 9 小撮 | shapeless | 1 full → 9 tiny |
| 4 小堆 → 1 整粉/锭 | shaped `2×2` + compressor | 4 small → 1 full |
| 1 整粉/锭 → 4 小堆 | shapeless | 1 full → 4 small |

核材料：整单位为 **锭** 时，小堆/小撮为 **粉**；压缩机「粉合锭」与「小撮合整」分开：

- 默认：**小撮/小堆 合的是粉**，再 `粉 → 锭`（熔炉/高炉，二期可加）  
- 或一期简化：**小撮/小堆 直接合回锭**（与现 `small_plutonium → plutonium` 行为一致），粉 id 仍注册以便洗矿/离心副产语义统一  

**一期推荐（少改机台逻辑）**: 小撮/小堆 直接合成/压缩为 **整锭**；整锭可 shapeless 拆为小撮或小堆粉；离心/洗矿副产输出 **tiny/small 粉**。二期再补完整 `*_dust` 与熔炼链。

### 6. 机器与配方改点清单

| 区域 | 改动要点 |
|:---|:---|
| `recipe/compressor/small_*` | 输入改为 `tiny_*`（保持 count 9）；新增 `small_*` count 4 配方 |
| `recipe/shaped/*_dust.json` 等 3×3 | 键改为 `tiny_*` |
| 新增 `2×2` shaped / shapeless 拆分 | 小堆线 |
| `recipe/shapeless/small_plutonium` 等 | 拆出 tiny；整锭拆 tiny/small |
| `ore_washer` 副产 `small_*_dust` | **决策**：保持副产数量语义时，改为 `tiny_*`（因原 9:1）或按平衡改为 `small_*`；默认 **原 9:1 副产 → tiny** |
| `centrifuge` 副产 / 乏燃料产钚 | 同上；`small_plutonium` → `tiny_plutonium_dust`（或数量重平衡） |
| `extractor` 硫副产 | 同上 |
| `shaped/mox_*`、`rtg_pellet*` | 统一为 `c:ingots/plutonium`，删除仅 item 的重复轨 |
| `shaped/uranium.json` / `uranium_2.json` | `small_uranium_235` → 新 tiny 粉或按燃料配方改写 |
| `TileEntityNuke` 等 Java 引用 | `PLUTONIUM` / `SMALL_PLUTONIUM` 常量与爆炸当量对照表更新 |
| 作物 `SMALL_DIAMOND_DUST` | 改为 tiny 掉落，或保留 small（4:1）并调整掉落量 |
| advancement 配方解锁 | 同步物品 id / tag |

### 7. 资源与贴图

| 类型 | 动作 |
|:---|:---|
| 现有 `textures/.../small_*.png` | 语义变为 tiny 后：贴图可复用到 `tiny_*` 路径，或改模型指向 |
| 新 small（4:1）贴图 | 需新增小堆粉贴图（粉堆体积介于 tiny 与 full 之间） |
| 核球 → 锭 | `plutonium` / `uranium_235` / `uranium_238` 改锭贴图；旧球贴图可留给 dust 或废弃 |
| models / lang | 全量重命名与三语（至少 zh_cn + en_us；ru 可删或随扁平化丢弃） |

### 8. 存档兼容（LegacyRegistryRemap）

在 `LegacyRegistryRemap.planItemPathRenames` 登记别名（读档旧 id → 新 id）：

| 旧 id | 新 id | 备注 |
|:---|:---|:---|
| `ic2r:small_{mat}_dust` | `ic2r:tiny_{mat}_dust` | 粉系全部 mat |
| `ic2r:small_plutonium` | `ic2r:tiny_plutonium_dust` | 核 |
| `ic2r:small_uranium_235` | `ic2r:tiny_uranium_235_dust` | 核 |
| `ic2r:small_uranium_238` | `ic2r:tiny_uranium_238_dust` | 核 |
| `ic2r:plutonium` | `ic2r:plutonium_ingot` | 整球→锭 |
| `ic2r:uranium_235` | `ic2r:uranium_235_ingot` | 整球→锭 |
| `ic2r:uranium_238` | `ic2r:uranium_238_ingot` | 整球→锭 |

新注册的 `small_*`（4:1）**没有**旧 id，无需别名。  
同步更新 `docs/spec/id_migrations.md`。

### 9. 实施阶段建议

| 阶段 | 内容 | 验收 |
|:---|:---|:---|
| **P0 文档** | 本小节 + id_migrations 表 | 范围与命名无歧义 |
| **P1 粉系重命名** | `small_*_dust`(9) → `tiny_*_dust`；新 `small_*_dust`(4)；配方/标签/lang | 压缩机与 3×3/2×2 互转正确 |
| **P2 核同位素** | 球→锭；small 球→ tiny 粉；新 small 粉；`c:ingots/plutonium` 填实；MOX/RTG 统一 tag | 核线可合成，tag 非空 |
| **P3 机台副产** | 洗矿/离心/提取/作物输出 id 与数量 | 与 P1/P2 一致，无悬空 id |
| **P4 贴图与创造栏** | 锭贴图、小堆贴图、排序 | 视觉可区分 full/small/tiny |
| **P5 兼容与清理** | Remap、advancement、重复配方删除、测试用例 | 旧档物品不消失 |

### 10. 与钚标签讨论的关系

- **不采用**「仅把球塞进 `c:ingots/plutonium`」的最小补丁作为终态。  
- 终态：`plutonium_ingot` ∈ `c:ingots/plutonium`，形态与标签一致；小撮/小堆走 dust 标签。  
- 实施前可临时把现 `plutonium` 写入 tag 解配方，但 **不作为** 本重构完成标准。

### 11. 开放决策（实现前确认）

1. **核整单位是否一期就注册 `*_dust` 整粉**，还是只上锭 + small/tiny 粉、整粉二期再加？  
2. **洗矿/离心副产** 原 `small`（9:1）一律改 `tiny` 并保持数量，还是改为 `small`（4:1）并重做数值？  
3. **`uranium_235` / `uranium_238` 是否强制 `_ingot` 后缀**，还是保留短 id、仅改贴图与标签？  
4. **俄语 lang**：随扁平化删除，还是本批仍同步？

---
