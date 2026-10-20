---
navigation:
  title: 盈能能源元件
  icon: neoecoprototype:energized_energy_cell_16g
  position: 23
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:energized_energy_cell_16g
  - neoecoprototype:energized_energy_cell_512g
---

# 盈能能源元件

<Row>
  <BlockImage id="neoecoprototype:energized_energy_cell_16g" scale="3"></BlockImage>
  <BlockImage id="neoecoprototype:energized_energy_cell_512g" scale="3"></BlockImage>
</Row>

**盈能能源元件**是两块独立电池：放下去自己就是一个 ME 网格节点，不需要任何多方块定义认识它，AE2 把它的缓冲直接计进网格总电量。空转 0，网络既能往里灌也能从里取。

| 元件 | 容量（AE） | 一次合成 |
|------|------------|----------|
| <ItemLink id="neoecoprototype:energized_energy_cell_16g" /> | 16,000,000,000 | 8 只 ECO - LT9 高密度能量元件 + 32 个盈能超导块 + 64 个绿晶晶格 |
| <ItemLink id="neoecoprototype:energized_energy_cell_512g" /> | 256,000,000,000 | 8 只 16G + 1 个盈能超导块 |

两台都在 eco 的 <ItemLink id="neoecoae:integrated_working_station" /> 里做，各耗能 100,000 FE；工作站的配方图这一页渲染不出来，所以原料写在表里。

拆下来会把当时的电量带走，tooltip 上"已存储能源"那一行就是它，重新放下接着用。

第二只的物品 id 仍写作 `energized_energy_cell_512g`，而它的容量是 256G——按 id 抓东西的脚本要知道这一条。
