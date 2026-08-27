package ai.yoofi.app.domain.chat

/**
 * 推进剧情，取回本轮新增的消息。ViewModel 只依赖这一处，不直接碰 Repository 实现。
 */
class AdvanceChatStoryUseCase(
    private val repository: ChatRoomRepository,
) {
    operator fun invoke(turn: Int): List<ChatItem> = repository.storyBeat(turn)
}
