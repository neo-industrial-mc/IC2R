# 2.10.32-ex120

欢迎来到制作和反馈清单。这里记录了下版本的计划和已知问题。

We'd apologize for our documentations are in Chinese. Please use translation.

## 已知问题

### 紧急

- 创造模式物品栏乱序（目前是按照注册顺序的，后续会调整为分类顺序）
- 末影珍珠粉材质更改
- 拟态板物品模型不显示
- 电表什么都不显示
- 高级采矿机挖掘深度验证
- 声音系统隐藏式字幕
- 高炉等等接电网
- 热交换器配方
- 防爆石配方添加
- 建筑泡沫导线
- 单元不正确识别为桶
- 待添加：太阳能头盔（3/2 韧性）、静电靴（3/2 韧性）
- 护甲值（数字表示：盔甲值/盔甲韧性）
  - 电池背包、高级电池背包、能量水晶储电背包、兰波顿储电背包 8/2
  - 喷气背包、电力喷气背包 8/2
  - 复合胸甲 9/2
  - 青铜套装没有盔甲韧性：头盔 2、胸甲 6、护腿 5、靴子 2
  - 防化套装：头盔 3/2、胸甲 8/2、护腿 6/2、橡胶靴 3/2
  - 夜视镜 3/2
  - 纳米和量子套装：头盔 3/2、胸甲 8/2、护腿 6/2、靴子 3/2

### 不急

- 自动弹出升级、自动抽入升级、撬棍：原版还没有配方
- 尚未添加：管道系统

### 要做

- 燃料棒（锂）和燃料棒（氚）：原版无功能
- （可能）改写电网。你能想象吗？MFSU 接 64 个打粉机正常工作而不爆炸。

### 准备迁移的联动 Mod

- [Iridium Source](https://www.mcmod.cn/class/2588.html)
- [METS](https://www.mcmod.cn/class/2217.html)
- [Gravitation Suite](https://www.mcmod.cn/class/255.html)
- [ASP](https://www.mcmod.cn/class/23.html)

## 与原版的区别

### 小巧思

- 充电座：现在的无线充电机制不受电压等级限制，但充电速率仍然受限于充电座的规格；充电的顺序为：主手、副手、头盔、胸甲、护腿、靴子、快捷栏0~8、物品栏从左上到右下。
- 删除：旧版粉色粘球形态的 UU 物质、精炼铁锭
- 翻译：以中文为基准, 重做翻译。全部扁平化
- 矿脉生成，请使用 [JustEnoughResources](https://github.com/way2muchnoise/JustEnoughResources) 查看。

### 大特性

- IC2 电网目前支持 [Applied Energetics 2](https://modrinth.com/mod/ae2) 的能源接收器！
- 您可以安装 [Configured](https://modrinth.com/mod/configured) 来使用 GUI 调整 IC2 的配置文件！
- 高级采矿机现在支持采矿过滤卡来自定义更多的矿石名单！
