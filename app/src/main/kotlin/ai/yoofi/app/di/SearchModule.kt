package ai.yoofi.app.di

import ai.yoofi.shared.config.DataSourceSwitch
import ai.yoofi.shared.config.DemoFeature
import ai.yoofi.app.data.search.DemoSearchRepository
import ai.yoofi.app.domain.search.SearchRepository
import ai.yoofi.app.domain.search.SearchStoriesUseCase
import ai.yoofi.app.domain.search.SuggestStoriesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchProvideModule {
    /** 搜索服务端接口未定，暂时只有 Demo；接好后改用 [select] 双参数重载。 */
    @Provides
    @Singleton
    fun provideSearchRepository(
        switch: DataSourceSwitch,
        demo: Provider<DemoSearchRepository>,
    ): SearchRepository = switch.selectDemoOnly(DemoFeature.Search, demo)

    @Provides
    fun provideSuggestStoriesUseCase(
        repository: SearchRepository,
    ): SuggestStoriesUseCase = SuggestStoriesUseCase(repository)

    @Provides
    fun provideSearchStoriesUseCase(
        repository: SearchRepository,
    ): SearchStoriesUseCase = SearchStoriesUseCase(repository)
}
