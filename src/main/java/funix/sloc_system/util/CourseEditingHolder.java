package funix.sloc_system.util;

import funix.sloc_system.dto.CourseDTO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CourseEditingHolder {
    private CourseDTO course;
    private boolean isEditing;
    private boolean isPending;
}