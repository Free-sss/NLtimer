package com.nltimer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.repository.BehaviorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    behaviorRepository: BehaviorRepository,
) : ViewModel() {

    val totalDurationMs: StateFlow<Long> = behaviorRepository
        .getTotalDurationAllBehaviors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
}
