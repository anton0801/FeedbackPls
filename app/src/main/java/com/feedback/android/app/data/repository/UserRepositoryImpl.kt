package com.feedback.android.app.data.repository

import com.feedback.android.app.data.remote.ApiService
import com.feedback.android.app.data.remote.dto.*
import com.feedback.android.app.domain.repository.UserRepository
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun fundUserByPhone(phone: String): BaseResponse<UserDto> =
        apiService.findUserByPhone(phone)

    override suspend fun auth(authBodyDto: AuthBodyDto): BaseResponse<Int> =
        apiService.auth(authBodyDto)

    override suspend fun getUserData(id: String): BaseResponse<UserDto> =
        apiService.getUserData(id)

    override suspend fun saveUserData(saveUserDataBodyDto: SaveUserDataBodyDto): BaseResponse<String> =
        apiService.saveUserData(
            phone = saveUserDataBodyDto.phone.toRequestBody(),
            whatsapp = saveUserDataBodyDto.whatsapp.toString().toRequestBody(),
            fullName = saveUserDataBodyDto.fullName.toRequestBody(),
            email = saveUserDataBodyDto.email.toRequestBody(),
            birthday = saveUserDataBodyDto.birthday.toRequestBody(),
            aboutTitle = saveUserDataBodyDto.aboutTitle.toRequestBody(),
            aboutText = saveUserDataBodyDto.aboutText.toRequestBody(),
            profession = saveUserDataBodyDto.profession.toRequestBody(),
            website = saveUserDataBodyDto.website.toRequestBody(),
            avatar = saveUserDataBodyDto.avatar
        )

    override suspend fun changePincode(changePinCodeBodyDto: ChangePinCodeBodyDto): BaseResponse<String> =
        apiService.changePincode(changePinCodeBodyDto)

    override suspend fun sendPinCode(sendPinCodeDto: SendPinCodeDto): BaseResponse<Boolean> =
        apiService.sendPinCode(sendPinCodeDto)

    override suspend fun checkPinCode(phone: String, pinCode: String): BaseResponse<Int> =
        apiService.checkPinCode(phone, pinCode)

    override suspend fun searchUsers(name: String?, userId: String?, page: Int): SearchUsersDto =
        apiService.searchUsers(name, userId, page)

    override suspend fun deleteUser(deleteUserBodyDto: DeleteUserBodyDto): BaseResponse<String> =
        apiService.deleteUser(deleteUserBodyDto)

    override suspend fun setUserTariff(
        id: String,
        setUserTariffBodyDto: SetUserTariffBodyDto
    ): BaseResponse<String> = apiService.setUserTariff(id, setUserTariffBodyDto)

    override suspend fun cancelUserTariff(id: String): BaseResponse<String> =
        apiService.cancelUserTariff(id)

    override suspend fun getAllUsers(): BaseResponse<List<UserDto>> = apiService.getAllUsers()

    override suspend fun getAllFeedbacks(): BaseResponse<List<FeedbackDto>> =
        apiService.getAllFeedbacks()

    override suspend fun payTariff(userId: Int, tariffId: Int): BaseResponse<PayTariffDto> =
        apiService.payTariff(userId, tariffId)

    override suspend fun checkPayment(userId: Int, tariffId: Int): BaseResponse<String> =
        apiService.checkPayment(userId, tariffId)
}