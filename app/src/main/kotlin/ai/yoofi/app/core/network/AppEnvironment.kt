package ai.yoofi.app.core.network

import ai.yoofi.app.core.config.BuildStage

/**
 * API 环境，只决定 Base URL。URL 全项目只写在这里，禁止业务代码硬编码。
 *
 * 与 [BuildStage] 是两个正交维度：阶段管「能不能用 Demo 数据源」，环境管「连哪台服务器」。
 * 不合并是因为「开发阶段 + 生产环境」这种组合真实存在——排查线上问题时要用它。
 * 两者共用同一套词汇（development / staging / production），同名即同义。
 *
 * 正交不等于无关：没有显式指定时，环境由阶段推导，见 [forStage]。
 * **那条映射的唯一定义处就是 [forStage]**，构建脚本只透传原始输入，不要在别处再写一份。
 *
 * TODO 拿到真实域名时把两个 URL 换成 `https://`。
 *   当前是 `http://` 占位地址，Android 从 API 28 起默认禁止明文流量
 *   （`usesCleartextTraffic` 默认 false），真实域名一接上，第一个请求就会被系统拦掉，
 *   报 `java.io.IOException: Cleartext HTTP traffic to ... not permitted`。
 *   首选换 https；服务端确实只有 http 时才退而配 network security config 白名单，
 *   且只对测试域名开，生产域名不许开。
 */
enum class AppEnvironment(val baseUrl: String) {
    /** 测试服务端。[BuildStage.Development] 与 [BuildStage.Staging] 共用这一台。 */
    Staging("http://test-cn.your-api-server.com/"),

    /** 生产服务端。只有 [BuildStage.Production] 默认连这里。 */
    Production("http://cn.your-api-server.com/"),
    ;

    companion object {
        /**
         * 阶段对应的默认环境：只有 [BuildStage.Production] 连生产，其余一律测试服务端，
         * 失手时偏向「不污染线上数据」的一侧。
         *
         * 开发与提测共用测试服务端，所以不是严格 1:1；日后若拆出独立 dev 服务端，
         * 在这里加一个 `Development` 环境即可，调用方不用改。
         *
         * 刻意用穷举 `when` 而不是 `if`：新增阶段时编译器会在此报错，
         * 强制作者显式决定它连哪个环境，而不是静默落到某个默认值。
         */
        fun forStage(stage: BuildStage): AppEnvironment = when (stage) {
            BuildStage.Development, BuildStage.Staging -> Staging
            BuildStage.Production -> Production
        }

        /**
         * 最终环境 = 显式覆盖优先，否则按阶段推导。
         *
         * @param override 来自 `-Pyoofi.api.env`；为空表示未指定，回退到 [forStage]。
         *   取值合法性由构建脚本校验，这里只做解析
         */
        fun resolve(stage: BuildStage, override: String): AppEnvironment =
            fromName(override) ?: forStage(stage)

        /** 名称解析。不认识返回 null，回退策略交给 [resolve] 决定。 */
        fun fromName(name: String): AppEnvironment? = when (name) {
            "staging" -> Staging
            "production" -> Production
            else -> null
        }
    }
}
