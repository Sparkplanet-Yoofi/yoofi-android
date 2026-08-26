# Yoofi（UGC-AI 互动故事游戏）Android 0-1 完整架构方案（Google Play 海外）

> 产品定位：AI Simulator Games / AI Interactive Stories（见 [yoofi.ai](https://yoofi.ai/)）。玩家创作 AI 互动故事与模拟器玩法、发布、社交游玩。  
> 架构目标：**面向多人协作与长期快速迭代**；优先成熟免费开源技术栈，最小化闭源 SDK 依赖，降低合规与成本风险；适配 AAB 分发、GDPR 合规、海外弱网、服务端 AI 大模型调用。  
> 核心约束：**架构规则必须由工具在 CI 上强制执行**。  
> 仅写在文档里的"强制"约定，在多人协作下必然腐化，这是技术债最主要的来源。

---

## 与当前工程的对齐说明（必读）

本方案是目标态架构。当前 `yoofi-android` 工程为 Studio 模板生成的单模块脚手架，两者存在如下差异，落地前需逐项决策：

| 项目 | 本方案目标态 | 当前工程实际 | 处理建议 |
|---|---|---|---|
| minSdk | **待决策，建议 26~31** | 24 | 见下方说明 |
| 模块结构 | 多模块（本文档第三章） | 单模块 `app` | 按第三章阶段一改造 |

**minSdk 决策说明：**  
1、minSdk 的真正取舍是设备覆盖率 vs 维护成本：API 26 可去掉绝大部分 Compose 兼容分支与 Java 8 desugar 负担，API 31 可直接使用 splash screen API、性能类 API 与更干净的权限模型。  
2、对海外 UGC 游戏，建议 26 起步、观察 Play Console 实际设备分布后再抬到 31，不建议留在 24——API 24/25 的存量设备在海外市场已极低，却会持续产生兼容成本。  

---

## 一、技术栈选型（全部成熟免费开源，生产验证）

> 原则：优先 Jetpack 官方，其次 Square 与社区高星稳定库；尽量减少 Firebase 闭源 SDK，避免 GDPR 审核风险；仅 Google Play Billing 因平台规则必须引入。

| 分层 | 组件 | 选型 | 说明 |
|---|---|---|---|
| 语言 | 语言 | **100% Kotlin，无 Java** | Coroutines + Flow 作为唯一异步方案 |
| UI | UI 框架 | Jetpack Compose Material3 | 单 Activity；MVI 单向数据流 |
| | 导航 | Navigation Compose **类型安全 API** | 用 `@Serializable` data class 定义 Route，编译期校验参数 |
| | 图片 | Coil 3 | Compose 原生适配，支持 WebP/GIF；Coil 3 已支持 KMP |
| | 分页 | Paging 3 | UGC 信息流、作品列表 |
| DI | 依赖注入 | **Hilt (Dagger)** | 编译期注入，适合模块化多人协作。不用 Koin：运行时解析，大项目错误暴露太晚 |
| 网络 | HTTP | OkHttp + Retrofit 2 | 弱网适配：自定义拦截器、超时分级、重试、多 CDN 降级、请求签名 |
| | 序列化 | **kotlinx.serialization**（替代 Moshi，见第七章） | 编译期生成，官方维护，**且是唯一可平滑迁移 KMP 的选择** |
| 持久化 | 数据库 | Room（2.7+） | UGC 草稿、离线内容缓存；2.7+ 起支持 KMP |
| | KV | DataStore (Proto) | 替代 SharedPreferences；存 token、隐私开关、配置 |
| 后台 | 调度 | WorkManager | 日志上报、资源续传、预下载；适配 Doze |
| 监控 | 崩溃/ANR | **Sentry**（开源，可自托管） | 完整堆栈、ANR、Native 崩溃；替代 Crashlytics；采集项可控，GDPR 友好 |
| | 性能 | Macrobenchmark + **Baseline Profile** | 见第九章，Baseline Profile 是冷启动达标的必要手段 |
| 埋点 | 事件 | 自研埋点 + Sentry | **不引入 Firebase Analytics**；事件本地入库，WorkManager 批量上报，可全局开关 |
| 推送 | 消息 | FCM | **唯一强制 Google 服务依赖**；非 GMS 设备降级隐藏推送入口 |
| 内购 | 支付 | Google Play Billing 7+ | 平台强制；凭证一律服务端校验 |
| 多媒体 | 图片选择 | 系统 PhotoPicker | 规避存储权限，GDPR 友好 |
| AI | LLM 调用 | 复用 Retrofit/OkHttp + SSE | **客户端不跑大模型**；仅做 prompt 组装与流式渲染 |
| 质量 | 静态检查 | Detekt + Ktlint | CI 阻断 |
| | **架构守卫** | **Konsist + module-graph-assert + dependency-analysis** | **见第四章，本方案新增的关键补强** |
| 版本 | 依赖管理 | Version Catalog + `[bundles]` | 统一版本，模块间零冲突 |
| 构建 | 构建系统 | AGP 9.x / Gradle 9.x + **build-logic Convention Plugins** | 见第五章 |

> **规避清单**
> 1. 不用 Flutter / React Native：Compose + 原生性能更优；未来若做 iOS，走 KMP/CMP 路线（第七章已预留）。
> 2. 不引入 Firebase 全家桶：仅保留 FCM。
> 3. 不用 RxJava：统一 Flow。
> 4. **不用 Moshi**：功能没问题，但阻断 KMP 迁移路径，详见第七章取舍分析。

---

## 二、分层架构（Clean Architecture + MVI）

严格单向依赖，上层依赖下层，**下层绝不反向依赖上层**：

```
UI (Presentation)  →  Domain  →  Data  →  外部数据源 / SDK
```

1. **UI 层**：只做渲染与派发 Intent，不含业务逻辑。
2. **Domain 层【核心】**：纯 Kotlin，**无任何 Android 框架依赖**。UseCase、业务实体、Repository 接口。全部可 JVM 单元测试，无需 Robolectric/仪器测试。
3. **Data 层**：实现 Domain 定义的 Repository 接口。含 Remote（Retrofit）与 Local（Room/DataStore）数据源，负责"本地优先 + 远程同步"协同。
4. **Core 层**：跨业务的水平能力（网络、存储、设计系统、导航契约、埋点等）。

**Domain 层无 Android 依赖不是洁癖，而是三重收益**：单元测试毫秒级运行、业务规则不受框架升级影响、是未来 KMP 迁移唯一低成本的层。这一条必须由工具强制（见第四章）。

---

## 三、模块化设计【核心章节】

### 3.1 第一性原理：模块化解决什么，代价是什么

模块化真正要解决的是三件事，其余都是衍生收益：

1. **编译期隔离**——让不该被调用的东西在编译期就调不到，而不是靠 code review。
2. **增量构建范围可控**——改一个业务不触发全量重编译，直接决定日常迭代速度。
3. **并行协作边界**——多人同时开发不同业务时冲突面最小。

同时必须承认代价：**每个模块都有成本**（Gradle 配置与同步开销、DI 与导航样板代码、跨模块重构摩擦）。因此模块拆分不是越细越好，**过早细分本身就是技术债**。本方案给出的是带演进节奏的方案（见 3.5），而不是一次性铺开三十个模块。

### 3.2 二维模块矩阵

原方案把 `domain` 和 `data` 各做成一个巨型模块，这是最需要修正的一点。其后果是：**任何业务的 domain 改动都会导致所有 feature 重编译**，模块化最重要的收益（增量构建隔离）直接归零；同时单个 domain 模块会膨胀到数百个 UseCase，失去边界。

正确的结构是二维矩阵：**垂直按业务切分，水平按能力切分**。

```
yoofi-android
├── app                          # 组装层：Application、Hilt 根、导航图组装、启动初始化。不含业务代码
├── build-logic                  # 复合构建：Convention Plugins（第五章）
│
├── core                         # 【水平能力层】跨业务复用，不含任何业务语义
│   ├── common                   # 纯 Kotlin：Result 封装、协程 Dispatcher、扩展函数
│   ├── model                    # 纯 Kotlin：跨业务共享的领域模型
│   ├── network                  # OkHttp/Retrofit/序列化、拦截器、SSE 流式解析
│   ├── database                 # Room 数据库实例、公共 DAO 基础设施、迁移
│   ├── datastore                # DataStore Proto、token 与配置读写
│   ├── designsystem             # 设计 Token、主题、原子组件（Button/Card/Skeleton）
│   ├── ui                       # 业务无关复合组件、UiState 容器、通用 Compose 工具
│   ├── navigation               # 【关键】导航契约：Navigator 接口、FeatureEntry 抽象
│   ├── analytics                # 埋点契约 + 实现，含 GDPR 全局开关
│   ├── billing                  # Billing 封装，对外只暴露接口
│   ├── ai                       # AI 网络能力：SSE 流式、超时分级、重试
│   └── testing                  # 测试基建：Fake、测试规则、test fixtures
│
└── feature                      # 【垂直业务层】每个业务 api / impl 双模块
    ├── auth        (api + impl)
    ├── onboarding  (api + impl)
    ├── home        (api + impl)
    ├── create      (api + impl)   # AI 创作，UGC 核心
    ├── play        (api + impl)   # 游玩 UGC 作品
    ├── social      (api + impl)
    ├── profile     (api + impl)
    └── settings    (api + impl)
```

### 3.3 feature 的 api / impl 分离（解决"feature 间禁止依赖"的落地问题）

原方案规定 feature 之间禁止互相依赖，但只给了单模块结构，规则无法落地——业务上首页必然要跳转游玩页、创作前必然要校验登录态。api/impl 分离正是解决这个矛盾的标准手段。

**`feature:x:api`**（极薄，只放契约）
- 该业务的导航 Route 定义
- 需要被其他业务调用的能力接口
- 必须跨业务共享的少量模型

**`feature:x:impl`**（全部实现，内部再分层）
```
feature/create/impl/src/main/kotlin/.../create/
├── ui              # CreateScreen / CreateViewModel / Intent / UiState / SideEffect
├── domain          # UseCase、业务规则、Repository 接口（本业务私有）
├── data            # Repository 实现、Remote/Local 数据源、DTO 与映射
└── di              # 本业务 Hilt Module
```

**依赖规则**
```
feature:x:impl  →  feature:x:api, feature:y:api（仅 api）, core:*
feature:x:api   →  core:model, core:navigation（保持极薄）
app             →  所有 feature:*:impl（仅为 Hilt 绑定与导航注册）
core:*          →  不得依赖任何 feature
```

**收益是可量化的**：改动 `feature:create:impl` 内部实现时，由于其他业务只依赖 `feature:create:api`，它们**不会重编译**。这正是原单模块方案拿不到的东西。

关于 domain/data 是否要独立成模块：**阶段一先放在 impl 内部作为包**。只有当某业务的 domain 逻辑确实需要被多个业务复用、或 impl 模块大到影响编译时，再抽成 `feature:x:domain`。这是 KISS 原则下的正确顺序——先用包边界，涨不动了再升级成模块边界。

### 3.4 导航契约与 Hilt 多绑定组装

要让 `app` 模块不硬编码每个业务的导航图（否则每加一个业务都要改 app，app 变成瓶颈和冲突热点），用 `core:navigation` 定义契约 + Hilt 多绑定收集。

`core:navigation` 中定义：

```kotlin
// 每个业务实现一份，向导航图注册自己的目的地
interface FeatureEntry {
    fun register(builder: NavGraphBuilder, navController: NavController)
}
```

`feature:create:api` 中定义类型安全路由（其他业务导航过来只需依赖 api）：

```kotlin
@Serializable
data class CreateRoute(val draftId: String? = null)
```

`feature:create:impl` 中注册，并且**实现类用 internal 修饰**，杜绝被外部直接引用：

```kotlin
internal class CreateFeatureEntry @Inject constructor() : FeatureEntry {
    override fun register(builder: NavGraphBuilder, navController: NavController) {
        builder.composable<CreateRoute> { CreateScreen() }
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal interface CreateNavModule {
    @Binds @IntoSet
    fun bind(entry: CreateFeatureEntry): FeatureEntry
}
```

`app` 模块只需注入全集并统一组装，**新增业务时 app 代码零改动**：

```kotlin
@Composable
fun YoofiNavHost(entries: Set<@JvmSuppressWildcards FeatureEntry>) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = HomeRoute) {
        entries.forEach { it.register(this, navController) }
    }
}
```

跨业务的**非导航调用**同样走 api 接口 + Hilt 绑定。例如创作前校验登录态，`feature:auth:api` 暴露 `AuthStatusProvider` 接口，由 `feature:auth:impl` 实现并绑定；`feature:create:impl` 只依赖接口。这就是依赖倒置在模块层面的应用，也是 Meta 等大型 monorepo 处理跨业务调用的通行做法。

### 3.5 模块粒度演进路径（避免过度设计）

| 阶段 | 触发条件 | 模块动作 | 预计模块数 |
|---|---|---|---|
| **阶段一** | 0-1 起步，1~3 人 | `app` + `build-logic` + `core`（common/model/network/database/datastore/designsystem/navigation/analytics/testing）+ 5 个核心业务 api/impl | 约 20 |
| **阶段二** | 团队 >3 人，业务稳定 | 补齐 billing/ai/ui 等 core 模块与剩余业务；大业务抽 `feature:x:domain` | 约 30 |
| **阶段三** | 全量构建时间成为瓶颈 | 按数据分析结果做进一步垂直切分；引入远程构建缓存 | 按需 |

判断依据要用数据而非感觉：用 `./gradlew build --scan` 或构建耗时统计定位真实瓶颈模块，再决定是否拆分。

### 3.6 Public API 边界控制

模块化能否长期不腐化，取决于"不该暴露的东西是否真的暴露不出去"。三道措施：

1. **impl 模块内所有类默认 `internal`**：只有极少数需要被 Hilt 或 app 反射到的类才 public。
2. **Kotlin explicit API mode**：在 `api` 模块与 `core` 模块开启，强制显式声明可见性与返回类型，避免无意扩大 API 面。
   ```kotlin
   kotlin { explicitApi() }
   ```
3. **Gradle 依赖声明纪律**：模块间一律用 `implementation`，只有需要向下游传递的类型才用 `api`。由 dependency-analysis 插件在 CI 上检查（第四章）。

---

## 四、架构规则的自动化强制【新增章节】

这是原方案最大的结构性缺失。原文档用"（强制）"标注依赖规则，但**没有任何技术手段保证它被执行**。多人协作下，人工 review 无法长期拦住违规依赖，架构会在数月内退化成隐式的大泥球。三层守卫必须在阶段一就位：

### 4.1 模块依赖图守卫（module-graph-assert）

在 CI 上断言模块依赖图的合法性，违规直接失败：

```kotlin
moduleGraphAssert {
    maxHeight = 4
    allowed = arrayOf(
        ":app -> :feature:.*:impl",
        ":feature:.*:impl -> :feature:.*:api",
        ":feature:.*:impl -> :core:.*",
        ":feature:.*:api -> :core:(model|navigation)",
        ":core:.* -> :core:.*"
    )
    restricted = arrayOf(
        // 严禁 feature 之间直接依赖实现
        ":feature:.*:impl -X> :feature:.*:impl",
        // 严禁 core 反向依赖业务
        ":core:.* -X> :feature:.*"
    )
}
```

这一条配置，直接把原文档中三条"强制"约定变成了编译期事实。

### 4.2 分层与代码结构守卫（Konsist）

Konsist 用 Kotlin 编写架构断言，作为普通单元测试运行，适合守卫模块内部的分层：

```kotlin
@Test
fun `domain 层不得依赖 Android 框架`() {
    Konsist.scopeFromProject()
        .files
        .withPackage("..domain..")
        .assertFalse { it.hasImport { imp -> imp.name.startsWith("android.") } }
}

@Test
fun `ViewModel 不得直接依赖 Repository 实现`() {
    Konsist.scopeFromProject()
        .classes()
        .withNameEndingWith("ViewModel")
        .assertFalse { it.hasProperty { p -> p.name.endsWith("RepositoryImpl") } }
}
```

### 4.3 依赖声明卫生（dependency-analysis-gradle-plugin）

检测未使用依赖、应降级为 `implementation` 的 `api` 声明、以及缺失的显式依赖。它直接决定增量编译效率——错误的 `api` 声明会让依赖变更沿依赖图扩散，是"改一行等三分钟"的常见根因。

```bash
./gradlew buildHealth
```

### 4.4 CI 阻断顺序

按执行成本从低到高排列，让最便宜的检查先失败：

```
ktlint → detekt → buildHealth → assertModuleGraph → 单元测试(含 Konsist) → 组装 AAB
```

---

## 五、Convention Plugins 与构建性能【新增章节】

### 5.1 为什么必须有 build-logic

20+ 个模块若各自维护 `build.gradle.kts`，版本与配置必然漂移，且升级 AGP/Kotlin 要改二十处。`build-logic` 复合构建把配置收敛成插件，**新建模块只需一行**。

建议提供的插件集：

| 插件 ID | 职责 |
|---|---|
| `yoofi.android.application` | app 模块：applicationId、签名、AAB 配置 |
| `yoofi.android.library` | 通用 Android Library：compileSdk/minSdk、编译选项 |
| `yoofi.android.library.compose` | 叠加 Compose 编译配置与编译器指标 |
| `yoofi.android.feature` | feature:impl 专用：Compose + Hilt + ViewModel + 测试依赖一次配齐 |
| `yoofi.jvm.library` | 纯 Kotlin 模块（domain/model/common），**不引入任何 Android 依赖** |
| `yoofi.android.hilt` | Hilt + KSP 配置 |
| `yoofi.android.test` | 单元测试与 Konsist 架构测试基线 |

新建一个业务模块的完整配置应当是这样（这才叫"快速迭代"）：

```kotlin
plugins {
    id("yoofi.android.feature")
}

android { namespace = "ai.yoofi.feature.create.impl" }

dependencies {
    implementation(projects.feature.create.api)
    implementation(projects.feature.auth.api)
    implementation(projects.core.ai)
}
```

`yoofi.jvm.library` 是 Domain 纯净性的第一道保障——纯 Kotlin 模块从插件层面就拿不到 Android 依赖，配合第四章的 Konsist 形成双保险。

### 5.2 构建性能配置（直接决定迭代速度）

已在当前工程实测验证：`./gradlew help --configuration-cache` 返回 `Configuration cache entry stored.`，**无兼容性问题，可以直接开启**。建议 `gradle.properties`：

```properties
org.gradle.configuration-cache=true
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.jvmargs=-Xmx4096m -XX:+UseParallelGC -Dfile.encoding=UTF-8
kotlin.incremental.useClasspathSnapshot=true
android.nonTransitiveRClass=true
android.useAndroidX=true
```

几点说明：configuration cache 在多模块项目上收益随模块数增长而放大，是模块化的必要配套；`nonTransitiveRClass` 必须开启，否则 R 类会跨模块传递，抵消模块化的编译隔离收益；内存从模板默认的 2G 提到 4G，20+ 模块并行编译下 2G 会频繁 GC 甚至 OOM。

---

## 六、关键业务架构设计

### 6.1 网络层与海外弱网适配

1. OkHttp 拦截器分层：超时分级（**AI 流式接口单独放大超时**）、指数退避重试、多 CDN 线路降级、请求签名。
2. 统一异常模型，明确区分：网络不可达、429 限流、5xx 服务错误、token 过期自动刷新。异常在 Data 层归一化为 Domain 可理解的类型，不把 `IOException` 泄漏到 UI。
3. UGC 大资源分片上传，失败进 Room 待办表，WorkManager 后台续传。

### 6.2 本地缓存与离线优先

1. Room 保存创作草稿与浏览过的作品，断网可浏览、可继续创作。
2. DataStore 存 token、GDPR 采集开关、应用配置。
3. 缓存配额与 LRU 清理——Play Vitals 会评估磁盘占用，UGC 素材极易失控。
4. **单一数据源（SSOT）**：UI 只观察 Room 的 Flow，网络结果先落库再由库驱动 UI。这样"离线优先"是架构的自然结果，不是靠业务代码里的 if-else 拼出来的。

### 6.3 GDPR 隐私合规（架构层嵌入，不可后补）

1. **采集总开关前置**：首启弹 GDPR 同意；不同意则 Sentry 与埋点**完全不初始化**，而不是初始化后不发送。
2. 最小权限：使用系统 PhotoPicker，不申请存储权限；推送权限运行时申请。
3. 本地敏感数据加密存储。
4. App 内提供账号注销与数据删除入口（Play 审核强制项）。
5. 推送可由用户关闭。

### 6.4 内购架构

1. 客户端只负责发起购买与上报凭证，**权限判定完全在服务端**；客户端绝不信任本地支付状态。
2. `core:billing` 对外只暴露接口，Billing SDK 的版本升级（历史上多次破坏性变更）不外溢到业务模块。

### 6.5 大资源分发：用 Play Asset Delivery，不要用 Feature Delivery

原方案将"UGC 大素材做成动态 Feature 模块"，这是选型错误，需要修正。原因有两点：

1. **依赖方向冲突**：Play Feature Delivery 的 dynamic feature 模块必须 `implementation(project(":app"))`，依赖方向是 `dynamic-feature → app`，与本架构 `app → feature` 完全相反。强行使用会在依赖图上引入反向边，破坏第四章的守卫规则，也让 Hilt 组件层级变得混乱。
2. **能力不匹配**：Feature Delivery 面向**代码模块**按需下载；游戏素材属于**资源**，对应的官方方案是 **Play Asset Delivery（PAD）**，支持 install-time / fast-follow / on-demand 三种模式、单包上限远高于 AAB 基础模块限制，且自带纹理压缩格式分发（TCF）。

**结论**：UGC 素材、AI 生成的图像音频等走 **PAD asset pack**；只有在确有"整块玩法代码需要按需下发"时才使用 Feature Delivery，且该动态模块只依赖 `feature:x:api` 与 `core:*`，不承载核心业务逻辑。

---

## 七、技术迁移路径【新增章节】

用户明确要求"方便以后新技术的迁移更新"。迁移能力不是到时候再说，而是**现在就要付的少量代价**。

### 7.1 KMP / Compose Multiplatform 预留

未来做 iOS 时，唯一低成本路径是复用 Domain 与 Data。为此现在需要三条约束：

1. **纯 Kotlin 模块禁用 `java.*`**：用 `kotlinx-datetime` 替代 `java.time`，用 `kotlinx-io` 替代 `java.io`。由 Konsist 断言守卫。
2. **序列化选 kotlinx.serialization，不选 Moshi**——这是本次修订中最重要的选型变更。Moshi 本身质量很好，但它是 JVM/Android 专用，**不支持 KMP**。一旦 DTO 与解析逻辑遍布 Data 层再想迁移，改造面是全量的。kotlinx.serialization 功能等价、官方维护、Retrofit 有官方 converter，现在选它的额外成本几乎为零，却保住了迁移路径。这是典型的"用零成本换未来可选项"。
3. **Room 2.7+ / DataStore 均已支持 KMP**，选版本时确认支持面。

按此约束，未来迁移的实际工作量为：`core:model`、`core:common`、各业务 `domain` 可直接复用；`core:network` 需把 Retrofit 换成 Ktor（**`ApiCaller` 与 `XxxRemoteDataSource` 接口不变**，只替换 `RetrofitApiCaller` / `RetrofitXxxRemoteDataSource`）；UI 层按需用 CMP 重写或各平台原生实现。

当前 Android 阶段仍用 Retrofit。新接口禁止在 Repository 里 catch `HttpException`，必须走 `RemoteDataSource` + `ApiCaller`，规范见 `.cursor/rules/remote-datasource.mdc`。

### 7.2 DI 框架的可迁移性

Hilt 绑定只存在于 `impl` 与 `app`；Domain 层全部使用**构造函数注入**，不出现任何 Hilt 注解。这样未来若迁移到 KMP 友好的 DI（如 kotlin-inject / Metro），Domain 层零改动。

### 7.3 版本升级节奏

| 对象 | 节奏 | 要点 |
|---|---|---|
| Compose BOM | 每季度 | 成套升级，勿单独升子库 |
| Kotlin / KSP | 跟随 Compose 编译器 | 版本必须匹配 |
| AGP / Gradle | 每半年，避开发版窗口 | 先在独立分支验证 |
| targetSdk | 每年 Play 政策截止前 | 提前一个季度适配 |

由 `build-logic` + Version Catalog 收敛后，上述升级均为改动数行的操作，这正是模块化配套设施的价值所在。

---

## 八、工程化 CI/CD 与 Git 规范

1. **Git 分支**：`dev` 保护主干（Trunk-Based）；`release/x.y.z` 发版冻结分支只合 bugfix；`feature/FS-xxxx-brief`；`bugfix/FS-xxxx-brief`；`hotfix/FS-xxxx-brief`。
2. **Commit 规范**：强制携带飞书工作项 ID，例如 `feat: AI 创作草稿保存 FS-1234`，CI 校验，PR 自动关联工作项。
3. **CI 流水线**：MR 触发第 4.4 节的阻断链；`release` 分支自动输出 AAB。
4. **签名与凭据**：release 签名凭据一律由 CI 凭据管理注入环境变量，**密钥与口令不入库**（当前工程 `app/build.gradle.kts` 已按此实现，`.gitignore` 已排除 `*.jks`/`*.keystore`/`keystore.properties`）。
5. **产物**：AAB 上传 Play Console；同时输出 APK 供内部分发。流水线在打包后执行 `apksigner verify` 硬校验，防止凭据注入失败时产出未签名包却显示成功。

---

## 九、性能与包体积目标（Play Vitals 考核，影响商店权重）

| 指标 | 目标 | 手段 |
|---|---|---|
| 冷启动 | < 2.2s | **Baseline Profile**（独立 `:benchmark` 模块生成）+ 启动项懒加载 |
| 崩溃率 | < 0.7% | Sentry 监控 + 灰度 |
| ANR 率 | < 0.4% | 主线程零 IO、Macrobenchmark 回归 |
| 初始安装包 | < 80MB | AAB + 大素材走 PAD |
| 列表流畅度 | 无明显掉帧 | Macrobenchmark 持续监控 UGC 信息流滑动 |

Baseline Profile 需要在模块清单中补充独立模块（原方案遗漏），它对冷启动的改善通常在 20%~30%，是达成 2.2s 目标的关键手段而非可选项。Compose 侧另需开启编译器指标，定位不稳定（unstable）参数导致的过度重组。

---

## 十、风险与取舍

### 优势
1. 全栈开源免费，无第三方 SDK 付费成本。
2. 二维模块矩阵 + api/impl 分离，业务改动的编译影响面被真正隔离。
3. 架构规则由 CI 强制，规则不随人员流动而腐化——这是"无技术债"的唯一可靠保障。
4. GDPR 前置，避免上架被拒后返工。
5. 离线优先，玩家创作内容不丢失。
6. KMP 迁移路径已预留，未来做 iOS 不必重写业务逻辑。

### 劣势与妥协
1. 放弃 Firebase 全家桶，埋点需自研；Sentry 免费额度在用户量上涨后需付费。
2. api/impl 双模块带来样板代码，模块数约翻倍——这是换取编译隔离与协作边界的必要成本，通过 Convention Plugin 将单模块配置压到 3~5 行来对冲。
3. Domain 层抽象前期开发略慢，长期多人维护收益显著。
4. FCM 依赖 GMS，非 GMS 设备需降级隐藏推送入口。
5. 架构守卫会在初期挡下一些"图快"的写法，团队需要适应期。

---

## 十一、里程碑

| 阶段 | 周期 | 产出物 |
|---|---|---|
| 阶段 1：脚手架 | 2 周 | 多模块骨架（约 20 模块）、build-logic 插件集、**架构守卫三件套接入 CI**、网络/Room/DataStore/Sentry、AAB 与签名注入、Git 规范、GDPR 基础框架 |
| 阶段 2：基础能力 | 3-4 周 | 登录、草稿本地优先、AI 生成链路（SSE 流式）、首页信息流、埋点开关、多语言 |
| 阶段 3：核心 UGC | 4-5 周 | 完整创作流程、发布、游玩、社交、Billing 接入 |
| 阶段 4：合规与性能 | 2-3 周 | GDPR 完整流程、权限裁剪、Baseline Profile、包体积与性能压测、商店材料 |
| 阶段 5：灰度 | 2 周 | Closed → Open Testing、Sentry 线上监控、修复迭代、正式上架 |

**阶段 1 必须包含架构守卫接入**。守卫是唯一"越早越省"的投入：阶段 1 接入成本约 1 人日，等到阶段 3 违规依赖已成事实再补，代价是数周的重构。

---

## 十二、落地清单（阶段一可直接执行）

1. `settings.gradle.kts` 启用 `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`，模块引用写成 `projects.core.network`，重命名可被编译期发现。
2. 建立 `build-logic` 复合构建与第五章插件集。
3. 按 3.2 建立 core 水平层与 5 个核心业务的 api/impl。
4. 接入 module-graph-assert、Konsist、dependency-analysis 并挂进 CI。
5. 开启 configuration cache（已实测兼容）、build cache、parallel，JVM 内存提到 4G。
6. `core`/`api` 模块开启 `explicitApi()`；`impl` 模块类默认 `internal`。
7. 统一 MVI 骨架：`Intent` / `UiState`（密封类）/ `SideEffect`（Channel），`stateIn(SharingStarted.WhileSubscribed(5000))` 作为 Flow→StateFlow 标准写法。
8. 建立 `:benchmark` 模块，产出 Baseline Profile 并纳入 release 构建。
