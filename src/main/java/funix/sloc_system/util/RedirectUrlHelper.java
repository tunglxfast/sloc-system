package funix.sloc_system.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RedirectUrlHelper {
    public static final String REDIRECT_INSTRUCTOR_DASHBOARD = "redirect:/instructor/courses";

    // Private constructor to prevent instantiation
    private RedirectUrlHelper() {
        throw new UnsupportedOperationException("This is a utility class and don't initialize it");
    }

    /**
     * Sanitize message by removing newlines and encoding for URL
     */
    private static String sanitizeMessage(String message) {
        if (message == null) return "";
        // Remove CR/LF and encode
        String sanitized = message.replace("\r", " ").replace("\n", " ").trim();
        return URLEncoder.encode(sanitized, StandardCharsets.UTF_8);
    }

    /**
     *
     * @param courseId
     * @param errorMessage
     * @return String 'redirect:/instructor/course/{courseId}/edit/topic/edit'
     */
    public static String buildRedirectErrorUrlToTopicEdit(Long courseId, Long chapterId, Long topicId, String errorMessage) {
        return String.format(
                "redirect:/instructor/course/%d/edit/topic/edit?chapterId=%d&topicId=%d&errorMessage=%s",
                courseId, chapterId, topicId, sanitizeMessage(errorMessage)
        );
    }

    /**
     *
     * @param courseId
     * @param successMessage
     * @return String 'redirect:/instructor/course/{courseId}/edit/chapters?successMessage=%s'
     */
    public static String buildRedirectSuccessUrlToCourseContent(Long courseId, String successMessage) {
        return String.format(
                "redirect:/instructor/course/%d/edit/chapters?successMessage=%s",
                courseId, sanitizeMessage(successMessage)
        );
    }

    public static String buildRedirectErrorUrlToCourseContent(Long courseId, String errorMessage) {
        return String.format(
                "redirect:/instructor/course/%d/edit/chapters?errorMessage=%s",
                courseId, sanitizeMessage(errorMessage)
        );
    }
}
