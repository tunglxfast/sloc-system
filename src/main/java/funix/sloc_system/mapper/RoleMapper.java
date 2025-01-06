package funix.sloc_system.mapper;

import funix.sloc_system.dto.RoleDTO;
import funix.sloc_system.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Set<RoleDTO> toDTO(Set<Role> roles) {
        return roles.stream().map(this::toDTO).collect(Collectors.toSet());
    }

    public List<RoleDTO> toDTO(List<Role> roles) {
        return roles.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Set<Role> toEntity(Set<RoleDTO> roleDTOSet) {
        return roleDTOSet.stream().map(this::toEntity).collect(Collectors.toSet());
    }

    public List<Role> toEntity(List<RoleDTO> roleDTOList) {
        return roleDTOList.stream().map(this::toEntity).collect(Collectors.toList());
    }
} 
