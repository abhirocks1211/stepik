package org.stepic.droid.jsonHelpers.deserializers

import com.google.gson.*
import org.stepik.android.model.feedback.ChoiceFeedback
import org.stepik.android.model.feedback.Feedback
import org.stepik.android.model.feedback.StringFeedback
import java.lang.Exception
import java.lang.reflect.Type

class FeedbackDeserializer : JsonDeserializer<Feedback> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Feedback {
        return if (json !is JsonObject) {
            try {
                val field = context.deserialize<String>(json, String::class.java)
                StringFeedback(field)
            } catch (e: Exception) {
                StringFeedback()
            }
        } else {
            context.deserialize<ChoiceFeedback>(json, ChoiceFeedback::class.java)
        }
    }
}