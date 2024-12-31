package funix.sloc_system.enums;

public enum ApprovalStatus {
    NOT_SUBMITTED,    // Default status when not send review request
    PENDING,         // Already sending review request and wait for response
    APPROVED,        // Moderator accept and give permission
    REJECTED         // Moderator reject request
} 