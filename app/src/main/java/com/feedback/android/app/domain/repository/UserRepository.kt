package com.feedback.android.app.domain.repository

import com.feedback.android.app.data.remote.dto.*

interface UserRepository {
    suspend fun fundUserByPhone(phone: String): BaseResponse<UserDto>
    suspend fun auth(authBodyDto: AuthBodyDto): BaseResponse<Int>
    suspend fun getUserData(id: String): BaseResponse<UserDto>
    suspend fun saveUserData(saveUserDataBodyDto: SaveUserDataBodyDto): BaseResponse<String>
    suspend fun changePincode(changePinCodeBodyDto: ChangePinCodeBodyDto): BaseResponse<String>
    suspend fun sendPinCode(sendPinCodeDto: SendPinCodeDto): BaseResponse<Boolean>
    suspend fun checkPinCode(phone: String, pinCode: String): BaseResponse<Int>
    suspend fun searchUsers(name: String?, userId: String?, page: Int = 1): SearchUsersDto
    suspend fun deleteUser(deleteUserBodyDto: DeleteUserBodyDto): BaseResponse<String>
    suspend fun setUserTariff(
        id: String,
        setUserTariffBodyDto: SetUserTariffBodyDto
    ): BaseResponse<String>

    suspend fun cancelUserTariff(id: String): BaseResponse<String>
    suspend fun getAllUsers(): BaseResponse<List<UserDto>>
    suspend fun getAllFeedbacks(): BaseResponse<List<FeedbackDto>>
    suspend fun payTariff(userId: Int, tariffId: Int): BaseResponse<PayTariffDto>
    suspend fun checkPayment(userId: Int, tariffId: Int): BaseResponse<String>
}