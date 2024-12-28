package funix.sloc_system.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Question;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.*;
import funix.sloc_system.util.AppUtil;
import funix.sloc_system.util.RedirectUrlHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/instructor/course/{courseId}/edit/topic")
public class CreatingTopicController {

    @Autowired
    private ChapterService chapterService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private TopicService topicService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private MinioService minioService;
    @Autowired
    private AppUtil appUtil;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping(value = {"","/","/create"})
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

            // Return different templates based on topic type
            if (topicType.equals("VIDEO") || topicType.equals("READING")) {
                return "instructor/create_lesson_topic";
            } else if (topicType.equals("QUIZ") || topicType.equals("EXAM")) {
                return "instructor/create_test_topic";
            } else {
                return RedirectUrlHelper.buildRedirectErrorUrl(courseId, "Invalid topic type");
            }
        } catch (Exception e) {
            return RedirectUrlHelper.buildRedirectErrorUrl(courseId, e.getMessage());
        }
    }

    @PostMapping("/create")
    public String addNewTopic(@PathVariable Long courseId,
                          @RequestParam Long chapterId,
                          @RequestParam String title,
                          @RequestParam String description,
                          @RequestParam String topicType,
                          @RequestParam(required = false) MultipartFile readingFile,
                          @RequestParam(required = false) MultipartFile videoFile,
                          @RequestParam(required = false) String videoUrl,
                          @RequestParam(required = false) String questions,
                          @RequestParam(required = false) Integer passScore,
                          @RequestParam(required = false) Integer timeLimit,
                          @AuthenticationPrincipal SecurityUser securityUser,
                          RedirectAttributes redirectAttributes) {
        try {
            TopicDTO topicDTO = new TopicDTO();
            topicDTO.setTitle(title);
            topicDTO.setDescription(description);
            topicDTO.setTopicType(topicType);

            // Handle different topic types
            switch (topicType) {
                case "READING":
                    if (readingFile != null && !readingFile.isEmpty()) {
                        String contentType = readingFile.getContentType();
                        if (contentType != null 
                        && (contentType.equals("application/pdf") || 
                            contentType.equals("application/msword") ||
                            contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
                            String fileUrl = minioService.uploadFile(readingFile, contentType);
                            topicDTO.setFileUrl(fileUrl);
                        }
                    }
                    break;

                case "VIDEO":
                    if (videoFile != null && !videoFile.isEmpty()) {
                        String contentType = videoFile.getContentType();
                        if (contentType != null && contentType.startsWith("video/")) {
                            String fileUrl = minioService.uploadFile(videoFile, contentType);
                            topicDTO.setVideoUrl(fileUrl);
                        }
                    } else if (videoUrl != null && !videoUrl.isEmpty()) {
                        topicDTO.setVideoUrl(videoUrl);
                    }
                    break;

                case "QUIZ":
                case "EXAM":
                    if (questions != null) {
                        List<QuestionDTO> questionDTOs = objectMapper.readValue(questions,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionDTO.class));
                        topicDTO.setQuestions(questionDTOs);
                        topicDTO.setPassScore(passScore);
                        
                        if ("EXAM".equals(topicType)) {
                            topicDTO.setTimeLimit(timeLimit);
                        }
                    }
                    break;
            }

            // Create topic first
            topicService.createTopic(chapterId, topicDTO, securityUser.getUserId());

            // If it's a quiz/exam, create questions
            if ((topicType.equals("QUIZ") || topicType.equals("EXAM")) && questions != null) {
                List<QuestionDTO> questionDTOs = objectMapper.readValue(questions,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionDTO.class));
                
                for (QuestionDTO questionDTO : questionDTOs) {
                    questionDTO.setTopicId(topicDTO.getId());
                    questionService.createQuestion(topicDTO.getId(), questionDTO, securityUser.getUserId());
                }
            }

            return RedirectUrlHelper.buildRedirectSuccessUrl(courseId, "Topic created successfully.");
        } catch (Exception e) {
            return RedirectUrlHelper.buildRedirectErrorUrl(courseId, e.getMessage());
        }
    }

    @GetMapping("/edit")
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

    @PostMapping("/edit")
    public String saveEditTopic(@PathVariable Long courseId,
                          @RequestParam Long chapterId,
                          @RequestParam String title,
                          @RequestParam String description,
                          @RequestParam String topicType,
                          @RequestParam(required = false) MultipartFile readingFile,
                          @RequestParam(required = false) MultipartFile videoFile,
                          @RequestParam(required = false) String videoUrl,
                          @RequestParam(required = false) String questions,
                          @RequestParam(required = false) Integer passScore,
                          @RequestParam(required = false) Integer timeLimit,
                          @RequestParam Long topicId,
                          @AuthenticationPrincipal SecurityUser securityUser,
                          RedirectAttributes redirectAttributes) {
        try {
            TopicDTO topicDTO = new TopicDTO();
            topicDTO.setId(topicId);
            topicDTO.setTitle(title);
            topicDTO.setDescription(description);
            topicDTO.setTopicType(topicType);

            // Handle different topic types
            switch (topicType) {
                case "READING":
                    if (readingFile != null && !readingFile.isEmpty()) {
                        String contentType = readingFile.getContentType();
                        if (contentType != null 
                        && (contentType.equals("application/pdf") || 
                            contentType.equals("application/msword") ||
                            contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
                            String fileUrl = minioService.uploadFile(readingFile, contentType);
                            topicDTO.setFileUrl(fileUrl);
                        }
                    }
                    break;

                case "VIDEO":
                    if (videoFile != null && !videoFile.isEmpty()) {
                        String contentType = videoFile.getContentType();
                        if (contentType != null && contentType.startsWith("video/")) {
                            String fileUrl = minioService.uploadFile(videoFile, contentType);
                            topicDTO.setVideoUrl(fileUrl);
                        }
                    } else if (videoUrl != null && !videoUrl.isEmpty()) {
                        topicDTO.setVideoUrl(videoUrl);
                    }
                    break;

                case "QUIZ":
                case "EXAM":
                    if (questions != null) {
                        List<QuestionDTO> questionDTOs = objectMapper.readValue(questions, new TypeReference<List<QuestionDTO>>() {});
                        topicDTO.setQuestions(questionDTOs);
                        topicDTO.setPassScore(passScore);
                        
                        if ("EXAM".equals(topicType)) {
                            topicDTO.setTimeLimit(timeLimit);
                        }

                        // Handle questions in service
                        questionService.handleTopicQuestions(topicId, questionDTOs, securityUser.getUserId());
                    }
                    break;
            }

            topicService.saveTopicChanges(courseId, chapterId, topicDTO, securityUser.getUserId());
            return RedirectUrlHelper.buildRedirectSuccessUrl(courseId, "Topic updated successfully.");
        } catch (Exception e) {
            return RedirectUrlHelper.buildRedirectErrorUrl(courseId, e.getMessage());
        }
    }
}
