package funix.sloc_system.mapper;

import funix.sloc_system.dto.RankingDTO;
import funix.sloc_system.entity.Ranking;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RankingMapper {
  public RankingDTO toDTO(Ranking ranking) {
    RankingDTO rankDTO = new RankingDTO();
    if (ranking.getUser() != null) {
      rankDTO.setUserId(ranking.getUser().getId());
      rankDTO.setUserName(ranking.getUser().getUsername());
      rankDTO.setFullName(ranking.getUser().getFullName());
    }
    if (ranking.getCourse() != null) {
      rankDTO.setCourseId(ranking.getCourse().getId());
      rankDTO.setCourseName(ranking.getCourse().getTitle());
    }
    rankDTO.setAllCourseScore(ranking.getAllCourseScore());
    rankDTO.setRankPosition(ranking.getRankPosition());
    return rankDTO;
  }

  public List<RankingDTO> toDTOs(List<Ranking> rankings) {
    if (rankings == null) {
      return new ArrayList<>();
    }
    return rankings.stream()
      .map(ranking -> toDTO(ranking))
      .collect(Collectors.toList());
  }
}
