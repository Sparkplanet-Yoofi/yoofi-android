package ai.yoofi.app.di

import ai.yoofi.app.data.chat.DemoChatRoomRepository
import ai.yoofi.app.domain.chat.AdvanceChatStoryUseCase
import ai.yoofi.app.domain.chat.ChatRoomRepository
import ai.yoofi.app.domain.chat.ObserveChatRoomUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatBindModule {
    @Binds
    @Singleton
    abstract fun bindChatRoomRepository(impl: DemoChatRoomRepository): ChatRoomRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ChatProvideModule {
    @Provides
    fun provideObserveChatRoomUseCase(
        repository: ChatRoomRepository,
    ): ObserveChatRoomUseCase = ObserveChatRoomUseCase(repository)

    @Provides
    fun provideAdvanceChatStoryUseCase(
        repository: ChatRoomRepository,
    ): AdvanceChatStoryUseCase = AdvanceChatStoryUseCase(repository)
}
