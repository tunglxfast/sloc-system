package funix.sloc_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import funix.sloc_system.entity.ScoreWeight;
import funix.sloc_system.repository.ScoreWeightRepository;

@Service
public class ScoreWeightService {
  private static final Double DEFAULT_QUIZ_WEIGHT = 0.4;
  private static final Double DEFAULT_EXAM_WEIGHT = 0.6;

  @Autowired
  private ScoreWeightRepository scoreWeightRepository;

  public ScoreWeight getScoreWeightByCourseId(Long courseId) {
    return scoreWeightRepository.findByCourseId(courseId).orElse(null);
  }

  public ScoreWeight saveScoreWeight(Long courseId, Double quizWeight, Double examWeight) {
    ScoreWeight scoreWeight = getScoreWeightByCourseId(courseId);
    if (scoreWeight == null) {
      scoreWeight = new ScoreWeight();
      scoreWeight.setCourseId(courseId);
    }
    Double totalWeight = quizWeight + examWeight;
    Double quizWeightRate = Math.round(quizWeight / totalWeight * 10) / 10.0;
    Double examWeightRate = 1.0 - quizWeightRate;
    scoreWeight.setQuizWeight(quizWeightRate);
    scoreWeight.setExamWeight(examWeightRate);
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
