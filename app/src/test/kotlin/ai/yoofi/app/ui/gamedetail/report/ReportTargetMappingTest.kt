package ai.yoofi.app.ui.gamedetail.report

import ai.yoofi.app.domain.gamedetail.GameAuthor
import ai.yoofi.app.domain.gamedetail.GameDetail
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportTargetMappingTest {

    @Test
    fun `详情只抽出举报需要的作品摘要`() {
        val target = GameDetail(
            id = "forbidden-world",
            title = "Arranged Marriage Simulator",
            coverKey = "cover-forbidden-world",
            author = GameAuthor(
                id = "author-anmi",
                name = "Author Name",
                avatarKey = "avatar-author",
                following = false,
            ),
            synopsisTitle = "title",
            synopsis = "body",
            cast = emptyList(),
            saved = false,
        ).toReportTarget()
        assertEquals("forbidden-world", target.gameId)
        assertEquals("Arranged Marriage Simulator", target.title)
        assertEquals("Author Name", target.authorName)
        assertEquals("cover-forbidden-world", target.coverKey)
    }
}
