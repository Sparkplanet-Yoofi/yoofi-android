# Yoofi Android · Agent 手册

UGC-AI 互动故事游戏客户端，Google Play 海外发行，严格 GDPR 合规。
包名 `ai.yoofi.app`；AGP 9.0.1 / Gradle 9.1.0 / Kotlin 2.0.21 / compileSdk 36 / minSdk 24。

> 本文件是**地图不是手册**。详细规范在下方引用的文档里，按需去读，不要一次性全读。

## 必读文档

| 文档 | 内容 | 何时读 |
|---|---|---|
| `PROJECT_MEMORY.md` | 架构决策、**禁区清单**、历史踩坑、技术债 | **每次动手前必读禁区清单** |
| `.ai/architecure.md` | 目标架构方案（模块结构第三章、架构守卫第四章） | 涉及模块划分、分层、技术选型时 |
| `.ai/codestyle.md` | Kotlin / Compose / KMP 预留代码规范 | **写或改 Kotlin 代码时必读**（含键盘覆盖 `ImeOverlayBox`） |
| `.ai/apicall.md` | 接口调用流程、Demo/真实数据源切换、构建阶段常量 | **新增或改接口、打提测/上线包时必读** |
| `.ai/harness.md` | AI 协作方法论与工作流 | 需要了解协作规范时 |
| `.cursor/rules/third-party-adapter.mdc` | 第三方必须经接口适配层 | 引入或调用第三方库时 |
| `.jack/context/current-task.md` | Jack 个人任务现场（不入库） | 仅本机新会话恢复上下文 |

## 工程现状（重要，勿臆测）

**当前是单模块脚手架**，尚未按目标架构拆分：

- 只有 `:app` 一个模块，源码在 `app/src/main/kotlin/`（**不是** `src/main/java/`，新文件请放 kotlin 目录）
- 依赖：Compose BOM + core-ktx + lifecycle + activity-compose + **Hilt** + **Ktor Client（OkHttp 引擎）** + **kotlinx.serialization**；图片裁剪 SDK 只允许出现在 `data.image.crop.canhub` 适配层
- 网络新接口走 `RemoteDataSource` + `ApiCaller`（见 `.cursor/rules/remote-datasource.mdc` 与 `.ai/apicall.md`）
- 数据源有 Demo / 真实两套，开关登记在 `core/config/DemoFeature.kt`；阶段由 `BuildConfig.BUILD_STAGE` 注入，**禁止手改 mock 常量**
- **尚未引入** Room、Navigation
- 目标架构（`core:*` + `feature:x:api/impl`）见 `.ai/architecure.md` 第三章，按其第十二章落地清单推进

引入新依赖前先在 `gradle/libs.versions.toml` 声明版本，禁止在 build 脚本里硬编码版本号。

## 目标模块结构（详见 architecure.md 第三章）

- `app`：组装层，导航编排 + Hilt 根，**不含业务代码**
- `core:*`：水平能力层（common / model / network / database / datastore / designsystem / navigation / analytics / testing）
- `feature:x:api`：业务契约（Route 定义、跨业务接口），极薄
- `feature:x:impl`：业务实现，内部分 ui / domain / data / di，**类默认 internal**
- `build-logic`：Convention Plugins

## 硬性红线（违反即返工）

1. `feature:*:impl` 之间禁止互相依赖，跨业务只能依赖对方的 `api` 模块
2. `core:*` 禁止依赖任何 `feature`
3. `domain` 包内禁止出现 `android.*` 导入（保持纯 Kotlin，为 KMP 留路）
4. ViewModel 只能调用 UseCase，禁止直接触碰 `HttpClient` / Room DAO
5. 禁止引入 Koin、RxJava、Moshi；DI 用 Hilt，异步用 Coroutine + Flow，序列化用 kotlinx.serialization
6. 禁止在 build 脚本里硬编码依赖版本，统一走版本目录
7. **第三方必须有接口适配层**：业务 / UI / UseCase 禁止直接 import 第三方包；只通过我方契约调用，换库只改适配实现

## 高风险区域（改动前必须先输出影响分析，不要直接改代码）

`app/build.gradle.kts` 的签名配置 / `.gitignore` 的 wrapper 例外 / 版本目录的 lifecycle 版本 /
UGC 草稿离线同步 / GDPR 采集开关 / Billing 凭证校验 / `build-logic` / `core:*`

完整清单与原因见 `PROJECT_MEMORY.md` 的「禁区」章节。

## 构建命令

```bash
./gradlew assembleDebug          # 调试构建
./gradlew testDebugUnitTest      # 单元测试
./gradlew assembleRelease        # 正式构建（需环境变量注入签名，否则产出未签名包）
```

注意：Android Studio 与命令行共用 `app/build` 目录和 Gradle daemon，同时构建会互相踩。

## 工作约定

1. 动手前先读 `PROJECT_MEMORY.md` 的禁区清单；命中禁区先输出影响范围分析，等人工确认
2. 大任务先拆成可编译、可验证的子任务，禁止一次性全项目重构
3. **优先扩展现有逻辑，不要删除既有业务代码**；修改函数前先理解原有实现
4. 改完输出：变更文件清单 + 风险点 + 建议的验证方式
5. 涉及用户数据、隐私、支付的改动，额外标注合规风险
6. 代码注释用中文，UTF-8 编码，注意检查中文乱码
7. 不确定的地方列出疑问，不要自行假设后直接实现
8. **禁止在对话里执行 `final_review_gate.py`**（Cursor Agent 的 stdin 立即 EOF，闸门无效且拖慢回合）
9. 按需读 `.ai/`，禁止一次读完全部文档；小改动不要拉 Figma 全量、不要默认开 subagent
