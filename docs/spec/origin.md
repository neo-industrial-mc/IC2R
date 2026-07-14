# IC2R 模块 Origin 标注

> **Work Unit**: W0.6 初版 → **G3.4** 按 G1–G3 实际交付回写  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §1 版权策略、§1.5 Origin 交付物、§5.1（0.5 模块 Origin）、§8.5 #4  
> **索引**: [docs/spec/README.md](README.md)  
> **扫描 / 回写日期**: 2026-07-14  
> **对照收口**: [phase1_closeout.md](phase1_closeout.md)、[phase2_closeout.md](phase2_closeout.md)、[phase3_closeout.md](phase3_closeout.md)

---

## 0. 免责与判定约定

| 项 | 说明 |
|:---|:---|
| **性质** | 本文件为基于**当前仓库目录结构、类型命名、控制流特征与公开文档**的**工程判断**，用于现代化排序与审计线索。 |
| **非法律意见** | **不是**法律意见、版权结论或权利归属鉴定；不得单独作为对外合规证明。 |
| **维护节奏** | 随重写/拆分/删残留同步更新；每完成核心域重写应回写本表状态。**禁止**为勾 §8.5 而假装 residual 清零。 |
| **路径基线** | Java 根包：`me.halfcooler.ic2r`（`src/main/java/me/halfcooler/ic2r/**`） |
| **规格交付位置** | 现代化约定放在 `docs/spec/origin.md`（本文件）；主文档 §1.5 曾写 `docs/copyright/ORIGIN.md`，二者语义相同，**以本文件为准**，避免双源。 |

### 0.1 状态枚举

| 状态 | 含义（工程口径） |
|:---|:---|
| **residual** | 移植/反编译时代残留仍占主导：类型图、控制流组织、组件分层与历史 IC2 系实现**明显同源**（包名/前缀可能已换为 `ic2r`）。高优先级走干净室式重写（§1.3）。 |
| **rewritten** | 已对 1.20.1 / Forge 等做过**显著适配或局部重写**，结构与历史有距离，但仍可辨出历史痕迹或同源模式；**或**本项目引入的可测纯逻辑切片（`*Math` / Sync codec 等）已替换局部路径，但宿主包仍含 residual。 |
| **original** | **IC2R 原创**或明确以本项目规格引入（如 GT 电网模式、platform SPI、集成插件、配置差分规则等），非从 IC2 源码结构照搬。 |
| **mixed** | 同一包/子系统内多状态并存；**主判据**写在「简短依据」列（以风险最大或体积最大子路径为准）。 |

### 0.2 判定方法（非源码 diff）

1. **目录与类型图**：是否仍为 `InvSlot*` 树、`TileEntityStandardMachine`、`EnergyNetLocal`/`Grid`/`Node`、`getNetworkedFields` 反射同步、XML `GuiParser` 等历史形态。  
2. **公开特征**：主文档/GTEU 文档标明的 IC2R 差分（GT Calculator、充电座规则等）；集成包（JEI/Jade/AE2）无对应 IC2 同源包。  
3. **现代化痕迹**：`recipe/v2`、`network/sync`、`*Math` 纯逻辑切口、Handler 适配、platform SPI、Deferred 注册拆分等 → 倾向 rewritten / original 切片，**不**自动把整个宿主包升为 original。  
4. **未做**：与任何第三方 IC2 jar 的逐文件哈希/逐行对照；因此标注粒度到**包/子系统/已证实切片**，不对单文件盖章。

### 0.3 G3.4 诚实结论（§8.5 #4）

| 项 | 判定 |
|:---|:---|
| §8.5 #4「Origin 文档中移植残留**核心模块清零**或仅剩标注兼容层」 | **未达成（gap）** |
| G1–G3 增量 | 多处 **rewritten 切片** 与 **mixed** 子系统；GT 电网 / platform SPI / 集成等 **original** 保持 |
| **禁止** | 将整表 P0 核心包改写为 `original`；宣称「核心 residual 清零」 |

**仍主导 residual 的 P0 核心（未清零）**：EnergyNet **图模型主体**（`Grid`/`Node`/`EnergyPath`，IC 求解已重写为 `IcEnergySolver`）、标准机 **TE 循环本体**、`network` **反射/TeUpdate 帧默认**、`InvSlot` **树主体**、核电/作物等高风险玩法域。

---

## 1. 核心包 / 子系统标注表

路径为相对 `me.halfcooler.ic2r` 的包或逻辑模块；「后续现代化动作」对齐主文档与 G1–G3 gap，供排序用。

| 包或模块路径 | 状态 | 简短依据（G1–G3 证据） | 后续现代化动作（可选） |
|:---|:---|:---|:---|
| `core/energy/`（整体） | **mixed** | 双模式入口与 GT 求解为 **original**；IC 路径网格/拓扑/统一计算器仍 **residual**；已抽出 `EnergyTransferMath` / `EnergyBridgeMath` 等 **rewritten** 可测切片 | IC Calculator 干净室重写；禁止重命名洗白 |
| `core/energy/EnergyNetMode` | **original** | `IC2R`/`GT` 配置切换枚举，项目自有双模策略 | 保持；Golden `EN-GT`/`EN-IC` 对齐 |
| `core/energy/EnergyBridgeMath` | **rewritten**（切片） | G2.8：EU↔FE 纯转换库 + 契约 [energy_bridge_contract.md](energy_bridge_contract.md)；非 IC 源码结构，工程侧现代化表达 | 保持测护；config 开关 / e2e FE 后置 |
| `core/energy/grid/EnergyCalculatorGT` | **original** | GT 1A 包、方向优先级、超压/超流等按 GTEU 规格实现的求解器 | Golden EN-GT-*；与 path cache 委托边界写清 |
| `core/energy/grid/EnergyCalculatorUnified` | **rewritten**（deprecated） | A40.3：标记 `@Deprecated`，仅保留为 GT path-cache 兼容层；IC 默认求解切至 `IcEnergySolver` | 后续可安全删除 |
| `core/energy/grid/IcEnergySolver` | **rewritten**（A40.3） | IC 模式现代求解器：复用 BFS+OptimizedGraph 路径设施，distribution/cable effects 以 `EnergyTransferMath` 为单一真源；控制流/命名与历史 `EnergyCalculatorUnified` 可区分 | 测护（EN-IC-* 全绿）；回接为 IC 默认 Calculator |
| `core/energy/grid/EnergyTransferMath` | **rewritten**（切片） | W0.4 / G1.3 / G3.3：inject/loss/distribute 纯逻辑切口 + 高测覆盖；**回接** IC 路径，非替换 Calculator 本体 | 继续扩边界测；主体仍 residual |
| `core/energy/grid/*`（`Grid`/`Node`/`EnergyNetLocal`/`ChangeHandler`/`EnergyPath` 等） | **residual** | 图模型、变更队列、本地网实例等控制流仍明显同源；已挂 GT 分支与配置钩子 | 拓扑可保留**语义**不变量，表达须重写 |
| `core/energy/profile/*` | **mixed** | 档位/电缆展示部分服务双模式；结构仍贴机端用电侧 | 与 VoltageTier API 一并现代化 |
| `api/energy/**` | **residual** | `IEnergySink`/`Source`/`Conductor`、Load/Unload 事件等经典 EU-net API 面（包名已 `ic2r`） | 长期收敛为精简现代 API（§2.2）；兼容层标注 |
| `core/block/machine/**`（标准机与专用机，整体） | **residual**（主判据） | 大量 `TileEntity*` 仍历史 TE 树；仅局部 Sync 绑定 / CycleMath 接线 | 域重写 + 测；勿整包标 rewritten |
| `core/block/machine/tileentity/StandardMachineCycleMath` | **rewritten**（切片） | G1.4 / G3.3：进度/长度/OC 纯逻辑 + SM-* 测绿；**TE 循环主体仍 residual** | 升级/真实 Inv 接线与 TE 本体重写后置 |
| `core/block/machine/tileentity/TileEntityStandardMachine` | **residual** | 标准加工循环与 `guiProgress`/`InvSlot*` 组合为历史标准机骨架；Sync 双写 + CycleMath 委托为现代化**贴片** | 切主 Sync；配方/Inv 现代化；Golden SM-* |
| `core/block/tileentity/*`（`Ic2rTileEntity` 等） | **mixed** | **W1.3**：显式 `ServerTicker`/`ClientTicker`，tick **不再** `getDeclaredMethod` 探测 → 该路径 **rewritten**；组件 map、网络字段协议、能力附着仍 **residual** 痕迹 | 组件模型现代化；cap 下沉 forge/SPI |
| `core/block/comp/**`（Energy/Process 等组件） | **mixed** | W1.5：Energy `energy_buffer` + `LegacyNbt` 试点 **rewritten** 边角；组件挂载与多数 NBT 键习惯仍 residual | Handler 对外（W2.x 已试点）；全库 snake_case 后置 |
| `core/block/invslot/**`（整体） | **mixed** | **InvSlot 树主体 residual**；`InvSlotTransferMath` + `InvSlotItemHandler` 对外 ITEM_HANDLER（W2.1/G2.1）为 **rewritten** 适配切片 | 真管道/AE2 e2e；可选收敛 InvSlot 表达 |
| `core/block/invslot/InvSlotTransferMath` | **rewritten**（切片） | 纯逻辑门闩/余量/布局定位；高测覆盖；契约 [item_handler_contract.md](item_handler_contract.md) | 保持；Handler 本体运行时测后置 |
| `core/slot/**` | **residual** | 容器槽位与历史 GUI/Container 配套 | 随 Menu 现代化一并替换 |
| `core/recipe/**`（非 v2，整体） | **mixed** | 运行时 `BasicMachineRecipeManager` 等仍 residual 形态；`MachineRecipeMatchMath` 为 **rewritten** 切片；materialize 后仍服务旧管理器 | tick 直查评估见 recipe_manager_query_eval；非 basic 异构后置 |
| `core/recipe/MachineRecipeMatchMath` | **rewritten**（切片） | G1.6 / W2.3：item/tag/名单纯匹配门闩 + 测；非运行时 ItemStack 全路径 | 与 bridge 语义对齐 |
| `core/recipe/v2/**` | **mixed** | Serializer/`RecipeHolder`/JSON 族 **rewritten**；`RecipeManagerMachineBridge` + basic 全链路（macerator/extractor/compressor）为工程桥接；运行时仍 materialize 到旧管理器 → **非**全路径 original | G2.2 residual：非直查 tick、非 basic 机 |
| `api/recipe/**` | **residual** | `IMachineRecipeManager`/`Recipes` 门面等历史 API 形 | 与实现一并收敛 |
| `core/network/**`（整体） | **mixed** | **主体积 residual**：`NetworkManager`/`GrowingBuffer`/`TeUpdate*`/`SubPacketType` 仍帧默认 + 字段名列表；**`network/sync` rewritten**；G1.1 已注册字段 TeUpdate **值路径优先 Sync**（`tryGet`/`trySet`）→ 混合双轨 | 全库切主 / 去反射默认；NS 扩字段 |
| `core/network/sync/**` | **rewritten** | W1.1–W1.2：`SyncKey`/`SyncCodec(s)`/`SyncedField`/`BlockEntitySync`；标准机 `gui_progress`/`active` 绑定；高测覆盖 | 扩注册面；最终废弃字段名直出 |
| `core/network` TeUpdate / 反射读字段 | **residual**（帧与未注册路径） | TeUpdate **帧仍默认**（`getNetworkedFields()` + 字符串名）；未注册字段仍反射 | G1.1 仍 open 的切主 Unit |
| `api/network/**` | **residual** | `INetworkDataProvider`/`getNetworkedFields` 风格接口 | 随 Sync 抽象废弃或薄封装 |
| `core/gui/**` + `core/gui/dynamic/**` | **residual**（主判据） | SAX `GuiParser`、XML GUI 方言、`DynamicContainer` 仍服务生产机；W2.4 冻结新增 XML + 代码样板；G2.3 storage_box 等代码 GUI 为 **rewritten** 切片 | 存量 guidef 迁移；删 Dynamic 兼容层 |
| `core/gui/code/**` | **rewritten** | W2.4 纯代码 Menu/Screen 样板（工程侧） | 扩生产 GUI 迁移 |
| `core/block/machine/gui/**`、`.../container/**` | **mixed** | 多数仍 XML/历史 Container；部分（如 storage_box）代码化 | 与 W2.4/G2.3 续迁 |
| `core/block/reactor/**` + `api/reactor/**` | **residual** | 核电舱室/组件/热逻辑高风险同源域（Golden 高风险预留）；**G1–G3 未域重写** | 强制干净室：规格→测→重写 |
| `core/crop/**` + `api/crops/**` | **residual**（主判据） | `CropCard`/`TileEntityCrop`/统计与生长字段网络同步；**G3.9** 仅抽出生长/存储/杂交资格纯逻辑切片，宿主仍 residual | 干净室重写后置；字段 snake_case 随 NS 迁移 |
| `core/crop/CropGrowthMath` | **rewritten**（切片） | G3.9 / §8.3：质量门槛、totalGrowth、reset 判定、cross base、storage accept + 单测；**回接** `TileEntityCrop`，非 TE 本体重写 | 保持测护；核电/采矿机同类切片后置 |
| `core/world/**` | **rewritten** | 橡胶树等世界生成已接 1.20 生成管线；内容主题仍来自 IC 系 | DataGen/配置键规范化；行为规格后置 |
| `core/uu/**` | **residual** | UU 图/扫描/解析器（含反射读区块缓存痕迹），物质复制价值链 | 规格化后再动；高耦合扫描慎改 |
| `core/block/wiring/**` | **residual** | 电缆/变压器/储能/充电座 TE 与网绑定；充电座等规则或有 IC2R 差分但骨架同源 | 差分写入 Golden；结构重写排期电网之后 |
| `core/block/generator/**`、`heatgenerator/**`、`kineticgenerator/**`、`steam/**`、`storage/**` | **residual** | 各类发电机/热/动能/蒸汽/储能，与历史 TE 树同构 | 按域拆分重写；测切入点后置 |
| `core/block/personal/**`、`misc/**`、`beam/**` | **residual** | 个人安全/杂项方块/光束等，仍 TE+InvSlot+网络旧模式 | 低优先；触及时再标细 |
| `core/item/**` | **mixed** | 大量工具/装备/升级为玩法移植；部分逻辑已 1.20 重写；类型名 `Item*` 历史风格重 | 分批：电量物品 API → 现代能力 |
| `api/item/**`、`api/upgrade/**`、`api/tile/**` | **residual** | 电量物品、升级、扳手等公共面贴近 IC API 习惯 | API 瘦身与 §2.2 终态包对齐 |
| `core/init/**`、`core/ref/**` | **rewritten** | 注册/配置已 `ic2r` 命名空间；**W1.6 / G1.8**：`Ic2rItems`/`Ic2rBlocks` **按域拆分** + 门面；**W1.7**：`Ic2rSoundEvents` 全类 **DeferredRegister**；配置键仍有 camelCase | 配置键规范化；其余注册类可继续 Deferred 化 |
| `core/ref/Ic2rSoundEvents` | **rewritten** | W1.7：SoundEvent 全类 Deferred/Holder + FmlMod bus；旧 `EnvProxy#registerSoundEvent` 拒注册 | 保持 |
| `core/fluid/**` | **mixed** | `FluidTransferMath` + `Ic2rFluidTank` 委托 / `Ic2rFluidTankHandler`（W2.2/G2.5）为 **rewritten** 适配；配方/热交换管理器与 Forge 类型泄漏仍 residual 痕迹 | 统一对外 FLUID_HANDLER；下沉 SPI |
| `core/fluid/FluidTransferMath` | **rewritten**（切片） | fill/empty 纯逻辑门闩；契约 [fluid_handler_contract.md](fluid_handler_contract.md) | 保持 |
| `core/util/**` | **mixed** | 通用工具 + `ReflectionUtil`/历史痕迹；非单一来源；热路径 Random 等 W1.4 已清部分 smell | 消灭反射热路径；工具可逐步替换 |
| `core/event/**`、`core/proxy/**`、`core/command/**` | **mixed** | 生命周期/命令已本模组化；**W3.2–W3.3 / G3.1–G3.2**：`isClient`/`getServer` 切片迁 SPI；**EnvProxy 上帝代理仍在**（G3.5）→ 未整包 original | E2–E6 继续退役 EnvProxy |
| `core/sound/**`、`core/model/**`、`core/loot/**` | **rewritten** | 资源与战利品挂钩现代化适配；音频资源本身另案 | 资源专项；代码侧随引用清理 |
| `core/profile/**` | **original** | Classic/Experimental 等配置画像注解，项目侧门控 | 保持 |
| `platform/services/**` | **original** | W3.1–W3.3：8 SPI 接口 + `PlatformServices`；项目为多 loader 引入的边界，**非** IC 移植结构；facet 部分仍 stub（实现在 forge） | 非 stub 化；可测时再补测（勿空转） |
| `forge/**` | **rewritten** | 1.20.1 Forge 能力/流体/网络/模型/`ForgePlatformServices`/`Platform*Forge` 适配层；表达现代但服务旧 core | 阶段 3 续：下沉 common、扩 SPI 真实现 |
| `integration/jei/**`、`jade/**`、`ae2/**`、`jeirei/**` | **original** | 第三方集成插件，IC2R 自研对接；AE2 FE 路径共享 `EnergyBridgeMath` | 随 API 收敛改调用点 |
| `compat/**` | **original** | 配方/兼容薄封装（项目侧） | 保持精简 |
| `mixin/**` | **original** | 本项目 Mixin（如 RecipeManager） | 能删则删；有则文档化 |
| `datagen/**` | **original** | W2.5：项目侧 DataGen（Tags 起步） | 扩 Recipes/Models/Lang |
| `api/**`（总览） | **mixed** | 整体为「可模组互操作的 IC 风格 API 面」+ 少量项目扩展；**主判据：residual** | 终态少而精；新 API 不得再扩历史形 |
| 根入口 `core/IC2R` 等 | **rewritten** | 模组主类/伤害源/爆炸等已本项目化；爆炸等玩法语义仍对齐 IC 系 | 行为规格挂钩；非首轮结构重写焦点 |

---

## 2. 按现代化风险汇总

| 风险带 | 模块（摘要） | 主状态 | G1–G3 后说明 |
|:---|:---|:---|:---|
| **P0 版权+行为** | EnergyNet **IC 路径**、标准机 **TE 循环**、Network **反射/TeUpdate 帧**、InvSlot **树** | **仍 residual**（宿主） | 仅有 Math/Sync/Handler **切片** rewritten；**未**核心清零 |
| **P0 高风险玩法** | 核电、作物、传送/UU、复杂矿机 | **residual** | Golden 预留；G1–G3 **未**域重写 |
| **P1 结构去同源** | GUI XML 存量、recipe 旧管理器、wiring/generator 树、api/* 历史面 | residual / mixed | 冻结+样板/bridge 试点；存量仍大 |
| **已差分/原创护栏** | `EnergyCalculatorGT`、`EnergyNetMode`、`platform/services`、integration/*、datagen | **original** | 规格中标注 IC2R 来源；测保护 |
| **工程现代化切片** | `network/sync`、`*TransferMath`/`*CycleMath`/`*MatchMath`/`EnergyBridgeMath`、Items/Blocks 域拆、SoundEvents Deferred、Fluid/Inv Handler 适配 | **rewritten** | **不**等于宿主包 original；回写时必须写「切片」 |
| **适配层** | forge/*、init/ref 注册、worldgen | rewritten | 不增加 residual 扩散即可 |

---

## 3. G1–G3 切片对照（证据索引）

| 域 | Origin 变化（相对 W0.6） | 证据 / Unit |
|:---|:---|:---|
| EnergyNet GT | 保持 **original** | `EnergyCalculatorGT`；GTEU 规格 |
| EnergyNet IC | 保持 **residual**；+ **EnergyTransferMath rewritten 切片** | W0.4、G1.3、G3.3；包级 grid 仍 ~3% |
| Energy bridge | **+ EnergyBridgeMath rewritten** | G2.8；[energy_bridge_contract.md](energy_bridge_contract.md) |
| network | **residual → mixed**；`sync/**` **rewritten**；TeUpdate 值路径 Sync 优先 | W1.1–W1.2、G1.1；帧仍默认 |
| 标准机 | 主体 **residual**；+ **CycleMath rewritten** | G1.4、G3.3；TE 本体未测透 |
| Tick | 反射探测 **已移除**（路径 rewritten） | W1.3；见 phase1 §6.3 #2 |
| InvSlot / Fluid | **residual → mixed**；Math + Handler **rewritten 切片** | W2.1–W2.2、G2.1、G2.5 |
| recipe | **residual → mixed**；MatchMath/v2/bridge | W2.3、G1.6、G2.2 |
| platform SPI | **+ original** | W3.1–W3.3 |
| SoundEvents / Items·Blocks 域拆 | **rewritten** 工程侧 | W1.6–W1.7、G1.8 |
| §8.5 #4 核心 residual 清零 | **未达成** | 本文件 §0.3；phase3 §1 #4 |

---

## 4. 与其它交付物的关系

| 交付物 | 关系 |
|:---|:---|
| [golden_suite.md](golden_suite.md) | Origin=residual 的域优先补 Golden 正文与测试链接 |
| [naming_audit.md](naming_audit.md) | 命名合规≠Origin 升级；camelCase 网络/NBT 多为 residual 症状 |
| [Modernization_Project.md](../Modernization_Project.md) §1.3 / §8.5 #4 | residual 核心域默认干净室；清零前不得勾 #4 |
| [phase1_closeout.md](phase1_closeout.md) / [phase2_closeout.md](phase2_closeout.md) / [phase3_closeout.md](phase3_closeout.md) | G1–G3 切片与 gap 的权威收口证据 |
| GTEU 文档 | GT 模式 invariants 支撑 `EnergyCalculatorGT` = original 的工程依据 |

---

## 5. 修订记录

| 日期 | 版本 | 说明 |
|:---|:---|:---|
| 2026-07-14 | 0.1-draft | W0.6：核心包初版 residual / rewritten / original / mixed 表 |
| 2026-07-14 | 0.2-g3.4 | **G3.4**：按 G1–G3 实际交付回写；显式 §8.5 #4 **未**核心 residual 清零；EnergyNet/network/标准机/Inv·Fluid/recipe 标 mixed 或 rewritten 切片；platform SPI = original；Sound/Items·Blocks 工程侧 rewritten |
