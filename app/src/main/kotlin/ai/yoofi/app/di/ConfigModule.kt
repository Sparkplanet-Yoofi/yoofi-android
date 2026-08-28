package ai.yoofi.app.di

import ai.yoofi.app.BuildConfig
import ai.yoofi.app.core.config.BuildStage
import ai.yoofi.app.core.config.DataSourceSwitch
import ai.yoofi.app.core.config.StageDataSourceSwitch
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 构建阶段与数据源开关的唯一注入点。
 *
 * [StageDataSourceSwitch] 刻意不带 Hilt 注解，保持纯 Kotlin，
 * 拆 KMP 或换 DI 框架时该类零改动。
 */
@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @Provides
    @Singleton
    fun provideBuildStage(): BuildStage = BuildStage.fromName(BuildConfig.BUILD_STAGE)

    @Provides
    @Singleton
    fun provideDataSourceSwitch(stage: BuildStage): DataSourceSwitch =
        StageDataSourceSwitch(stage)
}
