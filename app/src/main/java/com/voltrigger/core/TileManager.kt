package com.voltrigger.core

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Manages the enabled/disabled state of TileService components to remove them from QS completely.
 */
object TileManager {

    /**
     * Enables or disables a TileService component using PackageManager.
     * When disabled, the tile will disappear from the Quick Settings "Edit" menu,
     * consuming 0 memory and CPU.
     */
    fun setTileComponentEnabled(context: Context, tileClass: Class<*>, isEnabled: Boolean) {
        val componentName = ComponentName(context, tileClass)
        val state = if (isEnabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        
        context.packageManager.setComponentEnabledSetting(
            componentName,
            state,
            PackageManager.DONT_KILL_APP
        )
    }
    
    /**
     * Checks if a TileService component is currently enabled via PackageManager.
     */
    fun isTileComponentEnabled(context: Context, tileClass: Class<*>): Boolean {
        val componentName = ComponentName(context, tileClass)
        val state = context.packageManager.getComponentEnabledSetting(componentName)
        // If state is default, assume it's enabled (as defined in manifest unless disabled)
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
               state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
    }
}
