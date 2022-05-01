package com.feedback.android.app.domain.repository

import com.feedback.android.app.data.remote.dto.BaseResponse
import com.feedback.android.app.data.remote.dto.TariffDto
import com.feedback.android.app.domain.model.TariffModel

interface TariffsRepository {
    suspend fun getTariffs(currentUserTariffId: String): BaseResponse<List<TariffDto>>
}