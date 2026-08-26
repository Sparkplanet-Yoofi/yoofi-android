package ai.yoofi.app.ui.auth

import ai.yoofi.app.R
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAuthBgTop
import ai.yoofi.app.ui.theme.YoofiAuthError
import ai.yoofi.app.ui.theme.YoofiAuthFocusStroke
import ai.yoofi.app.ui.theme.YoofiAuthIdleButton
import ai.yoofi.app.ui.theme.YoofiStartGameFrom
import ai.yoofi.app.ui.theme.YoofiStartGameTo
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 顶栏 60，对齐 Figma `1761:10039`。 */
internal val AuthHeaderHeight = 60.dp

internal val AuthPageWidth = 350.dp

internal val AuthPillShape = RoundedCornerShape(1000.dp)

internal val AuthFieldShape = RoundedCornerShape(12.dp)

/**
 * 登录页公共底：#190441 → 黑 的垂直渐变 + 顶部紫光。
 * 不画假状态栏。
 */
@Composable
internal fun AuthBackground(
    modifier: Modifier = Modifier,
    showWelcomeTopArt: Boolean = false,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(YoofiAuthBgTop, Color.Black),
                    ),
                ),
        )
        if (showWelcomeTopArt) {
            Image(
                painter = painterResource(R.drawable.img_auth_welcome_top),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(321.dp)
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.Crop,
            )
        }
        Image(
            painter = painterResource(R.drawable.img_auth_glow),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
    }
}

@Composable
internal fun AuthSignUpHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(AuthHeaderHeight),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_auth_back),
            contentDescription = stringResource(R.string.cd_auth_back),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
                .size(24.dp)
                .clickableDismissingIme(onClick = onBack),
        )
        Text(
            text = stringResource(R.string.auth_sign_up_title),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
internal fun AuthNextButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = AuthPillShape
    val background = if (enabled) {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(YoofiStartGameFrom, YoofiStartGameTo),
            ),
            shape,
        )
    } else {
        Modifier.background(YoofiAuthIdleButton, shape)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(shape)
            .then(background)
            .clickableDismissingIme(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.auth_next),
            color = Color.White.copy(alpha = if (enabled) 1f else 0.4f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
internal fun AuthSocialButton(
    label: String,
    @DrawableRes iconRes: Int,
    iconSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(AuthPageWidth)
            .height(46.dp)
            .clip(AuthPillShape)
            .background(YoofiAuthIdleButton)
            .clickableDismissingIme(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

internal fun Modifier.authFieldBorder(error: Boolean, focused: Boolean): Modifier {
    val stroke = when {
        error -> YoofiAuthError
        focused -> YoofiAuthFocusStroke
        else -> Color.Transparent
    }
    return if (stroke == Color.Transparent) {
        this
    } else {
        border(1.dp, stroke, AuthFieldShape)
    }
}
