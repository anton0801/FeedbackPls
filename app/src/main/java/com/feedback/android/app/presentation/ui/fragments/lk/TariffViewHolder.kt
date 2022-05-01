package com.feedback.android.app.presentation.ui.fragments.lk

import android.view.View
import com.feedback.android.app.R
import com.feedback.android.app.common.BaseViewHolder
import com.feedback.android.app.databinding.TariffListItemBinding
import com.feedback.android.app.domain.model.TariffModel
import java.text.SimpleDateFormat
import java.util.*

class TariffViewHolder(
    itemView: View,
    private val payedTariffId: Int,
    private val payedTime: String?,
    private val onItemPayExtendBtnClicked: (TariffModel, Boolean) -> Unit
) : BaseViewHolder<TariffModel>(itemView) {
    override fun bind(item: TariffModel) {
        val binding = TariffListItemBinding.bind(itemView)
        binding.tariffName.text = "Тариф “${item.tariffName}”"
        binding.tariffPrice.text = "${item.tariffPrice} ₽"
        binding.tariffPaymentType.text = if (item.paymentType == 0) {
            val endDay = SimpleDateFormat("dd/MM/yyyy").format(getDate())
            binding.tariffDesc.text = item.tariffDesc.replace("%datetime%", payedTime ?: endDay)
            " / месяц"
        } else {
            binding.tariffDesc.text = item.tariffDesc
            " единоразово"
        }
        if (payedTariffId != -1 && item.id == payedTariffId) {
            binding.payOrExtendBtn.text = itemView.context.resources.getString(R.string.extend_text)
        }
        binding.payOrExtendBtn.setOnClickListener {
            onItemPayExtendBtnClicked(item, payedTariffId != -1 && item.id == payedTariffId)
        }
    }

    private fun getDate(): Date {
        val dt = Date()
        val c = Calendar.getInstance()
        c.time = dt
        c.add(Calendar.DATE, 30)
        return c.time
    }
}