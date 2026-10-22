---
navigation:
  title: FE存储矩阵
  icon: neoecoprototype:simplify_fe_storage_cell_1m
  position: 21
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_fe_cell_housing
  - neoecoprototype:simplify_fe_storage_cell_1k
  - neoecoprototype:simplify_fe_storage_cell_1m
  - neoecoprototype:simplify_fe_storage_cell_4m
---

# FE存储矩阵

FE 存储矩阵是把 **Forge Energy（FE）当成一种资源存进 ME 网络**的存储矩阵，行为与物品、流体那几族一样，只是内容物换成电。它需要 **AppliedFlux（应用通量，modid `appflux`）**。

## 容量

容量按**字节**算——这里的"字节"是 AE2 给每只仓记的存储额度，不是计算机里那个 8 bit。一个字节值多少 FE 由 AppliedFlux 的 `flux_cell.amount` 决定，出厂是每字节 1,048,576 FE。

| 矩阵 | 字节 | 默认换算 |
| --- | --- | --- |
| ECO - L1 1K FE存储矩阵 | 1,024 | 约 10.7 亿 FE |
| ECO - L1 1M FE存储矩阵 | 1,048,576 | 约 1.1 万亿 FE |
| ECO - L1 4M FE存储矩阵 | 4,194,304 | 约 4.4 万亿 FE |

调大 `flux_cell.amount`，游戏里每一只 FE 仓（包括这三只）的容量按比例变大，材料不变。

## 配方

> 这一族**只有装了 AppliedFlux 才会注册**。所以这里不放图标、也不放配方框：标签解析不到物品时不会留白，而是往页面上写一串红字。装了 AppliedFlux 时，下面四条都能在 JEI 里查到。

- **ECO - L1 FE存储矩阵外壳**：合成台 `ABA / B B / CCC` —— 2 个绿晶晶格 + 3 个红石 + 3 个 `appflux:harden_insulating_resin`。
- **ECO - L1 1K FE存储矩阵**：外壳 + AE2 的 1K 元件，无序。
- **ECO - L1 1M FE存储矩阵**：外壳 + ECO - L1 存储组件，无序。
- **ECO - L1 4M FE存储矩阵**：外壳 + ECO - L4 存储组件，无序。

装回原样也有一条配方，把外壳与成分还给你。

## 插在哪

它们属于 L1 这一级，所以 [L1 存储系统](storage_system.md) 直接收，eco 更高档的主机也收得下——等级决定哪台主机肯挂它。在 L1 存储主机面板里它们单独占一行，那一行的名字来自 eco 注册的 FE 单元类型（"能源"），插在 eco 自己的主机里也是这个名。
