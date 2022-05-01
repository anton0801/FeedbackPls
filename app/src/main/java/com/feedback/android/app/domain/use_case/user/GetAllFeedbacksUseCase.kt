package com.feedback.android.app.domain.use_case.user

import android.util.Log
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.FeedbackDto
import com.feedback.android.app.data.remote.dto.UserDto
import com.feedback.android.app.data.remote.dto.toUserModel
import com.feedback.android.app.domain.model.UserModel
import com.feedback.android.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllFeedbacksUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Resource<List<FeedbackDto>>> = flow {
        operateFunInUseCase(
            tryBlock = {
                emit(Resource.Loading<List<FeedbackDto>>())
                val getUserResponse = userRepository.getAllFeedbacks()
                if (getUserResponse.status == "ok") {
                    emit(Resource.Success<List<FeedbackDto>>(getUserResponse.data!!))
                } else {
                    emit(Resource.Error<List<FeedbackDto>>(getUserResponse.error.toString()))
                }
            },
            onFail = { e ->
                Log.e("ERROR_LOG", "invoke: $e")
                emit(Resource.Error<List<FeedbackDto>>(e))
            }
        )
    }

}