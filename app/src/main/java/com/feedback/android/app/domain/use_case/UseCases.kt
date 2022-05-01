package com.feedback.android.app.domain.use_case

import com.feedback.android.app.domain.use_case.tariffs.GetTariffsUseCase
import com.feedback.android.app.domain.use_case.user.*

data class UseCases(
    val findUserByPhone: FindUserByPhoneUseCase,
    val auth: AuthUseCase,
    val saveUserData: SaveUserDataUseCase,
    val getUserData: GetUserDataUseCase,
    val changePinCode: ChangePinCodeUseCase,
    val sendPinCode: SendPinCodeUseCase,
    val checkPinCode: CheckPinCodeUseCase,
    val getTariffs: GetTariffsUseCase,
    val searchUsers: SearchUsersUseCase,
    val deleteUser: DeleteUserUseCase,
    val setUserTariff: SetUserTariffUseCase,
    val cancelUserTariff: CancelUserTariffUseCase,
    val getAllUsers: GetAllUsersUseCase,
    val getAllFeedbacks: GetAllFeedbacksUseCase,
    val payTariff: PayTariffUseCase,
    val checkPayment: CheckPaymentUseCase
)
