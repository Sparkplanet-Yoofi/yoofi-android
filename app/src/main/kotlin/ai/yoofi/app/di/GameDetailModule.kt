package ai.yoofi.app.di

import ai.yoofi.app.core.config.DataSourceSwitch
import ai.yoofi.app.core.config.DemoFeature
import ai.yoofi.app.data.gamedetail.DemoGameDetailRepository
import ai.yoofi.app.domain.gamedetail.DeleteGameCommentUseCase
import ai.yoofi.app.domain.gamedetail.GameDetailRepository
import ai.yoofi.app.domain.gamedetail.GetGameCommentsUseCase
import ai.yoofi.app.domain.gamedetail.GetGameDetailUseCase
import ai.yoofi.app.domain.gamedetail.PostGameCommentUseCase
import ai.yoofi.app.domain.gamedetail.ToggleAuthorFollowUseCase
import ai.yoofi.app.domain.gamedetail.ToggleCommentLikeUseCase
import ai.yoofi.app.domain.gamedetail.ToggleGameSavedUseCase
import ai.yoofi.app.domain.report.SubmitReportUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameDetailProvideModule {
    /** 详情服务端接口未定，暂时只有 Demo；接好后改用 [select] 双参数重载。 */
    @Provides
    @Singleton
    fun provideGameDetailRepository(
        switch: DataSourceSwitch,
        demo: Provider<DemoGameDetailRepository>,
    ): GameDetailRepository = switch.selectDemoOnly(DemoFeature.GameDetail, demo)

    @Provides
    fun provideGetGameDetailUseCase(
        repository: GameDetailRepository,
    ): GetGameDetailUseCase = GetGameDetailUseCase(repository)

    @Provides
    fun provideGetGameCommentsUseCase(
        repository: GameDetailRepository,
    ): GetGameCommentsUseCase = GetGameCommentsUseCase(repository)

    @Provides
    fun providePostGameCommentUseCase(
        repository: GameDetailRepository,
    ): PostGameCommentUseCase = PostGameCommentUseCase(repository)

    @Provides
    fun provideDeleteGameCommentUseCase(
        repository: GameDetailRepository,
    ): DeleteGameCommentUseCase = DeleteGameCommentUseCase(repository)

    @Provides
    fun provideToggleCommentLikeUseCase(
        repository: GameDetailRepository,
    ): ToggleCommentLikeUseCase = ToggleCommentLikeUseCase(repository)

    @Provides
    fun provideToggleAuthorFollowUseCase(
        repository: GameDetailRepository,
    ): ToggleAuthorFollowUseCase = ToggleAuthorFollowUseCase(repository)

    @Provides
    fun provideToggleGameSavedUseCase(
        repository: GameDetailRepository,
    ): ToggleGameSavedUseCase = ToggleGameSavedUseCase(repository)

    @Provides
    fun provideSubmitReportUseCase(): SubmitReportUseCase = SubmitReportUseCase()
}
