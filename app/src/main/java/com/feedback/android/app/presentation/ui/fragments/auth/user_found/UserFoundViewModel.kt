package com.feedback.android.app.presentation.ui.fragments.auth.user_found

import androidx.lifecycle.ViewModel
import com.feedback.android.app.common.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserFoundViewModel : ViewModel() {

    private val _eventFlow: MutableStateFlow<Event<UiEvent>?> = MutableStateFlow(null)
    val eventFlow: StateFlow<Event<UiEvent>?> = _eventFlow

    fun onEvent(uiEvent: UiEvent) {
        _eventFlow.value = Event(uiEvent)
    }

    sealed class UiEvent {
        object Yes : UiEvent()
        object No : UiEvent()
    }

}