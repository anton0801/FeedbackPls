package com.feedback.android.app.presentation.ui.fragments.moderator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.feedback.android.app.R
import com.feedback.android.app.common.*
import com.feedback.android.app.common.extensions.setUnderlineEffect
import com.feedback.android.app.data.remote.dto.toUserModel
import com.feedback.android.app.databinding.FragmentModeratorBinding
import com.feedback.android.app.domain.model.UserModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ModeratorFragment : Fragment() {

    private val binding: FragmentModeratorBinding by viewBinding(FragmentModeratorBinding::inflate)
    private val vm: ModeratorViewModel by viewModels()

    private lateinit var rvAdapter: RVAdapter<UserModel>
    private val onItemCLickListener: (UserModel, Int) -> Unit = { item, clickType ->
        when (clickType) {
            ModeratorUserItemViewHolder.PERSONAL_PAGE_LINK_TYPE -> vm.onEvent(
                ModeratorViewModel.UiEvent.OpenLink(
                    "https://xn-----6kcicpbr6bkfi4aoch8mh.xn--p1ai/id${item.id}"
                )
            )
            ModeratorUserItemViewHolder.USER_SET_TARIFF_TYPE -> {
                buildAlertDialog(
                    "Уведомление",
                    "Вы уверены что хотите поставить тариф \"Акционный\" пользователю с именем \"${item.name ?: item.phone}\"?"
                ) {
                    vm.onEvent(ModeratorViewModel.UiEvent.SetUserTariff(item.id.toString()))
                }
            }
            ModeratorUserItemViewHolder.USER_CANCEL_TARIFF_TYPE -> {
                buildAlertDialog(
                    "Уведомление",
                    "Вы уверены что хотите убрать тариф \"Акционный\" пользователю с именем \"${item.name ?: item.phone}\"?"
                ) {
                    vm.onEvent(ModeratorViewModel.UiEvent.CancelUserTariff(item.id.toString()))
                }
            }
            ModeratorUserItemViewHolder.USER_DELETE_TYPE -> {
                buildAlertDialog(
                    "Уведомление",
                    "Вы уверены что хотите удалить пользователя с именем \"${item.name ?: item.phone}\"?"
                ) {
                    vm.onEvent(ModeratorViewModel.UiEvent.DeleteUser(item.id.toString()))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            vm.eventFlow.collectLatest { event ->
                event?.getContentIfNotHandled()?.let { uiEvent ->
                    when (uiEvent) {
                        is ModeratorViewModel.UiEvent.OpenLink -> {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uiEvent.url)))
                        }
                        is ModeratorViewModel.UiEvent.ShowSnackbar -> {
                            showSnackbar(uiEvent.message)
                        }
                    }
                }
            }
        }

        setTabs()
        applyUserModelToUi()

        rvAdapter = RVAdapter { parent, _ ->
            ModeratorUserItemViewHolder(
                layoutInflater.inflate(R.layout.moderator_user_list_item, parent, false),
                onItemCLickListener
            )
        }
        binding.usersRv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(object :
                PaginationScrollListener(layoutManager as LinearLayoutManager) {
                override fun loadMoreItems() {
                    vm.isLoading = true
                    vm.page += 1
                    vm.onEvent(ModeratorViewModel.UiEvent.SearchUsers)
                }

                override val isLastPage: Boolean = vm.isLastPage
                override val isLoading: Boolean = vm.isLoading
            })
        }
        binding.translateToExcel.setUnderlineEffect()

        binding.searchName.getInputField().doOnTextChanged { text, _, _, _ ->
            vm.searchName = text.toString()
            vm.searchUserId = null
            vm.onEvent(ModeratorViewModel.UiEvent.SearchUsers)
        }
        binding.searchId.getInputField().doOnTextChanged { text, _, _, _ ->
            vm.searchUserId = text.toString()
            vm.searchName = null
            vm.onEvent(ModeratorViewModel.UiEvent.SearchUsers)
        }
        binding.saveNewPinBtn.setOnClickListener {
            vm.onEvent(
                ModeratorViewModel.UiEvent.ChangePinCode(
                    phone = Utils.userData?.phone ?: "",
                    newPinCode = binding.newPinInput.text.toString()
                )
            )
        }
        binding.translateToExcel.setOnClickListener {
            vm.onEvent(ModeratorViewModel.UiEvent.ExportDataToExcel)
        }

        setObservers()
    }

    private fun applyUserModelToUi() {
        Utils.userData?.let { userModel ->
            val currentPin = Base64.decode(userModel.pinCode, Base64.DEFAULT)
            binding.currentPin.text = String(currentPin, Charsets.UTF_8)
        }
    }

    private fun setObservers() {
        vm.searchResults.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { state ->
                vm.isLoading = state.isLoading
                binding.progressBar.isVisible = state.isLoading

                if (!state.isLoading) {
                    state.data?.let { responseData ->
                        vm.isLastPage = responseData.lastPage == vm.page
                        rvAdapter.setItems(responseData.data.map { it.toUserModel() })
                    }
                }
            }
        }

        vm.deleteUserResponse.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let { error ->
                        vm.onEvent(ModeratorViewModel.UiEvent.ShowSnackbar(error))
                    }

                    state.data?.let { responseData ->
                        if (responseData.data?.isDigitsOnly() == true) {
                            rvAdapter.getItems().find { it.id == responseData.data?.toInt() }
                                ?.also {
                                    rvAdapter.removeItem(it)
                                    vm.onEvent(ModeratorViewModel.UiEvent.ShowSnackbar("Пользователь был удален"))
                                }
                        }
                    }
                }
            }
        }

        vm.setUserTariffResponse.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let { error ->
                        vm.onEvent(ModeratorViewModel.UiEvent.ShowSnackbar(error))
                    }

                    state.data?.let { responseData ->
                        updateItem(responseData.data?.toIntOrNull(), false)
                    }
                }
            }
        }

        vm.cancelUserTariffResponse.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let { error ->
                        vm.onEvent(ModeratorViewModel.UiEvent.ShowSnackbar(error))
                    }

                    state.data?.let { responseData ->
                        updateItem(responseData.data?.toIntOrNull(), true)
                    }
                }
            }
        }

        vm.changePinCodeState.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let { error ->
                        vm.onEvent(ModeratorViewModel.UiEvent.ShowSnackbar(error))
                    }
                    state.data?.let { data ->
                        binding.currentPin.text = binding.newPinInput.text.toString()
                        binding.newPinInput.setText("")
                        vm.onEvent(ModeratorViewModel.UiEvent.ShowSnackbar(data.data.toString()))
                    }
                }
            }
        }

        vm.dataToExportLiveData.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let { error ->
                        vm.onEvent(ModeratorViewModel.UiEvent.ShowSnackbar(error))
                    }

                    state.data?.let { data ->
                        val result = ExportDataToExcelFile().export(
                            requireContext(), data
                        )
                        when (result) {
                            ExportDataToExcelFile.ExportResult.SUCCESS -> {
                                vm.onEvent(ModeratorViewModel.UiEvent.ShowSnackbar("выгрузка произошла успешно"))
                            }
                            ExportDataToExcelFile.ExportResult.FAIL -> {
                                vm.onEvent(ModeratorViewModel.UiEvent.ShowSnackbar("Что-то пошло не так"))
                            }
                            ExportDataToExcelFile.ExportResult.NOT_PERMITTED -> {
                                vm.onEvent(ModeratorViewModel.UiEvent.ShowSnackbar("В доступе отказано"))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateItem(userId: Int?, isCancellation: Boolean) {
        rvAdapter.getItems().find { it.id == userId }?.let { item ->
                val newItem = if (!isCancellation)
                    item.copy(
                        isAccountPayed = 1,
                        tariffId = 3,
                        tariffName = "Акционный"
                    )
                else
                    item.copy(
                        isAccountPayed = 0,
                        tariffId = null,
                        tariffName = null
                    )
                Log.d("LOG_D_D", "updateItem: $isCancellation old item $item")
                Log.d("LOG_D_D", "updateItem: new item $newItem")
                rvAdapter.updateItem(item, newItem)
            }
    }

    private fun setTabs() {
        val activeTypeface = ResourcesCompat.getFont(requireContext(), R.font.sansation_regular)
        val disabledTypeface = ResourcesCompat.getFont(requireContext(), R.font.sansation_light)

        binding.tabUsers.setOnClickListener {
            binding.moderatorContainer.isVisible = true
            binding.changePinContainer.isVisible = false

            binding.tabUsers.typeface = activeTypeface
            binding.tabChangePin.typeface = disabledTypeface

            binding.tabChangePin.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                resources.getDimension(R.dimen._5ssp)
            )
            binding.tabUsers.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                resources.getDimension(R.dimen._6ssp)
            )
        }
        binding.tabChangePin.setOnClickListener {
            binding.moderatorContainer.isVisible = false
            binding.changePinContainer.isVisible = true

            binding.tabChangePin.typeface = activeTypeface
            binding.tabUsers.typeface = disabledTypeface

            binding.tabUsers.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                resources.getDimension(R.dimen._5ssp)
            )
            binding.tabChangePin.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                resources.getDimension(R.dimen._6ssp)
            )
        }
    }

}