package funix.sloc_system.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

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

    private String name;

    @Column(nullable = false)
    private String password;

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

    @ManyToMany(mappedBy = "instructors")
    private Set<Course> courses = new HashSet<>();
}
