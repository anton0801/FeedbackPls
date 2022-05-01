package com.feedback.android.app.presentation.ui.fragments.auth.enter_phone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.feedback.android.app.common.*
import com.feedback.android.app.common.extensions.setUnderlineEffect
import com.feedback.android.app.databinding.FragmentEnterPhoneBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class EnterPhoneFragment : Fragment() {

    private val binding: FragmentEnterPhoneBinding by viewBinding(FragmentEnterPhoneBinding::inflate)
    private val vm: EnterPhoneViewModel by viewModels()

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
                        is EnterPhoneViewModel.UiEvent.ChangePhoneInput -> changeStateBtn(uiEvent.text)
                        is EnterPhoneViewModel.UiEvent.Send -> {
                            if (binding.sendBtn.isActive) {
                                closeKeyboard()
                                vm.findUserByPhone(uiEvent.phone)
                            } else {
                                showSnackbar("Заполните все данные и чек боксы")
                            }
                        }
                        is EnterPhoneViewModel.UiEvent.OpenLink -> startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(uiEvent.url))
                        )
                    }
                }
            }
        }

        binding.phoneInput.doOnTextChanged { _, _, _, _ ->
            vm.onEvent(EnterPhoneViewModel.UiEvent.ChangePhoneInput(binding.phoneInput.rawText))
        }
        binding.sendBtn.setOnClickListener {
            vm.onEvent(
                EnterPhoneViewModel.UiEvent.Send(
                    binding.phoneInput.text.toString().replace("""[ ()+-]""".toRegex(), "")
                )
            )
        }

        checkBoxListener(
            binding.privacyPolicyCheckbox,
            binding.privacyPolicyText,
            Constants.SUBSCRIPTION_RULES_URL
        )
        checkBoxListener(
            binding.operationOfPersonalDataCheckbox,
            binding.operationOfPersonalDataText,
            Constants.OPERATE_PERSONAL_DATA_URL
        )

        vm.userData.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let { error ->
                        showSnackbar(error)
                    }

                    state.data?.let { userModel ->
                        val action =
                            EnterPhoneFragmentDirections.actionEnterPhoneFragmentToUserFoundFragment()
                                .apply {
                                    userId = userModel.id
                                    userName = userModel.name.toString()
                                    userPhone = userModel.phone.toString()
                                }
                        findNavController().navigate(action)
                    } ?: run {
                        if (state.error == null) {
                            val phone =
                                binding.phoneInput.text.toString()
                                    .replace("""[ ()+-]""".toRegex(), "")
                            val action =
                                EnterPhoneFragmentDirections.actionEnterPhoneFragmentToEnterPinCodeFragment(
                                    -1, "", phone, true, true
                                )
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        })
    }

    private fun changeStateBtn(text: String) {
        val length = text.length
        val isPhonePattern = Patterns.PHONE.matcher("+7$text").matches()
        binding.sendBtn.setState(
            length == 10 && isPhonePattern && binding.privacyPolicyCheckbox.isChecked
                    && binding.operationOfPersonalDataCheckbox.isChecked
        )
    }

    private fun checkBoxListener(checkBox: CheckBox, checkboxText: AppCompatTextView, url: String) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            vm.onEvent(EnterPhoneViewModel.UiEvent.ChangePhoneInput(binding.phoneInput.rawText))
        }

        checkboxText.setUnderlineEffect()
        checkboxText.setOnClickListener {
            vm.onEvent(EnterPhoneViewModel.UiEvent.OpenLink(url))
        }
    }

}