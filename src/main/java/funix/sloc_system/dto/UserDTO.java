package funix.sloc_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private boolean locked;
    private Set<RoleDTO> roles;

    public Set<String> getStringRoles() {
        return this.roles.stream().map(RoleDTO::getName).collect(Collectors.toSet());
    }
}
