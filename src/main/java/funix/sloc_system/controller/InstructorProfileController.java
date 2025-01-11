package funix.sloc_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import funix.sloc_system.entity.InstructorInfo;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.InstructorInfoService;

@Controller
@RequestMapping("/instructor/profile")
public class InstructorProfileController {

  @Autowired
  private InstructorInfoService instructorInfoService;

  @GetMapping
  public String viewInstructorProfile(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
    Long instructorId = securityUser.getUserId();
    InstructorInfo instructorInfo = instructorInfoService.getInstructorInfoByUserId(instructorId);
    if (instructorInfo == null) {
      return "redirect:/instructor/profile/create_profile";
    }
    model.addAttribute("instructorInfo", instructorInfo);
    return "instructor/profile";
  }

  @GetMapping("/create_profile")
  public String showCreateProfileForm(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
      Long instructorId = securityUser.getUserId();
      model.addAttribute("instructorId", instructorId);
      return "instructor/create_profile";
  }

  @PostMapping("/create_profile")
  public String createInstructorProfile(@AuthenticationPrincipal SecurityUser securityUser,
                                        @RequestParam Long instructorId,
                                        @RequestParam String name,
                                        @RequestParam String email,
                                        @RequestParam String phone,
                                        @RequestParam String bio,
                                        @RequestParam("avatarFile") MultipartFile avatar,
                                        RedirectAttributes redirectAttributes) {  
      if (instructorId != securityUser.getUserId()) {
          redirectAttributes.addFlashAttribute("errorMessage", "Create profile failed");
          return "redirect:/instructor";
      }

      String sanitizedBio = Jsoup.clean(bio, Safelist.relaxed());
      
      try { 
          instructorInfoService.createInstructorInfo(instructorId, name, email, phone, sanitizedBio, avatar);
          redirectAttributes.addFlashAttribute("successMessage", "Create profile successfully");
          return "redirect:/instructor/profile";
      } catch (Exception e) {
          redirectAttributes.addFlashAttribute("errorMessage", "Create profile failed");
          return "redirect:/instructor";
      }
  }   
  
  @PostMapping("/edit_profile")
  public String updateInstructorProfile(@AuthenticationPrincipal SecurityUser securityUser,
                                        @RequestParam Long instructorId,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String email,
                                        @RequestParam(required = false) String phone,
                                        @RequestParam(required = false) String bio,
                                        @RequestParam(required = false, name = "avatarFile") MultipartFile avatar,
                                        RedirectAttributes redirectAttributes) {
                                          
      if (instructorId != securityUser.getUserId()) {
          redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to update this profile");
          return "redirect:/instructor";
      }

      String sanitizedBio = null;
      if (bio != null) {
        sanitizedBio = Jsoup.clean(bio, Safelist.relaxed());
      }

      try {
        instructorInfoService.updateInstructorInfo(instructorId, name, email, phone, sanitizedBio, avatar);
        redirectAttributes.addFlashAttribute("successMessage", "Update profile successfully");
        return "redirect:/instructor/profile";
      } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Update profile failed");
        return "redirect:/instructor/profile";
      }
  }
}
