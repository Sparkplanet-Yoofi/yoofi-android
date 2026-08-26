package ai.yoofi.app.ui.auth

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.YoofiAccent
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 欢迎页，对齐 Figma `1761:9969` 登录/注册-样式0。
 * Google 当前为 UI 演示入口，直接视为已登录。
 */
@Composable
internal fun WelcomeScreen(
    onGoogle: () -> Unit,
    onEmail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AuthBackground(showWelcomeTopArt = true)
        Image(
            painter = painterResource(R.drawable.img_auth_logo),
            contentDescription = stringResource(R.string.cd_auth_logo),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 461.dp)
                .size(150.dp, 45.dp),
            contentScale = ContentScale.Fit,
        )
        AuthSocialButton(
            label = stringResource(R.string.auth_continue_google),
            iconRes = R.drawable.ic_auth_google,
            iconSize = 16.dp,
            onClick = onGoogle,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 570.dp),
        )
        Text(
            text = stringResource(R.string.auth_or),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 694.dp),
        )
        Text(
            text = stringResource(R.string.auth_sign_up_email),
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 722.dp)
                .clickable(role = Role.Button, onClick = onEmail),
        )
        WelcomeLegal(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = 766.dp)
                .navigationBarsPadding(),
        )
    }
}

@Composable
private fun WelcomeLegal(modifier: Modifier = Modifier) {
    val terms = stringResource(R.string.auth_terms)
    val and = stringResource(R.string.auth_legal_and)
    val privacy = stringResource(R.string.auth_privacy)
    val links = buildAnnotatedString {
        withStyle(SpanStyle(color = YoofiAccent)) { append(terms) }
        append(" ")
        withStyle(SpanStyle(color = Color.White.copy(alpha = 0.5f))) { append(and) }
        append(" ")
        withStyle(SpanStyle(color = YoofiAccent)) { append(privacy) }
        append(" ")
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.auth_legal_prefix),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(start = 59.dp),
        )
        Text(
            text = links,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(start = 94.dp, top = 5.dp),
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WelcomeScreenPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        WelcomeScreen(onGoogle = {}, onEmail = {})
    }
}
