package ai.yoofi.app.ui.profile

import ai.yoofi.app.R
import androidx.annotation.DrawableRes

/**
 * 资料卡上已经格式化好的展示字段。
 * 计数文案由调用方（或以后的 UseCase）定口径，这里不做本地换算。
 */
internal data class ProfileIdentity(
    val displayName: String,
    val publicId: String,
    val followingCount: String,
    val followerCount: String,
    @param:DrawableRes val avatarRes: Int,
)

/** 客态入口带来的 avatarKey；接 CDN 后这里改成 Coil，调用方仍只传 key。 */
internal fun profileAvatarRes(avatarKey: String): Int = when (avatarKey) {
    "avatar-author" -> R.drawable.img_detail_avatar_author
    "avatar-1" -> R.drawable.img_detail_avatar_1
    "avatar-2" -> R.drawable.img_detail_avatar_2
    "avatar-3" -> R.drawable.img_detail_avatar_3
    else -> R.drawable.img_me_avatar
}
