package funix.sloc_system.service;

import funix.sloc_system.entity.InstructorInfo;
import funix.sloc_system.repository.InstructorInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class InstructorInfoServiceTest {

    @Autowired
    private InstructorInfoService instructorInfoService;

    @Autowired
    private InstructorInfoRepository instructorInfoRepository;

    @Test
    public void testGetInstructorInfoByUserId() {
        InstructorInfo instructorInfo = instructorInfoService.getInstructorInfoByUserId(3L);
        assertNotNull(instructorInfo);
    }

    @Test
    public void testSaveInstructorInfo() {
        InstructorInfo instructorInfo = new InstructorInfo();
        instructorInfo.setUserId(1L);
        instructorInfo.setName("John Doe");
        instructorInfo.setEmail("john.doe@example.com");
        instructorInfo.setPhone("123456789");
        instructorInfo.setBio("Bio of John Doe");

        InstructorInfo savedInfo = instructorInfoService.saveInstructorInfo(instructorInfo);
        assertNotNull(savedInfo);
        assertEquals("John Doe", savedInfo.getName());
    }

    @Test
    public void testUpdateInstructorInfo() throws IOException {
        InstructorInfo instructorInfo = instructorInfoService.getInstructorInfoByUserId(3L);

        instructorInfoService.updateInstructorInfo(instructorInfo.getUserId(), "Jane Doe", "jane.doe@example.com", "987654321", "Updated bio", null);
        
        InstructorInfo updatedInfo = instructorInfoService.getInstructorInfoByUserId(instructorInfo.getUserId());
        assertEquals("Jane Doe", updatedInfo.getName());
        assertEquals("jane.doe@example.com", updatedInfo.getEmail());
    }

    @Test
    public void testUpdateInstructorInfo_WrongId() throws IOException {
        assertThrows(RuntimeException.class, () ->
                instructorInfoService.updateInstructorInfo(
                999L, "Jane Doe", "jane.doe@example.com",
                "987654321", "Updated bio", null)
        );
    }

    @Test
    public void testCreateInstructorInfo() throws IOException {
        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.png", "image/png", new byte[1]) {
            @Override
            public void transferTo(File dest){
                // do nothing
            }
        };
        InstructorInfo createdInfo = instructorInfoService.createInstructorInfo(2L, "Alice Smith", "alice.smith@example.com", "123456789", "Bio of Alice", avatar);
        
        assertNotNull(createdInfo);
        assertEquals("Alice Smith", createdInfo.getName());
    }
} 