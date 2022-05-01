package com.feedback.android.app.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.feedback.android.app.R
import com.feedback.android.app.common.*
import com.feedback.android.app.common.extensions.checkIfNeedsToReLogin
import com.feedback.android.app.databinding.ActivityMainBinding
import com.feedback.android.app.presentation.ui.fragments.lk.ProfileFragment
import com.feedback.android.app.presentation.ui.fragments.moderator.ModeratorFragment
import com.feedback.android.app.presentation.ui.fragments.splash.SplashFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)
    private val vm: MainViewModel by viewModels()

    private lateinit var navController: NavController
    private var handler: Handler? = null

    @Inject
    lateinit var sharedManager: SharedManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())

        vm.userData.observe(this, Observer { e ->
            e.getContentIfNotHandled()?.let { state ->
                if (!state.isLoading) {
                    if (state.data == null) {
                        // navigate to needed screen with animation
                        try {
                            navController.navigate(R.id.action_splashFragment_to_enterPhoneFragment)
                        } catch (e: Exception) {
                        }
                        handler?.postDelayed(Runnable {
                            navController.graph = navController.graph.apply {
                                startDestination = R.id.enterPhoneFragment
                            }
                        }, 500)
                    } else {
                        // navigate to needed screen with animation
                        val needToReLogin = sharedManager.getString(Constants.LAST_VISITED_TIME)
                            .checkIfNeedsToReLogin(sharedManager)
                        val action = if (!needToReLogin) {
                            if (state.data.userType == "moderator")
                                SplashFragmentDirections.actionSplashFragmentToModeratorFragment()
                            else
                                SplashFragmentDirections.actionSplashFragmentToProfileFragment()
                        } else {
                            SplashFragmentDirections.actionSplashFragmentToUserFoundFragment()
                                .apply {
                                    userId = state.data.id
                                    userName = state.data.name.toString()
                                    userPhone = state.data.phone.toString()
                                }
                        }
                        try {
                            navController.navigate(action)
                        } catch (e: Exception) {
                        }
                        if (!needToReLogin) {
                            navController.graph = navController.graph.apply {
                                startDestination = if (state.data.userType == "moderator")
                                    R.id.moderatorFragment
                                else
                                    R.id.profileFragment
                            }
                        }
                    }
                    Utils.userData = state.data
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        navController = binding.navHost.findNavController()
        if (vm.userData.value == null)
            checkAuthAndReplacePageIfNeed()
    }

    fun needSelectAnyCertainTab(): Int? {
        if (intent != null) {
            val tabIdForSelect = intent.getStringExtra("select_tab")
            if (tabIdForSelect != null) {
                return tabIdForSelect.toInt()
            }
        }
        return null
    }

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host)
        val currentFragment = navHostFragment!!.childFragmentManager.fragments[0]
        if (currentFragment is ProfileFragment || currentFragment is ModeratorFragment) {
            sharedManager.putBoolean(Constants.IS_AUTH_USER_KEY, false)
            sharedManager.putString(Constants.USER_ID, "")
        }
        super.onBackPressed()
    }

    private fun checkAuthAndReplacePageIfNeed() {
        if (sharedManager.getString(Constants.USER_ID).isNotBlank()) {
            vm.getUserData(sharedManager.getString(Constants.USER_ID))
        } else {
            if (navController.graph.startDestination != R.id.enterPhoneFragment) {
                try {
                    navController.navigate(R.id.action_splashFragment_to_enterPhoneFragment)
                } catch (e: Exception) {
                }
                handler?.postDelayed({
                    navController.graph = navController.graph.apply {
                        startDestination = R.id.enterPhoneFragment
                    }
                }, 500)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        sharedManager.putString(Constants.LAST_VISITED_TIME, Date().time.toString())
    }

}