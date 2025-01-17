package funix.sloc_system.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.*;
import funix.sloc_system.util.AppUtil;
import funix.sloc_system.util.RedirectUrlHelper;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
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
            return RedirectUrlHelper.buildRedirectErrorUrlToCourseContent(courseId, "Chapter info not found");
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
                return RedirectUrlHelper.buildRedirectErrorUrlToCourseContent(courseId, "Invalid topic type");
            }
        } catch (Exception e) {
            return RedirectUrlHelper.buildRedirectErrorUrlToCourseContent(courseId, e.getMessage());
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
                          @RequestParam(required = false) Integer passPoint,
                          @RequestParam(required = false) Integer timeLimit,
                          @AuthenticationPrincipal SecurityUser securityUser,
                          RedirectAttributes redirectAttributes) {
        try {
            TopicDTO topicDTO = new TopicDTO();
            topicDTO.setTitle(title);
            String sanitizedDescription = Jsoup.clean(description, Safelist.relaxed());
            topicDTO.setDescription(sanitizedDescription);
            topicDTO.setTopicType(topicType);

            // Handle different topic types
            switch (topicType) {
                case "READING":
                    handleReadingTopic(topicDTO, readingFile);
                    break;

                case "VIDEO":
                    handleVideoTopic(topicDTO, videoFile, videoUrl);
                    break;

                case "QUIZ":
                case "EXAM":
                    handleQuizExamTopic(topicDTO, topicType, questions, passPoint, timeLimit);
                    break;
            }
            
            topicService.createTopic(chapterId, topicDTO, securityUser.getUserId());

            return RedirectUrlHelper.buildRedirectSuccessUrlToCourseContent(courseId, "Topic created successfully.");
        } catch (Exception e) {
            return RedirectUrlHelper.buildRedirectErrorUrlToCourseContent(courseId, "Topic creation failed");
        }
    }

    @GetMapping("/edit")
    public String showEditTopicForm(@PathVariable Long courseId,
                                    @RequestParam(value = "errorMessage", required = false) String errorMessage,
                                    @RequestParam(value = "successMessage", required = false) String successMessage,
                                    @RequestParam("chapterId") Long chapterId,
                                    @RequestParam("topicId") Long topicId,
                                    Model model) {
        if (chapterId == null || topicId == null ) {
            return RedirectUrlHelper.buildRedirectErrorUrlToTopicEdit(courseId, chapterId, topicId, "Chapter or topic info not found");
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
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("successMessage", successMessage);

            if (topicType.equals(TopicType.READING.name())
                    || topicType.equals(TopicType.VIDEO.name())) {
                return "instructor/edit_lesson_topic";
            } else {
                return "instructor/edit_test_topic";
            }
        } catch (Exception e) {
            return RedirectUrlHelper.buildRedirectErrorUrlToTopicEdit(courseId, chapterId, topicId, e.getMessage());
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
                          @RequestParam(required = false) Integer passPoint,
                          @RequestParam(required = false) Integer timeLimit,
                          @RequestParam Long topicId,
                          @AuthenticationPrincipal SecurityUser securityUser,
                          RedirectAttributes redirectAttributes) {
        try {
            TopicDTO topicDTO = new TopicDTO();
            topicDTO.setId(topicId);
            topicDTO.setTitle(title);
            String sanitizedDescription = Jsoup.clean(description, Safelist.relaxed());
            topicDTO.setDescription(sanitizedDescription);
            topicDTO.setTopicType(topicType);

            // Handle different topic types
            switch (topicType) {
                case "READING":
                    handleReadingTopic(topicDTO, readingFile);
                    break;

                case "VIDEO":
                    handleVideoTopic(topicDTO, videoFile, videoUrl);
                    break;

                case "QUIZ":
                case "EXAM":
                    List<QuestionDTO> questionDTOs = handleQuizExamTopic(topicDTO, topicType, questions, passPoint, timeLimit);
                    if (questionDTOs != null && !questionDTOs.isEmpty()) {
                        // Handle questions, update/delete questions in database
                        List<QuestionDTO> afterHandle = questionService.handleTopicQuestions(topicId, questionDTOs, securityUser.getUserId());
                        topicDTO.setQuestions(afterHandle);
                    }
                    break;
            }

            topicService.saveTopicChanges(courseId, chapterId, topicDTO, securityUser.getUserId());
            return RedirectUrlHelper.buildRedirectSuccessUrlToCourseContent(courseId, "Topic updated successfully.");
        } catch (Exception e) {
            return RedirectUrlHelper.buildRedirectErrorUrlToTopicEdit(courseId, chapterId, topicId, e.getMessage());
        }
    }

    @GetMapping("/delete")
    public String deleteTopic(@PathVariable Long courseId,
                                @RequestParam("chapterId") Long chapterId,
                                @RequestParam("topicId") Long topicId,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                RedirectAttributes redirectAttributes) {
        Long instructorId = securityUser.getUserId();
        try {
            topicService.deleteTopic(courseId, chapterId, topicId, instructorId);
            redirectAttributes.addFlashAttribute("successMessage", "Topic deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error when deleting topic content");
        }
        return "redirect:/instructor/course/" + courseId + "/edit/chapters";
    }

    // -- Helper methods --

    /*
     * Handle reading topic.
     * @param topicDTO TopicDTO object to be updated
     * @param readingFile Reading file
     */ 
    private void handleReadingTopic(TopicDTO topicDTO, MultipartFile readingFile) throws Exception {
        if (readingFile != null && !readingFile.isEmpty()) {
            String contentType = readingFile.getContentType();
            if (contentType != null 
            && (contentType.equals("application/pdf") || 
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
                try {
                    String fileUrl = minioService.uploadFile(readingFile, contentType);
                    topicDTO.setFileUrl(fileUrl);
                } catch (Exception e) {
                    Long fileSizeInMB = readingFile.getSize() / (1024 * 1024);
                    throw new Exception("Fail uploading file: " + readingFile.getName() + " size: " + fileSizeInMB + "MB");
                }
            }
        }
    }

    private void handleVideoTopic(TopicDTO topicDTO, MultipartFile videoFile, String videoUrl) throws Exception {
        if (videoFile != null && !videoFile.isEmpty()) {
            String contentType = videoFile.getContentType();
            if (contentType != null && contentType.startsWith("video/")) {
                try {
                    String fileUrl = minioService.uploadFile(videoFile, contentType);
                    topicDTO.setVideoUrl(fileUrl);
                } catch (Exception e) {
                    Long fileSizeInMB = videoFile.getSize() / (1024 * 1024);
                    throw new Exception("Fail uploading file: " + videoFile.getName() + " size: " + fileSizeInMB + "MB");
                }
            }
        } else if (videoUrl != null && !videoUrl.isEmpty()) {
            topicDTO.setVideoUrl(videoUrl);
        }
    }

    /*
     * Handle quiz/exam topic.
     * @param topicDTO TopicDTO object to be updated
     * @param topicType Topic type
     * @param questions Questions in JSON format
     * @param passPoint Pass point
     * @param timeLimit Time limit
     * @param topicId Topic ID
     * @param instructorId Instructor ID
     */
    private List<QuestionDTO> handleQuizExamTopic(TopicDTO topicDTO, String topicType, String questions, Integer passPoint, Integer timeLimit) throws Exception {
        if (questions != null) {
            List<QuestionDTO> questionDTOs = objectMapper.readValue(questions, new TypeReference<List<QuestionDTO>>() {});
            // Sanitize question content
            for (QuestionDTO questionDTO : questionDTOs) {
                String sanitizedContent = Jsoup.clean(questionDTO.getContent(), Safelist.relaxed());
                questionDTO.setContent(sanitizedContent);
            }
            topicDTO.setQuestions(questionDTOs);
            topicDTO.setPassPoint(passPoint);
            if ("EXAM".equals(topicType)) {
                topicDTO.setTimeLimit(timeLimit);
            }

            // Calculate max point
            int maxPoint = questionDTOs.stream().mapToInt(QuestionDTO::getPoint).sum();
            if (passPoint < 0) {
                throw new IllegalArgumentException("Pass point must be greater than 0");
            } else if (maxPoint < passPoint) {
                throw new IllegalArgumentException("Total point must be greater than pass point");
            }
            topicDTO.setMaxPoint(maxPoint);

            return questionDTOs;
        } else {
            return null;
        }
    }
}
