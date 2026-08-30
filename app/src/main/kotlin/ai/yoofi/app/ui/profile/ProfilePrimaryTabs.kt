package ai.yoofi.app.ui.profile

import ai.yoofi.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ProfilePrimaryTabs(
    selected: ProfilePrimaryTab,
    onSelected: (ProfilePrimaryTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ProfilePrimaryTabItem(
            label = stringResource(R.string.me_tab_lorebook),
            selected = selected == ProfilePrimaryTab.Lorebook,
            onClick = { onSelected(ProfilePrimaryTab.Lorebook) },
        )
        ProfilePrimaryTabItem(
            label = stringResource(R.string.me_tab_creations),
            selected = selected == ProfilePrimaryTab.Creations,
            onClick = { onSelected(ProfilePrimaryTab.Creations) },
        )
    }
}

@Composable
private fun ProfilePrimaryTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.4f),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(16.dp, 4.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(if (selected) Color.White else Color.Transparent),
        )
    }
}
