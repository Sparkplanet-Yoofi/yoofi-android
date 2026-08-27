# Yoofi Android · 代码规范（给 AI 与人共同执行）

> 本文件是**可执行规范**，不是官方文档摘抄。写 Kotlin / Compose / Gradle 时按这里做。  
> 模块怎么拆、依赖怎么走，见 `.ai/architecure.md`。协作流程见 `.ai/harness.md`。禁区见 `PROJECT_MEMORY.md`。

## 规范来源与冲突时听谁的

| 优先级 | 来源 | 管什么 |
|---|---|---|
| 1 | **本文件 + `AGENTS.md` 红线** | Yoofi 项目取舍（分层、可见性、KMP 预留、中文注释） |
| 2 | [Google Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide) | 格式硬规则：UTF-8、4 空格、**行宽 100**、禁止通配 import |
| 3 | [Kotlin 官方编码约定](https://kotlinlang.org/docs/coding-conventions.html)（K2） | 命名、类内部顺序、修饰符顺序、惯用法、尾随逗号 |
| 4 | [Google / Kotlin KMP 工程实践](https://developer.android.com/kotlin/multiplatform) | `expect`/`actual`、源集后缀、入口模块与共享模块分离 |

官方两套指南冲突时：**格式听 Google（行宽 100、禁止 `import *`）**；语言惯用法听 Kotlin 官方。Yoofi 红线高于两者。

编译器按 **K2**（Kotlin 2.x，本工程 `kotlin` 版本见 `gradle/libs.versions.toml`）。不要写只在 K1 下成立的假设，不要引入依赖 KAPT 的旧插件。

IDE：Settings → Editor → Code Style → Kotlin → Set from → **Kotlin style guide**；并打开 Incorrect formatting 检查。格式化以 ktlint / 官方 style 为准，不要手调对齐空格。

---

## 一、AI 生成代码时必须先遵守的十条

1. 新文件只放 `src/main/kotlin/`（或测试的 `src/test/kotlin/`），**禁止**再开 `src/main/java/`。  
2. 标识符、包名、文件名用 **ASCII 英文**；注释、KDoc 用 **中文 UTF-8**。写完自检有无乱码。  
3. `domain` 包（含未来 `commonMain`）禁止 `import android.*`，禁止 `java.time` / `java.io`（用 `kotlinx-datetime` / `kotlinx-io`）。  
4. `feature:*:impl` 里的类默认 `internal`；只有 Route、跨业务接口放 `api` 且显式 `public`。  
5. ViewModel 只调 UseCase，禁止直接碰 Retrofit / Room DAO / Billing Client。  
6. 禁止 Koin、RxJava、Moshi、`GlobalScope`、通配 import、在 build 脚本硬编码版本号。  
7. 序列化只用 `kotlinx.serialization`；DI 只用 Hilt，且 Hilt 注解只出现在 `app` 与 `impl`。  
8. 优先扩展现有函数，禁止无关重构、禁止删除仍在用的业务代码。  
9. 行宽 100；缩进 4 空格；不用 Tab。  
10. 不确定就列疑问，不要猜完直接写。  
11. **第三方 SDK 必须经接口适配层**：契约在 `core.*`，`import com.xxx` 只允许出现在适配实现；UI / ViewModel / UseCase 禁止直连第三方。详见 `.cursor/rules/third-party-adapter.mdc`。

---

## 二、源码组织

### 2.1 包与目录

- 根包：`ai.yoofi.app`（仅 `app` 模块）。业务实现：`ai.yoofi.feature.<name>.…`，能力层：`ai.yoofi.core.<name>.…`。  
- 包名全小写、无下划线。多词直接拼接：`ai.yoofi.core.designsystem`。  
- 纯 Kotlin 模块按包建目录；**不要**再套一层无意义的 `util` 包。  
- 一个文件一个主类型时，文件名 = 类型名 + `.kt`。多类型同文件时，文件名用 PascalCase 描述内容，避免 `XxxUtil.kt`。

### 2.2 文件内部顺序（Google）

版权块（可选，用 `/* */`，不要用 KDoc 写版权）→ `@file:` 注解 → `package` → `import` → 顶层声明。各段之间空一行。

import：

- **禁止** `import foo.*`。  
- 不要换行；`package`/`import` 不受 100 列限制。  
- 按 IDE 默认分组即可，不要手排花式顺序。

### 2.3 类内部顺序（Kotlin 官方）

1. 属性与 `init`  
2. 次构造函数  
3. 方法（按阅读逻辑聚拢，**不要**按字母序或可见性排序）  
4. `companion object`  
5. 仅对外使用的嵌套类放最后  

重载必须挨在一起。实现接口时，成员顺序与接口声明一致。

### 2.4 修饰符顺序（Kotlin 官方，必须按此）

`public / protected / private / internal` → `expect / actual` → `final / open / abstract / sealed / const` → `external` → `override` → `lateinit` → `tailrec` → `vararg` → `suspend` → `inner` → `enum / annotation / fun` → `companion` → `inline / value` → `infix` → `operator` → `data`

注解在修饰符之前。非库模块省略多余的 `public`。

---

## 三、命名

| 种类 | 规则 | 例 |
|---|---|---|
| 包 | 小写，无 `_` | `ai.yoofi.feature.create.domain` |
| 类 / 接口 / object | PascalCase | `CreateViewModel`、`AuthStatusProvider` |
| 函数 / 属性 / 局部变量 | camelCase | `observeDrafts()`、`draftId` |
| `const` / 不可变顶层常量 | `SCREAMING_SNAKE` | `MAX_RETRY_COUNT` |
| 有行为的顶层/object 属性 | camelCase | `jsonParser` |
| 私有 backing | 前缀 `_` | `private val _uiState` / `val uiState` |
| 枚举 | `SCREAMING_SNAKE` 或 PascalCase，同一枚举内统一 | `LOADING` |
| `@Composable` 返回 `Unit` | 用 PascalCase（当作 UI 组件） | `CreateScreen`、`YoofiButton` |
| UseCase | `动词 + 名词 + UseCase` | `GenerateStoryUseCase` |
| Repository 接口 | `XxxRepository`；实现 `XxxRepositoryImpl` 且 `internal` | |
| 测试方法 | 可用反引号空格或下划线 | `` `sse 断线后应续传草稿` `` |

缩写：两字母全大写 `IOStream`；超过两字母只大写首字母 `HttpClient`、`XmlParser`（Google 表：`XmlHttpRequest`，不要 `XMLHTTPRequest`）。

禁止用空名字：`Manager`、`Helper`、`Wrapper`、`Util` 单独成类名。

---

## 四、格式（Google 硬约束 + Kotlin 惯用）

- UTF-8；缩进 4 空格；**列宽 100**（长 URL、`package`/`import` 除外）。  
- 左花括号跟在声明行末；`else` / `catch` / `finally` 与前一个 `}` 同一行。  
- 二元运算符两边有空格；`0..n` 的 `..` 两边无空格；`. ` / `?.` 无空格。  
- `if`/`when`/`for`/`while` 与 `(` 之间有空格；函数名与 `(` 之间无空格。  
- 类型 `: `：声明与类型之间无空格（`name: String`）；继承与委托的 `:` 前有空格。  
- 声明处参数列表**使用尾随逗号**（Kotlin 官方鼓励，diff 更干净）。调用处可选，多行参数建议也加。  
- 禁止为了对齐而填充空格。  
- 单表达式函数用表达式体：`fun foo() = 1`。  
- 省略 `: Unit`、分号、字符串模板里简单变量的 `${}`。  
- 链式调用换行时，`.` / `?.` 放在下一行，缩进 4 空格。  
- 单 lambda 尽量放在括号外：`list.filter { it.enabled }`。嵌套 lambda 必须显式参数名，不用外层的 `it`。

```kotlin
internal class CreateViewModel @Inject constructor(
    private val generateStory: GenerateStoryUseCase,
    private val observeDraft: ObserveDraftUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUiState())
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()
}
```

---

## 五、Kotlin 惯用法（K2 下按这个写）

- 默认 `val` + 不可变 `List`/`Set`/`Map`；工厂用 `listOf`/`persistentListOf`，不要无故 `arrayListOf`。  
- 可空类型用 `?.`、`?:`；禁止无注释的 `!!`。只有「语言/框架保证非空」才能 `!!`，并写中文原因。  
- 优先默认参数，少写重载。  
- 布尔或多个同类型参数的调用使用命名参数：`draw(width = 100, fill = true)`。  
- 二选一用 `if`；三分支及以上用 `when`。`when` 必须穷尽，优先 `sealed`。  
- 错误与 UI 状态用 `sealed class` / `sealed interface`，不要用魔法数字或可空「失败」。  
- 公开 API 需要文档时写 KDoc；参数用 `[name]` 链到正文，少用 `@param`/`@return`。override 且无新语义可不写 KDoc。  
- `api` / `core` 模块开启 `explicitApi()` 后，公开成员必须显式可见性与返回类型。

结果类型（放 `core:common`，业务不要各写一份）：

```kotlin
sealed interface Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>
    data class Err(val error: AppError) : Outcome<Nothing>
}
```

不要把 Retrofit / `IOException` 漏到 UI。Data 层映射成 Domain 能懂的 `AppError`。
统一入口：`ApiCaller.fetch` → `Outcome`（`ai.yoofi.app.core.common`）。
Repository 只消费 `Outcome`，禁止自己 catch HTTP。见
`.cursor/rules/remote-datasource.mdc`。

---

## 六、分层与 MVI（Yoofi 专用）

`feature:x:impl` 固定分包：`ui` / `domain` / `data` / `di`。

| 层 | 可以依赖 | 禁止 |
|---|---|---|
| `ui` | UseCase、UiState、Compose、ViewModel | Retrofit、DAO、SDK 实现类 |
| `domain` | 纯 Kotlin、Repository **接口**、`core:model`/`core:common` | `android.*`、Hilt 注解、DTO、OkHttp |
| `data` | Domain 接口、RemoteDataSource、Retrofit 适配器 | Compose、ViewModel；**禁止 Repository catch HttpException** |
| `di` | Hilt 绑定 | 业务规则 |

MVI 五件套命名与类型：

```kotlin
sealed interface CreateIntent {
    data object Refresh : CreateIntent
    data class SubmitPrompt(val text: String) : CreateIntent
}

data class CreateUiState(
    val prompt: String = "",
    val streamingText: String = "",
    val isGenerating: Boolean = false,
)

sealed interface CreateSideEffect {
    data class ShowMessage(val message: String) : CreateSideEffect
}
```

- `UiState` 用不可变 `data class`；流用 `StateFlow`。  
- `stateIn(scope, SharingStarted.WhileSubscribed(5_000), initial)` 作为 Flow → StateFlow 标准写法。  
- 一次性事件（Toast、导航）走 `Channel` / `SharedFlow` 的 SideEffect，不要塞进 UiState 当「消费后还在」的字段。  
- UseCase：一个类一件事，`suspend operator fun invoke(...)` 或 `operator fun invoke(...): Flow<T>`。  
- AI 流式文本在 UI 侧 **16～32ms 节流合并** 再写 State，禁止每个 token 触发重组。

Compose：

- 可组合函数第一参之后接受 `modifier: Modifier = Modifier`。  
- 列表必须稳定 `key`。  
- 禁止在 `@Composable` 里直接调挂起函数（用 `LaunchedEffect` / ViewModel）。  
- 禁止 Composable 持有 `Activity` Context；需要 Context 用 `LocalContext` 做短生命周期读取。  
- `impl` 内 Screen 为 `internal`。
- **二级 Tab / 嵌套页必须无限循环左右滑**：用 `ai.yoofi.app.ui.pager` 的
  `loopingPageCount` / `loopingStartPage` / `realPageIndex` / `animateToRealPage`，
  不要用 `when` 切页替代可滑动 Pager。划过最左再到最后一页，划过最右再到第一页。
  内层竖滑列表不得吞掉外层横滑。详见 `.cursor/rules/nested-loop-pager.mdc`。
- **含输入框的全屏页默认键盘覆盖**：根用 `ImeOverlayBox`，非输入点击用
  `clickableDismissingIme`，禁止 `imePadding()` / 按 IME 改按钮坐标把布局顶起。
  API 在 `ai.yoofi.app.ui.ime`。仅当产品书面要求「按钮贴键盘」才用 `imePadding`，
  且不要叠 Overlay。登录注册三页（邮箱 / 验证码 / 资料填写）均已覆盖。
  详见 `.cursor/rules/ime-overlay.mdc`。
- **网络**：OkHttp + Retrofit + kotlinx.serialization；Base URL 只来自
  `AppEnvironment`。新接口必须 `RemoteDataSource` + `ApiCaller.fetch`，
  全项目只允许 `RetrofitApiCaller` catch HTTP。登录成功把 User 写入
  `UserSessionStore`，读取走 `GetCurrentUserUseCase`。
  详见 `.cursor/rules/remote-datasource.mdc`、`.cursor/rules/network-auth.mdc`。

---

## 七、协程与并发

- 只用结构化并发：`viewModelScope`、`lifecycleScope`、`coroutineScope` / `supervisorScope`。  
- **禁止 `GlobalScope`**。  
- 并行用 `async` + `await`，且必须包在 `coroutineScope` 里。  
- 主线程零 IO：磁盘/网络在 UseCase 或 Repository 内切 `Dispatchers.IO`（Dispatcher 从 `core:common` 注入，方便测试）。  
- 不要在 Composable 里 `runBlocking`。  
- 取消必须可传播：循环生成/SSE 读取要检查 `ensureActive()` 或使用可取消的 Flow。

---

## 八、KMP 预留（现在就按共享代码写）

当前还不是多模块 KMP 工程，但 **domain / `core:model` / `core:common` 从第一天按可共享代码写**，避免将来迁 iOS 时返工。依据：[KMP 推荐工程结构](https://kotlinlang.org/docs/multiplatform/multiplatform-project-recommended-structure.html)、[expect/actual](https://kotlinlang.org/docs/multiplatform/multiplatform-expect-actual.html)、AGP 9 要求应用入口与共享库分离。

现在就必须做到：

1. 共享逻辑不用 Android API、不用 `java.time`/`java.io`。  
2. Domain **构造注入**，零 Hilt 注解。  
3. 序列化 `kotlinx.serialization`；时间 `kotlinx-datetime`。  
4. 不要在「将来的 common」里写死 `android.util.Log`，用抽象 `Logger` 接口。

以后若拆 KMP，再遵守这些（现在不要提前建空 `iosMain`）：

- 应用入口留在 Android 应用模块（`:app` / 将来的 `androidApp`），**不要**把 `Application`/`Activity` 放进共享模块（AGP 9 硬约束）。  
- `expect`/`actual` 必须同包同名；`actual` 放对应平台源集。  
- 平台源集里、含顶层声明的文件加后缀，避免 JVM facade 重名：`Platform.android.kt`、`Platform.ios.kt`；`commonMain` 用 `Platform.kt` 不加后缀。见 [Kotlin 源文件命名](https://kotlinlang.org/docs/coding-conventions.html#source-file-names)。  
- 能用接口 + 在入口组装实现，就少用 `expect/actual`（官方也推荐控制面用 DI/组装，而不是到处 expect）。

---

## 九、注释与文档

- 解释「为什么」用中文行内注释；「是什么」靠命名。  
- `//` 后面有一个空格。  
- 禁止把整段官方文档或变更日志贴进代码。  
- 不要提交带乱码的中文。  
- `api` 模块公开接口写简短 KDoc（中文），说明调用方何时用、不要用。  
- 版权头若加，用 `/* */`，不要用 `/** */`（Google）。

```kotlin
/**
 * 创作前校验登录态。调用方只依赖 api，不要依赖 auth impl。
 */
interface AuthStatusProvider {
    suspend fun isSignedIn(): Boolean
}
```

---

## 十、测试

- 单测放 `src/test/kotlin/`，包名与被测类一致。  
- 方法名可用反引号中文或英文短语，说清场景与预期。  
- Domain / UseCase 必须能在 JVM 跑，禁止依赖 Android 仪器测试才能验证业务规则。  
- 协程测试用 `runTest`，注入 `TestDispatcher`，禁止实等 `delay`。  
- 架构断言（Konsist）跟业务单测分开，失败视为规范违规，不是「小警告」。

---

## 十一、Gradle / 资源

- 版本只写在 `gradle/libs.versions.toml`。  
- 模块间依赖默认 `implementation`，只有类型要泄漏给下游才用 `api`。  
- 资源名：小写 + 下划线 `bg_create_header`。  
- 用户可见文案走 `string.xml`（后续多语言）；不要在 Composable 里写死德/法/西文案。日志和注释可以中文。

---

## 十二、禁止清单（违反即返工）

- `feature:impl` → 另一个 `feature:impl`  
- `core` → 任何 `feature`  
- ViewModel 持有 `*RepositoryImpl` 或 Retrofit 接口  
- Moshi / Gson / Koin / RxJava  
- `SharedPreferences` 存 Token（用加密 DataStore）  
- 冷启动强弹通知权限  
- 相册用存储权限代替 Photo Picker  
- GDPR 未同意就初始化 Sentry / 埋点  
- 客户端信任 IAP 本地状态发货  
- 为了「好看」一次性改无关文件  

---

## 十三、提交前 30 秒自检（AI 必须输出）

生成或改完代码后，在回复里列出：

1. 变更文件清单  
2. 是否触及禁区（签名、lifecycle 版本、GDPR、Billing、`.gitignore` wrapper）  
3. 本文件第一、十二节有无违反  
4. 建议的验证：`./gradlew :app:assembleDebug` 和相关单测  

对照官方原文时打开：

- https://kotlinlang.org/docs/coding-conventions.html  
- https://developer.android.com/kotlin/style-guide  
- https://developer.android.com/kotlin/multiplatform  
