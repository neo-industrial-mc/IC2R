# NeoForge 迁移计划（W3.4）

> **状态**：W3.4 — **文档级迁移计划**（本 Unit **不**新建多模块 Gradle / 不切换主构建）。  
> **决策依据**：仓库当前为 **Forge 1.20.1 单线**（`minecraft_version=1.20.1`，`forge_version=47.x`，`archivesName=…-forge`）；在 SPI 尚未补全、EnvProxy 仍双轨时，强上 NeoForge 多模块风险高、收益低。  
> **主文档**：[Modernization_Project.md](../Modernization_Project.md) §8.1（加载器策略）、§8.4（多 loader artifact）、§A W3.4 DoD。  
> **SPI 规格**：[platform_spi.md](platform_spi.md)（W3.1–W3.3 已落地接口与首批迁移）。

---

## 1. 当前产品线（稳定线）

| 项 | 值 |
|:---|:---|
| 稳定分支约定 | `forge/1.20.1`（主文档 §9.3） |
| MC | **1.20.1** |
| 加载器 | **Forge**（`net.minecraftforge.gradle`，`forge_version` 47.x） |
| Java | 17 |
| 产物坐标形态 | 单模块；`archivesName = ${mod_id}-forge` |
| 源码形态 | 单体源集；包 `me.halfcooler.ic2r.*`；Forge 实现混在 `forge` / `core.proxy` |

**本 Unit 硬约束**：

- **不**把主构建切到 NeoForge  
- **不**强制引入 Architectury  
- 现有 `.\gradlew.bat test` / 产品线行为保持可用  

稳定线继续服务发布与玩法回归；多加载器工作在 SPI 与 common 边界成熟后再开独立构建线。

---

## 2. 目标 NeoForge 版本线选项

并列评估，**选定前不落代码骨架到主线**。

| 选项 | MC / Loader | 优点 | 代价 | 倾向 |
|:---|:---|:---|:---|:---|
| **A. 同版线 NeoForge 1.20.1** | MC 1.20.1 + NeoForge 1.20.1 线 | 与现网 MC 一致；玩法/资源/映射差最小；可对照 Forge 线双发 | NeoForge 1.20.1 为历史分支，生态与文档弱于新版；部分 API 已与「现代 NeoForge」分叉 | 适合 **SPI 验证 + 最小可运行集** 试点 |
| **B. 升级 MC 再上 NeoForge**（如 1.21.x） | 更高 MC + 对应 NeoForge | 对齐当前 NeoForge 主生态、Java 21、长期维护 | 需同步：映射、注册、网络、流体/能力、DataGen、Mixin、依赖模组；Golden 全回归 | 适合 **M8 主产品线切换** |
| **C. 双线并行（Forge 1.20.1 稳定 + NeoForge 实验）** | 稳定线不变 + 实验 artifact | 不打断稳定发布 | 构建/CI 复杂；行为漂移风险 | 阶段 3 中后期可选 |

**建议路径（文档决策，非实现）**：

1. **短期（W3.5 前后）**：继续 Forge 1.20.1；补全 `PlatformServices` 与 EnvProxy 退役，不锁死 A/B。  
2. **中期（M7 达标后）**：用 **选项 A** 做 `ic2r-neoforge` **最小集**（注册 + 1 台机器 + 电网），验证 SPI 单向依赖。  
3. **长期（M8）**：再评估是否以 **选项 B** 作为主 NeoForge 产品线；若 B 成本可控，A 可降为过渡验证 artifact 或废弃。

**否决项（本阶段）**：在未完成 SPI 补全前「主线直接 FG→NeoGradle 换依赖」。

---

## 3. 模块目标与 SPI 关系

目标多 loader artifact（主文档 §8.4）：

```text
ic2r-common      →  领域逻辑 + 仅依赖 platform.services（SPI）
ic2r-neoforge    →  NeoForge 入口 + SPI 实现（registry/lifecycle/…）
ic2r-forge       →  （现状主线）Forge 入口 + 现有/收拢中的 SPI 实现
ic2r-fabric      →  （可选，3b）第二实现；不在 W3.4 范围
```

依赖方向（与 [platform_spi.md](platform_spi.md) §5 / 主文档 §2.3 一致）：

```text
ic2r-neoforge  ──implements──►  platform.services
ic2r-forge     ──implements──►  platform.services
ic2r-common    ──uses────────►  platform.services
api / integration              ──►  common 边界（无 loader 实现类型）
```

| 模块 | 职责 | 与 SPI |
|:---|:---|:---|
| **ic2r-common** | 机器、电网、配方逻辑、NBT/Sync 等 | 只调 `PlatformServices.*`；**禁止** `net.minecraftforge.*` / `net.neoforged.*` / `net.fabricmc.*` 实现型 import（§8.5 终态） |
| **ic2r-neoforge** | Mod 入口、`NeoForgePlatformServices.install()`、DeferredRegister/网络/能力桥 | 实现 8 个 SPI facet；可依赖 NeoForge API |
| **ic2r-forge**（现状收拢） | 今日 `FmlMod` + `ForgePlatformServices` + `EnvProxyForge` | 迁移期双轨；最终 thin 实现层 |
| **ic2r-fabric**（可选） | Fabric 入口与 Transfer/API 桥 | 同 SPI；后置 M9 |

**包名**：短期保持 `me.halfcooler.ic2r`；Maven 坐标可按 artifact 拆分（`…:ic2r-common` 等），**不**要求本 Unit 改 `group`。

**现状差距（单模块）**：

- SPI 接口已在 `me.halfcooler.ic2r.platform.services`（W3.1）  
- `ForgePlatformServices.install()` + `PlatformLifecycleForge` 已接（W3.2）；**G3.2** 增 `PlatformRegistryForge`；EnergyBridge 真实现；其余 Network/UI/Config/Item/Fluid 仍 stub  
- 业务仍大量经 `IC2R.envProxy` / `EnvProxyForge`（W3.3 退役 `isClientEnv`；G3.2 迁 `getServer` 调用点）  
- **尚无** Gradle 子项目 `ic2r-common` / `ic2r-neoforge`（G3.2 仅 kickoff 文档，主构建未切）

---

## 4. `PlatformServices` 补全与 EnvProxy 退役顺序

前置原则（主文档 §8.1）：**先抽/补全 platform SPI，再开 NeoForge 模块**；Architectury 非前提。

### 4.1 SPI facet 补全优先级

| 序 | Facet | 说明 | 阻塞 NeoForge 最小集？ |
|:---|:---|:---|:---|
| P0 | **PlatformLifecycle** | 已有实现路径；继续迁 `isForgeEnv`/`isFabricEnv`/`getServer`、tick/bootstrap | 部分已满足 |
| P0 | **PlatformRegistry** | 方块/物品/BE/菜单/配方注册；替代 `EnvProxy` 注册族 | **是** |
| P1 | **PlatformNetwork** | 通道与发包；对齐现 `ForgeNetworkHandler` / `NetworkManager` | **是**（有同步机器时） |
| P1 | **PlatformPlayerUi** | 开菜单、消息 | 最小 GUI 需要 |
| P1 | **PlatformConfig** | 配置目录/注册；去 `ForgeConfigSpec` 泄漏 | 可先 stub 默认 |
| P2 | **PlatformItemTransfer** | `IItemHandler` 工厂与邻接传输 | 机器 IO 需要 |
| P2 | **PlatformFluidBridge** | 流体 handler / 世界 mB | 有流体机时需要 |
| P3 | **PlatformEnergyBridge** | 外部 FE 桥；EU 留 common | 可后置 integration |
| 后置 | Client SPI | Screen/BER 等；现 `ClientEnvProxy*` | 客户端最小集再开 |

### 4.2 EnvProxy / SideProxy 退役顺序

与 [platform_spi.md](platform_spi.md) §4 映射一致，按**可测切片**推进（延续 W3.3 模式：一组方法/调用族一次 Unit）：

| 阶段 | 动作 | 验收线索 |
|:---|:---|:---|
| **E1**（已完成 W3.3） | 删除 `isClientEnv`；全库 `PlatformServices.lifecycle().isClient()` | 已交付 |
| **E2** | 迁 `isForgeEnv` / `isFabricEnv` / `getServer` → `PlatformLifecycle`（`getLoaderKind` / `getServer`） | 无上述 EnvProxy 调用点 |
| **E3** | 注册族 → `PlatformRegistry`；`IC2R.createEnvProxy()` 不再 `new EnvProxyForge()` 作为唯一路径 | 注册经 SPI；install 幂等 |
| **E4** | fluid/item handler 工厂 → Fluid/Item SPI；网络/配置/UI 跟进 | facet 非 stub 或有委托实现 |
| **E5** | `SideProxy` 生命周期/消息已映射部分迁完；side-only 可暂留 | common 不依赖 Forge 生命周期 |
| **E6** | 删除上帝代理表面 API；残留仅兼容层并记 Origin | §8.3 / §8.5 相关勾选 |

**安装约定**（保持现状扩展）：

- 各 loader 入口显式 `*PlatformServices.install(...)`（幂等）  
- `ServiceLoader` / `META-INF/services` 可选回退，**不**作为 W3.x 硬门槛  
- 禁止 common 在 class-init 假设未 install 的 facet（stub 调 UOE）

### 4.3 与多模块拆分的衔接

1. **逻辑边界先于物理模块**：在单模块内把 `forge` 实现收到实现包、common 只依赖 SPI。  
2. **再**拆 Gradle：`ic2r-common` 源集搬迁 + `ic2r-forge` 实现保留主线。  
3. **最后**加 `ic2r-neoforge` 空/最小实现，主构建默认仍可只 assemble forge 产物。

---

## 5. 分阶段里程碑（相对 W3.5 / 后续）

| 里程碑 | 相对 Unit / 主文档 | 交付 | 非交付 |
|:---|:---|:---|:---|
| **W3.4（本 Unit）** | DoD：模块 **或** 文档计划其一 | **本文档**；索引进 `docs/spec/README.md` | 无 NeoForge 依赖、无多模块工程 |
| **W3.5** | 阶段 3 收口 | §8.5 勾选 **或** 明确延期项（含：NeoForge 模块未建、common 仍有 Forge import 等 gap） | 不要求已可运行 NeoForge |
| **M7 续** | Platform 抽取完成度 | SPI facet 实质实现；EnvProxy 主路径退役过半+；common 直依赖 Forge 趋零 | 完整多 loader CI |
| **NeoForge 骨架 Unit**（Progress 可后续追加，如 W3.6） | 可选代码 Unit | 空 package / 占位 `NeoForgePlatformServices` **或** 独立 `settings.gradle` 子项目且 **默认不启用** | 不切换稳定产品线 |
| **M8** | NeoForge 产品线 | 最小可运行集（物品注册 + 一台机器 + 电网）；版本线在 §2 A/B 中选定 | 全模组 1:1 port |
| **M9** | Fabric | `ic2r-fabric` 最小集 | 全功能对齐 |

**W3.5 建议登记的延期项（预填）**：

- [ ] 物理模块 `ic2r-common` / `ic2r-neoforge` 未建立（W3.4 选文档路径）  
- [ ] common 仍含 `net.minecraftforge.*`（待 EnvProxy/注册迁移）  
- [ ] 非 Forge 加载器无可运行最小集（§8.5）  
- [ ] Architectury 未引入（**有意**：手写 thin platform 优先）

---

## 6. 明确不在本 Unit 做的事

| 禁止 / 不做 | 原因 |
|:---|:---|
| 完整 NeoForge / 全模组 port | 超出 W3.4；依赖 SPI 与版本线决策 |
| 主构建切换 NeoGradle / 改 `minecraft {}` 为 NeoForge | 破坏稳定线与现有 `gradlew test` |
| 强制 Architectury / `@ExpectPlatform` 绑定 | 主文档 §8.1：先 SPI，后可选框架 |
| 新建多模块工程并改 CI 默认 | 非本 Unit 必要；文档路径已满足 DoD |
| 继续大范围 EnvProxy 删除（属后续瘦身 Unit） | 一次一组；避免与「计划落地」捆绑 |
| Fabric 实现、Client SPI 全量设计 | 3b / 后置 |
| 资源包/材质迁移 | 现代化文档范围外 |

---

## 7. 风险与回滚

| 风险 | 影响 | 缓解 | 回滚 |
|:---|:---|:---|:---|
| 过早换主 loader | 稳定线不可发布；测试全红 | W3.4 仅文档；主线锁 Forge 1.20.1 | 不合并任何改 `build.gradle` loader 的 PR |
| SPI 未满就拆模块 | 循环依赖、大量 `UOE` stub | 先 E2–E4 再物理拆分 | 保持单模块；子项目 `include` 可逆 |
| 版本线选错（A vs B） | 重复 port | §2 先 A 验证 SPI，再评估 B 作主线 | 废弃实验 artifact；稳定线不动 |
| EnvProxy / SPI 双轨过久 | 维护成本、调用点分裂 | 映射表 + 按族退役；Progress 可追踪 | 双轨可回退单点到 EnvProxy（至 E6 前） |
| Architectury 过早引入 | 框架锁死、抽象泄漏 | 本计划默认手写 thin platform | 不引入即可；已引入则限 adapter 层 |
| 多 loader 行为漂移 | 玩法不一致 | Golden Suite + 同测例挂 common | 关闭非 Forge 发布通道 |
| 依赖模组仅 Forge | 集成缺口 | integration 按 loader 拆；缺失则禁用 | 稳定线保留 Forge integration |

**回滚总则**：

- 文档 Unit：删除/回退本文即可，**零运行时影响**。  
- 未来代码骨架：仅占位类 + 可选 `include` 时，默认 `settings` 不启用 NeoForge 工程；回滚 = 移除子项目与占位包。  
- **禁止**用「半切换主构建」作为中间态。

---

## 8. W3.4 验收（DoD）

| 条 | 状态 |
|:---|:---|
| 模块 **或** 文档级迁移计划落地其一 | **文档**：本文件 |
| 含稳定线、版本选项、模块/SPI、退役序、里程碑、非目标、风险回滚 | **是**（§1–§7） |
| 主构建仍为 Forge 1.20.1 | **是**（未改 Gradle loader） |
| 验证 | 文件存在性 + 结构检查；**不要求**为本文跑 NeoForge 构建。可选：`.\gradlew.bat test` 确认无误改代码 |

---

## 9. 修订记录

| 日期 | Unit | 说明 |
|:---|:---|:---|
| 2026-07-14 | W3.4 | 初版：文档级计划；不做模块骨架 |
| 2026-07-14 | G3.2 | 最小集 kickoff 见 [g3_2_neoforge_min_set.md](g3_2_neoforge_min_set.md)；Registry 真实现；仍无 NeoForge artifact |
