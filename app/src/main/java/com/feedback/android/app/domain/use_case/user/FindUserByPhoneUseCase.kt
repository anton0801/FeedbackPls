package com.feedback.android.app.domain.use_case.user

import android.util.Log
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.toUserModel
import com.feedback.android.app.domain.model.UserModel
import com.feedback.android.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FindUserByPhoneUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(phone: String): Flow<Resource<UserModel?>> = flow {
        operateFunInUseCase(
            tryBlock = {
                emit(Resource.Loading<UserModel?>())
                val response = userRepository.fundUserByPhone(phone)
                if (response.status == "ok") {
                    emit(Resource.Success<UserModel?>(response.data?.toUserModel()))
                } else {
                    emit(Resource.Error<UserModel?>("Не удалось получить данные с сервера"))
                }
            },
            onFail = { error ->
                Log.d("ERROR_LOG", "invoke: $error")
                emit(Resource.Error<UserModel?>(error))
            }
        )
    }

}