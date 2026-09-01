# Yoofi Android · 项目记忆

> 本文件是项目的长期记忆，保证每个人（和每次 AI 会话）进场时的「世界观」一致。
> **维护纪律**：每个需求收尾花 5 分钟更新踩坑与技术债。写得及时比写得全更重要。
> 过期的禁区清单比没有清单更危险——发现失效条目请立即删除或更正。
>
> 最近更新：2026-09-01

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
- **含输入框的全屏页键盘覆盖、不顶布局**：根用 `ImeOverlayBox`（`ai.yoofi.app.ui.ime`），
  非输入点击用 `clickableDismissingIme`。禁止改 Manifest `windowSoftInputMode` 做单页差异。
  登录注册三页均已覆盖。聊天室是书面例外（Figma `1826:10061`）：内容层用
  `imeAvoidingPadding()` 抬到键盘上沿，背景仍铺满。
  所有输入框获焦光标在末尾，只走 `rememberCursorAtEndField`，禁止直接喂 `String`。
- **网络环境写在 `AppEnvironment`，构建只注入原始参数**：映射唯一定义处是 `AppEnvironment.forStage`
  （只有 production 阶段连线上）；覆盖 `-Pyoofi.api.env=production|staging`。禁止业务代码写死 Base URL。
- **三档只有一套词汇：development / staging / production**，`BuildStage`、`AppEnvironment`、
  Gradle 参数、文档全部对齐，**禁止再引入 qa / release 等同义词**；旧取值传了会直接构建失败。
- **构建阶段与数据源开关**（详见 `.ai/apicall.md`）：`BuildStage` 三态
  由 `BuildConfig.BUILD_STAGE` 注入，`-Pyoofi.stage=` 覆盖；
  每个接口的 Demo / 真实切换登记在 `DemoFeature`，这是**唯一手改点**。
  提测 / 上线包若还有接口只有 Demo 实现，`YoofiApplication` 启动自检直接抛错。
  **禁止再写需要手工翻转的 mock 常量**——`TempMockLoginSuccess` 就是这么差点把 mock 登录带上线的。
- **资料页按观众拆屏，不共用 `isSelf` / `isEmpty`**：词汇是 `ProfileAudience`
  （`Mine.Populated` 主态 / `Mine.Vacant` 空态 / `Guest` 客态）。
  主态与空态都在 Tab「我的」（设置 + Preview + Get VIP），空态对齐 Figma `982:13113`
  （Nickname、占位头像、0 Create/Favorite/Follow、无 ID 编号）。
  客态仍是栈上 overlay（返回 + 三点拉黑）。公共壳在 `ui.profile`。
  「Preview Profile」独立 `ui.profile.preview` + `PreviewProfileViewModel`，不要塞进 MeViewModel，
  也不要用 `isSelf` 复用客态。对齐 Figma `2252:19446`：返回 + 居中标题，资料卡无铅笔 /
  复制 / VIP，头像角标是别人看到的关注 +（不可点）。Tab 是 Played / Lorebook / Props，
  不是「我的」的 Lorebook / Creations。Played 接口未定，四张 Demo 卡走
  `GetPreviewPlayedWorksUseCase`。
  `ResolveMineProfilePresenceUseCase` 看会话：未登录、`profileCompleted == false`、昵称为空 → 空态。
  创建资料成功走 `MarkProfileCompletedUseCase`，Skip 不标完善。
  空态铅笔进 `ProfileEditorEntry.Create`，主态铅笔进 `Edit`。
  「我的」主空差异用 `MineProfileStrategy`（`VacantMineStrategy` / `PopulatedMineStrategy`）
  一次解析，禁止在 `MeLayout` 里反复 `if (vacant)`。加新的「我的」态只加策略实现。
  禁止往资料卡塞布尔开关；客态仍是独立 Screen，不要用策略把主客缠回去。
  拉黑接口未定，确认后只发 `GuestSnackbar.BlockUser`，接接口时加 UseCase，不要把 HTTP 写进 Screen。
- **详情三点菜单与举报表单拆开**：详情 VM 只管家 `overlay` / 重置 Snackbar / `reportOpen`。
  举报表单独立 `ui.gamedetail.report` + `ReportContentViewModel` + `SubmitReportUseCase`。
  重置接口未定，确认后只发 `GameDetailSnackbar.StartNewStory`（文案 `Start a new story`）。
  举报三步 Reason → Details → Done，对齐 Figma `2252:18328` / `2252:18374` / `2252:18531`。
  菜单底栏对齐 `2252:18526`。截图走系统 Photo Picker，最多 3 张，不申请 `READ_MEDIA_*`。
  接接口只改 UseCase，不要把举报表单塞进 `GameDetailViewModel`。
- **资料创建与编辑是两条入口，不是 `isEdit` 开关**：`ProfileEditorEntry.Create`（注册后完善，Skip + Continue，走现有创建 mock）与
  `ProfileEditorEntry.Edit`（Me 铅笔进 `1943:14006`，返回 + Save，走 `UpdateProfileUseCase`）。
  表单壳共用 `ProfileSetupScreen`；提交契约分开，接接口时创建另加 CompleteProfileUseCase，不要把编辑塞进创建的 delay mock。
- **设置与注销不要塞进 MeViewModel**：设置独立 `ui.settings` + `SettingsViewModel`（列表 + 登出弹层）。
  注销独立 `ui.settings.delete` + `DeleteAccountViewModel`。
  点选路径：警告 `2252:16542` → 有密 `2252:16583` → 无密 `2252:16629` → 成功 `2252:16685`。
  Figma 把有密 / 无密画成并列稿；当前按点选路径两页都走到，接账号类型接口后只留一页。
  登出对齐 `2252:17923` 弹窗，不是警告页。`2252:16542` 标题是 Delete Account。
  登出 / 删号成功必须 `YoofiRoot.landing = null`，只 `UserSessionStore.clear()` 会停在 Tab。
  Language 等行先画出入口，子页未定不要顺手做。
  关联账号独立 `ui.settings.linked` + `LinkedAccountsViewModel`，不要塞进设置管家。
  双账号对齐 `2252:17106`，点已绑定行出 `2252:17312`；只剩一条对齐 `2252:17155`，
  点 Unlink 出 `2252:17254`。解绑接口未定，确认后只发 Snackbar，接接口改 `UnlinkAccountUseCase`。
  黑名单独立 `ui.settings.blocked` + `BlockedUsersViewModel`，不要塞进设置管家。
  列表对齐 `2252:17322`，点 Unblock 出 `2252:17548`，成功 Toast 对齐 `2252:17465`（文案 User Unblocked）。
  解禁接口未定，确认后只发 Toast 并就地删行，接接口改 `UnblockUserUseCase`。
  反馈独立 `ui.settings.feedback` + `FeedbackViewModel`，不要塞进设置管家。
  空表对齐 `2252:17719`，选类型+描述后对齐 `2252:17770`，成功对齐 `2252:17821`。
  提交接口未定，确认后只切成功页，接接口改 `SubmitFeedbackUseCase`。
  接注销接口只改 `DeleteAccountUseCase` / `SendDeleteCodeUseCase`。
- **登录会话在内存**：`UserSessionStore` / `GetCurrentUserUseCase`；导航看 `isNewUser`，
  `profileCompleted` 只存会话。Token 尚未落盘。
- **网络隔离**：Repository 只依赖纯 Kotlin `RemoteDataSource` + `Outcome`；
  全项目只允许 `KtorApiCaller` catch HTTP。
- **HTTP 客户端 = Ktor Client 3.5.2 + OkHttp 引擎**（2026-08-28 从 Retrofit 迁完，详见 `.ai/apicall.md`）：
  提前于「拆 `core:network` 再换」的原定节奏执行，理由是当时只有 4 个文件 import Retrofit/OkHttp、
  只有 1 个真实接口，迁移面最小且只会越拖越大；`ApiCaller` 接缝生效，Repository / UseCase / UI 零改动。
  引擎保留 OkHttp，`OkHttp.create { addInterceptor(...) }` 这条口子还在，弱网能力（超时分级 /
  退避重试 / CDN 降级 / 请求签名）没有丢。`createYoofiHttpClient` 是纯函数，可整体进 commonMain。
  **禁止再引入 Retrofit / OkHttp 直接依赖。**
- **第三方必须接口适配**：业务只依赖我方契约（如 `ImageCropHostRenderer`），
  第三方 import 只出现在 `data.*` 适配类。换库不改 UI / UseCase。
  图片裁剪用 CanHub（ArthurHub 已停更，且其 README 要求存储权限，与 Play 相册策略冲突）。

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

9. **Agent 在对话里跑 `final_review_gate.py` 会明显变慢且无效**：Cursor Agent 的
   stdin 非 TTY，脚本立刻 EOF 退出，不能做人工复核，却多一轮工具调用。
   闸门只允许人在终端跑。另：一次读完 `.ai/`、默认拉 Figma 全量、默认开
   kotlin-reviewer subagent、同一任务多次 `assembleDebug`，都会把几行 UI 改成分钟级。

10. **Figma 390 画板的 350 宽不要写死成 `350.dp`**：World / Stories 行锁 350
    后，宽屏只剩左侧对齐、右侧大块空白。内容区用 `fillMaxWidth` + 左右 20，
    三列用 `weight(1f)`；封面流焦点中心用 `maxWidth / 2`，不要用 175.dp。
    `Modifier.padding` 没有 `(horizontal, top, bottom)` 重载，要拆成两次
    `padding` 或写齐 `start/top/end/bottom`。

11. **换头像相册不要申请 READ_MEDIA / 存储权限**：用 `PickVisualMedia` 系统选择器
    即可读一张图；广域存储权限会被 Play 拒。    拍照才申请 `CAMERA`。相册选中或拍照成功后立刻
    拷到 `cacheDir/avatars` 再进裁剪页（契约 `ImageCropHost`，适配 CanHub），
    确认后压到 ≤5MB 再写入 `filesDir/avatars`。不要长期持有 Picker 的临时 URI。
    不要接 ArthurHub 原库的 `CropImageActivity`（停更 + 存储权限）。
    裁剪比例与体积上限走 `ImageProcessConfig`，不要把 1:1 / 5MB 写死在 UI 里。
    落盘路径固定为 `filesDir/avatars/profile.jpg`：`produceState` 只认路径字符串，
    覆盖写后必须递增 `avatarRevision` 才能重新解码；`asImageBitmap()` 放主线程。

12. **聊天室从 World Played 封面进全屏 overlay**：`MainTabShell` 打开 `ChatRoomScreen`
    并藏底栏，不要把聊天室嵌进 World 页。宽屏仍 `fillMaxWidth` + 左右 20，
    不要把 Figma 350 锁死。Recap 尚无独立画板，只留芯片不造假页。
    聊天室 Items 芯片跳独立 `ui.gamedetail.item` + `GameItemViewModel`，对齐
    `2304:24267`。点任意卡都走多人底栏 `2304:24509`（Select Target → 选人），
    不要给普通道具另开 Use Item 入口；`GameItemKind.General` 只控制徽章显隐。
    选人确认后经 `SendItemMessage` 回写聊天室，不要把道具 UI 塞进 ChatRoomViewModel。
    Hilt VM 跟 Activity 同作用域，Items 再进必须 `ShowList`，否则会停在选人页。
    底栏不要盖一层全屏点击取消，否则换卡会先 Dismiss 再才能选中。
    预览走 `ItemPreviewHost`（当前 2D，白蒙层 `2464:27742`），日后换 3D 只改适配。
    选人对齐 `2304:24760` / `2304:24649` / `2304:24871`。列表走
    `GetGameItemsUseCase`，目标走 `GetGameItemTargetsUseCase`，接接口只改 UseCase。
    聊天室 Cast 芯片跳独立 `ui.gamedetail.cast` + `GameCastViewModel`，对齐
    `2304:23753`，不要塞进 ChatRoomViewModel，也不要拆掉翻牌 overlay 代码。
    人物接口未定，四张金卡 + 两个空槽走 `GetGameCastCardsUseCase`。
    点金卡进独立 `ui.gamedetail.character` + `GameCastCharacterViewModel`，对齐
    `2409:27067`。空槽不跳。详情走 `GetGameCastCharacterUseCase`，接接口只改 UseCase。
    关闭回人物页；Continue Game 关 Cast 回聊天室。稿里隐藏的 Stories / Memories /
    Gallery 与右侧更多菜单不要画出来。
    聊天室 Map 芯片跳独立 `ui.gamedetail.map` + `GameMapViewModel`，对齐
    `2453:27236`（列表 `2453:27362`、切换 Dialog `2304:24255`、Go 气泡
    `2453:27489`）。用户给的 `2252:18315` / `2453:27240` / `2453:27366`
    是超大底图矩形，不是手机画板。底图按稿面 `916dp` 铺满视口再拖，禁止用 PNG
    像素当布局（xxhdpi 会缩成一角）。Location 用 0..1 图幅坐标打点。
    点标签或红钉弹出 Go 预览；点 Go 经 `SendMapMessage` 回写聊天室，底图走
    `sceneKey`（Demo `demo-scene` 用 `img_home_hero`，和默认房间底区分，点 Go 能看出换底）。
    不要把地图 UI 塞进 ChatRoomViewModel。Hilt VM 再进必须 `ShowMap`。
    地图列表走 `GetGameMapsUseCase`，接接口只改 UseCase。

13. **选 @ / 灵感回填后再 `requestFocus`，光标会钉在开头**：
    `BasicTextField` 在 `onFocusChanged` 之后把选区打回 0，再经 `onValueChange`
    写回。此时文案已是 `@tomy `，`SideEffect { if (value.text != text) }`
    以为不用同步。对齐只放 `rememberCursorAtEndField`：组合期改选区 +
    获焦后等一帧盖回末尾。不要在业务页自己 `TextRange(length)`。

14. **选 @ 后人名跑到 `@` 前面（`tomy @`）**：两处叠加。
    `pickMention` 不能只 `removePrefix("@")` 再前置插入，要替换文末未完成的
    `@` / `@xxx`（`applyPickedMention`）。回填后 IME 会把触发键 `@` 接到末尾
    并写回 ViewModel；握手期必须丢掉与外部文案不一致的 `onValueChange`。

15. **底栏毛玻璃 Local 包错层会整段失效**：`ContentBackdropProvider` 必须同时包
    「记录 Tab 页的 Recorder」和「读层的 YoofiBottomBar」。只包 Recorder 时
    底栏 `LocalContentBackdrop` 恒为 null，只剩半透明紫、没有 blur。
    不要改共享页层的 `renderEffect`（设完立刻清空，GPU 合成时已经没了），
    裁切后再 `Modifier.blur`，和详情卡同一套路。CSS `blur(10px)` 是标准差，
    Compose 半径按 `sigma ≈ 0.577 * radius + 0.5` 折成约 16dp。

16. **资料空态不要写成 `MeScreen(isEmpty)`**：Figma `982:13113` 的计数是
    Create / Favorite / Follow，主态仍是 Following / Follower。差异放
    `ProfileIdentity.stats`，观众放 `ProfileAudience` / `MineProfilePresence`。
    Demo 登录 `profileCompleted == false`，Skip 后「我的」就是空态；
    创建成功必须 `MarkProfileCompletedUseCase`，否则 Tab 复用 VM 会一直空。
    `MeLayout` 里把 presence 收成 `vacant` 再问五次，等于又把 `isEmpty` 请回来；
    可变部分只走 `MineProfileStrategy`。GoF 双类 + Hilt Map 不必上，运行时
    `when (presence)` 选 object 即可。客态不要塞进同一策略。

17. **详情举报不要写进 GameDetailViewModel**：三点菜单、重置 Snackbar、是否打开举报页
    归详情管家；原因 / 500 字详情 / 最多 3 张截图走独立 Screen。Hilt VM 跟详情同作用域，
    `bind` 每次进入必须重开原因页，否则提交成功后再点开会停在 Done。

18. **登出只清会话不够**：`YoofiRoot` 用 `landing != null` 决定是否停在 Tab。
    不把 `landing` 置空，Tab 壳还在，用户会看到已退出的「我的」。
    删号成功倒计时结束走 `onAccountDeleted`；警告页 Cancel 只关 overlay，不要误调登出。

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
- **`AppEnvironment` 两个 Base URL 仍是 `http://` 占位域名**（`your-api-server.com`）。
  API 28 起默认禁止明文流量，换成真实域名后第一个请求就会被系统拦掉。
  拿到域名时优先换 `https://`；服务端只有 http 才退而配 network security config，
  且只对测试域名开、生产域名不许开。源码内已留 TODO。
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
