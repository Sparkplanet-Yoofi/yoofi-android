package ai.yoofi.app.ui.create

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.PaytoneOne
import ai.yoofi.app.ui.theme.YoofiGameBg0
import ai.yoofi.app.ui.theme.YoofiGameBg1
import ai.yoofi.app.ui.theme.YoofiTitleGradientEnd
import ai.yoofi.app.ui.theme.YoofiTitleGradientStart
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Create Tab 壳。开发页无独立画板，正文等 1.01 `1465:19336`。
 */
@Composable
fun CreateScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(YoofiGameBg0, YoofiGameBg1)))
            .padding(bottom = 120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.create_placeholder),
            style = TextStyle(
                fontFamily = PaytoneOne,
                fontSize = 32.sp,
                brush = Brush.linearGradient(
                    listOf(YoofiTitleGradientStart, YoofiTitleGradientEnd),
                ),
            ),
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF131126)
@Composable
private fun CreateScreenPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        CreateScreen()
    }
}
