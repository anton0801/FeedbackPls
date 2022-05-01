package com.feedback.android.app.domain.use_case.user

import android.util.Log
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.BaseResponse
import com.feedback.android.app.data.remote.dto.SetUserTariffBodyDto
import com.feedback.android.app.data.remote.dto.toUserModel
import com.feedback.android.app.domain.model.UserModel
import com.feedback.android.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CancelUserTariffUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(id: String): Flow<Resource<BaseResponse<String>>> = flow {
        operateFunInUseCase(
            tryBlock = {
                emit(Resource.Loading<BaseResponse<String>>())
                val getUserResponse = userRepository.cancelUserTariff(id)
                if (getUserResponse.status == "ok") {
                    emit(Resource.Success<BaseResponse<String>>(getUserResponse))
                } else {
                    emit(Resource.Error<BaseResponse<String>>(getUserResponse.error.toString()))
                }
            },
            onFail = { e ->
                Log.e("ERROR_LOG", "invoke: $e")
                emit(Resource.Error<BaseResponse<String>>(e))
            }
        )
    }

}