package ai.yoofi.app.ui.chat

import ai.yoofi.app.R
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiChatObjectivePill
import ai.yoofi.app.ui.theme.YoofiChatObjectiveText
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 顶栏 60dp（状态栏之下）。左退出、中标题+目标胶囊、右音量与六边形设置。
 * 音量两态对应 Figma `1826:9730` 的 Volume_Max / Volume_Off_02。
 */
@Composable
internal fun ChatRoomHeader(
    title: String,
    objective: String,
    volumeMuted: Boolean,
    onExit: () -> Unit,
    onToggleVolume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(60.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chat_exit),
            contentDescription = stringResource(R.string.cd_chat_exit),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
                .size(24.dp)
                .clickableDismissingIme(role = Role.Button, onClick = onExit),
            contentScale = ContentScale.Fit,
        )
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier
                    .background(YoofiChatObjectivePill, RoundedCornerShape(100.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = objective,
                    color = YoofiChatObjectiveText.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                )
                Image(
                    painter = painterResource(R.drawable.ic_chat_caret),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(
                    if (volumeMuted) R.drawable.ic_chat_volume_off else R.drawable.ic_chat_volume,
                ),
                contentDescription = stringResource(
                    if (volumeMuted) R.string.cd_chat_volume_off else R.string.cd_chat_volume,
                ),
                modifier = Modifier
                    .size(24.dp)
                    .clickableDismissingIme(role = Role.Button, onClick = onToggleVolume),
                contentScale = ContentScale.Fit,
            )
            Image(
                painter = painterResource(R.drawable.ic_settings_hex),
                contentDescription = stringResource(R.string.cd_chat_settings),
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
