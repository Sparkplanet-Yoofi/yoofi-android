package ai.yoofi.app.domain.gamedetail

import ai.yoofi.app.domain.chat.ObserveChatRoomUseCase

/**
 * 多人道具的可选角色。现阶段复用聊天室 Cast，接独立接口后只改这里。
 */
class GetGameItemTargetsUseCase(
    private val observeChatRoom: ObserveChatRoomUseCase,
) {
    operator fun invoke(): List<GameItemTarget> =
        observeChatRoom().cast.map { member ->
            GameItemTarget(
                id = member.id,
                displayName = member.displayName,
                identity = member.identity,
                avatarKey = member.avatarKey,
            )
        }
}
