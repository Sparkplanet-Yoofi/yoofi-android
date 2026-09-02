package ai.yoofi.app.data.item.preview

import ai.yoofi.shared.common.Outcome

/**
 * 道具 3D 预览元数据的远程数据源。纯 Kotlin 契约，禁止出现 Ktor / android.*。
 *
 * 只取元数据（URL、相机预设、限位），GLB 本体由渲染层按 URL 自行拉取与缓存。
 */
interface Item3dRemoteDataSource {

    /**
     * @param itemKey 道具标识，与列表接口的 `imageKey` 同源
     * @return 道具没有 3D 资产时仍是 [Outcome.Ok]，只是 [Item3dModelDto.model] 为 null
     */
    suspend fun fetchModel(itemKey: String): Outcome<Item3dModelDto>
}
