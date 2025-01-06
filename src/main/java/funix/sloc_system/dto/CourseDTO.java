package funix.sloc_system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private CategoryDTO category;
    private UserDTO instructor;
    private UserDTO createdBy;
    private UserDTO lastUpdatedBy;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate endDate;
    private String contentStatus;
    private String approvalStatus;
    private String rejectReason;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate createdAt;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate updatedAt;
    private List<ChapterDTO> chapters;
    @JsonIgnore
    private Set<EnrollmentDTO> enrollments;

    // handy methods
    // update title, description, category, start/end date, update time and updater
    public void updateEditingValues(CourseDTO editingValues, CategoryDTO editCategory, UserDTO updater) {
        if (editingValues.getTitle() != null) {
            this.setTitle(editingValues.getTitle());
        }
        if (editingValues.getDescription() != null) {
            this.setDescription(editingValues.getDescription());
        }
        if (editingValues.getStartDate() != null) {
            this.setStartDate(editingValues.getStartDate());
        }
        if (editingValues.getEndDate() != null) {
            this.setEndDate(editingValues.getEndDate());
        }
        if (editCategory != null) {
            this.setCategory(editCategory);
        }
        if (updater != null) {
            this.setLastUpdatedBy(updater);
        }
        this.setUpdatedAt(LocalDate.now());

    }

    @JsonSetter("createdBy")
    public void setCreatedBy(UserDTO userDTO) {
        this.createdBy = userDTO;
    }

    @JsonSetter("lastUpdatedBy")
    public void setLastUpdatedBy(UserDTO userDTO) {
        this.lastUpdatedBy = userDTO;
    }

    @JsonSetter("instructor")
    public void setInstructor(UserDTO userDTO) {
        this.instructor = userDTO;
    }

    @JsonSetter("category")
    public void setCategory(CategoryDTO categoryDTO) {
        this.category = categoryDTO;
    }

    /**
     * Get chapter with last sequence.
     * @return
     */
    public ChapterDTO getLastChapter() {
        if (this.getChapters() != null && !this.getChapters().isEmpty()){
            ChapterDTO lastChapterDTO = this.getChapters().get(0);
            for (ChapterDTO chapterDTO : this.getChapters()) {
                if (chapterDTO.getSequence() > lastChapterDTO.getSequence()) {
                    lastChapterDTO = chapterDTO;
                }
            }
            return lastChapterDTO;
        }
        return null;
    }

    // Helper methods
    public void addChapter(ChapterDTO chapterDTO) {
        if (chapters == null) {
            chapters = new ArrayList<>();
        }  
        if (chapters.contains(chapterDTO)) {
            return;
        }
        chapterDTO.setCourseId(id);
        chapters.add(chapterDTO);
    }
}

