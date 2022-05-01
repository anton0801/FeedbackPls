package com.feedback.android.app.domain.use_case.user

import android.util.Log
import com.feedback.android.app.R
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.BaseResponse
import com.feedback.android.app.data.remote.dto.SaveUserDataBodyDto
import com.feedback.android.app.data.remote.dto.SaveUserDataException
import com.feedback.android.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception
import javax.inject.Inject
import kotlin.jvm.Throws

class SaveUserDataUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    @Throws(SaveUserDataException::class)
    operator fun invoke(saveUserDataBodyDto: SaveUserDataBodyDto): Flow<Resource<BaseResponse<String>>> =
        flow {
            operateFunInUseCase(
                tryBlock = {
                    if (checkData(saveUserDataBodyDto).isNotEmpty()) {
                        throw SaveUserDataException(checkData(saveUserDataBodyDto))
                    }
                    emit(Resource.Loading<BaseResponse<String>>())
                    val saveUserDataResponse = userRepository.saveUserData(saveUserDataBodyDto)
                    if (saveUserDataResponse.status == "ok") {
                        emit(Resource.Success<BaseResponse<String>>(saveUserDataResponse))
                    } else {
                        emit(Resource.Error<BaseResponse<String>>(saveUserDataResponse.error.toString()))
                    }
                },
                onFail = { e ->
                    Log.e("ERROR_LOG", "invoke: $e")
                    emit(Resource.Error<BaseResponse<String>>(e))
                }
            )
        }

    private fun checkData(saveUserDataBodyDto: SaveUserDataBodyDto): List<Int> {
        val result = mutableListOf<Int>()
        val nameParts = saveUserDataBodyDto.fullName.split(" ")
        val firstName = try {
            nameParts[1]
        } catch (e: Exception) {
            ""
        }
        val surname = try {
            nameParts[0]
        } catch (e: Exception) {
            ""
        }
        val lastname = try {
            nameParts[2]
        } catch (e: Exception) {
            ""
        }
        if (firstName.isBlank()) {
            result.add(R.id.first_name)
        }
        if (surname.isBlank()) {
            result.add(R.id.surname)
        }
        if (lastname.isBlank()) {
            result.add(R.id.last_name)
        }

        if (saveUserDataBodyDto.aboutText.isBlank()) {
            result.add(R.id.title_input)
        }
        if (saveUserDataBodyDto.aboutTitle.isBlank()) {
            result.add(R.id.about_input)
        }
        if (saveUserDataBodyDto.avatar == null) {
            result.add(R.id.avatar)
        }
        if (saveUserDataBodyDto.email.isBlank()) {
            result.add(R.id.email)
        }
        if (saveUserDataBodyDto.birthday.isBlank()) {
            result.add(R.id.birthday)
        }
        if (saveUserDataBodyDto.profession.isBlank()) {
            result.add(R.id.profession)
        }
        return result
    }

}