package com.feedback.android.app.presentation.ui.fragments.moderator

import com.feedback.android.app.data.remote.dto.FeedbackDto
import com.feedback.android.app.domain.model.UserModel

data class ModeratorExportData(
    val users: List<UserModel> = emptyList(),
    val feedbacks: List<FeedbackDto> = emptyList()
)