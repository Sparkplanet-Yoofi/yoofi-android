# Yoofi Android · 接口调用与数据源切换规范

> 面向对象：写业务接口的人、打提测/上线包的人、生成 DTO 的 AI。
> 配套规则：`.cursor/rules/remote-datasource.mdc`（分层模板）、`.ai/architecure.md` 第七章（KMP 预留）。
>
> 最近更新：2026-08-28

---

## 一、HTTP 客户端：Ktor Client 3.5.2（OkHttp 引擎）

2026-08-28 完成 Retrofit → Ktor 迁移。**决策已定，不要再改回去，也不要新增 Retrofit 接口。**

### 1.1 为什么现在换

原本的纪律是「等拆 `core:network` 模块再换」。提前执行的理由是**迁移面此刻最小**：
全项目只有 4 个文件 import 过 `retrofit2` / `okhttp3`，只有 1 个真实接口。
`ApiCaller` 这道接缝把改动锁死在网络层内部，Repository / UseCase / UI **一行未改**。
接口数量只会增长，迁移成本只会变大，所以在只有一个接口时做掉。

### 1.2 引擎为什么仍选 OkHttp

`architecure.md` §6.1 要求超时分级（AI 流式接口单独放大）、指数退避重试、多 CDN 线路降级、
请求签名。Ktor 的 OkHttp 引擎底下就是那套久经考验的连接池与 HTTP/2 实现，
`OkHttp.create { addInterceptor(...) }` 这条口子仍然在——迁移没有丢掉任何弱网能力。
拆 KMP 时 iOS 侧换成 Darwin 引擎，`createYoofiHttpClient` 本体不动。

### 1.3 迁移映射表

| 迁移前 | 迁移后 | 说明 |
|---|---|---|
| `RetrofitApiCaller` | `KtorApiCaller` | 仍是全项目唯一 catch HTTP 的地方 |
| `retrofit2.HttpException` | `io.ktor.client.plugins.ResponseException` | 靠 `expectSuccess = true` 抛出 |
| `java.io.IOException` | `kotlinx.io.IOException` | JVM 上是 typealias，行为一致，但可进 commonMain |
| `AuthApi`（`@POST` 注解接口） | 数据源里的路径常量 + `httpClient.post(path)` | Ktor 无注解接口 |
| `RetrofitAuthRemoteDataSource` | `KtorAuthRemoteDataSource` | 请求体组装逻辑照搬 |
| `AuthHeaderInterceptor`（OkHttp 拦截器） | `defaultRequest { header(Authorization, ...) }` | 拦截器是 Android 专属，进不了 commonMain |
| `Retrofit` + `OkHttpClient` provider | `createYoofiHttpClient(...)` | 抽成纯函数，可测且可进 commonMain |

异常归一化口径保持不变，由 `KtorApiCallerTest` 6 条用例钉住；
Base URL 拼接与 Bearer 头注入由 `YoofiHttpClientTest` 5 条用例钉住。

### 1.4 超时分级

`createYoofiHttpClient` 里 connect 15s / socket 20s / request 30s，与迁移前的 OkHttp 配置一致。
AI 流式接口日后需要单独放大超时时，用 per-request 的 `timeout { }` 覆盖，**不要改全局值**。

### 1.5 剩余的 KMP 待办

`createYoofiHttpClient`、`KtorApiCaller`、`ApiCaller`、`ApiMapping`、各 `XxxRemoteDataSource`
均已不含平台专属代码，拆模块时可直接进 commonMain。仍需处理的只有：
`Dispatchers.IO`（JS/Wasm 无此调度器，Android + iOS 不受影响）与 Hilt 注解（换 DI 时剥离）。

---

## 二、构建阶段：development / staging / production

### 2.1 全项目只有一套词汇

三个档位在任何地方都叫这三个名字，**不要再出现 qa / release / debug 等同义词**：

| 档位 | `BuildStage` | `AppEnvironment` | Gradle 参数 | buildType |
|---|---|---|---|---|
| 开发 | `Development` | `Staging` | `development` | debug（默认） |
| 提测 | `Staging` | `Staging` | `staging` | release |
| 上线 | `Production` | `Production` | `production` | release（默认） |

开发与提测共用同一台测试服务端，所以环境列只有两个值。日后若拆出独立 dev 服务端，
在 `AppEnvironment` 加一个枚举项、改 `forStage` 一处即可，调用方不动。

> 旧词汇 `-Pyoofi.stage=qa` / `=release` 已废弃，传了会直接构建失败并提示正确取值。

### 2.2 两个正交维度，不要合并

| 维度 | 类型 | 决定什么 | 注入源 |
|---|---|---|---|
| 阶段 | `BuildStage` | 能不能用 Demo 数据源 | `BuildConfig.BUILD_STAGE` |
| 环境 | `AppEnvironment` | Base URL 连哪台服务器 | 由阶段推导，`BuildConfig.API_ENV_OVERRIDE` 覆盖 |

分开是因为真实存在「开发阶段连生产环境」这种组合——排查线上问题时要用它。

正交不等于无关：不传覆盖时环境由阶段推导，**这条映射的唯一定义处是 `AppEnvironment.forStage`**：

```kotlin
fun forStage(stage: BuildStage): AppEnvironment = when (stage) {
    BuildStage.Development, BuildStage.Staging -> Staging
    BuildStage.Production -> Production
}
```

构建脚本只透传 `-Pyoofi.api.env` 的原始值，不再自己算映射。用穷举 `when` 是刻意的：
新增阶段时编译器会在这里报错，强制作者显式决定它连哪个环境。

### 2.3 阶段定义

```kotlin
enum class BuildStage(val allowsDemoDataSource: Boolean) {
    Development(allowsDemoDataSource = true),   // 开发
    Staging(allowsDemoDataSource = false),      // 提测
    Production(allowsDemoDataSource = false),   // 上线
}
```

未知取值一律落到 `Production`，保证失手时偏向安全的一侧。

### 2.4 构建命令

| 场景 | 命令 | 阶段 | 实际环境 |
|---|---|---|---|
| 日常开发 | `./gradlew assembleDebug` | development | staging |
| 提测包 | `./gradlew assembleRelease -Pyoofi.stage=staging` | staging | staging |
| 上线包 | `./gradlew assembleRelease` | production | production |
| 调线上问题 | `./gradlew assembleDebug -Pyoofi.api.env=production` | development | production |
| 给产品看 UI 的 release 签名包 | `./gradlew assembleRelease -Pyoofi.stage=development` | development | staging |

`-Pyoofi.api.env` 优先级最高；不传时环境跟随阶段。

两个参数都会**校验取值，拼错直接失败构建**，不静默退回默认值——
否则会出现「以为在打提测包，实际打出上线包」这种事故。

**阶段值不要在 Kotlin 源码里手改。** 这条纪律来自一次真实的坑：`TempMockLoginSuccess = true`
是个写在 domain 层、需要手工翻转的 `const val`，谁忘了改回去，登录就带着 mock 上线。
现在它已被删除，阶段全部由构建注入。

---

## 三、每个接口的 Demo / 真实双实现

### 3.1 唯一开关位置

`core/config/DemoFeature.kt` 是全项目唯一需要手改的开关：

```kotlin
enum class DemoFeature(
    val demoInDevelopment: Boolean,  // 开发阶段是否用 Demo；提测/上线阶段被忽略
    val realImplemented: Boolean,    // 是否已接真实接口
) {
    Auth(demoInDevelopment = true, realImplemented = true),
    Search(demoInDevelopment = true, realImplemented = false),
    ChatRoom(demoInDevelopment = true, realImplemented = false),
}
```

开发时想让某个接口改走真实服务端，把它的 `demoInDevelopment` 改成 `false`，**不用动 Hilt 模块**。

### 3.2 决策规则

```
useDemo(feature) =
    if (!feature.realImplemented) true                       // 还没接真接口，只能 Demo
    else stage.allowsDemoDataSource && feature.demoInDevelopment
```

### 3.3 上线兜底：启动自检

`YoofiApplication.onCreate()` 调用 `DataSourceSwitch.requireReleaseReady()`：
提测 / 上线阶段只要还有 `realImplemented = false` 的能力，**立刻抛异常终止启动**，
并在异常信息里列出是哪几个。

宁可在打提测包当天崩掉，也不能让假数据混进灰度。
需要出一个 release 签名但仍用 Demo 数据的包时，显式加 `-Pyoofi.stage=development`。

### 3.4 切换点选在哪一层

**原则：选在「最低的、已经有真实实现的那一层」。**

| 情况 | 切换点 | 理由 |
|---|---|---|
| 已有 `XxxRemoteDataSource` 契约（如 Auth） | `RemoteDataSource` | Demo 模式下 Repository 的 DTO 映射、会话写入照样跑，假数据和真接口走同一条业务路径，能提前暴露映射 bug |
| 服务端接口未定（如 Search / ChatRoom） | `Repository` | 下面还没有东西可切 |

接口文档落地后，把该能力的切换点从 Repository **下沉**到 RemoteDataSource，
删掉 Demo 仓库，这是一次性动作。

### 3.5 Hilt 接线写法

已有双实现（以 Auth 为例）：

```kotlin
@Provides
@Singleton
fun provideAuthRemoteDataSource(
    switch: DataSourceSwitch,
    demo: Provider<DemoAuthRemoteDataSource>,
    real: Provider<KtorAuthRemoteDataSource>,
): AuthRemoteDataSource = switch.select(DemoFeature.Auth, demo, real)
```

只有 Demo（以 Search 为例）：

```kotlin
@Provides
@Singleton
fun provideSearchRepository(
    switch: DataSourceSwitch,
    demo: Provider<DemoSearchRepository>,
): SearchRepository = switch.selectDemoOnly(DemoFeature.Search, demo)
```

两侧都用 `Provider` 传入：未选中的那一侧不会被实例化，Demo 里的假数据不会在真实构建中占内存。

---

## 四、新增一个接口的完整流程

假设要接 `GET /customer/story/search`。

> 若该业务已有 `RemoteDataSource`，直接从步骤 2 开始。

### 步骤 1：登记开关

`DemoFeature` 加一项（若该业务已存在则跳过）：

```kotlin
Search(demoInDevelopment = true, realImplemented = false),
```

### 步骤 2：写 DTO

`data/search/SearchDtos.kt`，`@Serializable`，**不进 domain**：

```kotlin
@Serializable
data class SearchStoryDto(
    val id: String,
    val title: String,
    val coverUrl: String = "",
)
```

> AI 生成 DTO 时的输入可以是接口文档，也可以是 UI 图。字段一律给默认值，
> `Json` 已配置 `ignoreUnknownKeys` + `explicitNulls = false`，服务端加字段不会炸。

### 步骤 3：纯 Kotlin 契约

`data/search/SearchRemoteDataSource.kt`，禁止出现 Ktor / `android.*`：

```kotlin
interface SearchRemoteDataSource {
    suspend fun search(keyword: String): Outcome<List<SearchStoryDto>>
}
```

### 步骤 4：两个实现

`DemoSearchRemoteDataSource`（假数据 + `delay` 模拟延迟）与
`KtorSearchRemoteDataSource`（注入 `HttpClient` + `ApiCaller`，只组请求）。

```kotlin
private const val SearchPath = "customer/story/search"

@Singleton
class KtorSearchRemoteDataSource @Inject constructor(
    private val httpClient: HttpClient,
    private val apiCaller: ApiCaller,
) : SearchRemoteDataSource {
    override suspend fun search(keyword: String): Outcome<List<SearchStoryDto>> =
        apiCaller.fetch {
            httpClient.get(SearchPath) {
                parameter("keyword", keyword)
            }.body<ApiResponse<List<SearchStoryDto>>>()
        }
}
```

路径写**相对路径**，Base URL 由 `defaultRequest` 注入；不要在这里拼 host。

### 步骤 5：Repository 只依赖契约

DTO→Domain 映射、错误码翻译在这里，**禁止 catch HTTP 异常**：

```kotlin
override suspend fun search(query: String): List<SearchStory> =
    when (val outcome = remote.search(query)) {
        is Outcome.Ok -> outcome.value.map { it.toDomain() }
        is Outcome.Err -> emptyList()
    }
```

### 步骤 6：接线并翻开关

Hilt 模块按 §3.5 写成 `select(...)` 双参数版本，
再把 `DemoFeature.Search.realImplemented` 改成 `true`。

### 步骤 7：验证

```bash
./gradlew testDebugUnitTest
./gradlew assembleRelease -Pyoofi.stage=staging   # 启动自检应当通过
```

---

## 五、红线

1. Repository / UseCase / ViewModel / UI 里 **禁止** catch `ResponseException` 或 `IOException`；
   全项目只有 `KtorApiCaller` 允许。
2. `HttpClient` **只能**注入 `KtorXxxRemoteDataSource`，禁止注入 Repository / UseCase / ViewModel。
3. domain 层禁止出现 DTO、Ktor、`android.*`，也禁止出现 mock 开关。
4. 数据源开关**只写在 `DemoFeature`**，禁止在业务代码里另写 `if (BuildConfig.DEBUG)` 判断。
5. Base URL 只写在 `AppEnvironment`，禁止业务代码硬编码。
6. 新接口未接真实实现前不得进提测包——启动自检会挡住，不要用 `-Pyoofi.stage=development` 绕过去交付。
7. 禁止再引入 Retrofit / OkHttp 直接依赖；OkHttp 只作为 Ktor 引擎传递引入。
8. 禁止改 `createYoofiHttpClient` 的全局超时；单接口要放大超时用 per-request `timeout { }`。

---

## 六、涉及文件索引

| 文件 | 职责 |
|---|---|
| `core/config/BuildStage.kt` | 三阶段常量 |
| `core/config/DemoFeature.kt` | **开关注册表，唯一手改点** |
| `core/config/DataSourceSwitch.kt` | 决策规则 + 启动自检 |
| `di/DataSourceSelection.kt` | `select` / `selectDemoOnly` 接线助手 |
| `di/ConfigModule.kt` | 阶段与开关的注入点 |
| `core/network/ApiCaller.kt` | 纯 Kotlin 网络契约（KMP 接缝） |
| `core/network/KtorApiCaller.kt` | **唯一允许 catch HTTP 的地方** |
| `core/network/YoofiHttpClient.kt` | `HttpClient` 工厂：超时分级 / 日志 / Bearer 头 |
| `core/network/ApiEnvelope.kt` · `ApiMapping.kt` | 信封与错误码归一化 |
| `core/network/AppEnvironment.kt` | Base URL |
| `app/build.gradle.kts` | `BUILD_STAGE` / `API_ENV_OVERRIDE` 注入与取值校验 |
| `YoofiApplication.kt` | 启动自检调用点 |
