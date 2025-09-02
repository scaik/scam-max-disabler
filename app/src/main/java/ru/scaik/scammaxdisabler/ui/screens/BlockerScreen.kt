package ru.scaik.scammaxdisabler.ui.screens

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FileDownloadOff
import androidx.compose.material.icons.filled.Layers
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.launch
import ru.scaik.scammaxdisabler.di.AppComponent
import ru.scaik.scammaxdisabler.ui.components.BlockerView
import ru.scaik.scammaxdisabler.ui.components.BlockerViewState
import ru.scaik.scammaxdisabler.ui.components.IconSelectorSheet
import ru.scaik.scammaxdisabler.ui.components.Permission
import ru.scaik.scammaxdisabler.utils.BlockerUtils

@Composable
fun BlockerScreen(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val blockerStateManager = AppComponent.blockerStateManager
    val iconPresetStateManager = AppComponent.iconPresetStateManager
    val iconPresetRepository = AppComponent.iconPresetRepository
    val appIconManager = AppComponent.appIconManager

    var isActiveBlockingEnabled by remember {
        mutableStateOf(blockerStateManager.isBlockerEnabled())
    }
    var isInstallationBlockingEnabled by remember {
        mutableStateOf(blockerStateManager.isInstallationBlockingEnabled())
    }
    var showInstallationBlocking by remember {
        mutableStateOf(blockerStateManager.isInstallationBlockingMode())
    }
    var isInstallationOperationInProgress by remember { mutableStateOf(false) }
    var hasInstallPermission by remember {
        mutableStateOf(BlockerUtils.canRequestPackageInstalls(context))
    }
    var showIconSelector by remember { mutableStateOf(false) }
    var isIconChangeInProgress by remember { mutableStateOf(false) }
    val selectedPresetId by iconPresetStateManager.selectedPresetId.collectAsState()
    var hasAccessibilityPermission by remember {
        mutableStateOf(BlockerUtils.checkAccessibilityPermission(context))
    }
    var hasOverlayPermission by remember {
        mutableStateOf(BlockerUtils.checkOverlayPermission(context))
    }
    var isBatteryOptimized by remember {
        mutableStateOf(BlockerUtils.isBatteryOptimizationIgnored(context))
    }

    val canToggleActiveBlocking = hasAccessibilityPermission && hasOverlayPermission
    val canToggleInstallationBlocking = true // Installation blocking doesn't require permissions

    ObserveLifecycleEvents(
        context = context,
        onResume = {
            hasAccessibilityPermission = BlockerUtils.checkAccessibilityPermission(context)
            hasOverlayPermission = BlockerUtils.checkOverlayPermission(context)
            isBatteryOptimized = BlockerUtils.isBatteryOptimizationIgnored(context)

            if (!hasAccessibilityPermission) {
                isActiveBlockingEnabled = false
                blockerStateManager.setBlockerEnabled(false)
            }
            if (hasAccessibilityPermission && blockerStateManager.isBlockerEnabled()) {
                BlockerUtils.startWarmUpService(context)
            }

            // Check installation blocking state and sync with actual package state
            val isPackageInstalled = BlockerUtils.isTargetPackageInstalled(context)
            if (!isPackageInstalled && isInstallationBlockingEnabled) {
                isInstallationBlockingEnabled = false
                blockerStateManager.setInstallationBlockingEnabled(false)
            }

            // Refresh install permission status
            hasInstallPermission = BlockerUtils.canRequestPackageInstalls(context)
        }
    )

    LaunchedEffect(hasAccessibilityPermission) {
        if (!hasAccessibilityPermission && isActiveBlockingEnabled) {
            isActiveBlockingEnabled = false
            blockerStateManager.setBlockerEnabled(false)
        }
    }

    // Check installation blocking state when switching modes or on initial load
    LaunchedEffect(showInstallationBlocking) {
        if (showInstallationBlocking) {
            val isPackageInstalled = BlockerUtils.isTargetPackageInstalled(context)
            if (!isPackageInstalled && isInstallationBlockingEnabled) {
                isInstallationBlockingEnabled = false
                blockerStateManager.setInstallationBlockingEnabled(false)
            }
            // If package is installed, keep current state (don't force it)
        }
    }

    fun createPermissions(): List<Permission> {
        return listOf(
            Permission(
                title = "Специальные возможности",
                description = "Требуется для обнаружения запуска MAX",
                icon = Icons.Filled.Accessibility,
                isGranted = hasAccessibilityPermission,
                instructions = BlockerUtils.buildAccessibilityInstructions(),
                actions =
                    listOf(
                        "Открыть «Спец. возможности»" to
                            {
                                BlockerUtils.openAccessibilitySettings(context)
                            },
                        "Открыть настройки приложения" to
                            {
                                BlockerUtils.openAppSettings(context)
                            }
                    )
            ),
            Permission(
                title = "Показ поверх других окон",
                description = "Требуется для блокировки экрана MAX",
                icon = Icons.Filled.Layers,
                isGranted = hasOverlayPermission,
                instructions =
                    "Откройте «Показ поверх других окон», найдите приложение «скаМ» в списке и разрешите показ.",
                actions =
                    listOf(
                        "Открыть настройки" to
                            {
                                BlockerUtils.openOverlaySettings(context)
                            }
                    )
            ),
            Permission(
                title = "Оптимизация батареи",
                description = "Отключите для надежной работы блокировки",
                icon = Icons.Filled.BatteryChargingFull,
                isGranted = isBatteryOptimized,
                instructions = BlockerUtils.buildBatteryOptimizationInstructions(),
                actions =
                    listOf(
                        "Открыть настройки автозапуска" to
                            {
                                BlockerUtils.openAutoloadSettings(context)
                            },
                        "Открыть настройки батареи" to
                            {
                                BlockerUtils.openBatteryOptimizationSettings(
                                    context
                                )
                            },
                        "Инструкции для Tecno/Infinix" to
                            {
                                BlockerUtils.openTecnoInfinixGuide(context)
                            }
                    )
            )
        )
    }

    val activeBlockingState =
        BlockerViewState(
            isBlockerEnabled = isActiveBlockingEnabled,
            permissions = createPermissions(),
            canToggleBlocker = canToggleActiveBlocking,
            onToggleBlocker = {
                isActiveBlockingEnabled = !isActiveBlockingEnabled
                blockerStateManager.setBlockerEnabled(isActiveBlockingEnabled)
                if (isActiveBlockingEnabled && hasAccessibilityPermission) {
                    BlockerUtils.startWarmUpService(context)
                }
            },
            isInstallationBlocking = false,
            onSwitchView = {
                if (!isActiveBlockingEnabled && !isInstallationBlockingEnabled) {
                    showInstallationBlocking = !showInstallationBlocking
                    blockerStateManager.setInstallationBlockingMode(
                        showInstallationBlocking
                    )
                }
            },
            powerButtonIcon = null,
            onOpenIconSelector = { showIconSelector = true }
        )

    val installationBlockingState =
        BlockerViewState(
            isBlockerEnabled = isInstallationBlockingEnabled,
            permissions = emptyList(), // No permissions for installation blocking
            canToggleBlocker =
                canToggleInstallationBlocking && !isInstallationOperationInProgress,
            onToggleBlocker = {
                if (!isInstallationOperationInProgress) {
                    // Check install permission before proceeding
                    if (!hasInstallPermission) {
                        BlockerUtils.requestInstallPermission(context)
                        return@BlockerViewState
                    }

                    coroutineScope.launch {
                        isInstallationOperationInProgress = true
                        try {
                            if (isInstallationBlockingEnabled) {
                                // Uninstall: remove the target package using system dialog
                                BlockerUtils.uninstallTargetPackageWithCallback(context) { success ->
                                    // Use coroutineScope to update UI state on main thread
                                    coroutineScope.launch {
                                        if (success) {
                                            // Verify package is actually uninstalled before
                                            // updating state
                                            val isStillInstalled =
                                                BlockerUtils.isTargetPackageInstalled(
                                                    context
                                                )
                                            if (!isStillInstalled) {
                                                isInstallationBlockingEnabled = false
                                                blockerStateManager
                                                    .setInstallationBlockingEnabled(
                                                        false
                                                    )
                                            }
                                        }
                                        // Reset operation flag after completion
                                        isInstallationOperationInProgress = false
                                    }
                                }
                                // Don't reset operation flag here since uninstall is async
                                return@launch
                            } else {
                                // Install: install dummy APK using system dialog
                                BlockerUtils.installDummyApkWithCallback(context) { success
                                    ->
                                    // Use coroutineScope to update UI state on main thread
                                    coroutineScope.launch {
                                        if (success) {
                                            // Verify package is actually installed before
                                            // updating
                                            // state
                                            val isNowInstalled =
                                                BlockerUtils.isTargetPackageInstalled(
                                                    context
                                                )
                                            if (isNowInstalled) {
                                                isInstallationBlockingEnabled = true
                                                blockerStateManager
                                                    .setInstallationBlockingEnabled(
                                                        true
                                                    )
                                            }
                                        }
                                        // Reset operation flag after completion
                                        isInstallationOperationInProgress = false
                                    }
                                }
                                // Don't reset operation flag here since install is async
                                return@launch
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Don't update state on error
                        } finally {
                            isInstallationOperationInProgress = false
                        }
                    }
                }
            },
            isInstallationBlocking = true,
            onSwitchView = {
                if (!isActiveBlockingEnabled && !isInstallationBlockingEnabled) {
                    showInstallationBlocking = !showInstallationBlocking
                    blockerStateManager.setInstallationBlockingMode(
                        showInstallationBlocking
                    )
                }
            },
            powerButtonIcon = Icons.Filled.FileDownloadOff,
            isOperationInProgress = isInstallationOperationInProgress,
            onOpenIconSelector = { showIconSelector = true }
        )

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(
            targetState = showInstallationBlocking,
            label = "BlockerModeCrossfade"
        ) { isInstallationMode ->
            if (isInstallationMode) {
                BlockerView(state = installationBlockingState)
            } else {
                BlockerView(state = activeBlockingState)
            }
        }

        IconSelectorSheet(
            context = context,
            isVisible = showIconSelector,
            presets = iconPresetRepository.getAllPresets(context),
            selectedPresetId = selectedPresetId,
            isLoading = isIconChangeInProgress,
            onPresetSelected = { preset ->
                coroutineScope.launch {
                    isIconChangeInProgress = true
                    val success = appIconManager.applyIconPreset(preset)
                    if (success) {
                        iconPresetStateManager.setSelectedPresetId(preset.id)
                        showIconSelector = false
                    }
                    isIconChangeInProgress = false
                }
            },
            onDismiss = { showIconSelector = false }
        )
    }
}

@Composable
fun ObserveLifecycleEvents(context: Context, onResume: () -> Unit) {
    val lifecycleOwner = context as? LifecycleOwner

    DisposableEffect(lifecycleOwner) {
        if (lifecycleOwner == null) return@DisposableEffect onDispose {}

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
