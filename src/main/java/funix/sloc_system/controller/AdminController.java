package funix.sloc_system.controller;

import funix.sloc_system.dto.UserDTO;
import funix.sloc_system.dto.RoleDTO;
import funix.sloc_system.entity.Role;
import funix.sloc_system.entity.User;
import funix.sloc_system.mapper.UserMapper;
import funix.sloc_system.mapper.RoleMapper;
import funix.sloc_system.service.UserService;
import funix.sloc_system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMapper roleMapper;

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOList = userMapper.toDTO(users);
        Set<Role> roles = roleService.getAllRoles();
        Set<RoleDTO> roleDTOList = roleMapper.toDTO(roles);
        model.addAttribute("users", userDTOList);
        model.addAttribute("roles", roleDTOList);

        return "admin/user_list";
    }

    @PostMapping("/users/{id}/edit_roles")
    public String editRoles(@PathVariable Long id, @RequestParam("role") List<String> updateRoles) {
        Set<Role> roles = roleService.getRolesByNames(updateRoles);
        User user = userService.getUserById(id);
        user.setRoles(roles);
        userService.updateUser(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/change_password")
    public String changePassword(@PathVariable Long id, @RequestParam("password") String newPassword, RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(id);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/admin/users";
        }
        if (newPassword.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Password cannot be empty");
            return "redirect:/admin/users";
        }
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters long");
            return "redirect:/admin/users";
        }
        if (newPassword.equals(user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "New password cannot be the same as the old password");
            return "redirect:/admin/users";
        }
        // change password
        user.setPassword(newPassword);
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("success", "Password changed successfully");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/lock/{id}")
    public String lockUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        user.setLocked(true);
        userService.updateUser(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/unlock/{id}")
    public String unlockUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        user.setLocked(false);
        userService.updateUser(user);
        return "redirect:/admin/users";
    }
}
