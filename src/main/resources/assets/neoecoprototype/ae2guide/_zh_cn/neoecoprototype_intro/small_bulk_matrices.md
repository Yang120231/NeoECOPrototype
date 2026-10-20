---
navigation:
  title: 小宗存储矩阵
  icon: neoecoprototype:simplify_small_bulk_storage_cell
  position: 20
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_small_bulk_storage_cell
  - neoecoprototype:simplify_small_bulk_storage_cell_expanded
  - neoecoprototype:simplify_small_bulk_fluid_storage_cell
  - neoecoprototype:simplify_small_bulk_fluid_storage_cell_expanded
  - neoecoprototype:simplify_small_bulk_chemical_storage_cell
  - neoecoprototype:simplify_small_bulk_chemical_storage_cell_expanded
  - neoecoprototype:simplify_small_bulk_expansion_card
  - neoecoprototype:simplify_small_bulk_storage_matrix_housing
  - neoecoprototype:simplify_small_bulk_fluid_storage_matrix_housing
  - neoecoprototype:simplify_small_bulk_chemical_storage_matrix_housing
---

# 小宗存储矩阵

**小宗存储矩阵**是<ItemLink id="ae2:drive" />那一族里的极端成员：普通矩阵是"很多种、每种有限"，它是"很少几种、每种近乎无限"。默认 **3 种**，用扩展卡升到 **10 种**。

| 项目 | 数值 |
|------|------|
| 类型上限 | 3，装上扩展卡 10 |
| 单类型容量 | 报告的字节数是 long 上限 **9,223,372,036,854,775,807**（终端里显示成 9.2E），实际到不了顶 |
| 需要的前置 | MEGA Cells（4.11.0 是量过的那一版）；化学品变体另需 Mekanism 与 Applied Mekanistics |

## 分区

分区就是给盘设一份白名单：被允许的资源才入盘，其余照常路过。在 AE2 的元件工作台里编辑；流体盘与化学品盘各自按自己的键类型分区。

## 压缩链

只有物品变体有压缩链：在元件工作台给盘装上 MEGA Cells 的**压缩升级卡**后，链上的任意形态（粒、锭、块……）都会折算进盘里分区的那一种，终端显示的也是那一种。

## 升到 10 种

在元件工作台里把盘与**扩展卡**一起放进去，出来的就是扩展版，同时退你一颗 MEGA Cells 的 1M 元件。扩展卡本身在合成台做：4 绿晶晶格 + 4 颗 MEGA Cells 1M 元件 + 1 盈能超导锭。

## 拆回

流体与化学品这两只基础盘可以拆回，退回外壳与一颗 1M 元件。
> **注意**：**物品那只基础盘与三只扩展版都没有拆解配方**。扩展版一旦做出来就拆不回外壳与元件，装卡前先想清楚。


<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_small_bulk_storage_cell" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_storage_cell_expanded" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_fluid_storage_cell" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_fluid_storage_cell_expanded" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_chemical_storage_cell" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_chemical_storage_cell_expanded" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_expansion_card" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_storage_matrix_housing" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_fluid_storage_matrix_housing" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_chemical_storage_matrix_housing" />
</ItemGrid>

<RecipeFor id="neoecoprototype:simplify_small_bulk_expansion_card" />
