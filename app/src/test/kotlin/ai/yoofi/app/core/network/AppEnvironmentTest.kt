package ai.yoofi.app.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class AppEnvironmentTest {

    @Test
    fun `production 名称映射到线上 URL`() {
        val env = AppEnvironment.fromName("production")
        assertEquals(AppEnvironment.Production, env)
        assertEquals("http://cn.your-api-server.com/", env.baseUrl)
    }

    @Test
    fun `其余名称默认测试环境`() {
        val env = AppEnvironment.fromName("staging")
        assertEquals(AppEnvironment.Staging, env)
        assertEquals("http://test-cn.your-api-server.com/", env.baseUrl)
    }
}
