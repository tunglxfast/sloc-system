package funix.sloc_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.ChapterService;
import funix.sloc_system.service.CourseService;
import funix.sloc_system.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/instructor/course/{courseId}/chapter/{chapterId}/topic")
public class CreatingTopicController {

    @Value("${app.upload.dir:src/main/resources/reading}")
    private String uploadDir;

    @Autowired
    private ChapterService chapterService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private TopicService topicService;

    @GetMapping("/create")
    public String showCreateTopic(@PathVariable Long courseId,
                                @PathVariable Long chapterId,
                                @RequestParam String type,
                                Model model) {
        try {
            CourseDTO courseDTO = courseService.getEditingCourseDTO(courseId);
            ChapterDTO chapterDTO = chapterService.getEditingChapterDTO(chapterId);
            model.addAttribute("course", courseDTO);
            model.addAttribute("chapter", chapterDTO);
            model.addAttribute("topicType", type);
            model.addAttribute("newTopic", new TopicDTO());
            return "instructor/create_topic";
        } catch (Exception e) {
            return "redirect:/instructor/course/" + courseId + "/chapter/" + chapterId + "?error=" + e.getMessage();
        }
    }

    @GetMapping("/{topicId}")
    public String showEditTopic(@PathVariable Long courseId,
                              @PathVariable Long chapterId,
                              @PathVariable Long topicId,
                              Model model) {
        try {
            CourseDTO courseDTO = courseService.getEditingCourseDTO(courseId);
            ChapterDTO chapterDTO = chapterService.getEditingChapterDTO(chapterId);
            TopicDTO topicDTO = topicService.getEditingTopicDTO(topicId);
            
            model.addAttribute("course", courseDTO);
            model.addAttribute("chapter", chapterDTO);
            model.addAttribute("topic", topicDTO);
            model.addAttribute("topicType", topicDTO.getTopicType());
            
            return "instructor/edit_topic";
        } catch (Exception e) {
            return "redirect:/instructor/course/" + courseId + "/chapter/" + chapterId + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/add")
    public String addTopic(@PathVariable Long courseId,
                          @PathVariable Long chapterId,
                          @RequestParam String title,
                          @RequestParam String description,
                          @RequestParam String topicType,
                          @RequestParam(required = false) MultipartFile content,
                          @RequestParam(required = false) String videoUrl,
                          @RequestParam(required = false) String questions,  // JSON string of QuestionDTO
                          @RequestParam(required = false) Integer passScore,
                          @RequestParam(required = false) Integer timeLimit,
                          RedirectAttributes redirectAttributes) {
        try {
            TopicDTO topicDTO = new TopicDTO();
            topicDTO.setTitle(title);
            topicDTO.setDescription(description);
            topicDTO.setTopicType(topicType);

            // Handle different topic types
            if ("READING".equals(topicType) && content != null && !content.isEmpty()) {
                // Create upload directory if it doesn't exist
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Generate unique filename
                String originalFilename = content.getOriginalFilename();
                String extension = "";
                if (originalFilename != null) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString() + extension;
                
                // Save file
                Path filePath = Paths.get(uploadDir, filename);
                Files.write(filePath, content.getBytes());
                
                // Save file path to DTO
                topicDTO.setFileUrl("/static/reading/" + filename);
            } else if ("VIDEO".equals(topicType) && videoUrl != null) {
                topicDTO.setVideoUrl(videoUrl);
            } else if (("QUIZ".equals(topicType) || "EXAM".equals(topicType)) && questions != null) {
                // Parse questions JSON string
                ObjectMapper objectMapper = new ObjectMapper();
                List<QuestionDTO> questionDTOs = objectMapper.readValue(questions, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionDTO.class));
                
                // Set common fields for Quiz and Exam
                topicDTO.setPassScore(passScore);
                topicDTO.setQuestions(questionDTOs);
                
                // Set time limit for Exam only
                if ("EXAM".equals(topicType)) {
                    topicDTO.setTimeLimit(timeLimit);
                }
            }

            chapterService.addTopic(chapterId, topicDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Topic created successfully.");
            return "redirect:/instructor/course/" + courseId + "/chapter/" + chapterId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/instructor/course/" + courseId + "/chapter/" + chapterId;
        }
    }

    @PostMapping("/save")
    public String saveTopic(@PathVariable Long courseId,
                          @PathVariable Long chapterId,
                          @ModelAttribute("topic") TopicDTO topicDTO,
                          @AuthenticationPrincipal SecurityUser securityUser,
                          RedirectAttributes redirectAttributes) {
        try {
            topicService.saveTopicChanges(topicDTO, securityUser.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Topic updated successfully.");
            return "redirect:/instructor/course/" + courseId + "/chapter/" + chapterId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/instructor/course/" + courseId + "/chapter/" + chapterId;
        }
    }
}
