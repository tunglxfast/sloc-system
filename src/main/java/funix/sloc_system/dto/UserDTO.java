package funix.sloc_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private Set<RoleDTO> roles;
}
