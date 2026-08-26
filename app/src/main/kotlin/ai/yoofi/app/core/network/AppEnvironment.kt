package ai.yoofi.app.core.network

/**
 * API 环境。URL 只写在这里，构建只注入环境名 [fromName]。
 *
 * - debug 默认 [Staging]
 * - release 默认 [Production]
 * - 覆盖：`./gradlew assembleDebug -Pyoofi.api.env=production`
 */
enum class AppEnvironment(val baseUrl: String) {
    Staging("http://test-cn.your-api-server.com/"),
    Production("http://cn.your-api-server.com/"),
    ;

    companion object {
        fun fromName(name: String): AppEnvironment =
            if (name == "production") Production else Staging
    }
}
