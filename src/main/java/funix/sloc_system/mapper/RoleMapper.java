package funix.sloc_system.mapper;

import funix.sloc_system.dto.RoleDTO;
import funix.sloc_system.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {
    
    public RoleDTO toDTO(Role role) {
        if (role == null) {
            return null;
        }

        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        
        return dto;
    }

    public Role toEntity(RoleDTO dto) {
        if (dto == null) {
            return null;
        }

        Role role = new Role();
        role.setId(dto.getId());
        role.setName(dto.getName());
        
        return role;
    }
} 