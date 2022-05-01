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

class GetUserDataUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(id: String): Flow<Resource<UserModel>> = flow {
        operateFunInUseCase(
            tryBlock = {
                emit(Resource.Loading<UserModel>())
                val getUserResponse = userRepository.getUserData(id)
                if (getUserResponse.status == "ok") {
                    emit(Resource.Success<UserModel>(getUserResponse.data!!.toUserModel()))
                } else {
                    emit(Resource.Error<UserModel>(getUserResponse.error.toString()))
                }
            },
            onFail = { e ->
                Log.e("ERROR_LOG", "invoke: $e")
                emit(Resource.Error<UserModel>(e))
            }
        )
    }

}