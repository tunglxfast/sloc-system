package funix.sloc_system.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDiscussionStatsHolder {
    private Long courseId;
    private String courseTitle;
    private String category;
    private int topicCount;
    private int discussionCount;
} 