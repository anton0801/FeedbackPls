package com.feedback.android.app.presentation.ui.fragments.auth.user_found

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.feedback.android.app.R
import com.feedback.android.app.common.viewBinding
import com.feedback.android.app.databinding.FragmentUserFoundBinding
import kotlinx.coroutines.flow.collectLatest

class UserFoundFragment : Fragment() {

    private val binding: FragmentUserFoundBinding by viewBinding(FragmentUserFoundBinding::inflate)
    private val args: UserFoundFragmentArgs by navArgs()
    private val vm: UserFoundViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userFoundName.text = args.userName

        lifecycleScope.launchWhenStarted {
            vm.eventFlow.collectLatest { event ->
                event?.getContentIfNotHandled()?.let { uiEvent ->
                    when (uiEvent) {
                        is UserFoundViewModel.UiEvent.Yes -> {
                            val action =
                                UserFoundFragmentDirections.actionUserFoundFragmentToEnterPinCodeFragment(
                                    args.userId, args.userName, args.userPhone, false, true
                                )
                            findNavController().navigate(action)
                        }
                        is UserFoundViewModel.UiEvent.No -> {
                            val action =
                                UserFoundFragmentDirections.actionUserFoundFragmentToEnterPinCodeFragment(
                                    args.userId, args.userName, args.userPhone, true,
                                    false
                                )
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }

        binding.yesBtn.setOnClickListener { vm.onEvent(UserFoundViewModel.UiEvent.Yes) }
        binding.noBtn.setOnClickListener { vm.onEvent(UserFoundViewModel.UiEvent.No) }
    }

}