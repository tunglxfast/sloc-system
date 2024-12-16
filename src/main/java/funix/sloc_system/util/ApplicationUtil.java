package funix.sloc_system.util;

import funix.sloc_system.dao.CategoryDao;
import funix.sloc_system.dao.ChapterDao;
import funix.sloc_system.dao.CourseDao;
import funix.sloc_system.dao.TopicDao;
import funix.sloc_system.entity.Category;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.CourseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationUtil {

    @Autowired
    private TopicDao topicDao;
    @Autowired
    private CourseDao courseDao;
    @Autowired
    private ChapterDao chapterDao;
    @Autowired
    private CategoryDao categoryDao;

    public Topic findNextTopic(Long topicId){
        Topic currentTopic = topicDao.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));


        int nextTopicSequence = currentTopic.getSequence() + 1;
        Chapter currentChapter = currentTopic.getChapter();

        List<Topic> topics = currentChapter.getTopics();
        for (Topic topic: topics) {
            if (topic.getSequence() == nextTopicSequence) {
                return topic;
            }
        }
        // nếu không có topic nào sequence topic+1 -> topic cuối của chapter -> qua chapter mới
        int nextChapterSequence = currentChapter.getSequence() + 1;
        Course currentCourse = currentChapter.getCourse();

        List<Chapter> chapters = chapterDao.findByCourseIdOrderBySequence(currentCourse.getId());
        for (Chapter chapter: chapters) {
            if (chapter.getSequence() == nextChapterSequence) {
                return chapter.getTopics().get(0);
            }
        }

        // nếu vẫn không tìm được -> null
        return null;
    }

    public boolean isCourseReady(Long courseId) {
        Course course = courseDao.findById(courseId).orElse(null);
        if (course == null) {
            return false;
        }
        CourseStatus courseStatus = course.getStatus();
        if (courseStatus != CourseStatus.APPROVED && courseStatus != CourseStatus.APPROVED) {
            return false;
        }
        return true;
    }

    public Category getCategoryById(Long id) {
        return categoryDao.findById(id).orElse(null);
    }
}
