package ai.yoofi.app.di

import ai.yoofi.app.core.image.crop.ImageCropHostRenderer
import ai.yoofi.app.data.image.crop.canhub.CanHubImageCropHostRenderer
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageCropBindModule {
    @Binds
    abstract fun bindImageCropHostRenderer(
        impl: CanHubImageCropHostRenderer,
    ): ImageCropHostRenderer
}

/** UI 通过入口取渲染器，避免业务 Composable 直接 new CanHub 实现。 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ImageCropHostEntryPoint {
    fun imageCropHostRenderer(): ImageCropHostRenderer
}

fun imageCropHostRenderer(context: Context): ImageCropHostRenderer {
    return EntryPointAccessors.fromApplication(
        context.applicationContext,
        ImageCropHostEntryPoint::class.java,
    ).imageCropHostRenderer()
}
