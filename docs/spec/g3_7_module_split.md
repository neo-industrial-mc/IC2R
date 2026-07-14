# G3.7 — 物理多模块（文档 + 安全骨架）

> **Work Unit**: G3.7  
> **日期**: 2026-07-14  
> **状态**: **partial / skeleton** — 目录级骨架 + 映射文档；**运行时仍为单模块** Forge 1.20.1  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §2.2 / §2.3 / §8.4；[neoforge_migration_plan.md](neoforge_migration_plan.md)；[g3_2_neoforge_min_set.md](g3_2_neoforge_min_set.md)；[phase3_closeout.md](phase3_closeout.md) G3.7；[platform_spi.md](platform_spi.md)  
> **性质**: **不**把 `src/main` 整棵搬走；**不**切换主依赖到 NeoForge；**不**把 `include` 多项目设为默认构建  

---

## 1. 本 Unit 明确边界

| 做 | 不做 |
|:---|:---|
| 写清目标模块、源码映射、依赖方向、为何不强制 Gradle 多项目 | 整棵迁移 `src/main` → 子项目 |
| 仓库 `modules/` **目录级**骨架 + README 说明落点 | 启用 `include ':ic2r-common'` 等导致主构建断裂 |
| `settings.gradle` **注释掉**的 `include` 示例 | 把主线切到 NeoForge / NeoGradle |
| phase3_closeout G3.7 → **partial/skeleton** | 声称多 loader 已可运行 / §8.5 #1/#2 done |
| `.\gradlew.bat compileJava test` 绿 | git commit/push |

**主线仍为单模块**（根 `build.gradle` + ForgeGradle；`archivesName=…-forge`）。本 Unit 仅铺路。

---

## 2. 目标模块（Maven / Gradle 坐标形态）

对齐主文档 §8.4 与 [neoforge_migration_plan.md](neoforge_migration_plan.md) §3：

| 模块 artifact | 角色 | 本 Unit 状态 |
|:---|:---|:---|
| **`ic2r-common`** | 加载器无关领域逻辑 + `platform.services` SPI **接口** | 骨架 README；源码仍在根 `src/main` |
| **`ic2r-forge`** | Forge 入口 + SPI 实现 + 现网适配（今日主产品线收拢目标） | 骨架 README；源码仍在 `me…forge` + 根 FG |
| **`ic2r-neoforge`** | NeoForge 入口 + SPI 实现（实验 / 最小集 G3.2 M1–M5） | 骨架 README；**无**源码 / **无**依赖 |
| **`ic2r-fabric`**（可选） | Fabric 入口 + Transfer/API 桥（M9 / 主文档 3b） | 骨架 README 可选说明；**后置** |

物理目录约定（**规划落点**，非当前 Gradle 工程）：

```text
modules/
├── README.md                 # 本骨架总览 + 与根单模块关系
├── common/                   # → 未来 :ic2r-common
│   └── README.md
├── forge/                    # → 未来 :ic2r-forge
│   └── README.md
├── neoforge/                 # → 未来 :ic2r-neoforge
│   └── README.md
└── fabric/                   # → 未来 :ic2r-fabric（可选，后置）
    └── README.md
```

包名短期保持 `me.halfcooler.ic2r.*`（§8.4）；Maven `group` **不**要求本 Unit 变更。

---

## 3. 源码迁移映射（现状根源集 → 目标模块）

现状：全部在根 `src/main/java/me/halfcooler/ic2r/**`（+ resources）。  
下表为**目标落点**；**G3.7 不执行搬迁**。

### 3.1 → `ic2r-common`（目标）

| 现状路径（包） | 说明 | 迁入前置 |
|:---|:---|:---|
| `platform/services/**` | SPI 接口 + `PlatformServices` | 已洁净（0 loader 实现 import） |
| `api/**` | 对外 API 面 | 尽量薄；去掉 loader 类型泄漏后迁 |
| `core/energy/**` | EU 电网与 profile 逻辑 | 已含 `*Math` 纯逻辑；宿主 residual 可后置 |
| `core/recipe/**`、`core/block/invslot/**`、`core/fluid/**`（domain） | 配方 / 库存 / 流体业务 | 去掉 Forge `IItemHandler`/`FluidStack` 实现型 import |
| `core/network/sync/**` | 现代 Sync codec | 已 rewritten 切片 |
| `core/block/machine/**`、`core/block/comp/**` 等 | 机器 / 组件逻辑 | **大量 residual + Forge import**（见 §5） |
| `core/init/**`（除 ForgeConfigSpec 字面） | 配置语义 / 常量 | Config 类型下沉 SPI 后 |
| 纯逻辑 `*Math`、部分 `util` | 可测 domain | 低风险先迁候选 |

**common 硬门槛（终态 §8.5 #1）**：无 `net.minecraftforge.*` / `net.neoforged.*` / `net.fabricmc.*` **实现型** import。

### 3.2 → `ic2r-forge`（目标；今日主线收拢）

| 现状路径 | 说明 |
|:---|:---|
| `forge/**` | `FmlMod`、`ForgePlatformServices`、全部 `Platform*Forge`、Env*Forge、cap、model |
| `core/proxy/**` | EnvProxy / SideProxy 上帝代理（迁移期；E3–E6 退役后只留兼容薄层） |
| `datagen/**` | Forge DataGen 入口 |
| `mixin/**` + `ic2r.mixins.json` | 加载器侧 Mixin 配置与实现 |
| `integration/**` | JEI / Jade / AE2 等（可再拆 integration 子模块，**非本 Unit**） |
| 仍含 Forge import 的 `core/**` 切片 | 在下沉 SPI 前可**暂时**留在 forge 适配或 dual-source；**禁止**假装已进 common |

### 3.3 → `ic2r-neoforge`（目标；实验）

| 内容 | 说明 |
|:---|:---|
| NeoForge Mod 入口 | 对标 `FmlMod` |
| `NeoForgePlatformServices.install()` | 实现 [platform_spi.md](platform_spi.md) 8 facet |
| 最小集注册 + 1 机 + 电网 tick | 见 [g3_2_neoforge_min_set.md](g3_2_neoforge_min_set.md) M1–M5 |
| **不**复制整棵 core | 依赖 `ic2r-common` 编译产物 |

### 3.4 → `ic2r-fabric`（可选 / 后置）

| 内容 | 说明 |
|:---|:---|
| Fabric 入口 + SPI 实现 | M9；Transfer API 等桥 |
| 依赖 `ic2r-common` | 同 neoforge |

### 3.5 资源（`src/main/resources`）

| 资源 | 目标倾向 |
|:---|:---|
| `assets/ic2r/**`、`data/ic2r/**` | common 共享，或 common + loader overlay |
| `META-INF/mods.toml` | **forge**（neoforge 用对应 meta） |
| `data/forge/**` tags | forge（neoforge 映射另议） |
| `ic2r.mixins.json` | 各 loader 模块各自持有或共享 refmap 策略 |

---

## 4. 依赖方向与 SPI

与主文档 §2.3、[platform_spi.md](platform_spi.md) §1、[neoforge_migration_plan.md](neoforge_migration_plan.md) §3 一致：

```text
ic2r-neoforge  ──implements──►  platform.services（位于 common）
ic2r-forge     ──implements──►  platform.services
ic2r-fabric    ──implements──►  platform.services   （可选）
ic2r-common    ──uses────────►  platform.services
integration    ──►  common / api（无 loader 实现类型泄漏进 common）
client 资源/屏 ──►  common（逻辑）+ loader client hooks
```

| 边 | 允许 | 禁止 |
|:---|:---|:---|
| common → SPI 接口 | JDK、MC 官方 API、本模组 domain 类型 | `net.minecraftforge` / `neoforged` / `fabricmc` 实现类型 |
| forge/neoforge → SPI | 实现 8 facet；可依赖 loader API | common 反向 import forge 实现类 |
| forge → common | 使用 domain 类型 | common 依赖 `FmlMod` / `EnvProxyForge` |
| SPI 签名 | MC + domain | 暴露 loader 类型作公共返回值 |

**现状 SPI（G3.6）**：8 facet 均有 Forge 薄委托真实现；注册主路径等仍双轨 EnvProxy（E3）。  
**物理拆模块不替代** SPI 去 residual：common 仍有 ~31 文件 / ~54 行 core Forge import（G3.1 后），**不能**在启用 common 独立编译前假装洁净。

---

## 5. 为何本 Unit **不**强制切 Gradle 多项目为默认构建

| 原因 | 说明 |
|:---|:---|
| **core residual Forge import** | G3.1 后 `core/**` 仍约 **31** 文件含 `net.minecraftforge.*`（`ForgeRegistries`、`IItemHandler*`/`LazyOptional`、`ForgeConfigSpec`、fluid cap、event 等）。独立 `ic2r-common` 编译会**立即失败**，除非先完成 G3.1 续 / E3–E6 / 类型下沉 |
| **单测与 FG 运行配置成本** | 根工程绑定 ForgeGradle：`minecraft {}`、`runs {}`、mixin、`runClient`/`runData`、JaCoCo 与 `src/test` 路径。拆多项目需重新接线 sourceSet、test classpath、IDE run config；**一次切默认 include 易打断** `.\gradlew.bat test` |
| **SPI 调用点未全迁** | EnvProxy 双轨仍在；注册族主路径未切 SPI（E3）。模块边界若按包硬拆，会在错误层留下循环依赖 |
| **主产品线稳定** | 发布与玩法回归仍在 Forge 1.20.1 单 artifact（`…-forge`）。G3.2 明确 **M6：主构建隔离**；多项目默认化属于后续骨架启用 Unit |
| **无 NeoForge 依赖线** | neoforge 模块尚无可 compile 的 loader 依赖与入口；空 include 无收益、有配置噪声 |

**结论（诚实）**：G3.7 = **文档 + 目录骨架 + 注释 include 示例**。运行时 / CI 默认路径 **仍是根单模块**。

---

## 6. 下一 Unit 启用 `include` 的前置条件

在将下列条件**基本满足**前，**禁止**把多模块设为默认 `settings.gradle` include：

| # | 前置条件 | 关联 |
|:---|:---|:---|
| P1 | **common 候选包**无 loader 实现型 import（或仅允许已文档化的过渡适配包且不进 common jar） | G3.1 续、§8.5 #1 |
| P2 | **EnvProxy 注册族 / 关键工厂**主路径经 SPI（E3–E4 至少完成最小集路径） | platform_spi / G3.5 |
| P3 | 根工程可 `include` common 为 **library**（`java-library`），forge 模块 `implementation project(':ic2r-common')`，且 `compileJava test` 绿 | 本骨架下一 Unit |
| P4 | FG `runs` / mixin / resources 仍可从 **默认** forge 聚合任务启动 | 勿打断开发者日常 |
| P5 | NeoForge 线：选项 A 依赖与入口可单独任务构建（**非**默认），且不污染 forge test | G3.2 / neoforge plan |
| P6 | 文档与 Progress 标明「默认仍 assemble forge」；CI 矩阵可选 `ic2r-neoforge` | G3.2 M6 |

**建议启用策略**（后续 Unit，非本 Unit）：

1. 先 `include` **仅** `ic2r-common` 为可选 composite（或 `-PenableMultiModule`），默认 off。  
2. 从**已洁净**包（`platform.services`、部分 `*Math`、sync）试迁，验证依赖方向。  
3. 再迁 forge 薄实现到 `modules/forge`，根工程变为聚合或 thin wrapper。  
4. 最后加 neoforge 实验模块（选项 A），默认不参与 `test`。

---

## 7. 本 Unit 交付物清单

| 路径 | 内容 |
|:---|:---|
| **本文件** | 目标模块、映射、SPI、不强制多项目原因、启用前置 |
| `modules/README.md` | 骨架总览 |
| `modules/common/README.md` | common 落点 |
| `modules/forge/README.md` | forge 落点 |
| `modules/neoforge/README.md` | neoforge 落点 |
| `modules/fabric/README.md` | 可选 fabric 落点（后置） |
| `settings.gradle` | **注释掉**的未来 `include` 示例（不激活） |
| [phase3_closeout.md](phase3_closeout.md) §4 G3.7 + §15 | **partial/skeleton** |
| [README.md](README.md)（spec 索引） | 登记本文件 |

**验证**：`.\gradlew.bat compileJava test` → BUILD SUCCESSFUL（行为与 G3.6 后一致；无生产代码搬迁）。

---

## 8. 诚实边界（勿过度宣称）

- **无**物理多模块 Gradle 工程参与默认构建  
- **无**可运行 NeoForge / Fabric artifact  
- **无** `src/main` 包搬迁  
- 逻辑边界（SPI）已存在；**物理边界**仅骨架  
- §8.5 #1 / #2 **仍** gap / deferred  

---

## 9. 变更范围（G3.7）

- `docs/spec/g3_7_module_split.md`（本文件）  
- `modules/**/README.md`（骨架）  
- `settings.gradle`（注释 include 示例）  
- `docs/spec/phase3_closeout.md`（G3.7 + §15）  
- `docs/spec/README.md`（索引）  
- **无**生产 Java 搬迁；**无**主依赖切换；**无** git commit/push  
