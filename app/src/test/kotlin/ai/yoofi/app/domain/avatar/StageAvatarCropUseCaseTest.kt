package ai.yoofi.app.domain.avatar

import ai.yoofi.shared.common.AppError
import ai.yoofi.shared.common.Outcome
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StageAvatarCropUseCaseTest {

    @Test
    fun `空白 URI 不暂存`() = runBlocking {
        val store = CropStore()
        val path = StageAvatarCropUseCase(store)("  ")
        assertNull(path)
        assertEquals(0, store.stageCount)
    }

    @Test
    fun `暂存成功返回缓存路径`() = runBlocking {
        val store = CropStore(stage = Outcome.Ok("/cache/crop_source.jpg"))
        val path = StageAvatarCropUseCase(store)("content://media/1")
        assertEquals("/cache/crop_source.jpg", path)
    }

    @Test
    fun `discard 会清掉暂存`() {
        val store = CropStore()
        StageAvatarCropUseCase(store).discard()
        assertTrue(store.discarded)
    }
}

class PersistEncodedAvatarUseCaseTest {

    @Test
    fun `空白路径不落盘`() = runBlocking {
        val store = CropStore()
        val path = PersistEncodedAvatarUseCase(store)(" ")
        assertNull(path)
        assertEquals(0, store.encodedCount)
    }

    @Test
    fun `已编码文件落盘成功`() = runBlocking {
        val store = CropStore(encoded = Outcome.Ok("/data/profile.jpg"))
        val path = PersistEncodedAvatarUseCase(store)("/cache/crop_result.jpg")
        assertEquals("/data/profile.jpg", path)
    }
}

private class CropStore(
    private val stage: Outcome<String> = Outcome.Err(AppError.Unknown),
    private val encoded: Outcome<String> = Outcome.Err(AppError.Unknown),
) : AvatarLocalStore {
    var stageCount: Int = 0
        private set
    var encodedCount: Int = 0
        private set
    var discarded: Boolean = false
        private set

    override fun createCaptureUri(): String = "content://yoofi/capture"

    override suspend fun persistFromUri(sourceUri: String): Outcome<String> {
        return Outcome.Err(AppError.Unknown)
    }

    override suspend fun stageFromUri(sourceUri: String): Outcome<String> {
        stageCount += 1
        return stage
    }

    override suspend fun persistEncodedFile(sourcePath: String): Outcome<String> {
        encodedCount += 1
        return encoded
    }

    override fun discardCapture(uri: String) = Unit

    override fun discardStagedCrop() {
        discarded = true
    }
}
