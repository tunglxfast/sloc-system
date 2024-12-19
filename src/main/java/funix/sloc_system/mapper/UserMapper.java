package funix.sloc_system.mapper;

import funix.sloc_system.dto.UserDTO;
import funix.sloc_system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {
    
    @Autowired
    private RoleMapper roleMapper;

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        
        // Map roles
        dto.setRoles(user.getRoles().stream()
                .map(roleMapper::toDTO)
                .collect(Collectors.toSet()));
        
        return dto;
    }

    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        
        // Map roles if present
        if (dto.getRoles() != null) {
            user.setRoles(dto.getRoles().stream()
                    .map(roleMapper::toEntity)
                    .collect(Collectors.toSet()));
        }
        
        return user;
    }
} 