package com.feedback.android.app.domain.use_case.user

import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.BaseResponse
import com.feedback.android.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CheckPaymentUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Int, tariffId: Int): Flow<Resource<BaseResponse<String>>> =
        flow {
            operateFunInUseCase(
                tryBlock = {
                    emit(Resource.Loading<BaseResponse<String>>())
                    val response = userRepository.checkPayment(userId, tariffId)
                    if (response.status == "ok") {
                        emit(Resource.Success<BaseResponse<String>>(response))
                    } else {
                        emit(Resource.Error<BaseResponse<String>>(response.error!!))
                    }
                },
                onFail = { error ->
                    emit(Resource.Error<BaseResponse<String>>(error))
                }
            )
        }
}