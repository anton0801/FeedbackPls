package com.feedback.android.app.domain.use_case.user

import android.util.Log
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.SearchUsersDto
import com.feedback.android.app.data.remote.dto.toUserModel
import com.feedback.android.app.domain.model.UserModel
import com.feedback.android.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(name: String?, userId: String?, page: Int = 1): Flow<Resource<SearchUsersDto>> = flow {
        operateFunInUseCase(
            tryBlock = {
                emit(Resource.Loading<SearchUsersDto>())
                val getUserResponse = userRepository.searchUsers(name, userId, page)
                emit(Resource.Success<SearchUsersDto>(getUserResponse))
            },
            onFail = { e ->
                Log.e("ERROR_LOG", "invoke: $e")
                emit(Resource.Error<SearchUsersDto>(e))
            }
        )
    }

}