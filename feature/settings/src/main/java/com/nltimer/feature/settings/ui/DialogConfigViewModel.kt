package com.nltimer.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.data.model.DialogGridConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DialogConfigViewModel @Inject constructor(
    private val settingsPrefs: SettingsPrefs,
) : ViewModel() {

    val dialogConfig: StateFlow<DialogGridConfig> = settingsPrefs.getDialogConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DialogGridConfig())

    fun updateConfig(config: DialogGridConfig) {
        viewModelScope.launch {
            settingsPrefs.updateDialogConfig(config)
        }
    }
}
