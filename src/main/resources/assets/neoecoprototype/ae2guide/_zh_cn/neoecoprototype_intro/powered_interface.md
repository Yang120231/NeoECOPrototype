---
navigation:
  title: L1 供能接口
  icon: neoecoprototype:simplify_powered_me_interface
  position: 80
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_powered_me_interface
  - neoecoprototype:cable_powered_me_interface
---

# L1 供能接口

<Row>
  <BlockImage id="neoecoprototype:simplify_powered_me_interface" scale="3"></BlockImage>
</Row>

<ItemGrid>
  <ItemIcon id="neoecoprototype:cable_powered_me_interface" />
</ItemGrid>

L1 供能接口是一台布局翻倍、还会给 ME 网络供电的 ME接口。除下面这几项之外，行为与原版接口完全一致，凡是接受 ME接口的地方都能用它。

| 项目 | 原版 ME接口 | L1 供能接口 |
|------|-------------|-------------|
| 标记槽 | 9 | **18** |
| 库存槽 | 9 | **18** |
| 单个标记可囤（物品） | 64 | 64 |
| 被动发电 | 无 | **200 AE/t** |
| 在哪里做 | 合成台 | [L1 处理器装配室](processor_assembler.md) |

## 被动发电

| 项目 | 规则 |
|------|------|
| 生效条件 | 通电且有频道。 |
| 只留最强的一台 | AE2 每个网格同时只保留输出功率最高的被动发电源，出现更强的源时本机停止贡献，直到重新被选中。 |
| 用途 | 子网供电。比水晶谐振发电机划算——后者 20 AE/t，还要单独接主网络。 |

## 获取

方块版在 [L1 处理器装配室](processor_assembler.md) 里组装：**2 台水晶谐振发电机 + 1 个 ME接口 + 1 枚 ECO - SA 超导处理器**。装配室的配方图这一页渲染不出来，原料写在文字里，格子去 JEI 看。

| 形态 | 转换 |
|------|------|
| 方块 → 线缆部件 | <RecipeFor id="neoecoprototype:cable_powered_me_interface" /> |
| 线缆部件 → 方块 | 同样一条无损转换，反向即可 |

一台[盈能超导接口](superconductive_interface.md)需要四台它。
