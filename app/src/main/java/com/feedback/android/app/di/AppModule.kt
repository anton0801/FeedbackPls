package com.feedback.android.app.di

import android.content.Context
import com.feedback.android.app.BuildConfig
import com.feedback.android.app.common.Constants
import com.feedback.android.app.common.SharedManager
import com.feedback.android.app.data.remote.ApiService
import com.feedback.android.app.data.repository.TariffsRepositoryImpl
import com.feedback.android.app.data.repository.UserRepositoryImpl
import com.feedback.android.app.domain.repository.TariffsRepository
import com.feedback.android.app.domain.repository.UserRepository
import com.feedback.android.app.domain.use_case.UseCases
import com.feedback.android.app.domain.use_case.tariffs.GetTariffsUseCase
import com.feedback.android.app.domain.use_case.user.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level =
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .addInterceptor {
                val r = it.request().newBuilder()
                    .addHeader("Authorization", "Bearer YXBpX2tleV9jcmVhdGVkXzE2NDc1NTAzNTA=")
                    .build()
                it.proceed(r)
            }
            .callTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideAppApi(httpClient: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService): UserRepository {
        return UserRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideTariffsRepository(apiService: ApiService): TariffsRepository {
        return TariffsRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideUseCases(
        userRepository: UserRepository,
        tariffsRepository: TariffsRepository
    ): UseCases {
        return UseCases(
            findUserByPhone = FindUserByPhoneUseCase(userRepository),
            auth = AuthUseCase(userRepository),
            saveUserData = SaveUserDataUseCase(userRepository),
            getUserData = GetUserDataUseCase(userRepository),
            changePinCode = ChangePinCodeUseCase(userRepository),
            getTariffs = GetTariffsUseCase(tariffsRepository),
            checkPinCode = CheckPinCodeUseCase(userRepository),
            sendPinCode = SendPinCodeUseCase(userRepository),
            searchUsers = SearchUsersUseCase(userRepository),
            deleteUser = DeleteUserUseCase(userRepository),
            setUserTariff = SetUserTariffUseCase(userRepository),
            cancelUserTariff = CancelUserTariffUseCase(userRepository),
            getAllFeedbacks = GetAllFeedbacksUseCase(userRepository),
            getAllUsers = GetAllUsersUseCase(userRepository),
            payTariff = PayTariffUseCase(userRepository),
            checkPayment = CheckPaymentUseCase(userRepository)
        )
    }

    @Provides
    @Singleton
    fun provideSharedManager(@ApplicationContext context: Context): SharedManager {
        return SharedManager(context)
    }

}