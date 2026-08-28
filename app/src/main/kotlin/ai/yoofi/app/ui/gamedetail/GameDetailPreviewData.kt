package ai.yoofi.app.ui.gamedetail

import ai.yoofi.app.domain.gamedetail.GameAuthor
import ai.yoofi.app.domain.gamedetail.GameCastMember
import ai.yoofi.app.domain.gamedetail.GameComment
import ai.yoofi.app.domain.gamedetail.GameDetail

/**
 * `@Preview` 专用假数据。
 *
 * 刻意不复用 `DemoGameDetailRepository`：那是 data 层实现，UI 预览不该反向依赖它。
 * 内容与 Figma `1943:13476` 一致，改设计稿时两边一起改。
 */
private const val PreviewParagraph =
    "The metal floor of the capsule rises beneath you, and then the world detonates " +
        "into sound and heat. Sand, sun, and the roar of a bloodthirsty,"

private const val PreviewCommentBody =
    "\u201CThe other tributes materialize around it in their own columns of light, " +
        "frozen for a single, horrible heartbeat.\u201D"

internal fun previewDetailState(synopsisExpanded: Boolean): GameDetailUiState =
    GameDetailUiState(
        loading = false,
        synopsisExpanded = synopsisExpanded,
        detail = GameDetail(
            id = "forbidden-world",
            title = "forbidden world",
            coverKey = "cover-forbidden-world",
            author = GameAuthor(
                id = "author-anmi",
                name = "Anmi",
                avatarKey = "avatar-author",
                following = false,
            ),
            synopsisTitle = "forbidden world：",
            synopsis = List(3) { PreviewParagraph }.joinToString(""),
            cast = List(5) { index ->
                GameCastMember(
                    id = "cast-${index + 1}",
                    name = "Name",
                    portraitKey = "cast-${index % 4 + 1}",
                )
            },
            saved = false,
        ),
        comments = listOf(
            GameComment(
                id = "comment-1",
                authorName = "Anmi",
                avatarKey = "avatar-1",
                body = PreviewCommentBody,
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
                avatarKey = "avatar-2",
                body = PreviewCommentBody,
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
                        avatarKey = "avatar-3",
                        body = PreviewCommentBody,
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
                        avatarKey = "avatar-3",
                        body = PreviewCommentBody,
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
        ),
    )
