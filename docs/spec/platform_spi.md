# Platform SPI 草案（W3.1）

> **状态**：**G3.2** — `PlatformRegistryForge` 真实现（薄委托 EnvProxy）；`getServer` 调用点迁 `PlatformLifecycle`（`SideProxyServer` / `ItemDrill`）。W3.3 已删 `isClientEnv`。`isForgeEnv`/`isFabricEnv` 与注册主路径仍双轨 EnvProxy。  
> **W3.4 / G3.2**：NeoForge 计划 [neoforge_migration_plan.md](neoforge_migration_plan.md)；最小集 kickoff [g3_2_neoforge_min_set.md](g3_2_neoforge_min_set.md)（**未**切主构建 / **无**可运行 NeoForge artifact）。  
> **下一**：E2 续（环境位）/ E3 注册调用点 / SPI 非 stub（Network、Item…）。  
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
**G3.2 实现**：`ForgePlatformServices.install()` 安装 `PlatformLifecycleForge` + **`PlatformRegistryForge`** + `PlatformEnergyBridgeForge`；其余 facet stub；**不**强制 `META-INF/services`。

---

## 3. 接口列表与职责

| 接口 | 职责（窄面） | 现状来源 |
|:---|:---|:---|
| **PlatformRegistry** | 方块/物品/BE/菜单/实体/配方/世界生成注册；创意页签；registry 后回调 | `EnvProxy` 注册族 + `createItemGroup` + `runAfterRegistryInit`；`EnvProxyForge` DeferredRegister |
| **PlatformEnergyBridge** | 外部能量（FE/RF 等）插入/抽出探测；**不**含 EU 电网逻辑 | 目前几乎只在 integration（如 AE2 FE）；EU 在 `core.energy` |
| **PlatformFluidBridge** | 流体 handler 工厂 + 世界/容器 mB 读写桥 | `EnvProxy#createFluidStackHandler`、`EnvFluidHandler` / `EnvFluidHandlerForge` |
| **PlatformItemTransfer** | 物品 handler 工厂 + 邻接库存 insert/extract | `EnvProxy#createItemHandler`、`EnvItemHandler` / `EnvItemHandlerForge` |
| **PlatformNetwork** | 自定义通道注册与发包 | `FmlMod` `NetworkRegistry` + `ForgeNetworkHandler`；业务包体多在 `NetworkManager` |
| **PlatformPlayerUi** | 打开菜单、玩家消息、错误展示 | `EnvProxy#openHandledScreen`、`SideProxy#messagePlayer` / `displayError` |
| **PlatformConfig** | 配置目录与 common/client 配置注册 | `FmlMod#registerConfig`、`IC2RConfig`（`ForgeConfigSpec` 仅实现侧） |
| **PlatformLifecycle** | 环境检测、server 可用、tick 调度、bootstrap 钩子 | `EnvProxy` 环境位 + `SideProxy` 生命周期 |

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
| `isForgeEnv` / `isFabricEnv` / `getServer` | **PlatformLifecycle**（`getLoaderKind` / `getServer` 已有；**G3.2** 已迁 `getServer` 调用点；`isForgeEnv`/`isFabricEnv` 待切） |
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
| `EnvFluidHandlerForge` / `Ic2rFluidStackImpl` / `*FluidCapImpl` | PlatformFluidBridge |
| `EnvItemHandlerForge` | PlatformItemTransfer |
| `ForgeNetworkHandler` + `FmlMod` 通道注册 | PlatformNetwork |
| `FmlMod` 配置注册 | PlatformConfig |
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

platform.services.*  ◄──  （W3.2）EventHandler.onInitLate 等少量 core 调用点
        ▲
        │ implements
forge.PlatformLifecycleForge + PlatformRegistryForge + ForgePlatformServices（其余 facet stub）
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

### W3.3 切片记录（一组方法）

| 动作 | 方法 | 说明 |
|:---|:---|:---|
| **删除** | `EnvProxy#isClientEnv` / `EnvProxyForge#isClientEnv` | 表面从上帝代理移除 |
| **迁移** | 调用点 → `PlatformLifecycle#isClient` | `IC2R.createSideProxy`、`SideGateway`、`WorldData`、`ItemDebug`（`EventHandler.onInitLate` 已在 W3.2） |
| **内部** | `EnvProxyForge#createFluidStackHandler` | 改用本类静态 `isClient`（Forge dist），不再经接口 |
| **安装** | `IC2R` static + `FmlMod` | 双入口 `ForgePlatformServices.install()`（幂等），保证 class-init 可用 SPI |

**非目标（禁止扩 scope）**：

- W3.3 只瘦 **一组**（client 环境判定）；不全库拆 EnvProxy  
- 不拆多模块 / Architectury  
- 不做 W3.4 NeoForge 骨架  
- 不把全部 SPI facet 做成完整委托实现（lifecycle 以外可 stub）

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
