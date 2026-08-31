package ai.yoofi.app.ui.me

import ai.yoofi.app.R
import ai.yoofi.app.domain.profile.MineProfilePresence
import ai.yoofi.app.ui.profile.ProfileCreationsPane
import ai.yoofi.app.ui.profile.ProfileIdentity
import ai.yoofi.app.ui.profile.ProfileLorebookEmptyPane
import ai.yoofi.app.ui.profile.ProfileStat
import ai.yoofi.app.ui.profile.ProfileWorkKind
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * 「我的」主态 / 空态的可变策略。
 * 差异只写在实现里，[MeLayout] 禁止再写 `if (vacant)`。
 * 客态不实现本接口——它是另一块 Screen，不要用策略把主客缠回去。
 */
internal sealed interface MineProfileStrategy {
    @get:StringRes
    val pencilCdRes: Int

    @Composable
    fun identity(publicId: String): ProfileIdentity

    fun onPencil(
        onEditProfile: () -> Unit,
        onSetupProfile: () -> Unit,
    ): () -> Unit

    fun wrapIdTrailing(
        copy: @Composable () -> Unit,
    ): (@Composable () -> Unit)?

    @Composable
    fun Creations(
        workKind: ProfileWorkKind,
        onWorkKindChange: (ProfileWorkKind) -> Unit,
        modifier: Modifier,
    )
}

internal fun MineProfilePresence.strategy(): MineProfileStrategy = when (this) {
    MineProfilePresence.Vacant -> VacantMineStrategy
    MineProfilePresence.Populated -> PopulatedMineStrategy
}

/**
 * 「我的」空态 的策略实现
 */
internal data object VacantMineStrategy : MineProfileStrategy {
    override val pencilCdRes: Int = R.string.cd_complete_profile

    @Composable
    override fun identity(publicId: String): ProfileIdentity {
        val zero = stringResource(R.string.me_stat_zero)
        return ProfileIdentity(
            displayName = stringResource(R.string.me_nickname_placeholder),
            publicId = null,
            avatarRes = null,
            showFanBadge = false,
            stats = listOf(
                ProfileStat(count = zero, label = stringResource(R.string.me_stat_create)),
                ProfileStat(count = zero, label = stringResource(R.string.me_stat_favorite)),
                ProfileStat(count = zero, label = stringResource(R.string.me_stat_follow)),
            ),
        )
    }

    override fun onPencil(
        onEditProfile: () -> Unit,
        onSetupProfile: () -> Unit,
    ): () -> Unit = onSetupProfile

    override fun wrapIdTrailing(
        copy: @Composable () -> Unit,
    ): (@Composable () -> Unit)? = null

    @Composable
    override fun Creations(
        workKind: ProfileWorkKind,
        onWorkKindChange: (ProfileWorkKind) -> Unit,
        modifier: Modifier,
    ) {
        ProfileLorebookEmptyPane(modifier)
    }
}

/**
 * 「我的」主态 的策略实现
 */
internal data object PopulatedMineStrategy : MineProfileStrategy {
    override val pencilCdRes: Int = R.string.cd_edit_profile

    @Composable
    override fun identity(publicId: String): ProfileIdentity = ProfileIdentity(
        displayName = stringResource(R.string.me_display_name),
        publicId = publicId,
        avatarRes = R.drawable.img_me_avatar,
        stats = listOf(
            ProfileStat(
                count = stringResource(R.string.me_following_count),
                label = stringResource(R.string.me_following_label),
            ),
            ProfileStat(
                count = stringResource(R.string.me_follower_count),
                label = stringResource(R.string.me_follower_label),
            ),
        ),
    )

    override fun onPencil(
        onEditProfile: () -> Unit,
        onSetupProfile: () -> Unit,
    ): () -> Unit = onEditProfile

    override fun wrapIdTrailing(
        copy: @Composable () -> Unit,
    ): (@Composable () -> Unit)? = copy

    @Composable
    override fun Creations(
        workKind: ProfileWorkKind,
        onWorkKindChange: (ProfileWorkKind) -> Unit,
        modifier: Modifier,
    ) {
        ProfileCreationsPane(
            workKind = workKind,
            onWorkKindChange = onWorkKindChange,
            modifier = modifier,
        )
    }
}
