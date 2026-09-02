package ai.yoofi.app.data.item.preview

import ai.yoofi.shared.common.Outcome
import ai.yoofi.shared.network.ApiCaller
import ai.yoofi.shared.network.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject
import javax.inject.Singleton

/** 道具 3D 预览元数据。相对路径，Base URL 由 `defaultRequest` 注入。 */
private const val Item3dModelPath = "customer/item/model3d"

/**
 * 3D 元数据接口的 Ktor 实现。HTTP 异常交给 [ApiCaller]，本类只组请求。
 *
 * 走 GET + 查询参数而不是路径参数：道具标识来自服务端下发，不保证是 URL 安全的字面量。
 *
 * Demo 分支在 [DemoItem3dModelSource]（在 [ai.yoofi.shared.item.orbit.Item3dModelSource]
 * 那一层分流，因为 Demo 用的是 `asset://` 包内资产、根本不打网），
 * 由 `DemoFeature.Item3dPreview` 选择，本类不感知 mock。
 */
@Singleton
class KtorItem3dRemoteDataSource @Inject constructor(
    private val httpClient: HttpClient,
    private val apiCaller: ApiCaller,
) : Item3dRemoteDataSource {

    override suspend fun fetchModel(itemKey: String): Outcome<Item3dModelDto> = apiCaller.fetch {
        httpClient.get(Item3dModelPath) {
            parameter("itemKey", itemKey)
        }.body<ApiResponse<Item3dModelDto>>()
    }
}
