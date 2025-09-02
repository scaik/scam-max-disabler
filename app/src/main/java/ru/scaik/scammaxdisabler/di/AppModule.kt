package ru.scaik.scammaxdisabler.di

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import ru.scaik.scammaxdisabler.ScamMaxDisablerApplication
import ru.scaik.scammaxdisabler.manager.AppIconManager
import ru.scaik.scammaxdisabler.repository.IconPresetRepository
import ru.scaik.scammaxdisabler.service.ServiceRestartHelper
import ru.scaik.scammaxdisabler.state.BlockerStateManager
import ru.scaik.scammaxdisabler.state.IconPresetStateManager
import ru.scaik.scammaxdisabler.state.PermissionStateManager
import ru.scaik.scammaxdisabler.state.ServiceStateManager

interface AppModule {
    val serviceStateManager: ServiceStateManager
    val blockerStateManager: BlockerStateManager
    val permissionStateManager: PermissionStateManager
    val iconPresetStateManager: IconPresetStateManager
    val appIconManager: AppIconManager
    val iconPresetRepository: IconPresetRepository
    val serviceRestartHelper: ServiceRestartHelper
    val applicationScope: CoroutineScope
}

class AppModuleImpl(
    appContext: Context,
) : AppModule {
    override val serviceStateManager by lazy { ServiceStateManager(appContext = appContext) }
    override val blockerStateManager by lazy { BlockerStateManager(appContext = appContext) }
    override val permissionStateManager by lazy { PermissionStateManager(appContext = appContext) }
    override val iconPresetStateManager by lazy { IconPresetStateManager(context = appContext) }
    override val appIconManager by lazy { AppIconManager(context = appContext) }
    override val iconPresetRepository by lazy { IconPresetRepository() }
    override val serviceRestartHelper by lazy { ServiceRestartHelper(context = appContext) }
    override val applicationScope = (appContext as ScamMaxDisablerApplication).applicationScope
}

val AppComponent: AppModule
    get() = (ScamMaxDisablerApplication.appContext as ScamMaxDisablerApplication).appModule
