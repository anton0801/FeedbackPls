package com.feedback.android.app.domain.use_case.user

import com.feedback.android.app.common.Constants
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.BaseResponse
import com.feedback.android.app.data.remote.dto.PayTariffDto
import com.feedback.android.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PayTariffUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: Int,
        tariffId: Int
    ): Flow<Resource<PayTariffDto>> =
        flow {
            operateFunInUseCase(
                tryBlock = {
                    emit(Resource.Loading<PayTariffDto>())
                    val payTariffResponse = userRepository.payTariff(userId, tariffId)
                    if (payTariffResponse.status == "ok")
                        emit(Resource.Success<PayTariffDto>(payTariffResponse.data!!))
                    else
                        emit(
                            Resource.Error<PayTariffDto>(
                                payTariffResponse.error ?: ""
                            )
                        )
                },
                onFail = { error ->
                    emit(Resource.Error<PayTariffDto>(error))
                }
            )
        }
}