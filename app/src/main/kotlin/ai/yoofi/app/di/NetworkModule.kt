package ai.yoofi.app.di

import ai.yoofi.app.BuildConfig
import ai.yoofi.app.core.config.BuildStage
import ai.yoofi.app.core.network.ApiCaller
import ai.yoofi.app.core.network.AppEnvironment
import ai.yoofi.app.core.network.KtorApiCaller
import ai.yoofi.app.core.network.createYoofiHttpClient
import ai.yoofi.app.domain.auth.UserSessionStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    /** 环境由阶段推导，`-Pyoofi.api.env` 显式覆盖优先。映射见 [AppEnvironment.forStage]。 */
    @Provides
    @Singleton
    fun provideAppEnvironment(stage: BuildStage): AppEnvironment =
        AppEnvironment.resolve(stage, BuildConfig.API_ENV_OVERRIDE)

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        isLenient = true
    }

    /**
     * 引擎选 OkHttp：Ktor 的多平台 API 之下仍是这套久经考验的连接池与 HTTP/2 实现。
     * 后续海外弱网要加重试 / CDN 降级时，`OkHttp.create { addInterceptor(...) }` 这条口子还在。
     */
    @Provides
    @Singleton
    fun provideHttpClient(
        environment: AppEnvironment,
        json: Json,
        userSessionStore: UserSessionStore,
    ): HttpClient = createYoofiHttpClient(
        engine = OkHttp.create(),
        baseUrl = environment.baseUrl,
        json = json,
        accessTokenProvider = userSessionStore::currentAccessToken,
        enableLogging = BuildConfig.DEBUG,
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindModule {
    @Binds
    @Singleton
    abstract fun bindApiCaller(impl: KtorApiCaller): ApiCaller
}
