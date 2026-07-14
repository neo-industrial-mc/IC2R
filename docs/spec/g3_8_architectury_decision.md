# G3.8 Architectury 决策

> **Work Unit**: G3.8  
> **日期**: 2026-07-14  
> **状态**: **skipped / deferred by design**（**非**缺陷、**非**未做完）  
> **主文档**: [Modernization_Project.md](../Modernization_Project.md) §8.1  
> **收口索引**: [phase3_closeout.md](phase3_closeout.md) §4 G3.8 / §16

---

## 1. 决策

| 项 | 结论 |
|:---|:---|
| Architectury API / `@ExpectPlatform` | **本阶段不引入** |
| 判定 | **skipped / deferred by design** |
| 性质 | 有意延期，**不是**实现缺口或失败 |

**禁止**（本 Unit 及当前主线）：在 `build.gradle` / 依赖树引入 Architectury；为「勾 G3.8」而做空接入。

---

## 2. 理由

1. **W3.1–G3.6 已用手写 thin platform SPI**（`platform.services` + `ForgePlatformServices` 安装）。主文档明确：**先 SPI，再可选框架**；Architectury **不是**前提。  
2. **G3.2 / G3.7 未切多 loader 产品线**：主构建仍 **Forge 1.20.1** 单模块；无第二加载器 artifact，引入 Architectury 无合并样板收益。  
3. **成本**：增加外部依赖、构建复杂度与迁移锁；在 SPI 与 EnvProxy 双轨未收敛前，框架抽象易泄漏进 core。  
4. **现状已够用**：lifecycle / registry / network / fluid / item / energy / config / playerUi 的 Forge 薄委托路径已落地；后续 NeoForge 可继续手写实现同一 SPI。

---

## 3. 何时再评估

满足**全部**下列前置后再开可选评估（仍可不采用）：

| # | 前置 |
|:---|:---|
| P1 | `ic2r-neoforge`（或等价）**最小可运行集**落地（注册 + 一台机 + 电网） |
| P2 | Platform SPI **面稳定**（facet 语义与调用点不再频繁改） |
| P3 | 评估问题明确：ExpectPlatform / 多 loader 合并是否**实质**减少重复，而非仅换皮 |

评估结论可为：继续手写 thin platform，或仅在 adapter 层试点 ExpectPlatform。**默认倾向**仍是手写 SPI，与 [neoforge_migration_plan.md](neoforge_migration_plan.md) / [g3_2_neoforge_min_set.md](g3_2_neoforge_min_set.md) 一致。

---

## 4. 关联

| 文档 / 代码 | 关系 |
|:---|:---|
| 主文档 §8.1 / 风险「多加载器延迟」 | SPI 先于 Architectury |
| [platform_spi.md](platform_spi.md) | 手写 SPI 现状 |
| [g3_7_module_split.md](g3_7_module_split.md) | 物理多模块骨架；未绑 Architectury |
| `modules/**/README.md` | 骨架说明 Architectury 有意延期 |
