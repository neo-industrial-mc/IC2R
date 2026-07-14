# `ic2r-fabric`（可选 / 后置）

> G3.7 可选骨架。主文档阶段 3b / M9；**非**当前优先级。

## 最终源码落点（目标）

| 内容 | 说明 |
|:---|:---|
| Fabric 入口 | `ModInitializer` 等 |
| Fabric SPI 实现 | 同 8 facet；Transfer API 等桥 |
| 依赖 | `ic2r-common` only for domain |

## 本目录现状

- **无**实现计划强制时间表；Architectury **有意延期**（G3.8）  
- 启用前同 [g3_7_module_split.md](../../docs/spec/g3_7_module_split.md) §6，且 common 洁净度须达标  
