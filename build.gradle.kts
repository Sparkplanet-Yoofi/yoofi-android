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
}

// 模块依赖图规则。当前为单模块，规则在模块化落地后才真正生效，
// 此处先建立骨架并验证插件与 Gradle 9 / AGP 9 的兼容性
moduleGraphAssert {
    maxHeight = 4
}