package org.stepic.droid.storage.operations

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.model.deadlines.Deadline
import org.stepic.droid.model.deadlines.DeadlineFlatItem
import org.stepic.droid.model.deadlines.DeadlinesWrapper
import org.stepic.droid.storage.dao.PersonalDeadlinesDao
import org.stepic.droid.storage.structure.DbStructureDeadlines
import org.stepic.droid.web.storage.model.StorageRecord
import java.util.*
import javax.inject.Inject


class DeadlinesRecordOperationsImpl
@Inject
constructor(
        private val personalDeadlinesDao: PersonalDeadlinesDao
): DeadlinesRecordOperations {
    companion object {
        private fun StorageRecord<DeadlinesWrapper>.flatten(): List<DeadlineFlatItem> =
                data.deadlines.map { DeadlineFlatItem(this.id ?: -1, this.data.course, it.section, it.deadline) }

        private const val DEFAULT_GAP = 60 * 60 * 1000L
    }

    override fun getClosestDeadlineTimestamp(): Single<Long> = Single.create { emitter ->
        emitter.onSuccess(personalDeadlinesDao.getClosestDeadlineDate()?.time ?: 0)
    }

    override fun getDeadlineRecordForCourse(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>> = Maybe.create { emitter ->
        val items = personalDeadlinesDao.getAll(DbStructureDeadlines.Columns.COURSE_ID, courseId.toString()).filterNotNull()
        if (items.isNotEmpty()) {
            val recordId: Long = items.first().recordId
            val deadlines = items.filter { it.recordId == recordId }.map {
                Deadline(it.sectionId, it.deadline)
            }
            emitter.onSuccess(StorageRecord(
                    id = recordId,
                    kind = "",
                    data = DeadlinesWrapper(items.first().courseId, deadlines)
            ))
        } else {
            emitter.onComplete()
        }
    }

    override fun getDeadlineRecordsForTimestamp(timestamp: Long): Single<List<DeadlineFlatItem>> = Single.fromCallable {
        personalDeadlinesDao.getDeadlinesForDate(Date(timestamp), DEFAULT_GAP)
    }

    override fun saveDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>> = Single.create { emitter ->
        personalDeadlinesDao.insertOrReplaceAll(record.flatten())
        emitter.onSuccess(record)
    }

    override fun removeDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Completable = Completable.fromCallable {
        personalDeadlinesDao.remove(DbStructureDeadlines.Columns.RECORD_ID, record.id.toString())
    }

    override fun removeAllDeadlineRecords(): Completable =
            Completable.fromCallable(personalDeadlinesDao::removeAll)
}