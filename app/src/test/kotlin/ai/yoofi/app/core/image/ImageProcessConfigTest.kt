package ai.yoofi.app.core.image

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageProcessConfigTest {

    @Test
    fun `头像默认 1比1 且上限 5MB`() {
        val config = ImageProcessConfig.Avatar
        assertEquals(1, config.aspectWidth)
        assertEquals(1, config.aspectHeight)
        assertEquals(5L * 1024 * 1024, config.maxBytes)
        assertEquals(1f, config.aspectRatio, 0.001f)
    }

    @Test
    fun `可配置其它比例与体积`() {
        val config = ImageProcessConfig(
            aspectWidth = 16,
            aspectHeight = 9,
            maxBytes = 2L * 1024 * 1024,
        )
        assertEquals(16f / 9f, config.aspectRatio, 0.001f)
        assertEquals(2L * 1024 * 1024, config.maxBytes)
    }
}
