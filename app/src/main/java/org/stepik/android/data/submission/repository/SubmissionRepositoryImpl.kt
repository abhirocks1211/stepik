package org.stepik.android.data.submission.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.submission.source.SubmissionCacheDataSource
import org.stepik.android.data.submission.source.SubmissionRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.model.Submission
import javax.inject.Inject

class SubmissionRepositoryImpl
@Inject
constructor(
    private val submissionRemoteDataSource: SubmissionRemoteDataSource,
    private val submissionCacheDataSource: SubmissionCacheDataSource
) : SubmissionRepository {
    override fun createSubmission(submission: Submission, dataSourceType: DataSourceType): Single<Submission> =
        when (dataSourceType) {
            DataSourceType.CACHE ->
                submissionCacheDataSource.createSubmission(submission)

            DataSourceType.REMOTE ->
                submissionRemoteDataSource.createSubmission(submission)
        }

    override fun getSubmissionsForAttempt(attemptId: Long, dataSourceType: DataSourceType): Single<List<Submission>> =
        when (dataSourceType) {
            DataSourceType.CACHE ->
                submissionCacheDataSource.getSubmissionsForAttempt(attemptId)

            DataSourceType.REMOTE ->
                submissionRemoteDataSource.getSubmissionsForAttempt(attemptId)
        }

    override fun getSubmissionsForStep(stepId: Long): Single<List<Submission>> =
        submissionRemoteDataSource.getSubmissionsForStep(stepId)

    override fun removeSubmissionsForAttempt(attemptId: Long): Completable =
        submissionCacheDataSource.removeSubmissionsForAttempt(attemptId)
}