package com.example.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleMusicAmber
import com.example.ui.theme.AppleMusicCyan
import com.example.ui.theme.AppleMusicGreen
import com.example.ui.theme.AppleMusicRed
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.StudioDarkSurfaceVariant
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongBottomSheet(
    onDismiss: () -> Unit,
    onPickFromDevice: () -> Unit,
    onImportFromYtMusic: () -> Unit,
    onImportFromLink: () -> Unit,
    onCreateManual: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioDarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextDim) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Add New Track / Lyrics",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Option 1: Pick from Device (SAF)
            AddOptionRow(
                icon = Icons.Default.FolderOpen,
                iconTint = AppleMusicCyan,
                title = "استيراد من الهاتف (Import from Device)",
                subtitle = "MP3, M4A, WAV, FLAC with embedded artwork",
                testTag = "btn_sheet_device",
                onClick = {
                    onDismiss()
                    onPickFromDevice()
                }
            )

            // Option 2: YouTube Music Search
            AddOptionRow(
                icon = Icons.Default.MusicNote,
                iconTint = AppleMusicRed,
                title = "استيراد من YouTube Music",
                subtitle = "Search, stream preview, and auto-fetch metadata",
                testTag = "btn_sheet_ytmusic",
                onClick = {
                    onDismiss()
                    onImportFromYtMusic()
                }
            )

            // Option 3: Direct Link / Stream
            AddOptionRow(
                icon = Icons.Default.Link,
                iconTint = AppleMusicGreen,
                title = "استيراد من رابط (Direct URL / Stream)",
                subtitle = "Paste direct audio URL or lyrics link",
                testTag = "btn_sheet_link",
                onClick = {
                    onDismiss()
                    onImportFromLink()
                }
            )

            // Option 4: Manual Lyrics
            AddOptionRow(
                icon = Icons.Default.Edit,
                iconTint = AppleMusicAmber,
                title = "إنشاء كلمات يدوياً (Manual Lyrics)",
                subtitle = "Create without audio or paste TTML / LRC",
                testTag = "btn_sheet_manual",
                onClick = {
                    onDismiss()
                    onCreateManual()
                }
            )
        }
    }
}

@Composable
private fun AddOptionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = StudioDarkSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
