# GUI 现代化约定（W2.4）

> 对齐 [Modernization_Project.md](../Modernization_Project.md) **§7.4 GUI**。  
> 本文件是**新增 UI 的强制约定**；实现新机台 / 新物品界面前必须遵守。

---

## 1. 规则摘要

| 规则 | 说明 |
|:---|:---|
| **禁止新增 guidef XML** | 不得再向 `assets/ic2r/guidef/**` 添加新的 `.xml` 定义 |
| **新 UI 纯代码** | 新 Menu（`ContainerBase` 子类）+ Screen（`Ic2rGui` / `GuiDefaultBackground` 子类）手写布局 |
| **旧 XML 兼容保留** | 现有 `guidef/*.xml` + `GuiParser` + `DynamicContainer` / `DynamicGui` **不得删除**，直至对应界面迁移完毕 |
| **不改本 Unit 玩家可见机台** | 迁移旧 GUI 另开 Work Unit；本约定以样板为示范 |

可选长期方向（**非本 Unit**）：数据驱动 UI 若需要，用 JSON 等现代格式，**不再扩展** IC2 XML 方言解析器。

---

## 2. 现状架构（只读地图）

### 2.1 XML 动态路径（legacy）

```
assets/ic2r/guidef/<block_path>.xml
        │
        ▼
GuiParser.parse(ResourceLocation, Class)   // SAX 解析
        │
        ├─► DynamicContainer  // 服务端/逻辑：按节点加 Slot、读 GuiSynced 字段
        └─► DynamicGui        // 客户端：按节点加 GuiElement（gauge/button/…）
```

- 入口：`DynamicContainer.create(syncId, inv, TileEntityInventory)`  
  → 用方块 `ResourceLocation` 找 `guidef/<path>.xml`。
- 注册：`Ic2rScreenHandlers.DYNAMIC_BE` / `DYNAMIC_ITEM`（`registerManagedBe` / `registerManagedItem`）。
- 打开：TE/物品实现 `IHasGui`，返回 `DynamicContainer`；客户端 `SideProxyClient` 绑定 `DynamicGui::create`。
- JEI 等集成仍可能读 `GuiParser` 节点（如 `DynamicCategory`）；迁移时需同步适配。

### 2.2 代码路径（已有先例 + 新 UI 标准）

项目中**早已存在**大量非 XML GUI，例如：

- Menu：`core/block/machine/container/ContainerItemBuffer` 等  
- Screen：`core/block/machine/gui/GuiItemBuffer` 等  
- 注册：`Ic2rScreenHandlers` 中 `registerManagedBe("…")`  
- 客户端：`SideProxyClient.preInit()` 里 `envProxy.registerScreen(MenuType, Gui::new)`  
- TE：`IHasGui.createServerScreenHandler` / `createClientScreenHandler` 返回具体 `Container*`  

**新机器应走此路径**，不要新建 guidef。

### 2.3 与 DynamicContainer 的关系

| 角色 | 状态 | 新 UI 是否使用 |
|:---|:---|:---|
| `GuiParser` + `guidef` XML | 兼容层 | **否** |
| `DynamicContainer` / `DynamicGui` | 仅服务仍绑定 XML 的 TE/物品 | **否**（新 UI 不写 XML 节点） |
| `ContainerBase` / `ContainerFullInv` | 公共 Menu 基类 | **是** |
| `Ic2rGui` / `GuiDefaultBackground` + `GuiElement` | 公共 Screen / 控件 | **是**（手写 `addElement`） |
| `Ic2rScreenHandlers` + `SideProxyClient` | MenuType / Screen 注册 | **是** |

结论：Dynamic 栈是 **XML 的运行时解释器**，不是新 UI 的推荐 API。新 UI 复用其下方的 `ContainerBase` / `Ic2rGui` / `GuiElement`，**绕过** `GuiParser`。

---

## 3. 新 UI 清单（DoD 检查表）

实现一台新机器 GUI 时：

1. **Menu 类**  
   - 继承 `ContainerBase` 或 `ContainerFullInv`（需要玩家背包槽时）。  
   - 在构造函数中 `addSlot` / 使用 `SlotInvSlot` 等，**硬编码坐标**。  
   - 持有 `MenuType` 引用（见 `Ic2rScreenHandlers`）。

2. **Screen 类**  
   - 继承 `Ic2rGui`（整张背景贴图）或 `GuiDefaultBackground`（通用九宫格背景）。  
   - 在构造函数中 `addElement(...)` 添加 `EnergyGauge`、`TextLabel`、`Button` 等。  
   - 覆盖 `getTextureLocation()`（`GuiDefaultBackground` 可返回 `null`）。

3. **注册 MenuType**  
   - 在 `Ic2rScreenHandlers` 增加常量：  
     - 绑定方块实体：优先 `registerManagedBe("snake_case_id")`（与现有机台一致，走 `IHasGui` 打开协议）。  
     - 无 BE / 仅演示：`register("…", Factory)`（见样板）。  
   - 底层已是 Forge `DeferredRegister<MenuType<?>>`（`EnvProxyForge.screenHandlerRegistry`）。

4. **注册 Screen（客户端）**  
   - 在 `SideProxyClient.preInit()`：`envProxy.registerScreen(TYPE, GuiXxx::new)`。

5. **TE / 物品接线**  
   - 实现 `IHasGui`：`createServerScreenHandler` / `createClientScreenHandler` 返回新 Menu。  
   - **不要**调用 `DynamicContainer.create`，除非该 TE 仍使用 XML。

6. **禁止**  
   - 新增 `assets/**/guidef/*.xml`  
   - 扩展 `GuiParser.NodeType` 仅为新机服务  
   - 删除尚未迁移的旧 XML / Dynamic 路径

---

## 4. 代码样板（W2.4）

| 类 | 路径 | 作用 |
|:---|:---|:---|
| `CodeGuiSampleMenu` | `core/gui/code/CodeGuiSampleMenu.java` | 纯代码 Menu：`SimpleContainer` + 玩家背包；**不绑定真实方块** |
| `CodeGuiSampleScreen` | `core/gui/code/CodeGuiSampleScreen.java` | 纯代码 Screen：`GuiDefaultBackground` + 说明 `TextLabel` |
| MenuType | `Ic2rScreenHandlers.CODE_GUI_SAMPLE` | `register("code_gui_sample", …)` |
| Screen 绑定 | `SideProxyClient` | `registerScreen(CODE_GUI_SAMPLE, CodeGuiSampleScreen::new)` |

- **不提供**创造模式 / 指令打开路径；存在目的是**编译期结构完整、可对照复制**。  
- 真实机台请改为：`registerManagedBe` + TE 的 `IHasGui` + 业务槽位，参考 `ContainerItemBuffer` / `GuiItemBuffer` 或 `ContainerMagnetizer` / `GuiMagnetizer`。

### 4.1 从样板接到真实 TE（示意）

```text
// 1) Ic2rScreenHandlers
public static final MenuType<ContainerMyMachine> MY_MACHINE = registerManagedBe("my_machine");

// 2) SideProxyClient
envProxy.registerScreen(Ic2rScreenHandlers.MY_MACHINE, GuiMyMachine::new);

// 3) TileEntity implements IHasGui
public ContainerBase<?> createServerScreenHandler(int syncId, Player player) {
    return new ContainerMyMachine(syncId, player.getInventory(), this);
}
public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inv, GrowingBuffer buf) {
    return new ContainerMyMachine(syncId, inv, this);
}
// 禁止: return DynamicContainer.create(...);  // 除非仍用 guidef
```

---

## 5. 迁移策略（后续 Unit）

1. **冻结**：自 W2.4 起零新增 XML。  
2. **并存**：旧机继续 XML；新机纯代码。  
3. **逐台迁移**（另开 Unit）：为某 TE 写 Menu/Screen → 改 `IHasGui` → 删对应 guidef → 回归 JEI/槽位。  
4. **收口**：当 guidef 为空且无引用时，再评估移除 `GuiParser` / `Dynamic*`（**当前禁止**）。

---

## 6. 验证

- `.\gradlew.bat compileJava` 必须通过。  
- 本约定不要求运行时打开样板 GUI。  
- 行为回归：不改动现有 guidef 绑定机台的打开路径。

---

## 7. 变更记录

| 日期 | Unit | 说明 |
|:---|:---|:---|
| 2026-07-14 | W2.4 | 初版：冻结 XML + 代码样板 + 与 Dynamic 关系 |
| 2026-07-14 | **G2.3** | **首台生产机迁移**：`storage_box` 全档（wood/iron/bronze/steel/iridium）→ `ContainerStorageBox` + `GuiStorageBox`；TE 不再 `DynamicContainer.create`；删 6 份 `guidef/*storage_box*.xml`；`Ic2rScreenHandlers.STORAGE_BOX` + `SideProxyClient` 绑定 |
