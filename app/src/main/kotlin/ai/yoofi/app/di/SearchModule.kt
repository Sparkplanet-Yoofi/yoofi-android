package ai.yoofi.app.di

import ai.yoofi.app.data.search.DemoSearchRepository
import ai.yoofi.app.domain.search.SearchRepository
import ai.yoofi.app.domain.search.SearchStoriesUseCase
import ai.yoofi.app.domain.search.SuggestStoriesUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchBindModule {
    /** 换真实接口时只改这一行的实现类。 */
    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: DemoSearchRepository): SearchRepository
}

@Module
@InstallIn(SingletonComponent::class)
object SearchProvideModule {
    @Provides
    fun provideSuggestStoriesUseCase(
        repository: SearchRepository,
    ): SuggestStoriesUseCase = SuggestStoriesUseCase(repository)

    @Provides
    fun provideSearchStoriesUseCase(
        repository: SearchRepository,
    ): SearchStoriesUseCase = SearchStoriesUseCase(repository)
}
