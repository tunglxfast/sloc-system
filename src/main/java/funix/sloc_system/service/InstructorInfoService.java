package funix.sloc_system.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import org.checkerframework.checker.units.qual.s;
import org.springframework.beans.factory.annotation.Autowired;

import funix.sloc_system.entity.InstructorInfo;
import funix.sloc_system.repository.InstructorInfoRepository;

@Service
public class InstructorInfoService {

  private static final String AVATAR_LOCAL = "/img/instructor_avatars/";
  @Autowired
  private InstructorInfoRepository instructorInfoRepository;

  public InstructorInfo getInstructorInfoByUserId(Long userId) {
    return instructorInfoRepository.findByUserId(userId).orElse(null);
  }

  public InstructorInfo saveInstructorInfo(InstructorInfo instructorInfo) {
    return instructorInfoRepository.save(instructorInfo);
  }

  public void updateInstructorInfo(Long instructorId, String name, String email, String phone, String sanitizedBio,
      MultipartFile avatar) throws IOException {
    InstructorInfo instructorInfo = getInstructorInfoByUserId(instructorId);
    if (instructorInfo == null) {
      throw new RuntimeException("Instructor info not found");
    }
    if (name != null && !name.isBlank()) {
      instructorInfo.setName(name);
    }
    if (email != null && !email.isBlank()) {
      instructorInfo.setEmail(email);
    }
    if (phone != null && !phone.isBlank()) {
      instructorInfo.setPhone(phone);
    }
    if (sanitizedBio != null && !sanitizedBio.isBlank()) {
      instructorInfo.setBio(sanitizedBio);
    }
    if (avatar != null && !avatar.isEmpty()) {
      String thumbnailUrl = saveAvatar(avatar);
      if (thumbnailUrl != null && !thumbnailUrl.isBlank()){
        instructorInfo.setAvatarUrl(thumbnailUrl);
      }
    }
    instructorInfoRepository.save(instructorInfo);
  }

  public InstructorInfo createInstructorInfo(Long userId, String name, String email, String phone, String bio, MultipartFile avatar) throws IOException {
    InstructorInfo instructorInfo = new InstructorInfo();
    instructorInfo.setUserId(userId);
    instructorInfo.setName(name);
    instructorInfo.setEmail(email);
    instructorInfo.setPhone(phone);
    instructorInfo.setBio(bio);

    if (avatar != null && !avatar.isEmpty()) {
      String thumbnailUrl = saveAvatar(avatar);
      if (thumbnailUrl != null && !thumbnailUrl.isBlank()){
        instructorInfo.setAvatarUrl(thumbnailUrl);
      }
    }
    return instructorInfoRepository.save(instructorInfo);
  }


  private String saveAvatar(MultipartFile file) throws NullPointerException, IOException {
    String uuid = UUID.randomUUID().toString();
    if (!file.isEmpty()) {
        String fileName = String.format("avatar-%s.jpg", uuid);
        String absolutePath = Paths.get("").toAbsolutePath() + "/src/main/resources/static" + AVATAR_LOCAL;
        File saveFile = new File(absolutePath + fileName);
        file.transferTo(saveFile);
        return AVATAR_LOCAL + fileName;
    } else {
        return null;
    }
  }
}

