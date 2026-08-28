package ai.yoofi.app.domain.gamedetail

/**
 * 游戏详情页数据契约。纯 Kotlin，不含任何 Android / HTTP 类型，为 KMP 留路。
 *
 * 详情与评论分两次取：评论要翻页、发完要局部刷新，跟详情主体不是一个生命周期。
 *
 * 几个 toggle 方法都返回服务端确认后的最新值，而不是 Unit——
 * 这样 UI 可以先乐观翻转，收到返回值再对齐，失败时也有东西可回滚。
 */
interface GameDetailRepository {

    suspend fun detail(gameId: String): GameDetail

    suspend fun comments(gameId: String): List<GameComment>

    /** 发表主楼评论，返回服务端落库后的完整对象（含 id 与时间）。 */
    suspend fun postComment(gameId: String, body: String): GameComment

    suspend fun deleteComment(gameId: String, commentId: String)

    /** @return 点赞后的最新状态 */
    suspend fun toggleCommentLike(commentId: String, liked: Boolean): Boolean

    /** @return 关注后的最新状态 */
    suspend fun toggleFollow(authorId: String, following: Boolean): Boolean

    /** @return 收藏后的最新状态 */
    suspend fun toggleSaved(gameId: String, saved: Boolean): Boolean
}
