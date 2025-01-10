package funix.sloc_system.controller;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.TestResultDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.dto.TopicDiscussionDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.StudyProcess;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.mapper.TestResultMapper;
import funix.sloc_system.mapper.TopicMapper;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.*;
import funix.sloc_system.util.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/courses")
public class CourseLearningController {
    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private DTOService dtoService;

    @Autowired
    private AppUtil appUtil;

    @Autowired
    private TestResultService testResultService;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private TestResultMapper testResultMapper;

    @Autowired
    private TopicDiscussionService topicDiscussionService;

    @Autowired
    private StudyProcessService studyProcessService;

    @Autowired
    private CategoryService categoryService;

    private void addCategoriesToModel(Model model) {
        model.addAttribute("categories", categoryService.findAllCategoriesDTO());
    }

    /*
     * Show all courses.
     */
    @GetMapping(value = {"", "/", "/search"})
    public String listCourses(@RequestParam(required = false) String title, 
                            @RequestParam(required = false) String category,
                            @RequestParam(defaultValue = "0") int page, 
                            @RequestParam(defaultValue = "10") int size, 
                            @AuthenticationPrincipal SecurityUser securityUser,
                            Model model) 
                            {
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> coursePage = courseService.getCoursesPagination(title, category, pageable);
    
        List<CourseDTO> courseDTOList = courseMapper.toDTO(coursePage.getContent());
        List<Long> enrolledCourseIds = enrollmentService.getEnrolledCourseIds(securityUser.getUserId());

        model.addAttribute("courses", courseDTOList);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalItems", coursePage.getTotalElements());
        model.addAttribute("searchTitle", title);
        model.addAttribute("searchCategory", category);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        addCategoriesToModel(model);
        return "courses";
    }

    /**
     * Course General View
     * @param courseId
     * @param securityUser
     * @param model
     * @return
     */
    @GetMapping(value = {"/{id}" ,"/{id}/general"})
    public String viewCourseGeneral(@PathVariable("id") Long courseId,
                                @AuthenticationPrincipal SecurityUser securityUser, 
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (!appUtil.isCourseReady(courseId)) {
            redirectAttributes.addAttribute("errorMessage", "Course not found.");
            return "redirect:/courses";
        }

        Course course = courseService.findById(courseId);
        User user = userService.findById(securityUser.getUserId());
        if (course != null && user != null) {
            boolean isEnrolled = enrollmentService.checkEnrollment(user, course);            
            CourseDTO courseDTO = dtoService.getAvailableCourseDTO(courseId);

            List<TestResultDTO> testResults = appUtil.getCourseTestsResult(user.getId(), courseId);
            StudyProcess studyProcess = studyProcessService.findByUserIdAndCourseId(user.getId(), courseId);
            Integer finalScore = studyProcess.getFinalScore();
            Boolean isFinalPassed = studyProcess.getPassCourse();
            Long lastViewTopicId = studyProcess.getLastViewTopic();
            String processAssessment = studyProcess.getProgressAssessment();
            Double learningProgress = studyProcess.getLearningProgress();
            Integer studyingTopicSeq = null;
            Integer studyingChapterSeq = null;
            if (lastViewTopicId != null) {
                Topic lastViewTopic = topicService.findById(lastViewTopicId);
                if (lastViewTopic != null) {
                    studyingTopicSeq = lastViewTopic.getSequence();
                    studyingChapterSeq = lastViewTopic.getChapter().getSequence();
                }
            }

            model.addAttribute("course", courseDTO);
            model.addAttribute("isEnrolled", isEnrolled);
            model.addAttribute("processes", testResults);
            model.addAttribute("learningProgress", learningProgress);
            model.addAttribute("finalScore", finalScore);
            model.addAttribute("finalPass", isFinalPassed);
            model.addAttribute("processAssessment", processAssessment);
            model.addAttribute("lastTopic", studyingTopicSeq);
            model.addAttribute("lastChapter", studyingChapterSeq);
            return "course/general";
        } else {
            return "redirect:/courses";
        }
    }

    @GetMapping("/{courseId}/enroll")
    public String enrollCourse(@PathVariable Long courseId, @AuthenticationPrincipal SecurityUser securityUser, Model model) {
        if (!appUtil.isCourseReady(courseId)) {
            return "redirect:/courses";
        }

        Course course = courseService.findById(courseId);
        User user = userService.findById(securityUser.getUserId());
        if (course != null && user != null) {
            String response  = enrollmentService.enrollCourse(user, course);
            if (response.equalsIgnoreCase("Register successfully")) {
                return String.format("redirect:/courses/%d/1_1", courseId);
            } else {
                return String.format("redirect:/courses?error=%s",response);
            }
        } else {
            return "redirect:/courses";
        }
    }

    @GetMapping("/{courseId}/{chapterNumber}_{topicNumber}")
    public String viewCourseDetail(
            @PathVariable Long courseId,
            @PathVariable int chapterNumber,
            @PathVariable int topicNumber,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model) {
        if (!appUtil.isCourseReady(courseId)) {
            return "redirect:/courses";
        }

        User user = userService.findById(securityUser.getUserId());
        Course course = courseService.findById(courseId);
        if (course == null || user == null ) {
            return "redirect:/courses";
        }
        // Check topic exist
        Topic topic = topicService.findByChapterAndTopicSequence(courseId, chapterNumber, topicNumber);
        // Check already enroll
        boolean isEnrolled = enrollmentService.checkEnrollment(user, course);
        if (!isEnrolled || topic == null) {
            return String.format("redirect:/courses/%d", courseId);
        }

        // Save the topic that the student recently viewed
        studyProcessService.saveLastViewTopic(user.getId(), courseId, topic.getId());

        TopicDTO topicDTO = topicMapper.toDTO(topic);
        topicDTO.setQuestions(dtoService.getAvailableQuestions(topicDTO));

        String nextTopicUrl = appUtil.getNextTopicUrl(topic.getId(), courseId);
        String previousTopicUrl = appUtil.getPreviousTopicUrl(topic.getId(), courseId);

        model.addAttribute("topic", topicDTO);
        model.addAttribute("courseId", courseId);
        model.addAttribute("nextTopicUrl", nextTopicUrl);
        model.addAttribute("previousTopicUrl", previousTopicUrl);
        
        TestResultDTO testResult = testResultService.findByUserIdAndTopicId(user.getId(), topic.getId());
        if (testResult != null) {
            model.addAttribute("result", testResult);
        }
        TopicType topicType = topic.getTopicType();
        if (topicType.equals(TopicType.EXAM)){
            return "course/topic_exam";
        } else if (topicType.equals(TopicType.QUIZ)) {
            return "course/topic_quiz";
        } else {
            List<TopicDiscussionDTO> discussions = topicDiscussionService.getDiscussionsByTopicId(topicDTO.getId());
            model.addAttribute("discussions", discussions);
            return "course/topic_lesson";
        }
    }

    @PostMapping("{courseId}/quiz/submit")
    public String submitQuiz(@PathVariable Long courseId,
                             @RequestParam("quizId") Long quizId,
                             @RequestParam Map<String, String> answers,
                             @AuthenticationPrincipal SecurityUser securityUser,
                             Model model) {

        if (!appUtil.isCourseReady(courseId)) {
            return "redirect:/courses";
        }

        TestResultDTO resultDTO = testResultService.calculateScore(securityUser.getUserId(), quizId, answers);
        
        Topic topic = topicService.findById(quizId);
        if (topic != null) {
            // try to calculate final score after finish a test
            studyProcessService.calculateAndSaveFinalResult(securityUser.getUserId(), courseId, quizId);

            TopicDTO topicDTO = topicMapper.toDTO(topic);
            topicDTO.setQuestions(dtoService.getAvailableQuestions(topicDTO));
    
            String nextTopicUrl = appUtil.getNextTopicUrl(topic.getId(), courseId);
            String previousTopicUrl = appUtil.getPreviousTopicUrl(topic.getId(), courseId);

            model.addAttribute("result", resultDTO);
            model.addAttribute("topic", topicDTO);
            model.addAttribute("courseId", courseId);
            model.addAttribute("nextTopicUrl", nextTopicUrl);
            model.addAttribute("previousTopicUrl", previousTopicUrl);

        }
        return "course/topic_quiz";
    }

    @PostMapping("/{courseId}/exam/submit")
    public String submitExam(@PathVariable Long courseId,
                             @RequestParam Long examId,
                             @RequestParam Map<String, String> answers,
                             @AuthenticationPrincipal SecurityUser securityUser,
                             Model model) {

        if (!appUtil.isCourseReady(courseId)) {
            return "redirect:/courses";
        }

        TestResultDTO result = testResultService.calculateScore(securityUser.getUserId(), examId, answers);

        Topic exam = topicService.findById(examId);

        if (exam != null) {
            // try to calculate final score after finish a test
            studyProcessService.calculateAndSaveFinalResult(securityUser.getUserId(), courseId, examId);

            TopicDTO examDTO = topicMapper.toDTO(exam);
            examDTO.setQuestions(dtoService.getAvailableQuestions(examDTO));

            String nextTopicUrl = appUtil.getNextTopicUrl(exam.getId(), courseId);
            String previousTopicUrl = appUtil.getPreviousTopicUrl(exam.getId(), courseId);

            model.addAttribute("result", result);
            model.addAttribute("topic", examDTO);
            model.addAttribute("courseId", courseId);
            model.addAttribute("nextTopicUrl", nextTopicUrl);
            model.addAttribute("previousTopicUrl", previousTopicUrl);

        }
        return "course/topic_exam";
    }
}
