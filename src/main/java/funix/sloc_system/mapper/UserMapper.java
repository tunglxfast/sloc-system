package funix.sloc_system.mapper;

import funix.sloc_system.dto.UserDTO;
import funix.sloc_system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        dto.setFullName(user.getFullName());
        
        // Map roles
        dto.setRoles(roleMapper.toDTO(user.getRoles()));
        
        return dto;
    }

    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        
        // Map roles if present
        if (dto.getRoles() != null) {
            user.setRoles(roleMapper.toEntity(dto.getRoles()));
        }
        
        return user;
    }
} 