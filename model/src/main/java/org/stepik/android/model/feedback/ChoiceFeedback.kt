package org.stepik.android.model.feedback

import com.google.gson.annotations.SerializedName

data class ChoiceFeedback(
    @SerializedName("options_feedback")
    val optionsFeedback: List<String>? = null
): Feedback