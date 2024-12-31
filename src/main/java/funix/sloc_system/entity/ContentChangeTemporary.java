package funix.sloc_system.entity;

import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.EntityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "content_change_temporary")
public class ContentChangeTemporary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type")
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column()
    private ContentAction action;  // 'UPDATE', 'DELETE', 'CREATE'

    @Column(columnDefinition = "TEXT", nullable = false)
    private String changes;  // JSON string containing the changes

    @Column(name = "updated_by")
    private Long updatedBy;  // User id

    @Column(nullable = false)
    private LocalDateTime changeTime;
}
