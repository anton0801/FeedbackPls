package com.feedback.android.app.presentation.ui.fragments.lk.webview

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.feedback.android.app.R
import com.feedback.android.app.common.restartMainActivity
import com.feedback.android.app.common.showSnackbar
import com.feedback.android.app.common.viewBinding
import com.feedback.android.app.databinding.FragmentWebViewBinding
import kotlinx.coroutines.flow.collectLatest

class WebViewFragment : Fragment() {

    private val binding: FragmentWebViewBinding by viewBinding(FragmentWebViewBinding::inflate)
    private val args: WebViewFragmentArgs by navArgs()

    private val vm: WebViewViewModel by activityViewModels()

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
                        is WebViewViewModel.UiEvent.OpenUrl -> {
                            binding.webView.loadUrl(uiEvent.url)
                        }
                        is WebViewViewModel.UiEvent.SuccessPayment -> {
                            restartMainActivity(hashMapOf("select_tab" to R.id.tariffs.toString()))
                        }
                    }
                }
            }
        }

        with(binding.webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setAppCacheEnabled(true)
        }
        binding.webView.webViewClient = MyWebViewClient()
        binding.webView.webChromeClient = MyChromeClient { progress ->
            binding.progressBar.isVisible = progress != 100
        }
        val xCookieManager = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT >= 21) {
            xCookieManager.setAcceptThirdPartyCookies(binding.webView, true)
        } else {
            xCookieManager.setAcceptCookie(true)
        }

        vm.onEvent(WebViewViewModel.UiEvent.OpenUrl(args.url))

        vm.checkPaymentStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let { error ->
                        showSnackbar(error)
                        findNavController().navigateUp()
                    }
                    state.data?.let { response ->
                        if (response.data?.isDigitsOnly() == true) {
                            vm.onEvent(WebViewViewModel.UiEvent.SuccessPayment)
                        }
                    }
                }
            }
        }
    }

    private var isYoomoneyWebSiteOpened = false

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            Log.d("LOG_D_D", "onPageStarted: $url")
            if (isYoomoneyWebSiteOpened) {
                if (url?.contains("check") == true) {
                    binding.webView.isVisible = false
                    vm.onEvent(WebViewViewModel.UiEvent.CheckPayment(args.userId, args.tariffId))
                } else {
                    binding.webView.isVisible = true
                }
            } else {
                if (url?.contains("yoomoney") == true) {
                    isYoomoneyWebSiteOpened = true
                }
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            view?.loadUrl(request?.url.toString())
            return true
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            view?.loadUrl(url.toString())
            return true
        }
    }

    private class MyChromeClient(val onProgressChangeListener: (Int) -> Unit) : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            onProgressChangeListener(newProgress)
        }
    }

}