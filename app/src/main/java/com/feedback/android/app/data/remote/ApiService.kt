package com.feedback.android.app.data.remote

import com.feedback.android.app.common.Constants
import com.feedback.android.app.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    @GET(Constants.FIND_USER_BY_PHONE_URL)
    suspend fun findUserByPhone(@Query("phone") phone: String): BaseResponse<UserDto>

    @POST(Constants.AUTH_URL)
    suspend fun auth(@Body authBodyDto: AuthBodyDto): BaseResponse<Int>

    @GET(Constants.GET_USER_DATA_URL)
    suspend fun getUserData(@Path("id") id: String): BaseResponse<UserDto>

    @Multipart
    @POST(Constants.SAVE_USER_DATA_URL)
    suspend fun saveUserData(
        @Part("phone") phone: RequestBody,
        @Part("whatsapp") whatsapp: RequestBody,
        @Part("full_name") fullName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("birthday") birthday: RequestBody,
        @Part("about_title") aboutTitle: RequestBody,
        @Part("about_text") aboutText: RequestBody,
        @Part("profession") profession: RequestBody,
        @Part("website") website: RequestBody,
        @Part avatar: MultipartBody.Part
    ): BaseResponse<String>

    @PUT(Constants.SET_NEW_PIN_CODE_URL)
    suspend fun changePincode(
        @Body changePincodeBody: ChangePinCodeBodyDto
    ): BaseResponse<String>

    @GET(Constants.TARIFFS_URL)
    suspend fun getTariffs(
        @Path("currentUserTariffId") currentUserTariffId: String
    ): BaseResponse<List<TariffDto>>

    @POST(Constants.SEND_PIN_CODE_URL)
    suspend fun sendPinCode(@Body sendPinCodeDto: SendPinCodeDto): BaseResponse<Boolean>

    @GET(Constants.CHECK_PIN_CODE_URL)
    suspend fun checkPinCode(
        @Query("phone") phone: String,
        @Query("pin_code") pinCode: String
    ): BaseResponse<Int>

    @GET(Constants.MODERATOR_SEARCH_USERS)
    suspend fun searchUsers(
        @Query("name") name: String?,
        @Query("user_id") userId: String?,
        @Query("page") page: Int = 1
    ): SearchUsersDto

    @HTTP(method = "DELETE", path = Constants.MODERATOR_DELETE_USER, hasBody = true)
    suspend fun deleteUser(
        @Body deleteUserBodyDto: DeleteUserBodyDto
    ): BaseResponse<String>

    @POST(Constants.SET_USER_TARIFF)
    suspend fun setUserTariff(
        @Path("id") id: String,
        @Body setUserTariffBodyDto: SetUserTariffBodyDto
    ): BaseResponse<String>

    @POST(Constants.CANCEL_USER_TARIFF)
    suspend fun cancelUserTariff(
        @Path("id") id: String
    ): BaseResponse<String>

    @GET(Constants.GET_ALL_USERS)
    suspend fun getAllUsers(): BaseResponse<List<UserDto>>

    @GET(Constants.GET_ALL_FEEDBACKS)
    suspend fun getAllFeedbacks(): BaseResponse<List<FeedbackDto>>

    @GET(Constants.PAY_TARIFF)
    suspend fun payTariff(
        @Query("user_id") userId: Int,
        @Query("tariff_id") tariffId: Int,
    ): BaseResponse<PayTariffDto>

    @POST(Constants.CHECK_PAYMENT)
    suspend fun checkPayment(
        @Path("user_id") userId: Int,
        @Path("tariff_id") tariffId: Int,
    ): BaseResponse<String>

}