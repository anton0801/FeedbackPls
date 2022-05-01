package com.feedback.android.app.common

import android.content.Context
import android.os.Environment
import android.util.Log
import com.feedback.android.app.domain.model.UserModel
import com.feedback.android.app.presentation.ui.fragments.moderator.ModeratorExportData
import org.apache.poi.hssf.usermodel.HSSFWorkbook

import org.apache.poi.ss.usermodel.CellStyle

import org.apache.poi.hssf.usermodel.HSSFCellStyle

import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*


class ExportDataToExcelFile {

    fun export(context: Context, data: ModeratorExportData): ExportResult {
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            return ExportResult.NOT_PERMITTED
        }

        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet(EXCEL_SHEET_NAME)

        val cellStyle: CellStyle = workbook.createCellStyle()
        cellStyle.fillForegroundColor = HSSFColor.AQUA.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.wrapText = true
        cellStyle.alignment = CellStyle.ALIGN_CENTER

        val usersLabelRow = sheet.createRow(0)

        val cellLabel = usersLabelRow.createCell(0)
        cellLabel.setCellValue("Пользователи")
        cellLabel.setCellStyle(cellStyle)

        val usersTitleRow = sheet.createRow(1)

        val cellUserId = usersTitleRow.createCell(0)
        cellUserId.setCellValue("ID пользователя")
        cellUserId.setCellStyle(cellStyle)

        val cell1 = usersTitleRow.createCell(1)
        cell1.setCellValue("Имя пользователя")
        cell1.setCellStyle(cellStyle)

        val cell2 = usersTitleRow.createCell(2)
        cell2.setCellValue("Номер телефона")
        cell2.setCellStyle(cellStyle)

        val cell3 = usersTitleRow.createCell(3)
        cell3.setCellValue("Профессия")
        cell3.setCellStyle(cellStyle)

        val cell4 = usersTitleRow.createCell(4)
        cell4.setCellValue("URL аватарки")
        cell4.setCellStyle(cellStyle)

        val cell5 = usersTitleRow.createCell(5)
        cell5.setCellValue("Email")
        cell5.setCellStyle(cellStyle)

        val cell6 = usersTitleRow.createCell(6)
        cell6.setCellValue("Райтинг")
        cell6.setCellStyle(cellStyle)

        val cell7 = usersTitleRow.createCell(7)
        cell7.setCellValue("Количество отзывов")
        cell7.setCellStyle(cellStyle)

        val cell8 = usersTitleRow.createCell(8)
        cell8.setCellValue("О себе")
        cell8.setCellStyle(cellStyle)

        val cell9 = usersTitleRow.createCell(9)
        cell9.setCellValue("дата рождения")
        cell9.setCellStyle(cellStyle)

        val cell10 = usersTitleRow.createCell(10)
        cell10.setCellValue("Тариф")
        cell10.setCellStyle(cellStyle)

        val cell11 = usersTitleRow.createCell(11)
        cell11.setCellValue("веб сайт")
        cell11.setCellStyle(cellStyle)

        val cell12 = usersTitleRow.createCell(12)
        cell12.setCellValue("Дата регистраций")
        cell12.setCellStyle(cellStyle)

        data.users.forEachIndexed { index, userModel ->
            val userRow = sheet.createRow(index + 2)

            val cellUserIdValue = userRow.createCell(0)
            cellUserIdValue.setCellValue(userModel.id.toString())

            val cellUserName = userRow.createCell(1)
            cellUserName.setCellValue(userModel.name)

            val cellPhone = userRow.createCell(2)
            cellPhone.setCellValue(userModel.phone)

            val cellProfession = userRow.createCell(3)
            cellProfession.setCellValue(userModel.profession)

            val cellAvatar = userRow.createCell(4)
            cellAvatar.setCellValue(userModel.avatar)

            val cellEmail = userRow.createCell(5)
            cellEmail.setCellValue(userModel.email)

            val cellRating = userRow.createCell(6)
            cellRating.setCellValue(userModel.rating)

            val feedbackCount = data.feedbacks.filter { it.specialistId == userModel.id }.size
            val cellFeedbackCount = userRow.createCell(7)
            cellFeedbackCount.setCellValue(feedbackCount.toString())

            val cellAbout = userRow.createCell(8)
            cellAbout.setCellValue(userModel.about)

            val cellBirthday = userRow.createCell(9)
            cellBirthday.setCellValue(userModel.birthday)

            val cellTariff = userRow.createCell(10)
            cellTariff.setCellValue(userModel.tariffName)

            val cellWebsite = userRow.createCell(11)
            cellWebsite.setCellValue(userModel.website)

            val cellRegistrationDate = userRow.createCell(12)
            cellRegistrationDate.setCellValue(userModel.createdUt)
        }

        val offsetOfTop = data.users.size + 5

        val feedbacksLabelRow = sheet.createRow(offsetOfTop)

        val cellFeedbacksLabel = feedbacksLabelRow.createCell(0)
        cellFeedbacksLabel.setCellValue("Отзывы")
        cellFeedbacksLabel.setCellStyle(cellStyle)

        val feedbacksTitleRow = sheet.createRow(offsetOfTop + 1)

        val cellFeedback1 = feedbacksTitleRow.createCell(0)
        cellFeedback1.setCellValue("ID специалиста")
        cellFeedback1.setCellStyle(cellStyle)

        val cellFeedback2 = feedbacksTitleRow.createCell(1)
        cellFeedback2.setCellValue("Имя специалиста")
        cellFeedback2.setCellStyle(cellStyle)

        val cellFeedback3 = feedbacksTitleRow.createCell(2)
        cellFeedback3.setCellValue("Имя пользователя")
        cellFeedback3.setCellStyle(cellStyle)

        val cellFeedback4 = feedbacksTitleRow.createCell(3)
        cellFeedback4.setCellValue("Комментарий")
        cellFeedback4.setCellStyle(cellStyle)

        val cellFeedback5 = feedbacksTitleRow.createCell(4)
        cellFeedback5.setCellValue("URL до видео отзыва")
        cellFeedback5.setCellStyle(cellStyle)

        val cellFeedback6 = feedbacksTitleRow.createCell(5)
        cellFeedback6.setCellValue("URL до фото комментария")
        cellFeedback6.setCellStyle(cellStyle)

        val cellFeedback7 = feedbacksTitleRow.createCell(6)
        cellFeedback7.setCellValue("Тип комментария")
        cellFeedback7.setCellStyle(cellStyle)

        val cellFeedback8 = feedbacksTitleRow.createCell(7)
        cellFeedback8.setCellValue("Оценка")
        cellFeedback8.setCellStyle(cellStyle)

        val cellFeedback9 = feedbacksTitleRow.createCell(8)
        cellFeedback9.setCellValue("Дата публикаций")
        cellFeedback9.setCellStyle(cellStyle)

        data.feedbacks.forEachIndexed { index, feedbackItem ->
            val feedbackRow = sheet.createRow(offsetOfTop + index + 2)

            val cellSpecialistId = feedbackRow.createCell(0)
            cellSpecialistId.setCellValue(feedbackItem.specialistId.toString())

            val specialistName = data.users.find { it.id == feedbackItem.specialistId }?.name
            val cellSpecialistName = feedbackRow.createCell(1)
            cellSpecialistName.setCellValue(specialistName.toString())

            val cellUserName = feedbackRow.createCell(2)
            cellUserName.setCellValue(feedbackItem.userName)

            val cellComment = feedbackRow.createCell(3)
            cellComment.setCellValue(feedbackItem.comment)

            val cellSrc = feedbackRow.createCell(4)
            cellSrc.setCellValue(feedbackItem.src)

            val cellImgSrc = feedbackRow.createCell(5)
            cellImgSrc.setCellValue(feedbackItem.imgSrc)

            val cellType = feedbackRow.createCell(6)
            cellType.setCellValue(feedbackItem.type)

            val cellRating = feedbackRow.createCell(7)
            cellRating.setCellValue(feedbackItem.rating.toString())

            val cellCreatedAt = feedbackRow.createCell(8)
            cellCreatedAt.setCellValue(feedbackItem.createdAt)
        }

        if (storeExcelInStorage(
                context,
                "feedback_pls_moderator_${Date().time / 1000}.xls",
                workbook
            )
        )
            return ExportResult.SUCCESS
        return ExportResult.FAIL
    }

    private fun isExternalStorageReadOnly(): Boolean {
        val externalStorageState: String = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED_READ_ONLY == externalStorageState
    }

    private fun isExternalStorageAvailable(): Boolean {
        val externalStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == externalStorageState
    }

    private fun storeExcelInStorage(
        context: Context,
        fileName: String,
        workbook: Workbook
    ): Boolean {
        var isSuccess: Boolean
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(dir, fileName)
        var fileOutputStream: FileOutputStream? = null
        Log.d("LOG_D", "storeExcelInStorage: $file")
        try {
            fileOutputStream = FileOutputStream(file)
            workbook.write(fileOutputStream)
            isSuccess = true
        } catch (e: IOException) {
            e.printStackTrace()
            isSuccess = false
        } catch (e: Exception) {
            e.printStackTrace()
            isSuccess = false
        } finally {
            try {
                fileOutputStream?.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return isSuccess
    }

    companion object {
        const val EXCEL_SHEET_NAME = "users_in_app"
    }

    enum class ExportResult {
        NOT_PERMITTED, FAIL, SUCCESS;
    }

}