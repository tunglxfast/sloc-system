package funix.sloc_system.controller;

import funix.sloc_system.dto.UserDTO;
import funix.sloc_system.entity.User;
import funix.sloc_system.mapper.UserMapper;
import funix.sloc_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOList = userMapper.toDTO(users);
        model.addAttribute("users", userDTOList);
        return "admin/user_list";
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        UserDTO userDTO = userMapper.toDTO(user);
        model.addAttribute("user", userDTO);
        return "admin/edit_user";
    }

    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        userService.updateUser(user);
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
