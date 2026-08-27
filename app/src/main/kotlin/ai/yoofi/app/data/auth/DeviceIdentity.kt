package ai.yoofi.app.data.auth

import android.content.Context
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 登录请求附带的设备信息；ANDROID_ID 在卸载重装后通常仍保持。
 */
@Singleton
class DeviceIdentity @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    // TODO 更新为服务器接口返回
    fun deviceId(): String =
        Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        ).orEmpty()

    fun deviceModel(): String = Build.MODEL.orEmpty()
}
