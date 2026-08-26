package ai.yoofi.app.di

import ai.yoofi.app.data.auth.MockAuthRepository
import ai.yoofi.app.domain.auth.AuthRepository
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
    abstract fun bindAuthRepository(impl: MockAuthRepository): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AuthProvideModule {
    @Provides
    fun provideVerifyEmailCodeUseCase(
        authRepository: AuthRepository,
    ): VerifyEmailCodeUseCase = VerifyEmailCodeUseCase(authRepository)
}
