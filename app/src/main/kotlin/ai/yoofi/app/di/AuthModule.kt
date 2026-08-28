package ai.yoofi.app.di

import ai.yoofi.app.core.config.DataSourceSwitch
import ai.yoofi.app.core.config.DemoFeature
import ai.yoofi.app.data.auth.AuthRemoteDataSource
import ai.yoofi.app.data.auth.DemoAuthRemoteDataSource
import ai.yoofi.app.data.auth.InMemoryUserSession
import ai.yoofi.app.data.auth.KtorAuthRemoteDataSource
import ai.yoofi.app.data.auth.RemoteAuthRepository
import ai.yoofi.app.domain.auth.AuthRepository
import ai.yoofi.app.domain.auth.GetCurrentUserUseCase
import ai.yoofi.app.domain.auth.UserSessionStore
import ai.yoofi.app.domain.auth.VerifyEmailCodeUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: RemoteAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserSessionStore(impl: InMemoryUserSession): UserSessionStore
}

@Module
@InstallIn(SingletonComponent::class)
object AuthProvideModule {
    /**
     * 切换点放在 DataSource 而非 Repository：这样 Demo 模式下
     * [RemoteAuthRepository] 的 DTO 映射与会话写入照样跑，
     * 假数据与真接口走的是同一条业务路径。
     */
    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(
        switch: DataSourceSwitch,
        demo: Provider<DemoAuthRemoteDataSource>,
        real: Provider<KtorAuthRemoteDataSource>,
    ): AuthRemoteDataSource = switch.select(DemoFeature.Auth, demo, real)

    @Provides
    fun provideVerifyEmailCodeUseCase(
        authRepository: AuthRepository,
    ): VerifyEmailCodeUseCase = VerifyEmailCodeUseCase(authRepository)

    @Provides
    fun provideGetCurrentUserUseCase(
        userSessionStore: UserSessionStore,
    ): GetCurrentUserUseCase = GetCurrentUserUseCase(userSessionStore)
}
