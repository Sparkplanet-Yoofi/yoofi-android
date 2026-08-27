package ai.yoofi.app.ui.chat

import ai.yoofi.app.domain.chat.AdvanceChatStoryUseCase
import ai.yoofi.app.domain.chat.ChatCastMember
import ai.yoofi.app.domain.chat.ChatItem
import ai.yoofi.app.domain.chat.ChatRoomContent
import ai.yoofi.app.domain.chat.ObserveChatRoomUseCase
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

internal enum class ChatRoomOverlay {
    None,
    Cast,
    Mention,
    Inspiration,
}

internal data class ChatRoomUiState(
    val chapterTitle: String = "",
    val chapterObjective: String = "",
    val items: List<ChatItem> = emptyList(),
    val cast: List<ChatCastMember> = emptyList(),
    val inspirations: List<String> = emptyList(),
    val draft: String = "",
    val overlay: ChatRoomOverlay = ChatRoomOverlay.None,
    val mentionPage: Int = 0,
    val volumeMuted: Boolean = false,
) {
    val mentionPageCount: Int
        get() = if (cast.isEmpty()) 0 else (cast.size + MentionPageSize - 1) / MentionPageSize

    val mentionPageMembers: List<ChatCastMember>
        get() {
            val start = mentionPage * MentionPageSize
            return cast.drop(start).take(MentionPageSize)
        }

    companion object
}

internal sealed interface ChatRoomIntent {
    data object OpenCast : ChatRoomIntent
    data object OpenMention : ChatRoomIntent
    data object OpenInspiration : ChatRoomIntent
    data object ContinueStory : ChatRoomIntent
    data object JumpToLatest : ChatRoomIntent
    data object DismissOverlay : ChatRoomIntent
    data class DraftChanged(val value: String) : ChatRoomIntent
    data class PickMention(val memberId: String) : ChatRoomIntent
    data class PickInspiration(val text: String) : ChatRoomIntent
    data object MentionPrevPage : ChatRoomIntent
    data object MentionNextPage : ChatRoomIntent
    data object ToggleVolume : ChatRoomIntent
    data class OpenSceneCharacter(val characterId: String) : ChatRoomIntent
}

internal sealed interface ChatRoomSideEffect {
    /**
     * [force] 为 true 时无条件滚到底（玩家主动发言或点箭头）；
     * 为 false 时只在用户已停在底部才跟随，避免打断回看历史，改由箭头提示新消息。
     */
    data class ScrollToBottom(val force: Boolean) : ChatRoomSideEffect
}

@HiltViewModel
internal class ChatRoomViewModel @Inject constructor(
    observeChatRoom: ObserveChatRoomUseCase,
    private val advanceChatStory: AdvanceChatStoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatRoomUiState.from(observeChatRoom()))
    val uiState: StateFlow<ChatRoomUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<ChatRoomSideEffect>(capacity = Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var playerSeq: Int = 0
    private var storyTurn: Int = 0

    fun onIntent(intent: ChatRoomIntent) {
        when (intent) {
            ChatRoomIntent.DismissOverlay -> {
                _uiState.update { it.copy(overlay = ChatRoomOverlay.None) }
            }
            ChatRoomIntent.OpenCast -> toggle(ChatRoomOverlay.Cast)
            ChatRoomIntent.OpenMention -> toggle(ChatRoomOverlay.Mention)
            ChatRoomIntent.OpenInspiration -> toggle(ChatRoomOverlay.Inspiration)
            ChatRoomIntent.ContinueStory -> continueStory()
            ChatRoomIntent.JumpToLatest -> emitScroll(force = true)
            is ChatRoomIntent.DraftChanged -> {
                _uiState.update { it.copy(draft = intent.value) }
            }
            is ChatRoomIntent.PickMention -> pickMention(intent.memberId)
            is ChatRoomIntent.PickInspiration -> {
                _uiState.update {
                    it.copy(draft = intent.text, overlay = ChatRoomOverlay.None)
                }
            }
            ChatRoomIntent.MentionPrevPage -> shiftMention(-1)
            ChatRoomIntent.MentionNextPage -> shiftMention(1)
            ChatRoomIntent.ToggleVolume -> {
                _uiState.update { it.copy(volumeMuted = !it.volumeMuted) }
            }
            is ChatRoomIntent.OpenSceneCharacter -> {
                // TODO 日后补齐角色详情页，届时在此发出导航副作用
            }
        }
    }

    private fun toggle(target: ChatRoomOverlay) {
        _uiState.update { state ->
            val next = if (state.overlay == target) ChatRoomOverlay.None else target
            state.copy(
                overlay = next,
                mentionPage = if (next == ChatRoomOverlay.Mention) 0 else state.mentionPage,
            )
        }
    }

    private fun pickMention(memberId: String) {
        val member = _uiState.value.cast.firstOrNull { it.id == memberId } ?: return
        _uiState.update { state ->
            val insertion = "@${member.displayName} "
            val rest = state.draft.trimStart().removePrefix("@").trimStart()
            state.copy(
                draft = insertion + rest,
                overlay = ChatRoomOverlay.None,
            )
        }
    }

    private fun continueStory() {
        val draft = _uiState.value.draft.trim()
        // 先落玩家气泡，再追加本轮剧情；草稿为空时就是纯推进剧情
        val beat = advanceChatStory(storyTurn)
        storyTurn += 1
        _uiState.update { state ->
            val playerLine = if (draft.isEmpty()) {
                emptyList()
            } else {
                playerSeq += 1
                listOf(ChatItem.Player(id = "player-$playerSeq", body = draft))
            }
            state.copy(
                items = state.items + playerLine + beat,
                draft = "",
                overlay = ChatRoomOverlay.None,
            )
        }
        emitScroll(force = draft.isNotEmpty())
    }

    private fun shiftMention(delta: Int) {
        _uiState.update { state ->
            val last = (state.mentionPageCount - 1).coerceAtLeast(0)
            state.copy(mentionPage = (state.mentionPage + delta).coerceIn(0, last))
        }
    }

    private fun emitScroll(force: Boolean) {
        _sideEffect.trySend(ChatRoomSideEffect.ScrollToBottom(force))
    }
}

private fun ChatRoomUiState.Companion.from(content: ChatRoomContent): ChatRoomUiState =
    ChatRoomUiState(
        chapterTitle = content.chapterTitle,
        chapterObjective = content.chapterObjective,
        items = content.items,
        cast = content.cast,
        inspirations = content.inspirations,
    )

internal const val MentionPageSize = 5
