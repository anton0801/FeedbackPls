package com.feedback.android.app.presentation.ui.fragments.auth.enter_phone

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feedback.android.app.common.Event
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.ResourceState
import com.feedback.android.app.domain.model.UserModel
import com.feedback.android.app.domain.use_case.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
class EnterPhoneViewModel @Inject constructor(
    private val useCases: UseCases
) : ViewModel() {

    private val _eventFlow: MutableStateFlow<Event<UiEvent>?> = MutableStateFlow(null)
    val eventFlow: StateFlow<Event<UiEvent>?> = _eventFlow

    private val _userData: MutableLiveData<Event<ResourceState<UserModel>>> = MutableLiveData()
    val userData: LiveData<Event<ResourceState<UserModel>>> = _userData

    fun onEvent(uiEvent: UiEvent) {
        _eventFlow.value = Event(uiEvent)
    }

    fun findUserByPhone(phone: String) {
        viewModelScope.launch {
            useCases.findUserByPhone(phone).collect { state ->
                when (state) {
                    is Resource.Loading -> _userData.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success -> _userData.postValue(Event(ResourceState(data = state.data)))
                    is Resource.Error -> _userData.postValue(Event(ResourceState(error = state.message)))
                }
            }
        }
    }

    sealed class UiEvent {
        data class ChangePhoneInput(val text: String) : UiEvent()
        data class Send(val phone: String) : UiEvent()
        data class OpenLink(val url: String): UiEvent()
    }

}