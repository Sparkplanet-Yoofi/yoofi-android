pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
// yoofi-shared 的接入模式，用 -Pyoofi.shared.mode 切换（详见 .ai/kmp.md §12.2）：
//   source（默认）：Composite Build 源码依赖。改 shared 立即生效，不用发版，开发期用。
//   binary        ：走 Maven 仓库的已发布产物。版本锁定、可复现，出包和回溯历史版本时用。
// 两种模式下 app 的依赖声明完全相同（libs.yoofi.shared），只有解析目标不同。
val sharedMode = providers.gradleProperty("yoofi.shared.mode").orNull ?: "source"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 二进制模式专用：私服就绪前，先用 publishToMavenLocal 的产物承接。
        // 源码模式不加 mavenLocal，避免本地陈旧产物意外污染日常构建。
        if (sharedMode == "binary") mavenLocal()
    }
}

rootProject.name = "yoofi-android"
include(":app")

// 路径可用 -Pyoofi.shared.dir 覆盖；目录不存在时自动退化为纯 Android 构建，
// 保证只克隆了本仓库的同事和 CI 仍能编译。
val sharedDir = (providers.gradleProperty("yoofi.shared.dir").orNull ?: "../yoofi-shared")
    .let(::file)
when {
    sharedMode == "binary" ->
        logger.lifecycle("yoofi-shared：二进制模式，按版本目录里的 yoofiShared 版本解析")

    sharedDir.resolve("settings.gradle.kts").exists() -> includeBuild(sharedDir)

    else -> logger.lifecycle("未找到 yoofi-shared（$sharedDir），本次为纯 Android 构建")
}
 