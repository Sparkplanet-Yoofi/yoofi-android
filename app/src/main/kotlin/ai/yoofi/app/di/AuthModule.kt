package ai.yoofi.app.di

import ai.yoofi.app.data.auth.AuthRemoteDataSource
import ai.yoofi.app.data.auth.InMemoryUserSession
import ai.yoofi.app.data.auth.RemoteAuthRepository
import ai.yoofi.app.data.auth.RetrofitAuthRemoteDataSource
import ai.yoofi.app.domain.auth.AuthRepository
import ai.yoofi.app.domain.auth.GetCurrentUserUseCase
import ai.yoofi.app.domain.auth.UserSessionStore
import ai.yoofi.app.domain.auth.VerifyEmailCodeUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: RemoteAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        impl: RetrofitAuthRemoteDataSource,
    ): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindUserSessionStore(impl: InMemoryUserSession): UserSessionStore
}

@Module
@InstallIn(SingletonComponent::class)
object AuthProvideModule {
    @Provides
    fun provideVerifyEmailCodeUseCase(
        authRepository: AuthRepository,
    ): VerifyEmailCodeUseCase = VerifyEmailCodeUseCase(authRepository)

    @Provides
    fun provideGetCurrentUserUseCase(
        userSessionStore: UserSessionStore,
    ): GetCurrentUserUseCase = GetCurrentUserUseCase(userSessionStore)
}
