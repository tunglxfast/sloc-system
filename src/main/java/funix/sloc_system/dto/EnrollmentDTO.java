package funix.sloc_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDTO {
    private Long id;
    private UserDTO user;
    private Long courseId;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate enrollmentDate;
}
