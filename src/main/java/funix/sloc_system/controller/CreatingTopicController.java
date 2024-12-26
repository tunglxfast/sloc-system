package funix.sloc_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.ChapterService;
import funix.sloc_system.service.CourseService;
import funix.sloc_system.service.TopicService;
import funix.sloc_system.util.AppUtil;
import funix.sloc_system.util.RedirectUrlHelper;
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
@RequestMapping("/instructor/course/{courseId}/edit/topic")
public class CreatingTopicController {

    @Value("${app.upload.dir:src/main/resources/reading}")
    private String uploadReadingDir;
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private TopicService topicService;
    @Autowired
    private AppUtil appUtil;

    @GetMapping("/create")
    public String showCreateTopicForm(@PathVariable Long courseId,
                              @RequestParam("chapterId") Long chapterId,
                              @RequestParam String topicType,
                              Model model) {
        if (chapterId == null ) {
            return RedirectUrlHelper.buildRedirectErrorUrl(courseId, "Chapter info not found");
        }

        try {
            CourseDTO courseDTO = appUtil.getEditingCourseDTO(courseId);
            ChapterDTO chapterDTO = AppUtil.getSelectChapterDTO(courseDTO, chapterId);


            model.addAttribute("courseId", courseDTO.getId());
            model.addAttribute("courseTitle", courseDTO.getTitle());
            model.addAttribute("chapterId", chapterDTO.getId());
            model.addAttribute("chapterTitle", chapterDTO.getTitle());
            model.addAttribute("topicType", topicType);
            model.addAttribute("topic", new TopicDTO());

            return "instructor/create_topic";
        } catch (Exception e) {
            return RedirectUrlHelper.buildRedirectErrorUrl(courseId, e.getMessage());
        }
    }

    @GetMapping(value = {"","/"})
    public String showEditTopicForm(@PathVariable Long courseId,
                                    @RequestParam("chapterId") Long chapterId,
                                    @RequestParam("topicId") Long topicId,
                                    Model model) {
        if (chapterId == null || topicId == null ) {
            return RedirectUrlHelper.buildRedirectErrorUrl(courseId, "Chapter or topic info not found");
        }

        try {
            CourseDTO courseDTO = appUtil.getEditingCourseDTO(courseId);
            ChapterDTO chapterDTO = AppUtil.getSelectChapterDTO(courseDTO, chapterId);
            TopicDTO topicDTO = AppUtil.getSelectTopicDTO(chapterDTO, topicId);
            String topicType = topicDTO.getTopicType();

            model.addAttribute("courseId", courseDTO.getId());
            model.addAttribute("courseTitle", courseDTO.getTitle());
            model.addAttribute("chapterId", chapterDTO.getId());
            model.addAttribute("chapterTitle", chapterDTO.getTitle());
            model.addAttribute("topicType", topicType);
            model.addAttribute("topic", topicDTO);

            if (topicType.equals(TopicType.READING.name())
                    || topicType.equals(TopicType.VIDEO.name())) {
                return "instructor/edit_lesson_topic";
            } else {
                return "instructor/edit_test_topic";
            }
        } catch (Exception e) {
            return RedirectUrlHelper.buildRedirectErrorUrl(courseId, e.getMessage());
        }
    }

    @PostMapping("/add")
    public String addTopic(@PathVariable Long courseId,
                          @RequestParam Long chapterId,
                          @RequestParam String title,
                          @RequestParam String description,
                          @RequestParam String topicType,
                          @RequestParam(required = false) MultipartFile content,
                          @RequestParam(required = false) String videoUrl,
                          @RequestParam(required = false) String questions,  // JSON string of QuestionDTO
                          @RequestParam(required = false) Integer passScore,
                          @RequestParam(required = false) Integer timeLimit,
                          RedirectAttributes redirectAttributes) {
        // TODO: đang làm, cần chia nhỏ ra thành lesson và test cho dễ quản lý
        try {
            TopicDTO topicDTO = new TopicDTO();
            topicDTO.setTitle(title);
            topicDTO.setDescription(description);
            topicDTO.setTopicType(topicType);

            // Handle different topic types
            if ("READING".equals(topicType) && content != null && !content.isEmpty()) {
                // Create upload directory if it doesn't exist
                File directory = new File(uploadReadingDir);
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
                Path filePath = Paths.get(uploadReadingDir, filename);
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
            return RedirectUrlHelper.buildRedirectErrorUrl(courseId, e.getMessage());
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
            return RedirectUrlHelper.buildRedirectSuccessUrl(courseId, "Topic updated successfully.");
        } catch (Exception e) {
            return RedirectUrlHelper.buildRedirectErrorUrl(courseId, e.getMessage());
        }
    }
}
