package ai.yoofi.app.core.network

import ai.yoofi.app.core.config.BuildStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 阶段 → 环境的映射此前写在 `build.gradle.kts` 里，无法单测。
 * 挪进 Kotlin 后由这组用例钉住。
 */
class AppEnvironmentTest {

    @Test
    fun `名称解析`() {
        assertEquals(AppEnvironment.Production, AppEnvironment.fromName("production"))
        assertEquals(AppEnvironment.Staging, AppEnvironment.fromName("staging"))
    }

    @Test
    fun `不认识的名称返回 null，由调用方决定回退`() {
        assertNull(AppEnvironment.fromName(""))
        assertNull(AppEnvironment.fromName("prod"))
    }

    @Test
    fun `Base URL 以斜杠结尾，否则相对路径会吞掉最后一段`() {
        AppEnvironment.entries.forEach { env ->
            assert(env.baseUrl.endsWith("/")) { "${env.name} 的 baseUrl 必须以 / 结尾" }
        }
    }

    @Test
    fun `只有上线阶段默认连生产`() {
        assertEquals(AppEnvironment.Production, AppEnvironment.forStage(BuildStage.Production))
        assertEquals(AppEnvironment.Staging, AppEnvironment.forStage(BuildStage.Development))
        assertEquals(AppEnvironment.Staging, AppEnvironment.forStage(BuildStage.Staging))
    }

    @Test
    fun `未指定覆盖时按阶段推导`() {
        BuildStage.entries.forEach { stage ->
            assertEquals(
                stage.name,
                AppEnvironment.forStage(stage),
                AppEnvironment.resolve(stage, override = ""),
            )
        }
    }

    @Test
    fun `显式覆盖优先于阶段默认`() {
        // 用 debug 包排查线上问题：开发阶段也能连生产
        assertEquals(
            AppEnvironment.Production,
            AppEnvironment.resolve(BuildStage.Development, override = "production"),
        )
        // 反向也成立：上线阶段的包可以指回测试服务端
        assertEquals(
            AppEnvironment.Staging,
            AppEnvironment.resolve(BuildStage.Production, override = "staging"),
        )
    }

    @Test
    fun `覆盖值不认识时回退到阶段默认，不会误连生产`() {
        assertEquals(
            AppEnvironment.Staging,
            AppEnvironment.resolve(BuildStage.Staging, override = "prod"),
        )
    }
}
