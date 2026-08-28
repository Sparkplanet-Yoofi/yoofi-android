package ai.yoofi.app.ui.gamedetail

import ai.yoofi.app.domain.gamedetail.DeleteGameCommentUseCase
import ai.yoofi.app.domain.gamedetail.GameComment
import ai.yoofi.app.domain.gamedetail.GameDetail
import ai.yoofi.app.domain.gamedetail.GetGameCommentsUseCase
import ai.yoofi.app.domain.gamedetail.GetGameDetailUseCase
import ai.yoofi.app.domain.gamedetail.PostGameCommentUseCase
import ai.yoofi.app.domain.gamedetail.ToggleAuthorFollowUseCase
import ai.yoofi.app.domain.gamedetail.ToggleCommentLikeUseCase
import ai.yoofi.app.domain.gamedetail.ToggleGameSavedUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class GameDetailUiState(
    val loading: Boolean = true,
    val detail: GameDetail? = null,
    val comments: List<GameComment> = emptyList(),
    /** 简介默认收起，只露 3 行，对应 Figma `1943:13409`。 */
    val synopsisExpanded: Boolean = false,
    val draft: String = "",
    /**
     * 回复被折叠起来的主楼 id。默认展开（设计稿底部是「Hide Replies」而非「Show」），
     * 所以这里存的是「例外」而不是「已展开集合」，省得每次加载都要初始化一遍。
     */
    val collapsedReplyIds: Set<String> = emptySet(),
)

internal sealed interface GameDetailIntent {
    data object ToggleSynopsis : GameDetailIntent
    data object ToggleFollow : GameDetailIntent
    data object ToggleSaved : GameDetailIntent
    data class DraftChanged(val value: String) : GameDetailIntent
    data object SubmitComment : GameDetailIntent
    data class ToggleLike(val commentId: String) : GameDetailIntent
    data class DeleteComment(val commentId: String) : GameDetailIntent

    /** 主楼底部「Hide Replies」。 */
    data class ToggleReplies(val commentId: String) : GameDetailIntent
}

/**
 * 游戏详情页 ViewModel。只依赖 UseCase，不碰任何数据源实现。
 *
 * 关注 / 收藏 / 点赞三个开关走乐观更新：先本地翻转让手感跟手，
 * 再拿 UseCase 的返回值对齐。返回值与本地不一致时以返回值为准，
 * 这样服务端拒绝（如未登录）能自动回滚，不用单独写失败分支。
 */
@HiltViewModel
internal class GameDetailViewModel @Inject constructor(
    private val getGameDetail: GetGameDetailUseCase,
    private val getGameComments: GetGameCommentsUseCase,
    private val postGameComment: PostGameCommentUseCase,
    private val deleteGameComment: DeleteGameCommentUseCase,
    private val toggleCommentLike: ToggleCommentLikeUseCase,
    private val toggleAuthorFollow: ToggleAuthorFollowUseCase,
    private val toggleGameSaved: ToggleGameSavedUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    private var gameId: String? = null

    /**
     * 载入指定作品。工程尚未引入 Navigation，拿不到路由参数，
     * 因此由 Screen 在 `LaunchedEffect(gameId)` 里调用。重复传同一个 id 不会重复请求。
     */
    fun load(gameId: String) {
        if (this.gameId == gameId) return
        this.gameId = gameId
        _uiState.value = GameDetailUiState(loading = true)
        viewModelScope.launch {
            val detail = getGameDetail(gameId)
            val comments = getGameComments(gameId)
            _uiState.value = GameDetailUiState(
                loading = false,
                detail = detail,
                comments = comments,
            )
        }
    }

    fun onIntent(intent: GameDetailIntent) {
        when (intent) {
            GameDetailIntent.ToggleSynopsis ->
                _uiState.update { it.copy(synopsisExpanded = !it.synopsisExpanded) }

            GameDetailIntent.ToggleFollow -> onToggleFollow()
            GameDetailIntent.ToggleSaved -> onToggleSaved()
            is GameDetailIntent.DraftChanged ->
                _uiState.update { it.copy(draft = intent.value) }

            GameDetailIntent.SubmitComment -> onSubmitComment()
            is GameDetailIntent.ToggleLike -> onToggleLike(intent.commentId)
            is GameDetailIntent.DeleteComment -> onDeleteComment(intent.commentId)
            is GameDetailIntent.ToggleReplies -> onToggleReplies(intent.commentId)
        }
    }

    private fun onToggleFollow() {
        val author = _uiState.value.detail?.author ?: return
        val next = !author.following
        updateAuthorFollowing(next)
        viewModelScope.launch {
            val confirmed = toggleAuthorFollow(author.id, next)
            if (confirmed != next) updateAuthorFollowing(confirmed)
        }
    }

    private fun updateAuthorFollowing(following: Boolean) = _uiState.update { state ->
        val detail = state.detail ?: return@update state
        state.copy(detail = detail.copy(author = detail.author.copy(following = following)))
    }

    private fun onToggleSaved() {
        val detail = _uiState.value.detail ?: return
        val next = !detail.saved
        updateSaved(next)
        viewModelScope.launch {
            val confirmed = toggleGameSaved(detail.id, next)
            if (confirmed != next) updateSaved(confirmed)
        }
    }

    private fun updateSaved(saved: Boolean) = _uiState.update { state ->
        state.copy(detail = state.detail?.copy(saved = saved))
    }

    private fun onToggleLike(commentId: String) {
        val current = _uiState.value.comments.findRecursively(commentId) ?: return
        val next = !current.liked
        _uiState.update { it.copy(comments = it.comments.mapLike(commentId, next)) }
        viewModelScope.launch {
            val confirmed = toggleCommentLike(commentId, next)
            if (confirmed != next) {
                _uiState.update { it.copy(comments = it.comments.mapLike(commentId, confirmed)) }
            }
        }
    }

    private fun onSubmitComment() {
        val gameId = gameId ?: return
        val draft = _uiState.value.draft
        viewModelScope.launch {
            // 空白校验在 UseCase 里，这里拿到 null 就说明没必要清空输入框
            val posted = postGameComment(gameId, draft) ?: return@launch
            _uiState.update { it.copy(draft = "", comments = listOf(posted) + it.comments) }
        }
    }

    private fun onDeleteComment(commentId: String) {
        val gameId = gameId ?: return
        _uiState.update { it.copy(comments = it.comments.mapNotNull { c -> c.remove(commentId) }) }
        viewModelScope.launch { deleteGameComment(gameId, commentId) }
    }

    private fun onToggleReplies(commentId: String) = _uiState.update { state ->
        val next = state.collapsedReplyIds.toMutableSet()
        if (!next.add(commentId)) next.remove(commentId)
        state.copy(collapsedReplyIds = next)
    }
}

private fun List<GameComment>.findRecursively(targetId: String): GameComment? {
    forEach { comment ->
        if (comment.id == targetId) return comment
        comment.replies.findRecursively(targetId)?.let { return it }
    }
    return null
}

private fun List<GameComment>.mapLike(targetId: String, liked: Boolean): List<GameComment> =
    map { comment ->
        val next = if (comment.id == targetId && comment.liked != liked) {
            comment.copy(
                liked = liked,
                likeCount = (comment.likeCount + if (liked) 1 else -1).coerceAtLeast(0),
            )
        } else {
            comment
        }
        next.copy(replies = next.replies.mapLike(targetId, liked))
    }

private fun GameComment.remove(targetId: String): GameComment? {
    if (id == targetId) return null
    return copy(replies = replies.mapNotNull { it.remove(targetId) })
}
