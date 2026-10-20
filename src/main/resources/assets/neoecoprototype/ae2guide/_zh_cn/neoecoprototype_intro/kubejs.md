---
navigation:
  title: KubeJS 支持
  icon: neoecoprototype:simplify_small_bulk_expansion_card
  position: 40
  parent: neoecoprototype_intro/index.md
---

# KubeJS 支持

KubeJS 是可选模组，装了才有下面这些。

| 能做什么 | 入口 |
|----------|------|
| 注册自定义存储矩阵 | 启动脚本，两种 builder 类型：`neoecoprototype:storage_matrix`、`neoecoprototype:infinite_storage_matrix`；可设容量、类型数、材质、绑定资源 |
| 造一只自己长货的仓 | 同一个 builder 加 `.grows(单批产量, 间隔刻)`：每隔那么久世界刻给出一次绑定的物品/流体/化学品，任何东西都推不进去 |
| 增删改本模组配方 | 服务端脚本；处理器装配室的配方可按字段删除，也可用原始 JSON 新增 |

两类脚本的时机不一样：**启动脚本**在游戏启动时跑，注册矩阵必须走它（要赶在注册表冻结之前）；**服务端脚本**在每个服务端加载时重跑，改配方用它。完整示例在本模组仓库的 `docs/kubejs.md` 与 `docs/kubejs-example.js`。
