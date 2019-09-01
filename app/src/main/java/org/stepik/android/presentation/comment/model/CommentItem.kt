package org.stepik.android.presentation.comment.model

import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.Vote
import org.stepik.android.model.user.User
import ru.nobird.android.core.model.Identifiable

sealed class CommentItem {
    data class Data(
        val comment: Comment,
        val user: User,
        val voteStatus: VoteStatus
    ) : Identifiable<Long>, CommentItem() {
        override val id: Long =
            comment.id

        sealed class VoteStatus {
            data class Resolved(val vote: Vote) : VoteStatus()
            object Pending : VoteStatus()
        }
    }

    data class MoreReplies(
        val parentComment: Comment,
        val lastCommentId: Long
    ) : CommentItem()

    object Placeholder : CommentItem()
    object LoadMore : CommentItem()
}