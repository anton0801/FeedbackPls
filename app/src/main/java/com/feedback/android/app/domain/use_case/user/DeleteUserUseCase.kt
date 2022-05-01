package com.feedback.android.app.domain.use_case.user

import android.util.Base64
import android.util.Log
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.AuthBodyDto
import com.feedback.android.app.data.remote.dto.BaseResponse
import com.feedback.android.app.data.remote.dto.DeleteUserBodyDto
import com.feedback.android.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(id: String): Flow<Resource<BaseResponse<String>>> =
        flow {
            operateFunInUseCase(
                tryBlock = {
                    emit(Resource.Loading<BaseResponse<String>>())
                    val response = userRepository.deleteUser(
                        DeleteUserBodyDto(
                            id = id, k = getK(id)
                        )
                    )
                    if (response.status == "ok")
                        emit(Resource.Success<BaseResponse<String>>(response))
                    else
                        emit(Resource.Error<BaseResponse<String>>(response.error.toString()))
                },
                onFail = { e ->
                    Log.d("ERROR_LOG", "invoke: $e")
                    emit(Resource.Error<BaseResponse<String>>(e))
                }
            )
        }

    private fun getK(id: String): String {
        val t = "remove_user_$id"
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val keyTimestamp = calendar.time.time / 1000

        return Base64.encodeToString("$t*$keyTimestamp".toByteArray(), Base64.DEFAULT)
    }

}