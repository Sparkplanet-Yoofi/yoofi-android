package ai.yoofi.app.ui.settings

import ai.yoofi.app.R
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAuthError
import ai.yoofi.app.ui.theme.YoofiDialogBg
import ai.yoofi.app.ui.theme.YoofiDialogButton
import ai.yoofi.app.ui.theme.YoofiDialogScrim
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DialogShape = RoundedCornerShape(16.dp)
private val ActionShape = RoundedCornerShape(20.dp)

/** Figma `2252:17923`：设置页退出登录确认。 */
@Composable
internal fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(YoofiDialogScrim)
                .clickableDismissingIme(onClick = onDismiss),
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(300.dp)
                .clip(DialogShape)
                .background(YoofiDialogBg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.settings_logout_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LogoutDialogAction(
                    label = stringResource(R.string.auth_cancel),
                    textColor = Color.White,
                    onClick = onDismiss,
                )
                LogoutDialogAction(
                    label = stringResource(R.string.settings_log_out),
                    textColor = YoofiAuthError,
                    onClick = onConfirm,
                )
            }
        }
    }
}

@Composable
private fun LogoutDialogAction(
    label: String,
    textColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(ActionShape)
            .background(YoofiDialogButton)
            .clickableDismissingIme(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
