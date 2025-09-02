package ru.scaik.scammaxdisabler.repository

import android.content.Context
import ru.scaik.scammaxdisabler.R
import ru.scaik.scammaxdisabler.model.IconPreset
import ru.scaik.scammaxdisabler.model.IconPresetId

class IconPresetRepository {

    fun getAllPresets(context: Context): List<IconPreset> {
        return listOf(
            IconPreset(
                id = IconPresetId.DEFAULT.value,
                displayName = context.getString(R.string.icon_preset_default),
                iconResourceName = "ic_launcher",
                isDefault = true
            ),
            IconPreset(
                id = IconPresetId.GALLERY.value,
                displayName = context.getString(R.string.icon_preset_gallery),
                iconResourceName = "ic_launcher_preset_gallery",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.FILES.value,
                displayName = context.getString(R.string.icon_preset_files),
                iconResourceName = "ic_launcher_preset_files",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.SETTINGS.value,
                displayName = context.getString(R.string.icon_preset_settings),
                iconResourceName = "ic_launcher_preset_settings",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.CAMERA1.value,
                displayName = context.getString(R.string.icon_preset_camera1),
                iconResourceName = "ic_launcher_preset_camera1",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.CAMERA2.value,
                displayName = context.getString(R.string.icon_preset_camera2),
                iconResourceName = "ic_launcher_preset_camera2",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.CAMERA3.value,
                displayName = context.getString(R.string.icon_preset_camera3),
                iconResourceName = "ic_launcher_preset_camera3",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.VPN.value,
                displayName = context.getString(R.string.icon_preset_vpn),
                iconResourceName = "ic_launcher_preset_vpn",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.ONE.value,
                displayName = context.getString(R.string.icon_preset_one),
                iconResourceName = "ic_launcher_preset_one",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.BLOCKBLAST.value,
                displayName = context.getString(R.string.icon_preset_blockblast),
                iconResourceName = "ic_launcher_preset_blockblast",
                isDefault = false
            ),

            IconPreset(
                id = IconPresetId.STANDOFF.value,
                displayName = context.getString(R.string.icon_preset_standoff),
                iconResourceName = "ic_launcher_preset_standoff",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.WALLKICKERS.value,
                displayName = context.getString(R.string.icon_preset_wallkickers),
                iconResourceName = "ic_launcher_preset_wallkickers",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.GMAIL.value,
                displayName = context.getString(R.string.icon_preset_gmail),
                iconResourceName = "ic_launcher_preset_gmail",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.FLAPPYBIRD.value,
                displayName = context.getString(R.string.icon_preset_flappybird),
                iconResourceName = "ic_launcher_preset_flappybird",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.VK.value,
                displayName = context.getString(R.string.icon_preset_vk),
                iconResourceName = "ic_launcher_preset_vk",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.YANDEXMUSIC.value,
                displayName = context.getString(R.string.icon_preset_yandexmusic),
                iconResourceName = "ic_launcher_preset_yandexmusic",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.MUSIC.value,
                displayName = context.getString(R.string.icon_preset_music),
                iconResourceName = "ic_launcher_preset_music",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.SPOTIFY.value,
                displayName = context.getString(R.string.icon_preset_spotify),
                iconResourceName = "ic_launcher_preset_spotify",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.YANDEXBROWSER.value,
                displayName = context.getString(R.string.icon_preset_yandexbrowser),
                iconResourceName = "ic_launcher_preset_yandexbrowser",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.CHROME.value,
                displayName = context.getString(R.string.icon_preset_chrome),
                iconResourceName = "ic_launcher_preset_chrome",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.CALC1.value,
                displayName = context.getString(R.string.icon_preset_calc1),
                iconResourceName = "ic_launcher_preset_calc1",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.CALC2.value,
                displayName = context.getString(R.string.icon_preset_calc2),
                iconResourceName = "ic_launcher_preset_calc2",
                isDefault = false
            ),
            IconPreset(
                id = IconPresetId.CALC3.value,
                displayName = context.getString(R.string.icon_preset_calc3),
                iconResourceName = "ic_launcher_preset_calc3",
                isDefault = false
            ),
        )
    }
}
