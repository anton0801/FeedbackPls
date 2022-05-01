package com.feedback.android.app.presentation

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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val useCases: UseCases
) : ViewModel() {

    private val _userData: MutableLiveData<Event<ResourceState<UserModel>>> =
        MutableLiveData()
    val userData: LiveData<Event<ResourceState<UserModel>>> = _userData

    val pendingActionOptions: HashMap<String, String> = hashMapOf()

    private var isRetriedCall = false

    fun getUserData(id: String) {
        viewModelScope.launch {
            useCases.getUserData(id).collect { result ->
                when (result) {
                    is Resource.Loading -> _userData.postValue(Event(ResourceState(isLoading = true)))
                    is Resource.Success -> _userData.postValue(Event(ResourceState(data = result.data)))
                    is Resource.Error -> {
                        if (result.message?.contains("timeout") == true && !isRetriedCall) {
                            getUserData(id)
                            isRetriedCall = true
                            _userData.postValue(Event(ResourceState(isLoading = true)))
                        } else {
                            _userData.postValue(Event(ResourceState(error = result.message)))
                        }
                    }
                }
            }
        }
    }

}