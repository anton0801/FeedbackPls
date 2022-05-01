package com.feedback.android.app.presentation.ui.fragments.moderator

import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.feedback.android.app.R
import com.feedback.android.app.common.BaseViewHolder
import com.feedback.android.app.common.extensions.downloadAndSetImageViewWithShimmer
import com.feedback.android.app.common.extensions.parseTimestampToBeautifulDate
import com.feedback.android.app.common.extensions.setUnderlineEffect
import com.feedback.android.app.databinding.ModeratorUserListItemBinding
import com.feedback.android.app.domain.model.UserModel

class ModeratorUserItemViewHolder constructor(
    itemView: View,
    private val onItemClickListener: (UserModel, Int) -> Unit
) : BaseViewHolder<UserModel>(itemView) {

    override fun bind(item: UserModel) {
        val binding = ModeratorUserListItemBinding.bind(itemView)

        binding.userAvatar.downloadAndSetImageViewWithShimmer(item.avatar.toString())
        binding.userName.text = if (item.name.isNullOrBlank()) item.phone else item.name
        binding.userId.text = "id${item.id}"
        binding.userTariff.text = "Тариф: ${item.tariffName}"
        binding.userRegistrationDate.text = item.createdUt.parseTimestampToBeautifulDate()

        binding.userSetTariff.isVisible = !item.tariffName.equals("бессрочный", true)
                && !item.tariffName.equals("акционный", true)
        binding.userCancelTariff.isVisible = item.tariffName.equals("акционный", true)
        binding.userStatus.setImageResource(
            if (item.isPublished == 1 && !item.isAccountArchived && item.userType != "moderator") {
                R.drawable.ic_user_published
            } else {
                if (item.userType == "moderator")
                    binding.userModeratorLabel.isVisible = true
                R.drawable.ic_user_unpublished
            }
        )
        binding.userDelete.isVisible = !item.isAccountArchived && item.userType != "moderator"
        if (item.isPublished != 1 || item.isAccountArchived) {
            binding.personalPageLink.setTextColor(
                binding.root.context.resources.getColor(R.color.colorInactive)
            )
        }

        binding.personalPageLink.setUnderlineEffect()
        binding.userSetTariff.setUnderlineEffect()
        binding.userCancelTariff.setUnderlineEffect()

        binding.personalPageLink.setOnClickListener {
            if (item.isPublished == 1 && !item.isAccountArchived) {
                onItemClickListener(item, PERSONAL_PAGE_LINK_TYPE)
            }
        }
        binding.userCancelTariff.setOnClickListener {
            onItemClickListener(item, USER_CANCEL_TARIFF_TYPE)
        }
        binding.userSetTariff.setOnClickListener {
            onItemClickListener(item, USER_SET_TARIFF_TYPE)
        }
        binding.userDelete.setOnClickListener {
            onItemClickListener(item, USER_DELETE_TYPE)
        }
    }

    companion object {
        const val PERSONAL_PAGE_LINK_TYPE = 1
        const val USER_CANCEL_TARIFF_TYPE = 2
        const val USER_SET_TARIFF_TYPE = 3
        const val USER_DELETE_TYPE = 4
    }

}