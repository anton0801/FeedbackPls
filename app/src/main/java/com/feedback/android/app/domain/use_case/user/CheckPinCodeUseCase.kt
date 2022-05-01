package com.feedback.android.app.domain.use_case.user

import android.util.Log
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.BaseResponse
import com.feedback.android.app.data.remote.dto.SendPinCodeDto
import com.feedback.android.app.data.remote.dto.SendPinCodeException
import com.feedback.android.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.jvm.Throws

class CheckPinCodeUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    @Throws(SendPinCodeException::class)
    operator fun invoke(phone: String, pinCode: String): Flow<Resource<BaseResponse<Int>>> =
        flow {
            operateFunInUseCase(
                tryBlock = {
                    if (pinCode.isBlank())
                        throw SendPinCodeException("ПИН-код не может быть пустым")
                    emit(Resource.Loading<BaseResponse<Int>>())
                    val sendPinCodeResponse = userRepository.checkPinCode(phone, pinCode)
                    if (sendPinCodeResponse.status == "ok") {
                        emit(Resource.Success<BaseResponse<Int>>(sendPinCodeResponse))
                    } else {
                        emit(Resource.Error<BaseResponse<Int>>(sendPinCodeResponse.error.toString()))
                    }
                },
                onFail = { e ->
                    Log.e("ERROR_LOG", "invoke: $e")
                    emit(Resource.Error<BaseResponse<Int>>(e))
                }
            )
        }
}