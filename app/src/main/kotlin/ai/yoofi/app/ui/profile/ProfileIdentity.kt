package ai.yoofi.app.ui.profile

import ai.yoofi.app.R
import androidx.annotation.DrawableRes

/**
 * 资料卡一行计数。空态是 Create / Favorite / Follow，主客态仍是 Following / Follower，
 * 口径由调用方决定，卡片只负责排版。
 */
internal data class ProfileStat(
    val count: String,
    val label: String,
)

/**
 * 资料卡上已经格式化好的展示字段。
 * [avatarRes] 为空走占位脸；[publicId] 为空不展示编号和复制。
 */
internal data class ProfileIdentity(
    val displayName: String,
    val publicId: String?,
    val stats: List<ProfileStat>,
    @param:DrawableRes val avatarRes: Int?,
    val showFanBadge: Boolean = true,
    val showIdBadge: Boolean = true,
)

/** 客态入口带来的 avatarKey；接 CDN 后这里改成 Coil，调用方仍只传 key。 */
internal fun profileAvatarRes(avatarKey: String): Int = when (avatarKey) {
    "avatar-author" -> R.drawable.img_detail_avatar_author
    "avatar-1" -> R.drawable.img_detail_avatar_1
    "avatar-2" -> R.drawable.img_detail_avatar_2
    "avatar-3" -> R.drawable.img_detail_avatar_3
    else -> R.drawable.img_me_avatar
}
