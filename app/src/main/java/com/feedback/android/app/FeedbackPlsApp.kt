package com.feedback.android.app

import android.app.Application
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FeedbackPlsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val config = YandexMetricaConfig.newConfigBuilder(BuildConfig.YANDEX_API_KEY).build()
        YandexMetrica.activate(this, config)
        YandexMetrica.enableActivityAutoTracking(this)
    }

}