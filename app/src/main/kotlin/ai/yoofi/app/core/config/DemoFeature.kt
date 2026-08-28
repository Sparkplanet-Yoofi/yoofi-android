package ai.yoofi.app.core.config

/**
 * 数据源开关注册表：每个支持 Demo / 真实双实现的契约在这里登记一项。
 *
 * **这是全项目唯一需要手改的开关位置。** 开发阶段想让某个接口改走真实服务端，
 * 把它的 [demoInDevelopment] 改成 false 即可，不必动 Hilt 模块。
 *
 * @property demoInDevelopment 开发阶段是否使用 Demo 实现；
 *   提测 / 上线阶段该值被忽略，见 [BuildStage.allowsDemoDataSource]
 * @property realImplemented 是否已经接好真实接口。为 false 时，提测 / 上线构建会在
 *   启动自检里直接抛错（见 [DataSourceSwitch.requireReleaseReady]），
 *   而不是把假数据静默带上线
 */
enum class DemoFeature(
    val demoInDevelopment: Boolean,
    val realImplemented: Boolean,
) {
    /** 登录：`AuthRemoteDataSource`，真实实现走 `/customer/auth/login`。 */
    Auth(demoInDevelopment = true, realImplemented = true),

    /** 搜索：`SearchRepository`，服务端接口未定，暂时只有 Demo。 */
    Search(demoInDevelopment = true, realImplemented = false),

    /** 聊天室：`ChatRoomRepository`，服务端接口未定，暂时只有 Demo。 */
    ChatRoom(demoInDevelopment = true, realImplemented = false),

    /** 游戏详情：`GameDetailRepository`，服务端接口未定，暂时只有 Demo。 */
    GameDetail(demoInDevelopment = true, realImplemented = false),
}
