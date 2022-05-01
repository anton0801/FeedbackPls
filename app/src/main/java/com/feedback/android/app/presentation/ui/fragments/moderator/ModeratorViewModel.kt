package com.feedback.android.app.presentation.ui.fragments.moderator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feedback.android.app.common.Event
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.ResourceState
import com.feedback.android.app.data.remote.dto.*
import com.feedback.android.app.domain.use_case.UseCases
import com.feedback.android.app.presentation.ui.fragments.lk.ProfileViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModeratorViewModel @Inject constructor(
    private val useCases: UseCases
) : ViewModel() {

    private val _eventFlow: MutableStateFlow<Event<UiEvent>?> = MutableStateFlow(null)
    val eventFlow: StateFlow<Event<UiEvent>?> = _eventFlow

    private val _searchResults: MutableLiveData<Event<ResourceState<SearchUsersDto>>> =
        MutableLiveData()
    val searchResults: LiveData<Event<ResourceState<SearchUsersDto>>> = _searchResults

    private val _deleteUserResponse: MutableLiveData<Event<ResourceState<BaseResponse<String>>>> =
        MutableLiveData()
    val deleteUserResponse: LiveData<Event<ResourceState<BaseResponse<String>>>> =
        _deleteUserResponse

    private val _setUserTariffResponse: MutableLiveData<Event<ResourceState<BaseResponse<String>>>> =
        MutableLiveData()
    val setUserTariffResponse: LiveData<Event<ResourceState<BaseResponse<String>>>> =
        _setUserTariffResponse

    private val _cancelUserTariffResponse: MutableLiveData<Event<ResourceState<BaseResponse<String>>>> =
        MutableLiveData()
    val cancelUserTariffResponse: LiveData<Event<ResourceState<BaseResponse<String>>>> =
        _cancelUserTariffResponse

    private val _changePinCodeState: MutableLiveData<Event<ResourceState<BaseResponse<String>>>> =
        MutableLiveData()
    val changePinCodeState: LiveData<Event<ResourceState<BaseResponse<String>>>> =
        _changePinCodeState

    private val _dataToExportLiveData: MutableLiveData<Event<ResourceState<ModeratorExportData>>> =
        MutableLiveData()
    val dataToExportLiveData: LiveData<Event<ResourceState<ModeratorExportData>>> =
        _dataToExportLiveData

    var page = 1
    var isLoading = true
    var isLastPage = false
    var searchName: String? = null
    var searchUserId: String? = null

    init {
        searchUsers(null, null)
    }

    fun onEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            is UiEvent.SearchUsers -> searchUsers(searchName, searchUserId, page)
            is UiEvent.DeleteUser -> deleteUser(uiEvent.id)
            is UiEvent.SetUserTariff -> setUserTariff(uiEvent.id)
            is UiEvent.CancelUserTariff -> cancelUserTariff(uiEvent.id)
            is UiEvent.ChangePinCode -> changePinCode(uiEvent.phone, uiEvent.newPinCode)
            is UiEvent.ExportDataToExcel -> getDataToExportInExcel()
        }
        _eventFlow.value = Event(uiEvent)
    }

    private fun searchUsers(name: String?, userId: String?, page: Int = 1) {
        viewModelScope.launch {
            useCases.searchUsers(name, userId, page).collectLatest { result ->
                when (result) {
                    is Resource.Loading ->
                        _searchResults.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success ->
                        _searchResults.postValue(Event(ResourceState(data = result.data)))
                    is Resource.Error ->
                        _searchResults.postValue(Event(ResourceState(error = result.message)))
                }
            }
        }
    }

    private fun deleteUser(id: String) {
        viewModelScope.launch {
            useCases.deleteUser(id).collectLatest { result ->
                when (result) {
                    is Resource.Loading ->
                        _deleteUserResponse.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success ->
                        _deleteUserResponse.postValue(Event(ResourceState(data = result.data)))
                    is Resource.Error ->
                        _deleteUserResponse.postValue(Event(ResourceState(error = result.message)))
                }
            }
        }
    }

    private fun setUserTariff(id: String) {
        viewModelScope.launch {
            useCases.setUserTariff(
                id,
                SetUserTariffBodyDto(
                    "Акционный"
                )
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading ->
                        _setUserTariffResponse.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success ->
                        _setUserTariffResponse.postValue(Event(ResourceState(data = resource.data)))
                    is Resource.Error ->
                        _setUserTariffResponse.postValue(Event(ResourceState(error = resource.message)))
                }
            }
        }
    }

    private fun cancelUserTariff(id: String) {
        viewModelScope.launch {
            useCases.cancelUserTariff(id).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading ->
                        _cancelUserTariffResponse.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success ->
                        _cancelUserTariffResponse.postValue(Event(ResourceState(data = resource.data)))
                    is Resource.Error ->
                        _cancelUserTariffResponse.postValue(Event(ResourceState(error = resource.message)))
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

    private fun getDataToExportInExcel() {
        viewModelScope.launch {
            var moderatorExportData = ModeratorExportData()
            useCases.getAllUsers().collect { resource ->
                when (resource) {
                    is Resource.Loading ->
                        _dataToExportLiveData.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success -> {
                        moderatorExportData = moderatorExportData.copy(users = resource.data!!)
                        useCases.getAllFeedbacks().collect { res ->
                            if (res is Resource.Success) {
                                moderatorExportData =
                                    moderatorExportData.copy(feedbacks = res.data!!)
                                _dataToExportLiveData.postValue(Event(ResourceState(data = moderatorExportData)))
                            }
                        }
                    }
                    is Resource.Error -> {
                        _dataToExportLiveData.postValue(Event(ResourceState(error = resource.message)))
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data class OpenLink(val url: String) : UiEvent()
        data class DeleteUser(val id: String) : UiEvent()
        data class SetUserTariff(val id: String) : UiEvent()
        data class CancelUserTariff(val id: String) : UiEvent()
        data class ChangePinCode(val phone: String, val newPinCode: String) : UiEvent()
        data class ShowSnackbar(val message: String) : UiEvent()

        object SearchUsers : UiEvent()
        object ExportDataToExcel : UiEvent()
    }

}