package ai.yoofi.app.data.gamedetail

import ai.yoofi.app.domain.gamedetail.GameAuthor
import ai.yoofi.app.domain.gamedetail.GameCastMember
import ai.yoofi.app.domain.gamedetail.GameComment
import ai.yoofi.app.domain.gamedetail.GameDetail
import ai.yoofi.app.domain.gamedetail.GameDetailRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 对齐 Figma `1943:13409`（简介收起）与 `1943:13476`（简介展开 + 评论）的英文演示稿。
 *
 * 演示期不区分 gameId：首页三张 Listed 卡都会落到同一份数据，
 * 这样点哪张都能看到完整效果。接真实接口后整个文件换成远端 DataSource，
 * [GameDetailRepository] 契约不动。
 *
 * 关注 / 收藏 / 点赞 / 发评论都改的是进程内状态，退出应用即还原——
 * 这是刻意的：Demo 不该伪装出持久化能力，否则联调时会误判成接口已通。
 */
@Singleton
class DemoGameDetailRepository @Inject constructor() : GameDetailRepository {

    /** 多个协程可能同时改这些可变态（点赞时连点、发评论与刷新并发），加锁保证一致。 */
    private val mutex = Mutex()
    private var following = false
    private var saved = false
    private var comments = DemoComments
    private var postedCount = 0

    override suspend fun detail(gameId: String): GameDetail {
        delay(DemoLatencyMillis)
        return mutex.withLock {
            DemoDetail.copy(
                id = gameId,
                author = DemoDetail.author.copy(following = following),
                saved = saved,
            )
        }
    }

    override suspend fun comments(gameId: String): List<GameComment> {
        delay(DemoLatencyMillis)
        return mutex.withLock { comments }
    }

    override suspend fun postComment(gameId: String, body: String): GameComment {
        delay(DemoLatencyMillis)
        return mutex.withLock {
            postedCount += 1
            val posted = GameComment(
                id = "posted-$postedCount",
                authorName = "You",
                avatarKey = AvatarKeyMe,
                body = body,
                likeCount = 0,
                liked = false,
                replyCount = 0,
                isAuthor = false,
                playedBadge = "0.1 h",
                createdAtLabel = "08/27 22:21",
                deletable = true,
            )
            // 新评论置顶，让发完立刻能看见，不必等列表滚动
            comments = listOf(posted) + comments
            posted
        }
    }

    override suspend fun deleteComment(gameId: String, commentId: String) {
        delay(DemoLatencyMillis)
        mutex.withLock {
            comments = comments.mapNotNull { it.removeRecursively(commentId) }
        }
    }

    override suspend fun toggleCommentLike(commentId: String, liked: Boolean): Boolean {
        delay(DemoLatencyMillis)
        mutex.withLock {
            comments = comments.map { it.applyLikeRecursively(commentId, liked) }
        }
        return liked
    }

    override suspend fun toggleFollow(authorId: String, following: Boolean): Boolean {
        delay(DemoLatencyMillis)
        mutex.withLock { this.following = following }
        return following
    }

    override suspend fun toggleSaved(gameId: String, saved: Boolean): Boolean {
        delay(DemoLatencyMillis)
        mutex.withLock { this.saved = saved }
        return saved
    }
}

/** 删掉命中的那条（含其整棵子树）；未命中则原样返回。 */
private fun GameComment.removeRecursively(targetId: String): GameComment? {
    if (id == targetId) return null
    return copy(replies = replies.mapNotNull { it.removeRecursively(targetId) })
}

/** 点赞同时改计数，避免 UI 自己算导致与服务端口径不一致。 */
private fun GameComment.applyLikeRecursively(targetId: String, liked: Boolean): GameComment {
    val next = if (id == targetId && liked != this.liked) {
        copy(liked = liked, likeCount = (likeCount + if (liked) 1 else -1).coerceAtLeast(0))
    } else {
        this
    }
    return next.copy(replies = next.replies.map { it.applyLikeRecursively(targetId, liked) })
}

/** 演示接口耗时，用来验证加载态，不代表真实网络。 */
private const val DemoLatencyMillis = 180L

/** 封面 / 头像标识。UI 层把它们映射到本地图，接接口后换成 URL。 */
private const val CoverKeyForbiddenGame = "cover-forbidden-world"
private const val AvatarKeyAuthor = "avatar-author"
private const val AvatarKeyAnmi = "avatar-1"
private const val AvatarKeyJenny = "avatar-2"
private const val AvatarKeyJennyReply = "avatar-3"
private const val AvatarKeyMe = "avatar-1"

/** Figma `1943:13474`：设计稿把同一段话重复三遍来撑满展开态，这里如实照搬。 */
private const val SynopsisParagraph =
    "The metal floor of the capsule rises beneath you, and then the world detonates " +
        "into sound and heat. Sand, sun, and the roar of a bloodthirsty,"

private val DemoDetail = GameDetail(
    id = "forbidden-world",
    title = "forbidden world",
    coverKey = CoverKeyForbiddenGame,
    author = GameAuthor(
        id = "author-anmi",
        name = "Anmi",
        avatarKey = AvatarKeyAuthor,
        following = false,
    ),
    synopsisTitle = "forbidden world：",
    synopsis = List(3) { SynopsisParagraph }.joinToString(""),
    // Figma `1943:13462`：5 格，最后一格被右边缘裁掉一半以暗示可横滑
    cast = List(5) { index ->
        GameCastMember(
            id = "cast-${index + 1}",
            name = "Name",
            portraitKey = "cast-${index % 4 + 1}",
        )
    },
    saved = false,
)

/** Figma `1889:12962`：四条评论共用同一段占位文案。 */
private const val DemoCommentBody =
    "\u201CThe other tributes materialize around it in their own columns of light, " +
        "frozen for a single, horrible heartbeat.\u201D"

/** Figma `1943:13548`：两条主楼，第二条带两层回复。 */
private val DemoComments = listOf(
    GameComment(
        id = "comment-1",
        authorName = "Anmi",
        avatarKey = AvatarKeyAnmi,
        body = DemoCommentBody,
        likeCount = 20,
        liked = false,
        replyCount = 10,
        isAuthor = true,
        playedBadge = "2.5 h",
        createdAtLabel = "08/27 22:21",
        deletable = true,
    ),
    GameComment(
        id = "comment-2",
        authorName = "Jenny",
        avatarKey = AvatarKeyJenny,
        body = DemoCommentBody,
        likeCount = 20,
        liked = false,
        replyCount = 10,
        isAuthor = false,
        playedBadge = "0.5 h",
        createdAtLabel = "08/27 22:21",
        deletable = true,
        replies = listOf(
            GameComment(
                id = "comment-2-1",
                authorName = "Jenny",
                avatarKey = AvatarKeyJennyReply,
                body = DemoCommentBody,
                likeCount = 1,
                liked = false,
                replyCount = 1,
                isAuthor = false,
                playedBadge = "2.5 h",
                createdAtLabel = "08/27 22:21",
            ),
            GameComment(
                id = "comment-2-2",
                authorName = "Jenny",
                avatarKey = AvatarKeyJennyReply,
                body = DemoCommentBody,
                likeCount = 1,
                liked = false,
                replyCount = 1,
                isAuthor = false,
                playedBadge = "",
                createdAtLabel = "08/27 22:21",
                replyToName = "Jenny",
            ),
        ),
    ),
)
