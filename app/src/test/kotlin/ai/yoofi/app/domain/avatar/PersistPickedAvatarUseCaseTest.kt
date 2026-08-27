package ai.yoofi.app.domain.avatar

import ai.yoofi.app.core.common.AppError
import ai.yoofi.app.core.common.Outcome
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersistPickedAvatarUseCaseTest {

    @Test
    fun `空白 URI 不落盘`() = runBlocking {
        val store = FakeAvatarLocalStore()
        val path = PersistPickedAvatarUseCase(store)("  ")
        assertNull(path)
        assertEquals(0, store.persistCount)
    }

    @Test
    fun `落盘成功返回路径`() = runBlocking {
        val store = FakeAvatarLocalStore(result = Outcome.Ok("/data/avatar.jpg"))
        val path = PersistPickedAvatarUseCase(store)("content://media/1")
        assertEquals("/data/avatar.jpg", path)
    }

    @Test
    fun `落盘失败返回 null`() = runBlocking {
        val store = FakeAvatarLocalStore(result = Outcome.Err(AppError.Unknown))
        val path = PersistPickedAvatarUseCase(store)("content://media/1")
        assertNull(path)
    }
}

private class FakeAvatarLocalStore(
    private val result: Outcome<String> = Outcome.Err(AppError.Unknown),
) : AvatarLocalStore {
    var persistCount: Int = 0
        private set

    override fun createCaptureUri(): String = "content://yoofi/capture"

    override suspend fun persistFromUri(sourceUri: String): Outcome<String> {
        persistCount += 1
        return result
    }

    override suspend fun stageFromUri(sourceUri: String): Outcome<String> {
        return result
    }

    override suspend fun persistEncodedFile(sourcePath: String): Outcome<String> {
        return result
    }

    override fun discardCapture(uri: String) = Unit

    override fun discardStagedCrop() = Unit
}
