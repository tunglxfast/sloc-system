package funix.sloc_system.enums;

public enum ContentStatus {
    DRAFT,                  // For new create, or edit when not published
    READY_TO_REVIEW,        // Creator finished creating and sent to review
    PUBLISHED,              // Got approved and can be seen/enrolled by students
    PUBLISHED_EDITING,      // Already published, but need to edit and waiting for review
    ARCHIVED               // Course stopped work (closed or out of learning time)
} 