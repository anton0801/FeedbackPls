package com.feedback.android.app.presentation.ui.fragments.lk

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feedback.android.app.R
import com.feedback.android.app.common.Event
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.ResourceState
import com.feedback.android.app.data.remote.dto.*
import com.feedback.android.app.domain.model.TariffModel
import com.feedback.android.app.domain.use_case.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val useCases: UseCases
) : ViewModel() {

    private val _eventFlow: MutableStateFlow<Event<UiEvent>?> = MutableStateFlow(null)
    val eventFlow: StateFlow<Event<UiEvent>?> = _eventFlow

    private val _sendUserDataLiveData: MutableLiveData<Event<ResourceState<BaseResponse<String>>>> =
        MutableLiveData()
    val sendUserDataLiveData: LiveData<Event<ResourceState<BaseResponse<String>>>> =
        _sendUserDataLiveData

    private val _changePinCodeState: MutableLiveData<Event<ResourceState<BaseResponse<String>>>> =
        MutableLiveData()
    val changePinCodeState: LiveData<Event<ResourceState<BaseResponse<String>>>> =
        _changePinCodeState

    private val _tariffs: MutableLiveData<Event<ResourceState<List<TariffModel>>>> =
        MutableLiveData()
    val tariffs: LiveData<Event<ResourceState<List<TariffModel>>>> = _tariffs

    private val _payTariff: MutableLiveData<Event<ResourceState<PayTariffDto>>> = MutableLiveData()
    val payTariff: LiveData<Event<ResourceState<PayTariffDto>>> = _payTariff

    var currentUserAvatar: File? = null
    var selectedTariffIdForPay: Int? = null
    var hasOpenedCertainTabInOnStart = false
    var isUserDataSet: Boolean = false

    fun onEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            is UiEvent.SendData -> {
                if (currentUserAvatar == null) {
                    onEvent(UiEvent.ShowErrorLabel(listOf(R.id.avatar)))
                    return
                }
                sendData(
                    SaveUserDataBodyDto(
                        phone = uiEvent.phone,
                        aboutText = uiEvent.aboutText,
                        aboutTitle = uiEvent.aboutTitle,
                        email = uiEvent.email,
                        fullName = "${uiEvent.surname} ${uiEvent.firstName} ${uiEvent.lastName}",
                        birthday = uiEvent.birthday,
                        profession = uiEvent.profession,
                        avatar = MultipartBody.Part.createFormData(
                            "avatar",
                            currentUserAvatar!!.name,
                            currentUserAvatar!!.asRequestBody("image/*".toMediaTypeOrNull())
                        ),
                        website = uiEvent.website,
                        whatsapp = if (uiEvent.whatsappAvailable) 1 else 0
                    )
                )
            }
            is UiEvent.ChangePinCode -> {
                changePinCode(phone = uiEvent.phone, newPinCode = uiEvent.newPinCode)
            }
            is UiEvent.PayTariff -> payTariff(uiEvent.userId, uiEvent.tariffId)
        }
        _eventFlow.value = Event(uiEvent)
    }

    fun getTariffs() {
        viewModelScope.launch {
            useCases.getTariffs().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _tariffs.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success -> _tariffs.postValue(Event(ResourceState(data = resource.data)))
                    is Resource.Error -> _tariffs.postValue(Event(ResourceState(error = resource.message)))
                }
            }
        }
    }

    private fun changePinCode(phone: String, newPinCode: String) {
        viewModelScope.launch {
            try {
                useCases.changePinCode(ChangePinCodeBodyDto(phone = phone, pinCode = newPinCode))
                    .collect { resource ->
                        when (resource) {
                            is Resource.Loading ->
                                _changePinCodeState.postValue(Event(ResourceState(isLoading = true)))
                            is Resource.Success ->
                                _changePinCodeState.postValue(Event(ResourceState(data = resource.data)))
                            is Resource.Error ->
                                _changePinCodeState.postValue(Event(ResourceState(error = resource.message)))
                        }
                    }
            } catch (e: ChangePinCodeException) {
                onEvent(UiEvent.ShowSnackbar(e.message ?: "что-то пошло не так"))
            }
        }
    }

    private fun sendData(saveUserDataBodyDto: SaveUserDataBodyDto) {
        viewModelScope.launch {
            try {
                useCases.saveUserData(saveUserDataBodyDto).collect { resource ->
                    when (resource) {
                        is Resource.Loading ->
                            _sendUserDataLiveData.postValue(Event(ResourceState(isLoading = true)))
                        is Resource.Success ->
                            _sendUserDataLiveData.postValue(Event(ResourceState(data = resource.data)))
                        is Resource.Error ->
                            _sendUserDataLiveData.postValue(Event(ResourceState(error = resource.message)))
                    }
                }
            } catch (e: SaveUserDataException) {
                val fieldsWithError = e.message?.split(", ")?.map { it.toInt() } ?: listOf()
                onEvent(UiEvent.ShowErrorLabel(fieldsWithError))
            }
        }
    }

    private fun payTariff(userId: Int, tariffId: Int) {
        selectedTariffIdForPay = tariffId
        viewModelScope.launch {
            useCases.payTariff(userId, tariffId).collect { res ->
                when (res) {
                    is Resource.Loading ->
                        _payTariff.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success ->
                        _payTariff.postValue(Event(ResourceState(data = res.data)))
                    is Resource.Error ->
                        _payTariff.postValue(Event(ResourceState(error = res.message)))
                }
            }
        }
    }

    sealed class UiEvent {
        data class SendData(
            val aboutTitle: String,
            val aboutText: String,
            val phone: String,
            val firstName: String,
            val surname: String,
            val lastName: String,
            val profession: String,
            val birthday: String,
            val email: String,
            val website: String,
            val whatsappAvailable: Boolean
        ) : UiEvent()

        data class ShowErrorLabel(val fieldsWithError: List<Int>) : UiEvent()
        data class ShowSnackbar(val message: String) : UiEvent()
        data class SelectTab(val tabId: Int?) : UiEvent()
        data class OpenLink(val link: String, val action: String = Intent.ACTION_VIEW) : UiEvent()
        data class ChangePinCode(val phone: String, val newPinCode: String) : UiEvent()
        data class PayTariff(val userId: Int, val tariffId: Int) : UiEvent()
        data class OpenWebView(val url: String, val tariffId: Int) : UiEvent()

        object CreateProfile : UiEvent()
        object AttachPhoto : UiEvent()
    }

}