package com.voltrigger.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voltrigger.core.Prefs
import com.voltrigger.core.TileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TileTogglesViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = Prefs(application)

    private val _githubUsername = MutableStateFlow("")
    val githubUsername: StateFlow<String> = _githubUsername.asStateFlow()
    
    private val _controlCenterEnabled = MutableStateFlow(false)
    val controlCenterEnabled: StateFlow<Boolean> = _controlCenterEnabled.asStateFlow()

    private val _tileStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val tileStates: StateFlow<Map<String, Boolean>> = _tileStates.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.githubUsernameFlow.collect { username ->
                _githubUsername.value = username
            }
        }
        viewModelScope.launch {
            prefs.controlCenterEnabledFlow.collect { enabled ->
                _controlCenterEnabled.value = enabled
            }
        }
    }

    fun loadInitialTileState(tileClasses: List<Class<*>>) {
        val states = mutableMapOf<String, Boolean>()
        for (tileClass in tileClasses) {
            val isEnabled = TileManager.isTileComponentEnabled(getApplication(), tileClass)
            states[tileClass.name] = isEnabled
            
            // Sync with datastore just in case
            viewModelScope.launch {
                prefs.setTileEnabled(tileClass.name, isEnabled)
            }
        }
        _tileStates.value = states
    }

    fun setGithubUsername(username: String) {
        viewModelScope.launch {
            prefs.setGithubUsername(username)
        }
    }

    fun toggleTile(tileClass: Class<*>, isEnabled: Boolean) {
        TileManager.setTileComponentEnabled(getApplication(), tileClass, isEnabled)
        val currentStates = _tileStates.value.toMutableMap()
        currentStates[tileClass.name] = isEnabled
        _tileStates.value = currentStates

        viewModelScope.launch {
            prefs.setTileEnabled(tileClass.name, isEnabled)
        }
    }

    fun setControlCenterEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setControlCenterEnabled(enabled)
        }
    }
}
