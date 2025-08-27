package ru.scaik.scammaxdisabler.domain

data class AppState(
    val blockerEnabled: Boolean = false,
    val accessibilityGranted: Boolean = false,
    val overlayGranted: Boolean = false,
    val batteryExempted: Boolean = false,
    val serviceHealthy: Boolean = false,
    val canToggle: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdate: Long = 0L
) {
    val allPermissionsGranted: Boolean
        get() = accessibilityGranted && overlayGranted
}
