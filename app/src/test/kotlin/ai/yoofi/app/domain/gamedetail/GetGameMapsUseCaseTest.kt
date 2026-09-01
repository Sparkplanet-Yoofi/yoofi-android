package ai.yoofi.app.domain.gamedetail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetGameMapsUseCaseTest {

    @Test
    fun `Demo 四张地图且首张对齐稿面 Map 01`() {
        val maps = GetGameMapsUseCase()()
        assertEquals(4, maps.size)
        assertEquals(listOf("Map 01", "Map 02", "Map 03", "Map 04"), maps.map { it.title })
        val first = maps.first()
        assertEquals("map-01", first.id)
        assertEquals("demo-world", first.imageKey)
        assertEquals(4, first.locations.count { it.kind == GameMapMarkerKind.Label })
        assertEquals(1, first.locations.count { it.kind == GameMapMarkerKind.Pin })
        first.locations.forEach { mark ->
            assertTrue(mark.x in 0f..1f)
            assertTrue(mark.y in 0f..1f)
            assertEquals("demo-go", mark.previewKey)
            assertEquals("demo-scene", mark.sceneKey)
        }
    }
}

class FormatMapGoMessageTest {

    @Test
    fun `有名字带上地点`() {
        assertEquals("Go to Ilyria.", formatMapGoMessage("Ilyria"))
    }

    @Test
    fun `空名字回落到 location`() {
        assertEquals("Go to location.", formatMapGoMessage(""))
        assertEquals("Go to location.", formatMapGoMessage("  "))
    }
}
