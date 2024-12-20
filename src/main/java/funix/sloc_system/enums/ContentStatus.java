package funix.sloc_system.enums;

public enum ContentStatus {
    DRAFT, // Initial state for new content
    READY_TO_REVIEW, // Waiting for create review
    PUBLISHED,
    PUBLISHED_EDITING, // Waiting for edit review
    ARCHIVED // closed status
} 