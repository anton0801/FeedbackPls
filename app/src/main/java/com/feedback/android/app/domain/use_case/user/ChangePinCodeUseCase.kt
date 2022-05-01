package com.feedback.android.app.domain.use_case.user

import android.util.Log
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.BaseResponse
import com.feedback.android.app.data.remote.dto.ChangePinCodeBodyDto
import com.feedback.android.app.data.remote.dto.ChangePinCodeException
import com.feedback.android.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.jvm.Throws

class ChangePinCodeUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    @Throws(ChangePinCodeException::class)
    operator fun invoke(changePinCodeBody: ChangePinCodeBodyDto):
            Flow<Resource<BaseResponse<String>>> = flow {
        operateFunInUseCase(
            tryBlock = {
                if (changePinCodeBody.pinCode.isBlank())
                    throw ChangePinCodeException("ПИН-код не может быть пустым")
                if (changePinCodeBody.pinCode.length < 4)
                    throw ChangePinCodeException("ПИН-код должен содержать 4 цифры")
                if (changePinCodeBody.phone.isBlank())
                    throw ChangePinCodeException("Не удалось найти ваш номер телефона! Воидите в приложение заного")
                emit(Resource.Loading<BaseResponse<String>>())
                val changePinCodeResponse = userRepository.changePincode(changePinCodeBody)
                if (changePinCodeResponse.status == "ok") {
                    emit(Resource.Success<BaseResponse<String>>(changePinCodeResponse))
                } else {
                    emit(Resource.Error<BaseResponse<String>>(changePinCodeResponse.error.toString()))
                }
            },
            onFail = { e ->
                Log.e("ERROR_LOG", "invoke: $e")
                emit(Resource.Error<BaseResponse<String>>(e))
            }
        )
    }
}