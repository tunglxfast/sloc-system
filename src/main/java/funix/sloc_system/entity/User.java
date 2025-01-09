package funix.sloc_system.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean locked;

    @Column(nullable = false)
    private int failedAttempts;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "token_expiry_date")
    private LocalDateTime tokenExpiryDate;

    @Column(name = "verified")
    private boolean verified = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Enrollment> enrollments = new HashSet<>();

    @ManyToMany(mappedBy = "instructor")
    private Set<Course> courses = new HashSet<>();

    public Set<String> getStringRoles() {
        return this.roles.stream().map(Role::getName).collect(Collectors.toSet());
    }

    public void updateWithOtherUser(User updatedUser) {
        if (updatedUser.getUsername() != null) {
            this.username = updatedUser.getUsername();
        }
        if (updatedUser.getEmail() != null) {
            this.email = updatedUser.getEmail();
        }
        if (updatedUser.getFullName() != null) {
            this.fullName = updatedUser.getFullName();
        }
        if (updatedUser.getPassword() != null) {
            this.password = updatedUser.getPassword();
        }
        if (updatedUser.getRoles() != null) {
            this.roles = updatedUser.getRoles();
        }
        this.locked = updatedUser.isLocked();
    }

    public boolean isTokenExpired() {
        return tokenExpiryDate != null && LocalDateTime.now().isAfter(tokenExpiryDate);
    }
}
