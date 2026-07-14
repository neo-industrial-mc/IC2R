# IC2R 模块 Origin 标注（初版）

> **Work Unit**: W0.6  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §1 版权策略、§1.5 Origin 交付物、§5.1（0.5 模块 Origin）  
> **索引**: [docs/spec/README.md](README.md)  
> **扫描日期**: 2026-07-14  

---

## 0. 免责与判定约定

| 项 | 说明 |
|:---|:---|
| **性质** | 本文件为基于**当前仓库目录结构、类型命名、控制流特征与公开文档**的**工程判断**，用于现代化排序与审计线索。 |
| **非法律意见** | **不是**法律意见、版权结论或权利归属鉴定；不得单独作为对外合规证明。 |
| **维护节奏** | 随 **W1+** 重写/拆分/删残留同步更新；每完成核心域重写应回写本表状态。 |
| **路径基线** | Java 根包：`me.halfcooler.ic2r`（`src/main/java/me/halfcooler/ic2r/**`） |
| **规格交付位置** | 现代化约定放在 `docs/spec/origin.md`（本文件）；主文档 §1.5 曾写 `docs/copyright/ORIGIN.md`，二者语义相同，**以本文件为准**，避免双源。 |

### 0.1 状态枚举

| 状态 | 含义（工程口径） |
|:---|:---|
| **residual** | 移植/反编译时代残留仍占主导：类型图、控制流组织、组件分层与历史 IC2 系实现**明显同源**（包名/前缀可能已换为 `ic2r`）。高优先级走干净室式重写（§1.3）。 |
| **rewritten** | 已对 1.20.1 / Forge 等做过**显著适配或局部重写**，结构与历史有距离，但仍可辨出历史痕迹或同源模式。 |
| **original** | **IC2R 原创**或明确以本项目规格引入（如 GT 电网模式、集成插件、配置差分规则等），非从 IC2 源码结构照搬。 |
| **mixed** | 同一包/子系统内多状态并存；**主判据**写在「简短依据」列（以风险最大或体积最大子路径为准）。 |

### 0.2 初版判定方法（非源码 diff）

1. **目录与类型图**：是否仍为 `InvSlot*` 树、`TileEntityStandardMachine`、`EnergyNetLocal`/`Grid`/`Node`、`getNetworkedFields` 反射同步、XML `GuiParser` 等历史形态。  
2. **公开特征**：主文档/GTEU 文档标明的 IC2R 差分（GT Calculator、充电座规则等）；集成包（JEI/Jade/AE2）无对应 IC2 同源包。  
3. **现代化痕迹**：`recipe/v2` 序列化、Forge 1.20.1 适配、官方映射命名等 → 倾向 rewritten，**不**自动升为 original。  
4. **未做**：与任何第三方 IC2 jar 的逐文件哈希/逐行对照；因此标注粒度到**包/子系统**，不对单文件盖章。

---

## 1. 核心包 / 子系统标注表

路径为相对 `me.halfcooler.ic2r` 的包或逻辑模块；「后续现代化动作」对齐主文档 Work Unit，供排序用。

| 包或模块路径 | 状态 | 简短依据 | 后续现代化动作（可选） |
|:---|:---|:---|:---|
| `core/energy/`（整体） | **mixed** | 双模式入口与 GT 求解为原创；IC 路径网格/拓扑/统一计算器仍呈历史 EnergyNet 形态 | 规格+测驱动拆 Calculator；IC 路径干净室重写（阶段 1–2 延续） |
| `core/energy/EnergyNetMode` | **original** | `IC2R`/`GT` 配置切换枚举，项目自有双模策略 | 保持；Golden `EN-GT`/`EN-IC` 对齐 |
| `core/energy/grid/EnergyCalculatorGT` | **original** | GT 1A 包、方向优先级、超压/超流等按 GTEU 规格实现的求解器 | Golden EN-GT-*；与 path cache 委托边界写清 |
| `core/energy/grid/EnergyCalculatorUnified` | **residual** | IC 模式路径/分配/线损等核心求解，类职责与历史统一计算器同构 | 先测（W0.4+）再规格化重写；禁止重命名洗白 |
| `core/energy/grid/*`（`Grid`/`Node`/`EnergyNetLocal`/`ChangeHandler`/`EnergyPath` 等） | **residual** | 图模型、变更队列、本地网实例等控制流仍明显同源；已挂 GT 分支与配置钩子 | 拓扑可保留**语义**不变量，表达须重写；与 Calculator 解耦 |
| `core/energy/profile/*` | **mixed** | 档位/电缆展示部分服务双模式；结构仍贴机端用电侧 | 与 VoltageTier API 一并现代化 |
| `api/energy/**` | **residual** | `IEnergySink`/`Source`/`Conductor`、Load/Unload 事件等经典 EU-net API 面（包名已 `ic2r`） | 长期收敛为精简现代 API（§2.2）；兼容层标注 |
| `core/block/machine/**`（标准机与专用机） | **residual** | `TileEntityStandardMachine` 进度/耗电/升级槽/事件码；`TileEntityMacerator` 等大量同类 TE | W1.2 同步试点 → 配方/Inv 现代化；Golden SM-* |
| `core/block/machine/tileentity/TileEntityStandardMachine` | **residual** | 标准加工循环与 `guiProgress`/`InvSlot*` 组合为历史标准机骨架 | Sync 抽象 + snake_case NBT（W1.1–W1.5） |
| `core/block/tileentity/*`（`Ic2rTileEntity` 等） | **residual** | `getDeclaredMethod` 探测 `updateEntityServer/Client`、组件 map、网络字段协议痕迹 | **W1.3** 去反射 Tick；组件模型现代化 |
| `core/block/comp/**`（Energy/Process 等组件） | **residual** | TE 组件化仍沿用 IC 系组件挂载与 NBT 键习惯 | W1.5 NBT 试点；Handler 对外暴露（W2.x） |
| `core/block/invslot/**` | **residual** | 完整 `InvSlot`/`InvSlotProcessable*`/`InvSlotUpgrade` 树，非 Forge `IItemHandler` 一等公民 | **W2.1** InvSlot→Handler 委托试点 |
| `core/slot/**` | **residual** | 容器槽位与历史 GUI/Container 配套 | 随 Menu 现代化一并替换 |
| `core/recipe/**`（非 v2） | **residual** | `BasicMachineRecipeManager`、`RecipeInput*`、`AdvRecipe` 等运行时管理器形态同源 | 规格 RC-*；迁 RecipeManager |
| `core/recipe/v2/**` | **rewritten** | Serializer/`RecipeHolder` 等面向现代数据包/序列化，仍服务旧管理器语义 | **W2.3** 全链路 RecipeType 试点 |
| `api/recipe/**` | **residual** | `IMachineRecipeManager`/`Recipes` 门面等历史 API 形 | 与实现一并收敛 |
| `core/network/**` | **residual** | `NetworkManager` + `ReflectionUtil` 字段同步、`GrowingBuffer`/`TeUpdate*`/`SubPacketType` | **W1.1–W1.2** SyncKey/编解码；去掉字段名直出 |
| `api/network/**` | **residual** | `INetworkDataProvider`/`getNetworkedFields` 风格接口 | 随 Sync 抽象废弃或薄封装 |
| `core/gui/**` + `core/gui/dynamic/**` | **residual** | SAX `GuiParser`、XML GUI 方言、`DynamicContainer`/`IGuiValueProvider` | **W2.4** 冻结 XML、代码化 Menu/Screen 样板 |
| `core/block/machine/gui/**`、`.../container/**` | **residual** | 每机一份 Gui/Container，网络字段字面量大量 camelCase（见 naming_audit） | 与 W1.2/W2.4 同步收敛 |
| `core/block/reactor/**` + `api/reactor/**` | **residual** | 核电舱室/组件/热逻辑高风险同源域（Golden 高风险预留） | 强制干净室：规格→测→重写；勿先大重构 |
| `core/crop/**` + `api/crops/**` | **residual** | `CropCard`/`TileEntityCrop`/统计与生长字段网络同步，作物高风险域 | 同上；字段 snake_case 随 NS 迁移 |
| `core/world/**` | **rewritten** | 橡胶树等世界生成已接 1.20 生成管线；内容主题仍来自 IC 系 | DataGen/配置键规范化；行为规格后置 |
| `core/uu/**` | **residual** | UU 图/扫描/解析器（含反射读区块缓存痕迹），物质复制价值链 | 规格化后再动；高耦合扫描慎改 |
| `core/block/wiring/**` | **residual** | 电缆/变压器/储能/充电座 TE 与网绑定；充电座等规则或有 IC2R 差分但骨架同源 | 差分写入 Golden；结构重写排期电网之后 |
| `core/block/generator/**`、`heatgenerator/**`、`kineticgenerator/**`、`steam/**`、`storage/**` | **residual** | 各类发电机/热/动能/蒸汽/储能，与历史 TE 树同构 | 按域拆分重写；测切入点后置 |
| `core/block/personal/**`、`misc/**`、`beam/**` | **residual** | 个人安全/杂项方块/光束等，仍 TE+InvSlot+网络旧模式 | 低优先；触及时再标细 |
| `core/item/**` | **mixed** | 大量工具/装备/升级为玩法移植；部分逻辑已 1.20 重写；类型名 `Item*` 历史风格重 | 分批：电量物品 API → 现代能力 |
| `api/item/**`、`api/upgrade/**`、`api/tile/**` | **residual** | 电量物品、升级、扳手等公共面贴近 IC API 习惯 | API 瘦身与 §2.2 终态包对齐 |
| `core/init/**`、`core/ref/**` | **rewritten** | 注册/配置已 `ic2r` 命名空间与大量 snake_case path；配置键仍有 camelCase | W1.6–W1.7 注册拆分/Deferred；配置键后置 |
| `core/fluid/**`、`core/*Fluid*Manager*` | **mixed** | 流体桥接已 Forge 化；配方/热交换管理器仍旧 | W2.2 流体路径试点 |
| `core/util/**` | **mixed** | 通用工具 + `ReflectionUtil`/历史 Vector 工具等；非单一来源 | 消灭反射热路径；工具可逐步替换 |
| `core/event/**`、`core/proxy/**`、`core/command/**` | **rewritten** | 生命周期/代理/命令已按本模组与 1.20 重接；EnvProxy 仍偏厚 | **W3.3** EnvProxy 瘦身 |
| `core/sound/**`、`core/model/**`、`core/loot/**` | **rewritten** | 资源与战利品挂钩现代化适配；音频资源本身另案 | 资源专项；代码侧随引用清理 |
| `core/profile/**` | **original** | Classic/Experimental 等配置画像注解，项目侧门控 | 保持 |
| `forge/**` | **rewritten** | 1.20.1 Forge 能力/流体/网络/模型适配层，表达现代但服务旧 core | 阶段 3 platform SPI 下沉 common |
| `integration/jei/**`、`jade/**`、`ae2/**`、`jeirei/**` | **original** | 第三方集成插件，IC2R 自研对接 | 随 API 收敛改调用点 |
| `compat/**` | **original** | 配方/兼容薄封装（项目侧） | 保持精简 |
| `mixin/**` | **original** | 本项目 Mixin（如 RecipeManager） | 能删则删；有则文档化 |
| `api/**`（总览） | **mixed** | 整体为「可模组互操作的 IC 风格 API 面」+ 少量项目扩展；**主判据：residual** | 终态少而精；新 API 不得再扩历史形 |
| 根入口 `core/IC2R` 等 | **rewritten** | 模组主类/伤害源/爆炸等已本项目化；爆炸等玩法语义仍对齐 IC 系 | 行为规格挂钩；非首轮结构重写焦点 |

---

## 2. 按现代化风险汇总

| 风险带 | 模块（摘要） | 主状态 | 说明 |
|:---|:---|:---|:---|
| **P0 版权+行为** | EnergyNet IC 路径、标准机循环、Network 反射同步、InvSlot | residual | 先规格/测试再重写；W0.4–W1.5 主战场 |
| **P0 高风险玩法** | 核电、作物、传送/UU、复杂矿机 | residual | Golden 预留域；禁止无测大改 |
| **P1 结构去同源** | GUI XML、recipe 管理器、wiring/generator 树、api/* 历史面 | residual / mixed | 阶段 2–3 与 Forge 对齐并行 |
| **已差分/原创护栏** | `EnergyCalculatorGT`、`EnergyNetMode`、integration/* | original | 规格中标注 IC2R 来源；测保护 |
| **适配层** | forge/*、init/ref 注册、worldgen | rewritten | 不增加 residual 扩散即可 |

---

## 3. 与其它交付物的关系

| 交付物 | 关系 |
|:---|:---|
| [golden_suite.md](golden_suite.md) | Origin=residual 的域优先补 Golden 正文与测试链接 |
| [naming_audit.md](naming_audit.md) | 命名合规≠Origin 升级；camelCase 网络/NBT 多为 residual 症状 |
| [Modernization_Project.md](../Modernization_Project.md) §1.3 | residual 核心域默认干净室工作流 |
| GTEU 文档 | GT 模式 invariants 支撑 `EnergyCalculatorGT` = original 的工程依据 |

---

## 4. 修订记录

| 日期 | 版本 | 说明 |
|:---|:---|:---|
| 2026-07-14 | 0.1-draft | W0.6：核心包初版 residual / rewritten / original / mixed 表 |
