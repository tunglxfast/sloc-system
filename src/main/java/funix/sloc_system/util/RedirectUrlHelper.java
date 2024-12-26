package funix.sloc_system.util;


public class RedirectUrlHelper {
    public static final String REDIRECT_INSTRUCTOR_COURSES = "redirect:/instructor/courses";

    // Private constructor to prevent instantiation
    private RedirectUrlHelper() {
        throw new UnsupportedOperationException("This is a utility class and don't initialize it");
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
                courseId, errorMessage
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
                courseId, successMessage
        );
    }

}
