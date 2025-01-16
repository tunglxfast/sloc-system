package funix.sloc_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import funix.sloc_system.entity.ScoreWeight;
import funix.sloc_system.repository.ScoreWeightRepository;

@Service
public class ScoreWeightService {
  public static final Double DEFAULT_QUIZ_WEIGHT = 0.4;
  public static final Double DEFAULT_EXAM_WEIGHT = 0.6;

  @Autowired
  private ScoreWeightRepository scoreWeightRepository;

  public ScoreWeight getScoreWeightByCourseId(Long courseId) {
    return scoreWeightRepository.findByCourseId(courseId).orElse(null);
  }

  public ScoreWeight saveScoreWeight(Long courseId, double quizWeight, double examWeight) {
    ScoreWeight scoreWeight = getScoreWeightByCourseId(courseId);
    if (scoreWeight == null) {
      scoreWeight = new ScoreWeight();
      scoreWeight.setCourseId(courseId);
    }
    double totalWeight = quizWeight + examWeight;
    double quizRate = Math.round(quizWeight / totalWeight * 10) / 10.0;
    double examRate = 1.0 - quizRate;
    scoreWeight.setQuizWeight(quizRate);
    scoreWeight.setExamWeight(examRate);
    return scoreWeightRepository.save(scoreWeight);
  }

  public ScoreWeight saveDefaultScoreWeight(Long courseId) {
    ScoreWeight scoreWeight = getScoreWeightByCourseId(courseId);
    if (scoreWeight == null) {
      scoreWeight = new ScoreWeight();
      scoreWeight.setCourseId(courseId);
    }
    scoreWeight.setQuizWeight(DEFAULT_QUIZ_WEIGHT);
    scoreWeight.setExamWeight(DEFAULT_EXAM_WEIGHT);
    return scoreWeightRepository.save(scoreWeight);
  }
}
