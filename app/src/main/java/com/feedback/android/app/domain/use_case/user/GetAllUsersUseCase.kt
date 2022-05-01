package com.feedback.android.app.domain.use_case.user

import android.util.Log
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.UserDto
import com.feedback.android.app.data.remote.dto.toUserModel
import com.feedback.android.app.domain.model.UserModel
import com.feedback.android.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Resource<List<UserModel>>> = flow {
        operateFunInUseCase(
            tryBlock = {
                emit(Resource.Loading<List<UserModel>>())
                val getUserResponse = userRepository.getAllUsers()
                if (getUserResponse.status == "ok") {
                    emit(Resource.Success<List<UserModel>>(getUserResponse.data!!.map { it.toUserModel() }))
                } else {
                    emit(Resource.Error<List<UserModel>>(getUserResponse.error.toString()))
                }
            },
            onFail = { e ->
                Log.e("ERROR_LOG", "invoke: $e")
                emit(Resource.Error<List<UserModel>>(e))
            }
        )
    }

}