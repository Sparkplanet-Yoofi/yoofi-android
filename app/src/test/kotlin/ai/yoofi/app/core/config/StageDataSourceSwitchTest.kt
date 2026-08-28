package ai.yoofi.app.core.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 断言的是「规则」而不是注册表当前的快照，
 * 这样 [DemoFeature] 逐个接上真实接口时用例不会失效。
 */
class StageDataSourceSwitchTest {

    @Test
    fun `阶段名解析，未知取值落到最保守的 Production`() {
        assertEquals(BuildStage.Development, BuildStage.fromName("development"))
        assertEquals(BuildStage.Staging, BuildStage.fromName("staging"))
        assertEquals(BuildStage.Production, BuildStage.fromName("production"))
        assertEquals(BuildStage.Production, BuildStage.fromName(""))
        assertEquals(BuildStage.Production, BuildStage.fromName("prod"))
        // 旧词汇已废弃，不再被识别，一律落到最保守的一侧
        assertEquals(BuildStage.Production, BuildStage.fromName("qa"))
        assertEquals(BuildStage.Production, BuildStage.fromName("release"))
    }

    @Test
    fun `开发阶段按每个能力自己的开关决定`() {
        val switch = StageDataSourceSwitch(BuildStage.Development)
        DemoFeature.entries.forEach { feature ->
            val expected = !feature.realImplemented || feature.demoInDevelopment
            assertEquals(feature.name, expected, switch.useDemo(feature))
        }
    }

    @Test
    fun `提测与上线阶段已接真实接口的能力一律不走 Demo`() {
        listOf(BuildStage.Staging, BuildStage.Production).forEach { stage ->
            val switch = StageDataSourceSwitch(stage)
            DemoFeature.entries
                .filter { it.realImplemented }
                .forEach { feature ->
                    assertFalse("$stage/${feature.name}", switch.useDemo(feature))
                }
        }
    }

    @Test
    fun `登录在提测阶段必须走真实接口`() {
        assertFalse(StageDataSourceSwitch(BuildStage.Staging).useDemo(DemoFeature.Auth))
    }

    @Test
    fun `开发阶段启动自检永远放行`() {
        StageDataSourceSwitch(BuildStage.Development).requireReleaseReady()
    }

    @Test
    fun `提测与上线阶段存在未接真实接口的能力时启动自检抛错`() {
        val pending = DemoFeature.entries.filterNot { it.realImplemented }
        listOf(BuildStage.Staging, BuildStage.Production).forEach { stage ->
            val switch = StageDataSourceSwitch(stage)
            val failed = runCatching { switch.requireReleaseReady() }.isFailure
            assertEquals("$stage", pending.isNotEmpty(), failed)
        }
    }

    @Test
    fun `没有真实实现的能力在任何阶段都只能取 Demo`() {
        BuildStage.entries.forEach { stage ->
            val switch = StageDataSourceSwitch(stage)
            DemoFeature.entries
                .filterNot { it.realImplemented }
                .forEach { feature ->
                    assertTrue("$stage/${feature.name}", switch.useDemo(feature))
                }
        }
    }
}
