---
navigation:
  title: L1 奇点元件
  icon: neoecoprototype:simplify_singularity_cell
  position: 22
  parent: neoecoprototype_intro/storage_system.md
item_ids:
  - neoecoprototype:simplify_singularity_cell
---

# L1 奇点元件

L1 奇点元件是一只**不消耗任何东西**的存储单元：插进驱动器之后，库存按世界时间往上走，取用与普通单元一样。

## 多快

| 键 | 默认 | 边界 |
|----|------|------|
| `ticks_per_batch` | 600 tick（30 秒） | 最短 60 tick |
| `amount_per_batch` | 120 | 最多 262,144 |

出厂速率约每秒 4 个奇点。库存没有上限，按 long 计数。两个键都在服务端配置的 `singularity_cell` 段里。

| 用途 | 需要 | 默认速率下 |
|------|------|------------|
| 本模组三台 L1 接口 | 6 | 第一批就到 |
| eco 一只 MEGA 4G 矩阵，或一个无限存储组件 | 128 | 32 秒 |
| eco 的无限存储解锁（64 个组件） | 8,192 | 约 34 分钟 |

## 库存是算出来的

| 项目 | 规则 |
|------|------|
| 怎么算 | 元件只在栈上记两个数：起始世界 tick 与已取走多少。每次有人问库存，都按当前 tick 现场算，后台没有任何累加器。 |
| 只与三件事有关 | 起始 tick、当前世界 tick、已取走的数量。 |
| 改配置会重读历史 | `amount_per_batch` 调大，库存立刻变大；调小可能把已存的清掉。 |
| 不 tick 就不产 | 暂停与关服期间时间不走，库存也不涨。 |
| 插进驱动器才开始 | 起始 tick 在第一次被问到时才记下，而只有驱动器与主机会问。手上、箱子里、地上的还没起算；已经起算过的拿出来照旧涨。 |
| 联机读到的数 | 世界 tick 每秒同步一次，客户端 hover 最多晚一秒；终端读的一直是服务端的值。 |

## 限制

只有一种内容物，因此不能在工作台上做分区，也不能装进另一只存储单元。报 L1 档位，[L1 存储系统](storage_system.md) 直接收，eco 更高档的主机也收得下。

## 配方

<RecipeFor id="neoecoprototype:simplify_singularity_cell" />

装回原样也有一条配方，把这四件材料还给你。
