package ai.yoofi.app.di

import ai.yoofi.app.core.item.preview.ItemPreviewHostRenderer
import ai.yoofi.app.data.item.preview.BitmapItemPreviewHostRenderer
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ItemPreviewBindModule {
    @Binds
    abstract fun bindItemPreviewHostRenderer(
        impl: BitmapItemPreviewHostRenderer,
    ): ItemPreviewHostRenderer
}

/** UI 通过入口取渲染器，日后换 3D 只改绑定。 */
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
