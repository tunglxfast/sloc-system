package funix.sloc_system.entity;

import jakarta.persistence.Entity;

@Entity
public class ReadingLesson extends Topic {
    private String fileUrl;

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
