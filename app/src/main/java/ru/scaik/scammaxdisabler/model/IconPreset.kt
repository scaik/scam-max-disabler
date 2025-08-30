package ru.scaik.scammaxdisabler.model

data class IconPreset(
        val id: String,
        val displayName: String,
        val iconResourceName: String,
        val isDefault: Boolean = false
)

enum class IconPresetId(val value: String) {
    DEFAULT("default"),
    GALLERY("gallery"),
    FILES("files"),
    SETTINGS("settings"),
    CAMERA1("camera1"),
    CAMERA2("camera2"),
    CAMERA3("camera3"),
    VPN("vpn"),
    ONE("one"),
    BLOCKBLAST("blockblast"),
    STANDOFF("standoff"),
    WALLKICKERS("wallkickers"),
    GMAIL("gmail"),
    FLAPPYBIRD("flappybird"),
    VK("vk"),
    YANDEXMUSIC("yandexmusic"),
    MUSIC("music"),
    SPOTIFY("spotify"),
    YANDEXBROWSER("yandexbrowser"),
    CHROME("chrome"),
    CALC1("calc1"),
    CALC2("calc2"),
    CALC3("calc3"),
}
