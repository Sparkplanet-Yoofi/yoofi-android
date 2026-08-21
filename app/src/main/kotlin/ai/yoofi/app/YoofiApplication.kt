package ai.yoofi.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用入口
 *
 * [HiltAndroidApp] 触发 Hilt 生成依赖注入容器，是整个 DI 图的根。
 * 后续多模块化后，各 feature 模块的 Hilt Module 都会挂到这个根组件上。
 *
 * @author JackXu
 */
@HiltAndroidApp
class YoofiApplication : Application()
