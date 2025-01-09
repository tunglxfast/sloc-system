package funix.sloc_system.repository;

import funix.sloc_system.entity.TopicDiscussion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicDiscussionRepository extends JpaRepository<TopicDiscussion, Long> {
    List<TopicDiscussion> findByTopicId(Long topicId);
    List<TopicDiscussion> findByCreatedById(Long userId);

    @Query("SELECT td FROM TopicDiscussion td " +
           "JOIN td.topic t " +
           "JOIN t.chapter c " +
           "WHERE c.course.id = :courseId " +
           "ORDER BY td.updatedAt DESC")
    List<TopicDiscussion> findByCourseIdOrderByUpdatedAtDesc(@Param("courseId") Long courseId);
} 