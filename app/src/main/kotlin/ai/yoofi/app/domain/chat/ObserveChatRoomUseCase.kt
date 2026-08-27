package ai.yoofi.app.domain.chat

/**
 * 读取当前聊天室快照。ViewModel 只依赖这一处，不直接碰 Repository 实现。
 */
class ObserveChatRoomUseCase(
    private val repository: ChatRoomRepository,
) {
    operator fun invoke(): ChatRoomContent = repository.current()
}
