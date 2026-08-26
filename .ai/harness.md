# Yoofi Android · Harness Engineering 落地方案

> **概念澄清**：`Agent = 模型 + Harness`。模型负责思考生成，Harness 是包裹模型的整套工作环境——规则、记忆、工具、反馈闭环。
> Cursor 里的 `@Harness` / `/harness` 只是 Agent 执行入口，**不等于 Harness Engineering**。后者是一套工程方法论，处于 `Prompt Engineering → Context Engineering → Harness Engineering` 演化链的最新节点。
>
> **核心命题**：在模型的不确定性之上，构建工作流程的确定性。凡是你希望 AI「一定」做到的事，就不要靠提示词，要靠结构。

## 本次修订要点（相对上一版）

| # | 问题 | 修订 |
|---|---|---|
| 1 | 上一版基于 `.cursorrules` | **`.cursorrules` 已被 Cursor 软性废弃**（已从官方文档移除）。改为 `AGENTS.md`（跨工具公共层）+ `.cursor/rules/*.mdc`（Cursor 专属、可按 glob 挂载） |
| 2 | **完全没有记忆层** | 新增第四章。这是上一版最大的结构性缺口——换会话即失忆是 AI 协作翻车的首要原因 |
| 3 | 没有 Skill 沉淀机制 | 新增第五章，把高频重复 prompt 固化为可版本化的 Skill |
| 4 | 模块结构与 `architecure.md` 不一致 | 统一为 `core:*` + `feature:x:api/impl`（详见 `architecure.md` 第三章）。**两份文档描述冲突时，AI 会产出违反架构的代码** |
| 5 | 工具层与架构守卫脱节 | 与 `architecure.md` 第四章的 Konsist / module-graph-assert / dependency-analysis 打通，形成同一套确定性约束 |
| 6 | 项目名为 SparkPlanet | 统一为 Yoofi，包名 `ai.yoofi.app` |
| 7 | 无真实项目记忆 | 第 4.2 节沉淀本项目**已实际发生**的六条踩坑记录 |

---

## 一、三层架构与三支柱

### 1.1 三层架构

| 层级 | 内容 | 特征 | 本项目载体 |
|---|---|---|---|
| **Skills 层** | 教 Agent「怎么做」的可复用 Markdown | 轻量、可版本化、人类可读 | `.cursor/skills/*.md` |
| **Harness 层** | 调度逻辑，串联 Skills 与工具 | **极薄**，只做编排，不含业务逻辑 | `AGENTS.md` + `.cursor/rules/` |
| **工具层** | 确定性执行单元 | 输入输出明确、无歧义 | Gradle / Detekt / Konsist / CI |

设计原则：**Skills 层越丰富，Harness 层越薄；工具层越确定，Agent 犯错空间越小**。三层分离带来的关键收益是可独立升级——换模型不影响 Skills，换工具不影响 Harness。

### 1.2 三支柱

| 支柱 | 作用时机 | 本项目手段 |
|---|---|---|
| **前馈 Guide** | Agent 行动**前**预防 | `AGENTS.md`、`.cursor/rules/`、`.ai/architecure.md` |
| **后馈 Sensor** | Agent 行动**后**检测 | Detekt/Ktlint、Konsist、module-graph-assert、编译、模拟器验证 |
| **记忆 Memory** | 跨会话保持 | `PROJECT_MEMORY.md`、任务上下文文档、Git 历史 |

### 1.3 五条设计原则

1. **约束优先于指令**：能用 linter 机械验证的，绝不写成提示词。文档说「应该怎么做」，工具强制「必须怎么做」。
2. **记忆外化**：用文件系统而非上下文窗口维护长期状态。上下文窗口会滚动丢失，文件不会。
3. **渐进式披露**：`AGENTS.md` 保持在 **100 行左右**，本身是**地图不是手册**，指向更详细文档，Agent 按需去取。一次性塞满上下文会稀释注意力。
4. **60 秒反馈**：lint 与快速检查必须在 1 分钟内返回，否则 Agent 的自我修正循环无法闭合。
5. **危险操作硬拦截**：涉及删除、force push、密钥的操作在权限层拦截，不靠模型自觉。

---

## 二、文件布局总览

```
yoofi-android/
├── AGENTS.md                       # 【前馈·跨工具】项目地图，约100行，Cursor/Codex/Copilot 等 20+ 工具通用
├── PROJECT_MEMORY.md               # 【记忆·长期】架构决策、禁区、踩坑、技术债
├── .cursor/
│   ├── rules/                      # 【前馈·Cursor 专属】按 glob 精准挂载，每个文件单一职责
│   │   ├── 00-core.mdc             #   alwaysApply：技术栈与全局禁止事项（务必精简）
│   │   ├── module-boundary.mdc     #   glob: feature/**, core/**  模块依赖边界
│   │   ├── mvi-feature.mdc         #   glob: feature/**/ui/**     MVI 与 Compose 规范
│   │   ├── domain-purity.mdc       #   glob: **/domain/**         Domain 层纯净性
│   │   ├── gradle-build.mdc        #   glob: **/*.gradle.kts      构建脚本与版本目录
│   │   └── privacy-gdpr.mdc        #   description 触发：隐私与埋点
│   ├── skills/                     # 【Skill 层】高频操作标准化
│   │   ├── new-feature-module.md
│   │   ├── mvi-screen.md
│   │   ├── compose-review.md
│   │   ├── anr-analyzer.md
│   │   └── arch-guard-check.md
│   └── cli.json                    # 【工具层】权限白名单（Shell / Read / Write）
├── .ai/                            # 【团队执行版】定稿后的架构与协作规范，必须入库
│   ├── architecure.md              # 目标架构方案（AGENTS.md 指向它）
│   ├── harness.md                  # 本文档
│   └── context/                    # 【记忆·任务级】随任务生命周期滚动
│       ├── current-task.md         #   实现上下文（节点三；个人进度，不入库）
│       ├── requirement.md          #   需求理解（节点一）
│       └── tech-decision.md        #   技术方案与决策理由（节点二）
├── .jack/                          # 【Jack 个人工作区】草稿与临时配置，不入库
└── final_review_gate.py            # 人工复核闸门（见第八章）
```

**入库策略（已落地）**：架构方案、协作规范、项目长期记忆都是**团队知识资产，必须入库**——否则同事与 CI 拉不到 `AGENTS.md` 所引用的文档，AI 每次进场的上下文在不同机器上就不一致，「记忆外化」也就失去了意义。

| 路径 | 入库 | 原因 |
|---|---|---|
| `.ai/architecure.md`、`.ai/harness.md` | ✅ | 团队共享的架构与协作规范（执行版） |
| `.ai/context/requirement.md`、`tech-decision.md` | ✅ | 需求边界与决策理由，交接必需 |
| `.ai/context/current-task.md` | ❌ | 每次中断都改写，入库只产生 diff 噪音 |
| `.jack/` | ❌ | Jack 个人草稿区，未定稿方案不进入团队上下文 |
| `AGENTS.md`、`PROJECT_MEMORY.md`、`.cursor/` | ✅ | 根目录，团队共享 |

---

## 三、前馈层：项目地图与规则

### 3.1 `AGENTS.md`——地图而非手册

`AGENTS.md` 是 2026 年的跨工具开放标准，Cursor、OpenAI Codex、Copilot、Windsurf、Zed、Aider 等 20+ 工具都读取它，可移植性远高于任何工具专属格式。Cursor 还支持**子目录嵌套**，就近的文件优先级更高。

关键纪律：**控制在 100 行左右，只做索引**。

```markdown
# Yoofi Android · Agent 手册

UGC-AI 互动故事游戏客户端。Google Play 海外发行，严格 GDPR 合规。
包名 `ai.yoofi.app`；AGP 9.0.1 / Gradle 9.1.0 / compileSdk 36。

## 必读文档（按需取用，勿一次性全读）
- 架构方案：`.ai/architecure.md`（模块结构见第三章，架构守卫见第四章）
- 项目长期记忆：`PROJECT_MEMORY.md`（**动手前必读禁区清单**）
- 当前任务现场：`.ai/context/current-task.md`

## 模块结构（详见 architecure.md 第三章）
- `app`：组装层，导航编排 + Hilt 根，**不含业务代码**
- `core:*`：水平能力层（common/model/network/database/datastore/designsystem/navigation/analytics/testing）
- `feature:x:api`：业务契约（Route 定义、跨业务接口），极薄
- `feature:x:impl`：业务实现，内部分 ui/domain/data/di，**类默认 internal**
- `build-logic`：Convention Plugins

## 硬性红线（违反即返工）
1. `feature:*:impl` 之间禁止互相依赖，只能依赖对方的 `api`
2. `core:*` 禁止依赖任何 `feature`
3. `domain` 包内禁止出现 `android.*` 导入
4. ViewModel 只能调用 UseCase，禁止直接触碰 Retrofit / Room DAO
5. 禁止引入 Koin、RxJava、Moshi（序列化统一 kotlinx.serialization）

## 高风险区域（改动前必须先输出影响分析）
UGC 草稿离线同步 / GDPR 采集开关 / Billing 凭证校验 / 签名配置 / build-logic

## 工作约定
- 动手前先读 `PROJECT_MEMORY.md` 的禁区清单
- 大任务先拆子任务，禁止一次性全项目重构
- 改完输出变更文件清单 + 风险点
- 优先扩展现有逻辑，不要删除既有业务代码
```

### 3.2 `.cursor/rules/*.mdc`——四种激活模式的分配

`.mdc` 文件由 frontmatter 的三个字段组合出四种激活模式，**选错模式会让规则要么从不生效、要么持续白占 token**：

| 模式 | frontmatter | 触发时机 | 该放什么 |
|---|---|---|---|
| 始终应用 | `alwaysApply: true` | 每次对话注入 | 技术栈声明、全局禁止事项。**必须极精简** |
| 自动挂载 | `globs: ["feature/**"]` + `alwaysApply: false` | 编辑匹配文件时 | 分层规范、模块边界、框架约定。**最高效，优先用** |
| Agent 判断 | 仅 `description` | Agent 读描述后自行判断 | 低频专项规则（如隐私合规） |
| 手动引用 | 三者都不设 | 对话中 `@rule-name` | 一次性重构指南 |

示例——模块边界规则：

```markdown
---
description: "Yoofi 模块依赖边界与分层约束"
globs: ["feature/**/*.kt", "core/**/*.kt"]
alwaysApply: false
---
# 模块依赖边界

## 允许的依赖方向
- `feature:x:impl` → `feature:x:api`、`feature:y:api`、`core:*`
- `feature:x:api` → `core:model`、`core:navigation`
- `app` → 所有 `feature:*:impl`（仅为 Hilt 绑定与导航注册）

## 禁止
- `feature:*:impl` → `feature:*:impl`（跨业务只能通过 api 接口，见 @.ai/architecure.md 3.4）
- `core:*` → 任何 `feature`
- `domain` 包 → `android.*`

## 跨业务调用的正确做法
在对方 `api` 模块定义接口，由其 `impl` 用 Hilt 绑定，调用方只依赖接口。
参考实现见 @.ai/architecure.md 第 3.4 节。
```

### 3.3 规则编写纪律

这几条来自 Cursor 官方建议与社区实践，违反会让规则失效：

1. **每个文件单一职责**。一个规则同时管 MVI、网络、测试、样式——拆开。
2. **指令式规则控制在 100 行内**。官方上限是 500 行，但**超过约 100 行模型就倾向当文档看而非指令执行**。
3. **引用而非复制**。用 `@.ai/architecure.md` 指向真实文件，而不是把内容抄进规则——抄进去的内容会随代码演进而过期。
4. **不写废话墙**。"做一个高级工程师、写干净的代码"这类内容会被完全忽略。
5. **渐进添加**。不要预设一堆规则，等 Agent 反复犯同一个错时再针对性补一条。
6. **验证有效性**。写完用代表性 prompt 实测；没生效就**重写这条**，而不是再加一条。
7. **禁止混用格式**。`.cursorrules` 与 `.cursor/rules/` 不要同时填内容，混用行为未定义。

---

## 四、记忆层：让 AI 每次进场世界观一致【本次新增·最大缺口】

上一版方案的致命缺口在这里。典型翻车场景：三天的需求跨了三个会话，第二天 AI 给出的方案与第一天有出入，第三天不知道某个公共组件是禁区直接改了，回滚花掉一小时。这些看起来是"模型不够聪明"，实质是**每次进场都裸着进，没有任何上下文承接**。

### 4.1 四层记忆模型

| 层级 | 载体 | 生命周期 | 作用 |
|---|---|---|---|
| 工作记忆 | LLM 上下文窗口 | 当次对话 | 由模型自行管理，会滚动丢失 |
| 短期记忆 | `.jack/context/current-task.md` | 当前任务 | 跨会话恢复现场 |
| 长期记忆 | `PROJECT_MEMORY.md` | 项目全生命周期 | 保证世界观一致 |
| 事实记忆 | Git 历史 + CI 日志 | 永久 | 客观事实，不可篡改 |

### 4.2 `PROJECT_MEMORY.md`——项目长期认知

**下面全部是本项目已实际发生的事实**，不是示意性模板。这类记录的价值在于：AI 下次进场立刻知道哪些坑已经踩过、为什么这样设计。

```markdown
# Yoofi Android · 项目记忆

## 架构决策（含理由，勿轻易推翻）
- 序列化用 kotlinx.serialization 而非 Moshi：Moshi 不支持 KMP，会阻断未来 iOS 迁移。
  不是因为 Moshi 不好用，而是为保留迁移路径，现在换成本为零。
- feature 拆 api/impl 双模块：单模块无法实现"feature 间禁止依赖"，
  且改一个业务会导致全部业务重编译。样板代码的成本由 Convention Plugin 对冲。
- 大资源分发用 Play Asset Delivery 而非 Feature Delivery：
  后者要求 `dynamic-feature → app` 的反向依赖，会破坏架构依赖图。
- 客户端不跑大模型：包体积与算力约束，AI 推理全部在服务端。

## 禁区（改动前必须人工评估影响范围）
- `app/build.gradle.kts` 的 `signingConfigs` 与 `hasReleaseKeystore` 判断：
  改错会静默产出**未签名 APK**，且构建仍显示成功。
- `.gitignore` 中 `!gradle/wrapper/gradle-wrapper.jar` 这行例外：
  删掉它，`*.jar` 规则会吞掉 wrapper jar，Jenkins 上 `./gradlew` 直接无法启动。
- `gradle/libs.versions.toml` 的 `lifecycleRuntimeKtx`：锁定 2.9.4，见下方踩坑记录。
- `core:*` 任何模块：被全部业务依赖，改动影响面最大。
- `build-logic`：改一处影响所有模块的编译配置。
- GDPR 采集开关、Billing 凭证校验逻辑：合规与资金风险。

## 历史踩坑（真实记录，勿重犯）
1. **lifecycle 2.11.0 装不上**：它要求 compileSdk ≥ 37 且 AGP ≥ 9.1.0，
   本项目是 AGP 9.0.1 / compileSdk 36，`checkDebugAarMetadata` 直接失败。
   已锁 2.9.4。升级前先确认 AGP 与 compileSdk 是否同步抬升。
2. **`*.jar` 忽略了 gradle-wrapper.jar**：初始 .gitignore 用的是 Java 通用模板，
   导致构建入口不入库。已加白名单例外，顺序必须在 `*.jar` 之后。
3. **手动 `rm -rf app/build` 后 IDE 报错**：Run 时报
   `Error loading build artifacts from .../redirect.txt`。
   redirect.txt 是 AGP 定位 APK 的索引，产物被删则链路断裂。重新构建即恢复。
4. **SSH 多账号推送被拒**：`github.com` 默认走 `~/.ssh/id_ed25519`（个人账号 xuningjack，
   对组织仓库无写权限）。已配 `github-sparkplanet` 别名 + `IdentitiesOnly yes`
   指向公司密钥。缺少 IdentitiesOnly 会导致 agent 里的个人密钥抢先认证成功。
5. **Studio 与命令行同时构建会互相踩**：共用同一 `app/build` 目录与 Gradle daemon，
   会出现产物莫名消失、锁等待。要在命令行构建前先停下 IDE 侧操作。
6. **Gradle 视图看不到 task 列表不是故障**：Studio 默认开启
   "Do not build Gradle task list during Gradle sync"，属预期行为。

## 当前技术债（临时方案，勿扩散模仿）
- 工程仍是单模块 `app`，尚未按 architecure.md 第三章改造。
- activity-compose 1.8.0 / core-ktx 1.10.1 偏旧；Compose BOM 已是 2025.12.01。
- minSdk 24 待决策抬到 26~31。
- 尚未接入架构守卫三件套（Konsist / module-graph-assert / dependency-analysis）。
- configuration cache 已验证兼容但尚未在 gradle.properties 开启。
```

**维护纪律**：关键不是写得全，而是**写得及时**。每个需求做完花 5 分钟更新踩坑与技术债，长期复利极大。这份文件同时也是新人入职和需求交接的最佳材料。

### 4.3 任务级上下文：四个节点强制产出文档

每个节点的产出，就是下一个会话的**入口协议**：

**节点一·需求理解**（拆解后立即产出）——功能点拆解、边界确认（明确不做什么）、疑问列表。价值在于**进入实现之前**就暴露 AI 与你对需求理解的偏差。

**节点二·技术方案**（方案定稿后）——涉及文件清单（精确到路径）、改动范围、**关键决策及理由（含备选方案与未选原因）**。最后这项最容易被跳过，却是换会话后最难重建的信息——它让下一个会话知道"这样设计是故意的，不是没想到别的"。

**节点三·实现上下文**（每次中断前更新）：

```markdown
# 当前任务：feature:create 模块 MVI 骨架

## 已完成
- [x] feature:create:api 的 CreateRoute 定义
- [x] CreateUiState / CreateIntent 密封类

## 待完成
- [ ] CreateViewModel 接入 AiGenerateStoryUseCase
- [ ] SSE 流式渲染的节流合并（16~32ms）
- [ ] 单元测试补充

## 注意事项
- core:ai 的 SSE 客户端已封装，勿直接用 OkHttp
- 草稿必须先落 Room 再同步远端（离线优先，见 architecure.md 6.2）
- 本模块所有类必须 internal，仅 Route 在 api 模块公开
```

格式不重要，**下次贴进新会话能让 AI 三句话内恢复现场**才重要。

**节点四·收尾**（需求完成后）——改了什么（精确到模块）、绕开了什么及原因、遗留问题、后续入口。完成后把有价值的部分**回写进 `PROJECT_MEMORY.md`**。

### 4.4 多步任务的 checkpoint

跨天、跨模块的任务要设计断点，避免推倒重来：

1. 每完成一个**可编译、可验证**的最小单元就 `git commit`，commit message 带任务标识。
2. 同步更新 `current-task.md` 的已完成/待完成清单。
3. checkpoint 的判据是**工具可验证**（编译通过 + lint 通过 + 相关测试通过），不是"我觉得写完了"。
4. 恢复时的标准开场：贴 `current-task.md` + 让 Agent 先跑一次编译确认基线，再继续。

---

## 五、Skill 层：高频操作标准化【本次新增】

反复手写同一类 prompt，每次措辞不同，输出质量自然不稳定。把它们固化成 Skill 文件，用 `@skills/xxx.md` 调用。

### 5.1 Yoofi 专属 Skill 清单

| Skill | 用途 |
|---|---|
| `new-feature-module.md` | 生成 `feature:x:api` + `impl` 完整骨架，含 Convention Plugin 引用、Hilt Module、FeatureEntry 注册 |
| `mvi-screen.md` | 生成 Intent/UiState/SideEffect/ViewModel/Screen 五件套 |
| `compose-review.md` | Compose 专项 review：重组稳定性、状态提升、`derivedStateOf` 误用、列表 key |
| `anr-analyzer.md` | ANR/trace 日志分析流程 |
| `arch-guard-check.md` | 提交前自检模块依赖与分层是否越界 |
| `gdpr-check.md` | 涉及用户数据的改动做合规自检 |

### 5.2 Skill 文件模板

固定写清四件事：**触发时机、输入、期望输出、项目特有约束**。

```markdown
# Compose 代码 Review Skill

## 触发时机
用户说"review 这段 Compose"或提交含 `@Composable` 的改动时

## 检查维度（按优先级）
🔴 严重：重组导致的无限循环、Composable 中直接调用挂起逻辑、
        列表缺少稳定 key、在 Composable 中持有 Activity Context
🟡 一般：状态未提升导致无法测试、unstable 参数引发过度重组、
        remember 缺少 key 导致状态错乱
🔵 建议：命名、可组合函数粒度、Modifier 参数顺序

## Yoofi 项目特有约束
- ViewModel 只能调 UseCase，禁止直接访问 Retrofit/Room
- UiState 必须密封类；Flow→StateFlow 用 stateIn(WhileSubscribed(5000))
- AI 流式渲染必须节流合并（16~32ms），否则长文本生成掉帧
- impl 模块内的 Composable 必须 internal

## 输出格式
每个问题：严重程度 | 文件:行号 | 问题描述 | 修复建议（附代码）
```

### 5.3 沉淀路径

第一次临时写 prompt → 觉得好用 → 提炼成 Skill 文件 → 下次直接引用。累积到一定程度，Skill 库就是团队的 AI 使用规范。**Skill 层越厚，对提示词技巧的依赖越低。**

---

## 六、工具层：确定性约束

### 6.1 按反馈速度分三级

Agent 的自我修正循环能否闭合，取决于反馈够不够快：

| 级别 | 耗时 | 内容 | 触发 |
|---|---|---|---|
| L1 即时 | < 10s | Ktlint 格式、IDE 诊断 | 保存时 |
| L2 快速 | < 60s | Detekt、Konsist 架构测试、模块依赖图断言 | pre-commit / Agent 自查 |
| L3 完整 | 分钟级 | 编译、单元测试、`buildHealth`、组装 AAB | CI |

**L2 必须控制在 60 秒内**，这是 Agent 能否自我修正的分水岭。超过这个量级，Agent 会倾向于跳过验证直接交付。

### 6.2 与架构守卫打通

`architecure.md` 第四章那套守卫，在 Harness 语境下就是**后馈传感器**——它把"依赖规则"从提示词变成机械事实：

| 工具 | 拦截什么 | 对应红线 |
|---|---|---|
| `module-graph-assert` | 模块依赖图违规 | AGENTS.md 红线 1、2 |
| `Konsist` | domain 引入 `android.*`、ViewModel 持有 Repository 实现 | 红线 3、4 |
| `dependency-analysis` | 错误的 `api` 声明、未使用依赖 | 增量编译效率 |
| `Detekt` 自定义规则 | 禁用库的 import（Koin/RxJava/Moshi） | 红线 5 |

**这是本方案最重要的一条**：Agent 长会话必然遗忘约束，但它写出的违规代码会被工具当场拦下。约束的可靠性由工具保证，不由模型的记忆力保证。

### 6.3 危险操作硬拦截

实际配置见仓库 `.cursor/cli.json`（已落地）。有四个**极易写错**的语法点，写错的规则会静默失效：

1. **`Shell(x)` 只匹配命令行的第一个 token**（命令基名）。`Shell(git status*)` 永远匹配不上，因为第一个 token 是 `git`。要限定子命令必须用 `command:args` 语法：`Shell(git:status*)`。
2. **`Shell(*keystore*)` 无效**。它匹配的是命令名而非整行，拦不住 `keytool -keystore xxx`。保护密钥要用 `Read(**/*.jks)` / `Write(**/*.jks)` 这类文件级 token。
3. **`approvalMode` 不能放项目级配置**。官方限制：`.cursor/cli.json` 只允许配 `permissions`，其余设置必须放全局 `~/.cursor/cli-config.json`。
4. **`deny` 优先于 `allow`**，且 deny 是硬阻止而非弹窗询问——被 deny 的操作 Agent 完全做不了，需人工在终端执行。这正是设计意图。

除 `Shell` 外还有 `Read(path)`、`Write(path)`、`WebFetch(domain)`、`Mcp(server:tool)` 四类 token，善用它们比只堆 Shell 规则严密得多。

本项目的拦截取舍与理由：

| 拦截项 | 理由 |
|---|---|
| `Shell(rm)` / `Shell(rmdir)` | 本项目已发生过 `rm -rf app/build` 导致 IDE 构建链路断裂 |
| `Shell(git:reset*)` / `git:clean*` / `git:checkout*` | 会无声销毁未提交的工作 |
| `Shell(git:push*)` | 推送必须人工确认，尤其本仓库存在多 SSH 账号场景 |
| `Shell(keytool)` / `apksigner` / `jarsigner` | 避免密钥口令进入日志与上下文 |
| `Read/Write(**/*.jks)`、`local.properties` | 防止凭据被读入上下文或被覆盖 |
| `Write(.gitignore)` / `Write(gradle/wrapper/**)` | 保护 wrapper jar 白名单例外，见 `PROJECT_MEMORY.md` 禁区 |
| `Shell(mv)` | 可静默覆盖目标文件；文件移动改用编辑工具完成 |

### 6.4 Git 前置快照

**Agent 执行前必须 `git commit` 保存快照**。这是最后一道保险——改坏了直接 `git reset --hard HEAD` 回滚，成本几乎为零。注意这条命令自身在 deny 列表里，需人工执行，这正是设计意图。

---

## 七、Agent 使用边界

### 7.1 适合交给 Agent

1. 新建 `feature:x:api` + `impl` 完整骨架（配合 `new-feature-module.md` Skill）
2. UseCase → Repository → UI 整条链路的样板代码
3. Room/Retrofit 数据层改动 + 自动修复 Hilt/KSP 注解编译错误
4. 批量生成 Compose 组件与 MVI 五件套
5. 单元测试补齐、Konsist 架构测试编写
6. 依赖升级的机械改动（改完必须跑完整构建验证）

### 7.2 禁止全权交给 Agent

1. Domain 核心业务规则、UGC 草稿离线同步策略
2. GDPR 隐私、埋点采集、Billing 凭证校验
3. 签名配置与 CI 凭据相关改动
4. `build-logic` 与 `core:*` 的结构性改动
5. 疑难运行时 bug 排查（Agent 缺少运行时观测能力，容易编造根因）
6. 架构文档与技术方案定稿
7. 全项目大规模重构——必须人工拆解为可验证的子任务

### 7.3 模型选择

| 场景 | 建议 |
|---|---|
| 完整模块脚手架、跨文件重构 | 高能力模型 + 允许执行 `./gradlew` 自修复 |
| 中等改动、新增单个 UseCase/UI | 高能力模型，**禁止 shell** |
| 几行 UI 微调、命名调整 | 普通对话即可，别动用 Agent |

原则：**任务规模与 Agent 权限成正比**。小改动动用大权限，是收益最低、风险最高的组合。

---

## 八、标准工作流（Loop Engineering 闭环）

完整闭环：**目标 → 上下文 → 动作 → 观测 → 评估 → 修正 → 沉淀**。

```
1. 冷启动     git commit 快照；新会话贴 PROJECT_MEMORY.md 禁区清单
              + .ai/context/current-task.md 恢复现场
2. 目标对齐   需求拆解 → 产出/更新 requirement.md（节点一）
3. 方案确认   产出 tech-decision.md（节点二），含决策理由
4. 禁区检查   命中 PROJECT_MEMORY.md 禁区？→ 先输出影响分析，人工确认后再动手
5. 执行       调用对应 Skill 辅助编码；Agent 自跑 L1/L2 检查
6. 观测       git diff 人工审查，优先看模块依赖与分层越界
7. 评估       L3 完整验证：编译 + 单测 + buildHealth + 模拟器跑通业务流程
8. 复核闸门   **人**在本机终端运行 `python3 ./final_review_gate.py` 并逐条确认。
              **Agent 禁止在对话里执行该脚本**（Cursor 注入的 stdin 立即 EOF，
              闸门瞬间退出，只会多一轮工具调用，不能做人工复核）。
9. 沉淀       更新 current-task.md；完成则产出收尾文档并回写 PROJECT_MEMORY.md
10. 提交      走 MR Code Review；有问题 git reset 丢弃全部改动
```

第 8 步的 `final_review_gate.py` 是**给人用的**人工复核闸门，防止「编译通过就等于做对了」。
它必须跑在真实 TTY 里。Agent 回合里跑它既无法交互，也会明显拖慢任务。

---

## 九、团队硬性规则

1. Agent 生成的代码**禁止直接进 main**，必须走 MR Review。
2. Agent 执行前必须有 git 快照。
3. 禁止模糊宽泛的 prompt，必须明确限定改动文件与范围。
4. Code Review 第一优先级永远是**模块依赖与分层越界**，其次才是业务逻辑。
5. 高风险模块（草稿同步 / GDPR / Billing / 签名）**逐行人工复核**，Agent 输出仅作参考。
6. Agent 犯过的架构错误，当天补进 `.cursor/rules/` 或 `PROJECT_MEMORY.md`——**能用工具拦的优先做成工具规则**，写文档是次优选择。
7. 每个需求收尾更新 `PROJECT_MEMORY.md`，5 分钟，不可省略。

---

## 十、落地优先级

按投入产出比排序，前三项建议本周内完成：

| 优先级 | 动作 | 成本 | 收益 |
|---|---|---|---|
| ~~P0~~ ✅ | 建 `AGENTS.md`（70 行）+ `PROJECT_MEMORY.md`（八条真实踩坑） | 已完成 | 立即消除"每次重新交代背景"，AI 世界观一致 |
| ~~P0~~ ✅ | 建 `.cursor/cli.json` 权限白名单 | 已完成 | 杜绝破坏性操作 |
| ~~P0~~ ✅ | 团队执行版迁入 `.ai/`（`architecure.md` / `harness.md`），`.jack/` 仅作个人草稿 | 已完成 | 架构文档对团队与 CI 可见 |
| **P1** | 建 `.cursor/rules/` 四个核心规则文件 | 半天 | 分层与模块边界前馈约束 |
| **P1** | 接入架构守卫三件套 + pre-commit | 1 人日 | 把红线变成机械事实，替代 90% 的提示词约束 |
| **P2** | 沉淀前 3 个 Skill（new-feature-module / mvi-screen / compose-review） | 按需累积 | 高频操作输出稳定 |
| **P2** | 建立 `.ai/context/` 四节点文档习惯 | 每任务 10 分钟 | 跨会话零损耗承接 |

---

## 十一、认知边界：Harness 不能解决什么

必须清楚它的上限，否则会产生错误的安全感：

1. **只降低风险，不消除缺陷**。架构与业务决策权必须在人手里。
2. **工具层只能验证结构，无法验证语义**。编译通过、依赖合法、lint 干净，都不代表业务逻辑正确——Compose 重组异常、离线同步时序、GDPR 逻辑错误，全部需要人工与运行时验证。
3. **长会话必然遗忘约束**。这不是靠更长的规则文件能解决的，只能靠工具拦截 + 及时开新会话 + 上下文文档承接。
4. **Agent 缺少运行时观测能力**。它看不到真机行为，排查运行时问题时容易编造看似合理的根因。
5. **记忆文档会腐化**。`PROJECT_MEMORY.md` 不及时更新，会从资产变成误导源——过期的禁区清单比没有清单更危险。

> 一句话总结：**Harness Engineering 把 AI 从不稳定的执行者，变成受约束、可预测的工程组件。它扩大了你能安全交给 AI 的任务范围，但没有改变"人对结果负责"这件事。**
