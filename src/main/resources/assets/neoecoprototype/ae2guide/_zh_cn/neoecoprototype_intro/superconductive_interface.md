---
navigation:
  title: 盈能超导接口
  icon: neoecoprototype:superconductive_interface
  position: 90
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:superconductive_interface
  - neoecoprototype:cable_superconductive_interface
---

# 盈能超导接口

<Row>
  <BlockImage id="neoecoprototype:superconductive_interface" scale="3"></BlockImage>
</Row>

<ItemGrid>
  <ItemIcon id="neoecoprototype:cable_superconductive_interface" />
</ItemGrid>

盈能超导接口是本模组两条接口里的高端那台：布局与 [L1 供能接口](powered_interface.md) 相同，单个标记能囤的量大得多，输出功率是它的二十倍。

| 项目 | 原版 ME接口 | 盈能超导接口 |
|------|-------------|--------------|
| 标记槽 | 9 | **18** |
| 库存槽 | 9 | **18** |
| 单个标记可囤（物品） | 64 | **8,192** |
| 单个标记可囤（流体、化学品） | 4,000 mB | **512,000 mB** |
| 被动发电 | 无 | **4,000 AE/t** |
| 在哪里做 | 合成台 | eco 的集成工作站 |

标记与设置方式和原版接口一样。

## 被动发电

4,000 AE/t 通常是网格里最高的那台，所以一台它就能接管整个网络的被动发电，并把所有 [L1 供能接口](powered_interface.md) 压下去。AE2 每个网格只保留输出功率最高的一个被动发电源。

## 获取

方块版在 eco 的**集成工作站**里组装：**1 个天外寒冰 + 4 台 [L1 供能接口](powered_interface.md) + 9 个盈能超导锭 + 4 个 ECO - SA 超导处理器**，耗能 10,000 AE。工作站的配方图这一页渲染不出来，所以原料写在文字里。

| 形态 | 转换 |
|------|------|
| 方块 → 线缆部件 | <RecipeFor id="neoecoprototype:cable_superconductive_interface" /> |
| 线缆部件 → 方块 | 同样一条无损转换，反向即可 |
