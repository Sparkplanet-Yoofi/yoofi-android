package ai.yoofi.app.data.item.preview

import ai.yoofi.shared.common.Outcome
import ai.yoofi.shared.item.orbit.Item3dModel
import ai.yoofi.shared.item.orbit.Item3dModelSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 3D 元数据的真实来源：查服务端，把 DTO 映射成渲染层认识的领域模型。
 *
 * 在分层上对应 Repository 这一层（DTO→Domain、错误归一），
 * 只是契约名沿用了渲染层依赖的 [Item3dModelSource]。
 *
 * **失败一律静默降级成 null**，不向上抛错误：3D 预览是锦上添花，
 * 断网或服务端抖动时用户该看到的是静态卡面，而不是一个报错弹窗。
 * 需要定位问题时看埋点，不是看 UI。
 */
@Singleton
class RemoteItem3dModelSource @Inject constructor(
    private val remoteDataSource: Item3dRemoteDataSource,
) : Item3dModelSource {

    override suspend fun load(itemKey: String): Item3dModel? =
        when (val outcome = remoteDataSource.fetchModel(itemKey)) {
            is Outcome.Ok -> outcome.value.toDomain()
            is Outcome.Err -> null
        }
}
