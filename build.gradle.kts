// Top-level build file where you can add configuration options common to all sub-projects/modules.
// 注意：AGP 9 内置 Kotlin 支持（对 KGP 2.2.10 有运行时依赖），
// 因此不再声明 org.jetbrains.kotlin.android 插件——它与 AGP 9 的新 DSL 不兼容。
// KSP 版本必须与 AGP 内置的 KGP 2.2.10 对齐，否则配置阶段报不兼容。
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // KSP：注解处理器（Hilt / Room 均依赖），版本与 Kotlin 严格绑定
    alias(libs.plugins.ksp) apply false
    // Hilt：依赖注入，2.59+ 才支持 AGP 9
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    // 架构守卫：依赖声明卫生检查（./gradlew buildHealth）
    alias(libs.plugins.dependency.analysis)
    // 架构守卫：模块依赖图断言（./gradlew assertModuleGraph）
    alias(libs.plugins.module.graph.assert)
    // 提供根项目的 clean / assemble / check 生命周期任务。
    // 与 yoofi-shared 根项目保持一致，避免 IDE 按完整路径请求 `:clean` 时落空。
    base
}

// 模块依赖图规则。当前为单模块，规则在模块化落地后才真正生效，
// 此处先建立骨架并验证插件与 Gradle 9 / AGP 9 的兼容性
moduleGraphAssert {
    maxHeight = 4
}

// ============================================================
// 双仓版本对齐守卫
//
// yoofi-android 与 yoofi-shared 是两个独立仓库，各有一份版本目录。
// 两者的 Kotlin / Ktor / serialization / coroutines 必须严格一致，否则：
//   - Ktor 版本漂移 → shared 的 iOS klib 与编译器 ABI 错配，iOS 直接编不过（见 .ai/kmp.md §3.3）
//   - Kotlin 版本漂移 → klib ABI 与 metadata 双重不兼容
// 这类问题在纯 Android 构建里**完全无感**，只在 iOS/Wasm 编译时爆炸，
// 靠人工约定必然失守，因此固化成可执行检查。
//
// 用法：./gradlew checkSharedVersionAlignment
// 已挂到 check 上，CI 跑 check 即自动覆盖。
// ============================================================
val sharedVersionKeyMapping = mapOf(
    // Android 侧键名 to shared 侧键名（两边命名不完全相同）
    "kotlin" to "kotlin",
    "ktor" to "ktor",
    "kotlinxSerializationJson" to "kotlinxSerializationJson",
    "kotlinxCoroutinesTest" to "kotlinxCoroutines",
)

tasks.register("checkSharedVersionAlignment") {
    group = "verification"
    description = "校验 yoofi-android 与 yoofi-shared 的关键依赖版本严格一致"

    val androidToml = layout.projectDirectory.file("gradle/libs.versions.toml").asFile
    val sharedTomlPath = (providers.gradleProperty("yoofi.shared.dir").orNull ?: "../yoofi-shared")
    val sharedToml = file("$sharedTomlPath/gradle/libs.versions.toml")
    val mapping = sharedVersionKeyMapping

    inputs.file(androidToml)
    outputs.upToDateWhen { false }

    doLast {
        if (!sharedToml.exists()) {
            logger.lifecycle("未找到 yoofi-shared 版本目录（$sharedToml），跳过对齐校验")
            return@doLast
        }

        // 只解析 [versions] 段的 key = "value"，忽略注释与其余段落
        fun parseVersions(f: File): Map<String, String> {
            val result = linkedMapOf<String, String>()
            var inVersions = false
            f.readLines().forEach { raw ->
                val line = raw.substringBefore('#').trim()
                when {
                    line.startsWith("[") -> inVersions = line == "[versions]"
                    inVersions && line.contains("=") -> {
                        val key = line.substringBefore('=').trim()
                        val value = line.substringAfter('=').trim().trim('"')
                        if (key.isNotEmpty() && value.isNotEmpty()) result[key] = value
                    }
                }
            }
            return result
        }

        val androidVersions = parseVersions(androidToml)
        val sharedVersions = parseVersions(sharedToml)

        val mismatches = mapping.mapNotNull { (androidKey, sharedKey) ->
            val a = androidVersions[androidKey]
            val s = sharedVersions[sharedKey]
            when {
                a == null -> "缺失：yoofi-android 的 [versions] 没有 $androidKey"
                s == null -> "缺失：yoofi-shared 的 [versions] 没有 $sharedKey"
                a != s -> "不一致：$androidKey=$a (android) ≠ $sharedKey=$s (shared)"
                else -> null
            }
        }

        if (mismatches.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("双仓版本不一致，会导致 shared 的 iOS/Wasm 目标编译失败：")
                    mismatches.forEach { appendLine("  - $it") }
                    appendLine()
                    appendLine("修复：让两仓的 gradle/libs.versions.toml 取相同值，改完重跑本任务。")
                    appendLine("升级 Ktor 前务必先读 .ai/kmp.md §3.3（klib ABI 上限）。")
                },
            )
        }
        logger.lifecycle("✅ 双仓版本对齐：${mapping.keys.joinToString()}")
    }
}