package ai.yoofi.app.di

import ai.yoofi.app.core.item.preview.ItemPreviewHostRenderer
import ai.yoofi.app.data.item.preview.DemoItem3dModelSource
import ai.yoofi.app.data.item.preview.Item3dRemoteDataSource
import ai.yoofi.app.data.item.preview.KtorItem3dRemoteDataSource
import ai.yoofi.app.data.item.preview.RemoteItem3dModelSource
import ai.yoofi.app.data.item.preview.SceneViewItemPreviewHostRenderer
import ai.yoofi.shared.config.DataSourceSwitch
import ai.yoofi.shared.config.DemoFeature
import ai.yoofi.shared.item.orbit.Item3dModelSource
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ItemPreviewBindModule {
    /**
     * 道具实时 3D 渲染器。换回纯静态卡面只需把实现改成 BitmapItemPreviewHostRenderer，
     * 日后换渲染引擎同样只改这一行。
     */
    @Binds
    abstract fun bindItemPreviewHostRenderer(
        impl: SceneViewItemPreviewHostRenderer,
    ): ItemPreviewHostRenderer

    @Binds
    abstract fun bindItem3dRemoteDataSource(
        impl: KtorItem3dRemoteDataSource,
    ): Item3dRemoteDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object ItemPreviewProvideModule {
    /**
     * 模型来源。Demo 读包内样例资产，真实实现查服务端（契约见 `.jack/Yoofi3D.md` §12），
     * 渲染层不受影响。
     *
     * 接口联调时改 `DemoFeature.Item3dPreview`：**先把 `realImplemented` 翻成 true**，
     * 否则 `useDemo()` 第一条判定就短路回 Demo，改 `demoInDevelopment` 不起作用。
     */
    @Provides
    @Singleton
    fun provideItem3dModelSource(
        switch: DataSourceSwitch,
        demo: Provider<DemoItem3dModelSource>,
        real: Provider<RemoteItem3dModelSource>,
    ): Item3dModelSource = switch.select(DemoFeature.Item3dPreview, demo, real)
}

/** UI 通过入口取渲染器，日后换渲染方式只改绑定。 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ItemPreviewHostEntryPoint {
    fun itemPreviewHostRenderer(): ItemPreviewHostRenderer
}

fun itemPreviewHostRenderer(context: Context): ItemPreviewHostRenderer {
    return EntryPointAccessors.fromApplication(
        context.applicationContext,
        ItemPreviewHostEntryPoint::class.java,
    ).itemPreviewHostRenderer()
}
