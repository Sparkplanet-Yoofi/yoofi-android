package ai.yoofi.app.di

import ai.yoofi.app.core.config.DataSourceSwitch
import ai.yoofi.app.core.config.DemoFeature
import ai.yoofi.app.data.auth.AuthRemoteDataSource
import ai.yoofi.app.data.auth.DemoAuthRemoteDataSource
import ai.yoofi.app.data.auth.InMemoryUserSession
import ai.yoofi.app.data.auth.KtorAuthRemoteDataSource
import ai.yoofi.app.data.auth.RemoteAuthRepository
import ai.yoofi.app.domain.auth.AuthRepository
import ai.yoofi.app.domain.auth.DeleteAccountUseCase
import ai.yoofi.app.domain.auth.GetCurrentUserUseCase
import ai.yoofi.app.domain.auth.GetLinkedAccountsUseCase
import ai.yoofi.app.domain.auth.LogoutUseCase
import ai.yoofi.app.domain.auth.SendDeleteCodeUseCase
import ai.yoofi.app.domain.auth.UnlinkAccountUseCase
import ai.yoofi.app.domain.block.GetBlockedUsersUseCase
import ai.yoofi.app.domain.block.UnblockUserUseCase
import ai.yoofi.app.domain.feedback.SubmitFeedbackUseCase
import ai.yoofi.app.domain.auth.UserSessionStore
import ai.yoofi.app.domain.auth.VerifyEmailCodeUseCase
import ai.yoofi.app.domain.gamedetail.GetGameCastCardsUseCase
import ai.yoofi.app.domain.profile.GetPreviewPlayedWorksUseCase
import ai.yoofi.app.domain.profile.MarkProfileCompletedUseCase
import ai.yoofi.app.domain.profile.ResolveMineProfilePresenceUseCase
import ai.yoofi.app.domain.profile.UpdateProfileUseCase
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

    @Provides
    fun provideUpdateProfileUseCase(): UpdateProfileUseCase = UpdateProfileUseCase()

    @Provides
    fun provideResolveMineProfilePresenceUseCase(
        userSessionStore: UserSessionStore,
    ): ResolveMineProfilePresenceUseCase = ResolveMineProfilePresenceUseCase(userSessionStore)

    @Provides
    fun provideMarkProfileCompletedUseCase(
        userSessionStore: UserSessionStore,
    ): MarkProfileCompletedUseCase = MarkProfileCompletedUseCase(userSessionStore)

    @Provides
    fun provideLogoutUseCase(
        userSessionStore: UserSessionStore,
    ): LogoutUseCase = LogoutUseCase(userSessionStore)

    @Provides
    fun provideDeleteAccountUseCase(
        userSessionStore: UserSessionStore,
    ): DeleteAccountUseCase = DeleteAccountUseCase(userSessionStore)

    @Provides
    fun provideSendDeleteCodeUseCase(): SendDeleteCodeUseCase = SendDeleteCodeUseCase()

    @Provides
    fun provideGetLinkedAccountsUseCase(): GetLinkedAccountsUseCase = GetLinkedAccountsUseCase()

    @Provides
    fun provideUnlinkAccountUseCase(): UnlinkAccountUseCase = UnlinkAccountUseCase()

    @Provides
    fun provideGetBlockedUsersUseCase(): GetBlockedUsersUseCase = GetBlockedUsersUseCase()

    @Provides
    fun provideUnblockUserUseCase(): UnblockUserUseCase = UnblockUserUseCase()

    @Provides
    fun provideSubmitFeedbackUseCase(): SubmitFeedbackUseCase = SubmitFeedbackUseCase()

    @Provides
    fun provideGetPreviewPlayedWorksUseCase(): GetPreviewPlayedWorksUseCase =
        GetPreviewPlayedWorksUseCase()

    @Provides
    fun provideGetGameCastCardsUseCase(): GetGameCastCardsUseCase = GetGameCastCardsUseCase()
}
