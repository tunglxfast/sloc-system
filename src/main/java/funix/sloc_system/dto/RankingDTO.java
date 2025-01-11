package funix.sloc_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankingDTO {
    private Long userId;
    private String userName;
    private String fullName;
    private Long courseId;
    private String courseName;
    private double allCourseScore;
    private int rankPosition;
}
