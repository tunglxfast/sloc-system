package funix.sloc_system.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RedirectUrlHelper {
    public static final String REDIRECT_INSTRUCTOR_COURSES = "redirect:/instructor/courses";

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
     * @return String 'redirect:/instructor/course/{courseId}/edit/chapters?errorMessage=%s'
     */
    public static String buildRedirectErrorUrl(Long courseId, String errorMessage) {
        return String.format(
                "redirect:/instructor/course/%d/edit/chapters?errorMessage=%s",
                courseId, sanitizeMessage(errorMessage)
        );
    }

    /**
     *
     * @param courseId
     * @param successMessage
     * @return String 'redirect:/instructor/course/{courseId}/edit/chapters?successMessage=%s'
     */
    public static String buildRedirectSuccessUrl(Long courseId, String successMessage) {
        return String.format(
                "redirect:/instructor/course/%d/edit/chapters?successMessage=%s",
                courseId, sanitizeMessage(successMessage)
        );
    }
}
