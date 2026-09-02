package ai.yoofi.app.data.item.preview

import ai.yoofi.shared.item.orbit.Item3dModel
import ai.yoofi.shared.item.orbit.OrbitCameraPreset
import ai.yoofi.shared.item.orbit.OrbitLimits
import kotlinx.serialization.Serializable

/**
 * 道具 3D 预览元数据的接口契约。字段命名跟随项目既有 DTO 走 camelCase。
 *
 * 完整的接口说明与 JSON 样例见 `.jack/Yoofi3D.md` §12。
 *
 * 所有字段都有默认值：服务端分批上线字段时老客户端不会解析失败，
 * 缺字段一律退回客户端默认（`Json` 已配 `ignoreUnknownKeys` + `explicitNulls = false`）。
 */
@Serializable
data class Item3dModelDto(
    val itemKey: String = "",
    /**
     * 为 null 表示这个道具暂时没有 3D 资产——是正常状态，不是错误。
     *
     * 不要用 `data: null` 或错误码表达，那会被 `toOutcome()` 判成失败：
     * 它只认 `code == 0 && data != null`。
     */
    val model: Item3dAssetDto? = null,
    val camera: Item3dCameraDto = Item3dCameraDto(),
    val limits: Item3dLimitsDto = Item3dLimitsDto(),
    /** 视口底色，`#RRGGBB`。空串则用客户端默认（纯黑） */
    val backgroundColor: String = "",
    /** IBL 环境贴图（.hdr / .ktx）。空串则用引擎自带的中性光照 */
    val environmentUrl: String = "",
)

@Serializable
data class Item3dAssetDto(
    val url: String = "",
    /** 目前只认 `glb`；给了别的值客户端会当作没有 3D 资产而降级，不会崩 */
    val format: String = FormatGlb,
    /** 弱网下先给用户一个下载量的预期，也用于对账 CDN 回源 */
    val sizeBytes: Long = 0L,
    /** 十六进制小写。客户端拿它当磁盘缓存键，模型换了内容 URL 不变也能失效 */
    val sha256: String = "",
) {
    companion object {
        const val FormatGlb = "glb"
    }
}

@Serializable
data class Item3dCameraDto(
    val baseDistance: Float = 2.4f,
    val verticalFovDeg: Float = 45f,
    val initialYawDeg: Float = 0f,
    val initialPitchDeg: Float = 12f,
)

@Serializable
data class Item3dLimitsDto(
    val minPitchDeg: Float = -85f,
    val maxPitchDeg: Float = 85f,
    val minScale: Float = 1f,
    val maxScale: Float = 4f,
)

/**
 * DTO → domain。返回 null 表示「这个道具不做 3D 预览」，调用方据此退回静态卡面。
 *
 * 这里对服务端数据是不信任的：URL 为空、格式不认识、数值越界都当作没有资产处理。
 * 预览只是锦上添花，宁可退回静态图，也不能让一条脏数据把弹窗搞崩。
 */
internal fun Item3dModelDto.toDomain(): Item3dModel? {
    val asset = model ?: return null
    if (asset.url.isBlank()) return null
    if (!asset.format.equals(Item3dAssetDto.FormatGlb, ignoreCase = true)) return null

    return Item3dModel(
        itemId = itemKey,
        modelUri = asset.url,
        environmentUri = environmentUrl.ifBlank { null },
        camera = camera.toDomain(),
        limits = limits.toDomain(),
        backgroundRgb = parseHexRgb(backgroundColor) ?: DefaultBackgroundRgb,
    )
}

private fun Item3dCameraDto.toDomain(): OrbitCameraPreset {
    val fallback = OrbitCameraPreset()
    return OrbitCameraPreset(
        // 距离和视场角为零会让投影矩阵退化成一片空白，兜回默认值
        baseDistance = baseDistance.takeIf { it > 0f } ?: fallback.baseDistance,
        verticalFovDeg = verticalFovDeg.takeIf { it > 0f && it < 180f } ?: fallback.verticalFovDeg,
        initialYawDeg = initialYawDeg,
        initialPitchDeg = initialPitchDeg,
    )
}

private fun Item3dLimitsDto.toDomain(): OrbitLimits {
    val fallback = OrbitLimits.Default
    // 上下界写反就整段作废：与其猜服务端想表达什么，不如用一套已知可用的默认值
    if (minPitchDeg > maxPitchDeg || minScale > maxScale || minScale <= 0f) return fallback
    return OrbitLimits(
        minPitchDeg = minPitchDeg,
        maxPitchDeg = maxPitchDeg,
        minScale = minScale,
        maxScale = maxScale,
    )
}

/** 解析 `#RRGGBB` / `RRGGBB`。解析不了返回 null，交给调用方兜默认色 */
private fun parseHexRgb(raw: String): Int? {
    val hex = raw.removePrefix("#")
    if (hex.length != HexRgbLength) return null
    return hex.toIntOrNull(radix = 16)
}

private const val HexRgbLength = 6
private const val DefaultBackgroundRgb = 0x000000
