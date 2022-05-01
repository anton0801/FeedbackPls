package com.feedback.android.app.data.repository

import com.feedback.android.app.data.remote.ApiService
import com.feedback.android.app.data.remote.dto.BaseResponse
import com.feedback.android.app.data.remote.dto.TariffDto
import com.feedback.android.app.domain.repository.TariffsRepository
import javax.inject.Inject

class TariffsRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : TariffsRepository {
    override suspend fun getTariffs(
        currentUserTariffId: String
    ): BaseResponse<List<TariffDto>> = apiService.getTariffs(currentUserTariffId)
}