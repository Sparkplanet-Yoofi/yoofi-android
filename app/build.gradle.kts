plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// 构建阶段。全项目这三档只有一套叫法：development / staging / production，
// 与 BuildStage、AppEnvironment、文档完全一致，不要再引入 qa / release 等同义词。
// development 允许 Demo 数据源，staging / production 强制真实接口。
// debug 默认 development，release 默认 production。打提测包：
//   ./gradlew assembleRelease -Pyoofi.stage=staging
// 拼错参数直接失败，不静默退回默认值——否则「以为在打提测包，实际打出上线包」。
fun resolveBuildStage(default: String): String {
    val override = project.findProperty("yoofi.stage") as? String ?: return default
    require(override in setOf("development", "staging", "production")) {
        "yoofi.stage 只接受 development / staging / production，收到：$override"
    }
    return override
}

// 环境（Base URL）默认由阶段推导，映射唯一定义在 AppEnvironment.forStage，
// 构建脚本只把显式覆盖透传过去，不在这里重复一份映射逻辑。
// 需要偏离默认时才传，例如用 debug 包排查线上问题：
//   ./gradlew assembleDebug -Pyoofi.api.env=production
fun apiEnvOverride(): String {
    val override = project.findProperty("yoofi.api.env") as? String ?: return ""
    require(override in setOf("staging", "production")) {
        "yoofi.api.env 只接受 staging / production，收到：$override"
    }
    return override
}

// release 签名凭据全部来自环境变量，由 Jenkins Credentials 在构建时注入，
// 确保密钥文件与口令永不进入版本库。
// 需要的环境变量：
//   YOOFI_KEYSTORE_PATH      keystore 文件的绝对路径
//   YOOFI_KEYSTORE_PASSWORD  keystore 口令
//   YOOFI_KEY_ALIAS          密钥别名
//   YOOFI_KEY_PASSWORD       密钥口令
val releaseKeystorePath: String? = System.getenv("YOOFI_KEYSTORE_PATH")
// 仅当路径已配置且文件确实存在时才启用签名，避免路径错误时
// 拖到 packageRelease 阶段才暴露问题
val hasReleaseKeystore: Boolean =
    !releaseKeystorePath.isNullOrBlank() && file(releaseKeystorePath).exists()

// 路径已配置却找不到文件，通常意味着 Jenkins 凭据注入失败。
// 此时构建会退化为未签名产物，必须在日志中显式告警，避免静默产出不可用的包
if (!releaseKeystorePath.isNullOrBlank() && !hasReleaseKeystore) {
    logger.warn("警告：环境变量 YOOFI_KEYSTORE_PATH 指向 $releaseKeystorePath，但该文件不存在，release 将产出未签名 APK")
}

android {
    namespace = "ai.yoofi.app"
    compileSdk {
        version = release(36)
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = System.getenv("YOOFI_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("YOOFI_KEY_ALIAS")
                keyPassword = System.getenv("YOOFI_KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "ai.yoofi.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "BUILD_STAGE",
                "\"${resolveBuildStage(default = "development")}\"",
            )
            buildConfigField("String", "API_ENV_OVERRIDE", "\"${apiEnvOverride()}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 环境变量齐备时启用正式签名；本地开发缺少变量时退化为未签名产物，
            // 保证日常构建不被阻塞
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
            buildConfigField(
                "String",
                "BUILD_STAGE",
                "\"${resolveBuildStage(default = "production")}\"",
            )
            buildConfigField("String", "API_ENV_OVERRIDE", "\"${apiEnvOverride()}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Hilt 依赖注入：注解处理由 KSP 完成（不再使用已进入维护态的 KAPT）
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.android.image.cropper)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.serialization.kotlinx.json)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}