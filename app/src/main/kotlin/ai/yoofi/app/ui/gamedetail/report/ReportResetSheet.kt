package ai.yoofi.app.ui.gamedetail.report

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
private val ActionShape = RoundedCornerShape(20.dp)
private val ActionHeight = 40.dp

/**
 * 详情页三点菜单，对齐 Figma `2252:18526`。
 * 重置接口未定，确认后由调用方弹 Snackbar。
 */
@Composable
internal fun ReportResetSheet(
    onReset: () -> Unit,
    onReport: () -> Unit,
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
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(SheetShape)
                .background(YoofiDialogBg)
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SheetAction(
                label = stringResource(R.string.detail_start_new_story),
                textColor = Color.White,
                weight = FontWeight.Medium,
                onClick = onReset,
            )
            SheetAction(
                label = stringResource(R.string.report_content),
                textColor = Color.White,
                weight = FontWeight.Medium,
                onClick = onReport,
            )
            SheetAction(
                label = stringResource(R.string.auth_cancel),
                textColor = YoofiAuthError,
                weight = FontWeight.SemiBold,
                onClick = onDismiss,
            )
        }
    }
}

@Composable
private fun SheetAction(
    label: String,
    textColor: Color,
    weight: FontWeight,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ActionHeight)
            .clip(ActionShape)
            .background(YoofiDialogButton)
            .clickableDismissingIme(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = weight,
        )
    }
}
