package funix.sloc_system.mapper;

import funix.sloc_system.dto.UserDTO;
import funix.sloc_system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
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
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setLocked(user.isLocked());
        // Map roles
        dto.setRoles(roleMapper.toDTO(user.getRoles()));
        dto.setVerificationToken(user.getVerificationToken());
        dto.setTokenExpiryDate(user.getTokenExpiryDate());
        dto.setVerified(user.isVerified());
        
        return dto;
    }

    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setLocked(dto.isLocked());
        user.setVerified(dto.isVerified());
        user.setVerificationToken(dto.getVerificationToken());
        user.setTokenExpiryDate(dto.getTokenExpiryDate());
        
        // Map roles if present
        if (dto.getRoles() != null) {
            user.setRoles(roleMapper.toEntity(dto.getRoles()));
        }
        
        return user;
    }

    public List<UserDTO> toDTO(List<User> users) {
        return users.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<User> toEntity(List<UserDTO> userDTOs) {
        return userDTOs.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }
} 