package ai.yoofi.app.ui.search

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchHighlightTest {

    @Test
    fun `命中片段被染色且大小写不敏感`() {
        val result = highlightQuery("Marriage Avenger", "marriage", Highlight)
        assertEquals("Marriage Avenger", result.text)
        val spans = result.spanStyles
        assertEquals(1, spans.size)
        assertEquals(0, spans[0].start)
        assertEquals("Marriage".length, spans[0].end)
        assertEquals(Highlight, spans[0].item.color)
    }

    @Test
    fun `命中在中间时前后原文保留`() {
        val result = highlightQuery("Abandon a terrible Marriage", "Marriage", Highlight)
        assertEquals("Abandon a terrible Marriage", result.text)
        val span = result.spanStyles.single()
        assertEquals("Abandon a terrible ".length, span.start)
        assertEquals("Abandon a terrible Marriage".length, span.end)
    }

    @Test
    fun `同一标题里多处命中都染色`() {
        val result = highlightQuery("Marriage after Marriage", "Marriage", Highlight)
        assertEquals(2, result.spanStyles.size)
    }

    @Test
    fun `未命中或空关键词不染色`() {
        assertTrue(highlightQuery("Marriage Stealer", "zzz", Highlight).spanStyles.isEmpty())
        assertTrue(highlightQuery("Marriage Stealer", "  ", Highlight).spanStyles.isEmpty())
        assertEquals("Marriage Stealer", highlightQuery("Marriage Stealer", "", Highlight).text)
    }
}

private val Highlight = Color(0xFF7945FF)
