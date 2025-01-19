package funix.sloc_system.service;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Ranking;
import funix.sloc_system.entity.User;
import funix.sloc_system.repository.RankingRepository;
import funix.sloc_system.repository.UserRepository;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.TestResultRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class RankingServiceTest {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RankingRepository rankingRepository;

    @Test
    public void testUpdateRankings() {
        assertDoesNotThrow(() -> rankingService.updateRankings());
    }

    @Test
    public void testCalculateAndSaveRankings() {
        Long courseId = 1L;
        List<Ranking> result = rankingService.calculateAndSaveRankings(courseId);
        assertNotNull(result);
    }

    @Test
    public void testGetRankingsByCourse() {
        Long courseId = 1L;
        List<Ranking> rankings = rankingService.getRankingsByCourse(courseId);
        assertNotNull(rankings);
    }

    @Test
    public void testGetRankingsByUserAndCourse() {
        Long userId = 4L;
        Long courseId = 1L;

        User user = userRepository.findById(userId).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);
        Ranking ranking = new Ranking(null, user, course, 0.0, 1);
        rankingRepository.save(ranking);
        Ranking result = rankingService.getRankingsByUserAndCourse(userId, courseId);
        assertNotNull(result);
    }

    @Test
    public void testGetRankingsByUser() {
        Long userId = 4L;
        List<Ranking> rankings = rankingService.getRankingsByUser(userId);
        assertNotNull(rankings);
    }
} 