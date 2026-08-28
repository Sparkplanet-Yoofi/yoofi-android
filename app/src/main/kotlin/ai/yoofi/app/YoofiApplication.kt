package ai.yoofi.app

import ai.yoofi.app.core.config.DataSourceSwitch
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * 应用入口
 *
 * [HiltAndroidApp] 触发 Hilt 生成依赖注入容器，是整个 DI 图的根。
 * 后续多模块化后，各 feature 模块的 Hilt Module 都会挂到这个根组件上。
 *
 * @author JackXu
 */
@HiltAndroidApp
class YoofiApplication : Application() {

    @Inject
    lateinit var dataSourceSwitch: DataSourceSwitch

    override fun onCreate() {
        super.onCreate()
        // 提测 / 上线包若还有接口只有 Demo 实现，在这里立刻崩掉。
        // 宁可打包当天发现，也不能让假数据混进灰度。
        dataSourceSwitch.requireReleaseReady()
    }
}
