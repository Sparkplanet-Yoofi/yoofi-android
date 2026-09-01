package ai.yoofi.app.ui.gamedetail.map

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

class GameMapPanTest {

    @Test
    fun `图比视口大时不能拖出空白`() {
        val tooRight = coerceMapOffset(
            offset = Offset(10f, 10f),
            mapWidth = 1000f,
            mapHeight = 1000f,
            viewportWidth = 390f,
            viewportHeight = 844f,
        )
        assertEquals(0f, tooRight.x)
        assertEquals(0f, tooRight.y)

        val tooLeft = coerceMapOffset(
            offset = Offset(-900f, -900f),
            mapWidth = 1000f,
            mapHeight = 1000f,
            viewportWidth = 390f,
            viewportHeight = 844f,
        )
        assertEquals(390f - 1000f, tooLeft.x)
        assertEquals(844f - 1000f, tooLeft.y)
    }

    @Test
    fun `图比视口小时居中`() {
        val centered = coerceMapOffset(
            offset = Offset.Zero,
            mapWidth = 200f,
            mapHeight = 200f,
            viewportWidth = 390f,
            viewportHeight = 844f,
        )
        assertEquals((390f - 200f) / 2f, centered.x)
        assertEquals((844f - 200f) / 2f, centered.y)
    }
}
