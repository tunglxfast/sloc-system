package funix.sloc_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private boolean locked;
    private Set<RoleDTO> roles;
    private String verificationToken;
    private LocalDateTime tokenExpiryDate;
    private boolean verified;

    // methods

    public Set<String> getStringRoles() {
        return this.roles.stream().map(RoleDTO::getName).collect(Collectors.toSet());
    }

    public boolean haveRoleName(String roleName) {
        for (RoleDTO role : this.roles) {
            if (role.getName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }
}
