package ai.yoofi.app.ui.settings.blocked

import ai.yoofi.app.R
import ai.yoofi.app.domain.block.BlockedUser
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAccent
import ai.yoofi.app.ui.theme.YoofiDialogBg
import ai.yoofi.app.ui.theme.YoofiDialogButton
import ai.yoofi.app.ui.theme.YoofiDialogScrim
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DialogShape = RoundedCornerShape(16.dp)
private val ActionShape = RoundedCornerShape(20.dp)
private val ToastShape = RoundedCornerShape(8.dp)
private val DialogPad = 24.dp
private val ActionHeight = 40.dp
private val ToastFill = Color(0xFF302C55)

/** Figma `2252:17548`：解除拉黑确认。 */
@Composable
internal fun UnblockConfirmDialog(
    user: BlockedUser,
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
                .background(YoofiDialogBg)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {},
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.settings_blocked_confirm_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = DialogPad, end = DialogPad, top = DialogPad),
            )
            Spacer(Modifier.height(24.dp))
            Image(
                painter = painterResource(blockedAvatarRes(user.avatarKey)),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .border(1.5.dp, YoofiAccent, CircleShape)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = user.displayName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.settings_blocked_confirm_body),
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DialogPad),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DialogPad),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DialogAction(
                    label = stringResource(R.string.settings_blocked_unblock),
                    onClick = onConfirm,
                )
                DialogAction(
                    label = stringResource(R.string.auth_cancel),
                    onClick = onDismiss,
                )
            }
        }
    }
}

/** Figma `2252:17465`：解除拉黑成功 Toast。 */
@Composable
internal fun UserUnblockedToast(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(ToastFill, ToastShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_blocked_check),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = stringResource(R.string.settings_blocked_unblocked),
            color = Color.White,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun DialogAction(
    label: String,
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
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
