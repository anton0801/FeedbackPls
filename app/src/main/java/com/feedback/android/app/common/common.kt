package com.feedback.android.app.common

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.feedback.android.app.presentation.MainActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.getSystemService
import kotlin.collections.HashMap


val monthsName = listOf(
    "Январь",
    "Февраль",
    "Март",
    "Апрель",
    "Май",
    "Июнь",
    "Июль",
    "Август",
    "Сентябрь",
    "Октябрь",
    "Ноябрь",
    "Декабрь"
)

fun Fragment.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this.requireView(), message, duration).show()
}

fun AppCompatActivity.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(findViewById(android.R.id.content), message, duration).show()
}

fun Fragment.openKeyboard() {
    val inputMethodManager =
        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInputFromWindow(
        this.requireView().applicationWindowToken,
        InputMethodManager.SHOW_IMPLICIT, 0
    )
}

fun Fragment.closeKeyboard() {
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm!!.hideSoftInputFromWindow(view!!.windowToken, 0)
}

fun Fragment.restartMainActivity(options: HashMap<String, String>? = null) {
    activity?.finish()
    startActivity(Intent(context, MainActivity::class.java).apply {
        options?.forEach { option ->
            putExtra(option.key, option.value)
        }
    })
}

inline fun operateFunInUseCase(
    tryBlock: () -> Unit,
    onFail: (String) -> Unit
) = try {
    tryBlock()
} catch (e: HttpException) {
    onFail(e.localizedMessage ?: "Что-то пошло не так")
    null
} catch (e: IOException) {
    onFail(
        e.localizedMessage
            ?: "Не удалось подключиться к серверу! Проверьте ваше подключение к интернету"
    )
    null
} catch (e: Exception) {
    onFail(e.localizedMessage ?: "Что-то пошло не так")
    null
}

fun getAllMonthsNames(): List<String> = monthsName

fun getMonthNameFromNumber(monthNumber: Int) = monthsName[monthNumber]

fun getMonthIndexByName(name: String): Int = monthsName.indexOf(name)

fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

suspend fun downloadFileFromServer(saveDir: File, fileUrl: String) =
    flow<File?> {
        val okHttpBuilder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
        val okHttpClient = okHttpBuilder.build()
        val request = Request.Builder().url(fileUrl).build()
        val response = okHttpClient.newCall(request).execute()
        val body = response.body
        val responseCode = response.code
        if (responseCode >= HttpURLConnection.HTTP_OK &&
            responseCode < HttpURLConnection.HTTP_MULT_CHOICE &&
            body != null
        ) {
            body.byteStream().apply {
                saveDir.outputStream().use { fileOut ->
                    copyTo(fileOut)
                }
                emit(saveDir)
            }
        } else {
            emit(null)
        }
    }

fun Fragment.buildAlertDialog(title: String, message: String, okAction: () -> Unit) {
    AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Да") { dialog, _ ->
            okAction()
            dialog.dismiss()
        }
        .setNegativeButton("Нет", null)
        .show()
}