package ai.yoofi.app.ui.settings.linked

import ai.yoofi.app.R
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAuthError
import ai.yoofi.app.ui.theme.YoofiDialogBg
import ai.yoofi.app.ui.theme.YoofiDialogButton
import ai.yoofi.app.ui.theme.YoofiDialogScrim
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
private val DialogPad = 24.dp

/** Figma `2252:17312`：双账号解绑确认。 */
@Composable
internal fun UnlinkConfirmDialog(
    providerLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    LinkedAccountDialogScaffold(onDismiss = onDismiss) {
        DialogCopy(
            body = stringResource(R.string.settings_linked_unlink_body, providerLabel),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DialogPad),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DialogAction(
                label = stringResource(R.string.auth_cancel),
                textColor = Color.White,
                onClick = onDismiss,
            )
            DialogAction(
                label = stringResource(R.string.settings_linked_unlink),
                textColor = YoofiAuthError,
                onClick = onConfirm,
            )
        }
    }
}

/** Figma `2252:17254`：只剩一种登录方式时不可解绑。 */
@Composable
internal fun LastAccountDialog(
    onDismiss: () -> Unit,
) {
    LinkedAccountDialogScaffold(onDismiss = onDismiss) {
        DialogCopy(body = stringResource(R.string.settings_linked_last_body))
        DialogAction(
            label = stringResource(R.string.settings_linked_ok),
            textColor = Color.White,
            onClick = onDismiss,
            modifier = Modifier.padding(DialogPad),
        )
    }
}

@Composable
private fun LinkedAccountDialogScaffold(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
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
                .background(YoofiDialogBg)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {},
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
        }
    }
}

@Composable
private fun DialogCopy(body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = DialogPad, end = DialogPad, top = DialogPad),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DialogTitle()
        Text(
            text = body,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )
    }
}

@Composable
private fun DialogTitle() {
    Text(
        text = stringResource(R.string.settings_linked_unlink_title),
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DialogAction(
    label: String,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
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
