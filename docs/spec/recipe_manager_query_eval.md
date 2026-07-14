# RecipeManager：tick 直查 vs 缓存 materialize

> **Work Unit**: G2.2  
> **日期**: 2026-07-14  
> **依据**: [Modernization_Project.md](../Modernization_Project.md) §7.3；[phase2_closeout.md](phase2_closeout.md) G2.2；RC-\*  
> **范围**: 评估 basic 机主路径；**不**在本 Unit 切换运行时实现。

---

## 1. 当前链路（已落地）

```text
data/ic2r/recipes/<type>/*.json   type: "ic2r:<id>"
        ↓ Serializer (Ic2rRecipeSerializers)
vanilla RecipeManager
        ↓ RecipeManagerMachineBridge.loadBasic
BasicMachineRecipeManager          ← 按 RecipeManager 实例 WeakHashMap 缓存 (RecipeManagerGetter)
        ↓ Recipes.<type>.get(level)
机器 tick：manager.getOutputFor / isApplicable …
```

**证据（≥2 basic type）**：

| Type | JSON 目录 | RecipeType | Serializer | Rezepte 接线 |
|:---|:---|:---|:---|:---|
| macerator | `recipes/macerator/**` | `Ic2rRecipeTypes.MACERATOR` | `WeightedMachineRecipeSerializer` | `Recipes.macerator = basicRecipe(…)` |
| extractor | `recipes/extractor/**` | `Ic2rRecipeTypes.EXTRACTOR` | `BasicMachineRecipeSerializer` | `Recipes.extractor = basicRecipe(…)` |
| compressor | `recipes/compressor/**` | `Ic2rRecipeTypes.COMPRESSOR` | `BasicMachineRecipeSerializer` | `Recipes.compressor = basicRecipe(…)` |

共用入口：`Rezepte#basicRecipe` → `RecipeManagerMachineBridge.loadBasic` → `RecipeManagerGetter`。  
直查 API 已存在但**未**挂到 TE tick：`RecipeManagerMachineBridge.findMatching`（amount 门闩与 `MachineRecipeMatchMath.acceptsMatchedInput` 一致）。

---

## 2. 方案对比

| 维度 | A. 缓存 materialize（现状） | B. tick 直查 `RecipeManager` |
|:---|:---|:---|
| **语义** | 首次 `get(level)` 把该 type 全部 recipe 拷进 `BasicMachineRecipeManager`；之后扫描本地 list | 每 tick / 每次匹配调用 `getAllRecipesFor(type)` + `findMatching` |
| **reload** | 服务端按 `RecipeManager` 实例缓存；reload 换新 RM 则自动新 materialize。客户端每次 `factory.apply` 不缓存 | 天然跟 vanilla reload；无需二次拷贝 |
| **动态 `addRecipe`** | 写在 materialized manager 上，**不回写** datapack / RM（兼容旧 API / 运行时注入） | 仅 RM 内配方可见；动态加需事件写回 RM 或并查两源 |
| **匹配实现** | `BasicMachineRecipeManager` 历史路径 + 部分 Math 对齐 | `findMatching` 已对齐 first-wins + amount/remainder |
| **性能** | 热路径不反复从 RM 取 list；list 可能略冗余拷贝 | 每查一次 RM list（通常已是 type 桶）；无二次 list 存储 |
| **测试** | manager 行为可单测；bridge 0% 需 Level/RM | pure `findMatchingIndex` / Math 可无 bootstrap；真 RM 仍需 boot |
| **与 §7.3 目标** | 主数据已在 RM；运行时仍经自定义 Manager | 更接近「查询即 `RecipeManager`」 |

---

## 3. 推荐路径

| 阶段 | 推荐 | 理由 |
|:---|:---|:---|
| **现在（G2.2 / 近期）** | **保持 materialize 缓存** | 动态 `addRecipe`、现有 TE 调用面、JEI/UU 解析均假设 `IBasicMachineRecipeManager`；改 tick 直查收益小、回归面大 |
| **中期** | 新代码优先经 bridge/`findMatching` 语义；materialize 视为 RM 的 **投影缓存** | 匹配规则单源（`MachineRecipeMatchMath`）；避免 TE 再长自定义 scan |
| **长期（可选）** | 在确认无运行时 `addRecipe` 硬依赖后，TE 改为直查或薄包装 `IGetter` 直返 match | 满足 §7.3「主路径走 RecipeManager」字面；需清 API + 动态配方事件化 |

**明确不推荐**：在未迁动态配方前，仅把 tick 换成直查却仍保留双写 list（两套真相源）。

---

## 4. 风险与 residual

| 风险 | 说明 |
|:---|:---|
| 动态配方不进 JSON | materialize 后 `addRecipe` 只活在内存 manager；reload 丢失；直查也看不到除非写 RM |
| 顺序契约 | first-wins 依赖 `getAllRecipesFor` / materialize 插入序；改源需 SM-010 回归 |
| 非 basic | canner / scrapbox / electrolyzer 等异构；本评估不覆盖 |
| 客户端 | `RecipeManagerGetter` 客户端不缓存；直查同样每帧可接受若仅 UI |

---

## 5. 验收对照（G2.2）

- [x] macerator 之外至少 1 个 basic type 全链路文档化（extractor + compressor）  
- [x] 本文件记录直查 vs materialize 利弊与推荐  
- [x] 加深测例：第二 type JSON 烟测 + `findMatching`/`acceptsMatchedInput` 组合（见 `MachineRecipeMatchMathTest`）  
- [x] 不要求改完非 basic 机型  
- [x] `gradlew test` 绿（G2.2 本地：BUILD SUCCESSFUL）
