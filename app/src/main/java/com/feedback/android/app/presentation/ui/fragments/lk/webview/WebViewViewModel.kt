package com.feedback.android.app.presentation.ui.fragments.lk.webview

import androidx.lifecycle.*
import com.feedback.android.app.common.Event
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.ResourceState
import com.feedback.android.app.data.remote.dto.BaseResponse
import com.feedback.android.app.domain.use_case.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebViewViewModel @Inject constructor(
    private val useCases: UseCases
) : ViewModel() {

    private val _eventFlow: MutableStateFlow<Event<UiEvent>?> = MutableStateFlow(null)
    val eventFlow: StateFlow<Event<UiEvent>?> = _eventFlow

    private val _checkPaymentStatus: MutableLiveData<Event<ResourceState<BaseResponse<String>>>> =
        MutableLiveData()
    val checkPaymentStatus: LiveData<Event<ResourceState<BaseResponse<String>>>> =
        _checkPaymentStatus

    fun onEvent(uiEvent: UiEvent) {
        if (uiEvent is UiEvent.CheckPayment) {
            checkPayment(uiEvent.userId, uiEvent.tariffId)
        }
        _eventFlow.value = Event(uiEvent)
    }

    private fun checkPayment(userId: Int, tariffId: Int) {
        viewModelScope.launch {
            useCases.checkPayment(userId, tariffId).collect { res ->
                when (res) {
                    is Resource.Loading -> _checkPaymentStatus.postValue(
                        Event(ResourceState(isLoading = true))
                    )
                    is Resource.Success -> _checkPaymentStatus.postValue(Event(ResourceState(data = res.data)))
                    is Resource.Error -> _checkPaymentStatus.postValue(Event(ResourceState(error = res.message)))
                }
            }
        }
    }

    sealed class UiEvent {
        data class OpenUrl(val url: String) : UiEvent()
        data class CheckPayment(val userId: Int, val tariffId: Int) : UiEvent()
        object SuccessPayment : UiEvent()
    }

}