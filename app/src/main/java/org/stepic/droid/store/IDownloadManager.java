package org.stepic.droid.store;

import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.store.operations.DatabaseManager;

public interface IDownloadManager {


    boolean isDownloadManagerEnabled();

    void addStep(Step step, String title);

    void addSection(Section section);

    void addCourse(Course course, DatabaseManager.Table type);

    void addLesson(Lesson lesson);
}