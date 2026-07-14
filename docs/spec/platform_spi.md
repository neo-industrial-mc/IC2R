# Platform SPI 草案（W3.1）

> **状态**：**G3.6 done** — 全部 8 个 SPI facet 已装 **Forge 薄委托真实现**（无 Stub*）；调用点增量迁 SPI（`IHasGui` openMenu、`StackUtil`/`FluidHandler` 工厂、`EventHandler` messagePlayer）。G3.5 / E2 已删环境位表面。注册主路径仍双轨 EnvProxy（E3）。  
> **W3.4 / G3.2**：NeoForge 计划 [neoforge_migration_plan.md](neoforge_migration_plan.md)；最小集 kickoff [g3_2_neoforge_min_set.md](g3_2_neoforge_min_set.md)（**未**切主构建 / **无**可运行 NeoForge artifact）。  
> **下一**：E3 注册调用点继续迁 SPI；可选 Network 发包路径替换。  
> **主文档**：[Modernization_Project.md](../Modernization_Project.md) §2.2–2.3、§8.2。

---

## 1. 目标

将「加载器相关」能力从上帝代理 `EnvProxy` / `SideProxy` 拆为**窄 SPI**，使 common 逻辑只依赖接口，为 NeoForge / Fabric 实现预留单向依赖：

```text
common / core  ──uses──►  platform.services (SPI 接口)
platform-impl (forge/…)  ──implements──►  platform.services
platform-impl  ──may use──►  common / core domain types
```

**禁止（阶段 3 硬门槛，迁移期逐步达成）**：

- common / `core` 业务逻辑 `import net.minecraftforge.*` 实现类型  
- SPI 接口签名暴露 `net.minecraftforge.*` / `net.neoforged.*` / `net.fabricmc.*`  
- SPI 实现包反向依赖 integration 等上层（保持 thin）

**允许**：

- SPI 使用 JDK、Minecraft 官方 API、本模组 domain 类型（如 `Ic2rFluidStack`、`EnvFluidHandler`、`GrowingBuffer`）  
- 迁移期内 `core` 与 `forge` 并存；SPI 旁路新增，不强制一次切完

---

## 2. 包路径

| 角色 | 包 |
|:---|:---|
| SPI 接口 + 访问器 | `me.halfcooler.ic2r.platform.services` |
| Forge 实现（现状，W3.2+ 逐步对齐） | `me.halfcooler.ic2r.forge` |
| 遗留上帝代理（待 W3.3 瘦身） | `me.halfcooler.ic2r.core.proxy` |

访问入口：`PlatformServices`（`install(...)` 显式注入，或 `ServiceLoader` 回退）。  
**G3.6 实现**：`ForgePlatformServices.install()` 安装 **全部 8 facet** 真实现（见下表）；**不**强制 `META-INF/services`；**无** Stub* 内部类。

---

## 3. 接口列表与职责

| 接口 | 职责（窄面） | Forge 实现（G3.6） | 备注 |
|:---|:---|:---|:---|
| **PlatformRegistry** | 方块/物品/BE/菜单/实体/配方/世界生成注册；创意页签；registry 后回调 | `PlatformRegistryForge` | 薄委托 `EnvProxy` 注册族（G3.2） |
| **PlatformEnergyBridge** | 外部能量（FE/RF 等）插入/抽出探测；**不**含 EU 电网逻辑 | `PlatformEnergyBridgeForge` | G2.8；EU 在 `core.energy` |
| **PlatformFluidBridge** | 流体 handler 工厂 + 世界/容器 mB 读写桥 | **`PlatformFluidBridgeForge`** | `createHandler`→`EnvProxy`；drain/fill/getContained→`EnvFluidHandler`；调用点：`FluidHandler.ENV_HANDLER` |
| **PlatformItemTransfer** | 物品 handler 工厂 + 邻接库存 insert/extract | **`PlatformItemTransferForge`** | `createHandler`→`EnvProxy`；`insert`→`deposit`；**`extract` 返回 EMPTY**（`EnvItemHandler` 无公开 BE extract，见实现 javadoc）；调用点：`StackUtil.ENV` |
| **PlatformNetwork** | 自定义通道注册与发包 | **`PlatformNetworkForge`** | `registerChannel` 幂等 no-op（`FmlMod` 已注册 `NetworkManager.channelId`）；send→vanilla custom payload |
| **PlatformPlayerUi** | 打开菜单、玩家消息、错误展示 | **`PlatformPlayerUiForge`** | →`EnvProxy#openHandledScreen` / `SideProxy#messagePlayer`/`displayError`；调用点：`IHasGui`、`EventHandler.onPlayerLogin` |
| **PlatformConfig** | 配置目录与 common/client 配置注册 | **`PlatformConfigForge`** | `FMLPaths.CONFIGDIR`；`register*` no-op（`FmlMod` 已注册）；`isCommonConfigLoaded`→`IC2RConfig.SPEC.isLoaded()` |
| **PlatformLifecycle** | 环境检测、server 可用、tick 调度、bootstrap 钩子 | `PlatformLifecycleForge` | W3.2+；环境位 + `SideProxy` 生命周期 |

可选后续（**不在 W3.1**）：

- Client SPI（`ClientEnvProxy`：Screen/BER/色调/模型谓词）  
- 假玩家 / 生物群系标签 / 爆炸与 retexture 事件公告（仍可暂留 EnvProxy）

---

## 4. 与 EnvProxy / SideProxy 映射

### 4.1 `EnvProxy` → SPI

| EnvProxy 方法簇 | 目标 SPI |
|:---|:---|
| `registerBlock/Item/Entity/…`、`registerRecipe*`、`register*Feature*`、`createItemGroup`、`runAfterRegistryInit` | **PlatformRegistry** |
| `createFluidStackHandler`（及 EnvFluidHandler 实现细节） | **PlatformFluidBridge** |
| `createItemHandler`（及 EnvItemHandler 实现细节） | **PlatformItemTransfer** |
| `openHandledScreen` | **PlatformPlayerUi** |
| ~~`isClientEnv`~~（**W3.3 已删**） | **PlatformLifecycle#isClient**（全库已切） |
| ~~`isForgeEnv` / `isFabricEnv` / `getServer`~~（**G3.5 / E2 已删**） | **PlatformLifecycle**（`getLoaderKind` / `getServer`；调用点：`Ic2r*Tags`、`SideProxyServer`、`ItemDrill`） |
| `announce*` 事件、假玩家、burn time、biome types、blast resistance… | **暂留 EnvProxy**（后续再切或删） |

### 4.2 `SideProxy` → SPI

| SideProxy 方法簇 | 目标 SPI |
|:---|:---|
| `preInit` / `onPostInit` / `onServerAvailable` / `requestTick` / `isSimulating` / `isRendering` | **PlatformLifecycle** |
| `messagePlayer` / `displayError` | **PlatformPlayerUi** |
| `getSoundManager` / `getKeyboard` / `playSound*` / `getPlayerInstance` / world helpers | **暂留 SideProxy**（偏 side 实现，非 loader 边界） |
| `registerRotorProvider` | 可并入 **PlatformRegistry** 或 client SPI（后置） |

### 4.3 Forge 适配层（参考，不在本 Unit 改代码）

| 类 | 对应 SPI 实现角色 |
|:---|:---|
| `EnvProxyForge` | Registry 委托目标 + 部分 Lifecycle + 菜单打开 + fluid/item 工厂 |
| `PlatformRegistryForge` | **PlatformRegistry**（G3.2：薄委托 EnvProxy） |
| `PlatformFluidBridgeForge` | **PlatformFluidBridge**（G3.6：工厂 + drain/fill/getContained） |
| `PlatformItemTransferForge` | **PlatformItemTransfer**（G3.6：工厂 + insert；extract 见缺口） |
| `PlatformNetworkForge` | **PlatformNetwork**（G3.6：幂等 register + vanilla send） |
| `PlatformPlayerUiForge` | **PlatformPlayerUi**（G3.6：openMenu / message / error） |
| `PlatformConfigForge` | **PlatformConfig**（G3.6：CONFIGDIR + loaded 标志） |
| `EnvFluidHandlerForge` / `Ic2rFluidStackImpl` / `*FluidCapImpl` | FluidBridge 底层 |
| `EnvItemHandlerForge` | ItemTransfer 底层 |
| `ForgeNetworkHandler` + `FmlMod` 通道注册 | Network 通道仍在 FmlMod 注册 |
| `FmlMod` 配置注册 | Config 仍在 FmlMod 注册；SPI register* no-op |
| `ClientEnvProxyForge` | 未来 client SPI |
| AE2 / 其它 FE 使用点 | **G2.8**：`PlatformEnergyBridgeForge` + `EnergyBridgeMath`；AE2 FE 路径已共享 Math |

---

## 5. 依赖方向（单向）

终态示意（与主文档 §2.3 一致）：

```text
integration  ──►  common/api
client       ──►  common
forge-impl   ──►  platform.services + common
common       ──►  platform.services + api
api          ──►  JDK + 薄 MC 抽象
```

迁移期（当前仓库仍是单模块）：

```text
core.proxy.EnvProxy  ◄──  core 业务（现状）
        ▲
        │ implements
forge.EnvProxyForge

platform.services.*  ◄──  core 增量调用点（lifecycle / UI / item / fluid 工厂…）
        ▲
        │ implements
forge.Platform*Forge ×8 + ForgePlatformServices（G3.6 全 facet 真实现）
```

规则：

1. **common → spi**：只依赖接口与 domain 类型。  
2. **forge 实现 → spi**：实现接口；可依赖 Forge API。  
3. **禁止 common 直依赖 forge 实现类**（目标；`IC2R.createEnvProxy()` 里 `new EnvProxyForge()` 为已知债，后续改为 `PlatformServices` / ServiceLoader）。  
4. **SPI 不反向依赖** `integration` / GUI XML 运行时等。

---

## 6. 签名原则（草案）

- 精简优先：能覆盖职责切分即可，不追求与 EnvProxy 1:1。  
- 无 Forge 类型：例如 extended menu 用 `ByteBuf` 工厂，而不是 `IForgeMenuType`。  
- 能量桥用 `long` 外部单位；EU 换算留在 common。  
- `PlatformConfig.register*Config(Object spec, …)` 用 `Object` 暂避 `ForgeConfigSpec` 泄漏；后续可引入本模组 `ConfigSpecHandle`。  
- `PlatformEnergyBridge`：**G2.8** 已由 `PlatformEnergyBridgeForge`（`IEnergyStorage`）实现；EU↔FE 纯转换见 `EnergyBridgeMath`（默认 2.0 FE/EU）；契约 [energy_bridge_contract.md](energy_bridge_contract.md)。

---

## 7. 验证与非目标

**W3.1 验证**：`.\gradlew.bat compileJava` 通过；既有 test 不破。  
**W3.2 验证**：`.\gradlew.bat compileJava test`；首迁调用点不直调 `envProxy.isClientEnv()`。  
**W3.3 验证**：`.\gradlew.bat compileJava test`；`EnvProxy`/`EnvProxyForge` 无 `isClientEnv`；common 调用点均用 `PlatformServices.lifecycle().isClient()`。  
**G3.5 / E2 验证**：`.\gradlew.bat compileJava test`；`EnvProxy`/`EnvProxyForge` 无 `isForgeEnv`/`isFabricEnv`/`getServer`；loader 标签用 `getLoaderKind()`。  
**G3.6 验证**：`.\gradlew.bat compileJava test`；`ForgePlatformServices` 无 Stub*；5 个新 `Platform*Forge` 可委托；≥2 common 调用点已切 SPI。

### W3.3 切片记录（一组方法）

| 动作 | 方法 | 说明 |
|:---|:---|:---|
| **删除** | `EnvProxy#isClientEnv` / `EnvProxyForge#isClientEnv` | 表面从上帝代理移除 |
| **迁移** | 调用点 → `PlatformLifecycle#isClient` | `IC2R.createSideProxy`、`SideGateway`、`WorldData`、`ItemDebug`（`EventHandler.onInitLate` 已在 W3.2） |
| **内部** | `EnvProxyForge#createFluidStackHandler` | 改用本类静态 `isClient`（Forge dist），不再经接口 |
| **安装** | `IC2R` static + `FmlMod` | 双入口 `ForgePlatformServices.install()`（幂等），保证 class-init 可用 SPI |

### G3.5 / E2 切片记录（环境位 + getServer 表面）

| 动作 | 方法 | 说明 |
|:---|:---|:---|
| **删除** | `EnvProxy#isForgeEnv` / `#isFabricEnv` / `#getServer` 及 `EnvProxyForge` 实现 | E2 从上帝代理移除 |
| **迁移** | `isFabricEnv` 调用点 → `getLoaderKind() == FABRIC` | `Ic2rBlockTags`、`Ic2rItemTags`、`Ic2rFluidTags` |
| **已有** | `getServer` 调用点（G3.2） | `SideProxyServer`、`ItemDrill` → `lifecycle().getServer()`；本 Unit 删表面方法 |
| **未做** | 注册族 / 流体物品工厂主路径（E3+） | 仍 EnvProxy 双轨（工厂已可走 SPI） |

### G3.6 切片记录（SPI facet 去 stub）

| 动作 | 说明 |
|:---|:---|
| **实现** | `PlatformNetworkForge` / `PlatformPlayerUiForge` / `PlatformConfigForge` / `PlatformItemTransferForge` / `PlatformFluidBridgeForge` |
| **安装** | `ForgePlatformServices.install` 全 facet 真实现；删除 Stub* 内部类 |
| **调用点** | `IHasGui` → `playerUi().openMenu`；`StackUtil.ENV` → `itemTransfer().createHandler`；`FluidHandler.ENV_HANDLER` → `fluid().createHandler`；`EventHandler.onPlayerLogin` → `playerUi().messagePlayer` |
| **缺口** | `PlatformItemTransfer#extract` 返回 EMPTY（`EnvItemHandler` 无公开 BE extract API，实现 javadoc 说明）；Network/Config 主注册仍在 `FmlMod`（SPI register* 幂等 no-op） |
| **未做** | 全库 EnvProxy/SideProxy 调用点清零；物理多模块；NeoForge 实现 |

**非目标（禁止扩 scope）**：

- 不全库拆 EnvProxy 调用点（仅试点 ≥2）  
- 不拆多模块 / Architectury  
- 不做 NeoForge 骨架 / 注册族大迁（E3）  

---

## 8. 风险

| 风险 | 说明 | 缓解 |
|:---|:---|:---|
| 双轨并存 | EnvProxy 与 SPI 短期重复 | 文档映射；W3.2 只迁 1 点证明方向 |
| domain 类型耦合 | SPI 引用 `EnvFluidHandler` 等 | 接受；或后续把 Env* 收成 SPI 本身 |
| ServiceLoader 未注册 | 误调用未 install 的 facet 会抛 | `FmlMod` 显式 `install`；stub facet 调用会 UOE |
| 签名不稳 | 草案可能微调 | 标注 Draft；薄适配器 |
| Config `Object` 弱类型 | 实现易错 | 实现类内 cast + 单测（后置） |
| install 时机 | 过早 static 路径仍无 SPI | 首迁选 post-init（`onInitLate`），非 class-init |
