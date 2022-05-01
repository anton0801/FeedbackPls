package com.feedback.android.app.domain.use_case.user

import android.util.Log
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.AuthBodyDto
import com.feedback.android.app.data.remote.dto.BaseResponse
import com.feedback.android.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(authBodyDto: AuthBodyDto): Flow<Resource<BaseResponse<Int>>> =
        flow {
            operateFunInUseCase(
                tryBlock = {
                    emit(Resource.Loading<BaseResponse<Int>>())
                    val response = userRepository.auth(authBodyDto)
                    emit(Resource.Success<BaseResponse<Int>>(response))
                },
                onFail = { e ->
                    Log.d("ERROR_LOG", "invoke: $e")
                    emit(Resource.Error<BaseResponse<Int>>(e))
                }
            )
        }

}