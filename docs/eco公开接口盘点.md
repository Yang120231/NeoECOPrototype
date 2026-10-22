# eco 21.2.4 公开接口盘点（86 个 api 类，我们引用 14 个）

现量来源：eco 21.2.4 那颗发布 jar（`neoecoae-21.2.4.jar`，用任一 JDK 的 `javap -v` 读常量池）+ 我们自己的 `src/main/java`。**本表是对着 21.2.4 量的，编译基线现在已经抬到 21.2.9** —— 引用它之前先照那份新 jar 重跑一遍符号核对，别把这张表当成对 21.2.9 的读数。
"eco 内部引用数" = 该类的内部名出现在 eco 自己多少个 class 常量池里（不含自身）。**这个数只说明它是不是 eco 的主干管道，不说明我们用不用得上**；判定看右边。计数器自证：`AuxiliaryPatternHolder` 单独量 9 处、表里也是 9。

结论一句话：**没有一片"我们自建、eco 已有公开口"的重复实现可以删掉**——除了第一节那条纯删除。

---

## 一、纯减法（今天量出来的，两条）

| 东西 | 事实 | 处置 |
|---|---|---|
| `multiblock/trinity/fastpath/TrinityFastPathCompatibility.java` | 它引用了 `ECOFastPathFacade` 与 `ECOFastPathDispatchProvider`（都在我们"已引用 14 个"里），但**全仓库没有任何调用方**——`isFacadeAvailable()` 与 `resolveReadOnly()` 只被自己命中 | 删这个类 ⇒ 已用接口从 14 掉到 12，少两个上游符号引用面 |
| `api/storage/IECOStoragePriorityHost`（未用列表里） | eco 内部只有它自己的 `ECOStorageSystemBlockEntity` 引用（1 处），**没有任何 instanceof 检查**；我们的主机有同签名方法（`SimplifyStorageHostBlockEntity.java:89 :165 :772`）但只是字段+方法 | **别去 implements 它**——照签名补一遍是零现象，只会多一个假"接上了" |

---

## 二、可评估（有真实现象，但要先验前提）

| 类 | eco 内部引用 | 我们这边对应什么 | 要验的前提 |
|---|---|---|---|
| ~~`storage/ECOBigIntegerStorage`~~ | 3 | **已验完：对不上。** 它的签名是 `insertBigInteger(AEKey, BigInteger, Actionable, IActionSource)` + `static insert(MEStorage, ...)`，引用方是 eco 的 `NetworkStorageMixin` / `ECOCraftingThread` / `ECOInfiniteStorage` —— 讲的是"**往 MEStorage 插超过 long 的量**"（无限仓与合成线程用）。我们的 `OversizeInterfaceLogic` 讲的是"接口一格允许多少"（`getMaxAmount(AEKey)` 返回 8192），两回事 | 无动作。eco 整个 jar 里 `InterfaceLogic` 与 `getMaxAmount` **各出现 0 次** ⇒ 它根本不碰接口槽，不可能有替这条的口子 |
| `AuxiliaryPatternHolder` | 9 | 我们的 L1 样板总线**直接注册 eco 的 BE**（`block/crafting/SimplifyCraftingPatternBusBlock.java:9` + `registration/ModRegistration.java:1215 :1260`） | 如果复用成立，那这条联动我们**本来就有**，旧记忆里"白拿、零读数"要改口径为"已具备、缺读数"。要跑一次盘片联动的眼验/断言 |
| `fastpath/EcoFastpathHost` | 8 | 我们的 Trinity 合成模块自己实现 AE2 的 `ICraftingProvider`（`SimplifyTrinityCraftingModuleBlockEntity.java:135`） | eco 的派发器会不会因为我们的 provider 没实现它的宿主口而跳过/重复计——这条只能读 eco 的调用点，或实测 |
| `me/cooling/ECOVirtualCraftingCoolantRegistry` | 2 | 我们有**第二套冷却**：分解机独立流体箱（`MeteoriteDissociatorBlockEntity.java:46 :50`）+ "冷却→功耗"模型（`api/MachineMood.java:52 :61`） | 这个注册表管的是"哪些流体允许当冷却剂"。我们的 `cryotheum_solution` 若不在里面，eco 的虚拟合成可能不认——但那是 eco 的功能，不是我们的分解机 |
| `integration/Integration` / `IntegrationManager` | 15 / 1 | 我们没有等价机制：靠构造函数里手写 `ModList.isLoaded`（`NeoECOPrototype.java:39 :42 :46 :49 :218 :246 :262`）+ 自建注册表 `api/StorageMatrixRegistry.java:15`（冻结点 `:268`） | 旧结论仍在：两个前提未验（我们的注解会不会被它扫到、`apply()` 能否赶在我们构造器之前）。**它治的是"漏注册"，我们那个 mixin 崩溃就是这类** |
| `storage/IECOBulkDisplayCell` | 2 | 我们的小宗仓 + `items/SmallBulkTypeCap.java:20`（绕开 eco final 后端硬编码 25） | `cycleCompressionCutoff` 是"循环切压缩阈值"，和我们的类型上限补丁**不是一回事**。加了它的现象只出现在 **eco 自己的巨型控制器**里，不在我们界面 |
| `NEFakePlayer` | 1 | 生产代码里没有假玩家；只有 gametest 用 `FakePlayerFactory`（`NeoECOPrototypeGameTests.java:8329`） | 备用。哪天解离器要"代客取料"再用它，别自己造 |
| `IOverlayTextureHolder` | 4 | 无对应 | 与主机面板无关，但它是 eco 第二处**"递路径不递下标"**的先例 ⇒ 写上游稿子时引它（面板 marker 与档位名也该这样） |
| `PatternEncodingTermMenuExtension` / `components/AutoExportSides` | 1 / 0 | 我们的接口/供应器有导出侧逻辑（`SimplifySuperconductiveInterfaceBlockEntity.java:33`） | 需逐条比对，还没读 |

---

## 三、无关：合成侧 `me.**` 那 40 个

判定依据是实测的"我们没有对应自建"：`blockentity/trinity/SimplifyTrinityCraftingModuleBlockEntity.java:32` 的注释写得很明白——**`ECOCraftingCPU` / `ECOCraftingCPULogic` 绑死 eco 的 `NEComputationCluster`，要重写 CPU 生命周期与派发得改几千行**。我们没有重写，走的是另一条路：

- 我们自建的是**单任务状态机**，不是 CPU：`multiblock/trinity/TrinityCraftingExecutor.java:38`（Phase `:39`，提交给 AE2 `:232`，单槽 `isBusy()` `:118`）；CPU 在我们那儿只被数个数（`SimplifyTrinityControllerBlockEntity.java:610`）。
- 并行/线程数是**喂给 eco 的数据**，不是实现：`api/SimplifyTier.java:53` + `config/NeoECOPrototypeServerConfig.java:26`。
- 订单：`multiblock/trinity/TrinityTaskRequest.java:8` 只有单 target + `int quantity` ⇒ `me/bigorder.**`（`ECOBigOrderAdmission/Progress/Request/State`、`ECOExactInventory` 17 处）整族与我们无关。
- 生命周期/进度/诊断/附件：我们用的是**手写 NBT**（`TrinityCraftingExecutor.java:52 :396 :406`）+ 自建快照（`TrinityWorkSnapshot.java:9`、`TrinityEnergySnapshot.java:14`、`TrinityTaskRuntime.java:11`）+ LDLib2 的 `@DescSynced`（`SimplifyStorageHostBlockEntity.java:75`）。eco 那套 attachment/lifecycle/progress/diagnostics 挂在**它自己的 crafting job** 上，我们的 executor 不是它的 job ⇒ 接不上，也不需要接。
- 输出路由 `me/output.**`（7 个类）：我们的产物走 AE2 原生路径 ⇒ 无关。
- 菜单侧 `me/menu.**`（6 个）：那是"给 eco 的合成确认菜单提供模式/大订单状态"的宿主口。**只有当我们想让 Trinity 出现在 eco 的合成确认界面里**才需要——目前没有这条需求。

---

## 四、我们已经引用的 14 个

`ECOCellModels`、`ECOComputationModels`、`ECOTier`、`IECOTier`、`api/me/ECOCraftingCPU`、`api/me/ECOFastPathFacade`、`api/me/provider/ECOFastPathDispatchProvider`、`api/storage/ECOCellType`、`api/storage/ECOStorageCells`、`api/storage/IBasicECOCellItem`、`api/storage/IECOBulkMarkableCellItem`、`api/storage/IECOCellHandler`、`api/storage/IECOStorageCell`、`api/storage/IECOStorageCellItem`

其中两个（`ECOFastPathFacade`、`ECOFastPathDispatchProvider`）按第一节应当随 `TrinityFastPathCompatibility` 一起退掉。
另外 `api/storage/ICellHost` 记在"未引用"里是**假未用**：`util.ICellHost extends api.storage.ICellHost`，我们的 `SimplifyDriveBlockEntity.java:50` 与 `SimplifyTrinityComputationModuleBlockEntity.java:30` 实现的是 util 那个 ⇒ 已间接实现。副作用：`canExtractCell` / `getCellExtractionBlockReasonText` 走 default，我们没覆写 ⇒ 新面板上"这只仓为什么取不出来"的提示会是空的。

---

## 五、下一步（按性价比排）

1. 删 `TrinityFastPathCompatibility`（零调用方，纯减法）。
2. ~~验 `ECOBigIntegerStorage` 能不能替掉那个 mixin~~ —— **已验完，替不掉**（见第二节该行）。顺带量到 AE2 自己也没开口子：`InterfaceLogic.config` / `.storage` 都是 `private final`，两个公开构造子 `(IManagedGridNode, InterfaceLogicHost, Item)` 与 `(..., Item, int)` 都不收 `ConfigInventory`，也没有可覆写的工厂方法 ⇒ **mixin 是唯一路，留着**。真要减一个 mixin，对象是 **AE2**（请它把 config/storage 的构造开成 protected 工厂或让构造子收 `ConfigInventory`），不是 eco。
3. 验样板总线那条：我们复用 eco 的 BE，`AuxiliaryPatternHolder` 的 9 处联动到底在不在（旧记忆写"零读数"，需要一次断言或眼验把它钉成"有"或"没有"）。
4. 上游稿子两件事：面板贴图四张覆盖不到 + 档位名 `tierName` 写死三档（引 `getCPUOverlayTexture()` 与 `IOverlayTextureHolder` 两个现成先例）→ 已写成 `NEO_ECO_AE_PR_GUI_TIER_ART.md`，未发送。
