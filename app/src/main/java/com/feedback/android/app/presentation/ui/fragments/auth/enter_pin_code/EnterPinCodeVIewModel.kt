package com.feedback.android.app.presentation.ui.fragments.auth.enter_pin_code

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feedback.android.app.common.Event
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.ResourceState
import com.feedback.android.app.data.remote.dto.AuthBodyDto
import com.feedback.android.app.data.remote.dto.BaseResponse
import com.feedback.android.app.data.remote.dto.SendPinCodeDto
import com.feedback.android.app.domain.model.UserModel
import com.feedback.android.app.domain.use_case.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnterPinCodeVIewModel @Inject constructor(
    private val useCases: UseCases
) : ViewModel() {

    private val _eventFlow: MutableStateFlow<Event<UiEvent>?> = MutableStateFlow(null)
    val eventFlow: StateFlow<Event<UiEvent>?> = _eventFlow

    private val _authLiveData: MutableLiveData<Event<ResourceState<BaseResponse<Int>>>> =
        MutableLiveData()
    val authLiveData: LiveData<Event<ResourceState<BaseResponse<Int>>>> = _authLiveData

    private val _sendPinCodeLiveData: MutableLiveData<Event<ResourceState<BaseResponse<Boolean>>>> =
        MutableLiveData()
    val sendPinCodeLiveData: LiveData<Event<ResourceState<BaseResponse<Boolean>>>> =
        _sendPinCodeLiveData

    private val _checkPinCodeLiveData: MutableLiveData<Event<ResourceState<BaseResponse<Int>>>> =
        MutableLiveData()
    val checkPinCodeLiveData: LiveData<Event<ResourceState<BaseResponse<Int>>>> =
        _checkPinCodeLiveData

    private val _userDataLiveData: MutableLiveData<Event<ResourceState<UserModel>>> =
        MutableLiveData()
    val userDataLiveData: LiveData<Event<ResourceState<UserModel>>> = _userDataLiveData

    fun onEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            is UiEvent.Auth -> auth(uiEvent.phone, uiEvent.pincode, uiEvent.isUserRecognizedAccount)
            is UiEvent.SendPinCode -> sendPinCode(SendPinCodeDto(uiEvent.phone))
            is UiEvent.CheckPinCode -> checkPinCode(uiEvent.phone, uiEvent.pincode)
            is UiEvent.GetUserData -> getUserData(uiEvent.id)
        }
        _eventFlow.value = Event(uiEvent)
    }

    private fun auth(phone: String, pincode: String, isUserRecognizedAccount: Int) {
        viewModelScope.launch {
            useCases.auth(
                AuthBodyDto(
                    phone = phone,
                    pinCode = pincode,
                    isUserRecognizedAccount = isUserRecognizedAccount
                )
            ).collect { state ->
                when (state) {
                    is Resource.Loading -> _authLiveData.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success -> _authLiveData.postValue(Event(ResourceState(data = state.data)))
                    is Resource.Error -> _authLiveData.postValue(Event(ResourceState(error = state.message)))
                }
            }
        }
    }

    private fun getUserData(userId: String) {
        viewModelScope.launch {
            useCases.getUserData(userId).collect { res ->
                when (res) {
                    is Resource.Loading -> _userDataLiveData.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success -> _userDataLiveData.postValue(Event(ResourceState(data = res.data)))
                    is Resource.Error -> _userDataLiveData.postValue(Event(ResourceState(error = res.message)))
                }
            }
        }
    }

    private fun sendPinCode(sendPinCodeDto: SendPinCodeDto) {
        viewModelScope.launch {
            useCases.sendPinCode(sendPinCodeDto).collect { resource ->
                when (resource) {
                    is Resource.Loading ->
                        _sendPinCodeLiveData.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success ->
                        _sendPinCodeLiveData.postValue(Event(ResourceState(data = resource.data)))
                    is Resource.Error ->
                        _sendPinCodeLiveData.postValue(Event(ResourceState(error = resource.message)))
                }
            }
        }
    }

    private fun checkPinCode(phone: String, pinCode: String) {
        viewModelScope.launch {
            useCases.checkPinCode(phone, pinCode).collect { resource ->
                when (resource) {
                    is Resource.Loading ->
                        _checkPinCodeLiveData.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success ->
                        _checkPinCodeLiveData.postValue(Event(ResourceState(data = resource.data)))
                    is Resource.Error ->
                        _checkPinCodeLiveData.postValue(Event(ResourceState(error = resource.message)))
                }
            }
        }
    }

    sealed class UiEvent {
        data class Auth(val phone: String, val pincode: String, val isUserRecognizedAccount: Int) : UiEvent()
        data class SendPinCode(val phone: String) : UiEvent()
        data class CheckPinCode(val phone: String, val pincode: String) : UiEvent()
        data class ShowSnackbar(val message: String) : UiEvent()
        data class IncorrectPinCode(val message: String) : UiEvent()
        data class CorrectPinCode(val userId: String) : UiEvent()
        data class GetUserData(val id: String) : UiEvent()

        object ErrorSendPinCode : UiEvent()
    }

}