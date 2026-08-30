package ai.yoofi.app.ui.gamedetail

import ai.yoofi.app.domain.gamedetail.GameAuthor
import ai.yoofi.app.domain.gamedetail.GameComment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuestProfileTargetMappingTest {

    @Test
    fun `作者头像带上 id 与关注态`() {
        val author = GameAuthor(
            id = "author-anmi",
            name = "Anmi",
            avatarKey = "avatar-author",
            following = true,
        )
        val target = author.toGuestProfileTarget()
        assertEquals("author-anmi", target.userId)
        assertEquals("Anmi", target.displayName)
        assertEquals("avatar-author", target.avatarKey)
        assertEquals(true, target.following)
    }

    @Test
    fun `自己的评论不进客态`() {
        val mine = GameComment(
            id = "c-me",
            authorName = "You",
            avatarKey = "avatar-1",
            body = "hi",
            likeCount = 0,
            liked = false,
            replyCount = 0,
            isAuthor = false,
            playedBadge = "1 h",
            createdAtLabel = "08/27 22:21",
            deletable = true,
        )
        assertNull(mine.toGuestProfileTargetOrNull())
    }
}
