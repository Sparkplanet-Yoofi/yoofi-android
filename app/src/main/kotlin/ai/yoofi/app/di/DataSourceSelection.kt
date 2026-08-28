package ai.yoofi.app.di

import ai.yoofi.app.core.config.DataSourceSwitch
import ai.yoofi.app.core.config.DemoFeature
import javax.inject.Provider

/**
 * Hilt `@Provides` 里挑实现的统一写法。
 *
 * 两侧都用 [Provider] 传入，未选中的那一侧不会被实例化——Demo 仓库里的假数据
 * 不会在真实构建中占内存，真实实现也不会在开发阶段白白建连接。
 *
 * ```kotlin
 * @Provides
 * @Singleton
 * fun provideAuthRemoteDataSource(
 *     switch: DataSourceSwitch,
 *     demo: Provider<DemoAuthRemoteDataSource>,
 *     real: Provider<RetrofitAuthRemoteDataSource>,
 * ): AuthRemoteDataSource = switch.select(DemoFeature.Auth, demo, real)
 * ```
 */
fun <T : Any> DataSourceSwitch.select(
    feature: DemoFeature,
    demo: Provider<out T>,
    real: Provider<out T>,
): T = if (useDemo(feature)) demo.get() else real.get()

/**
 * 尚未接入真实服务端的能力专用。
 *
 * 接口文档落地后，把这里换成双参数的 [select]，并把
 * [DemoFeature.realImplemented] 改成 true，改动就到此为止。
 */
fun <T : Any> DataSourceSwitch.selectDemoOnly(
    feature: DemoFeature,
    demo: Provider<out T>,
): T {
    check(useDemo(feature)) {
        "${feature.name} 尚未接入真实服务端，当前构建阶段不允许提供实现。"
    }
    return demo.get()
}
