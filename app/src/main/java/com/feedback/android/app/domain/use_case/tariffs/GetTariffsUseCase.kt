package com.feedback.android.app.domain.use_case.tariffs

import android.util.Log
import com.feedback.android.app.common.Utils
import com.feedback.android.app.common.Resource
import com.feedback.android.app.common.operateFunInUseCase
import com.feedback.android.app.data.remote.dto.toTariffModel
import com.feedback.android.app.domain.model.TariffModel
import com.feedback.android.app.domain.repository.TariffsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetTariffsUseCase @Inject constructor(
    private val tariffsRepository: TariffsRepository
) {
    operator fun invoke(): Flow<Resource<List<TariffModel>>> = flow {
        operateFunInUseCase(
            tryBlock = {
                emit(Resource.Loading<List<TariffModel>>())
                val tariffsResponse =
                    tariffsRepository.getTariffs(Utils.userData?.tariffId?.toString() ?: "-1")
                if (tariffsResponse.status == "ok") {
                    val tariffs = tariffsResponse.data?.map { it.toTariffModel() } ?: listOf()
                    emit(Resource.Success<List<TariffModel>>(tariffs))
                } else {
                    emit(Resource.Error<List<TariffModel>>(tariffsResponse.error.toString()))
                }
            },
            onFail = { e ->
                Log.e("ERROR_LOG", "invoke: $e")
                emit(Resource.Error<List<TariffModel>>(e))
            }
        )
    }
}