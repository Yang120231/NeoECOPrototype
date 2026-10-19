---
navigation:
  title: FE存储矩阵
  icon: neoecoprototype:simplify_fe_storage_cell_1m
  position: 21
  parent: neoecoprototype_intro/storage_system.md
item_ids:
  - neoecoprototype:simplify_fe_cell_housing
  - neoecoprototype:simplify_fe_storage_cell_1m
  - neoecoprototype:simplify_fe_storage_cell_4m
---

# FE存储矩阵

FE 存储矩阵需要 **AppliedFlux（应用通量，modid `appflux`）**，用于在 ME 网络里存储 Forge Energy（FE）。

## 容量

容量按**字节**算，一个字节值多少 FE 由 AppliedFlux 的 `flux_cell.amount` 决定，出厂是每字节 1,048,576 FE。

| 矩阵 | 字节 | 默认换算 |
| --- | --- | --- |
| <ItemLink id="neoecoprototype:simplify_fe_storage_cell_1m" /> | 1,048,576 | 约 1.1 万亿 FE |
| <ItemLink id="neoecoprototype:simplify_fe_storage_cell_4m" /> | 4,194,304 | 约 4.4 万亿 FE |

调大 `flux_cell.amount`，游戏里每一只 FE 仓（包括这两只）一起变宽松，材料不变。

## 配方

<RecipeFor id="neoecoprototype:simplify_fe_cell_housing" />

<RecipeFor id="neoecoprototype:simplify_fe_storage_cell_1m" />

<RecipeFor id="neoecoprototype:simplify_fe_storage_cell_4m" />

装回原样也有一条配方，把外壳与成分还给你。

## 插在哪

报 L1 档位，所以 [L1 存储系统](storage_system.md) 直接收，eco 更高档的主机也收得下。在 L1 存储主机面板里它们单独占一行，那一行的名字来自 eco 注册的 FE 单元类型（"能源"），插在 eco 自己的主机里也是这个名。
