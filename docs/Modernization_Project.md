# IC2R Code Modernization Project

**\- Project Specification \-**

> **目标**：在**代码层面**与原版 IC2 玩法同源（行为/数值可对照），但**编程思想与表达形式完全现代化**；并以此作为规避代码版权风险的主路径。  
> **资源文件**（纹理、模型、音效、部分 UI 美术）**不在本文档范围内**，另案处理。  
> **关联文档**：[GTEU_Migration_Project.md](GTEU_Migration_Project.md)、[GTEU_GT_Reference.md](GTEU_GT_Reference.md)  
> **进度状态**（会话恢复用）：[Modernization_Progress.md](Modernization_Progress.md)（若文件不存在则由主 Agent 在首个小阶段前创建）

---

## ⚡ Agent Session Protocol（会话自治执行协议）

> **本节优先于其余章节。**  
> 新会话若用户指示「按现代化文档推进 / 继续现代化 / 执行 Modernization」，主 Agent **仅需阅读本文档 + `docs/Modernization_Progress.md`（若存在）** 即可开工，不必先通读整个代码库。  
> 规格细节在 §0 之后；执行纪律以本节为准。

### A.0 会话目标

在**不替用户 commit** 的前提下，按**小阶段（Work Unit）**推进迁移与验证；每完成一个小阶段，主 Agent **只向用户输出一句话总结**，然后**停止并等待**用户手动 commit 与下一步指令。

### A.1 角色分工

| 角色 | 职责 | 禁止 |
|:---|:---|:---|
| **主 Agent** | 读进度 → 选定下一个 Work Unit → 派发 Subagent → 汇总验证结果 → **一句话总结** → 更新 `Modernization_Progress.md` → **停等用户** | 不 `git commit` / 不 `git push`；不一次做多个 Work Unit；不跳过验证 |
| **Migrate Subagent** | 仅实现当前 Work Unit 的代码/文档改动 | 不扩大范围；不改无关模块；不 commit |
| **Verify Subagent** | 只读或只跑检查：编译/测试/命名扫描/清单对照 | 不「顺手修功能」扩大 diff；失败时报告复现步骤 |
| **用户** | 阅读一句话总结 → **手动 commit** → 回复继续 / 暂停 / 改方向 | — |

### A.2 硬性规则

1. **唯一启动输入**：本文档 + `docs/Modernization_Progress.md`。需要代码上下文时，由 Subagent 按 Work Unit 的「触及路径」自行检索，主 Agent 不先做全库漫游。  
2. **一次一个 Work Unit**：ID 见 §A.5（如 `W0.1`、`W1.2`）。完成并验证前不得开始下一个。  
3. **禁止 Agent commit**：任何 `git commit` / `git push` / 改 git config 均禁止。暂存（`git add`）默认也不做，除非用户明确要求。  
4. **先测后改（能测的模块）**：涉及行为的改动遵循 §1.3；先补/改测试再改实现。  
5. **验证不过 = 未完成**：不得把失败的 Work Unit 标为 done；应修复或回滚到可编译状态后，用一句话说明 **BLOCKED** 原因。  
6. **版权红线**：遵守 §1；禁止无规格的大段重命名洗白。  
7. **用户未说继续前不得自动开下一 Unit**。用户说「继续」= 做进度表中的下一个 pending Unit。

### A.3 单次 Work Unit 标准流程

```
1. 主 Agent 读取 Modernization_Progress.md
2. 取第一个 status=pending 的 Work Unit（或用户指定的 ID）
3. 将该 Unit 标为 in_progress（写回 Progress 文件）
4. 启动 Migrate Subagent（prompt 见 §A.6）
5. Migrate 返回后，启动 Verify Subagent（prompt 见 §A.6）
6. 若 Verify 失败：主 Agent 再派 Migrate 修，或自行小修；最多 3 轮；仍失败 → BLOCKED
7. 成功：更新 Progress（done + 验证摘要 + 建议 commit message）
8. 主 Agent 对用户输出【唯一回复正文】：一句话总结（§A.4）
9. 停止，等待用户手动 commit 与「继续」
```

并行规则：

- **同一 Work Unit 内**可串行多个 Subagent（先迁移后验证）。  
- **禁止**并行两个会改同一批文件的 Migrate。  
- 只读 Verify 可与无重叠文件的探索并行（通常不需要）。

### A.4 一句话总结格式（主 Agent 对用户的唯一正文）

成功时，**整条回复只有一句**（可附建议 commit message 作为同一句的一部分），模板：

```text
[W{x.y} 完成] {做了什么}；验证：{test/compile 结果}。请手动 commit（建议：{type}: {short}）。
```

示例：

```text
[W0.1 完成] 已接入 JUnit5/JaCoCo 与空测试烟测；验证：gradlew test 通过。请手动 commit（建议：test: bootstrap junit5 and jacoco）。
```

失败/阻塞时，同样一句：

```text
[W{x.y} BLOCKED] {失败点}；原因：{一句话}；需你决策：{选项A / 选项B}。
```

**禁止**在停等 commit 时输出长篇报告、文件列表或下一阶段计划（除非用户问）。细节写入 `Modernization_Progress.md` 的 `last_notes`。

### A.5 Work Unit 目录（小阶段 = 一次 commit 粒度）

状态机：`pending` → `in_progress` → `done` | `blocked` | `skipped`。

#### 阶段 0 — 护栏

| ID | 标题 | 触及路径（指引） | 完成定义 (DoD) | 验证命令 |
|:---|:---|:---|:---|:---|
| **W0.1** | 测试基建 | `build.gradle`, `src/test/**` | JUnit 5 可运行；至少 1 个占位测试通过；JaCoCo 插件可选已接 | `gradlew test` |
| **W0.2** | Progress/规格目录 | `docs/spec/`, `docs/Modernization_Progress.md` | 进度文件与 `docs/spec/README.md` 存在；Golden 大纲骨架就位 | 文件存在性检查 |
| **W0.3** | Golden Suite 大纲 | `docs/spec/golden_suite.md` | 含 EnergyNet IC/GT、标准机、配方、NBT 条目表（可先无用例正文） | 文档结构检查 |
| **W0.4** | EnergyNet 可测切口 + 首批测试 | `core/energy/**`, `src/test/**/energy/**` | ≥3 个有意义断言的电网纯逻辑/半纯逻辑测试；不依赖客户端 | `gradlew test` |
| **W0.5** | 命名审计 | `docs/spec/naming_audit.md` | 扫描报告：非 snake_case 字面量/网络字段/NBT 键抽样列表 + 修复优先级 | 报告非空且含路径 |
| **W0.6** | Origin 初版 | `docs/spec/origin.md` | 核心包标注 residual / rewritten / original | 文档检查 |

#### 阶段 1 — 现代 Java 与基础设施

| ID | 标题 | 触及路径（指引） | DoD | 验证 |
|:---|:---|:---|:---|:---|
| **W1.1** | Sync 抽象骨架 | `core/network/**` 或未来 `common/network/**` | `SyncKey`/等价物 + 编解码接口；旧反射路径仍可用 | `gradlew test` + 编译 |
| **W1.2** | 标准机同步试点 | `TileEntityStandardMachine` 及 1 台子类（如 Macerator） | 进度/active 等走新同步或双写；测试覆盖编解码往返 | `gradlew test` |
| **W1.3** | 去反射 Tick | `Ic2rTileEntity` 等 | 无 `getDeclaredMethod` 探测 update；显式 Ticker | `gradlew test` + 编译 |
| **W1.4** | 清理致命味道 | 命中 `Vector` recipes、`RandomSource.create()` 热路径等 | 本 Unit 列出的 smell 清零或仅剩登记豁免 | 编译 + 相关测试 |
| **W1.5** | NBT/网络字面量 snake_case（试点域） | 标准机 + Energy 组件 NBT | 新键 snake_case；旧键可读；迁移测过 | `gradlew test` |
| **W1.6** | 注册拆分（Items 或 Blocks 其一） | `Ic2rItems` 或 `Ic2rBlocks` | 按域拆文件；行为不变；编译通过 | `gradlew build` 或 compile |
| **W1.7** | Deferred/Holder 试点 | 单一注册类别 | 一类注册完全 Deferred/Holder 化 | 编译 |
| **W1.8** | 阶段 1 收口 | 文档 + 覆盖率 | 对照 §6.3 勾选；覆盖率达 §4.5 阶段 1 门槛或记 gap | `gradlew test jacoco*`（若已配） |

#### 阶段 2 — Forge 生态对齐

| ID | 标题 | DoD | 验证 |
|:---|:---|:---|:---|
| **W2.1** | InvSlot → Handler 委托试点 | 1 类机器对外 ITEM_HANDLER 可用 | 编译 + 单测/说明性测例 |
| **W2.2** | 流体适配收窄试点 | 1 条 fill/empty 路径单测绿 | `gradlew test` |
| **W2.3** | 配方走 RecipeManager 试点 | 1 个 RecipeType 全链路 | `gradlew test` |
| **W2.4** | 冻结 XML：新 UI 代码化样板 | 文档约定 + 样板 Menu/Screen 或迁移 1 个简单 GUI | 编译 |
| **W2.5** | DataGen 起步 | 至少 Tags 或 Recipes 一类生成 | `gradlew runData` 或等价 |
| **W2.6** | 阶段 2 收口 | §7.7 勾选或 gap 列表 | 测试 + 文档 |

#### 阶段 3 — 架构瘦身 / 多加载器

| ID | 标题 | DoD | 验证 |
|:---|:---|:---|:---|
| **W3.1** | platform SPI 草案 | 接口文件 + 依赖方向说明 | 编译 |
| **W3.2** | 迁移 1 个调用点到 SPI | common 一处不再直依赖 Forge 实现 | 编译 + 测试 |
| **W3.3** | EnvProxy 瘦身切片 | 删/缩一组代理方法 | 编译 + 测试 |
| **W3.4** | NeoForge 骨架（可后置版本线） | 模块或文档级迁移计划落地其一 | 按当时仓库策略验证 |
| **W3.5** | 阶段 3 收口 | §8.5 勾选或明确延期项 | 文档 + 测试 |

> 后续可在 Progress 文件中**追加** `W{x.y}` 子单元，但不得破坏「一次一个、完成即停等 commit」规则。

### A.6 Subagent Prompt 模板

#### Migrate Subagent

```text
你是 IC2R 现代化 Migrate Subagent。只读并遵守：
- docs/Modernization_Project.md（版权 §1、命名 §3、当前阶段章节）
- 当前 Work Unit：{ID} {标题}
- DoD：{...}
- 触及路径指引：{...}

任务：仅实现该 Work Unit，最小 diff。
禁止：git commit/push；修改超出 Unit 的重构；无测试的行为变更。
完成后回报：变更文件列表、如何验证、风险点。
```

#### Verify Subagent

```text
你是 IC2R 现代化 Verify Subagent（只验证）。
Work Unit：{ID}
DoD：{...}
要求：
1) 对照 DoD 逐条是否满足
2) 运行：{验证命令}
3) 快速检查是否误改玩法数值/无关文件
输出：PASS 或 FAIL + 失败证据；不要扩大修改范围（FAIL 时最多给出建议补丁要点）。
```

### A.7 `docs/Modernization_Progress.md` 格式（主 Agent 维护）

若文件不存在，主 Agent 在开始 **W0.1 之前** 用下列骨架创建：

```markdown
# IC2R Modernization Progress

**active_unit:** none
**last_completed:** none
**updated:** YYYY-MM-DD

## Queue

| ID | status | last_notes |
|----|--------|------------|
| W0.1 | pending | |
| W0.2 | pending | |
| W0.3 | pending | |
| W0.4 | pending | |
| W0.5 | pending | |
| W0.6 | pending | |
| W1.1 | pending | |
| W1.2 | pending | |
| W1.3 | pending | |
| W1.4 | pending | |
| W1.5 | pending | |
| W1.6 | pending | |
| W1.7 | pending | |
| W1.8 | pending | |
| W2.1 | pending | |
| W2.2 | pending | |
| W2.3 | pending | |
| W2.4 | pending | |
| W2.5 | pending | |
| W2.6 | pending | |
| W3.1 | pending | |
| W3.2 | pending | |
| W3.3 | pending | |
| W3.4 | pending | |
| W3.5 | pending | |

## Last session

- unit:
- result:
- suggested_commit:
- verify_log:
```

### A.8 用户指令速查

| 用户说 | 主 Agent 做 |
|:---|:---|
| `开始现代化` / `按文档推进` | 读 Progress → 执行下一个 pending Unit → 一句总结 → 停 |
| `继续` | 同上 |
| `做 W1.3` | 仅执行指定 Unit |
| `跳过 Wx.y` | 标 skipped，一句确认，不停下下个除非再说继续 |
| `状态` | 一句：当前 last_completed / next pending |
| `暂停` | 不启动新 Unit |

### A.9 启动检查清单（每个会话开头，主 Agent 默做）

1. 读本节 §A 与 `Modernization_Progress.md`  
2. 确认工作区无用户未完成的半截手工修改冲突（若有 diff，一句询问还是纳入当前 Unit）  
3. 选定 Unit → 派 Subagent → 验证 → **一句总结** → 停  

---

## 0 项目目标与边界

### 0.1 一句话目标

把当前「反编译迁移 + 修复」代码库，演进为：

- **行为规格驱动**（玩法同源）  
- **现代 Java / 现代 Minecraft 工程实践**  
- **可测试、可多加载器、可长期维护**  
- **在结构与表达上与原 IC2 源码可区分**（版权安全）

### 0.2 非目标（明确不做）

| 非目标 | 说明 |
|:---|:---|
| 改变默认玩法数值 | 除非有独立配置开关；默认保持 IC2R 既定保真策略 |
| 本文档内处理资源版权 | 贴图/模型/音效另开资源现代化专项 |
| 一次性推倒重写上线 | 禁止 Big Bang；必须分阶段可回滚 |
| 为“现代”而引入无关技术栈 | 不因潮流强行上 Kotlin 等（可作为后续选项，非本阶段硬要求） |

### 0.3 成功判据（Definition of Done）

1. **版权向**：核心子系统（电网、机器循环、网络、配方、库存）以**规格 + 自研实现**为主；公开 API/包名/类型命名与原 IC2 源码结构显著不同；无大段可逐行对照的反编译残留。  
2. **工程向**：关键路径具备自动化测试；构建可在 CI 中验证。  
3. **命名向**：注册名、路径、NBT/网络字面量、翻译键等全面符合现代 MC `snake_case` 规范。  
4. **现代 Java 向**：阶段 1 清单项关闭（见 §6）；Work Unit **W1.\*** 均为 `done`。  
5. **生态向**：阶段 2 清单项关闭（见 §7）；**W2.\*** 均为 `done`。  
6. **架构向**：阶段 3 清单项关闭（见 §8）；**W3.\*** 均为 `done`；加载器逻辑收敛到 `platform`。  
7. **玩法向**：与冻结的行为黄金用例集（Golden Suite）对照通过。  
8. **执行向**：全程遵守 §A（Agent Session Protocol）：小阶段验证通过 → 用户手动 commit。

---

## 1 最高优先级：代码版权策略

> 本项目首先是**法律与伦理约束下的工程问题**，其次才是重构美学问题。

### 1.1 基本原则

| 原则 | 含义 |
|:---|:---|
| **行为可同源，表达须自有** | 玩法/数值可对齐 IC2；类结构、命名体系、控制流组织、注释文案应现代化重写 |
| **规格先于代码** | 先写/冻结行为规格与测试，再改实现；禁止“对着反编译边抄边改”作为主路径 |
| **结构去同源** | 包布局、类型命名、字段同步方式、GUI 描述方式等与原 IC2 拉开距离 |
| **增量可审计** | 每个 PR 应能说明：改了哪层、为何不增加版权风险、测了什么 |
| **资源另案** | 代码现代化完成度不以资源替换为前提；但发布策略上资源与代码许可应分开表述 |

### 1.2 允许 / 禁止

**允许**

- 参照公开游戏行为、Wiki、社区观测、自有测试世界录制，编写行为规格  
- 保留已在本仓库**原创**的逻辑（GT 电网模式、矿石过滤卡、充电座规则变更等），并在规格中标注来源为 IC2R  
- 使用 Minecraft / Forge / NeoForge / Fabric **公共 API** 的惯用模式  
- 为兼容保留**数据迁移**（旧 NBT 键 → 新键），迁移表写在规格中  

**禁止**

- 大段保留反编译控制流仅改名（“重命名式洗白”）  
- 复制原 IC2 特有注释、标识符体系、异常文案、调试字符串  
- 在新架构中继续扩散 `ic2` 风格内部 API 作为长期公共面  
- 无测试覆盖的“等价重写”直接合入主干  

### 1.3 干净室式工作流（推荐默认）

```
[行为观测 / 既有 Golden 用例]
        │
        ▼
[书面规格 Specification]  ──►  [单元/属性测试先红]
        │
        ▼
[现代实现 Implementation] ──►  [测试转绿]
        │
        ▼
[与旧实现并行对照 Diff 跑] ──►  [切换默认路径 / 删除旧实现]
```

对高风险模块（EnergyNet、核电、作物、传送、配方图）**强制**走此流程。

### 1.4 与“原模组同源”的精确定义

本文档中的**同源**仅指：

- 玩家可感知的机制与默认数值（在 IC2R 已声明的差异列表之外）  
- 物品/方块注册 ID 的可迁移性（`ic2r:` 命名空间内可演进）  
- 存档与合成的可升级路径  

**不指**：

- Java 类型名、包名、方法名与原 IC2 源码一致  
- 网络协议字节布局与原 IC2 一致  
- 内部组件图与原 IC2 一致  

### 1.5 版权相关交付物

| 交付物 | 用途 |
|:---|:---|
| `docs/spec/` 行为规格集 | 证明实现来自规格而非抄写 |
| Golden Suite 测试 | 证明行为稳定且可回归 |
| `docs/copyright/ORIGIN.md`（后续可建） | 标注各模块：移植残留 / 已重写 / 原创 |
| PR 模板中的 Origin checklist | 强制作者声明改动来源 |

---

## 2 全局工程约束

### 2.1 语言与运行时

| 项 | 当前 | 现代化目标 |
|:---|:---|:---|
| Java | 17 | 17（1.20.1 周期）；迁移 NeoForge 新版本时可升 21 |
| 映射 | official | 保持 official |
| 构建 | Gradle + FG | 阶段 3 评估 Architectury + 多 loader 插件 |
| 测试 | **无** | JUnit 5 +（可选）GameTest / 无头服务器测试 |

### 2.2 包结构目标（阶段 3 终态示意）

```
me.halfcooler.ic2r
├── api/                 # 稳定对外 API（少而精，语义现代）
├── common/              # 加载器无关：逻辑、规格实现、网络抽象
│   ├── energy/
│   ├── machine/
│   ├── recipe/
│   ├── inventory/
│   ├── network/
│   └── world/
├── platform/            # 加载器适配 SPI
│   ├── services/        # ServiceLoader / ExpectPlatform 接口
│   └── ...
├── forge/ 或 neoforge/  # 实现 platform（迁移期可并存）
├── fabric/              # 阶段 3+ 
├── client/              # 仅客户端
└── integration/         # JEI / Jade / AE2 …
```

原则：

- **common 零依赖** Forge/Fabric 实现类型  
- **platform** 只做注册、Capability/Attachment、网络发送、生命周期钩子  
- 禁止在机器逻辑中 `import net.minecraftforge...`（阶段 3 硬门槛）

### 2.3 模块依赖方向（单向）

```
integration → common/api
client → common
platform-impl → platform-api + common
common → api
api →（仅 JDK + 必要的 MC API 抽象，尽量薄）
```

---

## 3 命名规范（现代 Minecraft + 版权去同源）

### 3.1 分层约定

| 层 | 规范 | 示例 |
|:---|:---|:---|
| **Java 类型** | `PascalCase`（Java 语言惯例） | `MaceratorBlockEntity` |
| **Java 方法/字段** | `camelCase` | `getWorkingVoltage()` |
| **Java 常量** | `UPPER_SNAKE_CASE` | `MAX_SINK_TIER` |
| **包名** | 全小写，点分 | `...common.energy.grid` |
| **注册 ID / 路径 path** | `snake_case` | `electric_furnace` |
| **ResourceLocation** | `namespace:path`，path 仅 `[a-z0-9/._-]` | `ic2r:textures/block/machine.png` |
| **翻译键** | `snake_case` 段 | `gui.ic2r.electric_furnace` |
| **NBT 键（新）** | `snake_case` | `energy_buffer`，`sink_tier` |
| **网络字段逻辑名（新）** | `snake_case` | `gui_progress`（禁止反射字段名直出） |
| **配置键** | `snake_case` / TOML 惯用 | `energy_net_mode` |
| **数据包路径** | 目录与文件 `snake_case` | `data/ic2r/recipes/...` |
| **Tag** | `snake_case` | `forge:ingots/copper`（遵循社区 tag 惯例） |

> **重要澄清**：  
> “驼峰/Pascal → 下划线”针对的是**字面量与跨边界标识符**（注册名、NBT、网络、lang、资源路径），**不是**把 Java 源码改成 `snake_case` 方法名（那既违反 Java 惯例，也无助于可读性）。  
> Java 类型名则应**摆脱 IC2 历史前缀风格**（见 §3.3），这是版权去同源的一部分。

### 3.2 字面量现代化规则

1. **禁止**在新代码中新增 camelCase 注册 path（如 `geoGenerator`）。  
2. **禁止**网络同步再使用 Java 字段名字符串（如 `"rotationSpeed"`）；改为显式 codec 键 `rotation_speed`。  
3. **NBT**：新键一律 `snake_case`；读档时兼容旧键一版（`LegacyNbt` 适配层），兼容期结束后删除。  
4. **已是 snake_case 的注册名**保持稳定，避免无意义破坏性变更。  
5. **破坏性重命名**必须：  
   - 写入 `docs/spec/id_migrations.md`  
   - 提供 datapack/存档迁移或至少 changelog 大字警告  

### 3.3 类型命名去 IC2 同源（强制方向）

| 旧风格（移植残留） | 新风格（目标） |
|:---|:---|
| `TileEntityMacerator` | `MaceratorBlockEntity` |
| `TileEntityStandardMachine` | `StandardProcessingMachine` / `ProcessingMachineBlockEntity` |
| `Ic2rTileEntity` | `MachineBlockEntity` / `Ic2rBlockEntity`（项目前缀可保留） |
| `InvSlot*` | `MachineSlot` / `*ItemHandler` 语义名 |
| `NetworkManager` + 反射 | `BlockEntitySync` / `SyncedData` |
| `Recipes.macerator` 静态 | `ModRecipeTypes.MACERATOR` + `RecipeManager` 查询 |

类名允许保留 `Ic2r` 项目前缀以标识模组；**避免**继续扩散原版 IC2 的 `TileEntityXxx` / `IEnergy*` 历史镜像作为长期公共 API（内部迁移期可 `@Deprecated` 桥接）。

### 3.4 命名审计清单（阶段 0 产出）

- [ ] 扫描所有 `register("...")` / `getIdentifier("...")` 非 snake_case  
- [ ] 扫描 `getNetworkedFields()` 返回的字符串  
- [ ] 扫描 NBT `put/get` 键  
- [ ] 扫描 lang JSON 键风格不一致处  
- [ ] 产出 `docs/spec/naming_audit.md` 与批量修复 PR 计划  

---

## 4 测试策略（全项目单元测试基座）

> 测试是版权重写与行为保真的**共同地基**。没有测试的重构 = 赌博。

### 4.1 测试金字塔

```
        /  E2E 少量 \          手动清单 + 可选 GameTest
       / 集成测试    \         最小服务器 / 配方加载 / 注册烟测
      / 单元与属性测试 \       电网、配方匹配、数值、NBT 迁移（主力）
```

### 4.2 技术选型（阶段 0 锁定）

| 层级 | 推荐 | 说明 |
|:---|:---|:---|
| 单元测试 | JUnit 5 + AssertJ（可选） | 不启游戏；纯逻辑 |
| 属性测试 | jqwik 或手工参数化 | 电压/电流/线损边界 |
| 配方/数据 | 用 MC 测试 fixtures 或抽纯函数 | 先测匹配器与序列化 |
| 集成 | Forge GameTest 或 CI `runServer` 烟测 | 阶段 1 末引入 |
| 覆盖率 | JaCoCo | 核心包门槛逐步提高 |

### 4.3 目录布局

```
src/
  main/java/...
  test/java/me/halfcooler/ic2r/
    energy/
    recipe/
    inventory/
    machine/
    nbt/
    naming/
  test/resources/
    fixtures/          # 样例 NBT、配方 JSON 片段
```

### 4.4 Golden Suite（行为黄金用例）——必须先冻结

下列模块**先写规格与测试，再动实现**：

#### 4.4.1 EnergyNet（IC 模式）

- 单源单汇、路径选择、线损后 EU 到达量  
- 多汇分配策略（与现行 `EnergyCalculatorUnified` 对齐的规格描述）  
- 绝缘击穿 / 导体熔断 / 过压爆炸条件  
- 变压器 packet 倍率（升压/降压）  

#### 4.4.2 EnergyNet（GT 模式）

- 见 [GTEU_Migration_Project.md](GTEU_Migration_Project.md) invariants  
- 1A 包不可拆分、空闲 1A 请求、maxAmps 公式  
- 超压/超流熔断、方向优先级  

#### 4.4.3 标准处理机

- 进度、耗电、中断、输出满阻塞  
- 升级：超频、变压器、储能、拉取/推送  
- 配方匹配与消耗顺序  

#### 4.4.4 配方系统

- `IRecipeInput` 匹配（item / tag / ore 语义）  
- 黑白名单（回收机等）  
- JSON 反序列化稳定性  

#### 4.4.5 NBT / 同步

- 旧键兼容读  
- 新键写出  
- 同步字段编解码往返  

#### 4.4.6 其他高风险

- 核电热与爆炸阈值（至少纯函数部分）  
- 作物生长关键公式（可测部分）  
- UU 图算法（`UuGraph`）  
- 流体容器 fill/empty  

### 4.5 覆盖率门槛（渐进）

| 阶段 | 核心包行覆盖率目标 | 核心包定义 |
|:---|:---|:---|
| 阶段 0 结束 | ≥ 40% | `energy.grid` + recipe 匹配器 |
| 阶段 1 结束 | ≥ 60% | + machine 标准循环 + network codec |
| 阶段 2 结束 | ≥ 70% | + inventory/fluid 适配 |
| 阶段 3 结束 | ≥ 75% | common 全量（不含 client 渲染） |

### 4.6 CI 要求

- PR 必须：`test` 通过  
- main 分支：coverage 报告归档  
- 禁止 `--no-verify` 合入核心包  

### 4.7 测试编写规范

1. 测试名：`method_condition_expected` 或中文规格编号 `@Spec("EN-IC-014")`  
2. 禁止依赖真实世界存档路径  
3. 时间相关使用可注入的 `TickClock` 抽象（阶段 1 引入）  
4. 随机逻辑可注入 `RandomSource` / seed  
5. 禁止“睡眠等异步”的脆弱测试  

---

## 5 阶段 0 — 护栏与审计（先于一切重构）

**目标**：在不改变大规模架构的前提下，建立可回归能力与命名/版权基线。

### 5.1 工作项

| ID | 工作项 | 产出 |
|:---|:---|:---|
| 0.1 | 启用 `src/test` + JUnit 5 + JaCoCo | 可运行空测试 |
| 0.2 | 冻结 Golden Suite 规格大纲 | `docs/spec/golden_suite.md` |
| 0.3 | EnergyNet IC/GT 纯逻辑可测切入点 | 测试可调用 Calculator 关键路径 |
| 0.4 | 命名审计脚本/报告 | `docs/spec/naming_audit.md` |
| 0.5 | 模块 Origin 标注初版 | 残留 / 已改 / 原创 |
| 0.6 | PR 模板 + 贡献约定 | 版权 checklist |
| 0.7 | 已知缺陷与 release.md TODO 挂钩 | 重构不吞 bug |

### 5.2 完成标准

- CI 可跑测试（本地 `gradlew test`）  
- 至少 **EnergyNet + 标准机耗电公式** 有首批测试  
- 命名审计清单存在且可排序修复  

### 5.3 预估节奏

以人力为 1 全职开发计：**1–2 周**。多人或并行可压缩，但不可跳过。

---

## 6 阶段 1 — 现代 Java 与基础设施现代化

**目标**：去掉反编译时代最危险的模式；不强制一次对齐全部 Forge 生态。

### 6.1 范围清单

#### 6.1.1 网络同步（P0）

| 现状 | 目标 |
|:---|:---|
| `List<String> getNetworkedFields()` + 反射读写 | 显式 `SyncedData` / codec 字段表 |
| 字段名字符串耦合 Java 成员 | `snake_case` 逻辑键 + 类型安全编解码 |
| `GrowingBuffer` 全能序列化 | 收窄为内部实现，对外 Packet payload 清晰 |

**迁移策略**：

1. 在 `BlockEntity` 基类引入 `SyncKey` 注册表  
2. 标准机先迁移（进度、active、energy 显示）  
3. 旧反射路径标 `@Deprecated`，双写一版  
4. Golden 测试验证往返  

#### 6.1.2 Tick 订阅（P0）

| 现状 | 目标 |
|:---|:---|
| `getDeclaredMethod("updateEntityServer")` | 显式 `ServerTicker` / `ClientTicker` 接口或 `BlockEntityTicker` 工厂 |

禁止反射探测方法是否存在。

#### 6.1.3 现代 Java 编码规范（P1）

强制（新代码 / 触碰即改）：

- `final` 能加则加；优先不可变 DTO（`record`）  
- 空安全：边界用 `Optional` 或明确 `@Nullable` 约定（二选一，项目统一）  
- 替换遗留集合：`Vector` → `ArrayList`/`List.of`；无同步需求不用同步容器  
- 资源与 IO：try-with-resources  
- 日志：统一 `IC2R.log` / slf4j，禁止 `printStackTrace` 作为正式路径  
- 随机：禁止 tick 内 `RandomSource.create()`；用 `level.random` 或注入字段  
- 枚举与 sealed 类型表达闭环状态机（机器状态、电网模式）  
- switch 表达式、模式匹配在 Java 版本允许范围内使用  

风格（建议写入 `.editorconfig` / checkstyle 或 spotless）：

- 缩进与大括号：与现仓库一致或统一一次后锁定  
- import：禁止 `*`（允许过渡期，阶段 1 末清零）  
- 魔法数：进入 `ModConstants` / 规格常量  

#### 6.1.4 注册与 Holder（P1）

| 现状 | 目标 |
|:---|:---|
| `public static final Item X = register(...)` + pending 列表 | `DeferredRegister` + `RegistryObject`/`Holder`（或加载器等价物） |
| 巨型 `Ic2rItems` / `Ic2rBlocks` | 按域拆分：`ModItems.Resources` / `ModBlocks.Machines`… |

#### 6.1.5 死代码与 API 收敛（P1）

- 删除确认无用的静态 `recipes = new Vector<>()` 等残留  
- `@Deprecated` 的 recipe 旧 API：给出替代并设删除里程碑  
- `UnsupportedOperationException` 桩：实现或删除调用方  

#### 6.1.6 可测试性改造（P0）

- 电网、配方匹配、升级计算与 `Level` 解耦（端口 + 适配器）  
- 时间、随机、世界查询可注入  

### 6.2 阶段 1 明确不做

- 完整拆除 `InvSlot`（留到阶段 2）  
- 完整拆除 XML GUI（阶段 2 起对新 UI 冻结 XML）  
- 多加载器落地（阶段 3）  

### 6.3 完成标准

- [ ] 反射网络同步不再是默认路径  
- [ ] Tick 反射探测移除  
- [ ] 核心命名面字面量 snake_case 化完成（或剩余仅兼容层）  
- [ ] 单元测试覆盖率达 §4.5 阶段 1 门槛  
- [ ] Spotless/Checkstyle（若启用）在 CI 通过  

### 6.4 预估节奏

**4–8 周**（视并行度与是否双写兼容）。

---

## 7 阶段 2 — 对齐 Forge 生态（并为平台抽象预留）

**目标**：内部实现与现代模组生态互通；同时把“Forge 类型”隔离，避免阶段 3 撕裂。

### 7.1 库存

| 现状 | 目标 |
|:---|:---|
| `InvSlot` 平行体系 | 语义保留（可升级槽、可处理槽），底层 `IItemHandler`（NeoForge `IItemHandler` / Fabric Transfer 在 platform） |
| 自动化不透明 | 对外暴露标准 capability/transfer |

步骤：

1. `InvSlot` 委托到内部 `ItemStackHandler`  
2. BE `getCapability(ITEM_HANDLER)` 方向化暴露  
3. 机器逻辑仍可走安全的领域 API（`MachineInventory`）  

### 7.2 流体

| 现状 | 目标 |
|:---|:---|
| 厚 `Ic2rFluidStack` 抽象 | 领域层保留最小值类型；platform 映射 Forge `FluidStack` / Fabric 流体 |
| 双轨容器逻辑 | 统一 fill/empty 服务 + 测试 |

### 7.3 配方

| 现状 | 目标 |
|:---|:---|
| `Recipes` 静态字段 + 自定义 Manager | `RecipeType` + `RecipeSerializer` + 原版 `RecipeManager` 查询 |
| 数据驱动不完整 | 全部机器配方可 JSON；代码只注册类型 |

兼容：

- 运行时动态加配方（若仍需要）走事件/reload 监听，不写死静态全局  

### 7.4 GUI

| 现状 | 目标 |
|:---|:---|
| XML `guidef` + `GuiParser` | **冻结新增 XML**；新机器纯代码 Menu/Screen |
| 旧 XML | 保留兼容层直至迁移完毕 |

可选长期：数据驱动 UI 用 JSON（非 IC2 XML 方言），降低自研解析器版权与维护成本。

### 7.5 能量对外桥

- EU 为内部权威  
- FE/RF（及后续 Fabric 能量）仅 platform 适配  
- 比例与开关配置化；默认不破坏 IC 电网语义  

### 7.6 数据生成 DataGen

- BlockState / Model / Tags / Recipes / Lang（可部分）  
- 减少手写海量 JSON 的漂移  

### 7.7 完成标准

- [ ] 管道模组/AE2/常见自动化可稳定与机器交互（文档化测例）  
- [ ] 配方主路径走 `RecipeManager`  
- [ ] 新 UI 零 XML  
- [ ] 覆盖率达 §4.5 阶段 2 门槛  

### 7.8 预估节奏

**6–12 周**。

---

## 8 阶段 3 — 架构瘦身与多加载器

**目标**：抛弃“Forge 深深耦合的单体”，形成 common + platform；迁移 NeoForge / Fabric；可选 Architectury。

### 8.1 加载器策略

| 阶段 | Loader | 说明 |
|:---|:---|:---|
| 现在 | Forge 1.20.1 | 稳定产品线 |
| 3a | NeoForge（同 MC 版本线或升级版本） | 主替换目标 |
| 3b | Fabric | 第二实现 |
| 3c（可选） | Architectury API | 合并加载器样板；**概率不大则保持手写 thin platform** |

原则：

- **先抽 platform SPI，再引入 Architectury**；避免未抽象先绑框架  
- Architectury 是选项不是前提  

### 8.2 必须抽出的 SPI（示例）

```text
PlatformRegistry
PlatformEnergyBridge
PlatformFluidBridge
PlatformItemTransfer
PlatformNetwork
PlatformPlayerUi
PlatformConfig
PlatformLifecycle
```

实现：

- `ServiceLoader` 或 Architectury `@ExpectPlatform`  
- common 只依赖接口  

### 8.3 架构瘦身清单

| 项 | 动作 |
|:---|:---|
| `EnvProxy` / `SideProxy` | 拆为窄 SPI；删除上帝代理 |
| 巨型 BE | 作物/核电/采矿机按领域服务拆分 |
| `api` 面 | 仅保留外部模组真正需要的接口；其余 internal |
| 组件系统 | 保留思想，重命名并文档化为现代组件（lifecycle 明确） |
| Mixin | 维持最小集合；能事件则事件 |

### 8.4 包与 Maven 坐标

- 保持 `me.halfcooler.ic2r` 或评估更中性坐标（若未来品牌调整）  
- 多 loader artifact：`ic2r-common` / `ic2r-neoforge` / `ic2r-fabric`  

### 8.5 完成标准

- [ ] common 源码无 `net.minecraftforge.*` / `net.neoforged.*` / `net.fabricmc.*` 实现型 import  
- [ ] 至少一条非 Forge 加载器可运行最小集（物品注册 + 一台机器 + 电网）  
- [ ] 覆盖率达 §4.5 阶段 3 门槛  
- [ ] Origin 文档中“移植残留”核心模块清零或仅剩标注的兼容层  

### 8.6 预估节奏

**一到多个大版本周期**；与 MC 版本升级可合并进行。

---

## 9 跨阶段执行纪律

> Agent 执行粒度与停等规则以 **§A** 为准；本节约束人类 PR 与合并纪律。

### 9.1 PR / Commit 粒度

- 与 Work Unit 对齐：**一个 Work Unit ≈ 用户一次手动 commit**（或同 Unit 的一小簇相关 commit，由用户决定）  
- 单 commit 单一层级：测试 / 命名 / 网络 / 库存 …  
- 禁止“重命名全仓库 + 改电网算法”捆绑  
- 行为变更必须挂规格 ID  
- **Agent 禁止 `git commit` / `git push`**  

### 9.2 双轨与删除

```
旧实现 ──双写/适配──► 新实现
              │
              ▼
        测试全绿后切主路径
              │
              ▼
           删除旧实现
```

删除旧实现前：搜索残留 import 与反射字符串。

### 9.3 分支策略建议

| 分支 | 用途 |
|:---|:---|
| `forge/1.20.1` | 稳定发布 |
| `modern/stage-0` … | 阶段集成分支 |
| 功能分支 | 短生命周期 |

定期从稳定分支快进；避免现代化分支永久分叉。

### 9.4 风险登记（摘要）

| 风险 | 缓解 |
|:---|:---|
| 重写导致玩法漂移 | Golden Suite + 对照存档 |
| 版权质疑 | 规格/Origin/测试三联证据 |
| 大爆炸冲突 | 分阶段、双写、小 PR |
| 多加载器延迟 | SPI 先于 Architectury |
| 资源仍敏感 | 代码与资源许可与替换路线分离 |

---

## 10 里程碑总表

| 里程碑 | 关键结果 | 依赖 |
|:---|:---|:---|
| **M0** 护栏 | 测试基建 + 审计 + Golden 大纲 | 无 |
| **M1** 字面量/命名 | snake_case 字面量与迁移表 | M0 |
| **M2** 网络与 Tick | 无反射默认同步/tick | M0 |
| **M3** 注册现代化 | Deferred/Holder + 拆分注册表 | M1 |
| **M4** 配方与数据 | RecipeManager 主路径 + DataGen 起步 | M2–M3 |
| **M5** 库存/流体生态 | Handler/Transfer 对齐 | M4 |
| **M6** GUI 现代化 | 新 UI 无 XML | M5 |
| **M7** Platform 抽取 | common 无 loader 实现依赖 | M5–M6 |
| **M8** NeoForge 产品线 | 主加载器切换就绪 | M7 |
| **M9** Fabric 最小集 | 第二加载器验证 | M7–M8 |
| **M10** 残留清零 | 核心反编译结构退役 | 贯穿 |

GTEU 双模电网工作与 **M0–M2** 并行，且必须纳入同一 Golden Suite。

---

## 11 编码宪法（现代化后的“思想”）

所有新代码默认遵守：

1. **规格与测试优先于实现**  
2. **领域逻辑与加载器隔离**  
3. **显式优于反射**  
4. **组合优于深继承**（继承深度有上限；标准机模板可保留一层）  
5. **数据驱动优于硬编码表**（数值/配方/生成）  
6. **字面量跨边界一律 snake_case**  
7. **公共 API 极小且稳定；内部可大胆重构**  
8. **可观测**：日志分类、debug 开关、电网 dump 保留  
9. **失败可诊断**：禁止空洞 `IllegalStateException()` 无消息  
10. **版权敏感改动可审计**  

---

## 12 与现有文档/工作的关系

| 已有工作 | 纳入方式 |
|:---|:---|
| GTEU 双模电网 | 现代化样板；能量域优先完成规格+测试+去机器分支 |
| `ic2` → `ic2r` 命名空间 | 已完成的版权/身份隔离；继续在 ID 层保持独立 |
| 玩法差异列表（README） | 行为规格的一部分，测试需覆盖“有意差异” |
| release.md 已知问题 | 重构时不得默默永久化；进 issue 或规格 |

---

## 13 立即下一步（可执行）

对人：新开会话，只发：

```text
按 docs/Modernization_Project.md 推进现代化。
```

对 Agent：严格执行 **§A Agent Session Protocol**，从 `Modernization_Progress.md` 中第一个 `pending` 的 Work Unit 开始（通常为 **W0.1**）。

逻辑顺序（已拆成 Work Unit，勿一次做完）：

1. W0.1 测试基建 → 用户 commit  
2. W0.2–W0.3 规格目录与 Golden 大纲 → 各 commit  
3. W0.4 EnergyNet 首批测试 → commit  
4. W0.5–W0.6 命名审计与 Origin → commit  
5. 进入 W1.\*（网络 / Tick / 字面量 / 注册…）  

---

## 14 文档维护

| 项 | 约定 |
|:---|:---|
| 本文档路径 | `docs/Modernization_Project.md` |
| 进度路径 | `docs/Modernization_Progress.md`（会话恢复；每 Unit 更新） |
| 状态更新 | 每完成 Work Unit 更新 Progress；每完成大阶段复核 §10 |
| 重大策略变更 | 修改本文并在**用户** commit message 中说明 *why* |
| 子规格 | 放在 `docs/spec/`，本文只保留索引级链接 |
| Agent commit | **禁止**；仅用户手动 commit |

---

## 附录 A — 字面量重命名示例

| 种类 | 旧 | 新 |
|:---|:---|:---|
| 网络字段 | `rotationSpeed` | `rotation_speed` |
| NBT | `energy`（若冲突/含糊） | `energy_buffer` |
| 内部同步 | `"active"` 反射字段 | SyncKey `active` → wire name `active`（已是 snake 可保留） |
| 翻译 | `ic2.dir.north` 风格残留 | `direction.ic2r.north` |
| 配置 | 混用 | `energy_net_mode = "GT"` |

## 附录 B — 类型重命名示例（分批，勿一次爆改）

| 旧 | 新 |
|:---|:---|
| `TileEntityMacerator` | `MaceratorBlockEntity` |
| `TileEntityElectricMachine` | `ElectricMachineBlockEntity` |
| `Ic2rTileEntityBlock` | `EntityBlockIc2r` / `MachineBlock` |
| `BasicMachineRecipeManager` | `BasicMachineRecipeType` + `BasicMachineRecipe` |

每次重命名必须伴随 IDE 重构 + 全量编译 + 相关测试。

## 附录 C — 阶段与版权控制对照

| 阶段 | 版权收益 |
|:---|:---|
| 0 测试与规格 | 建立“行为来自规格”的证据链 |
| 1 去反射/现代 Java/命名 | 消除最易对照的结构指纹 |
| 2 生态对齐 | 实现形态靠近社区惯例，远离 IC2 内部模型 |
| 3 平台化重写 | 包结构与加载器层全新，同源性主要只剩玩法 |

---

**文档版本**：1.1  
**状态**：可执行（Executable Draft）— 含 §A 会话自治协议  
**适用范围**：代码与工程；不含资源授权替换方案  
**会话入口**：§A Agent Session Protocol + `docs/Modernization_Progress.md`  
