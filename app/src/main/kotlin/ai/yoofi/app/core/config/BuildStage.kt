package ai.yoofi.app.core.config

/**
 * 构建阶段。决定「能否使用 Demo 数据源」。
 *
 * 全项目对这三档**只有一套叫法：development / staging / production**。
 * buildType、Gradle 参数、[ai.yoofi.app.core.network.AppEnvironment] 与所有文档一律用这三个词，
 * 不要再引入 qa / release 这类同义词——一个概念两个名字是上一版最大的理解成本来源。
 *
 * 与 `AppEnvironment`（决定 Base URL）仍是两个正交维度，不要合并：
 * 调试线上问题时会出现「开发阶段 + 生产环境」这种组合。
 * 各阶段默认连哪个环境由 `AppEnvironment.forStage` 单独定义，这里不重复一份，免得两处走样。
 *
 * | 阶段 | 数据源 | buildType | 触发方式 |
 * |---|---|---|---|
 * | [Development] | 允许 Demo | debug | 默认 |
 * | [Staging] | 强制真实接口 | release | `-Pyoofi.stage=staging` |
 * | [Production] | 强制真实接口 | release | 默认 |
 *
 * 阶段值由构建注入（`BuildConfig.BUILD_STAGE`），**不要在源码里手改**。
 * 手改常量正是 `TempMockLoginSuccess` 留下的教训：忘记改回去，mock 就跟着上线了。
 * 未知取值一律落到 [Production]，保证失手时偏向安全的一侧。
 *
 * @property allowsDemoDataSource 该阶段是否允许任何接口走 Demo 实现
 */
enum class BuildStage(val allowsDemoDataSource: Boolean) {
    /** 开发：接口文档没落地也能先跑通 UI。 */
    Development(allowsDemoDataSource = true),

    /** 提测：连测试服务端，Demo 全部关闭。 */
    Staging(allowsDemoDataSource = false),

    /** 上线：连生产服务端，Demo 全部关闭。 */
    Production(allowsDemoDataSource = false),
    ;

    companion object {
        fun fromName(name: String): BuildStage = when (name) {
            "development" -> Development
            "staging" -> Staging
            else -> Production
        }
    }
}
