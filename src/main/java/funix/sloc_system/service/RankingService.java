package funix.sloc_system.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Ranking;
import funix.sloc_system.entity.User;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.RankingRepository;
import funix.sloc_system.repository.TestResultRepository;
import funix.sloc_system.repository.UserRepository;
import jakarta.annotation.PostConstruct;

@Service
public class RankingService {

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RankingRepository rankingRepository;

    // run every day at 00:00
    @Scheduled(cron = "0 0 0 * * ?") 
    public void updateRankings() {
        List<Long> courseIds = getAllCourseIds();

        for (Long courseId : courseIds) {
            calculateAndSaveRankings(courseId);
        }
    }

    // run when start app
    @PostConstruct
    public void updateRankingsOnStartup() {
        updateRankings();
    }

    @Transactional
    public List<Ranking> calculateAndSaveRankings(Long courseId) {
        List<Object[]> results = testResultRepository.calculateWeightedTotalScores(courseId);

        User userHolder;
        Course courseHolder;
        List<Ranking> rankings = new ArrayList<>();
        int rank = 1;

        for (Object[] result : results) {
            Long userId = ((Long) result[0]).longValue();
            userHolder = userRepository.findById(userId).orElse(null);
            Long courseIdResult = ((Long) result[1]).longValue();
            courseHolder = courseRepository.findById(courseIdResult).orElse(null);
            double totalScore = ((Double) result[2]).doubleValue();

            if (userHolder == null || courseHolder == null) {
                continue;
            }
            
            Ranking ranking = rankingRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);
            if (ranking == null) {
                ranking = new Ranking();
            }
            ranking.setUser(userHolder);
            ranking.setCourse(courseHolder);
            ranking.setAllCourseScore(totalScore);
            ranking.setRankPosition(rank);
            rankings.add(ranking);

            rank++;
        }

        return rankingRepository.saveAll(rankings);
    }

    public List<Ranking> getRankingsByCourse(Long courseId) {
        return rankingRepository.findByCourseIdOrderByAllCourseScoreDesc(courseId);
    }

    public Ranking getRankingsByUserAndCourse(Long userId, Long courseId) {
        return rankingRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);
    }

    public List<Ranking> getRankingsByUser(Long userId) {
        return rankingRepository.findByUserId(userId);
    }

    private List<Long> getAllCourseIds() {
        return courseRepository.findAll().stream()
                .map(Course::getId)
                .collect(Collectors.toList());
    }
}