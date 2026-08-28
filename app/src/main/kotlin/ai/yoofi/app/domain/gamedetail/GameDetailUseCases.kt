package ai.yoofi.app.domain.gamedetail

/**
 * 详情页的全部 UseCase。
 *
 * 与 search 那边一个文件一个 UseCase 的写法不同：这里有七个操作，
 * 拆七个五行文件只会让人翻不过来。它们共享同一个 Repository、同属一个用例族，
 * 放一个文件更好读。分层约束不变——ViewModel 仍然只认这些类型，碰不到 Repository。
 */
class GetGameDetailUseCase(private val repository: GameDetailRepository) {
    suspend operator fun invoke(gameId: String): GameDetail = repository.detail(gameId)
}

class GetGameCommentsUseCase(private val repository: GameDetailRepository) {
    suspend operator fun invoke(gameId: String): List<GameComment> = repository.comments(gameId)
}

class PostGameCommentUseCase(private val repository: GameDetailRepository) {
    /**
     * 空白内容直接返回 null，不打接口。校验放这里而不是 ViewModel，
     * 是为了让「发评论」这件事的规则只有一处定义。
     */
    suspend operator fun invoke(gameId: String, body: String): GameComment? {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return null
        return repository.postComment(gameId, trimmed)
    }
}

class DeleteGameCommentUseCase(private val repository: GameDetailRepository) {
    suspend operator fun invoke(gameId: String, commentId: String) =
        repository.deleteComment(gameId, commentId)
}

class ToggleCommentLikeUseCase(private val repository: GameDetailRepository) {
    suspend operator fun invoke(commentId: String, liked: Boolean): Boolean =
        repository.toggleCommentLike(commentId, liked)
}

class ToggleAuthorFollowUseCase(private val repository: GameDetailRepository) {
    suspend operator fun invoke(authorId: String, following: Boolean): Boolean =
        repository.toggleFollow(authorId, following)
}

class ToggleGameSavedUseCase(private val repository: GameDetailRepository) {
    suspend operator fun invoke(gameId: String, saved: Boolean): Boolean =
        repository.toggleSaved(gameId, saved)
}
