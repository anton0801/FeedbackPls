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

class SendPinCodeUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    @Throws(SendPinCodeException::class)
    operator fun invoke(sendPinCodeDto: SendPinCodeDto): Flow<Resource<BaseResponse<Boolean>>> =
        flow {
            operateFunInUseCase(
                tryBlock = {
                    emit(Resource.Loading<BaseResponse<Boolean>>())
                    val sendPinCodeResponse = userRepository.sendPinCode(sendPinCodeDto)
                    if (sendPinCodeResponse.status == "ok") {
                        emit(Resource.Success<BaseResponse<Boolean>>(sendPinCodeResponse))
                    } else {
                        emit(Resource.Error<BaseResponse<Boolean>>(sendPinCodeResponse.error.toString()))
                    }
                },
                onFail = { e ->
                    Log.e("ERROR_LOG", "invoke: $e")
                    emit(Resource.Error<BaseResponse<Boolean>>(e))
                }
            )
        }
}