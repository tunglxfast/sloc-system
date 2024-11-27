package funix.sloc_system.entity;

import jakarta.persistence.Entity;

@Entity
public class VideoLesson extends Topic{
    private String videoUrl;

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
