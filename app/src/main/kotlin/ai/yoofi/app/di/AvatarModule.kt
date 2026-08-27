package ai.yoofi.app.di

import ai.yoofi.app.data.avatar.AndroidCameraCapability
import ai.yoofi.app.data.avatar.FileAvatarLocalStore
import ai.yoofi.app.domain.avatar.AvatarLocalStore
import ai.yoofi.app.domain.avatar.CameraCapability
import ai.yoofi.app.domain.avatar.PersistEncodedAvatarUseCase
import ai.yoofi.app.domain.avatar.PersistPickedAvatarUseCase
import ai.yoofi.app.domain.avatar.PrepareCameraCaptureUseCase
import ai.yoofi.app.domain.avatar.ResolveTakePhotoUseCase
import ai.yoofi.app.domain.avatar.StageAvatarCropUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AvatarBindModule {
    @Binds
    @Singleton
    abstract fun bindCameraCapability(impl: AndroidCameraCapability): CameraCapability

    @Binds
    @Singleton
    abstract fun bindAvatarLocalStore(impl: FileAvatarLocalStore): AvatarLocalStore
}

@Module
@InstallIn(SingletonComponent::class)
object AvatarProvideModule {
    @Provides
    fun provideResolveTakePhotoUseCase(
        camera: CameraCapability,
    ): ResolveTakePhotoUseCase = ResolveTakePhotoUseCase(camera)

    @Provides
    fun providePrepareCameraCaptureUseCase(
        store: AvatarLocalStore,
    ): PrepareCameraCaptureUseCase = PrepareCameraCaptureUseCase(store)

    @Provides
    fun providePersistPickedAvatarUseCase(
        store: AvatarLocalStore,
    ): PersistPickedAvatarUseCase = PersistPickedAvatarUseCase(store)

    @Provides
    fun provideStageAvatarCropUseCase(
        store: AvatarLocalStore,
    ): StageAvatarCropUseCase = StageAvatarCropUseCase(store)

    @Provides
    fun providePersistEncodedAvatarUseCase(
        store: AvatarLocalStore,
    ): PersistEncodedAvatarUseCase = PersistEncodedAvatarUseCase(store)
}
