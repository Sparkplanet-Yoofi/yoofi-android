package ai.yoofi.app.di

import ai.yoofi.app.core.config.DataSourceSwitch
import ai.yoofi.app.core.config.DemoFeature
import ai.yoofi.app.data.chat.DemoChatRoomRepository
import ai.yoofi.app.domain.chat.AdvanceChatStoryUseCase
import ai.yoofi.app.domain.chat.ChatRoomRepository
import ai.yoofi.app.domain.chat.ObserveChatRoomUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatProvideModule {
    /** 聊天室服务端接口未定，暂时只有 Demo；接好后改用 [select] 双参数重载。 */
    @Provides
    @Singleton
    fun provideChatRoomRepository(
        switch: DataSourceSwitch,
        demo: Provider<DemoChatRoomRepository>,
    ): ChatRoomRepository = switch.selectDemoOnly(DemoFeature.ChatRoom, demo)

    @Provides
    fun provideObserveChatRoomUseCase(
        repository: ChatRoomRepository,
    ): ObserveChatRoomUseCase = ObserveChatRoomUseCase(repository)

    @Provides
    fun provideAdvanceChatStoryUseCase(
        repository: ChatRoomRepository,
    ): AdvanceChatStoryUseCase = AdvanceChatStoryUseCase(repository)
}
