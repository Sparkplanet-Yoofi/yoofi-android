# Yoofi KMP shared 模块下沉方案（v2 · 可执行版）

> 修订日期 2026-09-01。v1 是方向性讨论稿，**照它写代码会编译失败**（原因见第零章）。
> v2 基于对 `/Users/jackxu/Desktop/androidspace/yoofi-android` 与
> `/Users/jackxu/Desktop/androidspace/yoofi-shared` 的实际扫描重写，
> 每一步都给出**改哪个文件、写什么代码、用什么命令验证**。
>
> **读者是 AI 与执行者，不是决策者**：本文不再论证「要不要做 KMP」，只回答「怎么做不出错」。

---

## 〇、v1 方案的阻断项（必须先纠正，否则无法落地）

按严重度分三档。前四条属于「照做必然失败」。

### A 级 · 致命（照 v1 写代码 100% 失败）

| # | v1 的说法 | 实际情况 | 后果 |
|---|---|---|---|
| A1 | 下沉「Work 作品 / Chapter 章节 / Node 节点 / Event 事件 / PerformanceCard 演出卡 / Prop 道具 / NPC / Condition 条件 / Achievement 成就」，实现 `ConditionEvaluator`、画布 PRD 发布校验、`SaveCanvasUseCase` / `TriggerEventUseCase` / `PropOperateUseCase` | **这些类在 Android 仓库中一个都不存在。** 实际 domain 是 auth / avatar / block / chat / feedback / gamedetail / profile / report / search，共 57 文件 1396 行 | AI 会去迁移不存在的文件，整章无法执行。**这是 v1 最大的问题**，见第九章待确认事项 |
| A2 | 「跨平台本地存储用 **Realm-KMP**」 | Realm / Atlas Device SDK 于 2024-09 弃用，**2025-09-30 已 EOL**；且 Realm-Kotlin 停留在 Kotlin 1.x，与本项目 Kotlin 2.2.10 不兼容 | 引入一个已停止维护且无法编译的库 |
| A3 | 未提及 AGP 与 KMP 插件的兼容约束 | 本项目 **AGP 9.0.1**。AGP 9 起 `org.jetbrains.kotlin.multiplatform` **不能**与 `com.android.library` / `com.android.application` 同模块共存，必须改用 `com.android.kotlin.multiplatform.library` | shared 模块 Gradle sync 直接失败 |
| A4 | 全篇未提 DI 策略 | **Hilt 不支持 KMP**。待下沉的 13 个类带 `@Inject` / `@Singleton` | commonMain 里出现 `javax.inject` 会在 iOS/Web 目标编译失败 |

### B 级 · 严重（会导致返工或选型错误）

| # | v1 的说法 | 修正 |
|---|---|---|
| B1 | 编译目标写作 `js(WasmJs)` | KMP 中 `js()` 与 `wasmJs()` 是**两个独立 target**，不存在 `js(WasmJs)` 写法。且 wasmJs 应推迟到 P2，理由见 §3.3 |
| B2 | 「P0 就实现 `IRealTimeChatApi`(IM) / `IAnalyticsApi`(埋点) / `II18nApi`」 | 这三者在 Android 仓库中**尚无任何实现**（无融云 SDK、无 Firebase、无 i18n 抽象）。为不存在的能力先建抽象是空转，应推迟到接入时 |
| B3 | `IImageLoaderApi` 契约「输出加载状态 Flow」 | 过度设计。Coil / Kingfisher 自带状态管理，包一层是负收益。真正该下沉的是 **URL 构建 / 尺寸档位 / 缓存 key** 等纯字符串逻辑，见 §7.2 |
| B4 | IM SDK 一律称 "Nexconn" | 实际选型是**融云（RongCloud）海外版 IMLib**。平台矩阵与桥接方式见 §7.3 |
| B5 | 未说明两个 Git 仓库如何协同 | `yoofi-shared` 是**独立空仓库**（仅 README + .gitignore）。消费方式必须先定，见 §2 |

### C 级 · 与现状不符（描述偏差）

| # | v1 的说法 | 现状 |
|---|---|---|
| C1 | 目录结构含 `repository` / `storage` 分包 | 本项目 Repository 接口在 `domain/*`、实现在 `data/*`；无任何数据库。分包应沿用现有约定，见 §4 |
| C2 | 未提 `Outcome` / `AppError` | 项目已有统一结果类型（`core/common/`），且**已经是纯 Kotlin**，直接复用，不要另造 |
| C3 | 未提测试框架差异 | 现有 57 个单测全部使用 JUnit4（`org.junit.Test`）。commonTest **只能用 `kotlin.test`**，下沉时必须改写，见 §8.3 |
| C4 | 未提 `BuildConfig` | `BuildStage` / `AppEnvironment` 依赖 `BuildConfig.BUILD_STAGE` 等字段，这是 Android 专属，下沉需替换注入方式，见 §7.6 |

---

## 一、现状盘点（2026-09-01 实扫）

### 1.1 Android 仓库真实规模

```
app/src/main/kotlin/ai/yoofi/app/    共 193 文件 / 22729 行
├─ core    17 文件 /   620 行   common(2) config(3) network(6) image(5) item(1)
├─ data    15 文件 /  1083 行   auth(7) avatar(2) chat(1) gamedetail(1) search(1) image.crop(2) item(1)
├─ di      10 文件 /   544 行   全部 Hilt
├─ domain  57 文件 /  1396 行   ★ 零 android.* / javax.* / dagger.* 导入
└─ ui      92 文件 / 19025 行   18 个 ViewModel（2208 行）+ Compose 页面
```

**关键事实**：`domain/` 57 个文件经 grep 验证，**没有任何 `android.*`、`androidx.*`、`java.*`、`javax.*`、`dagger.*` 导入**。这是 v1 没有指出、但决定成败的最有利条件——domain 层几乎可以原样搬迁。

### 1.2 domain 真实业务清单（替代 v1 的画布清单）

| 业务包 | 文件数 | 内容 |
|---|---|---|
| `domain/auth` | 14 | User / AuthSession / LinkedAccount / DeleteAccountProof + 7 个 UseCase + 2 个 Repository 契约 |
| `domain/gamedetail` | 12 | GameDetailModels / GameItem / GameMap / GameCastCard / GameCastCharacter + 6 个 UseCase |
| `domain/avatar` | 8 | 头像拍照 / 裁剪 / 落盘编排（**含平台语义**，见 §6.3） |
| `domain/profile` | 7 | ProfileDraft / PreviewPlayedWork / MineProfilePresence + 3 个 UseCase |
| `domain/chat` | 4 | ChatModels / ChatRoomRepository / ObserveChatRoom / AdvanceChatStory |
| `domain/search` | 4 | SearchModels / SearchRepository + 2 个 UseCase |
| `domain/block` | 3 | BlockedUser + 2 个 UseCase |
| `domain/report` | 3 | ReportDraft / ReportReason / SubmitReport |
| `domain/feedback` | 2 | FeedbackType / SubmitFeedback |

### 1.3 第三方接入现状（v1 假设了很多其实不存在的东西）

| 能力 | v1 假设 | 实际 |
|---|---|---|
| 网络 | 需下沉 | ✅ **已是 Ktor 3.5.2 + kotlinx.serialization**，`YoofiHttpClient` 已按「engine 由外部传入」设计，注释明写「拆 KMP 时可整体进 commonMain」 |
| 图片加载 | 封装 Coil / Kingfisher | ❌ **项目根本没有 Coil**。当前是 7 处 `when(key) -> R.drawable.xxx` 的 Demo 映射 |
| IM | 已有 Nexconn | ❌ **无任何 IM SDK**。聊天走 `DemoChatRoomRepository` 假数据 |
| 埋点 | 已有 Firebase | ❌ **无**。`architecure.md` 明确不用 Firebase Analytics |
| 数据库 | 迁 Realm-KMP | ❌ **无任何数据库**（Room 尚未引入） |
| DataStore | — | ❌ 无。token 存在 `InMemoryUserSession`（进程内存） |

**结论**：v1 大量篇幅在讨论尚不存在的能力。**P0 的真实工作面只有：domain + core.common + core.config + core.network + data 的 Demo/Remote 实现**。

### 1.4 yoofi-shared 仓库现状

```
/Users/jackxu/Desktop/androidspace/yoofi-shared/
├── .git/            独立 Git 仓库
├── .gitignore
├── .idea/
└── README.md        48 字节
```

**完全空白，无 Gradle 工程**。所有构建脚手架需从零创建（§8.1）。

---

## 二、工程形态决策（先解决「两个仓库怎么协同」）

三种方案，**推荐方案 B**。

| 方案 | 做法 | 优点 | 缺点 |
|---|---|---|---|
| A 单仓 | 把 shared 并入 yoofi-android | 最简单，一次构建 | iOS / Vue 团队要拉整个 Android 仓；与「三端平权」冲突 |
| **B 独立仓 + Composite Build**（推荐） | shared 独立仓，Android 用 `includeBuild` 引入源码 | 改 shared 立即在 Android 生效，无需发包；iOS / Vue 各自独立消费 | 开发者需同时 clone 两个仓库 |
| C 独立仓 + Maven 私服 | shared 发 `.aar` / `.klib` 到私服 | 版本可控，CI 友好 | 迭代期每改一行都要发包，效率低 |

### 2.1 推荐落地：开发期 B，集成期叠加 C

目录约定（两仓库**同级放置**，Composite Build 的相对路径依赖此约定）：

```
~/Desktop/androidspace/
├── yoofi-android/     Android 宿主
└── yoofi-shared/      KMP shared
```

`yoofi-android/settings.gradle.kts` 追加：

```kotlin
// shared 的接入模式，-Pyoofi.shared.mode 切换（完整说明见 §12.2）：
//   source（默认）= Composite Build 源码依赖；binary = Maven 仓库已发布产物
val sharedMode = providers.gradleProperty("yoofi.shared.mode").orNull ?: "source"

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // 二进制模式专用，源码模式不加，避免本地陈旧产物污染日常构建
        if (sharedMode == "binary") mavenLocal()
    }
}

// 路径按「两仓库同级」约定；不成立时用 -Pyoofi.shared.dir 覆盖，
// 缺失则退化为纯 Android 构建（只克隆本仓的同事和 CI 仍可编译）。
val sharedDir = (providers.gradleProperty("yoofi.shared.dir").orNull
    ?: "../yoofi-shared").let(::file)
when {
    sharedMode == "binary" ->
        logger.lifecycle("yoofi-shared：二进制模式，按版本目录里的 yoofiShared 版本解析")

    sharedDir.resolve("settings.gradle.kts").exists() -> includeBuild(sharedDir)

    else -> logger.lifecycle("未找到 yoofi-shared（$sharedDir），本次为纯 Android 构建")
}
```

坐标登记进版本目录（红线：禁止硬编码版本），`gradle/libs.versions.toml`：

```toml
[versions]
yoofiShared = "0.1.0-SNAPSHOT"

[libraries]
yoofi-shared = { group = "ai.yoofi", name = "shared", version.ref = "yoofiShared" }
```

`app/build.gradle.kts` 依赖写法——**源码与二进制两种模式共用这一行**：

```kotlin
implementation(libs.yoofi.shared)
```

> **为什么不用 `project(":shared")`**：Composite Build 的模块不在同一 settings 里，`project(...)` 解析不到。必须用坐标 + `includeBuild` 的自动替换机制。源码模式下 `includeBuild` 按 `group:name` 匹配并忽略版本号，所以同一行声明在两种模式下都成立。

> ⚠️ **`clean` 陷阱（实测踩到）**：included build 的**根项目**必须应用 `base` 插件。
> 根 `build.gradle.kts` 里插件全是 `apply false` 时，根项目没有 `clean` 任务，
> Android Studio 的 Clean Project 会按完整路径请求 `:yoofi-shared:clean` 并失败：
> `Cannot locate tasks that match ':yoofi-shared:clean'`。
> **命令行察觉不到**——`./gradlew clean` 走任务名模糊匹配，命中了 `:shared:clean` 就成功了。
> 两仓根 `build.gradle.kts` 现均已加 `base`。

**实测结果：一次成功，未踩坑。** `./gradlew :app:dependencies` 输出
`+--- ai.yoofi:shared -> project :yoofi-shared:shared`，Android variant 选择正确。

**兜底方案（保留备查）**：KMP 模块有多个平台 variant，Composite Build 的坐标替换在某些版本组合下会解析到错误 variant（典型报错：`Could not resolve ai.yoofi:shared` 或选中了 `-jvm` 而非 Android variant）。若日后遇到，按顺序尝试：

1. 确认 shared 模块的 `group` 与 `version` 和 Android 侧声明的坐标完全一致；
2. 在 Android 侧显式声明替换关系：

```kotlin
includeBuild(sharedDir) {
    dependencySubstitution {
        substitute(module("ai.yoofi:shared")).using(project(":shared"))
    }
}
```

3. 仍不行则退回方案 C（发布到 `mavenLocal()`，`./gradlew publishToMavenLocal` 后在 Android 侧加 `mavenLocal()` 仓库）。

---

## 三、版本矩阵与兼容性红线

### 3.1 被 AGP 钉死的版本（不可自选）

本项目 `libs.versions.toml` 已注明：AGP 9.0.1 内置 KGP **2.2.10**，低于此版本会被自动抬升。因此：

| 组件 | 版本 | 约束来源 |
|---|---|---|
| AGP | 9.0.1 | 现状 |
| Gradle | 9.1.0 | 现状（KMP 库插件要求 ≥ 9.1.0，满足） |
| **KGP / kotlin-multiplatform 插件** | **必须 2.2.10** | 与 AGP 内置 KGP 严格对齐，否则 KSP 报 "KSP is not compatible with AGP's built-in Kotlin" |
| `com.android.kotlin.multiplatform.library` | 9.0.1 | 与 AGP 同版本 |
| KSP | 2.2.10-2.0.2 | 现状 |
| **Ktor** | **3.3.3**（不是现状的 3.5.2） | **实测强制降档，原因见 §3.3** |
| kotlinx-serialization-json | 1.9.0 | 现状 |
| kotlinx-coroutines | 1.10.2 | 现状（test 已引入） |

**红线**：shared 仓库的 `kotlin` 版本**必须写死 2.2.10**，与 Android 仓库一致。两仓库版本漂移会导致 klib 元数据不兼容。

### 3.2 AGP 9 的 KMP 插件切换（A3 的具体解法）

```kotlin
// ❌ AGP 9 下会失败：KMP 插件不能与 com.android.library 同模块
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

// ✅ 正确
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}
```

配套差异（AI 极易踩坑，逐条列出）：

| 项 | 旧（com.android.library） | 新（com.android.kotlin.multiplatform.library） |
|---|---|---|
| Android 配置块 | 顶层 `android { }` | `kotlin { android { } }` |
| Android target 声明 | `kotlin { androidTarget() }` | 不需要，`android { }` 即声明 |
| 单元测试源集目录 | `src/test/` | **`src/androidHostTest/`** |
| 仪器测试源集目录 | `src/androidTest/` | **`src/androidDeviceTest/`** |
| 启用单元测试 | 默认开 | 需显式 `withHostTest { }` |
| Java 编译 | 默认开 | 需显式 `withJava()` |
| Android 资源 | 默认开 | 需显式 `androidResources { enable = true }` |
| 依赖声明 | 顶层 `dependencies { }` | `kotlin { sourceSets { androidMain.dependencies { } } }` |

> AGP 8.12 起 `androidLibrary { }` 被 `android { }` 取代，且 `androidLibrary { }` 自 AGP 9.1.0-alpha09 起弃用。本项目 AGP 9.0.1，**用 `android { }`**。

### 3.3 ⚠️ klib ABI 陷阱：Ktor 必须降到 3.3.3（实测踩到）

**这是本次搭建骨架时真实撞到的编译失败**，不降版本 iOS 目标 100% 编不过：

```
w: KLIB resolver: Skipping 'ktor-client-content-negotiation-iosSimulatorArm64Main-3.5.2.klib'
   having incompatible ABI version '2.3.0'. The library was produced by '2.3.21' compiler.
   The current Kotlin compiler can consume libraries having ABI version <= '2.2.0'.
e: KLIB resolver: Could not find "...ktor-client-content-negotiation-iosSimulatorArm64Main-3.5.2.klib"
```

**原理**：Kotlin/Native 的 klib 有 ABI 版本，**编译器只能消费 ABI ≤ 自身版本的 klib**。AGP 9.0.1 把编译器钉在 Kotlin 2.2.10（ABI 2.2.0），而 Ktor 3.4+ 的 Native 产物是 Kotlin 2.3.x 编译的（ABI 2.3.0）。

**为什么 Android 侧一直没事**：JVM 字节码没有 klib ABI 这套机制，`ktor-client-okhttp` 是普通 jar。**这个雷只在加 Native/Wasm 目标时才炸**，纯 Android 项目永远碰不到。

**Ktor 版本 ↔ 编译它的 Kotlin 版本**（查 Maven Central pom 实测）：

| Ktor | 编译用 Kotlin | klib ABI | Kotlin 2.2.10 能用？ |
|---|---|---|:--:|
| 3.0.3 | 2.0.21 | 2.0.0 | ✅ |
| 3.1.3 | 2.1.20 | 2.1.0 | ✅ |
| 3.2.3 | 2.1.21 | 2.1.0 | ✅ |
| 3.3.0 | 2.2.10 | 2.2.0 | ✅ |
| **3.3.3** | **2.2.21** | **2.2.0** | ✅ **推荐（3.3.x 最新）** |
| 3.4.0 | 2.3.0 | 2.3.0 | ❌ |
| 3.5.2 | 2.3.21 | 2.3.0 | ❌ **当前 Android 仓版本** |

**决策：两仓库统一降到 Ktor 3.3.3。** Android 仓的 `libs.versions.toml` 也要改——否则 app 依赖 shared 后 Gradle 会把 Ktor 解析到 3.5.2，与 shared klib 的编译期版本不一致，埋运行时隐患。

> 同理需要盯的还有 kotlinx-coroutines 与 kotlinx-serialization。实测当前的 1.10.2 / 1.9.0 在 Kotlin 2.2.10 下 iOS 编译通过，**升级这三个库前必须先跑一次 `:shared:compileKotlinIosSimulatorArm64`**。
>
> 长期解法是升 AGP 让编译器版本跟上，但那会牵动整条版本链（compileSdk、Compose BOM、lifecycle），属于独立议题。

### 3.4 编译目标：P0 只做三个，wasmJs 推迟

| Target | P0 | 理由 |
|---|:--:|---|
| `android` | ✅ | 宿主 |
| `iosArm64` / `iosSimulatorArm64` / `iosX64` | ✅ | iOS 消费，产出 XCFramework |
| `jvm` | ✅ | **纯 JVM 单测跑得最快**，不依赖 Android SDK，CI 上 `:shared:jvmTest` 秒级反馈 |
| `wasmJs` | ❌ 推迟 P2 | 见下方风险 |

**wasmJs 推迟到 P2 的原因**：

1. 与 §3.3 的 klib ABI 问题同源（wasmJs 也是 klib 体系）。降到 Ktor 3.3.3 后**技术上大概率可用，但未实测**，纳入前必须先跑通一次。
2. 已确认「画布业务在 Web 创作端、Android 只做消费端」，因此 **Android/iOS 与 Web 的共享面比 v1 设想的小得多**：三端真正共用的主要是网络 DTO 与条件求值这类纯逻辑，而后者尚未开发。
3. Web 端未必需要 Kotlin 产物——若 Vue 侧只要类型定义，导出 TypeScript 声明或走 OpenAPI 生成，成本远低于 wasmJs。这是待决策项（§9）。

**P0 结论：先交付 Android + iOS 双端复用，Web 端作为独立议题。** 这不影响后续加 target——commonMain 只要守住「零平台 API」，加 wasmJs 是纯构建配置工作。

---

## 四、模块与源集结构

### 4.1 shared 仓库目录（P0 落地形态）

```
yoofi-shared/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/                       # 与 Android 仓同为 Gradle 9.1.0
└── shared/
    ├── build.gradle.kts
    └── src/
        ├── commonMain/kotlin/ai/yoofi/shared/
        │   ├── common/                # ✅已落地 Outcome / AppError        ← core/common
        │   ├── config/                # ✅已落地 BuildStage / DemoFeature / DataSourceSwitch
        │   ├── network/               # ✅已落地 ApiCaller / ApiEnvelope / ApiMapping
        │   │                          #   AppEnvironment / YoofiHttpClient / KtorApiCaller
        │   ├── domain/                # ⬜待迁 按业务分包，镜像 Android 现状
        │   │   ├── auth/  chat/  gamedetail/  profile/
        │   │   ├── search/  block/  report/  feedback/
        │   ├── data/                  # ⬜待迁 Repository / DataSource 实现
        │   │   ├── auth/  chat/  gamedetail/  search/
        │   ├── platform/              # ⬜待建 DeviceIdentity 接口，随 data/auth 一起落地（§6.4）
        │   └── di/                    # ⬜待建 SharedContainer，仅 iOS/Web 需要（§5.2）
        ├── commonTest/kotlin/         # ✅已落地 5 个测试类 / 29 用例，kotlin.test，禁止 org.junit
        ├── androidMain/kotlin/        # 空：引擎与 DeviceIdentity 都由 Hilt 在宿主侧传入
        ├── iosMain/kotlin/            # ✅已落地 network/IosHttpClient.kt（见 §5.4）
        ├── jvmMain/kotlin/            # 空
        └── androidHostTest/kotlin/    # 注意目录名不是 test/
```

> 标记含义：✅已落地 = 当前仓库中真实存在；⬜待迁 / ⬜待建 = 本方案规划、尚未创建。
> **AI 执行前先对照实际目录**，不要假设 ⬜ 项已存在。

### 4.2 与 `architecure.md` 目标架构的关系（v1 未澄清）

`architecure.md` 第三章的目标是 `core:* + feature:x:api/impl` 多模块。两者**不冲突，但要说清分工**：

- **shared 是「跨端维度」的切分**，Android 侧看它就是一个 `core` 级依赖。
- Android 仓库未来拆 `feature:x:impl` 时，各 impl 依赖 shared 中对应的 `domain/x`，而不是自己再写一份。
- **红线不变**：shared 内部同样禁止 `feature` 间横向依赖；`shared/domain` 禁止依赖 `shared/data`。

> 落地顺序建议：**先 shared 下沉，后 Android 多模块拆分**。反过来做，拆完的模块还要再拆一次。

---

## 五、DI 策略（A4 的解法）

### 5.1 结论：shared 内不引入任何 DI 框架

三个候选：

| 方案 | 结论 |
|---|---|
| Hilt | ❌ 不支持 KMP，直接排除 |
| Koin | ❌ **项目红线禁止**（AGENTS.md 硬性红线第 5 条） |
| **纯构造函数 + 手写 Factory** | ✅ **采用**。零依赖、零注解处理、iOS/Web 侧调用最自然 |

### 5.2 具体写法

> ### 📌 实测修正：Android 侧**不需要** SharedContainer
>
> 下面的 `SharedContainer` 是为 **iOS / Web** 准备的——那两端没有 DI 框架，需要一个装配入口。
>
> **Android 侧实测证明它是多余的**：现有 `NetworkModule` / `ConfigModule` 本来就是手写 `@Provides` 风格
> （`provideJson`、`provideHttpClient`、`provideDataSourceSwitch`…），下沉后这些方法体一个字都不用改，
> 只需改 import。硬套 SharedContainer 反而会造出「Hilt 提供 Container、Container 再吐组件」的双层间接。
>
> **唯一必须改的一处**：`NetworkModule` 原有
> `@Binds abstract fun bindApiCaller(impl: KtorApiCaller): ApiCaller`。
> `@Binds` 要求实现类自身可被 Hilt 构造，而 `KtorApiCaller` 下沉后已无 `@Inject` 构造函数，
> 必须改成 `@Provides fun provideApiCaller(json: Json): ApiCaller = KtorApiCaller(json)`。
>
> **通用规则：所有下沉类型在 Android 侧一律从 `@Binds` / 构造注入改为手写 `@Provides`**，
> 因为 shared 不认识 Hilt，装配责任全部落在宿主侧。

**shared 侧**：类只保留构造函数，**删除所有 `@Inject` / `@Singleton`**。

```kotlin
// commonMain/kotlin/ai/yoofi/shared/network/KtorApiCaller.kt
// 无任何 DI 注解：单例与生命周期由各端宿主决定（Android 用 Hilt，iOS 用 Swift 单例）
class KtorApiCaller(
    private val json: Json,
) : ApiCaller { /* 实现不变 */ }
```

**shared 侧提供一个纯 Kotlin 装配入口**，避免每端重复接线：

```kotlin
// commonMain/kotlin/ai/yoofi/shared/di/SharedContainer.kt

/**
 * shared 的装配入口。各端宿主只需构造一次并持有。
 *
 * 不用任何 DI 框架：Hilt 不支持 KMP，Koin 被项目红线禁止。
 * 依赖少的时候手写 Factory 最直观，也让 iOS / Web 调用方零学习成本。
 *
 * @param engine 平台 HTTP 引擎，Android 传 OkHttp、iOS 传 Darwin
 * @param stage 构建阶段，由各端从自己的构建配置读取后传入（Android 来自 BuildConfig）
 * @param deviceIdentity 设备标识，由各端实现后传入——用接口注入而非 expect/actual，
 *   因为 Android 实现需要 Context，无参 expect 函数拿不到（见 §6.4）
 * @param apiEnvOverride 显式环境覆盖，空串表示按 stage 推导
 */
class SharedContainer(
    engine: HttpClientEngine,
    stage: BuildStage,
    private val deviceIdentity: DeviceIdentity,
    apiEnvOverride: String = "",
    enableLogging: Boolean = false,
) {
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        isLenient = true
    }

    val dataSourceSwitch: DataSourceSwitch = StageDataSourceSwitch(stage)

    val environment: AppEnvironment = AppEnvironment.resolve(stage, apiEnvOverride)

    val userSessionStore: UserSessionStore = InMemoryUserSession()

    val httpClient: HttpClient = createYoofiHttpClient(
        engine = engine,
        baseUrl = environment.baseUrl,
        json = json,
        accessTokenProvider = userSessionStore::currentAccessToken,
        enableLogging = enableLogging,
    )

    val apiCaller: ApiCaller = KtorApiCaller(json)

    val authRepository: AuthRepository = RemoteAuthRepository(
        remote = if (dataSourceSwitch.useDemo(DemoFeature.Auth)) {
            DemoAuthRemoteDataSource()
        } else {
            KtorAuthRemoteDataSource(httpClient, apiCaller, deviceIdentity)
        },
        sessionStore = userSessionStore,
    )
}
```

**Android 侧**：Hilt 只负责把 `SharedContainer` 拆成可注入的 bean，**业务代码零改动**。

```kotlin
// app/src/main/kotlin/ai/yoofi/app/di/SharedModule.kt
@Module
@InstallIn(SingletonComponent::class)
object SharedModule {

    @Provides
    @Singleton
    fun provideSharedContainer(
        stage: BuildStage,
        // AndroidDeviceIdentity 实现 shared 的 DeviceIdentity 接口，由 Hilt 注入 Context 构造
        deviceIdentity: DeviceIdentity,
    ): SharedContainer = SharedContainer(
        engine = OkHttp.create(),
        stage = stage,
        deviceIdentity = deviceIdentity,
        apiEnvOverride = BuildConfig.API_ENV_OVERRIDE,
        enableLogging = BuildConfig.DEBUG,
    )

    // 逐个转出，ViewModel 的注入签名完全不用改
    @Provides @Singleton
    fun provideAuthRepository(c: SharedContainer): AuthRepository = c.authRepository

    @Provides @Singleton
    fun provideUserSessionStore(c: SharedContainer): UserSessionStore = c.userSessionStore

    @Provides @Singleton
    fun provideHttpClient(c: SharedContainer): HttpClient = c.httpClient

    @Provides @Singleton
    fun provideApiCaller(c: SharedContainer): ApiCaller = c.apiCaller
}
```

> **迁移期的关键收益**：ViewModel 的 `@Inject constructor(private val xxxUseCase: XxxUseCase)` 一行都不用改，只要 UseCase 的**包名**在 Android 侧仍能解析（§8.4 用 typealias 过渡）。

### 5.3 UseCase 的 DI

现状 UseCase 全部由 `di/AuthModule.kt` 等以 `@Provides` 手写构造（**没有用构造函数注入**），这是巧合的利好：UseCase 类本身零注解，可直接下沉，Android 侧 `@Provides` 方法体不变。

### 5.4 iOS 侧的便捷工厂（`iosMain` 唯一的文件）

`SharedContainer` 要求宿主传入 `HttpClientEngine`。Android 侧这很自然（Hilt 里一行 `OkHttp.create()`），但 **Swift 调用方要构造 Ktor 的 Darwin 引擎非常别扭**——需要跨语言引用 Kotlin 的 `Darwin` object，导出的符号名难认且随版本变化。

因此在 `iosMain` 放一个工厂，把引擎选择留在 Kotlin 侧。

> **当前实际落地的是简化版**：`iosMain/kotlin/ai/yoofi/shared/network/IosHttpClient.kt` 里的
> `createIosHttpClient(baseUrl, json, accessTokenProvider, enableLogging)`。
> 因为 `SharedContainer` 尚未创建（依赖 data/auth 先下沉），当前只需要包住 Darwin 引擎这一层。
> 等 §6.1 的 data/auth 迁完、`SharedContainer` 建起来后，再升级为下面的完整形态。

```kotlin
// iosMain/kotlin/ai/yoofi/shared/di/IosSharedContainer.kt（⬜ 待建，SharedContainer 就绪后）

/**
 * iOS 侧的装配入口。Swift 只需传业务参数，不必接触 Ktor 引擎类型。
 *
 * 这不是 expect/actual——commonMain 没有对应的 expect 声明，
 * 只是给 iOS 调用方的一个便捷重载，Android 侧不需要（Hilt 已经在做同样的事）。
 */
fun createSharedContainer(
    stage: BuildStage,
    deviceIdentity: DeviceIdentity,
    apiEnvOverride: String = "",
    enableLogging: Boolean = false,
): SharedContainer = SharedContainer(
    engine = Darwin.create(),
    stage = stage,
    deviceIdentity = deviceIdentity,
    apiEnvOverride = apiEnvOverride,
    enableLogging = enableLogging,
)
```

Swift 侧用法：

```swift
let container = IosSharedContainerKt.createSharedContainer(
    stage: .production,
    deviceIdentity: IosDeviceIdentity(),   // Swift 实现 shared 的 DeviceIdentity 接口
    apiEnvOverride: "",
    enableLogging: false
)
```

> 这也是「shared API 必须由三端共同评审」的具体体现：只从 Android 视角设计，就会漏掉这类 Swift 侧的可用性问题。

---

## 六、逐文件迁移清单

图例：**AS-IS** 只改 package 行原样搬 · **STRIP** 删 DI 注解后搬（其余不动）

### 6.1 P0 批次一：基础设施（17 文件，零业务风险）

| 源文件（`app/src/main/kotlin/ai/yoofi/app/`） | 目标源集 | 动作 | 备注 |
|---|---|---|---|
| `core/common/AppError.kt` | commonMain `common/` | AS-IS | |
| `core/common/Outcome.kt` | commonMain `common/` | AS-IS | |
| `core/config/BuildStage.kt` | commonMain `config/` | AS-IS | BuildConfig 只在 KDoc 中提及 |
| `core/config/DemoFeature.kt` | commonMain `config/` | AS-IS | |
| `core/config/DataSourceSwitch.kt` | commonMain `config/` | AS-IS | 含 `StageDataSourceSwitch` |
| `core/network/ApiCaller.kt` | commonMain `network/` | AS-IS | |
| `core/network/ApiEnvelope.kt` | commonMain `network/` | AS-IS | 纯 kotlinx.serialization |
| `core/network/ApiMapping.kt` | commonMain `network/` | AS-IS | |
| `core/network/AppEnvironment.kt` | commonMain `network/` | AS-IS | |
| `core/network/YoofiHttpClient.kt` | commonMain `network/` | AS-IS | engine 已是入参，**零改动** |
| `core/network/KtorApiCaller.kt` | commonMain `network/` | **STRIP** | 删 `javax.inject.Inject/Singleton`；`Dispatchers.IO` 需改，见下 |
| `data/auth/AuthDtos.kt` | commonMain `data/auth/` | AS-IS | |
| `data/auth/AuthRemoteDataSource.kt` | commonMain `data/auth/` | AS-IS | |
| `data/auth/DemoAuthRemoteDataSource.kt` | commonMain `data/auth/` | STRIP | |
| `data/auth/InMemoryUserSession.kt` | commonMain `data/auth/` | STRIP | |
| `data/auth/KtorAuthRemoteDataSource.kt` | commonMain `data/auth/` | STRIP | 依赖 DeviceIdentity，见 §6.4 |
| `data/auth/RemoteAuthRepository.kt` | commonMain `data/auth/` | STRIP | |

**`KtorApiCaller` 的唯一实质改动** —— `Dispatchers.IO` 在 commonMain 不存在（Kotlin/Native 无此调度器）：

```kotlin
// ❌ commonMain 编译失败
withContext(Dispatchers.IO) { ... }

// ✅ 方案一（推荐）：直接去掉。Ktor 的 suspend API 本身不阻塞线程，
//    OkHttp / Darwin 引擎内部各自管理 IO 线程池，外层再切一次是多余的。
try { block().toOutcome() } catch ( ... )

// ✅ 方案二：确需切换时用 expect/actual 提供调度器
// commonMain: internal expect val ioDispatcher: CoroutineDispatcher
// androidMain: internal actual val ioDispatcher = Dispatchers.IO
// iosMain:     internal actual val ioDispatcher = Dispatchers.Default
```

> 采用方案一。现有 `KtorApiCallerTest` 用 MockEngine 验证异常归一化，去掉 `withContext` 后行为不变，测试可原样跑。

### 6.2 P0 批次二：domain 层（49 文件，AS-IS 为主）

以下**整包 AS-IS 搬迁**，仅改 `package` 声明：

| 源包 | 文件数 | 动作 |
|---|---|---|
| `domain/auth/` | 14 | AS-IS |
| `domain/chat/` | 4 | AS-IS |
| `domain/gamedetail/` | 12 | AS-IS |
| `domain/profile/` | 7 | AS-IS |
| `domain/search/` | 4 | AS-IS |
| `domain/block/` | 3 | AS-IS |
| `domain/report/` | 3 | AS-IS |
| `domain/feedback/` | 2 | AS-IS |

配套 data 层实现（STRIP，删 `@Inject`/`@Singleton`）：

| 源文件 | 行数 | 动作 |
|---|---|---|
| `data/chat/DemoChatRoomRepository.kt` | 149 | STRIP |
| `data/gamedetail/DemoGameDetailRepository.kt` | 216 | STRIP |
| `data/search/DemoSearchRepository.kt` | 57 | STRIP |

### 6.3 P0 不动：`domain/avatar/`（8 文件）

**唯一需要判断的 domain 包**。它虽无 `android.*` 导入，但语义强绑定 Android 文件系统与相机权限（`AvatarLocalStore` 操作文件路径、`CameraCapability` 查权限），其实现 `FileAvatarLocalStore`（256 行）深度依赖 `Context` / `Uri` / `FileProvider` / `ExifInterface`。

**决策：P0 保留在 Android 侧。** 强行下沉需要先抽象文件系统与权限模型，投入产出比在 iOS 尚未启动时为负。P2 再评估。

### 6.4 P0 的平台抽象：expect/actual 数量为 0

v1 列了 6 个 `expect` 抽象接口，其中 4 个对应的能力**在项目中尚不存在**（IM / 埋点 / i18n / 图片），属于为空气建抽象。P0 真正需要跨端差异化的只有一处，且**它不该用 expect/actual**。

**唯一的平台抽象：DeviceIdentity** —— `KtorAuthRemoteDataSource` 登录时上报设备信息，Android 实现读 `Settings.Secure.ANDROID_ID` + `Build.MODEL`。

```kotlin
// commonMain/kotlin/ai/yoofi/shared/platform/DeviceIdentity.kt

/** 登录请求需要的设备标识。各端读各自系统 API，业务侧不感知来源。 */
interface DeviceIdentity {
    /** 设备唯一标识；GDPR 要求不得使用广告 ID 等可跨应用追踪的标识 */
    val deviceId: String
    /** 设备型号，用于服务端问题排查 */
    val deviceModel: String
    /** 平台标识：android / ios / web */
    val platform: String
}
```

**注意：这里只是普通接口，不是 `expect`。** 这是刻意的选择——

> ⚠️ **Android 的 Context 是 expect/actual 的经典陷阱**：`expect fun createDeviceIdentity(): DeviceIdentity` 无参数，androidMain 的 actual 实现拿不到 Context，只能靠全局 `lateinit var` 或 `androidx.startup.Initializer` 兜，既脆弱又难测。
>
> **正解是接口注入**：`SharedContainer` 构造函数接收 `DeviceIdentity` 实例，Android 侧现有的 `data/auth/DeviceIdentity.kt` 改为 `class AndroidDeviceIdentity @Inject constructor(@ApplicationContext ctx: Context) : DeviceIdentity`，由 Hilt 注入后传进来。iOS 侧同理，用 Swift 实现后传入。
>
> **凡是实现需要平台上下文（Context / UIApplication）的能力，一律用接口注入而非 expect/actual。** expect/actual 只适合「无需上下文即可构造」的场景。

**唯一考虑过 expect/actual 的地方：平台 HTTP 引擎** —— 若不想让各端宿主自己传 engine，本可以写成：

```kotlin
// commonMain
internal expect fun defaultHttpEngine(): HttpClientEngine
// androidMain: actual fun defaultHttpEngine() = OkHttp.create()
// iosMain:     actual fun defaultHttpEngine() = Darwin.create()
// jvmMain:     actual fun defaultHttpEngine() = CIO.create()
```

> P0 建议**也不做**：`SharedContainer` 已经接收 `engine` 参数，宿主传入更灵活（Android 侧要保留 `OkHttp.create { addInterceptor(...) }` 加弱网重试的口子）。
>
> **P0 最终 expect/actual 数量：0。** 这是好事——纯 commonMain 无平台分支，编译与测试都最简单。

### 6.5 永不下沉（明确留 Android）

| 文件 | 原因 |
|---|---|
| `di/*`（10 文件） | Hilt 专属 |
| `core/image/ImageCropCompressor.kt`、`ImageCropExporter.kt` | Bitmap / File / Build.VERSION |
| `core/image/crop/ImageCropHost.kt`、`ImageCropSession.kt` | Compose 契约 + Bitmap |
| `core/item/preview/ItemPreviewHost.kt` | Compose 契约 |
| `data/avatar/*`（2 文件） | Context / Uri / FileProvider / ExifInterface |
| `data/image/crop/canhub/*`（2 文件） | CanHub SDK 适配层 |
| `data/item/preview/BitmapItemPreviewHostRenderer.kt` | Compose + R.drawable |
| `data/auth/DeviceIdentity.kt` | Context / Settings.Secure（改为实现 shared 的接口） |
| `ui/*`（92 文件） | 全部 Compose |

### 6.6 P1 候选：ViewModel 中的业务逻辑

扫描发现 **6 个「胖」ViewModel** 含非展示逻辑，可在 P1 抽成 shared 的 UseCase / Validator：

| ViewModel | 行数 | 可下沉的部分 |
|---|---:|---|
| `GameDetailViewModel` | 230 | 评论树递归操作（`findRecursively` / `mapLike` / `remove`，L206-230）——纯数据结构算法 |
| `ChatRoomViewModel` | 215 | `storyTurn` 回合推进与消息装配（L101-102, L171-191） |
| `DeleteAccountViewModel` | 212 | 4 个校验 getter（L38-47），与 `DeleteAccountProof.isValid()` **重复**，应合并 |
| `ReportContentViewModel` | 125 | 字数上限 500 / 截图上限 3 / 步骤流转（L17-18, L30-32, L71-87） |
| `SearchViewModel` | 123 | 搜索状态机与 300ms 防抖编排（L83-122） |
| `FeedbackViewModel` | 93 | `canSubmit` 表单规则（L28） |

另有 **Composable 中的业务逻辑**（更该优先下沉，因为完全不该在 UI 层）：

| 位置 | 内容 |
|---|---|
| `ui/chat/ChatMentionDraft.kt` L9-35 | `@mention` 插入/替换规则，**已有独立单测**，是最干净的下沉候选 |
| `ui/auth/EmailSignUpScreen.kt` L49-51 | 邮箱正则校验——**当前只在 UI 层，domain 无对应规则** |
| `ui/auth/VerificationCodeScreen.kt` L57,76 | 验证码长度 6 / 纯数字过滤，与 `VerifyEmailCodeUseCase` L17-18 **重复定义** |
| `ui/auth/ProfileSetupScreen.kt` L104,117-120 | 昵称长度上限 24 与占用校验 |

> P1 的价值主张很明确：**这些规则现在有 Android / iOS 各写一遍的风险，且已经出现 Android 内部重复定义**（验证码长度、删除确认短语各有两处）。

---

## 七、第三方专题（修正 v1 的判断）

### 7.1 网络请求 —— ✅ 下沉，且几乎零成本

v1 判断正确，但低估了现状：**本项目已经做完了 90%**。

- `YoofiHttpClient.kt` 的 `createYoofiHttpClient(engine, baseUrl, json, accessTokenProvider, enableLogging)` 签名已完全平台无关。
- `KtorApiCaller` 用 `kotlinx.io.IOException`（KMP 安全）而非 `java.io.IOException`。
- 唯一改动是删 `withContext(Dispatchers.IO)` 与 `javax.inject` 注解（§6.1）。

各端引擎：Android `OkHttp`、iOS `Darwin`、JVM 测试 `CIO`、（未来）Web `Js`。

**平台专属配置留在各端**：Android 的证书固定 / 弱网重试拦截器写在 `OkHttp.create { }` 里传入；iOS 的 ATS 配置在 Info.plist。commonMain 不碰。

### 7.2 图片加载 —— ⚠️ v1 的契约设计过度，需缩小范围

**v1 的问题**：主张 `IImageLoaderApi` 输出「加载状态 Flow」。这是负收益——Coil 的 `AsyncImage` 与 Kingfisher 的 `KFImage` 都自带状态管理与 Compose/SwiftUI 集成，外面包一层 Flow 反而要手写 placeholder / error / 取消逻辑，代码更多、性能更差。

**正确边界**：下沉「**算 URL 的纯逻辑**」，不下沉「**加载动作**」。

```kotlin
// commonMain/kotlin/ai/yoofi/shared/image/ImageUrlBuilder.kt

/** 图片尺寸档位。与 CDN 预设一致，避免各端随意传宽高导致缓存击穿。 */
enum class ImageVariant(val width: Int) {
    Thumb(160), Card(480), Cover(1080), Original(0)
}

/**
 * 图片 URL 构建。纯字符串运算，三端必须一致——
 * 一旦 Android 和 iOS 拼出不同 URL，CDN 缓存命中率会腰斩，且 A/B 数据无法对齐。
 */
object ImageUrlBuilder {
    fun build(baseUrl: String, key: String, variant: ImageVariant): String { /* ... */ }

    /** 本地缓存 key：三端一致才能保证「同一张图只下一次」的口径可比 */
    fun cacheKey(key: String, variant: ImageVariant): String = "$key@${variant.width}"
}
```

各端用法：Android `AsyncImage(model = ImageUrlBuilder.build(...))`，iOS `KFImage(URL(string: ImageUrlBuilder.build(...)))`。**shared 不出现 Bitmap / UIImage / 解码 / 缓存**——这一点 v1 说得对，保留。

**当前阻塞**：项目尚未引入 Coil，且图片全是 `when(key) -> R.drawable.xxx` 的本地 Demo 映射（7 处）。**接入真实 CDN 前，此项无事可做**。

### 7.3 融云海外版 IM —— expect/actual，但方向与 v1 不同

**平台矩阵（官方文档核实）**：

| 平台 | SDK | 接口语言 | KMP 支持 |
|---|---|---|:--:|
| Android | IMLib 5.10.x | Java | ❌ |
| iOS | IMLib 5.10.x | Objective-C | ❌ |
| Web | IMLib 5.9.x | JavaScript | ❌ |

融云**没有官方 KMP SDK**，三端都是原生 SDK，必须 expect/actual 桥接。v1 结论方向正确，但遗漏了关键设计。

**真正的高价值下沉点不是「连接 SDK 的抽象」，而是消息契约**：

```kotlin
// commonMain/kotlin/ai/yoofi/shared/im/ChatMessagePayload.kt

/**
 * 自定义消息体。融云传输的是 JSON 字符串，三端各自解析极易漂移——
 * 这里用 kotlinx.serialization 定义唯一真源，Android/iOS/Web 共用同一份反序列化代码。
 *
 * 新增消息类型时只改这里，三端同时生效；忘记加的一端会编译失败而不是线上静默丢消息。
 */
@Serializable
sealed interface ChatMessagePayload {
    @Serializable @SerialName("text")
    data class Text(val body: String) : ChatMessagePayload

    @Serializable @SerialName("map_go")
    data class MapGo(val locationName: String, val sceneKey: String) : ChatMessagePayload

    @Serializable @SerialName("item_use")
    data class ItemUse(val itemId: String, val targetIds: List<String>) : ChatMessagePayload
}
```

> 这直接对应已实现的 Map「Go」消息与道具使用消息（见 `PROJECT_MEMORY.md` 的 Map 章节），是**现在就能落地的真实收益**。

**分层建议**：

| 层 | 位置 | 内容 |
|---|---|---|
| 消息 payload 契约 | commonMain | ✅ 上面的 `@Serializable` 定义 |
| 会话 / 消息领域模型 | commonMain | ✅ 复用现有 `domain/chat/ChatModels.kt` |
| 重连退避、消息去重、本地排序 | commonMain | ✅ 纯算法，JVM 可测 |
| 连接 / 收发 / token 续期 | expect/actual | ⚠️ 各端封装原生 SDK |
| 推送（FCM / APNs） | 各端原生 | ❌ 不下沉 |

**iOS 侧注意**：融云 iOS 是 Objective-C，Kotlin/Native 可通过 CocoaPods 集成 + ObjC interop 直接调用，但**回调需转成 Kotlin Flow**，且 ObjC 的 nullability 标注不全会产生大量 `T?`。这部分工作量 v1 完全未估计。

### 7.4 埋点 —— P0 不做

项目**无任何埋点实现**，且 `architecure.md` 明确不用 Firebase Analytics（改自研 + Sentry）。为不存在的能力先建 expect 接口是空转。接入时再按 §7.3 的模式办：**事件名与参数 schema 下沉 commonMain**（这是三端一致性的真需求），上报通道各端 actual。

### 7.5 本地存储 —— Realm 已死，改选型

| 候选 | 结论 |
|---|---|
| ~~Realm-KMP~~ | ❌ **2025-09-30 EOL**，且卡在 Kotlin 1.x |
| **Room 2.7+** | ✅ **推荐**。官方 KMP 支持（Android / iOS / JVM），且 `architecure.md` 第一章已选定 Room |
| Room 3.0 | ⚠️ 加了 JS / WasmJs target，但**仍是 alpha**，且 Web 端需自备 Web Worker。P0 不用 |
| SQLDelight | 可选备胎，但与 architecure.md 选型冲突，不推荐 |
| multiplatform-settings | KV 场景可选；但 `architecure.md` 定的是 DataStore（DataStore 也支持 KMP） |

**P0 不引入任何存储**：项目当前零数据库，token 存内存。存储下沉应与「引入 Room」这件事**一起做**，而不是先建抽象。

> Room KMP 注意点：需为每个 target 单独加 KSP 配置（`add("kspAndroid", ...)`、`add("kspIosArm64", ...)` 等），iOS 需 `-lsqlite3` 链接选项。

### 7.6 BuildConfig 的替代（C4 的解法）

`BuildConfig` 是 AGP 生成的 Android 专属类，commonMain 不可见。现有三处引用：

| 位置 | 字段 | 处理 |
|---|---|---|
| `di/ConfigModule.kt` | `BUILD_STAGE` | 留 Android，解析成 `BuildStage` 后**作为参数传给 `SharedContainer`** |
| `di/NetworkModule.kt` | `API_ENV_OVERRIDE` | 同上，传参 |
| `di/NetworkModule.kt` | `DEBUG` | 同上，传 `enableLogging` |

**原则：shared 不读构建配置，只接收已解析好的值。** iOS 侧从 `Info.plist` / Build Configuration 读，Web 侧从环境变量读，各自转成 `BuildStage` 传入。这样 `BuildStage` / `AppEnvironment` / `DataSourceSwitch` 三个类可以原样 AS-IS 下沉。

---

## 八、分阶段落地步骤（每步含验证命令）

> ### ✅ 落地进度（2026-09-01 实测，非纸面推演）
>
> **Step 1～4 已全部实际执行并通过，Android 已跑在 shared 之上。**
>
> | 步骤 | 状态 | 证据 |
> |---|:--:|---|
> | 8.1 工程骨架 | ✅ | `com.android.kotlin.multiplatform.library` 与 `withHostTest { }` 在 AGP 9.0.1 下确认可用 |
> | 8.2 迁移 core 层 11 文件 | ✅ | common(2) + config(3) + network(6) 已在 commonMain |
> | 8.3 迁移单测 | ✅ | `:shared:jvmTest` **29 个用例全绿** |
> | iOS 编译 + Framework | ✅ | 三架构 `YoofiShared.framework` 均产出（**降 Ktor 到 3.3.3 之后**） |
> | 8.4 Android 接入 | ✅ | Composite Build 替换成功；40 文件改 import；**174 + 29 = 203，与迁移前用例总数守恒**；APK 内确认含 14 个 shared 类 |
> | 双模式依赖（§12.2） | ✅ | 源码 / 二进制两种模式**均实测编出 APK**；二进制模式解析到 `shared-android` variant |
> | ABI 契约守卫（§12.5） | ✅ | 基线 `shared/api/` 已生成；实测能拦下破坏性签名变更；`:shared:check` 全绿（2m09s） |
>
> **Android 仓同步改动**：Ktor 3.5.2 → 3.3.3、`settings.gradle.kts` 加模式开关与 `includeBuild`、
> 版本目录登记 `yoofi-shared` 坐标、`app/build.gradle.kts` 用 `implementation(libs.yoofi.shared)`、
> `NetworkModule` 的 `@Binds` 改 `@Provides`、删除 11 个源文件 + 5 个测试文件。
>
> **过程中修正了本文档四处错误**，均已回写：Ktor 版本（§3.3）、kotlin.test 断言参数顺序（§8.3）、
> Android 侧无需 SharedContainer（§5.2、§8.4）、KGP 内置 ABI 校验不会自动挂 check（§12.5）。
>
> **当前 shared 实际内容**：commonMain 11 文件（common 2 / config 3 / network 6）、
> commonTest 5 文件 29 用例、iosMain 1 文件（`IosHttpClient.kt`）。
> `domain/` `data/` `platform/` `di/` **尚未创建**。
>
> 剩余待迁移：`data/auth` 6 文件 + domain 49 文件 + data 3 个 Demo Repository。

### 8.1 Step 1 —— 创建 shared 工程骨架

**产物**：可编译的空 KMP 模块。

`yoofi-shared/gradle/libs.versions.toml`：

```toml
[versions]
# 必须与 yoofi-android 完全一致：AGP 9.0.1 内置 KGP 2.2.10，版本漂移会导致 klib 不兼容
agp = "9.0.1"
kotlin = "2.2.10"
# ⚠️ 不能用 Ktor 3.4+：Native klib 由 Kotlin 2.3.x 编译（ABI 2.3.0），
# 而 Kotlin 2.2.10 只能消费 ABI <= 2.2.0，iOS 目标会编译失败。详见 §3.3
ktor = "3.3.3"
kotlinxSerializationJson = "1.9.0"
kotlinxCoroutines = "1.10.2"

[libraries]
ktor-client-core = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }
# 只有 darwin 在 P0 被引用（iosMain 的便捷工厂）；okhttp / cio 预留给 P1，
# 现阶段 Android 引擎由宿主 app 模块自己依赖，JVM 侧测试用 MockEngine
ktor-client-darwin = { group = "io.ktor", name = "ktor-client-darwin", version.ref = "ktor" }
ktor-client-okhttp = { group = "io.ktor", name = "ktor-client-okhttp", version.ref = "ktor" }
ktor-client-cio = { group = "io.ktor", name = "ktor-client-cio", version.ref = "ktor" }
ktor-client-content-negotiation = { group = "io.ktor", name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-logging = { group = "io.ktor", name = "ktor-client-logging", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { group = "io.ktor", name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-mock = { group = "io.ktor", name = "ktor-client-mock", version.ref = "ktor" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "kotlinxCoroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
android-kmp-library = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }
```

`yoofi-shared/settings.gradle.kts`：

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "yoofi-shared"
include(":shared")
```

`yoofi-shared/build.gradle.kts`：

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.kmp.library) apply false
}

// Composite Build 下 Android 侧按坐标依赖，group / version 必须与之匹配
allprojects {
    group = "ai.yoofi"
    version = "0.1.0-SNAPSHOT"
}
```

`yoofi-shared/shared/build.gradle.kts`：

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    // AGP 9 起 KMP 不能与 com.android.library 同模块，必须用这个插件
    alias(libs.plugins.android.kmp.library)
    // 二进制模式（§12.2）与发版（§12.4）用。KMP 插件会自动为每个 target 建 publication
    `maven-publish`
}

kotlin {
    // ABI 校验：守住「shared 公开 API 只增不改不删」，完整说明见 §12.5
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {
        enabled.set(true)
    }

    // Android target：注意配置块在 kotlin { } 内部，不是顶层 android { }
    android {
        namespace = "ai.yoofi.shared"
        compileSdk = 36
        minSdk = 24

        // 新插件默认不开单元测试，必须显式声明；源集目录是 androidHostTest 而非 test。
        // 该 DSL 在 AGP 9.x 早期版本名为 withHostTestBuilder {}.configure {}，
        // 若此处报「Unresolved reference」，换成 builder 写法，两者语义相同。
        withHostTest { }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // 纯 JVM target：单测在这里跑最快，不需要 Android SDK
    jvm()

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "YoofiShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        // androidMain / jvmMain 在 P0 无代码也无依赖：
        // 引擎由宿主传入（Android app 模块自己已依赖 ktor-client-okhttp），
        // 单测用 commonTest 的 MockEngine，不需要真实引擎。
        iosMain.dependencies {
            // 仅供 §5.4 的 iOS 便捷工厂使用
            implementation(libs.ktor.client.darwin)
        }
    }
}

// ⚠️ 必须显式挂 check。KGP 内置的 ABI 校验**不会**自动插入 check 流水线
// （已废弃的独立 binary-compatibility-validator 插件会），不挂就永远不跑。见 §12.5
tasks.named("check") {
    dependsOn("checkLegacyAbi")
}
```

`yoofi-shared/gradle.properties`：

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
org.gradle.caching=true
kotlin.code.style=official
android.useAndroidX=true
# 暂不开 org.gradle.configuration-cache：Kotlin/Native 的 link 任务在部分版本上与之不兼容，
# 骨架跑通、CI 稳定之后再单独验证开启
```

Gradle Wrapper 与 Android 仓保持一致（9.1.0）：

```bash
cd /Users/jackxu/Desktop/androidspace/yoofi-shared
gradle wrapper --gradle-version 9.1.0
```

**✅ 验证**：

```bash
cd /Users/jackxu/Desktop/androidspace/yoofi-shared
./gradlew :shared:compileKotlinJvm
```

通过标准：BUILD SUCCESSFUL。若报 `com.android.kotlin.multiplatform.library` 找不到，检查 `pluginManagement` 的 google 仓库过滤是否放行 `com.android.*`。

---

### 8.2 Step 2 —— 迁移基础设施（§6.1 的 17 个文件）

顺序严格按依赖倒序（先无依赖的）：

1. `common/`（AppError、Outcome）— ✅ 已完成
2. `config/`（BuildStage、DemoFeature、DataSourceSwitch）— ✅ 已完成
3. `network/`（ApiCaller、ApiEnvelope、ApiMapping、AppEnvironment、YoofiHttpClient、KtorApiCaller）— ✅ 已完成
4. `data/auth/`（**下沉 6 个**）— ⬜ 待做。该目录下共 7 个文件，
   但 `DeviceIdentity.kt` 依赖 `Context` / `Settings.Secure`，按 §6.5 留在 Android，
   改为实现 shared 的 `DeviceIdentity` 接口（§6.4）

包名映射规则：`ai.yoofi.app.core.common` → `ai.yoofi.shared.common`，`ai.yoofi.app.core.network` → `ai.yoofi.shared.network`，`ai.yoofi.app.data.auth` → `ai.yoofi.shared.data.auth`。

改动清单（除 package 行外）：
- 删除所有 `import javax.inject.Inject` / `Singleton` 与对应注解
- `KtorApiCaller` 删 `withContext(Dispatchers.IO)`（§6.1）
- `KtorAuthRemoteDataSource` 的 `DeviceIdentity` 改为构造参数（§6.4）

**✅ 验证**：

```bash
./gradlew :shared:compileKotlinJvm :shared:compileKotlinIosSimulatorArm64
```

同时编译 JVM 与 iOS 是**关键**：只编 JVM 无法暴露 `java.*` 泄漏。

---

### 8.3 Step 3 —— 迁移单元测试（JUnit4 → kotlin.test）

现有 57 个测试全用 JUnit4，commonTest **必须**改写。转换规则：

| JUnit4 | kotlin.test |
|---|---|
| `import org.junit.Test` | `import kotlin.test.Test` |
| `import org.junit.Assert.assertEquals` | `import kotlin.test.assertEquals` |
| `import org.junit.Assert.assertTrue` | `import kotlin.test.assertTrue` |
| `import org.junit.Assert.assertNull` | `import kotlin.test.assertNull` |
| `import org.junit.Assert.assertNotNull` | `import kotlin.test.assertNotNull` |
| `import org.junit.Assert.assertFalse` | `import kotlin.test.assertFalse` |
| `assertEquals(expected, actual)` | 两参数版顺序**相同**，无需调整 |
| `assertEquals(message, expected, actual)` | ⚠️ **`assertEquals(expected, actual, message)`——message 从最前挪到最后** |
| `assertTrue(message, condition)` | ⚠️ **`assertTrue(condition, message)`** |
| `assertFalse(message, condition)` | ⚠️ **`assertFalse(condition, message)`** |
| `kotlinx.coroutines.runBlocking` | `kotlinx.coroutines.test.runTest`（commonMain 无 runBlocking） |
| `@Rule MainDispatcherRule` | 不可用；改用 `runTest` + `StandardTestDispatcher` |

> ⚠️ **带 message 的断言是最容易静默出错的地方**：JUnit4 把 message 放第一个参数，kotlin.test 放最后一个。
> 由于 `assertEquals` 的前两参是泛型，**写反了往往仍能编译通过**，只是断言对象变成了 message 字符串，测试失去意义。
> `StageDataSourceSwitchTest` 有 4 处这种三参数调用，迁移时逐个核对。

> ⚠️ `runBlocking` 在 Kotlin/Native 与 JS 上不可用。`VerifyEmailCodeUseCaseTest` 等 3 个测试用了它，必须改 `runTest`。

P0 需迁移的测试（对应已下沉的代码）：

```
StageDataSourceSwitchTest / ApiMappingTest / AppEnvironmentTest
KtorApiCallerTest / YoofiHttpClientTest / RemoteAuthRepositoryTest
domain/auth/*Test（6 个）
```

**✅ 验证**：

```bash
./gradlew :shared:jvmTest
```

通过标准：全绿，且用例数与迁移前一致（不允许静默丢测试）。

---

### 8.4 Step 4 —— Android 侧接入（保证零业务改动）

1. `settings.gradle.kts` 加模式开关与 `includeBuild`（§2.1）
2. `gradle/libs.versions.toml` 登记 `yoofi-shared` 坐标，`app/build.gradle.kts` 加 `implementation(libs.yoofi.shared)`（§2.1）
3. **删除**已下沉的源文件（P0 全量为 69 个：基础设施 17 + domain 49 + data 的 3 个 Demo Repository）
4. 改各 Hilt Module 的 import 指向 `ai.yoofi.shared.*`

> ⚠️ **不要新建 `di/SharedModule.kt`，也不要让 Hilt 去取 `SharedContainer`。**
> 这是 §5.2 实测修正过的结论：现有 `NetworkModule` / `ConfigModule` 本来就是手写 `@Provides` 风格，
> 下沉后方法体一个字都不用改，只改 import。`SharedContainer` 只服务于 iOS / Web，
> Android 侧套它会造出「Hilt 提供 Container、Container 再吐组件」的双层间接。
>
> **唯一必须改的一处**：`NetworkModule` 的
> `@Binds abstract fun bindApiCaller(impl: KtorApiCaller): ApiCaller`
> 改为 `@Provides fun provideApiCaller(json: Json): ApiCaller = KtorApiCaller(json)`——
> `@Binds` 要求实现类自身可被 Hilt 构造，而 `KtorApiCaller` 下沉后已无 `@Inject` 构造函数。

**包名变更的过渡技巧** —— 避免一次性改 92 个 UI 文件的 import：

```kotlin
// app/src/main/kotlin/ai/yoofi/app/core/common/Aliases.kt
// 迁移期过渡：让既有 import ai.yoofi.app.core.common.Outcome 继续可编译。
// P1 全量替换 import 后删除本文件，不要长期保留。
package ai.yoofi.app.core.common

typealias Outcome<T> = ai.yoofi.shared.common.Outcome<T>
typealias AppError = ai.yoofi.shared.common.AppError
```

> ⚠️ typealias **不能**用于 `sealed interface` 的子类匹配（`when (x) { is Outcome.Ok -> }` 仍需真实包名可见）。实测若 `when` 分支报错，改为直接全量替换 import（IDE 的 Replace in Path 一次完成），不要硬撑 typealias。

**✅ 验证**：

```bash
cd /Users/jackxu/Desktop/androidspace/yoofi-android
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

通过标准：APK 产出成功，剩余单测（UI / ViewModel 层）全绿。

---

### 8.5 Step 5 —— iOS Framework 产出（验证「真能跨端」）

```bash
cd /Users/jackxu/Desktop/androidspace/yoofi-shared
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

通过标准：`shared/build/bin/iosSimulatorArm64/debugFramework/YoofiShared.framework` 存在。

> **这一步不能跳过**。它是「commonMain 真的没有 JVM 依赖」的唯一硬证明——很多 `java.*` 泄漏只在 Native 链接期才暴露。

---

### 8.6 阶段划分总览

| 阶段 | 内容 | 验证 |
|---|---|---|
| **P0**（本方案主体） | 基础设施 17 文件 + domain 49 文件 + data 3 文件；zero expect/actual；Android 接入；iOS framework 产出 | `:shared:jvmTest` + `assembleDebug` + `linkDebugFramework*` |
| **P1** | ViewModel / Composable 中的业务规则下沉（§6.6）；接入 Room 2.7+ 时同步下沉存储；接入 Coil 时下沉 `ImageUrlBuilder` | 新增 commonTest 覆盖 |
| **P2** | 融云 IM 桥接（§7.3）；埋点 schema；`domain/avatar` 重估；**wasmJs spike**（先解决 §3.3 的版本冲突） | 各自专项 |

---

## 九、待确认事项（不要自行假设）

1. ~~**画布业务归属（阻塞 A1）**~~ —— **已确认（2026-09-01）：画布是创作端独有，在 Web/Vue；Android 只做消费端（玩故事）。**

   由此推出三条结论，已并入正文：
   - v1 整套「下沉画布业务」的规划**对 Android 不适用**，P0 的真实工作面就是 §6 那 69 个文件。
   - shared 的近期价值是 **Android + iOS 共享消费端逻辑**，不是三端共享创作逻辑。
   - Android 与 Web 的共享面很窄（网络 DTO + 未来的条件求值），**wasmJs 的收益随之下降**，进一步支持把它推到 P2。
   
   遗留问题：消费端将来玩故事时也要做条件求值（读取创作端产出的条件配置）。**那份 `ConditionEvaluator` 一旦开发，就是三端共享收益最高的一块**，届时应直接写在 commonMain，不要先在 Android 写一遍再搬。

2. **iOS 项目是否已存在**？本次只扫到 `yoofi-android` 与 `yoofi-shared`。若 iOS 尚未启动，§8.5 只能验证 framework 产出，无法验证真实调用体验，shared 的 API 设计存在「只面向 Android 思维」的风险（v1 §5 风险 4 说得对）。

3. **融云是否已确定选型并拿到 App Key**？若尚在选型期，§7.3 只做消息 payload 契约即可，桥接推迟。

4. **Web 端 Vue 是否真的要消费 Kotlin 产物**？若 Web 团队更倾向「shared 只导出 TypeScript 类型定义 + OpenAPI」，则不需要 wasmJs，成本大幅下降。这是本方案最大的分叉点，建议三端一起拍板。

---

## 十、CR 强制检查清单

- [ ] `commonMain` 无任何 `android.*` / `androidx.*` / `java.*` / `javax.*` / `dagger.*` 导入
- [ ] `commonMain` 无 `Dispatchers.IO`（Native 不存在）、无 `runBlocking`（Native/JS 不可用）
- [ ] `commonTest` 只用 `kotlin.test`，不出现 `org.junit`
- [ ] shared 内无任何 DI 框架注解；单例由宿主决定
- [ ] shared 不读 `BuildConfig` / `Info.plist` / 环境变量，配置一律由宿主传参
- [ ] `shared/domain` 不依赖 `shared/data`；业务包之间不横向依赖
- [ ] 每次 PR 必须同时通过 `:shared:jvmTest` 与 `:shared:linkDebugFrameworkIosSimulatorArm64`
- [ ] 两仓库的 `kotlin` / `ktor` / `serialization` 版本严格一致
- [ ] 图片：shared 只算 URL 与 cacheKey，不出现 Bitmap / UIImage / 解码 / 缓存
- [ ] IM：shared 只有消息 payload 契约与纯算法，SDK 调用在各端
- [ ] UI 组件、Compose 契约、平台资源 ID 一律不进 shared

## 十一、架构反模式（禁止出现）

1. ❌ 在 commonMain 引入 Coil / Kingfisher / 融云 SDK
2. ❌ commonMain 出现 Context / UIKit / DOM
3. ❌ 把 Compose 的 `@Composable` 契约（如 `ImageCropHost`）下沉到 plain KMP commonMain
4. ❌ 为「尚不存在的能力」预先创建 expect 抽象（v1 的 IM / 埋点 / i18n 接口即属此类）
5. ❌ Android 单方面定义 shared API，iOS / Web 被动适配
6. ❌ 用 `com.android.library` 配 KMP（AGP 9 下必然失败）
7. ❌ 引入已 EOL 的 Realm-KMP
8. ❌ 一次性大重构：不按 §8 的 Step 逐步验证，攒到最后一起编译

---

## 十二、双仓协作与 shared 版本管理

> 本章回答两个高频问题：**shared 改了代码，Android 怎么用到？** **版本怎么管？**
> 全章代码均已在本仓实测跑通，不是纸面方案。

### 12.1 TL;DR

| 问题 | 答案 |
|---|---|
| shared 改完，Android 要做什么才能用上新代码？ | **什么都不用做。** 默认源码模式下 `./gradlew :app:assembleDebug` 会连带编译 shared，改动立即生效，不发版、不改版本号 |
| 那版本号还有什么用？ | 只在**二进制模式**（出包 / 回溯历史版本）下参与解析。日常开发它是摆设 |
| 怎么保证不把 iOS / Web 编译搞崩？ | 两道机器守卫：`checkSharedVersionAlignment`（版本对齐）+ `checkLegacyAbi`（API 契约，§12.5） |
| 什么时候该合成一个仓？ | 见 §12.8 的触发条件，现在**不合** |
