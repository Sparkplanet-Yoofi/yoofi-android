package ai.yoofi.app.core.config

/**
 * 决定某个契约取 Demo 还是真实实现。
 *
 * 纯 Kotlin，不感知 Hilt 与 HTTP 客户端，拆 KMP 时可整体复用。
 * DI 侧的接线写法见 `ai.yoofi.app.di.select`。
 */
interface DataSourceSwitch {
    /** 该能力当前是否走 Demo 实现。 */
    fun useDemo(feature: DemoFeature): Boolean

    /**
     * 启动自检。提测 / 上线阶段若还有能力没接真实服务端，直接抛错终止启动。
     *
     * 宁可在第一次打提测包时炸掉，也不能让假数据混进灰度。
     */
    fun requireReleaseReady()
}

/**
 * 按构建阶段决策的默认实现。
 *
 * 判定顺序刻意把 [DemoFeature.realImplemented] 放在最前：没有真实实现时只能回退 Demo，
 * 而这种状态能否出现在提测 / 上线包里，由 [requireReleaseReady] 单独把关。
 */
class StageDataSourceSwitch(
    private val stage: BuildStage,
) : DataSourceSwitch {

    override fun useDemo(feature: DemoFeature): Boolean = when {
        !feature.realImplemented -> true
        else -> stage.allowsDemoDataSource && feature.demoInDevelopment
    }

    override fun requireReleaseReady() {
        if (stage.allowsDemoDataSource) return
        val pending = DemoFeature.entries.filterNot { it.realImplemented }
        check(pending.isEmpty()) {
            "构建阶段 $stage 不允许 Demo 数据源，但以下能力尚未接入真实服务端：" +
                pending.joinToString { it.name } +
                "。请先补上真实实现并把 DemoFeature.realImplemented 改为 true。"
        }
    }
}
