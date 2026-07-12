# 2.10.37-ex120

欢迎来到制作和反馈清单。这里记录了下版本的计划和已知问题。

## 已知问题和 TODO

- 注册键 `ingot_*` 和 `*_ingot` 到底哪个才是正确的？这导致 **暂时** 和机械动力不兼容。
- 创造模式物品栏目前是按照注册键A-Z排序，后续会调整为分类顺序
- 拟态板物品模型不显示。功能似乎正常。
- 磁化机耗电但是不正常升空
- 模式扫描机 UU 物品和对应消耗重写
- 和 Jade 的联动
- 自动弹出升级、自动抽入升级、撬棍：原版还没有配方
- 迁移至 GTEU 后，导线必须重写一套
- 尚未添加：管道系统
- 超频过的泵稳定性极差，极差极差特别差很差非常差
- 燃料棒（锂）和燃料棒（氚）：原版无功能
- 建筑泡沫和硬化墙、导线防爆
- ITNT 和核弹在激活后，实体具有碰撞箱
- 批量工作台默认需要手动分拣
- JEI 热区显示配方

### 准备迁移的联动 Mod

- [Iridium Source](https://www.mcmod.cn/class/2588.html)
- [METS](https://www.mcmod.cn/class/2217.html)
- [Gravitation Suite](https://www.mcmod.cn/class/255.html)
- [ASP](https://www.mcmod.cn/class/23.html)
- [Advanced Machines](https://www.mcmod.cn/class/22.html)
- [Compact Solar Arrays](https://www.mcmod.cn/class/112.html)
- [AFSU](https://www.mcmod.cn/class/852.html)

### 联动 Mod

- Applied Energetics 2
- JEI (WIP: REI, EMI, etc.)
- Jade (WIP)

## 与原版的区别

### 小巧思

- 充电座：现在的无线充电机制不受电压等级限制，但充电速率仍然受限于充电座的规格；充电的顺序为：主手、副手、头盔、胸甲、护腿、靴子、快捷栏0~8、物品栏从左上到右下。
- 删除：旧版粉色粘球形态的 UU 物质、精炼铁锭、脚手架
- 翻译：以中文为基准, 重做翻译。全部扁平化
- 矿脉生成，请使用 [JustEnoughResources](https://github.com/way2muchnoise/JustEnoughResources) 查看。

### 大特性

- IC2R 电网目前支持 [Applied Energetics 2](https://modrinth.com/mod/ae2) 的能源接收器。
- 您可以安装 [Configured](https://modrinth.com/mod/configured) 来使用 GUI 调整 IC2 的配置文件。
- 高级采矿机现在支持采矿过滤卡来自定义更多的矿石名单。
- IC2R 现在已采用 GTEU 的电力显示方式！您可以在 `config/ic2-common.toml` 中设置字段 `misc.energyNet.mode` 为 `IC2`（默认，经典 IC 电网）或 `GT`（GT 电网）来切换电力计算方式。
