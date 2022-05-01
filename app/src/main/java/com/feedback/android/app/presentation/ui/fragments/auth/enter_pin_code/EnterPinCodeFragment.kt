package com.feedback.android.app.presentation.ui.fragments.auth.enter_pin_code

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.feedback.android.app.R
import com.feedback.android.app.databinding.FragmentEnterPinCodeBinding
import android.text.style.UnderlineSpan

import android.text.SpannableString
import android.view.MotionEvent
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.feedback.android.app.common.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class EnterPinCodeFragment : Fragment() {

    private val binding: FragmentEnterPinCodeBinding by viewBinding(FragmentEnterPinCodeBinding::inflate)
    private val args: EnterPinCodeFragmentArgs by navArgs()
    private val vm: EnterPinCodeVIewModel by viewModels()

    @Inject
    lateinit var sharedManager: SharedManager

    private var canResendPinCode = true
    private var resendPinCodeCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            vm.eventFlow.collectLatest { e ->
                e?.getContentIfNotHandled()?.let { uiEvent ->
                    when (uiEvent) {
                        EnterPinCodeVIewModel.UiEvent.ErrorSendPinCode -> {
                            countDownTimer?.onFinish()
                            countDownTimer?.cancel()
                        }
                        is EnterPinCodeVIewModel.UiEvent.ShowSnackbar -> {
                            showSnackbar(uiEvent.message)
                        }
                        is EnterPinCodeVIewModel.UiEvent.IncorrectPinCode -> {
                            binding.labelText.text = getString(R.string.error_pin_code_label)
                            binding.labelText.setTextColor(resources.getColor(R.color.colorError))
                            binding.otpView.setTextColor(resources.getColor(R.color.colorError))

                            binding.otpView.setText("")
                            binding.otpView.requestFocus()
                        }
                        is EnterPinCodeVIewModel.UiEvent.CorrectPinCode -> {
                            sharedManager.putBoolean(Constants.IS_AUTH_USER_KEY, true)
                            sharedManager.putString(Constants.USER_ID, uiEvent.userId)
                            vm.onEvent(EnterPinCodeVIewModel.UiEvent.GetUserData(uiEvent.userId))
                        }
                        is EnterPinCodeVIewModel.UiEvent.SendPinCode -> {
                            showSnackbar("Ваш запрос получен")
                        }
                    }
                }
            }
        }

        binding.otpView.requestFocus()
        openKeyboard()

        if (!args.isRegister) {
            try {
                val nameParts = args.userName.split(" ")
                val userName = nameParts[nameParts.size - 2]
                binding.userFoundName.text = getString(R.string.greetings_label, userName)
            } catch (e: Exception) {
                binding.userFoundName.text = getString(R.string.greetings_label, args.userName)
            }

            binding.helpLabel.isVisible = false
            binding.forgotPin.text = getString(R.string.forgot_pin_code)
        } else {
            vm.onEvent(EnterPinCodeVIewModel.UiEvent.SendPinCode(args.userPhone))

            binding.helpLabel.isVisible = true
            binding.labelText.text = getString(R.string.enter_pin_code_title)
            binding.forgotPin.text = getString(R.string.resend_code_text)
        }

        val content = SpannableString(binding.forgotPin.text.toString())
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        binding.forgotPin.text = content

        binding.otpView.setOtpCompletionListener { pinCode ->
            if (!args.isRegister || !args.isUserRecognizedAccount) {
                // when user is found and need to login
                vm.onEvent(
                    EnterPinCodeVIewModel.UiEvent.Auth(
                        phone = args.userPhone,
                        pincode = pinCode,
                        isUserRecognizedAccount = if (args.isUserRecognizedAccount) 1 else 0
                    )
                )
            } else {
                // when user is not found and need to register
                vm.onEvent(
                    EnterPinCodeVIewModel.UiEvent.CheckPinCode(
                        args.userPhone, pinCode
                    )
                )
            }
        }
        binding.otpView.doOnTextChanged { _, _, _, _ ->
            if (binding.otpView.currentTextColor == requireContext().resources.getColor(R.color.colorError)) {
                binding.otpView.setTextColor(resources.getColor(R.color.black))
            }
        }
        binding.otpView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                binding.otpView.requestFocus()
                openKeyboard()
            }
            true
        }

        binding.forgotPin.setOnClickListener {
            if (canResendPinCode) {
                vm.onEvent(EnterPinCodeVIewModel.UiEvent.SendPinCode(args.userPhone))
                startCountDownTimer()
                canResendPinCode = false
                resendPinCodeCount++
            }
        }

        vm.authLiveData.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let { error ->
                        showSnackbar(error)
                    }

                    state.data?.let { response ->
                        if (response.message == "success") {
                            vm.onEvent(EnterPinCodeVIewModel.UiEvent.CorrectPinCode(response.data.toString()))
                        } else {
                            vm.onEvent(EnterPinCodeVIewModel.UiEvent.IncorrectPinCode(response.message.toString()))
                        }
                    }
                }
            }
        })

        vm.sendPinCodeLiveData.observe(viewLifecycleOwner) { event ->
            if (resendPinCodeCount > 0) {
                event?.getContentIfNotHandled()?.let { state ->
                    binding.progressBar.isVisible = state.isLoading
                    if (!state.isLoading) {
                        state.error?.let {
                            vm.onEvent(EnterPinCodeVIewModel.UiEvent.ErrorSendPinCode)
                        }

                        state.data?.let { data ->
                            if (data.data == true) {
                                vm.onEvent(EnterPinCodeVIewModel.UiEvent.ShowSnackbar("ПИН-код выслан"))
                            }
                        }
                    }
                }
            }
        }

        vm.checkPinCodeLiveData.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let {
                        vm.onEvent(EnterPinCodeVIewModel.UiEvent.ShowSnackbar("Повторите попытку"))
                    }
                    state.data?.let { response ->
                        if (response.message == "success") {
                            vm.onEvent(EnterPinCodeVIewModel.UiEvent.CorrectPinCode(response.data.toString()))
                        } else {
                            vm.onEvent(EnterPinCodeVIewModel.UiEvent.IncorrectPinCode(response.message.toString()))
                        }
                    }
                }
            }
        }

        vm.userDataLiveData.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    if (state.data != null) {
                        val action = if (state.data.userType == "moderator")
                            R.id.action_enterPinCodeFragment_to_moderatorFragment
                        else
                            R.id.action_enterPinCodeFragment_to_profileFragment
                        try {
                            findNavController().navigate(action)
                        } catch (e: Exception) {
                        }

                        findNavController().graph = findNavController().graph.apply {
                            startDestination = if (state.data.userType == "moderator")
                                R.id.moderatorFragment
                            else
                                R.id.profileFragment
                        }
                        Utils.userData = state.data
                    }
                }
            }
        }
    }

    private var countDownTimer: CountDownTimer? = null

    private fun startCountDownTimer() {
        binding.otpView.setText("")
        countDownTimer = object : CountDownTimer(120 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = "0${TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)}"
                val s = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                millisUntilFinished
                            )
                        )
                val seconds = if (s <= 9) "0$s" else s
                binding.forgotPin.text = String.format(
                    "Код выслан. Запросить новый \nможно через: %s:%s",
                    minutes,
                    seconds
                );
            }

            override fun onFinish() {
                binding.forgotPin.text = if (args.isRegister) {
                    getString(R.string.resend_code_text)
                } else {
                    getString(R.string.forgot_pin_code)
                }
                canResendPinCode = true
            }
        }
        countDownTimer?.start()
    }

}