# Yoofi Android · 项目记忆

> 本文件是项目的长期记忆，保证每个人（和每次 AI 会话）进场时的「世界观」一致。
> **维护纪律**：每个需求收尾花 5 分钟更新踩坑与技术债。写得及时比写得全更重要。
> 过期的禁区清单比没有清单更危险——发现失效条目请立即删除或更正。
>
> 最近更新：2026-08-22

---

## 零、目录约定（勿混用）

| 目录 | 定位 | 入库 |
|---|---|---|
| `.jack/` | **Jack 个人工作区**：临时配置、草稿、未定稿方案 | ❌ 不入库 |
| `.ai/` | **团队共享区**：定稿后的架构方案、协作规范、Skill | ✅ 入库 |
| `AGENTS.md`、`PROJECT_MEMORY.md`、`.cursor/` | 根目录团队资产 | ✅ 入库 |

流转规则：文档先在 `.jack/` 里迭代，**评审通过后迁入 `.ai/`** 转为团队执行版。
因此 `AGENTS.md` 中的文档引用应指向 `.ai/`，指向 `.jack/` 的引用在他人机器上必然断链。
例外：`.ai/context/current-task.md` 是个人当前进度，不入库。

---

## 一、架构决策（含理由，勿轻易推翻）

- **序列化选 kotlinx.serialization 而非 Moshi**：Moshi 不支持 KMP，会阻断未来 iOS 复用。
  不是因为 Moshi 不好用，而是为保留迁移路径——现在换成本为零，将来换成本极高。
- **feature 拆 api/impl 双模块**：单模块无法在工具层强制「feature 间禁止依赖」，
  且改一个业务会触发全部业务重编译。样板代码成本由 Convention Plugin 对冲。
- **大资源分发用 Play Asset Delivery 而非 Play Feature Delivery**：
  后者要求 `dynamic-feature → app` 的反向依赖，会破坏架构依赖图的单向性。
- **客户端不跑大模型**：包体积与端侧算力约束，AI 推理全部在服务端，客户端只做流式渲染。
- **DI 选 Hilt**：Google 官方支持、与 Jetpack 集成最好；代价是 KMP 场景需要替换，
  因此约定 domain 层不感知 DI 框架，仅靠构造注入。

---

## 二、禁区（改动前必须人工评估影响范围）

- **`app/build.gradle.kts` 的 `signingConfigs` 与 `hasReleaseKeystore` 判断**：
  改错会**静默产出未签名 APK**，而构建仍然显示成功。签名凭据全部来自环境变量
  （`YOOFI_KEYSTORE_PATH` / `_PASSWORD` / `YOOFI_KEY_ALIAS` / `YOOFI_KEY_PASSWORD`）。
- **`.gitignore` 第 57 行 `!gradle/wrapper/gradle-wrapper.jar`**：
  删掉这行例外，上方的 `*.jar` 规则会吞掉构建入口，Jenkins 上 `./gradlew` 直接无法启动。
  该例外必须位于 `*.jar` 之后，顺序颠倒则无效。
- **`gradle/libs.versions.toml` 的 `lifecycleRuntimeKtx`**：锁定 2.9.4，原因见踩坑记录 1。
- **`core:*` 任何模块**（模块化落地后）：被全部业务依赖，改动影响面最大。
- **`build-logic`**：改一处影响所有模块的编译配置。
- **GDPR 采集开关、埋点上报逻辑**：合规风险，改动需专项评审。
- **Google Play Billing 凭证校验**：资金风险，禁止 AI 全权改动。

---

## 三、历史踩坑（真实记录，勿重犯）

1. **lifecycle 2.11.0 装不上**：它要求 compileSdk ≥ 37 且 AGP ≥ 9.1.0，
   本项目是 AGP 9.0.1 / compileSdk 36，`checkDebugAarMetadata` 直接失败。
   已锁 2.9.4。**升级 AndroidX 库前先确认它对 AGP / compileSdk 的下限要求**，
   Jetpack 库现在普遍通过 AAR 元数据强校验，不再是运行时才报错。

2. **`*.jar` 规则忽略了 gradle-wrapper.jar**：初始 `.gitignore` 用的是 Java 通用模板，
   导致构建入口不入库，CI 上必然失败。已加白名单例外。
   教训：Android 项目不要直接套用 Java 模板的 `.gitignore`。

3. **AGP 9 改了 compileSdk 的写法**：现在是
   `compileSdk { version = release(36) }`，不是旧的 `compileSdk = 36`。
   按旧写法（含大多数模型的训练惯性）会配置失败。

4. **手动 `rm -rf app/build` 后 IDE 报错**：Run 时报
   `Error loading build artifacts from .../redirect.txt`。
   `redirect.txt` 是 AGP 定位 APK 的索引文件，产物被删则 IDE 安装链路断裂。
   重新执行 `assembleDebug` 即恢复。**不要手工删 build 目录，用 `./gradlew clean`**。

5. **SSH 多账号推送被拒**：`github.com` 默认走 `~/.ssh/id_ed25519`（个人账号 xuningjack，
   对组织仓库只有读权限），报 `Permission to Sparkplanet-Yoofi/... denied`。
   已配 `github-sparkplanet` 别名指向公司密钥，remote 为
   `git@github-sparkplanet:Sparkplanet-Yoofi/yoofi-android.git`。
   **`IdentitiesOnly yes` 必须加**，否则 ssh-agent 里的个人密钥会抢先认证成功。

6. **Studio 与命令行同时构建会互相踩**：共用同一 `app/build` 目录与 Gradle daemon，
   表现为产物莫名消失、锁等待、增量状态错乱。命令行构建前先停下 IDE 侧的构建操作。

7. **Gradle 视图看不到 task 列表不是故障**：Android Studio 默认开启
   "Do not build Gradle task list during Gradle sync"，为加快同步速度，属预期行为。

8. **源码目录曾出现 `java/` 与 `kotlin/` 双份并存**：同包同名类会引发重复类冲突。
   现已统一到 `app/src/main/kotlin/`，且 `build.gradle.kts` 未显式配置 `sourceSets`
   （依赖 Kotlin plugin 的默认识别）。**新文件一律放 `src/main/kotlin/`**。

---

## 四、当前技术债（临时方案，勿扩散模仿）

- **工程仍是单模块 `:app`**，尚未按 `.ai/architecure.md` 第三章拆分。
- **依赖版本偏旧**：activity-compose 1.8.0 / core-ktx 1.10.1，待成套评估。
  Compose BOM 已升到 2025.12.01（2025 年内最后稳定档）；Kotlin 已是 2.2.10。
  不要把 BOM 升到 2026.08.00，它要求 compileSdk 37 且 AGP ≥ 9.2.0。
- **minSdk 24 待决策**：是否抬到 26~31，取决于目标市场机型分布与 Compose 性能要求。
- **尚未接入架构守卫三件套**（Konsist / module-graph-assert / dependency-analysis），
  当前所有分层约束仅靠人工与文档，无工具强制。见 `.ai/architecure.md` 第四章。
- **`gradle.properties` 未开启并行与 configuration cache**：
  `org.gradle.parallel` 仍被注释，`org.gradle.caching` / configuration cache 未配置。
  `org.gradle.jvmargs` 仅 2048m，模块化后需要上调。
- **无 CI 配置**：Jenkins 流水线尚未建立，签名注入机制已就绪但未接线。
- **架构守卫缺位期间靠人工**：在 Konsist 等工具接入前，分层越界只能靠 Code Review 兜底，
  Review 时第一优先级必须是模块依赖与分层，而非业务逻辑。

---

## 五、需要人工评估的变更类型

命中以下任一项时，AI 应先输出影响范围分析，等人工确认后再动手：

1. 修改上述任何禁区文件
2. 新增或升级依赖（尤其 AndroidX / AGP / Kotlin）
3. 调整模块边界或依赖方向
4. 触碰用户数据采集、隐私开关、支付校验
5. 修改构建脚本、签名、CI 配置
6. 跨 3 个以上文件的重构
