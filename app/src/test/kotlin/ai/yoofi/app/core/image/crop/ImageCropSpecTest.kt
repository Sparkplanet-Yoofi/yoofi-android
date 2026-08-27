package ai.yoofi.app.core.image.crop

import ai.yoofi.app.core.image.ImageProcessConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageCropSpecTest {

    @Test
    fun `从头像配置派生 1比1 且允许旋转`() {
        val spec = ImageCropSpec.from(ImageProcessConfig.Avatar)
        assertEquals(1, spec.aspectWidth)
        assertEquals(1, spec.aspectHeight)
        assertEquals(true, spec.allowRotate)
    }
}
