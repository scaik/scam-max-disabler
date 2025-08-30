package ru.scaik.scammaxdisabler.ui.components

import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import ru.scaik.scammaxdisabler.R
import ru.scaik.scammaxdisabler.model.IconPreset
import ru.scaik.scammaxdisabler.ui.theme.SkyBlueAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconSelectorSheet(
    context: Context,
    isVisible: Boolean,
    presets: List<IconPreset>,
    selectedPresetId: String,
    isLoading: Boolean,
    onPresetSelected: (IconPreset) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var localSelectedId by remember(selectedPresetId) { mutableStateOf(selectedPresetId) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1A1A1A),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = context.getString(R.string.icon_selector_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                items(presets) { preset ->
                    IconPresetItem(
                        context,
                        preset = preset,
                        isSelected = localSelectedId == preset.id,
                        onSelect = { localSelectedId = preset.id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    presets.find { it.id == localSelectedId }?.let { onPresetSelected(it) }
                },
                enabled = !isLoading && localSelectedId != selectedPresetId,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = SkyBlueAccent,
                        disabledContainerColor = Color.Gray
                    ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = context.getString(R.string.icon_selector_apply),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun IconPresetItem(
    context: Context,
    preset: IconPreset,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor by
    animateColorAsState(
        targetValue =
            if (isSelected) SkyBlueAccent.copy(alpha = 0.2f) else Color.Transparent,
        animationSpec = tween(300),
        label = "backgroundAnimation"
    )

    val borderColor by
    animateColorAsState(
        targetValue = if (isSelected) SkyBlueAccent else Color.Gray.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "borderAnimation"
    )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onSelect() }
                .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val iconResourceId = getIconResourceId(preset.iconResourceName)
                val adaptiveIcon =
                    ContextCompat.getDrawable(context, iconResourceId) as? AdaptiveIconDrawable
                val bitmapPainter = if (adaptiveIcon != null) {
                    BitmapPainter(adaptiveIcon.toBitmap().asImageBitmap())
                } else {
                    painterResource(iconResourceId)
                }

                Image(
                    painter = bitmapPainter,
                    contentDescription = preset.displayName,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = preset.displayName,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = if (isSelected) Color.White else Color.Gray,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun getIconResourceId(resourceName: String): Int {
    return when (resourceName) {
        "ic_launcher" -> R.mipmap.ic_launcher
        "ic_launcher_preset_gallery" -> R.mipmap.ic_launcher_preset_gallery
        "ic_launcher_preset_files" -> R.mipmap.ic_launcher_preset_files
        "ic_launcher_preset_settings" -> R.mipmap.ic_launcher_preset_settings
        "ic_launcher_preset_camera1" -> R.mipmap.ic_launcher_preset_camera1
        "ic_launcher_preset_camera2" -> R.mipmap.ic_launcher_preset_camera2
        "ic_launcher_preset_camera3" -> R.mipmap.ic_launcher_preset_camera3
        "ic_launcher_preset_vpn" -> R.mipmap.ic_launcher_preset_vpn
        "ic_launcher_preset_one" -> R.mipmap.ic_launcher_preset_one
        "ic_launcher_preset_blockblast" -> R.mipmap.ic_launcher_preset_blockblast
        "ic_launcher_preset_standoff" -> R.mipmap.ic_launcher_preset_standoff
        "ic_launcher_preset_wallkickers" -> R.mipmap.ic_launcher_preset_wallkickers
        "ic_launcher_preset_gmail" -> R.mipmap.ic_launcher_preset_gmail
        "ic_launcher_preset_flappybird" -> R.mipmap.ic_launcher_preset_flappybird
        "ic_launcher_preset_vk" -> R.mipmap.ic_launcher_preset_vk
        "ic_launcher_preset_yandexmusic" -> R.mipmap.ic_launcher_preset_yandexmusic
        "ic_launcher_preset_music" -> R.mipmap.ic_launcher_preset_music
        "ic_launcher_preset_spotify" -> R.mipmap.ic_launcher_preset_spotify
        "ic_launcher_preset_yandexbrowser" -> R.mipmap.ic_launcher_preset_yandexbrowser
        "ic_launcher_preset_chrome" -> R.mipmap.ic_launcher_preset_chrome
        "ic_launcher_preset_calc1" -> R.mipmap.ic_launcher_preset_calc1
        "ic_launcher_preset_calc2" -> R.mipmap.ic_launcher_preset_calc2
        "ic_launcher_preset_calc3" -> R.mipmap.ic_launcher_preset_calc3
        else -> R.mipmap.ic_launcher
    }
}
