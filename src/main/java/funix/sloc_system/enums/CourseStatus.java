package funix.sloc_system.enums;

public enum CourseStatus {
    DRAFT, // creating
    PENDING_CREATE, // waiting for creating review
    PENDING_EDIT, // waiting for update review
    APPROVED,
    REJECTED
}
