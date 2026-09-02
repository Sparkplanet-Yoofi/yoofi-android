package ai.yoofi.app.data.item.preview

import ai.yoofi.shared.common.AppError
import ai.yoofi.shared.common.Outcome
import ai.yoofi.shared.item.orbit.OrbitLimits
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 契约测试：直接喂 JSON 字符串而不是构造 DTO 对象。
 *
 * 字段名就是和服务端的约定，改名等于破坏契约，必须让测试红给你看；
 * 用 DTO 构造函数写测试是发现不了改名的。
 */
class Item3dModelMappingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        isLenient = true
    }

    @Test
    fun `完整响应解析成领域模型`() {
        val model = decode(
            """
            {
              "itemKey": "knife",
              "model": {
                "url": "https://cdn.yoofi.ai/item3d/knife/v1/model.glb",
                "format": "glb",
                "sizeBytes": 486231,
                "sha256": "9f2a1c"
              },
              "camera": {
                "baseDistance": 2.2,
                "verticalFovDeg": 40.0,
                "initialYawDeg": 15.0,
                "initialPitchDeg": 14.0
              },
              "limits": {
                "minPitchDeg": -60.0,
                "maxPitchDeg": 60.0,
                "minScale": 1.0,
                "maxScale": 3.0
              },
              "backgroundColor": "#1C1408",
              "environmentUrl": "https://cdn.yoofi.ai/item3d/env/studio.ktx"
            }
            """.trimIndent(),
        )

        assertNotNull(model)
        requireNotNull(model)
        assertEquals("knife", model.itemId)
        assertEquals("https://cdn.yoofi.ai/item3d/knife/v1/model.glb", model.modelUri)
        assertEquals("https://cdn.yoofi.ai/item3d/env/studio.ktx", model.environmentUri)
        assertEquals(2.2f, model.camera.baseDistance, Tolerance)
        assertEquals(40f, model.camera.verticalFovDeg, Tolerance)
        assertEquals(15f, model.camera.initialYawDeg, Tolerance)
        assertEquals(14f, model.camera.initialPitchDeg, Tolerance)
        assertEquals(-60f, model.limits.minPitchDeg, Tolerance)
        assertEquals(3f, model.limits.maxScale, Tolerance)
        assertEquals(0x1C1408, model.backgroundRgb)
        // https 开头才会走 CDN 加载分支
        assertEquals(true, model.isRemote)
    }

    @Test
    fun `只给必填字段时其余走客户端默认`() {
        val model = decode(
            """
            {
              "itemKey": "goblet",
              "model": { "url": "https://cdn.yoofi.ai/item3d/goblet/v1/model.glb" }
            }
            """.trimIndent(),
        )

        requireNotNull(model)
        assertEquals(2.4f, model.camera.baseDistance, Tolerance)
        assertEquals(OrbitLimits.Default, model.limits)
        assertEquals(0x000000, model.backgroundRgb)
        assertNull(model.environmentUri)
    }

    @Test
    fun `道具没有 3D 资产时返回 null 而不是报错`() {
        // 服务端用 model:null 表达「这个道具不做 3D」，外层仍是 code 0
        assertNull(decode("""{ "itemKey": "lollipops", "model": null }"""))
        assertNull(decode("""{ "itemKey": "lollipops" }"""))
    }

    @Test
    fun `脏数据一律退回静态卡面`() {
        assertNull(decode("""{ "itemKey": "k", "model": { "url": "" } }"""))
        assertNull(
            decode(
                """{ "itemKey": "k", "model": { "url": "https://a/b.usdz", "format": "usdz" } }""",
            ),
        )
    }

    @Test
    fun `大小写不同的 glb 仍然认`() {
        assertNotNull(
            decode(
                """
                {
                  "itemKey": "k",
                  "model": { "url": "https://a/b.glb", "format": "GLB" }
                }
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun `相机与限位的越界值回退默认`() {
        val model = decode(
            """
            {
              "itemKey": "k",
              "model": { "url": "https://a/b.glb" },
              "camera": { "baseDistance": 0.0, "verticalFovDeg": 200.0 },
              "limits": { "minPitchDeg": 80.0, "maxPitchDeg": -80.0 }
            }
            """.trimIndent(),
        )

        requireNotNull(model)
        // 距离为 0 会让投影矩阵退化，视场角 200 度同理，都必须被挡住
        assertEquals(2.4f, model.camera.baseDistance, Tolerance)
        assertEquals(45f, model.camera.verticalFovDeg, Tolerance)
        assertEquals(OrbitLimits.Default, model.limits)
    }

    @Test
    fun `背景色容忍不带井号的写法`() {
        assertEquals(0x1C1408, requireNotNull(decodeWithBackground("1C1408")).backgroundRgb)
    }

    @Test
    fun `背景色写坏了用默认黑而不是丢掉整个模型`() {
        assertEquals(0x000000, requireNotNull(decodeWithBackground("chartreuse")).backgroundRgb)
    }

    private fun decodeWithBackground(color: String) = decode(
        """
        {
          "itemKey": "k",
          "model": { "url": "https://a/b.glb" },
          "backgroundColor": "$color"
        }
        """.trimIndent(),
    )

    @Test
    fun `请求失败时静默降级为 null`() = runBlocking {
        val source = RemoteItem3dModelSource(
            FakeItem3dRemoteDataSource(Outcome.Err(AppError.Network)),
        )
        assertNull(source.load("knife"))
    }

    @Test
    fun `请求成功时映射成领域模型`() = runBlocking {
        val source = RemoteItem3dModelSource(
            FakeItem3dRemoteDataSource(
                Outcome.Ok(
                    Item3dModelDto(
                        itemKey = "knife",
                        model = Item3dAssetDto(url = "https://a/b.glb"),
                    ),
                ),
            ),
        )
        assertEquals("knife", requireNotNull(source.load("knife")).itemId)
    }

    private fun decode(raw: String) =
        json.decodeFromString(Item3dModelDto.serializer(), raw).toDomain()

    private companion object {
        const val Tolerance = 1e-4f
    }
}

private class FakeItem3dRemoteDataSource(
    private val outcome: Outcome<Item3dModelDto>,
) : Item3dRemoteDataSource {
    override suspend fun fetchModel(itemKey: String): Outcome<Item3dModelDto> = outcome
}
