package ai.yoofi.app.ui.gamedetail

import ai.yoofi.app.domain.gamedetail.DeleteGameCommentUseCase
import ai.yoofi.app.domain.gamedetail.GameComment
import ai.yoofi.app.domain.gamedetail.GameDetail
import ai.yoofi.app.domain.gamedetail.GameDetailRepository
import ai.yoofi.app.domain.gamedetail.GetGameCommentsUseCase
import ai.yoofi.app.domain.gamedetail.GetGameDetailUseCase
import ai.yoofi.app.domain.gamedetail.PostGameCommentUseCase
import ai.yoofi.app.domain.gamedetail.ToggleAuthorFollowUseCase
import ai.yoofi.app.domain.gamedetail.ToggleCommentLikeUseCase
import ai.yoofi.app.domain.gamedetail.ToggleGameSavedUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameDetailViewModelTest {

    @Test
    fun `三点菜单再取消回到无弹层`() {
        val viewModel = viewModel()
        viewModel.onIntent(GameDetailIntent.OpenMenu)
        assertEquals(GameDetailOverlay.Menu, viewModel.uiState.value.overlay)
        viewModel.onIntent(GameDetailIntent.DismissOverlay)
        assertEquals(GameDetailOverlay.None, viewModel.uiState.value.overlay)
    }

    @Test
    fun `重置关闭菜单并留下 Snackbar 占位`() {
        val viewModel = viewModel()
        viewModel.onIntent(GameDetailIntent.OpenMenu)
        viewModel.onIntent(GameDetailIntent.ResetStory)
        assertEquals(GameDetailOverlay.None, viewModel.uiState.value.overlay)
        assertEquals(GameDetailSnackbar.StartNewStory, viewModel.uiState.value.snackbar)
        viewModel.onIntent(GameDetailIntent.ConsumeSnackbar)
        assertNull(viewModel.uiState.value.snackbar)
    }

    @Test
    fun `举报关闭菜单并打开举报表单`() {
        val viewModel = viewModel()
        viewModel.onIntent(GameDetailIntent.OpenMenu)
        viewModel.onIntent(GameDetailIntent.OpenReport)
        assertEquals(GameDetailOverlay.None, viewModel.uiState.value.overlay)
        assertTrue(viewModel.uiState.value.reportOpen)
        viewModel.onIntent(GameDetailIntent.CloseReport)
        assertFalse(viewModel.uiState.value.reportOpen)
    }

    private fun viewModel(): GameDetailViewModel {
        val repository = UnusedGameDetailRepository()
        return GameDetailViewModel(
            getGameDetail = GetGameDetailUseCase(repository),
            getGameComments = GetGameCommentsUseCase(repository),
            postGameComment = PostGameCommentUseCase(repository),
            deleteGameComment = DeleteGameCommentUseCase(repository),
            toggleCommentLike = ToggleCommentLikeUseCase(repository),
            toggleAuthorFollow = ToggleAuthorFollowUseCase(repository),
            toggleGameSaved = ToggleGameSavedUseCase(repository),
        )
    }
}

private class UnusedGameDetailRepository : GameDetailRepository {
    override suspend fun detail(gameId: String): GameDetail = error("unused")
    override suspend fun comments(gameId: String): List<GameComment> = error("unused")
    override suspend fun postComment(gameId: String, body: String): GameComment = error("unused")
    override suspend fun deleteComment(gameId: String, commentId: String) = error("unused")
    override suspend fun toggleCommentLike(commentId: String, liked: Boolean): Boolean =
        error("unused")
    override suspend fun toggleFollow(authorId: String, following: Boolean): Boolean =
        error("unused")
    override suspend fun toggleSaved(gameId: String, saved: Boolean): Boolean = error("unused")
}
